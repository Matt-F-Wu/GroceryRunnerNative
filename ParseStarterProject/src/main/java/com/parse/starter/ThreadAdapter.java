package com.parse.starter;

/**
 * Created by HaoWu on 7/14/2016.
 */

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
import android.text.util.Linkify;
import java.util.List;

public class ThreadAdapter extends ArrayAdapter<MyThreads.MsgThread> {
    private final Context context;
    private final int[] fields;
    private final int resource;
    private final List<MyThreads.MsgThread> cvalues;

    public ThreadAdapter(Context context, int resource, int[] fields, List<MyThreads.MsgThread> p_cvalues) {
        super(context, resource, p_cvalues);
        this.context = context;
        this.cvalues = p_cvalues;
        this.fields = fields;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(resource, parent, false);


        for (int i=0; i < fields.length; i++){
            View view = rowView.findViewById(fields[i]);
            String value = cvalues.get(position).spitHeader()[i];
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




