package com.wingoku.marvel.modules;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.wingoku.marvel.interfaces.qualifiers.NetworkComponentScope;
import com.wingoku.marvel.interfaces.qualifiers.PicassoLoggingInterceptor;
import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

/**
 * Created by Umer on 4/8/2017.
 */

@Module(includes = OKHttpModule.class)
public class PicassoModule {
    @Provides
    @NetworkComponentScope
    public Picasso providesPicasso(Picasso.Builder picassoBuilder) {
        Picasso picasso = picassoBuilder.build();
        picasso.setLoggingEnabled(true);
//        Picasso.setSingletonInstance(picasso);
//        Picasso.with(getContext()).load(/*"http://image.tmdb.org/t/p/w300/sM33SANp9z6rXW8Itn7NnG1GOEs.jpg"*/marvelComic.getThumbnailUrl()+Constants.PORTAIT_INCREDIBLE).fetch();
        return picasso;
    }

    @Provides
    @NetworkComponentScope
    public Picasso.Builder providesPicassoBuilder(Context context, @PicassoLoggingInterceptor OkHttpClient client) {
        return new Picasso.Builder(context)
                  .downloader(new OkHttp3Downloader(client)).listener(new Picasso.Listener() {
                        @Override
                        public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                            Log.e("wingoki", "failure: "+ exception.getLocalizedMessage());
                        }
                  });
    }
}