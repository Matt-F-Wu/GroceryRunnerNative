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

package com.parse.favourama;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper to simplify crop image work like starting pick-image acitvity and handling camera/gallery intents.<br>
 * The goal of the helper is to simplify the starting and most-common usage of image cropping and not
 * all porpose all possible scenario one-to-rule-them-all code base. So feel free to use it as is and as
 * a wiki to make your own.<br>
 * Added value you get out-of-the-box is some edge case handling that you may miss otherwise, like the
 * stupid-ass Android camera result URI that may differ from version to version and from device to device.
 */
public final class CropImage {

    //region: Fields and Consts

    /**
     * The key used to pass crop image source URI to {@link ACTImgCrop}.
     */
    public static final String CROP_IMAGE_EXTRA_SOURCE = "CROP_IMAGE_EXTRA_SOURCE";

    /**
     * The key used to pass crop image options to {@link ACTImgCrop}.
     */
    public static final String CROP_IMAGE_EXTRA_OPTIONS = "CROP_IMAGE_EXTRA_OPTIONS";

    /**
     * The key used to pass crop image result data back from {@link ACTImgCrop}.
     */
    public static final String CROP_IMAGE_EXTRA_RESULT = "CROP_IMAGE_EXTRA_RESULT";

    /**
     * The request code used to start pick image activity to be used on result to identify the this specific request.
     */
    public static final int PICK_IMAGE_CHOOSER_REQUEST_CODE = 200;

    /**
     * The request code used to start {@link ACTImgCrop} to be used on result to identify the this specific
     * request.
     */
    public static final int CROP_IMAGE_ACTIVITY_REQUEST_CODE = 203;

    /**
     * The result code used to return error from {@link ACTImgCrop}.
     */
    public static final int CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE = 204;
    //endregion

    private CropImage() {
    }

    /**
     * Create a new bitmap that has all pixels beyond the oval shape transparent.
     * Old bitmap is recycled.
     */
    public static Bitmap toOvalBitmap(@NonNull Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);

        int color = 0xff424242;
        Paint paint = new Paint();

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);

        RectF rect = new RectF(0, 0, width, height);
        canvas.drawOval(rect, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, 0, 0, paint);

        bitmap.recycle();

        return output;
    }

    /**
     * Start an activity to get image for cropping using chooser intent that will have all the available
     * applications for the device like camera (MyCamera), galery (Photos), store apps (Dropbox), etc.<br>
     * Use "pick_image_intent_chooser_title" string resource to override pick chooser title.
     *
     * @param activity the activity to be used to start activity from
     */
    public static void startPickImageActivity(@NonNull Activity activity) {
        activity.startActivityForResult(getPickImageChooserIntent(activity), PICK_IMAGE_CHOOSER_REQUEST_CODE);
    }

    /**
     * Create a chooser intent to select the  source to get image from.<br>
     * The source can be camera's  (ACTION_IMAGE_CAPTURE) or gallery's (ACTION_GET_CONTENT).<br>
     * All possible sources are added to the intent chooser.<br>
     * Use "pick_image_intent_chooser_title" string resource to override chooser title.
     *
     * @param context used to access Android APIs, like content resolve, it is your activity/fragment/widget.
     */
    public static Intent getPickImageChooserIntent(@NonNull Context context) {
        return getPickImageChooserIntent(context, context.getString(R.string.pick_image_intent_chooser_title), false);
    }

    /**
     * Create a chooser intent to select the  source to get image from.<br>
     * The source can be camera's  (ACTION_IMAGE_CAPTURE) or gallery's (ACTION_GET_CONTENT).<br>
     * All possible sources are added to the intent chooser.
     *
     * @param context used to access Android APIs, like content resolve, it is your activity/fragment/widget.
     * @param title the title to use for the chooser UI
     * @param includeDocuments if to include KitKat documents activity containing all sources
     */
    public static Intent getPickImageChooserIntent(@NonNull Context context, CharSequence title, boolean includeDocuments) {

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();

        // collect all camera intents
        allIntents.addAll(getCameraIntents(context, packageManager));

        // collect all gallery intents
        allIntents.addAll(getGalleryIntents(packageManager, includeDocuments));

        Intent target;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            target = new Intent();
        } else {
            target = allIntents.get(allIntents.size() - 1);
            allIntents.remove(allIntents.size() - 1);
        }

        // Create a chooser from the main  intent
        Intent chooserIntent = Intent.createChooser(target, title);

        // Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }

    /**
     * Get all Camera intents for capturing image using device camera apps.
     */
    public static List<Intent> getCameraIntents(@NonNull Context context, @NonNull PackageManager packageManager) {

        List<Intent> allIntents = new ArrayList<>();

        // Determine Uri of camera image to  save.
        Uri outputFileUri = getCaptureImageOutputUri(context);

        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

        return allIntents;
    }

    /**
     * Get all Gallery intents for getting image from one of the apps of the device that handle images.
     */
    public static List<Intent> getGalleryIntents(@NonNull PackageManager packageManager, boolean includeDocuments) {
        List<Intent> intents = new ArrayList<>();
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            intents.add(intent);
        }

        // remove documents intent
        if (!includeDocuments) {
            for (Intent intent : intents) {
                if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                    intents.remove(intent);
                    break;
                }
            }
        }
        return intents;
    }


    /**
     * Get URI to image received from capture  by camera.
     *
     * @param context used to access Android APIs, like content resolve, it is your activity/fragment/widget.
     */
    public static Uri getCaptureImageOutputUri(@NonNull Context context) {
        Uri outputFileUri = null;
        File getImage = context.getExternalCacheDir();
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "pickImageResult.jpeg"));
        }
        return outputFileUri;
    }

    /**
     * Get the URI of the selected image from {@link #getPickImageChooserIntent(Context)}.<br>
     * Will return the correct URI for camera and gallery image.
     *
     * @param context used to access Android APIs, like content resolve, it is your activity/fragment/widget.
     * @param data the returned data of the  activity result
     */
    public static Uri getPickImageResultUri(@NonNull Context context, @Nullable Intent data) {
        boolean isCamera = true;
        if (data != null && data.getData() != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        return isCamera || data.getData() == null ? getCaptureImageOutputUri(context) : data.getData();
    }

}