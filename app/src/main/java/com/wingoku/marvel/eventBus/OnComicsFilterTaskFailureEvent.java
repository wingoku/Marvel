package com.wingoku.marvel.eventBus;

/**
 * Created by Umer on 4/13/2017.
 */

public class OnComicsFilterTaskFailureEvent {
    private String mError;

    public OnComicsFilterTaskFailureEvent(String error) {
        mError = error;
    }

    public String getError() {
        return mError;
    }
}
