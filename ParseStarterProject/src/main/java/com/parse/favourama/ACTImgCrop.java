package com.parse.favourama;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**
 * Created by Jeremy on 6/6/2016.
 */
public class ACTImgCrop extends AppCompatActivity{

    private MainFragment mCurrentFragment;

    private Uri mCropImageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.img_crop_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.image_crop_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

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


    public void cancelAndReturn(View view) {
        setResult(AppCompatActivity.RESULT_CANCELED);
        finish();
    }
}
