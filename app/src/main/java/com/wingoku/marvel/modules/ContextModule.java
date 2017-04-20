package com.wingoku.marvel.modules;

import android.content.Context;

import com.wingoku.marvel.fragments.ComicListFragment;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Umer on 4/8/2017.
 */

@Module
public class ContextModule {

    private Context mContext;

    public ContextModule(Context context) {
        this.mContext = context;
    }

    @Provides
    public Context providesContext() {
        return mContext;
    }
}
