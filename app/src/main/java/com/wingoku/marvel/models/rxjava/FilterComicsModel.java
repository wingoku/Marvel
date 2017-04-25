package com.wingoku.marvel.models.rxjava;

/**
 * Created by Umer on 4/25/2017.
 */

public class FilterComicsModel {
    private double mPrice;
    private int mComicCount;
    private int mPageCount;

    public FilterComicsModel() {
        mPageCount = mComicCount = 0;
        mPrice = 0.0;
    }
    public double getPrice() {
        return mPrice;
    }

    public void setPrice(double mPrice) {
        this.mPrice = mPrice;
    }

    public int getComicCount() {
        return mComicCount;
    }

    public void setComicCount(int mComicCount) {
        this.mComicCount = mComicCount;
    }

    public int getPageCount() {
        return mPageCount;
    }

    public void setPageCount(int mPageCount) {
        this.mPageCount = mPageCount;
    }
}
