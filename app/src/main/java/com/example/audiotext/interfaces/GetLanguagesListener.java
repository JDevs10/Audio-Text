package com.example.audiotext.interfaces;

import java.util.ArrayList;
import java.util.Locale;

public interface GetLanguagesListener {
    void onGetLanguagesListener(ArrayList<String> localeListString, ArrayList<Locale> localeList);
}
