package com.example.audiotext.interfaces;

import android.widget.ProgressBar;

import com.example.audiotext.adapters.AudioFileAdapter;

import java.io.File;

public interface FilesListener {
    void onAudioFileListener(int index, File file);
    void onAudioFileProgress(Boolean active, ProgressBar progress);
//    void onAudioFilePlay(File file);
//    void onAudioFilePause(File file);
//    void onAudioFilePrevious(File file);
//    void onAudioFileNext(File file);
//    void onAudioFileStop(File file);
}
