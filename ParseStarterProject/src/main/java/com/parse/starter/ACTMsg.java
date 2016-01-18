package com.parse.starter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SendCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ACTMsg extends AppCompatActivity {
    private String[] header;
    ParseQuery<ParseInstallation> chatQuery;
    private BroadcastReceiver msgReceiver;
    private ArrayList<String> requestCollector;
    private int msgCounter;

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
                    requestCollector.add(jsonObject.toString());
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
                    updateDisplay(jsonObject);
                }

            }
        };
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
            msg.put("time", System.currentTimeMillis());
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
    }

    public void backToMain(View view) {
        Intent result = new Intent();
        result.putStringArrayListExtra("RequestCollection", requestCollector);
        result.putExtra("CounterValue", msgCounter);
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    public String formatHeader(){
        String res = "In Regards to:\n" + header[0] + ": " + header[2];
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
    }
}
