package com.parse.favourama;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by HaoWu on 6/13/2016.
 */
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;
    public static String reqImgPrefix = "FavReqImg";
    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
	
        String imageID = urls[0];
        Bitmap bitmap = null;

	  File iFile = new File(StarterApplication.getUserFilesDir(), reqImgPrefix + imageID);

	  //Try to read from file if the image exists on file
	  if(iFile.exists()){
	  	bitmap = BitmapFactory.decodeFile(iFile.getPath());
		if(bitmap != null) return bitmap;
	  }

        ParseQuery<ParseObject> query = ParseQuery.getQuery("ImageBox");
        try {
            ParseObject parseObject = query.get(imageID);
            bitmap = ImageChannel.StringToBitMap(ImageChannel.getImageString(parseObject));
		try {
                    FileOutputStream fos = new FileOutputStream(iFile);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.d("TPIMG", "File can't be opened: " + e.getMessage());
                } catch (IOException e) {
                    Log.d("TPIMG", "IO Error: " + e.getMessage());
                }
		return bitmap;
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
