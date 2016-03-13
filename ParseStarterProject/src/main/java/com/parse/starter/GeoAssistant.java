package com.parse.starter;

import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.parse.ParseObject;

import java.io.IOException;
import java.util.List;

/**
 * Created by HaoWu on 3/12/2016.
 */
public class GeoAssistant {


    public static ParseObject getLocationFromAddress(String strAddress, Context context){

        if(strAddress == null) return null;

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        ParseObject p1 = null;
        p1 = new ParseObject("AlternativeLocation");
        p1.put("address", strAddress);

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address==null) {
                p1.put("latidute", "NF");
                p1.put("longitude", "NF");
                return p1;
            }
            Address location=address.get(0);

            p1.put("latidute",location.getLatitude());
            p1.put("longitude",location.getLongitude());

            return p1;

        }catch (IOException e){
            p1.put("latidute", "NF");
            p1.put("longitude", "NF");
            return p1;
        }
    }
}
