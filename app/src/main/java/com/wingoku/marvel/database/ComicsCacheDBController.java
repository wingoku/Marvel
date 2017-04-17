package com.wingoku.marvel.database;

import com.wingoku.marvel.models.MarvelComic;
import com.wingoku.marvel.utils.Utils;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;

/**
 * Created by Umer on 4/11/2017.
 */

public class ComicsCacheDBController {
    private final Realm realm;

    public ComicsCacheDBController() {
        realm = Realm.getDefaultInstance();
    }
    
    public Realm getRealm() {
        return realm;
    }

    public void insertComic(MarvelComic comic) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(comic);
        realm.commitTransaction();
    }

    public void insertComicList(final List<MarvelComic> comicList) {
        for(MarvelComic comic : comicList) {
            insertComic(comic);
        }
    }

    /**
     * Clear all the entries in the RealmDB
     */
    public void clearAll() {
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
    }

    /**
     * Get all MarvelComic entries from RealmDB
     */
    public RealmResults<MarvelComic> getAllComics() {
        return realm.where(MarvelComic.class).findAll();
    }

    /**
     * Delete a comic entry from databse created on specific date
     * @param date date on which the comic was inserted in DB
     */
    public void deleteComicsInsertedOnDate(final String date) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<MarvelComic> result = realm.where(MarvelComic.class).equalTo("mDBEntryDate", date).findAll();
                result.deleteAllFromRealm();
            }
        });
    }

    /**
     * Get @{@link MarvelComic} inserted in DB on a specific date
     * @param date date on which MarvelComic was inserted in DB
     * @return returns {@link MarvelComic}
     */
    public MarvelComic getComicForDate(String date) {
        return realm.where(MarvelComic.class).equalTo("mDBEntryDate", date).findFirst();
    }

    /**
     * Validate if the entries in DB are older then the {@link com.wingoku.marvel.utils.Constants#MAX_STALE_DAYS}, if yes, delete them
     * @param maxAge {@link com.wingoku.marvel.utils.Constants#MAX_STALE_DAYS}
     */
    public void validateExpiryDateForDBEntry(int maxAge) {
        Timber.d("previous date is: %s", Utils.getDate(maxAge));
        deleteComicsInsertedOnDate(Utils.getDate(maxAge));
    }
}
