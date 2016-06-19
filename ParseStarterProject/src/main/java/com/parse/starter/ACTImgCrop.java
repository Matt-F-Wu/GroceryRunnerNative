package com.parse.starter;

import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Jeremy on 6/6/2016.
 */
public class ACTImgCrop extends AppCompatActivity{

    private MainFragment mCurrentFragment;

    private Uri mCropImageUri;

    private String picture_filename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.img_crop_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.image_crop_toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            setMainFragmentByPreset(CropDemoPreset.CIRCULAR);
        }

        String img_uri_string = this.getIntent().getStringExtra("imageUri");
        mCropImageUri = Uri.parse(img_uri_string);

    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mCurrentFragment.setImageUri(mCropImageUri);

    }

    public void onClick_img_crop_option(View v){

        if (mCurrentFragment != null) {

            int view_id = v.getId();
            mCurrentFragment.onOptionsItemSelected(view_id);
        }


    }

    private void setMainFragmentByPreset(CropDemoPreset demoPreset) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance(demoPreset))
                .commit();
    }


    public void setCurrentFragment(MainFragment fragment) {

        mCurrentFragment = fragment;
    }


}
