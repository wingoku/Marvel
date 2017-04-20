package com.wingoku.marvel.modules;

import android.content.Context;
import android.net.Uri;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.wingoku.marvel.interfaces.qualifiers.PerFragmentScope;
import com.wingoku.marvel.interfaces.qualifiers.PicassoLoggingInterceptorQualifier;
import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import timber.log.Timber;

/**
 * Created by Umer on 4/8/2017.
 */

@Module(includes = OKHttpModule.class)
public class PicassoModule {
    @Provides
    @PerFragmentScope
    public Picasso providesPicasso(Picasso.Builder picassoBuilder) {
        Picasso picasso = picassoBuilder.build();
        picasso.setLoggingEnabled(true);
        return picasso;
    }

    @Provides
    @PerFragmentScope
    public Picasso.Builder providesPicassoBuilder(Context context, @PicassoLoggingInterceptorQualifier OkHttpClient client) {
        return new Picasso.Builder(context)
                  .downloader(new OkHttp3Downloader(client)).listener(new Picasso.Listener() {
                        @Override
                        public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                            Timber.e("Picasso Image Load Failure: %s", exception.getLocalizedMessage());
                        }
                  });
    }
}