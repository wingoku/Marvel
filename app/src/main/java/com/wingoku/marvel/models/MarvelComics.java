package com.wingoku.marvel.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Umer on 4/9/2017.
 */

public class MarvelComics implements Parcelable {
    List<MarvelComic> marvelComicList;

    public List<MarvelComic> getMarvelComicList() {
        return marvelComicList;
    }

    public void setMarvelComicList(List<MarvelComic> marvelComicList) {
        this.marvelComicList = marvelComicList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.marvelComicList);
    }

    public MarvelComics() {
        marvelComicList = new ArrayList<>();
    }

    protected MarvelComics(Parcel in) {
        this.marvelComicList = in.createTypedArrayList(MarvelComic.CREATOR);
    }

    public static final Parcelable.Creator<MarvelComics> CREATOR = new Parcelable.Creator<MarvelComics>() {
        @Override
        public MarvelComics createFromParcel(Parcel source) {
            return new MarvelComics(source);
        }

        @Override
        public MarvelComics[] newArray(int size) {
            return new MarvelComics[size];
        }
    };
}
