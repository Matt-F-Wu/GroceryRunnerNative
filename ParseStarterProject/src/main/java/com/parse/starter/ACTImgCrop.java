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

    private CropImageViewOptions mCropImageViewOptions = new CropImageViewOptions();

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

        mCurrentFragment.updateCurrentCropViewOptions();
    }

    public void onClick_img_crop_option(View v){

        if (mCurrentFragment != null) {

            int view_id = v.getId();
            mCurrentFragment.onOptionsItemSelected(view_id);
        }



        //store cropped picture in local phone storage
        //storeImage(mCurrentFragment.getImageBitmap());

//        Bitmap bitmap = null;
//        try {
//
//             bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mCurrentFragment.getCropImageViewUri());
//        }catch(Exception e){
//            Log.d("JM", "EXCEPTION " + e.getMessage());
//        }
//        storeImage(bitmap);



//        Intent i = new Intent();
//
//        Log.d("jm", "picture filename " + mCurrentFragment.get_picture_filename());
//
//        i.putExtra("picture_name", mCurrentFragment.get_picture_filename());
//        setResult(300, i);
//        finish();

    }

    private void storeImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d("JM",
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("JM", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("JM", "Error accessing file: " + e.getMessage());
        }
    }


    /** Create a File for saving an image or video */
    private  File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        picture_filename="MI_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + picture_filename);
        return mediaFile;
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



    public void setCurrentOptions(CropImageViewOptions options) {
        mCropImageViewOptions = options;
    }



}
