package com.wingoku.marvel;

import android.support.annotation.NonNull;

import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by Umer on 4/22/2017.
 */

public class RxBus {

    private static RxBus mRxBus;
    private static PublishSubject mPublishSubject;

    public static RxBus getInstance() {
        if(mRxBus == null) {
            mRxBus = new RxBus();
            mPublishSubject = PublishSubject.create();
        }

        return mRxBus;
    }

    /**
     * IMPORTANT NOTES:
     * 1. A subject can have MULTIPLE observers attached to it at the same time.
     * 2. When onNext on the Subject is called, it will call all the observers that have subscribed to it.
     * 3. When subject.onNext is called, it will call all the methods attached to subject object before the subscribe() method
     * @param type Type of the object that will be passed in {@link #postEvent(Object)}
     * @param observerAction observer that will subscribe to the subject
     */
    @SuppressWarnings("unchecked")
    public <T> void register(final Class<? super T> type, Action1<? super T> observerAction) {
        mPublishSubject.filter(new Func1<T, Boolean>() {
            @Override
            public Boolean call(T o){
//                Timber.e("Filter(): object type: %s typeClass is: %s", o.getClass(), type.getName());
                if(o.getClass().equals(type)) {
                    Timber.e("Filer() return true");
                    return true;
                }
                Timber.e("Filer() return false");
                return false;
            }
        }).map(new Func1<T, T>() {
            @Override
            public T call(@NonNull T o) {
//                Timber.e("Map(): object received from filter: %s", o);
                return o;
            }
        }).subscribe(observerAction);
    }

    public <T> void postEvent(T object) {
//        Timber.e("onPostEvent: object is: %s", object);
        mPublishSubject.onNext(object);
    }
}
