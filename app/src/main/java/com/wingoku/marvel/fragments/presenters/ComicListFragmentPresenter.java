package com.wingoku.marvel.fragments.presenters;

import android.content.Context;

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

import com.wingoku.marvel.models.MarvelComic;
import com.wingoku.marvel.models.MarvelComics;
import com.wingoku.marvel.models.serverResponse.Item;
import com.wingoku.marvel.models.serverResponse.MarvelResponse;
import com.wingoku.marvel.models.serverResponse.Price;
import com.wingoku.marvel.models.serverResponse.Result;
import com.wingoku.marvel.utils.Constants;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import timber.log.Timber;

/**
 * Created by Umer on 4/12/2017.
 */

public class ComicListFragmentPresenter {

    private MarvelComics mMarvelComics;
    private CompositeDisposable mCompositeDisposable;

    ComicsCacheDBController mComicsCacheDBController;
    Retrofit mRetrofit;
    Picasso mPicasso;
    Context mContext;

    /**
     *
     * @param context Context of activity or prefereably context of the application
     * @param retrofit instance of retrofit
     * @param picasso instance of picasso
     * @param comicsCacheDBController instance of comicsCacheDB
     */
    @Inject //This is called constructor injection
    public ComicListFragmentPresenter (Context context, Retrofit retrofit, Picasso picasso, ComicsCacheDBController comicsCacheDBController) {
        mContext = context;
        mRetrofit = retrofit;
        mPicasso = picasso;
        mComicsCacheDBController = comicsCacheDBController;

        mMarvelComics = new MarvelComics();
        mCompositeDisposable = new CompositeDisposable();

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
        Disposable marvelNetworkDisposable = MarvelAPI.Factory.getInstance(mRetrofit).getComics(apiKey, md5Hash, timeStamp, limit, offset)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<MarvelResponse>() {
                    @Override
                    public void onNext(MarvelResponse response) {
                        if(response == null || response.getData() == null || response.getData().getResults() == null) {
                            comicsFetchingFailure(mContext.getString(R.string.string_no_data_found_on_server));
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
        mCompositeDisposable.add(marvelNetworkDisposable);
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
        Disposable createMarvelComicsListDisposable = Observable.fromIterable(marvelResults)
                .map(new Function<Result, MarvelComic>() {
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
                        if (itemList != null && itemList.size() > 0) {
                            marvelComic.setAuthor(itemList.get(0).getName());
                        }

                        List<Price> priceList = result.getPrices();
                        if (priceList != null && priceList.size() > 0) {
                            marvelComic.setPrice(priceList.get(0).getPrice());
                        }
                        return marvelComic;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<MarvelComic>() {
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

        mCompositeDisposable.add(createMarvelComicsListDisposable);
    }

    /**
     * Filter the comics list according to specified budget
     * @param budget budget amount
     */
    public void filterComicsAccordingToBudget(final double budget) {
        final String PRICE = "price";
        final String PAGE_COUNT = "pageCount";
        final String COMICS_COUNT = "comicsCount";
        Disposable filterDisposable = Observable.fromIterable(getMarvelComicsList())
                .map(new Function<MarvelComic, HashMap<String, Double>>() {
                    HashMap<String, Double> filterComicDataMap = new HashMap<String, Double>();
                    double count = 0;
                    @Override
                    public HashMap<String, Double> apply(@NonNull MarvelComic marvelComic) throws Exception {
                        filterComicDataMap.put(PRICE, Double.valueOf(marvelComic.getPrice()));
                        filterComicDataMap.put(PAGE_COUNT, Double.valueOf(marvelComic.getPageCount()));
                        filterComicDataMap.put(COMICS_COUNT, count++);
                        return filterComicDataMap;
                    }
                })
                .takeWhile(new Predicate<HashMap<String, Double>>() {
                    double sum;
                    @Override
                    public boolean test(@NonNull HashMap<String, Double> map) throws Exception {
                        return (sum+=map.get(PRICE)) < budget;
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<HashMap<String, Double>>() {
                    double count = 0;
                    double pageCount = 0;

                    @Override
                    public void onNext(HashMap<String, Double> map) {
                        Timber.d("filterComicsAccordingToBudget():onNext");
                        pageCount = map.get(PAGE_COUNT);
                        count = map.get(COMICS_COUNT);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("onFilterComicsForBudget:onError() %s", e);
                        EventBus.getDefault().post(new OnComicsFilterTaskFailureEvent(e.getLocalizedMessage()));
                    }

                    @Override
                    public void onComplete() {
                        Timber.d("filterComicsAccordingToBudget():onComplete");
                        EventBus.getDefault().post(new OnComicsFilterTaskCompleteEvent((int)count, (int)pageCount));
                    }
                });

        mCompositeDisposable.add(filterDisposable);
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

    /**
     * Clears off all the observables that are current active.
     * Must call this method upon configuration/orientation change!
     *  READ MORE ABOUT THE DIFFERENCE BETWEEN CLEAR & DISPOSE on RxJava website
     *
     *   Using clear will clear all, but can accept new disposable
            disposables.clear();
         Using dispose will clear all and set isDisposed = true, so it will not accept any new disposable
            disposables.dispose();
     */
    public void clearOffObservables() {
        mCompositeDisposable.clear();
    }
}