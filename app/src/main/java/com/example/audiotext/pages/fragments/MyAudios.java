package com.example.audiotext.pages.fragments;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.audiotext.R;
import com.example.audiotext.adapters.AudioFileAdapter;
import com.example.audiotext.database.AppDatabase;
import com.example.audiotext.database.entity.SettingsEntry;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;


public class MyAudios extends Fragment {
    final private String TAG = MyAudios.class.getSimpleName();
    private Context mContext;

    private RecyclerView audioList_rv;

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

        audioList_rv = (RecyclerView)v.findViewById(R.id.fragment_my_audios_recyclerview);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setSubtitle("");

        audioList_rv.setLayoutManager(new LinearLayoutManager(mContext));
        audioList_rv.setHasFixedSize(true);
        AudioFileAdapter audioFileAdapter = new AudioFileAdapter(mContext,getAudioFiles());
        audioList_rv.setAdapter(audioFileAdapter);

    }

    private File[] getAudioFiles(){
        String directoryPath = Environment.getExternalStorageDirectory().toString()+"/AudioText/sounds";
        Log.d("Files", "Path: " + directoryPath);
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();

        Log.d("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++)
        {
            Log.d("Files", "FileName:" + files[i].getName());
        }
        return files;
    }

    @Override
    public void onStop() {
        super.onStop();

    }
}
