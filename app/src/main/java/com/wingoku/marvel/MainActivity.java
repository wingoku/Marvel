package com.wingoku.marvel;

import android.os.Bundle;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.crashlytics.android.Crashlytics;
import com.wingoku.marvel.fragments.ComicDetailFragment;
import com.wingoku.marvel.fragments.ComicListFragment;
import com.wingoku.marvel.models.MarvelComic;
import com.wingoku.marvel.utils.Constants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;
import rx.functions.Action1;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    // this idling resource will be used by Espresso to wait for and synchronize with RetroFit Network call
    // https://youtu.be/uCtzH0Rz5XU?t=3m23s
    CountingIdlingResource mEspressoTestIdlingResource = new CountingIdlingResource("Network_Call");
    private FragmentManager mFManager;

    @BindView(R.id.fragment_container)
    FrameLayout mFragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // initializing Crashlytics for error reporting
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_main);

        //must bind for View Injection
        ButterKnife.bind(this);
        mFManager = getSupportFragmentManager();

        if(savedInstanceState == null)
            openFragment(new ComicListFragment(), true, false, false, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    /**
     * Open a fragment
     *
     * @param frag Fragment to open
     * @param isReplaced should this fragment replace current visible fragment
     * @param isAdded should this fragment be added on top of current fragment
     * @param addToBackStack should this fragment be added to backstack for removal upon onBackPressed
     * @param setEnterAnimation should this fragment be animated when replaced/added in the fragment container
     *
     */
    private void openFragment(Fragment frag, boolean isReplaced, boolean isAdded, boolean addToBackStack, boolean setEnterAnimation) {
        FragmentTransaction fTranscation = mFManager.beginTransaction();

        if(setEnterAnimation)
            fTranscation.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        else
            fTranscation.setCustomAnimations(0, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

        if (isReplaced)
            fTranscation.replace(R.id.fragment_container, frag);
        else if (isAdded)
            fTranscation.add(R.id.fragment_container, frag);

        if (addToBackStack)
            fTranscation.addToBackStack(frag.getClass().getSimpleName());

        fTranscation.commit();
    }

    public void onNetworkProcessStarted() {
        mEspressoTestIdlingResource.increment();
    }

    public void onNetworkProcessEnded() {
        if(!mEspressoTestIdlingResource.isIdleNow()) {
            mEspressoTestIdlingResource.decrement();
        }
    }

    /**
     * This method will return Espresso IdlingResource for aiding sync between RetroFit's custom background threads & Espresso
     *
     * @return MainActvity's idling resource for Espresso testing
     */
    public CountingIdlingResource getEspressoIdlingResourceForMainActivity() {
        return mEspressoTestIdlingResource;
    }

    private void exampleForUsingRxEventBus() {
        /**
         * NOTE:
         *  Currently I'm not using RxBus instead of using EventBus because of the clutter that new Action1<DataType>() {}
         *   will make for all the methods that are tagged with @Subscribe annotation.
         *
         *   RxBus can be used even now but I'm not using it just for cosmetic reasons. However if I start using
         *   lambdas using RetroLambda lib, the new Action1(DataType){} will be reduced to single statement, which
         *   will be EXTREMLY clean solution.
         *   I'll switch to using RetroLamda with Rxjava in the near future.
         */
        RxBus.getInstance().register(String.class,
                new Action1<String>() {
                    @Override
                    public void call(String string) {
                        Timber.e("rxBus onAction() called with value %s", string);
                    }
                });
    }

    @Subscribe
    public void onComicCardClickCallBacks(MarvelComic marvelComic) {
        // open detail fragment
        ComicDetailFragment comicDetailFragment = new ComicDetailFragment();
        Bundle bund = new Bundle();
        bund.putParcelable(Constants.PARCELABLE_MARVEL_COMIC_OBJECT, marvelComic);
        comicDetailFragment.setArguments(bund);
        openFragment(comicDetailFragment, true, false, true, true);
    }
}
