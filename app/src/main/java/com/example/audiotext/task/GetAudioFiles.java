package com.example.audiotext.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.example.audiotext.interfaces.GetFilesListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class GetAudioFiles extends AsyncTask<Void, Void, ArrayList<File>> {
    private final String TAG = GetAudioFiles.class.getSimpleName();

    private Context mContext;
    private GetFilesListener mGetAudioFiles;
    private String title;

    public GetAudioFiles(Context mContext, GetFilesListener mGetAudioFiles, String title){
        this.mContext = mContext;
        this.mGetAudioFiles = mGetAudioFiles;
        this.title = title;
    }

    @Override
    protected ArrayList<File> doInBackground(Void... voids) {
        ArrayList<File> filteredArray = null;

        Log.e(TAG, "doInBackground => title: "+title);
        if (title != null) {
            filteredArray = new ArrayList<>();
            ArrayList<File> files = null;

            try{
                String directoryPath = Environment.getExternalStorageDirectory().toString() + "/AudioText/sounds";
                Log.e("Files", "Path: " + directoryPath);
                File directory = new File(directoryPath);
                Log.e(TAG, "directory: "+directory);
                files = new ArrayList<>(Arrays.asList(directory.listFiles()));

                Log.e("Files", "Size: " + files.size());
                if (title.equals("")) {
                    filteredArray = files;
                } else {
                    for (int i = 0; i < files.size(); i++) {
                        Log.d("Files", "FileName:" + files.get(i).getName());
                        if (files.get(i).getName().contains(title)) {
                            filteredArray.add(files.get(i));
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return filteredArray;
    }

    @Override
    protected void onPostExecute(ArrayList<File> files) {
        super.onPostExecute(files);
        Log.e(TAG, "onPostExecute files size: "+files.size());
        mGetAudioFiles.onGetFilesListener(files);
    }
}
