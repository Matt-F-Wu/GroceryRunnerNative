package com.parse.favourama;

/**
 * Created by HaoWu on 1/7/2016.
 */

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.util.Linkify;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import com.parse.ParseInstallation;
import com.parse.ParseGeoPoint;
import java.lang.Double;

public class MsgAdapter extends ArrayAdapter<String[]> {
    private final Context context;
    private final int[] fields;
    private final int resource;
    private final List<String[]> values;
    private List<Integer> backgrounds;
    private List<String> images;
    private ParseInstallation installation;


    public MsgAdapter(Context context, int default_resource, List<Integer> drawable_resources, List<String> images, int[] fields, List<String[]> values, ParseInstallation ins) {
        super(context, default_resource, values);
        this.context = context;
        this.values = values;
        this.fields = fields;
        this.resource = default_resource;
        this.backgrounds = drawable_resources;
        this.images = images;
        this.installation = ins;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(resource, parent, false);

        if(backgrounds != null) {
            LinearLayout rowLayout = (LinearLayout) rowView.findViewById(R.id.request_row_container);
            rowLayout.setBackgroundResource(backgrounds.get(position));
        }

        if(images != null){
            if (images.get(position) != null){

                ImageView user_pic_view = (ImageView) rowView.findViewById(R.id.request_user_picture);

                File iFile = new File(StarterApplication.getUserFilesDir(), DownloadImageTask.reqImgPrefix + images.get(position));
                //Try to read from file if the image exists on file
                if(iFile.exists()){
                    Log.d("Something", "Fishy");
                    Bitmap bitmap = BitmapFactory.decodeFile(iFile.getPath());
                    if(bitmap != null){
                        user_pic_view.setImageBitmap(bitmap);
                    }else{
                        new DownloadImageTask(user_pic_view)
                                .execute(images.get(position));
                    }
                }else {
                    new DownloadImageTask(user_pic_view)
                            .execute(images.get(position));
                }

            }
        }


        for (int i=0; i < fields.length; i++){
            View view = rowView.findViewById(fields[i]);
            String value = values.get(position)[i];
            if(view instanceof TextView){
                TextView textView = (TextView) view;
                textView.setText(value);
                Linkify.addLinks(textView, Linkify.WEB_URLS);
            }
            else if (view instanceof Button) {
                Button button = (Button) view;
                button.setText(value);
            }
            else if (view instanceof ImageView) {
                ImageView imageView = (ImageView) view;
                imageView.setImageResource(Integer.parseInt(value));
            }
            else if (view instanceof RatingBar) {
                RatingBar rBar = (RatingBar) view;
                rBar.setRating(Float.parseFloat(value));
            }
            else{
                Log.d("MsgAdapter Error: ", "Does not support your view type.");
            }
        }
        
        TextView distV = (TextView) rowView.findViewById(R.id.r_distance_to_receiver);

        ParseGeoPoint senderLoc = new ParseGeoPoint(Double.parseDouble(values.get(position)[5]), Double.parseDouble(values.get(position)[6]));


    if(installation.get("location") != null) {

        String dis_str = new DecimalFormat("#.# km").format(senderLoc.distanceInKilometersTo((ParseGeoPoint) installation.get("location")));

        distV.setText(dis_str);
    }else{
        distV.setText("Unknown Distance");
    }
        return rowView;
    }
}



