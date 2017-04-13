package com.wingoku.marvel.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wingoku.marvel.R;
import com.wingoku.marvel.models.MarvelComic;
import com.wingoku.marvel.utils.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Umer on 4/8/2017.
 */

public class ComicDetailFragment extends Fragment{

    @BindView(R.id.tv_title_value)
    TextView mComicNameTV;

    @BindView(R.id.tv_author_value)
    TextView mComicAuthorTV;

    @BindView(R.id.tv_price_value)
    TextView mComicPriceTV;

    @BindView(R.id.tv_description_value)
    TextView mComicDescriptionTV;

    @BindView(R.id.tv_pageCount_value)
    TextView mComicPageCountTV;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbarLayout;

    @BindView(R.id.app_bar_layout)
    AppBarLayout mAppbarLayout;

    // might get used by Snackbar upon image loading failure
    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_comic_details, container, false);

        //must bind for View Injection
        ButterKnife.bind(this, mView);

        // getting the comic object that contains all the details about the user selected comic
        MarvelComic marvelComic = getArguments().getParcelable(Constants.PARCELABLE_MARVEL_COMIC_OBJECT);

        // if there is no data to be shown, then don't execute this fragment further
        if(marvelComic == null) {
            getActivity().onBackPressed();
            return mView;
        }

        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);

        if(((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
            setHasOptionsMenu(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String comicName = marvelComic.getTitle();
        mToolbar.setTitle(comicName);
        addOffsetListenerToAppbarLayout();
        assignDetailDataToTextViews(marvelComic);
        return mView;
    }

    /**
     * Add listener to AppbarLayout
     */
    private void addOffsetListenerToAppbarLayout() {
        mAppbarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                mCollapsingToolbarLayout.setTitleEnabled(verticalOffset == 0);
            }
        });
    }

    /**
     * Assign comic details to the layout views
     * @param marvelComic MarvelComic object containing details about the user tapped comic card
     */
    private void assignDetailDataToTextViews(MarvelComic marvelComic) {
        mComicNameTV.setText(marvelComic.getTitle());
        mComicDescriptionTV.setText(marvelComic.getDescription());
        mComicAuthorTV.setText(marvelComic.getAuthor());
        mComicPageCountTV.setText(marvelComic.getPageCount());
        String price = getString(R.string.string_dollar)+marvelComic.getPrice();
        mComicPriceTV.setText(price);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // id of the back button in toolbar
            case android.R.id.home:
                getActivity().onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
