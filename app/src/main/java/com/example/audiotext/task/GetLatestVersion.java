package com.example.audiotext.task;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import com.example.audiotext.interfaces.GooglePlayVersion;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class GetLatestVersion extends AsyncTask<String, String, JSONObject> {
    private final String TAG = GetLatestVersion.class.getSimpleName();

    private Context mContext;
    private GooglePlayVersion googlePlayVersion = null;
    private PackageManager pm;
    private PackageInfo pInfo;
    private String currentVersion = null;
    private String latestVersion = null;
    private boolean active;

    public GetLatestVersion(Context mContext, GooglePlayVersion googlePlayVersion, boolean active_PlayStore){
        this.mContext = mContext;
        this.googlePlayVersion = googlePlayVersion;
        this.active = active_PlayStore;
        this.pm = mContext.getPackageManager();
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        try {
            pInfo =  pm.getPackageInfo(mContext.getPackageName(),0);
            currentVersion = pInfo.versionName;
            Log.e(TAG, " current app version : "+currentVersion);

        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        }

        if (active) {
            try {
                //It retrieves the latest version by scraping the content of current version from play store at runtime
                Document doc = Jsoup.connect("https://play.google.com/store/apps/details?id=com.analyst.fragmenttest").get();
                latestVersion = doc.getElementsByClass("htlgb").get(6).text();
                Log.e(TAG, " current PlayStore version (enable) : " + latestVersion);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            latestVersion = currentVersion;
            Log.e(TAG, " current PlayStore version (Disable) : "+currentVersion);
        }

        return new JSONObject();
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        googlePlayVersion.onReceiveGooglePlayVersion(currentVersion, latestVersion);
    }
}