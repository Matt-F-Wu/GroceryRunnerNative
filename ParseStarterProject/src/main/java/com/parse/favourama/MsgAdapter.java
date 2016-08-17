package com.parse.favourama;

/**
 * Created by HaoWu on 1/7/2016.
 */

import java.util.List;
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

public class MsgAdapter extends ArrayAdapter<String[]> {
    private final Context context;
    private final int[] fields;
    private final int resource;
    private final List<String[]> values;
    private List<Integer> backgrounds;
    private List<String> images;


    public MsgAdapter(Context context, int default_resource, List<Integer> drawable_resources, List<String> images, int[] fields, List<String[]> values) {
        super(context, default_resource, values);
        this.context = context;
        this.values = values;
        this.fields = fields;
        this.resource = default_resource;
        this.backgrounds = drawable_resources;
        this.images = images;
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
                new DownloadImageTask(user_pic_view)
                        .execute(images.get(position));

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


        return rowView;
    }
}



