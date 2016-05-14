package com.parse.starter;

import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.parse.ParseGeoPoint;
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
        p1.put("latitude", "NF");
        p1.put("longitude", "NF");

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address==null) {
                p1.put("latitude", "NF");
                p1.put("longitude", "NF");
                return p1;
            }
            Address location=address.get(0);
            if (location != null) {
                p1.put("latitude", location.getLatitude());
                p1.put("longitude", location.getLongitude());
                Log.d("RLOCATION", location.getLatitude() + " " + location.getLongitude());
            }
            return p1;

        }catch (IOException e){
            p1.put("latitude", "NF");
            p1.put("longitude", "NF");
            return p1;
        }
    }

    public static ParseGeoPoint spitGeoPoint(ParseObject addr){
        if (addr == null) return null;

        if(addr.get("latitude").toString().equals("NF") || addr.get("longitude").toString().equals("NF") ){
            return null;
        }else{
            return new ParseGeoPoint(addr.getDouble("latitude"), addr.getDouble("longitude"));
        }
    }
}
