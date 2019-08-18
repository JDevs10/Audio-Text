package com.example.audiotext.pages.fragments;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.audiotext.R;
import com.example.audiotext.adapters.AudioFileAdapter;
import com.example.audiotext.database.AppDatabase;
import com.example.audiotext.database.entity.SettingsEntry;
import com.example.audiotext.interfaces.FilesListener;
import com.example.audiotext.interfaces.GetFilesListener;
import com.example.audiotext.interfaces.ViewAudioInformation;
import com.example.audiotext.task.GetAudioFiles;
import com.example.audiotext.utility.Utility;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;


public class MyAudios extends Fragment implements GetFilesListener, FilesListener, ViewAudioInformation {
    final private String TAG = MyAudios.class.getSimpleName();
    private Context mContext;

    private EditText searchET;
    private String filterText;
    private ProgressBar AudioFileIconProgress;
    private AudioFileAdapter audioFileAdapter;
    private RecyclerView audioList_rv;
    private ArrayList<AudioFileAdapter.Holder> holderList;

    private int audioPlayerPosition = -1;
    private MediaPlayer audioPlayer;
    private Timer timer;
    private Thread thread;
    private Runnable runPlay;
    private Handler handler;
    private ArrayList<File> fileList;
    private ArrayList<File> filterFileList;

    private ImageView playPause_iv, pause_iv, previous_iv, next_iv, stop_iv;
    private TextView audioFileTitle_tv, audioFileDuration_tv;
    private SeekBar audioSeekBarr;
    private boolean isPlaying = false;

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

        searchET = (EditText)v.findViewById(R.id.fragment_my_audios_search_text);
        audioList_rv = (RecyclerView)v.findViewById(R.id.fragment_my_audios_recyclerview);

        audioFileTitle_tv = (TextView)v.findViewById(R.id.fragment_my_audios_file_title);
        audioFileDuration_tv = (TextView)v.findViewById(R.id.fragment_my_audios_audio_time_tv);
        playPause_iv = (ImageView) v.findViewById(R.id.fragment_my_audios_audio_play_iv);
        previous_iv = (ImageView) v.findViewById(R.id.fragment_my_audios_audio_previous_iv);
        next_iv = (ImageView) v.findViewById(R.id.fragment_my_audios_audio_next_iv);
        stop_iv = (ImageView) v.findViewById(R.id.fragment_my_audios_audio_stop_iv);
        audioSeekBarr = (SeekBar)v.findViewById(R.id.fragment_my_audios_audio_time_sb);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setSubtitle("");

        audioList_rv.setLayoutManager(new LinearLayoutManager(mContext));
        audioList_rv.setHasFixedSize(true);
        audioFileAdapter = new AudioFileAdapter(mContext, this, this, null);
        audioList_rv.setAdapter(audioFileAdapter);

        holderList = audioFileAdapter.getHolders();

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                new GetAudioFiles(mContext, MyAudios.this, searchET.getText().toString()).execute();
            }
        });

        playPause_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isPlaying){
                    play();
                    isPlaying = true;
                    playPause_iv.setImageResource(R.drawable.ic_pause_circle_outline_white);
                }else{
                    pause();
                    isPlaying = false;
                    playPause_iv.setImageResource(R.drawable.ic_play_circle_outline_white);
                }
            }
        });
        previous_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previous(audioPlayerPosition);
            }
        });
        next_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next(audioPlayerPosition);
            }
        });
        stop_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPlaying = false;
                playPause_iv.setImageResource(R.drawable.ic_play_circle_outline_white);
                holderList.get(audioPlayerPosition).progressBarIcon.setVisibility(View.GONE);
                stop();
            }
        });

        //Gestion de la seekbar
        handleSeekbar();

        Log.e(TAG, "Start => GetAudioFiles");
        new GetAudioFiles(mContext, MyAudios.this, "").execute();
    }

    @Override
    public void onGetFilesListener(ArrayList<File> files) {
        if (files != null) {
            Log.e(TAG, "onGetFilesListener 1");
            filterFileList = files;
            audioList_rv.setLayoutManager(new LinearLayoutManager(mContext));
            audioList_rv.setHasFixedSize(true);
            Log.e(TAG, "onGetFilesListener 2");
            audioFileAdapter = new AudioFileAdapter(mContext, MyAudios.this, MyAudios.this, filterFileList);
            Log.e(TAG, "onGetFilesListener 3");
            audioList_rv.setAdapter(audioFileAdapter);

            holderList = audioFileAdapter.getHolders();
        }else{
            Toast.makeText(mContext, "No Audio files received", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onViewAudioInformation(final ArrayList<File> fileList, final Dialog dialog, final int index) {
        ImageView close = dialog.findViewById(R.id.dialog_audio_info_close_btn);
        TextView tittle = dialog.findViewById(R.id.dialog_audio_info_tittle);
        TextView text = dialog.findViewById(R.id.dialog_audio_info_text_et);
        Button cancel = dialog.findViewById(R.id.dialog_audio_info_cancel_btn);
        Button delete = dialog.findViewById(R.id.dialog_audio_info_delete_btn);

        tittle.setText(fileList.get(index).getName());
        text.setText("You're about to delete '"+fileList.get(index).getName()+"' from your device.\nAre you sure ?");

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = fileList.get(index).getName();

                if (fileList.get(index).delete()){

                    new GetAudioFiles(mContext, MyAudios.this, "").execute();
                    Toast.makeText(mContext, name+" deleted!", Toast.LENGTH_LONG).show();
                    /*
                    fileList.remove(index);
                    holderList.remove(index);
                    audioFileAdapter.notifyItemRemoved(index);
                    audioFileAdapter.notifyItemChanged(index, fileList.size());
                    */
                    dialog.dismiss();
                }else {
                    Toast.makeText(mContext, name+" not deleted!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onAudioFileListener(final int index, final File file) {
        Log.e(TAG, "Index: "+index+" ; audio name: "+file.getName());
        audioPlayerPosition = index;
        holderList.get(0).progressBarIcon.setVisibility(View.GONE);
        holderList.get(audioPlayerPosition).progressBarIcon.setVisibility(View.GONE);

        if (audioPlayer == null){
            try {
                audioPlayer = new MediaPlayer();
                audioPlayer.setDataSource(file.getAbsolutePath());
                audioPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            holderList.get(index).progressBarIcon.setVisibility(View.VISIBLE);
            audioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    isPlaying = false;
                    holderList.get(index).progressBarIcon.setVisibility(View.GONE);
                    playPause_iv.setImageResource(R.drawable.ic_play_circle_outline_white);
                    audioFileDuration_tv.setText(new Utility().convertFileDuration(file));
                }
            });

            audioFileTitle_tv.setText(file.getName());
            audioFileDuration_tv.setText(new Utility().convertFileDuration(file));
            play();
        }else{
            stopPlayer();
            try {
                audioPlayer = new MediaPlayer();
                audioPlayer.setDataSource(file.getAbsolutePath());
                audioPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            holderList.get(index).progressBarIcon.setVisibility(View.VISIBLE);
            audioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    isPlaying = false;
                    holderList.get(index).progressBarIcon.setVisibility(View.GONE);
                    playPause_iv.setImageResource(R.drawable.ic_play_circle_outline_white);
                    audioFileDuration_tv.setText(new Utility().convertFileDuration(file));
                }
            });

            audioFileTitle_tv.setText(file.getName());
            audioFileDuration_tv.setText(new Utility().convertFileDuration(file));
            play();
        }
    }

    @Override
    public void onAudioFileProgress(Boolean active, ProgressBar progress) {
        AudioFileIconProgress = progress;
        if (progress != null) {
            if (active) {
                progress.setVisibility(View.VISIBLE);
            } else {
                progress.setVisibility(View.GONE);
                AudioFileIconProgress = null;
            }
        }
    }

    private void play(){
        if (audioPlayer != null){
            isPlaying = true;
            playPause_iv.setImageResource(R.drawable.ic_pause_circle_outline_white);
            holderList.get(audioPlayerPosition).progressBarIcon.setVisibility(View.VISIBLE);
            //audioPlayer.start();
        }else{
            if (audioPlayerPosition == -1){
                audioPlayerPosition = 0;
            }
            try {
                audioPlayer = new MediaPlayer();
                audioPlayer.setDataSource(filterFileList.get(0).getAbsolutePath());
                audioPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            holderList.get(0).progressBarIcon.setVisibility(View.VISIBLE);
            audioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    isPlaying = false;
                    holderList.get(0).progressBarIcon.setVisibility(View.GONE);
                    playPause_iv.setImageResource(R.drawable.ic_play_circle_outline_white);
                    audioFileDuration_tv.setText(new Utility().convertFileDuration(filterFileList.get(0)));
                }
            });
            audioFileTitle_tv.setText(filterFileList.get(0).getName());
            audioFileDuration_tv.setText(new Utility().convertFileDuration(filterFileList.get(0)));

            isPlaying = true;
            playPause_iv.setImageResource(R.drawable.ic_pause_circle_outline_white);
            //audioPlayer.start();
        }

        audioPlayer.start();

        /** Show UI duration **/
        runPlay = new Runnable() {
            @Override
            public void run() {
                if (audioPlayer != null) {
                    if (audioPlayer.isPlaying()) {
                        audioSeekBarr.setVisibility(View.VISIBLE);
                        audioSeekBarr.setMax(audioPlayer.getDuration());
                        audioSeekBarr.setProgress(audioPlayer.getCurrentPosition());
                        audioFileDuration_tv.setText(getTimeString(audioPlayer.getCurrentPosition()));
                        audioFileDuration_tv.postDelayed(this, 250);
                    } else {
                        audioSeekBarr.setVisibility(View.GONE);
                        audioFileDuration_tv.setText(getTimeString(audioPlayer.getDuration()));
                        audioFileDuration_tv.removeCallbacks(this);
                    }
                }else {
                    audioSeekBarr.setVisibility(View.GONE);
                    audioFileTitle_tv.setText("");
                    audioFileDuration_tv.setText("");
                    audioFileDuration_tv.removeCallbacks(this);
                }
            }
        };
        audioFileDuration_tv.postDelayed(runPlay, 250);
    }

    private void pause(){
        if (audioPlayer != null){
            audioPlayer.pause();
        }
    }

    private void previous(int index){
        if (audioPlayer == null){
            try {
                audioPlayer = new MediaPlayer();
                audioPlayer.setDataSource(filterFileList.get(0).getAbsolutePath());
                audioPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            holderList.get(0).progressBarIcon.setVisibility(View.VISIBLE);
            audioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    isPlaying = false;
                    holderList.get(0).progressBarIcon.setVisibility(View.GONE);
                    playPause_iv.setImageResource(R.drawable.ic_play_circle_outline_white);
                    audioFileDuration_tv.setText(new Utility().convertFileDuration(filterFileList.get(0)));
                }
            });
            audioFileTitle_tv.setText(filterFileList.get(0).getName());
            audioFileDuration_tv.setText(new Utility().convertFileDuration(filterFileList.get(0)));
            play();

        }else{
            stopPlayer();
            audioPlayer = new MediaPlayer();
            try {
                index--;
                if (index > -1){
                    audioPlayer.setDataSource(filterFileList.get(index).getAbsolutePath());
                    audioPlayerPosition--;
                }else {
                    audioPlayerPosition = filterFileList.size()-1 ;
                    audioPlayer.setDataSource(filterFileList.get(filterFileList.size()-1).getAbsolutePath());
                }
                audioPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            holderList.get(audioPlayerPosition).progressBarIcon.setVisibility(View.VISIBLE);
            audioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    isPlaying = false;
                    holderList.get(audioPlayerPosition).progressBarIcon.setVisibility(View.GONE);
                    playPause_iv.setImageResource(R.drawable.ic_play_circle_outline_white);
                    audioFileDuration_tv.setText(new Utility().convertFileDuration(filterFileList.get(audioPlayerPosition)));
                }
            });
            audioFileTitle_tv.setText(filterFileList.get(audioPlayerPosition).getName());
            audioFileDuration_tv.setText(new Utility().convertFileDuration(filterFileList.get(audioPlayerPosition)));
            play();
        }
    }

    private void next(int index){
        if (audioPlayer == null){
            try {
                audioPlayer = new MediaPlayer();
                audioPlayer.setDataSource(filterFileList.get(0).getAbsolutePath());
                audioPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            holderList.get(0).progressBarIcon.setVisibility(View.VISIBLE);
            audioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    isPlaying = false;
                    holderList.get(0).progressBarIcon.setVisibility(View.GONE);
                    playPause_iv.setImageResource(R.drawable.ic_play_circle_outline_white);
                    audioFileDuration_tv.setText(new Utility().convertFileDuration(filterFileList.get(0)));
                }
            });
            audioFileTitle_tv.setText(filterFileList.get(0).getName());
            audioFileDuration_tv.setText(new Utility().convertFileDuration(filterFileList.get(0)));
            play();

        }else{
            stopPlayer();
            audioPlayer = new MediaPlayer();
            try {
                index++;
                if (index < filterFileList.size()-1){
                    audioPlayerPosition++;
                    audioPlayer.setDataSource(filterFileList.get(index).getAbsolutePath());
                }else {
                    audioPlayerPosition = 0;
                    audioPlayer.setDataSource(filterFileList.get(0).getAbsolutePath());
                }
                audioPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            holderList.get(audioPlayerPosition).progressBarIcon.setVisibility(View.VISIBLE);
            audioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    isPlaying = false;
                    holderList.get(audioPlayerPosition).progressBarIcon.setVisibility(View.GONE);
                    playPause_iv.setImageResource(R.drawable.ic_play_circle_outline_white);
                    audioFileDuration_tv.setText(new Utility().convertFileDuration(filterFileList.get(audioPlayerPosition)));
                }
            });
            audioFileTitle_tv.setText(filterFileList.get(audioPlayerPosition).getName());
            audioFileDuration_tv.setText(new Utility().convertFileDuration(filterFileList.get(audioPlayerPosition)));
            play();
        }
    }

    private void stop(){
        if (audioPlayer != null) {
            this.onAudioFileProgress(false, AudioFileIconProgress);
            audioPlayer.release();
            audioPlayer = null;
            audioSeekBarr.setVisibility(View.GONE);
            audioFileDuration_tv.removeCallbacks(runPlay);
            audioFileDuration_tv.setText("");
            audioFileTitle_tv.setText("");
        }
    }

    private void stopPlayer(){
        if (audioPlayer != null){
            audioPlayer.release();
            this.onAudioFileProgress(false, AudioFileIconProgress);
            audioPlayer = null;
            holderList.get(audioPlayerPosition).progressBarIcon.setVisibility(View.GONE);
        }
    }

    private String getTimeString(long millis) {
        StringBuffer buf = new StringBuffer();

        //int hours = (int) (millis / (1000 * 60 * 60));
        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

        buf/*.append(String.format("%02d", hours))
            .append(":")*/
            .append(String.format("%02d", minutes))
            .append(":")
            .append(String.format("%02d", seconds));

        return buf.toString();
    }

    private void handleSeekbar(){
        audioSeekBarr.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (audioPlayer != null && fromUser) {
                    audioPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        stopPlayer();
    }

}
