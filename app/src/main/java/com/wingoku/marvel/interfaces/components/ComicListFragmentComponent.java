package com.wingoku.marvel.interfaces.components;

import com.wingoku.marvel.fragments.ComicListFragment;
import com.wingoku.marvel.interfaces.qualifiers.PerFragmentScope;
import com.wingoku.marvel.modules.ComicCacheDBModule;
import com.wingoku.marvel.modules.PicassoModule;
import com.wingoku.marvel.modules.RetrofitModule;

import dagger.Component;

/**
 * Created by Umer on 4/8/2017.
 */

@Component(modules = {RetrofitModule.class, PicassoModule.class, ComicCacheDBModule.class})
@PerFragmentScope
public interface ComicListFragmentComponent {
    void inject(ComicListFragment fragment);
}
