package com.wingoku.marvel.eventbus;

/**
 * Created by Umer on 4/13/2017.
 */

public class OnComicsFilterTaskCompleteEvent {

    private int mCount;
    private int mPageCountOfComicsWithInBudget;

    public OnComicsFilterTaskCompleteEvent(int count, int pageCount) {
        mCount = count;
        mPageCountOfComicsWithInBudget = pageCount;
    }

    public int getComicsCount() {
        return mCount;
    }

    public int getPageCount() {
        return mPageCountOfComicsWithInBudget;
    }
}
