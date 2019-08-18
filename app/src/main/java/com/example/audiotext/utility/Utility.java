package com.example.audiotext.utility;

import android.media.MediaMetadataRetriever;
import android.util.Log;

import java.io.File;

public class Utility {
    private final String TAG = Utility.class.getSimpleName();

    public String convertFileDuration(File file){
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(file.getAbsolutePath());
        String duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

        long dur = Long.parseLong(duration);
        String seconds = String.valueOf((dur % 60000) / 1000);
        Log.e("seconds", seconds);

        String minutes = String.valueOf(dur / 60000);
        Log.e("minutes", minutes);

        if (seconds.length() == 1) {
            Log.e(TAG, "0" + minutes + ":0" + seconds);
        }else {
            Log.e(TAG, "0" + minutes + ":" + seconds);
        }
        return minutes + ":" + seconds;
    }
}
