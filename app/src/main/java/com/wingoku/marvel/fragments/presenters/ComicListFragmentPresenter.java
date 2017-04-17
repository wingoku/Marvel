package com.wingoku.marvel.fragments.presenters;

import com.squareup.picasso.Picasso;
import com.wingoku.marvel.R;
import com.wingoku.marvel.database.ComicsCacheDBController;
import com.wingoku.marvel.eventbus.OnComicsFetchFailureEvent;
import com.wingoku.marvel.eventbus.OnComicsFetchSuccessEvent;
import com.wingoku.marvel.eventbus.OnComicsFilterTaskCompleteEvent;
import com.wingoku.marvel.eventbus.OnComicsFilterTaskFailureEvent;
import com.wingoku.marvel.eventbus.OnMarvelComicListCreationCompleteEvent;
import com.wingoku.marvel.eventbus.OnMarvelComicListCreationFailureEvent;
import com.wingoku.marvel.fragments.ComicListFragment;
import com.wingoku.marvel.interfaces.MarvelAPI;

import com.wingoku.marvel.interfaces.components.ComicListPresenterComponent;
import com.wingoku.marvel.interfaces.components.DaggerComicListPresenterComponent;
import com.wingoku.marvel.models.MarvelComic;
import com.wingoku.marvel.models.MarvelComics;
import com.wingoku.marvel.models.serverResponse.Item;
import com.wingoku.marvel.models.serverResponse.MarvelResponse;
import com.wingoku.marvel.models.serverResponse.Price;
import com.wingoku.marvel.models.serverResponse.Result;
import com.wingoku.marvel.modules.ContextModule;
import com.wingoku.marvel.utils.Constants;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import timber.log.Timber;

/**
 * Created by Umer on 4/12/2017.
 */

public class ComicListFragmentPresenter {

    private MarvelComics mMarvelComics;
    private ComicListPresenterComponent mComicListPresenterComponent;
    private ComicListFragment mFragment;

    @Inject
    ComicsCacheDBController mComicsCacheDBController;

    @Inject
    Retrofit mRetrofit;

    @Inject
    Picasso mPicasso;

    /**
     * Instantiate {@link ComicListFragmentPresenter}
     * @param fragment {@link ComicListFragment} instance
     */
    public ComicListFragmentPresenter (ComicListFragment fragment) {
        // Building dagger DI component
        mComicListPresenterComponent = DaggerComicListPresenterComponent
                                        .builder()
                                        .contextModule(new ContextModule(fragment.getContext()))
                                        .build();
        mComicListPresenterComponent.inject(this);

        mMarvelComics = new MarvelComics();
        mFragment = fragment;

        mComicsCacheDBController.validateExpiryDateForDBEntry(Constants.MAX_STALE_DAYS);
    }

    /**
     *  Fetch Marvel Comics From Marvel Server
     * @param limit number of comics to fetched in a single network call
     * @param offset number of comics to offset or in other words Number Of Comics that have already been fetched from server
     * @param apiKey Marvel API key
     * @param md5Hash MD5 hash for MarvelServer authentication
     * @param timeStamp Current system time
     */
    public void fetchComics(int limit, int offset, String apiKey, String md5Hash, String timeStamp) {
        Timber.d("Fetch Comics From Server");
        MarvelAPI.Factory.getInstance(mRetrofit).getComics(apiKey, md5Hash, timeStamp, limit, offset)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<MarvelResponse>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(MarvelResponse response) {
                        if(response == null || response.getData() == null || response.getData().getResults() == null) {
                            comicsFetchingFailure(mFragment.getString(R.string.string_no_data_found_on_server));
                            return;
                        }
                        comicsFetchingSuccess(response.getData().getResults());
                    }

                    @Override
                    public void onError(Throwable e) {
                        comicsFetchingFailure(e.getLocalizedMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * call upon comics fetching failure from server
     * @param localizedMessage ERROR MESSAGE
     */
    private void comicsFetchingFailure(String localizedMessage) {
        EventBus.getDefault().post(new OnComicsFetchFailureEvent(localizedMessage));
    }

    /**
     * Call upon success in fetching comics from server.
     * @param marvelResults list containing {@link Result}
     */
    private void comicsFetchingSuccess(List<Result> marvelResults) {
        EventBus.getDefault().post(new OnComicsFetchSuccessEvent(marvelResults));
    }

    /**
     * Generate List of {@link MarvelComic} from the List of {@link Result} fetched from Marvel server
     * @param marvelResults list of {@link Result} fetching server
     */
    public void createComicListFromServerResponse(final List<Result> marvelResults) {
        Observable<MarvelComic> marvelObservable = Observable.fromIterable(marvelResults).map(new Function<Result, MarvelComic>() {
            @Override
            public MarvelComic apply(@NonNull Result result) throws Exception {
                MarvelComic marvelComic = new MarvelComic();
                Timber.d("Comics DB Entry Date: %s", marvelComic.getDBEntryDate());
                marvelComic.setDescription(result.getDescription());
                marvelComic.setTitle(result.getTitle());
                marvelComic.setPageCount(result.getPageCount());
                marvelComic.setThumbnailUrl(result.getThumbnail().getPath());
                marvelComic.setId(result.getId());
                List<Item> itemList = result.getCreators().getItems();
                if(itemList != null && itemList.size() > 0) {
                    marvelComic.setAuthor(itemList.get(0).getName());
                }

                List<Price> priceList = result.getPrices();
                if(priceList != null && priceList.size() > 0) {
                    marvelComic.setPrice(priceList.get(0).getPrice());
                }
                return marvelComic;
            }
        });

        marvelObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<MarvelComic>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(MarvelComic comic) {
                        Timber.d("createComicListFromServerResponse()::onNext");
                        getMarvelComicsList().add(comic);
                        mComicsCacheDBController.insertComic(comic);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("createComicListFromServerResponse()::onError: %s", e);
                        EventBus.getDefault().post(new OnMarvelComicListCreationFailureEvent(e.getLocalizedMessage()));
                    }

                    @Override
                    public void onComplete() {
                        Timber.d("createComicListFromServerResponse()::onComplete");
                        EventBus.getDefault().post(new OnMarvelComicListCreationCompleteEvent());
                    }
                });
    }

    /**
     * Filter the comics list according to specified budget
     * @param budget budget amount
     */
    public void filterComicsAccordingToBudget(final double budget) {
        Timber.d("filterComicsAccordingToBudget()");
        Observable<Integer> filterObservable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                Timber.d("filterComicsAccordingToBudget():subscribe");
                int pageCountOfComicsWithInBudget = 0;
                double totalCost = 0.0;
                for(MarvelComic comic : getMarvelComicsList()) {
                    totalCost += Double.valueOf(comic.getPrice());
                    Timber.d("totalCost: %s budget: %s priceOfComic: %s", totalCost, budget, comic.getPrice());
                    if(totalCost > budget) {
                        break;
                    }

                    pageCountOfComicsWithInBudget += Integer.valueOf(comic.getPageCount());
                    Timber.d("pageCount: %s price: %s comicName: %s totalPages: %s", comic.getPageCount(), comic.getPrice(), comic.getTitle(), pageCountOfComicsWithInBudget);
                    e.onNext(pageCountOfComicsWithInBudget);
                }
                e.onComplete();
            }
        });

        filterObservable.subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    int comicCount = 0;
                    int pageCountOfComicsWithInBudget = 0;

                    @Override
                    public void onSubscribe(Disposable d) {
                        Timber.d("filterComicsAccordingToBudget():onSubscribe");
                    }

                    @Override
                    public void onNext(Integer pageCountOfComicsWithInBudget) {
                        Timber.d("filterComicsAccordingToBudget():onNext");
                        comicCount++;
                        this.pageCountOfComicsWithInBudget = pageCountOfComicsWithInBudget;
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("onFilterComicsForBudget:onError() %s", e);
                        EventBus.getDefault().post(new OnComicsFilterTaskFailureEvent(e.getLocalizedMessage()));
                    }

                    @Override
                    public void onComplete() {
                        Timber.d("filterComicsAccordingToBudget():onComplete");
                        EventBus.getDefault().post(new OnComicsFilterTaskCompleteEvent(comicCount, pageCountOfComicsWithInBudget));
                    }
                });
    }

    /**
     * Get {@link MarvelComics}
     * @return MarvelComics
     */
    public MarvelComics getMarvelComics() {
        return mMarvelComics;
    }

    /**
     * Get {@link MarvelComic} List
     * @return MarvelComic List
     */
    public List<MarvelComic> getMarvelComicsList() {
        return mMarvelComics.getMarvelComicList();
    }

    /**
     * {@link MarvelComic} list size
     * @return MarelComic List size
     */
    public int getMarvelComicsListSize() {
        return mMarvelComics.getMarvelComicList().size();
    }

    /**
     * Get picasso Instance from Network Component used in this presenter
     * @return Picasso instance
     */
    public Picasso getPicassoInstance() {
        return mPicasso;
    }

    /**
     * Rerturns list of {@link MarvelComics} list from DB to be used as offline cache
     * @return List<MarvelComic></>
     */
    public List<MarvelComic> getCachedMarvelComicsFromDB() {
        return mComicsCacheDBController.getRealm().copyFromRealm(mComicsCacheDBController.getAllComics());
    }
}