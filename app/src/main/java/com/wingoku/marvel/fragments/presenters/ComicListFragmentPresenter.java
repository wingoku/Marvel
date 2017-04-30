package com.wingoku.marvel.fragments.presenters;

import android.content.Context;

import com.squareup.picasso.Picasso;
import com.wingoku.marvel.R;
import com.wingoku.marvel.database.ComicsCacheDBController;
import com.wingoku.marvel.eventBus.OnComicsFetchFailureEvent;
import com.wingoku.marvel.eventBus.OnComicsFetchSuccessEvent;
import com.wingoku.marvel.eventBus.OnComicsFilterTaskCompleteEvent;
import com.wingoku.marvel.eventBus.OnComicsFilterTaskFailureEvent;
import com.wingoku.marvel.eventBus.OnMarvelComicListCreationCompleteEvent;
import com.wingoku.marvel.eventBus.OnMarvelComicListCreationFailureEvent;
import com.wingoku.marvel.interfaces.MarvelAPI;

import com.wingoku.marvel.models.MarvelComic;
import com.wingoku.marvel.models.MarvelComics;
import com.wingoku.marvel.models.rxjava.FilterComicsModel;
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
import io.reactivex.functions.BiFunction;
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

    private ComicsCacheDBController mComicsCacheDBController;
    private Retrofit mRetrofit;
    /**
     *
     * @param retrofit instance of retrofit
     * @param picasso instance of picasso
     * @param comicsCacheDBController instance of comicsCacheDB
     */
    @Inject //This is called constructor injection
    public ComicListFragmentPresenter (Retrofit retrofit, Picasso picasso, ComicsCacheDBController comicsCacheDBController) {
        mRetrofit = retrofit;
        mPicasso = picasso;
        mComicsCacheDBController = comicsCacheDBController;

        mMarvelComics = new MarvelComics();
        mCompositeDisposable = new CompositeDisposable();

        mComicsCacheDBController.validateExpiryDateForDBEntry(Constants.MAX_STALE_DAYS);
    }

    private Picasso mPicasso;

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
                            comicsFetchingFailure("");
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
        Disposable filterDisposable = Observable.fromIterable(getMarvelComicsList())
                .map(new Function<MarvelComic, FilterComicsModel>() {
                    @Override
                    public FilterComicsModel apply(MarvelComic marvelComic){
                        Timber.e("map()");
                        FilterComicsModel filterComicsModel = new FilterComicsModel();
                        filterComicsModel.setPrice(Double.valueOf(marvelComic.getPrice()));
                        filterComicsModel.setPageCount(Integer.valueOf(marvelComic.getPageCount()));
                        filterComicsModel.setComicCount(1);
                        return filterComicsModel;
                    }
                })
                .scan(new BiFunction<FilterComicsModel, FilterComicsModel, FilterComicsModel>() {
                    @Override
                    public FilterComicsModel apply(FilterComicsModel previousDataModel, FilterComicsModel newDataModel) throws Exception {
                        Timber.e("scan()");
                        Timber.e("inputPageCount: %s  newValuePageCount: %s", previousDataModel.getPageCount(), newDataModel.getPageCount());
                        FilterComicsModel filterComicsModel = new FilterComicsModel();
                        filterComicsModel.setPrice(previousDataModel.getPrice()+newDataModel.getPrice());
                        filterComicsModel.setPageCount(previousDataModel.getPageCount()+newDataModel.getPageCount());
                        filterComicsModel.setComicCount(previousDataModel.getComicCount()+newDataModel.getComicCount());
                        return filterComicsModel;
                    }
                })
                .takeWhile(new Predicate<FilterComicsModel>() {
                    @Override
                    public boolean test(@NonNull FilterComicsModel filterData) throws Exception {
                        Timber.e("takeWhile()");
                        return filterData.getPrice() < budget;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<FilterComicsModel>() {
                    int count = 0;
                    int pageCount = 0;
                    @Override
                    public void onNext(FilterComicsModel filteredData) {
                        Timber.e("onNext()");
                        count = filteredData.getComicCount();
                        pageCount = filteredData.getPageCount();
                        Timber.e("sum %s  pageCount %s  ComicCount: %s", filteredData.getPrice(), filteredData.getPageCount(), filteredData.getComicCount());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        EventBus.getDefault().post(new OnComicsFilterTaskFailureEvent(e.getLocalizedMessage()));
                    }

                    @Override
                    public void onComplete() {
                        Timber.e("onComplete");
                        EventBus.getDefault().post(new OnComicsFilterTaskCompleteEvent(count, pageCount));
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

    // alternative way to perform filter
    private void retroLambda(final double budget) {
        Observable.fromIterable(getMarvelComicsList()).
                map(new Function<MarvelComic, HashMap<String, Double>>() {
                    @Override
                    public HashMap<String, Double> apply(@NonNull MarvelComic marvelComic) {
                        HashMap<String, Double> map = new HashMap<String, Double>();
                        map.put("price", Double.valueOf(marvelComic.getPrice()));
                        map.put("pageCount", Double.valueOf(marvelComic.getPageCount()));
                        map.put("comicCount", Double.valueOf(marvelComic.getPageCount()));
                        return map;
                    }
                })
                .scan(new BiFunction<HashMap<String, Double>,
                                HashMap<String, Double>, HashMap<String, Double>>() {
                            @Override
                            public HashMap<String, Double> apply(
                                    @NonNull HashMap<String, Double> inputMap,
                                    @NonNull HashMap<String, Double> newValueMap) {
                                double sum = inputMap.get("price")+newValueMap.get("price");
                                double count = inputMap.get("pageCount")
                                        +newValueMap.get("pageCount");
                                double comicCount = inputMap.get("comicCount")
                                        +newValueMap.get("comicCount");

                                HashMap<String, Double> map = new HashMap<String, Double>();
                                map.put("price", sum);
                                map.put("pageCount", count);
                                map.put("comicCount", comicCount);

                                return map;
                            }
                        })
                .takeWhile(new Predicate<HashMap<String, Double>>() {
                    @Override
                    public boolean test(@NonNull HashMap<String, Double> stringDoubleHashMap) throws Exception {
                        return stringDoubleHashMap.get("price") < budget;
                    }
                })
                .subscribe(new DisposableObserver<HashMap<String, Double>>() {
                    @Override
                    public void onNext(HashMap<String, Double> stringDoubleHashMap) {
                        Timber.e("sum: %s pageCount: %s comicCount: %s", stringDoubleHashMap.get("price"), stringDoubleHashMap.get("pageCount"), stringDoubleHashMap.get("comicCount"));
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

        // example for learning scan() operator
        /* Observable.fromIterable(getMarvelComicsList())
                  .scan(0.0, new io.reactivex.functions.BiFunction<Double, MarvelComic, Double>() {
                      @Override
                      public Double apply(@NonNull Double aDouble, @NonNull MarvelComic marvelComic) throws Exception {
                          double sum = aDouble+Double.valueOf(marvelComic.getPrice());
                          Timber.e("scan() -> initialValue: %s   Sum: %s   marvelComic.getPrice() %s", aDouble, sum, marvelComic.getPrice());
                          return sum;
                      }
                  })
                .skip(1) // because scan() will return with the default value provided in the very first parameter of the scan()
                  .takeWhile(new Predicate<Double>() {
                      @Override
                      public boolean test(@NonNull Double aDouble) throws Exception {
                          Timber.e("takeWhile() -> value is %s", aDouble);
                          return aDouble < budget;
                      }
                  })
                  .subscribe(new DisposableObserver<Double>() {
                      @Override
                      public void onNext(Double aDouble) {
                            Timber.e("onNext() -> value is: %s", aDouble);
                      }

                      @Override
                      public void onError(Throwable e) {
                          Timber.e("onError() -> error is: %s", e);
                      }

                      @Override
                      public void onComplete() {
                          Timber.e("onComplete()");
                      }
                  });*/

    }
}