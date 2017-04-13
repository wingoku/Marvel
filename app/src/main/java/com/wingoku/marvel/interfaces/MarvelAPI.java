package com.wingoku.marvel.interfaces;

import android.content.Context;
import android.util.Log;

import com.wingoku.marvel.BuildConfig;
import com.wingoku.marvel.models.serverResponse.MarvelResponse;
import com.wingoku.marvel.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import io.reactivex.Observable;

/**
 * Created by Umer on 4/9/2017.
 */

public interface MarvelAPI {

    // SAMPLE URL:
    /* http://gateway.marvel.com:80/v1/public/comics?apikey=PUBLIC_KEY&ts=SYSTEM_TIME&hash=MD5_OF_TS+PRIVATE_KEY+PUBLIC_KEY&limit=NUMBER_OF_COMICS_TO_FETCH */
    String BASE_URL = "http://gateway.marvel.com:80/v1/public/";

    @GET("comics")
    Observable<MarvelResponse> getComics(@Query("apikey") String apiKey, @Query("hash") String hash, @Query("ts") String timeStamp, @Query("limit") int limit, @Query("offset") int offset);

    class Factory {
        private static MarvelAPI mMarvelAPI;

        /**
         * Get Retrofit instance
         *
         * @return Returns RetroFit instance for Network Calls
         */
        public static MarvelAPI getInstance(Retrofit retrofit) {
            if (mMarvelAPI == null) {
                mMarvelAPI = retrofit.create(MarvelAPI.class);
                return mMarvelAPI;
            } else {
                return mMarvelAPI;
            }
        }
    }
}
