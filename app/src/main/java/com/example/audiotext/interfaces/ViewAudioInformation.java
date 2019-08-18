package com.example.audiotext.interfaces;

import android.app.Dialog;

import java.io.File;
import java.util.ArrayList;

public interface ViewAudioInformation {
    void onViewAudioInformation(ArrayList<File> fileList, Dialog dialog, int index);
}
