package com.example.audiotext.pages;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.audiotext.R;
import com.example.audiotext.database.AppDatabase;
import com.example.audiotext.database.entity.SettingsEntry;
import com.example.audiotext.interfaces.GooglePlayVersion;
import com.example.audiotext.task.GetLatestVersion;

public class Loading extends AppCompatActivity implements GooglePlayVersion {
    private static final String TAG = Loading.class.getSimpleName();

    private ProgressDialog progressDialog;
    private TextView error_tv;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 96;

    /**
     * By JL --- Check Google PlayStore App Version
     */
    private String currentVersion, latestVersion;
    private Dialog dialog;
    private boolean versionChecked = false;

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        db = AppDatabase.getInstance(this);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(Loading.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(Loading.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                /**
                 * Show an explanation to the user *asynchronously* -- don't block
                 * this thread waiting for the user's response! After the user
                 * sees the explanation, try again to request the permission.
                 */

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(Loading.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(Loading.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        }

        error_tv = (TextView)findViewById(R.id.loadingActivity_error);

        /** Google Play Store getting app version / Checking app version **/
        progressDialog = new ProgressDialog(Loading.this);
        progressDialog.setTitle("Google Play Store");
        progressDialog.setMessage("Checking application version...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        defaultSettings();
        getCurrentVersion();
    }

    private void defaultSettings(){
        if (db.settingsDao().getSettings().size() == 0){
            db.settingsDao().deleteAllSettings();
            db.settingsDao().insertSettings(new SettingsEntry(1,50,50));
            Log.e(TAG, "Default settings set: "+db.settingsDao().getSettings().size());
        }else{
            Log.e(TAG, "Default settings active: "+db.settingsDao().getSettings().size());
        }
    }

    private void loadScreen(){
        progressDialog = new ProgressDialog(Loading.this);

        // Set loading...
        progressDialog.setTitle("Configuration");
        progressDialog.setMessage("Loading save configurations...");
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        /** new Thread to Retrieve/Load Configurations **/
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                progressDialog.incrementProgressBy(10);
            }
        };

        /** New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds. **/
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    while (progressDialog.getProgress() <= progressDialog.getMax()) {
                        Thread.sleep(200);
                        handler.sendMessage(handler.obtainMessage());

                        // Create an Intent that will start the Menu-Activity.
                        if (progressDialog.getProgress() == progressDialog.getMax() && versionChecked) {
                            progressDialog.dismiss();
                            startActivity(new Intent(Loading.this, Home.class));

                            //kill the Thread with return
                            return;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getCurrentVersion(){
        Log.e(TAG, " getCurrentVersion() : JL Test Version App");
        GetLatestVersion version = new GetLatestVersion(this,this, false);
        version.execute();
    }

    @Override
    public void onReceiveGooglePlayVersion(String currentVersion, String latestVersion) {
        if(latestVersion != null) {
            //check if version is the same
            if (!currentVersion.equalsIgnoreCase(latestVersion)){
                //check if the 2 first digits (#.#) are the same
                if (!checkVersionDigits(currentVersion).equalsIgnoreCase(checkVersionDigits(latestVersion))){
                    if(!isFinishing()){ //This would help to prevent Error : BinderProxy@45d459c0 is not valid; is your activity running? error
                        showUpdateDialog();
                        progressDialog.dismiss();
                    }
                }else{
                    // if version is correct then proceed
                    progressDialog.dismiss();
                    versionChecked = true;
                    loadScreen();
                }
            }else{
                // if version is correct then proceed
                progressDialog.dismiss();
                versionChecked = true;
                loadScreen();
            }
        }else {
            versionChecked = false;
            error_tv.setVisibility(View.VISIBLE);
            error_tv.setText("\nLoading Error [002] :\nPlayStore application version error !");
            progressDialog.dismiss();
            Toast.makeText(this, "latestVersion == NULL", Toast.LENGTH_LONG).show();

            /** ===== Version debug =====
             * progressDialog.dismiss();
             * versionChecked = true;
             * loadScreen();
             * */
        }
    }

    private String checkVersionDigits(String fullVersion){
        //Log.e(TAG, "checkVersionDigits( "+fullVersion+" )");
        String[] versionSection = fullVersion.split("\\.");
        //Log.e(TAG, "split: "+versionSection[0]+"."+versionSection[1]);
        return versionSection[0]+"."+versionSection[1];
    }

    private void showUpdateDialog(){
        Dialog updateDialog = new Dialog(this);
        updateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        updateDialog.setContentView(R.layout.dialog_playstore_update);
        ImageView close_btn = (ImageView) updateDialog.findViewById(R.id.dialog_playstore_close_btn);
        Button update_btn = (Button) updateDialog.findViewById(R.id.dialog_playstore_update_btn);

        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //kill the app
                System.exit(0);
            }
        });

        update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse
                        ("https://play.google.com/store/apps/details?id=com.analyst.fragmenttest")));
                dialog.dismiss();
            }
        });
        updateDialog.setCancelable(false);
        updateDialog.show();
    }
}
