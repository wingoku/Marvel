package com.wingoku.marvel.modules;

import com.wingoku.marvel.database.ComicsCacheDBController;
import com.wingoku.marvel.interfaces.qualifiers.PerFragmentScope;

import dagger.Module;
import dagger.Provides;
import timber.log.Timber;

/**
 * Created by Umer on 4/17/2017.
 */

@Module
public class ComicCacheDBModule {

    @Provides
    @PerFragmentScope
    public ComicsCacheDBController providesComicsDBController() {
        Timber.e("proviesComicsDBController()");
        return new ComicsCacheDBController();
    }
}
