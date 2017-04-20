package com.wingoku.marvel.modules;

import com.wingoku.marvel.fragments.ComicListFragment;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Umer on 4/20/2017.
 */

@Module
public class ComicListFragmentModule {

    private ComicListFragment mComicListFragment;

    public ComicListFragmentModule(ComicListFragment fragment) {
        mComicListFragment = fragment;
    }

    @Provides
    public ComicListFragment providesComicListFragmentInstance() {
        return mComicListFragment;
    }
}
