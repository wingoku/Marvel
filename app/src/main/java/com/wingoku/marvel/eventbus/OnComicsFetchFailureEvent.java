package com.wingoku.marvel.eventbus;

/**
 * Created by Umer on 4/13/2017.
 */

public class OnComicsFetchFailureEvent {
    private String mError;

    public OnComicsFetchFailureEvent(String error) {
        mError = error;
    }

    public String getError() {
        return mError;
    }
}
