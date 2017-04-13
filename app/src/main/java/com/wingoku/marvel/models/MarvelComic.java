package com.wingoku.marvel.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.wingoku.marvel.utils.Utils;

import java.util.Calendar;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import timber.log.Timber;

/**
 * Created by Umer on 4/9/2017.
 */

public class MarvelComic extends RealmObject implements Parcelable {
    @PrimaryKey
    private String mId; // id from MarvelServer response
    private String mTitle;
    private String mDescription;
    private String mAuthor;
    private String mPageCount;
    private String mPrice;
    private String mThumbnailUrl;
    private String mDBEntryDate;

    public MarvelComic() {
        mDBEntryDate = Utils.getDate(0);
        Timber.e("date is: %s", mDBEntryDate);
    }

    public void setDBEntryDate(String date) {
        mDBEntryDate = date;
    }

    public String getDBEntryDate() {
        return mDBEntryDate;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getThumbnailUrl() {
        return mThumbnailUrl;
    }

    public void setThumbnailUrl(String mThumbnailUrl) {
        this.mThumbnailUrl = mThumbnailUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String mAuthor) {
        this.mAuthor = mAuthor;
    }

    public String getPageCount() {
        return mPageCount;
    }

    public void setPageCount(String mPageCount) {
        this.mPageCount = mPageCount;
    }

    public String getPrice() {
        return mPrice;
    }

    public void setPrice(String mPrice) {
        this.mPrice = mPrice;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mTitle);
        dest.writeString(this.mDescription);
        dest.writeString(this.mAuthor);
        dest.writeString(this.mPageCount);
        dest.writeString(this.mPrice);
        dest.writeString(this.mThumbnailUrl);
    }

    protected MarvelComic(Parcel in) {
        this.mTitle = in.readString();
        this.mDescription = in.readString();
        this.mAuthor = in.readString();
        this.mPageCount = in.readString();
        this.mPrice = in.readString();
        this.mThumbnailUrl = in.readString();
    }

    public static final Parcelable.Creator<MarvelComic> CREATOR = new Parcelable.Creator<MarvelComic>() {
        @Override
        public MarvelComic createFromParcel(Parcel source) {
            return new MarvelComic(source);
        }

        @Override
        public MarvelComic[] newArray(int size) {
            return new MarvelComic[size];
        }
    };
}
