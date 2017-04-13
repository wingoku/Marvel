package com.wingoku.marvel.interfaces.components;

import com.squareup.picasso.Picasso;
import com.wingoku.marvel.interfaces.qualifiers.NetworkComponentScope;
import com.wingoku.marvel.modules.PicassoModule;
import com.wingoku.marvel.modules.RetrofitModule;

import dagger.Component;
import retrofit2.Retrofit;

/**
 * Created by Umer on 4/8/2017.
 */

@Component(modules = {RetrofitModule.class, PicassoModule.class})
@NetworkComponentScope
public interface NetworkComponent {
    Retrofit getRetrofitInstance();
    Picasso getPicassoInstance();
}
