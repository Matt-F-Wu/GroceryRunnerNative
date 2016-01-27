package com.parse.starter;

/**
 * Created by HaoWu on 1/7/2016.
 */

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MsgAdapter extends ArrayAdapter<String[]> {
    private final Context context;
    private final int[] fields;
    private final int resource;
    private final List<String[]> values;
    private List<Integer> backgrounds;
    private List<Integer> alignment;

    public MsgAdapter(Context context, int resource, int[] fields, List<String[]> values) {
        super(context, resource, values);
        this.context = context;
        this.values = values;
        this.fields = fields;
        this.resource = resource;
        this.backgrounds = null;
        this.alignment = null;
    }

    public MsgAdapter(Context context, int default_resource, List<Integer> drawable_resources, List<Integer> alignment, int[] fields, List<String[]> values) {
        super(context, default_resource, values);
        this.context = context;
        this.values = values;
        this.fields = fields;
        this.resource = default_resource;
        this.backgrounds = drawable_resources;
        this.alignment = alignment;
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

        if(alignment != null){
            /*Value > 0, set margin left; value < 0, set margin right*/
            int margin = alignment.get(position);
            if(margin > 0){
                LinearLayout rowLayout = (LinearLayout) rowView.findViewById(R.id.request_row_container);
                LinearLayout.LayoutParams layoutParams =
                        (LinearLayout.LayoutParams) rowLayout.getLayoutParams();
                /*Convert margin from dip to pixles*/
                float d = context.getResources().getDisplayMetrics().density;
                int margin_px = (int)(margin * d);
                layoutParams.setMargins(margin_px, 0, 0, 0);
            }
        }

        for (int i=0; i < fields.length; i++){
            View view = rowView.findViewById(fields[i]);
            if(view instanceof TextView){
                TextView textView = (TextView) view;
                textView.setText(values.get(position)[i]);
            }
            else if (view instanceof Button) {
                Button button = (Button) view;
                button.setText(values.get(position)[i]);
            }
            else if (view instanceof ImageView) {
                ImageView imageView = (ImageView) view;
                imageView.setImageResource(Integer.parseInt(values.get(position)[i]));
            }
            else if (view instanceof RatingBar) {
                RatingBar rBar = (RatingBar) view;
                rBar.setRating(Float.parseFloat(values.get(position)[i]));
            }
            else{
                Log.d("MsgAdapter Error: ", "Does not support your view type.");
            }
        }


        return rowView;
    }
}



