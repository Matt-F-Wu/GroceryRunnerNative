package com.parse.starter;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

/**
 * Created by HaoWu on 6/13/2016.
 */
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String imageID = urls[0];
        Bitmap bitmap = null;

        ParseQuery<ParseObject> query = ParseQuery.getQuery("ImageBox");
        try {
            ParseObject parseObject = query.get(imageID);
            return ImageChannel.StringToBitMap(ImageChannel.getImageString(parseObject));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    protected void onPostExecute(Bitmap result) {
        if(result != null) {
            bmImage.setImageBitmap(result);
        }
    }
}
