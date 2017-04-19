package com.wingoku.marvel.modules;

import com.wingoku.marvel.interfaces.qualifiers.PerFragment;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Umer on 4/8/2017.
 */

@Module(includes = {OKHttpModule.class, RetrofitBaseUrlModule.class})
public class RetrofitModule {

    @Provides
    @PerFragment
    public Retrofit providesRetrofit(OkHttpClient okHttpClient, GsonConverterFactory gsonConverterFactory, RxJava2CallAdapterFactory rxJava2AdapterFactory, String baseUrl) {
        return new Retrofit.Builder()
                .client(okHttpClient)
                .addCallAdapterFactory(rxJava2AdapterFactory)
                .addConverterFactory(gsonConverterFactory)
                .baseUrl(baseUrl)
                .build();
    }

    @Provides
    @PerFragment
    public GsonConverterFactory providesGsonConverterFactory() {
        return GsonConverterFactory.create();
    }

    @Provides
    @PerFragment
    public RxJava2CallAdapterFactory providesRxJava2AdapterFactory() {
        return RxJava2CallAdapterFactory.create();
    }
}
