package com.example.audiotext.utility;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;
import android.content.Context;

import java.io.File;

public class Utility {
    private final String TAG = Utility.class.getSimpleName();

    public String convertFileDuration(Context mContext, File file){
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        String seconds = "null";
        String minutes = "null";

        try{
            Log.e("File path: ", ""+Uri.parse(file.getAbsolutePath()));
            metaRetriever.setDataSource(file.getAbsolutePath());
            String duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            long dur = Long.parseLong(duration);
            seconds = String.valueOf((dur % 60000) / 1000);
            Log.e("seconds", seconds);

            minutes = String.valueOf(dur / 60000);
            Log.e("minutes", minutes);

            if (seconds.length() == 1) {
                Log.e(TAG, "0" + minutes + ":0" + seconds);
            }else {
                Log.e(TAG, "0" + minutes + ":" + seconds);
            }
        }catch (Exception e){
            String stackTrace = "";
            for (int x=0; x<e.getStackTrace().length; x++) {
                stackTrace += e.getStackTrace()[x] + "\n";
            }
            Log.e(TAG, "Error[Utility][1] => Message :\n"+e.getMessage()+"\nStackTrace: "+stackTrace);
            showLongDurationToast(mContext, "Error[Utility][1] => Message :\n"+e.getMessage(), 10000);
        }

        return minutes + ":" + seconds;
    }

    public void showLongDurationToast(Context mContext, String message, int toastDurationInMilliSeconds) {
        // Set the toast and duration
       // int toastDurationInMilliSeconds = 10000;
        final Toast mToastToShow = Toast.makeText(mContext, message, Toast.LENGTH_LONG);

        // Set the countdown to display the toast
        CountDownTimer toastCountDown;
        toastCountDown = new CountDownTimer(toastDurationInMilliSeconds, 1000 /*Tick duration*/) {
            public void onTick(long millisUntilFinished) {
                mToastToShow.show();
            }
            public void onFinish() {
                mToastToShow.cancel();
            }
        };

        // Show the toast and starts the countdown
        mToastToShow.show();
        toastCountDown.start();
    }
}
