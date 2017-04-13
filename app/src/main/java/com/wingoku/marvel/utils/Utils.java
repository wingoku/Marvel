package com.wingoku.marvel.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.wingoku.marvel.R;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

/**
 * Created by Umer on 4/8/2017.
 */

public class Utils {
    /**
     * This method will return a Snackbar object
     *
     * @param con Activity/Application context
     * @param view View with which {@link Snackbar} will be attached
     * @return Useable Snackbar object
     */
    public static Snackbar initSnackbar(Context con, View view) {
        final Snackbar snackBar = Snackbar.make(view, "", Snackbar.LENGTH_LONG);
        snackBar.setAction(con.getString(R.string.ok_string), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackBar.dismiss();
            }
        });
        snackBar.setActionTextColor(Color.GREEN);

        return snackBar;
    }

    // source: https://dzone.com/articles/android-snippet-making-md5

    /**
     * Generate md5 hash using provided parameters
     * @param con Context of the activity/fragment/application
     * @param unsecureString string to be converted into md5 hash
     * @return md5 hash string
     */
    public static String generateMD5Hash(Context con, String unsecureString) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(unsecureString.getBytes());
            byte[] a = digest.digest();
            int len = a.length;
            StringBuilder sb = new StringBuilder(len << 1);
            for (int i = 0; i < len; i++) {
                sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
                sb.append(Character.forDigit(a[i] & 0x0f, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            Crashlytics.log(Log.ERROR, con.getClass().getSimpleName() ,ex.getLocalizedMessage());
            Crashlytics.logException(ex);
        }
        return null;
    }

    /**
     * Check if network is available
     * @param con Context of the activity/fragment/application
     * @return true/false for network availability and offline respectively
     */
    public static boolean isNetworkAvailable(Context con) {
        ConnectivityManager cm = (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    /**
     * Get date in String
     * @param daysPast number of days to go back to from current date
     * @return returns date string in dd/mm format
     */
    public static String getDate(int daysPast) {
        //Calendar set to the current date
        Calendar calendar= Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, daysPast*-1);

        return calendar.get(Calendar.DAY_OF_MONTH)+"/"+calendar.get(Calendar.MONTH);
    }
}
