package com.wingoku.marvel.eventBus;

/**
 * Created by Umer on 4/13/2017.
 */

public class OnMarvelComicListCreationFailureEvent {
    private String mError;

    public OnMarvelComicListCreationFailureEvent(String localizedMessage) {
        mError = localizedMessage;
    }

    public String getError() {
        return mError;
    }
}
