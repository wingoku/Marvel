package com.wingoku.marvel.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.wingoku.marvel.MainActivity;
import com.wingoku.marvel.R;
import com.wingoku.marvel.adapters.ComicsListRecyclerViewAdapter;
import com.wingoku.marvel.eventBus.OnComicListCardClickedEvent;
import com.wingoku.marvel.eventBus.OnComicsFetchFailureEvent;
import com.wingoku.marvel.eventBus.OnComicsFetchSuccessEvent;
import com.wingoku.marvel.eventBus.OnComicsFilterTaskCompleteEvent;
import com.wingoku.marvel.eventBus.OnComicsFilterTaskFailureEvent;
import com.wingoku.marvel.eventBus.OnMarvelComicListCreationCompleteEvent;
import com.wingoku.marvel.eventBus.OnMarvelComicListCreationFailureEvent;
import com.wingoku.marvel.fragments.presenters.ComicListFragmentPresenter;
import com.wingoku.marvel.interfaces.components.ComicListFragmentComponent;
import com.wingoku.marvel.interfaces.components.DaggerComicListFragmentComponent;
import com.wingoku.marvel.models.MarvelComic;
import com.wingoku.marvel.models.MarvelComics;
import com.wingoku.marvel.modules.ContextModule;
import com.wingoku.marvel.utils.Constants;
import com.wingoku.marvel.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.wingoku.marvel.utils.Constants.MAX_NUMBER_OF_COMICS_TO_FETCH;

/**
 * Created by Umer on 4/8/2017.
 */

public class ComicListFragment extends Fragment {

    @BindView(R.id.list_comics)
    RecyclerView mComicsListRecyclerView;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.progressBar)
    CircleProgressBar mProgressBar;

    @BindView(R.id.app_bar_layout)
    AppBarLayout mAppbarLayout;

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbarLayout;

    @BindView(R.id.layout_toolbar_pageCount)
    LinearLayout mToolbarPageCountLayout;

    @BindView(R.id.tv_toolbarPageCount)
    TextView mToolbarPageCountTV;

    private View mView;
    private SearchView mSearchView;

    private final int mLimit = 6;
    private int mOffset = 6;
    private int mNumberOfFetchedComics;
    private int mComicCountWithInBudget;
    private double mBudget;
    private boolean mNetworkRequestEnqueue;
    private boolean isAppbarExpanded;
    private boolean isBudgetModeActive;
    private ComicsListRecyclerViewAdapter mAdapter;

    // this will tell Dagger to call ComicListFragmentPresenter constructor to inject dependencies. NOTE that
    // the ComicListFragmentPresetner class must have a constructor with all the required dependencies as arguments
    // & the constructor must be annotated with @Inject. This is called constructor injection
    @Inject
    ComicListFragmentPresenter mComicListFragmentPresenter;

    private ComicListFragmentComponent mComicListFragmentComponent;


    /**
     * COMIC LIST FRAGMENT ONSAVEDINSTANCE KEYS
     */

    private final String FETCHED_COMICS_COUNT = "fetchedComicsCount";
    private final String IS_BUDGET_MODE_ACTIVE = "isBudgetModeActive";
    private final String IS_APP_BAR_EXPANDED = "isAppbarExpanded";
    private final String COMICS_BUDGET = "comicsBudget";
    private final String MARVEL_COMICS_PARCELABLE_OBJECT = "marvelComicsParcelableObject";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Building dagger DI component
        mComicListFragmentComponent = DaggerComicListFragmentComponent
                .builder()
                .contextModule(new ContextModule(getContext()))
                .build();
        mComicListFragmentComponent.inject(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Timber.d("onActivityCreated");
        if(savedInstanceState != null) {
            Timber.d("onActivityCreated::savedInstanceState");
            mNumberOfFetchedComics = savedInstanceState.getInt(FETCHED_COMICS_COUNT);
            MarvelComics savedMarvelComics = savedInstanceState.getParcelable(MARVEL_COMICS_PARCELABLE_OBJECT);
            mComicListFragmentPresenter.getMarvelComicsList().addAll(savedMarvelComics.getMarvelComicList());
            updateComicAdapter(mComicListFragmentPresenter.getMarvelComicsListSize());
            isBudgetModeActive = savedInstanceState.getBoolean(IS_BUDGET_MODE_ACTIVE);
            isAppbarExpanded = savedInstanceState.getBoolean(IS_APP_BAR_EXPANDED);
            mBudget = savedInstanceState.getDouble(COMICS_BUDGET);

            if(mNetworkRequestEnqueue) {
                initiateComicFetchingCall();
            }
        }

        if(isBudgetModeActive) {
            Timber.d("comic count in budget mode: %s", mComicCountWithInBudget);
            updateComicAdapter(mComicCountWithInBudget);
            mComicListFragmentPresenter.filterComicsAccordingToBudget(mBudget*1.0);
            showEndBudgetModeSnackBar();
        }
        else {
            updateComicAdapter(mComicListFragmentPresenter.getMarvelComicsListSize());
        }

        if(mComicListFragmentPresenter.getMarvelComicsListSize() == 0 && !mNetworkRequestEnqueue) {
            initiateComicFetchingCall();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_comics_list, container, false);

        //must bind for View Injection
        ButterKnife.bind(this, mView);

        // register event bus to receive events
        EventBus.getDefault().register(this);

        getMainActivity().setSupportActionBar(mToolbar);
        if(getMainActivity().getSupportActionBar() != null) {
            setHasOptionsMenu(true);
        }

        addOffsetListenerToAppbarLayout();
        readFromCacheIfNetworkNotAvailable();

        initRecyclerView();
        populateRecyclerViewAdapter(mComicListFragmentPresenter.getMarvelComicsList());
        addScrollListenerToRecyclerView();

        return mView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Timber.d("onSavedInstance");
        outState.putParcelable(MARVEL_COMICS_PARCELABLE_OBJECT, mComicListFragmentPresenter.getMarvelComics());
        outState.putInt(FETCHED_COMICS_COUNT, mNumberOfFetchedComics);
        outState.putBoolean(IS_BUDGET_MODE_ACTIVE, isBudgetModeActive);
        outState.putBoolean(IS_APP_BAR_EXPANDED, isAppbarExpanded);
        outState.putDouble(COMICS_BUDGET, mBudget);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu, menu);

        final MenuItem actionMenuItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) actionMenuItem.getActionView();
        mSearchView.setInputType(InputType.TYPE_CLASS_NUMBER);
        setQueryListenerOnSearchView(actionMenuItem);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                mAppbarLayout.setExpanded(false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        Timber.e("onDestroyView");
        // unregister event bus
        EventBus.getDefault().unregister(this);
        if(getMainActivity() != null) {
            getMainActivity().onNetworkProcessEnded();
        }
        // clear off all the active observables
        mComicListFragmentPresenter.clearOffObservables();
        mNetworkRequestEnqueue = false;
        super.onDestroyView();
    }

    /**
     * Add listener to AppbarLayout
     */
    private void addOffsetListenerToAppbarLayout() {
        mAppbarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                isAppbarExpanded = (verticalOffset == 0);
                mCollapsingToolbarLayout.setTitleEnabled(isAppbarExpanded);
            }
        });
    }

    /**
     * Read MarvelComic from DB when offline
     */
    private void readFromCacheIfNetworkNotAvailable() {
        if(!Utils.isNetworkAvailable(getContext())) {
            mComicListFragmentPresenter.getMarvelComicsList().addAll(mComicListFragmentPresenter.getCachedMarvelComicsFromDB());
        }
    }

    /**
     * Initialize RecyclerView and assign the LayoutManger
     */
    private void initRecyclerView() {
        // show two columns in the RecyclerView grid
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), getResources().getInteger(R.integer.comic_grid_columns));
        mComicsListRecyclerView.setLayoutManager(layoutManager);
    }

    /**
     * Send required info in the RecyclerView's adapter for displaying data
     */
    private void populateRecyclerViewAdapter(List<MarvelComic> marvelComicList) {
        mAdapter = new ComicsListRecyclerViewAdapter(getContext(), marvelComicList, R.layout.layout_comic_card, mComicListFragmentPresenter.getPicassoInstance());
        mComicsListRecyclerView.setAdapter(mAdapter);
    }

    private void onFailureInDataFetching() {
        Snackbar snackbar = Utils.initSnackbar(getContext(), mView);
        snackbar.setText(getString(R.string.failure_message)).show();
    }

    private void initiateComicFetchingCall() {
        if(mNumberOfFetchedComics < MAX_NUMBER_OF_COMICS_TO_FETCH && !mNetworkRequestEnqueue && !isBudgetModeActive) {
            mProgressBar.setVisibility(View.VISIBLE);
            mNetworkRequestEnqueue = true;
            if(getMainActivity() != null) {
                getMainActivity().onNetworkProcessStarted();
            }

            int currentTime = (int)(System.currentTimeMillis()/1000);
            String hash = Utils.generateMD5Hash(getContext(), currentTime + Constants.MARVEL_PRIVATE_KEY + Constants.MARVEL_PUBLIC_KEY);
            mComicListFragmentPresenter.fetchComics(mLimit, mOffset, Constants.MARVEL_PUBLIC_KEY, hash, currentTime + "");
        }
    }

    private void addScrollListenerToRecyclerView() {
        mComicsListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Timber.d("onScrollStateChanged : isBudgetModeActive: %s mNetworkRequestEnqueed: %s", isBudgetModeActive, mNetworkRequestEnqueue);
                if(!recyclerView.canScrollVertically(RecyclerView.VERTICAL)) {
                    Timber.d("onScrollStateChanged:: can't scroll vertically");
                    initiateComicFetchingCall();
                }
            }
        });
    }

    /**
     * Update Comics list size in the adaper. This method will be used to manipulate the number of comics to be shown for a specific budget
     * @param comicListSize adapter list size
     */
    private void updateComicAdapter(int comicListSize) {
        Timber.d("updated adapter size: %s", comicListSize);
        mComicCountWithInBudget = comicListSize;
        mAdapter.setComicListSize(comicListSize);
        mAdapter.notifyDataSetChanged();
    }

    private void showEndBudgetModeSnackBar() {
        if(mView == null)
            return;

        Snackbar snackbar = Utils.initSnackbar(getContext(), mView);
        snackbar.setText(R.string.string_end_budget_mode);
        snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(getString(R.string.ok_string), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Timber.d("end budget mode now!");
                isBudgetModeActive = false;
                mToolbarPageCountLayout.setVisibility(View.INVISIBLE);
                updateComicAdapter(mComicListFragmentPresenter.getMarvelComicsListSize());
            }
        });
        snackbar.show();
    }

    private void setQueryListenerOnSearchView(final MenuItem actionMenuItem) {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Timber.d("Budget Entered By User: %s", query);
                mBudget = Double.valueOf(query);
                mComicListFragmentPresenter.filterComicsAccordingToBudget(mBudget*1.0);
                if(!mSearchView.isIconified()) {
                    mSearchView.setIconified(true);
                }
                actionMenuItem.collapseActionView();
                mAppbarLayout.setExpanded(isAppbarExpanded);

                showEndBudgetModeSnackBar();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    private MainActivity getMainActivity() {
        return ((MainActivity) getActivity());
    }

    @Subscribe
    public void onComicListCellClicked(OnComicListCardClickedEvent event) {
        Timber.d("onComicListCellClicked");
        EventBus.getDefault().post(mComicListFragmentPresenter.getMarvelComicsList().get(event.getClickedItemPosition()));
    }

    @Subscribe
    public void onComicsFetchedSuccessfully(OnComicsFetchSuccessEvent event) {
        if(getMainActivity() != null) {
            getMainActivity().onNetworkProcessEnded();
        }
        mOffset += mLimit;

        mNumberOfFetchedComics+=mLimit;
        mNetworkRequestEnqueue = false;
        mProgressBar.setVisibility(View.GONE);
        mComicListFragmentPresenter.createComicListFromServerResponse(event.getResultsList());
    }

    @Subscribe
    public void onComicsFetchingFailed(OnComicsFetchFailureEvent event) {
        Timber.e("comics Fetching failure message: %s", event.getError());
        mNetworkRequestEnqueue = false;
        if(getMainActivity() != null) {
            getMainActivity().onNetworkProcessEnded();
        }
        onFailureInDataFetching();
    }

    @Subscribe
    public void onComicListCreationComplete(OnMarvelComicListCreationCompleteEvent event) {
        updateComicAdapter(mComicListFragmentPresenter.getMarvelComicsListSize());
    }

    @Subscribe
    public void onComicListCreationFailure(OnMarvelComicListCreationFailureEvent event) {
        Timber.e("onComicListCreationFailure: %s", (event.getError().isEmpty())?getString(R.string.string_no_data_found_on_server):event.getError());
    }

    @Subscribe
    public void onFilterTaskComplete(final OnComicsFilterTaskCompleteEvent event) {
        Timber.d("onFilterTaskComplete");
        mToolbarPageCountLayout.setVisibility(View.VISIBLE);
        mToolbarPageCountTV.setText(event.getPageCount()+"");
        isBudgetModeActive = true;
        updateComicAdapter(event.getComicsCount());
    }

    @Subscribe
    public void onFilterTaskFailure(OnComicsFilterTaskFailureEvent event) {
        Timber.e("onFilterTaskComplete %s", event.getError());
    }
}
