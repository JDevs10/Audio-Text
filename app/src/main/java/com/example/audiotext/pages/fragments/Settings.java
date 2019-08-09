package com.example.audiotext.pages.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.audiotext.R;
import com.example.audiotext.database.AppDatabase;
import com.example.audiotext.database.entity.SettingsEntry;

import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;


public class Settings extends Fragment {
    final private String TAG = Settings.class.getSimpleName();
    private Context mContext;

    private SeekBar pitch_sb;
    private SeekBar speed_sb;
    private Button rest_btn, save_btn;

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

        SortedSet<String> allLanguages = new TreeSet<String>();
        String[] languages = Locale.getISOLanguages();
        for (int i = 0; i < languages.length; i++){
            Log.e(TAG, "languages: "+languages[i]);
            Locale loc = new Locale(languages[i]);
            allLanguages.add(loc.getDisplayLanguage());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

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

        rest_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetSettings();
            }
        });
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSettings();
            }
        });

        getDatabaseSettings();
    }

    private void getDatabaseSettings(){
        SettingsEntry newSettingsEntry = db.settingsDao().getSettings().get(0);
        pitch_sb.setProgress(newSettingsEntry.getSpeekPitch());
        speed_sb.setProgress(newSettingsEntry.getSpeekSpeed());
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
        db.settingsDao().updateSettings(newSettingsEntry);

        Toast.makeText(mContext, "All settings are reseted !", Toast.LENGTH_SHORT).show();
    }
}
