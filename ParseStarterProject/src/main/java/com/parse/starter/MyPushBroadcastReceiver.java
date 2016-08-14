package com.parse.starter;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by HaoWu on 1/11/2016.
 */
public class MyPushBroadcastReceiver extends ParsePushBroadcastReceiver {

    public static final String PARSE_DATA_KEY = "com.parse.Data";
    private static final String REQUEST_TYPE = "REQUEST";
    private static final String MESSAGE_TYPE = "MESSAGE";
    private static final String PROMO_TYPE = "TOPICS";

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        // deactivate standard notification
        return null;
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        // I am only using the default open behaviour at this moment
        super.onPushOpen(context, intent);
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        JSONObject data = getDataFromIntent(intent);
        // Do something with the data. To create a notification do:
        if (data == null) {
            Log.d("GET PUSH DATA", "FAILED");
        }

        boolean ignore = false;
        String type = null;
        String description = "From: ";

        try {
            type = data.getString("TYPE");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("Push Receive Exception", "failed to retrieve type");
            return;
        }

        /*If application is not visible (aka closed or running in background), write notification to file then process when app resumes.
        When app is visible but is message interface, say A conversation, show notification for messages arrived for any conversation
        other than A, and show notification for rrequests.
        */

        if ( !StarterApplication.isActivityVisible() || (StarterApplication.isActivityVisible() && StarterApplication.isInMessage()) ) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            Intent openIntent = new Intent(context, ACTRequest.class);
            openIntent.putExtra("notification", true);
            PendingIntent pIntent = null;
            if(type.equals(REQUEST_TYPE)){
                /*Go to the main page where requests are displayed*/
                openIntent.putExtra("enter", 1);
                builder.setContentTitle("New Favourama Request(s)");
            }
            else if(type.equals(MESSAGE_TYPE)){
                openIntent.putExtra("enter", 2);
                String fname = data.optString("username");

                if(fname.equals(StarterApplication.getToWhom())){
                    ignore = true;
                }
                if(!ignore) {
                    builder.setContentTitle("New Favourama Message(s)");
                }
            } else if(type.equals(PROMO_TYPE)){
                openIntent.putExtra("enter", 2);
                String admin = "admin";
                if(admin.equals(StarterApplication.getToWhom())){
                    ignore = true;
                }
                if(!ignore) {
                    builder.setContentTitle("New Favourama Topics");
                }
            }

            if(!ignore) {
                //Making notifications, write data to file
                File user_dir = new File(context.getFilesDir(), ParseUser.getCurrentUser().getUsername());
                MyThreads.fileWrite(data, "favouramaNotification.json", user_dir);
                
                openIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                pIntent = PendingIntent.getActivity(context, 0, openIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                description += data.optString("username", "UNKNOWN");

                builder.setContentText(description);
                builder.setSmallIcon(R.drawable.ic_notification);
                builder.setContentIntent(pIntent);
                builder.setAutoCancel(true);
                notificationManager.notify("FavouramaTag", 0, builder.build());
                // OPTIONAL create soundUri and set sound:
                /*builder.setSound(soundUri);*/
                
                return;
            }
        }

        Intent i = new Intent();
        i.putExtra("CONTENT", data.toString());

        if (type.equals(REQUEST_TYPE)) {
            i.setAction("com.parse.favourama.HANDLE_FAVOURAMA_REQUESTS");
            context.sendBroadcast(i);
        } else if (type.equals(MESSAGE_TYPE)) {
            i.setAction("com.parse.favourama.HANDLE_FAVOURAMA_MESSAGES");
            context.sendBroadcast(i);
        } else if (type.equals(PROMO_TYPE)) {
            i.setAction("com.parse.favourama.HANDLE_FAVOURAMA_TOPICS");
            context.sendBroadcast(i);
        }

    }

    private JSONObject getDataFromIntent(Intent intent) {
        JSONObject data = null;
        try {
            data = new JSONObject(intent.getExtras().getString(PARSE_DATA_KEY));
            Log.d("RECDATA", intent.getExtras().get(PARSE_DATA_KEY).toString());
        } catch (JSONException e) {
            // Json was not readable...
            Log.d("PUSH RECEIVE FAILURE", ">>>COULD NOT PROCESS JSON DATA");
        }
        return data;
    }
}
