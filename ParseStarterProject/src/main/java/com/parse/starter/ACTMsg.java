package com.parse.starter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SendCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ACTMsg extends AppCompatActivity {
    private String[] header;
    ParseQuery<ParseInstallation> chatQuery;
    private BroadcastReceiver msgReceiver;
    private ArrayList<String> requestCollector;
    private int msgCounter;

    //message file global
    private File common_dir;

    //display global
    private ChatAdapter adapter;
    private ListView messagesContainer;
    private String msg_filename;
    private File msg_file;
    static private String conversation_list_filename = "conversation_list.json";
    static private File conversation_file;

/*  example Json Format in conversation files
    {
        "TYPE": "HEADER",
        "content": "{\n  \"uname\": \"junwei\",\n  \"topic\": \"hi\"\n}",
        "time": 1456634111506,
        "username": "junwei"
    }
    {
        "TYPE": "MESSAGE",
        "content": "sup",
        "time": 1456634115145,
        "username": "junwei"
    }
    {
        "TYPE": "MESSAGE",
        "content": "sup",
        "time": 1456634115145,
        "username": "junwei"
    }
*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actmsg);

        Bundle b = this.getIntent().getExtras();
        if (b != null)
            header = b.getStringArray("ThreadHeader");

        TextView headerView = (TextView) findViewById(R.id.msg_thread_header);
        headerView.setText( formatHeader() );

        requestCollector = new ArrayList<String>();

        chatQuery = ParseInstallation.getQuery();
        chatQuery.whereEqualTo("username", header[0]);

        //manage files
        common_dir = getApplicationContext().getFilesDir();
        msg_filename = "MSG_"+header[0]+".json";//todo change this later
        msg_file = new File(common_dir, msg_filename);
        conversation_file = new File(common_dir, conversation_list_filename);

        //read back for testing
        fileRead(conversation_file);


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
                    //Hao to Self: Suspicion is that we don't need to update request list here
                    // requestCollector.add(jsonObject.toString());
                    //Record all the requests coming during the period where the user is on this activity
                    /*Return all the requests received to ACTRequest to be handled when this activity finishes*/
                }
                else if (action.equals("com.parse.favourama.HANDLE_FAVOURAMA_MESSAGES")){
                    /* HAO to JEREMY
                    In updateCounter(), I check if the message incoming is with in the current thread. If not, I update
                    counter number to be returned to the ACTRequest Activity, otherwise, I do nothing
                    * */
                    updateCounter(jsonObject);

                    /*HAO to JEREMY:
                    * You need to implement this, I declared an empty function for you*/
                    //update screen
                    String chat_content = new String();
                    try{
                        chat_content = jsonObject.getString("content");
                    }catch(org.json.JSONException e){
                        e.printStackTrace();
                    }
                    ChatMessage chatmsg = new ChatMessage();
                    chatmsg.setId(0);//todo do I need an id?
                    chatmsg.setMe(false);
                    chatmsg.setMessage(chat_content);
                    chatmsg.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                    displayMessage(chatmsg);

                    //updateDisplay(jsonObject);
                }
                else if(action.equals("com.parse.favourama.HANDLE_FAVOURAMA_RATINGS")){
                    /*The action is rating*/
                    processRating(jsonObject);
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
    }

    public void onClickSend(View view) {
        EditText editText = (EditText) findViewById(R.id.typing_box);

        String content = editText.getText().toString();

        JSONObject msg = new JSONObject();

        try {
            msg.put("TYPE", "MESSAGE");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            msg.put("content", content);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            msg.put("time", DateFormat.getDateTimeInstance().format(new Date()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            msg.put("username", header[0]);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ParsePush.sendDataInBackground(msg, chatQuery, new SendCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("push", "Message Sent!");
                } else {
                    Log.d("push", "Message failure >_< \n" + "Plese check your internet connection!\n"
                            + e.getMessage() + " <><><><><><> Code: " + e.getCode());
                }
            }
        });

        editText.getText().clear();

        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);

        //update screen
        ChatMessage chatmsg = new ChatMessage();
        chatmsg.setId(0);//todo do I need an id?
        chatmsg.setMe(true);
        chatmsg.setMessage(content);
        chatmsg.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        displayMessage(chatmsg);

        updateDisplay(msg);
    }

    public void backToMain(View view) {

        unregisterReceiver(msgReceiver);

        Intent result = new Intent();
        result.putStringArrayListExtra("RequestCollection", requestCollector);
        result.putExtra("CounterValue", msgCounter);
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


    public void updateCounter(JSONObject jsonObject){
        String nameOfThread;
        try {
            nameOfThread = jsonObject.getString("username");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        if ( !nameOfThread.equals(header[0]) ){
            msgCounter ++;
        }

    }

    public void updateDisplay(JSONObject jsonObject){
        /*HAO to Jeremy:
        * This function is where you implement updating the display and/or
        * writing to files which store conversation threads,
        * Check if the incoming message is for the current chat thread or other threads and act differently*/

        //make sure not to change the original value
        String[] namesToWrite = {"TYPE","content","time","username"};
        JSONObject jsonObjectToWrite = null;
        try {
            jsonObjectToWrite = new JSONObject(jsonObject, namesToWrite);
        }catch(JSONException e){
            e.printStackTrace();
        }

        //write to file
        MyThreads.fileWrite(jsonObjectToWrite, msg_filename, this);

        //read back for testing
        fileRead(msg_file);

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
            if(username_read == theOtherPersonUsername){
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

    /*
    There is no need to do this programmatically, just set stack from bottom in layout.
    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }*/

    //TO HAO: My read function for reading entire file, testing purposes
    private String fileRead(File file_read){
        InputStream in = null;
        String msg_read = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file_read));
            try {
                int i = in.available();
                Log.d("ACTmsg file", "input stream bytes available: " + i);
                in.mark(i);
                byte[] buffer = new byte[i];
                in.read(buffer,0,i);
                msg_read = new String(buffer,"UTF-8");
                Log.d("ACTmsg file", "the msg read is " + msg_read);
                in.reset();
            }catch (IOException ex){
                ex.printStackTrace();
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } finally{
            if (in != null) {
                try {
                    in.close();
                }catch (IOException ex){
                    ex.printStackTrace();
                }
            }
        }
        return msg_read;
    }


    public void rateFavour(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.rating_dialog, null))
                // Add action buttons
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...
                        RatingBar ratingBar = (RatingBar) ((AlertDialog)dialog).findViewById(R.id.rate_user);
                        float rating = ratingBar.getRating();

                        JSONObject msg = new JSONObject();

                        try {
                            msg.put("TYPE", "RATING");
                            msg.put("Rating", rating);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        ParsePush.sendDataInBackground(msg, chatQuery, new SendCallback() {
                            public void done(ParseException e) {
                                if (e == null) {
                                    Log.d("Rating", "Message Sent!");
                                } else {
                                    Log.d("Rating", "Message failure >_< \n" + "Plese check your internet connection!\n"
                                            + e.getMessage() + " <><><><><><> Code: " + e.getCode());
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


                Button pb = rdialog.getButton(DialogInterface.BUTTON_POSITIVE);
                pb.setTextColor(0xffcaaaaa);

                Button nb = rdialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                nb.setTextColor(0xff000000);
                nb.setTextColor(0xff000000);

            }
        });

        rdialog.show();
    }

    static void processRating(JSONObject jsonObject){

        double ratingReceived = 2.5;

        try {
            ratingReceived = jsonObject.getDouble("Rating");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ParseUser user = ParseUser.getCurrentUser();
        double ratingCurrent = user.getDouble("Rating");

        int numRatings = user.getInt("NumRating");

        double ratingNew = ((ratingCurrent * numRatings) + ratingReceived)/(numRatings + 1);

        numRatings++;

        user.put("NumRating", numRatings);
        user.put("Rating", ratingNew);

        user.saveInBackground();

        /*TBD:
        * write a flag to the conversations.json file corresponding to this conversation
        *
        * rated = true;
        *
        * So the user cannot rate one person multiple times, implement this later.
        *
        * */
    }
}
