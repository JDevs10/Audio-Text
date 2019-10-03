package com.example.audiotext.pages;

import android.app.Dialog;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.audiotext.R;
import com.example.audiotext.interfaces.GetFilesListener;
import com.example.audiotext.interfaces.ViewAudioInformation;
import com.example.audiotext.pages.fragments.ImageText;
import com.example.audiotext.pages.fragments.MyAudios;
import com.example.audiotext.pages.fragments.Settings;
import com.example.audiotext.task.GetAudioFiles;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ViewAudioInformation {
    private static final String TAG = Home.class.getSimpleName();

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ImageView icon;

    private ViewAudioInformation mViewAudioInformation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Hides App bar at the top..................................................................
        //getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_homeLayout);
        navigationView = findViewById(R.id.home_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null){
            //getSupportActionBar().hide();
            //default fragment when activity is running
            getSupportActionBar().setTitle("Image Text");
            navigationView.setCheckedItem(R.id.nav_menu_image_text);
            getSupportFragmentManager().beginTransaction().replace(R.id.home_fragment_container, new ImageText()).commit();
        }

        icon = navigationView.getHeaderView(0).findViewById(R.id.home_nav_header_icon_iv);
        icon.setImageResource(R.mipmap.ic_launcher_round);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_menu_image_text:
                toolbar.setTitle("Image Text");
                navigationView.setCheckedItem(R.id.nav_menu_image_text);
                getSupportFragmentManager().beginTransaction().replace(R.id.home_fragment_container, new ImageText()).commit();
                break;

            case R.id.nav_menu_myAudios:
                toolbar.setTitle("My Audios");
                navigationView.setCheckedItem(R.id.nav_menu_myAudios);
                getSupportFragmentManager().beginTransaction().replace(R.id.home_fragment_container, new MyAudios()).commit();
                break;

            case R.id.nav_menu_settings:
                toolbar.setTitle("Settings");
                navigationView.setCheckedItem(R.id.nav_menu_myAudios);
                getSupportFragmentManager().beginTransaction().replace(R.id.home_fragment_container, new Settings()).commit();
                break;

            case R.id.nav_menu_delete_all_audios:
                Dialog dialog = new Dialog(this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_audio_info);
                Home.this.onDeleteAllAudioFiles(dialog);
                dialog.setCancelable(false);
                dialog.show();
        }

        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onViewAudioInformation(ArrayList<File> fileList, Dialog dialog, int index) {

    }

    @Override
    public void onDeleteAllAudioFiles(final Dialog dialog) {
        ImageView close = dialog.findViewById(R.id.dialog_audio_info_close_btn);
        TextView tittle = dialog.findViewById(R.id.dialog_audio_info_tittle);
        TextView text = dialog.findViewById(R.id.dialog_audio_info_text_et);
        Button cancel = dialog.findViewById(R.id.dialog_audio_info_cancel_btn);
        Button delete = dialog.findViewById(R.id.dialog_audio_info_delete_btn);

        ArrayList<File> files = null;
        int audioNumbers = 0;

        try{
            String directoryPath = Environment.getExternalStorageDirectory().toString() + "/AudioText/sounds";
            Log.e("Files", "Path: " + directoryPath);
            File directory = new File(directoryPath);
            Log.e(TAG, "directory: "+directory);
            files = new ArrayList<>(Arrays.asList(directory.listFiles()));
            audioNumbers = files.size();
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(Home.this, "ERROR[101] : Could not get Audios...\nMessage: "+e.getMessage(), Toast.LENGTH_LONG).show();
        }

        tittle.setText("");
        text.setText("You're about to delete all "+audioNumbers+" files from your device.\nAre you sure ?");

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Toast.makeText(Home.this, "Aborted!", Toast.LENGTH_LONG).show();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Toast.makeText(Home.this, "Aborted!", Toast.LENGTH_LONG).show();
            }
        });
        final int finalAudioNumbers = audioNumbers;
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    ArrayList<File> files;
                    String directoryPath = Environment.getExternalStorageDirectory().toString() + "/AudioText/sounds";
                    Log.e("Files", "Path: " + directoryPath);
                    File directory = new File(directoryPath);
                    Log.e(TAG, "directory: "+directory);
                    files = new ArrayList<>(Arrays.asList(directory.listFiles()));

                    Log.e("Files", "Size: " + files.size());
                    for (int i = 0; i < files.size(); i++) {
                        files.get(i).delete();
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(Home.this, "ERROR[101] : Could not delete file or files...\nMessage: "+e.getMessage(), Toast.LENGTH_LONG).show();
                }

                try{
                    if (finalAudioNumbers == 1) {
                        Toast.makeText(Home.this, "One file was deleted!", Toast.LENGTH_LONG).show();
                    } else if (finalAudioNumbers > 1){
                        Toast.makeText(Home.this, "All "+ finalAudioNumbers +" files were deleted!", Toast.LENGTH_LONG).show();
                    }

                    finish();
                    startActivity(getIntent());
                }catch (Exception e){
                    Log.e(TAG, "ERROR[102] : Can't refresh Activity \nMessage: "+e.getMessage());
                }
                dialog.dismiss();
            }
        });
    }
}
