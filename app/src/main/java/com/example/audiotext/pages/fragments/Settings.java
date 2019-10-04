package com.example.audiotext.pages.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.audiotext.R;
import com.example.audiotext.database.AppDatabase;
import com.example.audiotext.database.entity.SettingsEntry;
import com.example.audiotext.interfaces.GetLanguagesListener;
import com.example.audiotext.task.GetLanguages;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;


public class Settings extends Fragment implements GetLanguagesListener {
    final private String TAG = Settings.class.getSimpleName();
    private Context mContext;

    private Spinner language_sp;
    private SeekBar pitch_sb;
    private SeekBar speed_sb;
    private Button rest_btn, save_btn;

    private TextToSpeech tts;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> localeListString = new ArrayList<>();
    private ArrayList<Locale> localeList = new ArrayList<>();
    private static final String SPINNER_LANGUAGE_0 = "Select Language...";
    private static final String SPECIFIC_TTS_PACKAGE_NAME = "com.google.android.tts";

    private AppDatabase db;
    private SettingsEntry settingsEntry;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = AppDatabase.getInstance(mContext);
        settingsEntry = db.settingsDao().getSettings().get(0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        language_sp = v.findViewById(R.id.fragment_settings_spinner_language);
        pitch_sb = v.findViewById(R.id.fragment_settings_seek_bar_pitch);
        speed_sb = v.findViewById(R.id.fragment_settings_seek_bar_speed);
        rest_btn = v.findViewById(R.id.fragment_settings_reset_btn);
        save_btn = v.findViewById(R.id.fragment_settings_save_btn);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setSubtitle("");

        tts = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {

                    new GetLanguages(mContext, Settings.this, tts, SPINNER_LANGUAGE_0).execute();
                }
            }
        }, SPECIFIC_TTS_PACKAGE_NAME);

        localeListString.add(SPINNER_LANGUAGE_0);
        adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item,localeListString);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        language_sp.setAdapter(adapter);

        language_sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(mContext, "Selected Language: "+adapterView.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        rest_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetSettings();
            }
        });
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {saveSettings();}
        });

        //getDatabaseSettings();
    }

    @Override
    public void onGetLanguagesListener(ArrayList<String> localeListString_, ArrayList<Locale> localeList_) {
        localeList = localeList_;
        localeListString = localeListString_;
        Log.e(TAG, "localeList size: "+localeList.size()+" || localeListString size: "+localeListString.size());

        adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item,localeListString);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        language_sp.setAdapter(adapter);
        getDatabaseSettings();
    }

    private void getDatabaseSettings(){
        SettingsEntry newSettingsEntry = db.settingsDao().getSettings().get(0);
        pitch_sb.setProgress(newSettingsEntry.getSpeekPitch());
        speed_sb.setProgress(newSettingsEntry.getSpeekSpeed());

        Locale locale1 = new Locale(newSettingsEntry.getLocaleLanguage(),newSettingsEntry.getLocaleCountry(),newSettingsEntry.getLocaleVariant());
        language_sp.setSelection(adapter.getPosition(locale1.getDisplayName()));
    }

    private void resetSettings(){
        SettingsEntry newSettingsEntry = settingsEntry;
        pitch_sb.setProgress(50);
        speed_sb.setProgress(50);

        newSettingsEntry.setSpeekPitch(50);
        newSettingsEntry.setSpeekSpeed(50);
        db.settingsDao().updateSettings(newSettingsEntry);

        Toast.makeText(mContext, "All settings are reseted !", Toast.LENGTH_SHORT).show();
    }

    private void saveSettings(){
        SettingsEntry newSettingsEntry = settingsEntry;
        newSettingsEntry.setSpeekPitch(pitch_sb.getProgress());
        newSettingsEntry.setSpeekSpeed(speed_sb.getProgress());

        for (int i=1; i<localeList.size(); i++){
            if (language_sp.getSelectedItem().toString().equals(localeList.get(i).getDisplayName())){
                //Log.e(TAG, language_sp.getSelectedItem().toString()+" === "+localeList.get(i).getDisplayName());

                newSettingsEntry.setLocaleLanguage(localeList.get(i).getLanguage());
                newSettingsEntry.setLocaleCountry(localeList.get(i).getCountry());
                newSettingsEntry.setLocaleVariant(localeList.get(i).getVariant());
                break;
            }
        }

        db.settingsDao().updateSettings(newSettingsEntry);
        Toast.makeText(mContext, "All settings are reseted !", Toast.LENGTH_SHORT).show();
    }

}
