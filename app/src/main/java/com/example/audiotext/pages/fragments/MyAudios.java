package com.example.audiotext.pages.fragments;

import android.content.Context;
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


public class MyAudios extends Fragment {
    final private String TAG = MyAudios.class.getSimpleName();
    private Context mContext;

    private SeekBar pitch_sb;
    private SeekBar speed_sb;
    private Button rest_btn, save_btn;

    private AppDatabase db;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = AppDatabase.getInstance(mContext);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_audios, container, false);

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

    }
}
