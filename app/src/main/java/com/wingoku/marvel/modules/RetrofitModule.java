package com.wingoku.marvel.modules;

import com.wingoku.marvel.interfaces.qualifiers.NetworkComponentScope;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Umer on 4/8/2017.
 */

@Module(includes = {OKHttpModule.class, RetrofitBaseUrlModule.class})
public class RetrofitModule {

    @Provides
    @NetworkComponentScope
    public Retrofit providesRetrofit(OkHttpClient okHttpClient, GsonConverterFactory gsonConverterFactory, String baseUrl) {
        return new Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(gsonConverterFactory)
                .baseUrl(baseUrl)
                .build();
    }

    @Provides
    @NetworkComponentScope
    public GsonConverterFactory providesGsonConverterFactory() {
        return GsonConverterFactory.create();
    }
}
