package com.parse.starter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by HaoWu on 6/18/2016.
 */
public class ImageChannel {

    public static String file_pre = "FavouramaIMG";
    public static String file_self = "FavouramaSelfIMG";

    public static void makeImageBox(Bitmap bitmap, final Activity activity){

        final String imgData = BitMapToString(bitmap);
        if(imgData == null) {
            Toast.makeText(activity.getApplicationContext(),
                    "Image is too big, please try a smaller one! >_<", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        final ParseObject parseObject = new ParseObject("ImageBox");
        parseObject.put("data", imgData);
        parseObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null) {
                    Toast.makeText(activity.getApplicationContext(),
                        "Could not send image, please try later!", Toast.LENGTH_LONG)
                        .show();
                }else {
                    String imageID = parseObject.getObjectId();
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("img", imgData);
                        MyThreads.fileWrite(jsonObject, file_self + imageID, activity.getApplicationContext());
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                    ((ACTMsg) activity).SendPictureHelper(imageID);
                }
            }
        });

    }

    public static String BitMapToString(Bitmap bitmap){
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
            Log.d("IMGSIZE", temp.length() * 2 / 1000 + "KB");
            i-=10;
        } while ( temp.length() * 2 / 1000 > 127 && i >=0 );

        Log.d("IMGSIZE", temp.length() * 2 / 1000 + "KB");

        if(temp.length() * 2 / 1000 > 127){
            return null;
        }

        return temp;
    }

    public static String BitMapToStringPNG(Bitmap bitmap){
        if(bitmap == null) return null;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        /*Maybe compress to WEBP to shrink size, since we can't convert to gif >_<*/
        byte [] b;
        String temp;

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        b = baos.toByteArray();
        baos.reset();
        temp = Base64.encodeToString(b, Base64.DEFAULT);

        if(temp.length() * 2 / 1000 > 127){
            return BitMapToString(bitmap);
        }

        return temp;
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
            MyThreads.readLine(new File(context.getFilesDir(), name), jsonObjectArrayList, context);
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
                        String imgData = object.getString("data");
                        if (imgData != null) {

                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("img", imgData);
                                MyThreads.fileWrite(jsonObject, file_pre + data, context);
                                eraseImageFromCloud(object);
                                ((ACTMsg)activity).chat_show_image(data, ChatMessage.PICTURE_TYPE);
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
        if(parseObject != null){
            try {
                ((ParseObject)parseObject).delete();
            } catch (ParseException e) {
                Log.d("DELETEIMG", "FAIL");
                e.printStackTrace();
            }
        }
    }

}
