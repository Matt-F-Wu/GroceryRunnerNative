package com.parse.favourama;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import android.os.AsyncTask;
import android.os.Build;

import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;



/**
 * Created by HaoWu on 6/18/2016.
 */
public class ImageChannel {

    public static String file_pre = "FavouramaIMG";
    public static String file_self = "FavouramaSelfIMG";

    public static void makeImageBox(Bitmap bitmap, final Activity activity){
        
        final long mId = System.currentTimeMillis();
        ((ACTMsg) activity).setPostDelayed(mId);
        /*Hao: 2 seconds delay before showing progress bar*/
        
        final LinkedList<String> imgData = BitMapToString(bitmap);
        if(imgData == null) {
            Toast.makeText(activity.getApplicationContext(),
                    "Image is too big or is corrupted, please try a smaller one! >_<", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        final ParseObject parseObject = new ParseObject("ImageBox");

        ParseACL parseACL = new ParseACL();
        parseACL.setPublicReadAccess(true);
        parseACL.setPublicWriteAccess(true);
        parseObject.setACL(parseACL);

        final String whole = fillImageBox(parseObject, imgData, true);

        parseObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

                ((ACTMsg) activity).hideSendPBar(mId);

                if (e != null) {
//                    Toast.makeText(activity.getApplicationContext(),
//                            "Could not send image, please try later!", Toast.LENGTH_LONG)
//                            .show();
                    String msg = "Could not send: ";
                    if(e.getCode() == 100){
                        /*Poor internet connection*/
                        msg += "please check you internet connection.";
                    }else{
                        msg += e.getMessage();
                    }
                    ((ACTMsg) activity).showErrorDialog(null, msg);

                } else {
                    String imageID = parseObject.getObjectId();
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("img", whole);
                        MyThreads.fileWrite(jsonObject, file_self + imageID);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }

                    ((ACTMsg) activity).SendPictureHelper(imageID);
                }
            }
        });

    }

    public static LinkedList<String> BitMapToString(Bitmap bitmap){
        if(bitmap == null) return null;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        /*Maybe compress to WEBP to shrink size, since we can't convert to gif >_<*/
        byte [] b;
        String temp;

        int i = 70; /*Compression ratio*/
        /*check if string is larger than 128KB*/
        do {
            bitmap.compress(Bitmap.CompressFormat.JPEG, i, baos);
            b = baos.toByteArray();
            baos.reset();
            temp = Base64.encodeToString(b, Base64.DEFAULT);
            //Log.d("IMGSIZE", temp.length() * 2 / 1024 + "KB");
            i-=10;
        } while ( temp.length() * 2 / 1024 > 127 && i >=0 );


        LinkedList<String> imageList = new LinkedList<>();
        while(temp.length() > 127 * 1024 / 2){
            imageList.add(temp.substring(0, 127 * 1024 / 2));
            temp = temp.substring(127 * 1024 / 2);
        }

        imageList.add(temp);

        return imageList;
    }

    public static LinkedList<String> BitMapToStringPNG(Bitmap bitmap){
        if(bitmap == null) return null;

        LinkedList<String> imageList = new LinkedList<>();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        /*Maybe compress to WEBP to shrink size, since we can't convert to gif >_<*/
        byte [] b;
        String temp;

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        b = baos.toByteArray();
        baos.reset();
        temp = Base64.encodeToString(b, Base64.DEFAULT);


        while(temp.length() > 127 * 1024 / 2){
            imageList.add(temp.substring(0, 127 * 1024 / 2));
            temp = temp.substring(127 * 1024 / 2);
        }

        imageList.add(temp);

        return imageList;
    }


    public static Bitmap StringToBitMap(String encodedString){
        if (encodedString == null) return null;
        try {
            byte [] encodeByte=Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap=BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            Log.d("TOBITMAP", e.getMessage());
            return null;
        }
    }

    public static void displayImage(final ImageView view, String name, Context context){
        /*Hao: Get imaged stored in object through ID query*/
        if (name == null || name.isEmpty()){
            Log.d("DATA_EMPTY", "FILE NOT SAVED PROPERLY");
        }else{
            LinkedList<JSONObject> jsonObjectArrayList = new LinkedList<>();
            MyThreads.readLine(new File(StarterApplication.getUserFilesDir(), name), jsonObjectArrayList, context);
            try {
                JSONObject imgj = jsonObjectArrayList.poll();
                if(imgj != null) {
                    Bitmap bitmap = StringToBitMap(imgj.getString("img"));
                    if (bitmap!=null) view.setImageBitmap(bitmap);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveImageToFile (final String data, final Context context, final Activity activity){
        if (data == null || data.isEmpty()){
            Log.d("DATA_EMPTY", "WHAT COULD BE THE REASON?");
        }else{
            ParseQuery<ParseObject> query = ParseQuery.getQuery("ImageBox");
            query.getInBackground(data, new GetCallback<ParseObject>() {
                public void done(ParseObject object, ParseException e) {
                    if (e == null) {
                        String imgData = getImageString(object);
                        if (imgData != null) {

                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("img", imgData);
                                MyThreads.fileWrite(jsonObject, file_pre + data);
                                eraseImageFromCloud(object);
                                if (StarterApplication.isInMessage()) {
                                    // If in message interface, show right after the file write
                                    ((ACTMsg) activity).chat_show_image(data, ChatMessage.PICTURE_TYPE);
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }

                        }
                    } else {
                        //do nothing for now
                        Log.d("error", e.getMessage());
                    }
                }
            });
        }
    }

    public static void eraseImageFromCloud(Object parseObject){
        if (parseObject == null) return;
        ParseObject imgBox = (ParseObject) parseObject;
        if( !imgBox.has("num") ) return;
        if(parseObject != null){
            try {
                int num = imgBox.getInt("num");
                for (int i = 1; i <= num; i++){
                    imgBox.getParseObject("data" + i).delete();
                }
                imgBox.delete();
            } catch (ParseException e) {
                Log.d("DELETEIMG", "FAIL");
                e.printStackTrace();
            }
        }
    }

    public static String fillImageBox(ParseObject parseObject, LinkedList<String> imgData, boolean publicWrite){
        String data = "";
        int i = 1;
        for (String s : imgData){
            ParseObject p = new ParseObject("ImageData");
            p.put("data", s);
            if(publicWrite) {
                ParseACL parseACL = new ParseACL();
                parseACL.setPublicReadAccess(true);
                parseACL.setPublicWriteAccess(true);
                p.setACL(parseACL);
            }
            p.saveInBackground();
            data += s;
            parseObject.put("data" + i, p);
            i++;
        }

        parseObject.put("num", i - 1);
        return data;
    }

    public static String getImageString(ParseObject parseObject) {
        if(parseObject == null) return null;
        String imgData = "";
        LinkedList<String> res = new LinkedList<>();
        int num = parseObject.getInt("num");
        for (int i = 1; i <= num; i++) {
            final ParseObject obj = parseObject.getParseObject("data" + i);
            try {
                obj.fetchIfNeeded();
                res.add(obj.getString("data"));
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }

        for (String s : res){
            imgData += s;
        }

        Log.d("LENGHT", "is " + imgData.length());

        return imgData;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
    
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            // Using OR logic to make sure both dimensions are around req 
            while ((halfHeight / inSampleSize) >= reqHeight
                    || (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }

        }

        return inSampleSize;
    }

    public static Bitmap decodeScaledDownBitmap(String imgPath) {

        int reqWidth = 350;
        int reqHeight = 450;
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgPath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imgPath, options);
    }

    public static Bitmap getBitmapFromLink(String img_url){
        if(img_url.isEmpty()) return null;

        URL url;
        try {
            url = new URL(img_url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        Bitmap img = null;
        try {
            img = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return img;
    }

    public static class DownloadTPBGTask extends AsyncTask<String, Void, Bitmap> {

        View container;
        Context context;
        Boolean saveToFile;

        public DownloadTPBGTask(View v, Context c, Boolean s) {
            this.container = v;
            this.context = c;
            this.saveToFile = s;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap bitmap = getBitmapFromLink(urls[0]);

            if(saveToFile && bitmap != null){
                File imgf = new File(StarterApplication.getUserFilesDir(), "TOPICSBoardImg.png");
                if(imgf.exists()) imgf.delete();
                try {
                    FileOutputStream fos = new FileOutputStream(imgf);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.d("TPIMG", "File can't be opened: " + e.getMessage());
                } catch (IOException e) {
                    Log.d("TPIMG", "IO Error: " + e.getMessage());
                }
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                container.setBackground(new BitmapDrawable(context.getResources(), result));
            }else{
                container.setBackgroundDrawable(new BitmapDrawable(context.getResources(), result));
            }
        }

    }

}
