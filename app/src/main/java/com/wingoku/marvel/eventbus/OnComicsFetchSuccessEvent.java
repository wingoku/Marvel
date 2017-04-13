package com.wingoku.marvel.eventbus;

import com.wingoku.marvel.R;
import com.wingoku.marvel.models.serverResponse.Result;

import java.util.List;

/**
 * Created by Umer on 4/13/2017.
 */

public class OnComicsFetchSuccessEvent {
    private List<Result> mList;

    public OnComicsFetchSuccessEvent(List<Result> resultList) {
        mList = resultList;
    }

    public List<Result> getResultsList() {
        return mList;
    }
}
