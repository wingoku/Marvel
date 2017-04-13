package com.wingoku.marvel.modules;

import com.wingoku.marvel.interfaces.MarvelAPI;
import com.wingoku.marvel.interfaces.qualifiers.NetworkComponentScope;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Umer on 4/8/2017.
 */
@Module
public class RetrofitBaseUrlModule {

    @Provides
    @NetworkComponentScope
    public String providesMarvelAPIUrl() {
        return MarvelAPI.BASE_URL;
    }
}
