package com.example.audiotext.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.example.audiotext.interfaces.GetFilesListener;
import com.example.audiotext.interfaces.GetLanguagesListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class GetLanguages extends AsyncTask<Void, Void, Void> {
    private final String TAG = GetLanguages.class.getSimpleName();

    private Context mContext;
    private TextToSpeech tts;
    private ArrayList<String> localeListString;
    private ArrayList<Locale> localeList;
    private GetLanguagesListener mGetLanguagesListener;
    private String SPINNER_LANGUAGE_0;

    public GetLanguages(Context mContext, GetLanguagesListener mGetLanguagesListener, TextToSpeech tts, String SPINNER_LANGUAGE_0){
        this.mContext = mContext;
        this.mGetLanguagesListener = mGetLanguagesListener;
        this.tts = tts;
        this.SPINNER_LANGUAGE_0 = SPINNER_LANGUAGE_0;
        this.localeList = new ArrayList<>();
        this.localeListString = new ArrayList<>();
    }

    @Override
    protected Void doInBackground(Void... voids) {

        Locale[] locales = Locale.getAvailableLocales();
        localeListString.add(SPINNER_LANGUAGE_0);
        localeList.add(Locale.getAvailableLocales()[0]);

        for (Locale locale : locales) {
            int res = tts.isLanguageAvailable(locale);
            if (res == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                Log.e(TAG, "locale language: "+locale.getDisplayName());
//                if (locale.getDisplayName().equals("English (United States)")){
//                    Log.e(TAG, "Default Language: "+locale.getLanguage()+" country: "+locale.getCountry()+" variant: "+locale.getVariant());
//                }
                localeListString.add(locale.getDisplayName());
                localeList.add(locale);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void voids) {
        super.onPostExecute(voids);
        mGetLanguagesListener.onGetLanguagesListener(localeListString, localeList);
    }
}
