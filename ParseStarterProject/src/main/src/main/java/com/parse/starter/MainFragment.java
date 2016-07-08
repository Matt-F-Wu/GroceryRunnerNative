// "Therefore those skilled at the unorthodox
// are infinite as heaven and earth,
// inexhaustible as the great rivers.
// When they come to an end,
// they begin again,
// like the days and months;
// they die and are reborn,
// like the four seasons."
//
// - Sun Tsu,
// "The Art of War"

package com.parse.starter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.parse.starter.R;
import com.parse.starter.CropImage;
import com.parse.starter.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The fragment that will show the Image Cropping UI by requested preset.
 */
public final class MainFragment extends Fragment
        implements CropImageView.OnSetImageUriCompleteListener, CropImageView.OnGetCroppedImageCompleteListener {

    //region: Fields and Consts

    private CropDemoPreset mDemoPreset;

    private CropImageView mCropImageView;

    private static String picture_filename = "profile_picture.jpg";

    //endregion

    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static MainFragment newInstance(CropDemoPreset demoPreset) {

        MainFragment fragment = new MainFragment();



        Bundle args = new Bundle();
        args.putString("DEMO_PRESET", demoPreset.name());
        fragment.setArguments(args);
        return fragment;
    }


    /**
     * Set the image to show for cropping.
     */
    public void setImageUri(Uri imageUri) {
        mCropImageView.setImageUriAsync(imageUri);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView;
        switch (mDemoPreset) {
//            case RECT:
//                rootView = inflater.inflate(R.layout.fragment_main_rect, container, false);
//                break;
            case CIRCULAR:
                rootView = inflater.inflate(R.layout.fragment_main_oval, container, false);
                break;
            default:
                throw new IllegalStateException("Unknown preset: " + mDemoPreset);
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        mCropImageView = (CropImageView) view.findViewById(R.id.cropImageView);
        mCropImageView.setOnSetImageUriCompleteListener(this);
        mCropImageView.setOnGetCroppedImageCompleteListener(this);

        if (savedInstanceState == null) {
            Toast.makeText(getActivity(), "no image selected", Toast.LENGTH_LONG).show();
        }
    }


    public boolean onOptionsItemSelected(int view_id) {
        if (view_id == R.id.main_action_crop) {
            mCropImageView.getCroppedImageAsync();
            return true;
        } else if (view_id == R.id.main_action_rotate) {
            mCropImageView.rotateImage(90);
            return true;
        }
        return true;
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        mDemoPreset = CropDemoPreset.valueOf(getArguments().getString("DEMO_PRESET"));
        ((ACTImgCrop) context).setCurrentFragment(this);
    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
        mDemoPreset = CropDemoPreset.valueOf(getArguments().getString("DEMO_PRESET"));
        ((ACTImgCrop) activity).setCurrentFragment(this);
    }




    @Override
    public void onDetach() {
        super.onDetach();
        if (mCropImageView != null) {
            mCropImageView.setOnSetImageUriCompleteListener(null);
            mCropImageView.setOnGetCroppedImageCompleteListener(null);
        }
    }

    @Override
    public void onSetImageUriComplete(CropImageView view, Uri uri, Exception error) {
        if (error == null) {
            Toast.makeText(getActivity(), "Image load successful", Toast.LENGTH_SHORT).show();
        } else {
            Log.e("AIC", "Failed to load image by URI", error);
            Toast.makeText(getActivity(), "Image load failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onGetCroppedImageComplete(CropImageView view, Bitmap bitmap, Exception error) {

        handleCropResult(null, bitmap, error);
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//            handleCropResult(result.getUri(), null, result.getError());
//        }
//    }

    private void handleCropResult(Uri uri, Bitmap bitmap, Exception error) {
          if (error == null) {
              Bitmap finalBitmap = mCropImageView.getCropShape() == CropImageView.CropShape.OVAL? CropImage.toOvalBitmap(bitmap): bitmap;
              storeImage(finalBitmap);
          } else {
              Log.e("AIC", "Failed to crop image", error);
              Toast.makeText(getActivity(), "Image crop failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
          }

        Intent i = new Intent();

        i.putExtra("picture_name", picture_filename);
        getActivity().setResult(300, i);
        getActivity().finish();

    }



    private void storeImage(Bitmap image) {

        File pictureFile = getOutputMediaFile(picture_filename);
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
    private File getOutputMediaFile(String picture_name){

        Log.d("JM", "getOutputMediaFile inside");

        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getActivity().getApplicationContext().getPackageName()
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

        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + File.separator + picture_name);

        if(mediaFile != null)
            Log.d("jm", "MEDIA FILE IS NOT NULL");

        return mediaFile;
    }


}
