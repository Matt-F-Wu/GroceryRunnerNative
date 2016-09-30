package com.parse.favourama;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.LinkedList;

/**
 * Created by HaoWu on 1/11/2016.
 */
public class MyPushBroadcastReceiver extends ParsePushBroadcastReceiver {

    public static final String PARSE_DATA_KEY = "com.parse.Data";
    private static final String REQUEST_TYPE = "REQUEST";
    private static final String MESSAGE_TYPE = "MESSAGE";
    private static final String PROMO_TYPE = "TOPICS";
    private static final String RATING_TYPE = "RATING";

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
        ParseUser me = ParseUser.getCurrentUser();
        JSONObject data = getDataFromIntent(intent);
        // Do something with the data. To create a notification do:
        if (data == null) {
            Log.d("GET PUSH DATA", "FAILED");
        }

        boolean ignore = false;
        String type = null;
        String description = "";

        try {
            type = data.getString("TYPE");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("Push Receive Exception", "failed to retrieve type");
            return;
        }

        /*Process rating secretely, fetch new user data and refresh local copy*/
        if(RATING_TYPE.equals(type)){
            me.fetchInBackground();
            return;
        }else if(MESSAGE_TYPE.equals(type)){
            String destination = data.optString("destination");
            if(!destination.equals(me.getUsername())){
                /*This message is intended for a previous installation, which should be ignored*/
                return;
            }
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

            }
            else if(type.equals(MESSAGE_TYPE)){
                openIntent.putExtra("enter", 2);
                String fname = data.optString("username");

                if(StarterApplication.isActivityVisible() && StarterApplication.isInMessage() && fname.equals(StarterApplication.getToWhom())){
                    ignore = true;
                }

            } else if(type.equals(PROMO_TYPE)){
                openIntent.putExtra("enter", 2);
                String admin = "admin";
                if(StarterApplication.isActivityVisible() && StarterApplication.isInMessage() && admin.equals(StarterApplication.getToWhom())){
                    ignore = true;
                }
            }

            if(!ignore) {
                //Making notifications, write data to file
                /* User is null then don't receive anything */
                ParseUser curUser = ParseUser.getCurrentUser();
                if(curUser == null) return;

                File user_dir = new File(context.getFilesDir(), curUser.getUsername());
                MyThreads.fileWrite(data, "favouramaNotification.json", user_dir);

                LinkedList<JSONObject> notis = new LinkedList<>();
                MyThreads.readLine(new File(user_dir, "favouramaNotification.json"), notis, context);

                int req=0, msg=0, tpc=0;

                for (JSONObject noti : notis){
                    String tp = noti.optString("TYPE");
                    if(tp.equals(MESSAGE_TYPE)){
                        msg++;
                    }else if(tp.equals(REQUEST_TYPE)){
                        req++;
                    }else if(tp.equals(PROMO_TYPE)){
                        tpc++;
                    }
                }

                String title = "";

                if(req != 0){
                    title += (req + " New Request(s) ");
                }
                if(msg != 0){
                    title += (msg + " New Message(s) ");
                }
                if(tpc != 0){
                    title += (tpc + " New Topic(s)");
                }

                builder.setContentTitle(title);

                openIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                pIntent = PendingIntent.getActivity(context, 0, openIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                description += data.optString("username", "UNKNOWN");

                if(type.equals(REQUEST_TYPE)){
                    description += (": " + data.optString("note"));
                }else if (type.equals(MESSAGE_TYPE)){
                    description += (": " + data.optString("content"));
                    /*Message type vibrate*/
                    builder.setVibrate(new long[] { 1000, 1000});
                }

                if(req + msg + tpc > 1){
                    description += " ... ...";
                }

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

        } catch (JSONException e) {
            // Json was not readable...
            //Log.d("PUSH RECEIVE FAILURE", ">>>COULD NOT PROCESS JSON DATA");
        }
        return data;
    }
}
