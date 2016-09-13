package com.parse.favourama;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.AlertDialog;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;


public class ACTMsg extends AppCompatActivity {
    private String[] header;
    private BroadcastReceiver msgReceiver;
    private ParseUser me;
    //message file global
    private File common_dir;
    private HashSet<Long> msgId;
    //display global
    private ChatAdapter adapter;
    private ListView messagesContainer;
    private String msg_filename;
    private File msg_file;
    static private String conversation_list_filename = "conversation_list.json";
    static private File conversation_file;
    static Activity activity;

    /*Variables for pinch zoom*/
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    private int mode = NONE;
    private PointF start, mid;
    private float oldDist = 1f;
    private Matrix matrix, savedMatrix;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actmsg);

        activity = this;
        me = ParseUser.getCurrentUser();
        Bundle b = this.getIntent().getExtras();
        if (b != null)
            header = b.getStringArray("ThreadHeader");

        TextView headerView = (TextView) findViewById(R.id.msg_thread_header);
        headerView.setText( formatHeader() );

        //manage files
        common_dir = StarterApplication.getUserFilesDir();
        msg_filename = "MSG_"+header[0]+".json";//todo change this later
        msg_file = new File(common_dir, msg_filename);
        conversation_file = new File(common_dir, conversation_list_filename);

        //read back for testing
        //fileRead(conversation_file);


        msgReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //... update ui here
                String action = intent.getAction();


                JSONObject jsonObject;
                String content = intent.getExtras().getString("CONTENT");
                try {
                    jsonObject = new JSONObject(content);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                if(action.equals("com.parse.favourama.HANDLE_FAVOURAMA_REQUESTS")){
                   //Hao: do nothing here
                }
                else if (action.equals("com.parse.favourama.HANDLE_FAVOURAMA_MESSAGES")){
                    /* HAO
                    * */

                    Log.d("MCONTENT", jsonObject.toString());

                    String chat_content = new String();
                    String txt_type =  new String();
                    try{
                        chat_content = jsonObject.getString("content");
                        txt_type = jsonObject.getString("ctype");
                        if (txt_type.equals(ChatMessage.PICTURE_TYPE)){
                            ImageChannel.saveImageToFile(chat_content, getApplicationContext(), activity);

                        }else{
                            ChatMessage chatmsg = new ChatMessage();
                            chatmsg.setId(0);//todo do I need an id?
                            chatmsg.setMe(false);
                            chatmsg.setMessage(chat_content);
                            chatmsg.setMessageType(txt_type);
                            chatmsg.setDate(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.CANADA).format(new Date()));
                            displayMessage(chatmsg);
                        }
                    }catch(org.json.JSONException e){
                        e.printStackTrace();
                    }

                    //updateDisplay(jsonObject);
                }
                else if(action.equals("com.parse.favourama.HANDLE_FAVOURAMA_RATINGS")){
                    /*The action is rating*/
                    //processRating(jsonObject);
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.parse.favourama.HANDLE_FAVOURAMA_REQUESTS");
        filter.addAction("com.parse.favourama.HANDLE_FAVOURAMA_MESSAGES");
        filter.addAction("com.parse.favourama.HANDLE_FAVOURAMA_RATINGS");
        registerReceiver(msgReceiver, filter);


        //init
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        adapter = new ChatAdapter(ACTMsg.this, new ArrayList<ChatMessage>());
        messagesContainer.setAdapter(adapter);

        //reconstruct old messages, checking to prevent filenotfound error
        if(msg_file.length()!=0){
            msg_reconstruct();
        }

        matrix = new Matrix();
        savedMatrix = new Matrix();
        start = new PointF();
        mid = new PointF();
        msgId = new HashSet<>();
    }

    @Override
    public void onPause() {
        StarterApplication.activityPaused();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        StarterApplication.activityResumed();
        StarterApplication.setInMessage();
        StarterApplication.setToWhom(header[0]);
    }

    @Override
    public void onDestroy(){

        Log.d("onDestroy msg", " " + msgReceiver.getResultData());

        unregisterReceiver(msgReceiver);
        super.onDestroy();
    }

    public void onClickSend(View view) {
        final EditText editText = (EditText) findViewById(R.id.typing_box);

        final String content = editText.getText().toString();
        
        if( content == null || content.isEmpty() ){
            return;
        }

        final JSONObject msg = new JSONObject();

        try {
            msg.put("TYPE", "MESSAGE");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            msg.put("ctype", ChatMessage.TEXT_TYPE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            msg.put("content", content);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            msg.put("time", DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.CANADA).format(new Date()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            msg.put("username", me.getUsername());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            msg.put("destination", header[0]);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        HashMap<String, Object> params = new HashMap<String, Object>();

        params.put("TYPE", "MESSAGE");
        params.put("ctype", ChatMessage.TEXT_TYPE);
        params.put("content", content);
        params.put("time", DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.CANADA).format(new Date()));
        params.put("destination", header[0]);
        params.put("username", me.getUsername());
        params.put("rating", me.get("Rating"));

        final long mId = System.currentTimeMillis();
        msgId.add(mId);
        /*Hao: 2 seconds delay before showing progress bar*/
        findViewById(R.id.send_msg_pbar).postDelayed(new Runnable() {
            @Override
            public void run() {
                if(msgId.contains(mId)) {
                    findViewById(R.id.send_msg_pbar).setVisibility(View.VISIBLE);
                }
            }
        }, 2000);
        ParseCloud.callFunctionInBackground("sendMessageToUser", params, new FunctionCallback<String>() {
            public void done(String success, ParseException e) {
                msgId.remove(mId);
                if (e == null) {
                    //Log.d("push", "Message Sent!");
                    editText.getText().clear();
                    //update screen
                    ChatMessage chatmsg = new ChatMessage();
                    chatmsg.setId(0);//todo do I need an id?
                    chatmsg.setMe(true);
                    chatmsg.setMessage(content);
                    chatmsg.setMessageType(ChatMessage.TEXT_TYPE);
                    chatmsg.setDate(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.CANADA).format(new Date()));
                    displayMessage(chatmsg);
            
                    updateDisplay(msg);
                } else {
                    /*Log.d("push", "Message failure >_< \n" + "Plese check your internet connection!\n"
                            + e.getMessage() + " <><><><><><> Code: " + e.getCode());*/
                    String msg = "Could not send: ";
                    if(e.getCode() == 100){
                        /*Poor internet connection*/
                        msg += "please check you internet connection.";
                    }else{
                        msg += e.getMessage();
                    }
                    showErrorDialog(null, msg);
                }
                findViewById(R.id.send_msg_pbar).setVisibility(View.GONE);
            }
        });

        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public void backToMain(View view) {

        //unregisterReceiver(msgReceiver);

        Intent result = new Intent();
        
        setResult(Activity.RESULT_OK, result);
        finish();
    }

//    public String formatHeader(){
//        String res = "In Regards to:\n" + header[0] + ": " + header[2];
//        return res;
//    }

    //JM
    public String formatHeader(){
        String res = header[0];
        return res;
    }



    public void updateDisplay(JSONObject jsonObject){
        /*HAO to Jeremy:
        * This function is where you implement updating the display and/or
        * writing to files which store conversation threads,
        * Check if the incoming message is for the current chat thread or other threads and act differently*/

        //make sure not to change the original value
        String[] namesToWrite = {"TYPE","content","ctype","time","username"};
        JSONObject jsonObjectToWrite = null;
        try {
            jsonObjectToWrite = new JSONObject(jsonObject, namesToWrite);
        }catch(JSONException e){
            e.printStackTrace();
        }

        //write to file
        MyThreads.fileWrite(jsonObjectToWrite, msg_filename);

        //read back for testing
        //fileRead(msg_file);

    }

    private void msg_reconstruct(){
        /*Hao: use linkedlist for better performance*/
        LinkedList<JSONObject> jsonObjectArrayList = new LinkedList<>();
        LinkedList<ChatMessage> chatMessageArrayList = new LinkedList<>();

        /*Hao: Code has been refactored to be more extensive, and has better structure*/
        MyThreads.readLine(msg_file, jsonObjectArrayList, this);

        for(int i=0; i<jsonObjectArrayList.size(); i++){
            JSONObject jObject = jsonObjectArrayList.get(i);
            ChatMessage msg = new ChatMessage();
            msg.setId(i);
            String theOtherPersonUsername = header[0];
            String username_read = null;
            try {
                username_read = jObject.get("username").toString();
            }catch(JSONException e){
                e.printStackTrace();
            }
            if( username_read.equals(theOtherPersonUsername) ){
                msg.setMe(false);
            }else{
                msg.setMe(true);
            }
            try {
                msg.setMessage(jObject.get("content").toString());
            }catch(JSONException e){
                e.printStackTrace();
            }
            try {
                msg.setMessageType(jObject.get("ctype").toString());
            }catch(JSONException e){
                e.printStackTrace();
            }
            try {
                msg.setDate(jObject.get("time").toString());
            }catch(JSONException e){
                e.printStackTrace();
            }
            chatMessageArrayList.add(msg);
        }

        for (ChatMessage msgToDisplay:chatMessageArrayList){
            displayMessage(msgToDisplay);
        }
    }

    private void displayMessage(ChatMessage message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        /*scroll();*/
    }

    public void showErrorDialog(String nag, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        if (nag != null) {
            builder.setNegativeButton(nag, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /*Do nothing*/
            }
            });
        }
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /*Do nothing*/
            }
        });
        builder.create().show();
    }

    public void rateFavour(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if( !MyThreads.isRatable(common_dir, header[0]) ){
            builder.setMessage("Sorry, you have already rated this user for this request, you cannot rate again.");
            builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    /*Do nothing really*/
                }
            });
            AlertDialog error = builder.create();
            error.show();
            return;
        }

        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.rating_dialog, null))
                // Add action buttons
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Remove ratable indicator, cannot rate again
                        MyThreads.removeRatable(common_dir, header[0]);

                        RatingBar ratingBar = (RatingBar) ((AlertDialog)dialog).findViewById(R.id.rate_user);
                        float rating = ratingBar.getRating();

                        HashMap<String, Object> params = new HashMap<String, Object>();

                        params.put("TYPE", "RATING");
                        params.put("Rating", rating);
                        params.put("username", header[0]);

                        ParseCloud.callFunctionInBackground("RateUser", params, new FunctionCallback<String>() {
                            public void done(String success, ParseException e) {
                                if (e == null) {
                                    //Log.d("push", "Rating Sent!");
                                } else {
                                    /*Log.d("push", "Rating failure >_< \n" + "Plese check your internet connection!\n"
                                            + e.getMessage() + " <><><><><><> Code: " + e.getCode());*/
                                    String msg = "Action failed: ";
                                    if(e.getCode() == 100){
                                        /*Poor internet connection*/
                                        msg += "please check you internet connection.";
                                    }else{
                                        msg += e.getMessage();
                                    }
                                    showErrorDialog(null, msg);
                                }
                            }
                        });

                        /*dialog.dismiss();*/
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        /*dialog.dismiss();*/
                    }
                });

        final AlertDialog rdialog = builder.create();

        rdialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                Button nb = rdialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                nb.setTextColor(0xff909090);
            }
        });

        rdialog.show();
    }

    public void send_picture(View view) {
        findViewById(R.id.send_options).setVisibility(View.GONE);
        CropImage.startPickImageActivity(this);
    }



    public void send_location(View view){
        findViewById(R.id.send_options).setVisibility(View.GONE);
        EditText editText = (EditText) findViewById(R.id.typing_box);
        ParseGeoPoint location = ParseInstallation.getCurrentInstallation().getParseGeoPoint("location");
        String locStr = "http://maps.google.com/maps?q=" + location.getLatitude()
                + "," + location.getLongitude();
        editText.setText(locStr);
        onClickSend(null);
    }

    public void confirmSendPic(final Bitmap bitmap){
        LayoutInflater inflater = this.getLayoutInflater();
        View fullView = inflater.inflate(R.layout.image_popup, null, false);
        ImageView dView = ((ImageView) fullView.findViewById(R.id.image_full_screen));

        dView.setImageBitmap(bitmap);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        builder.setView(fullView)
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ImageChannel.makeImageBox(bitmap, activity);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*Do nothing*/
                    }
                });

        AlertDialog fullImage = builder.create();
        fullImage.show();
    }
    
    public void setPostDelayed(final long mId){
        msgId.add(mId);
        findViewById(R.id.send_msg_pbar).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(msgId.contains(mId)) {
                                    findViewById(R.id.send_msg_pbar).setVisibility(View.VISIBLE);
                                }
                            }
                        }, 2000);
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            if (cursor == null) { // Source is Dropbox or other similar local file path
                return contentUri.getPath();
            }
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            String imgPath = getRealPathFromURI(this, imageUri);

            Log.d("RPATH", imgPath);

            Bitmap bitmap = ImageChannel.decodeScaledDownBitmap(imgPath);

            confirmSendPic(bitmap);
        }
    }

    public void SendPictureHelper(String imageID) {

        Log.d("JM","function called");


        /*String url was previous paramter*/
        JSONObject msg = new JSONObject();

        try {
            msg.put("TYPE", "MESSAGE");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            msg.put("ctype", ChatMessage.PICTURE_TYPE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            msg.put("content", imageID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            msg.put("time", DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.CANADA).format(new Date()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            msg.put("username", me.getUsername());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            msg.put("destination", header[0]);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HashMap<String, Object> params = new HashMap<String, Object>();

        params.put("TYPE", "MESSAGE");
        params.put("ctype", ChatMessage.PICTURE_TYPE);
        params.put("content", imageID);
        params.put("time", DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.CANADA).format(new Date()));
        params.put("username", me.getUsername());
        params.put("destination", header[0]);
        params.put("rating", me.get("Rating"));

        final long mId = System.currentTimeMillis();
        msgId.add(mId);
        /*Hao: 2 seconds delay before showing progress bar*/
        findViewById(R.id.send_msg_pbar).postDelayed(new Runnable() {
            @Override
            public void run() {
                if(msgId.contains(mId)) {
                    findViewById(R.id.send_msg_pbar).setVisibility(View.VISIBLE);
                }
            }
        }, 2000);
        ParseCloud.callFunctionInBackground("sendMessageToUser", params, new FunctionCallback<String>() {
            public void done(String success, ParseException e) {
                msgId.remove(mId);
                if (e == null) {
                    //Log.d("push", "Message Sent!");
                } else {
                    /*Log.d("push", "Message failure >_< \n" + "Plese check your internet connection!\n"
                            + e.getMessage() + " <><><><><><> Code: " + e.getCode());*/
                    String msg = "Could not send: ";
                    if(e.getCode() == 100){
                        /*Poor internet connection*/
                        msg += "please check you internet connection.";
                    }else{
                        msg += e.getMessage();
                    }
                    showErrorDialog(null, msg);
                }
                findViewById(R.id.send_msg_pbar).setVisibility(View.GONE);
            }
        });


        //For testing purposes currently
        ChatMessage chatmsg = new ChatMessage();
        chatmsg.setId(0);//todo do I need an id?
        chatmsg.setMe(true);
        chatmsg.setMessage(imageID);
        chatmsg.setMessageType(ChatMessage.PICTURE_TYPE);
        chatmsg.setDate(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.CANADA).format(new Date()));
        displayMessage(chatmsg);

        updateDisplay(msg);
    }

    public void hideSendPBar(long mId){

        msgId.remove(mId);
        findViewById(R.id.send_msg_pbar).setVisibility(View.GONE);
    }

    public void chat_show_image(String chat_content, String txt_type){
        ChatMessage chatmsg = new ChatMessage();
        chatmsg.setId(0);//todo do I need an id?
        chatmsg.setMe(false);
        chatmsg.setMessage(chat_content);
        chatmsg.setMessageType(txt_type);
        chatmsg.setDate(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.CANADA).format(new Date()));
        displayMessage(chatmsg);
    }

    public void fullScreenDisplay(View view) {
        //Get screen size
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;

        Drawable image = ((ImageView) view).getDrawable();
        double sw = (double) view.getWidth();
        double sh = (double) view.getHeight();

        double scale = (width/sw > height/sh)? height/sh : width/sw;

        Log.d("IMGSize", "Ori: " + sw + " " + sh + ", " + "Scale: " + scale);

        double fw = sw*scale;
        double fh = sh*scale;

        LayoutInflater inflater = this.getLayoutInflater();
        View fullView = inflater.inflate(R.layout.image_popup, null, false);
        ImageView dView = ((ImageView) fullView.findViewById(R.id.image_full_screen));

        dView.setImageDrawable(image);
        dView.getLayoutParams().width = (int) fw;
        dView.getLayoutParams().height = (int) fh;

        /*Initial scaling*/
        matrix.reset();
        matrix.postScale((float) scale, (float) scale);
        dView.setImageMatrix(matrix);

        dView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() != R.id.image_full_screen) {
                    return false;
                }

                ImageView view = (ImageView) v;
                view.setScaleType(ImageView.ScaleType.MATRIX);

                Log.d("ONTOUCH", "Entering onTouch...");

                // Handle touch events here...
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        savedMatrix.set(matrix);
                        start.set(event.getX(), event.getY());
                        mode = DRAG;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        oldDist = spacing(event);
                        if (oldDist > 10f) {
                            savedMatrix.set(matrix);
                            midPoint(mid, event);
                            mode = ZOOM;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        mode = NONE;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mode == DRAG) {
                            // Leaves a red canvas behind, need to fix this
                            matrix.set(savedMatrix);
                            matrix.postTranslate(event.getX() - start.x, event.getY()
                                    - start.y);
                        } else if (mode == ZOOM) {
                            float newDist = spacing(event);
                            if (newDist > 10f) {
                                matrix.set(savedMatrix);
                                float scale = newDist / oldDist;
                                matrix.postScale(scale, scale, mid.x, mid.y);
                            }
                        }
                        break;
                }

                view.setImageMatrix(matrix);
                return true;
            }
        });

        //Log.d("IMGSize", "Final: " + fw + " " + fh + ", " + "Window width: " + width);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(fullView);
        AlertDialog fullImage = builder.create();
        fullImage.show();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = fullImage.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        lp.copyFrom(window.getAttributes());
        lp.width = (int) fw;
        lp.height = (int) fh;
        window.setAttributes(lp);
    }

    /** Determine the space between the first two fingers */
    private float spacing(MotionEvent event) {
        double x = event.getX(0) - event.getX(1);
        double y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /** Calculate the mid point of the first two fingers */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    public void showSendOptions(View view) {
        View v = findViewById(R.id.send_options);
        if(v.getVisibility() == View.VISIBLE){
            v.setVisibility(View.GONE);
        }else {
            v.setVisibility(View.VISIBLE);
        }
    }

    public void send_card(View view) {
        findViewById(R.id.send_options).setVisibility(View.GONE);
        EditText editText = (EditText) findViewById(R.id.typing_box);
        String email = me.getEmail();
        String phone = me.getString("phoneNumber");
        String msg = me.getUsername() + ", Email: " + email;

        if(!ACTRequest.missInfo(phone)){
            msg += ", Phone: ";
            msg += phone;
        }

        editText.setText(msg);
    }
}
