package com.example.audiotext.pages;

import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;

import com.example.audiotext.R;
import com.example.audiotext.pages.fragments.ImageText;
import com.example.audiotext.pages.fragments.MyAudios;
import com.example.audiotext.pages.fragments.Settings;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private static final String TAG = Home.class.getSimpleName();

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private NavigationView navigationView;

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
        }

        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
}
