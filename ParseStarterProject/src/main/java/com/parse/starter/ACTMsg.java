package com.parse.starter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.renderscript.ScriptGroup;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SendCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    String msg_filename;
    File msg_file;

/*  example Json Format in conversation files
    {
        "type": HEADER,
        "username": J_Ma,
        "content": {\"uname\":\"wuhao\",\"rating\":\"3.5\",\"topic\":\"Borrow a Iphone Charger\"}"},
        "time": 2016
    },
    {
        "type": MSG,
        "username": J_Ma,
        "content": "Hello! Is the price negotiable?",
        "time": 2016
    },
    {
        "type": MSG,
        "username": H_Wu,
        "content": "Yes.",
        "time": 2016
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
        msg_filename = "testMSG_"+header[0]+".json";//todo change this later
        msg_file = new File(common_dir, msg_filename);
        Log.d("directory: ",getApplicationContext().getFilesDir().toString());//TODO delete after testing


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

                    updateDisplay(jsonObject);
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.parse.favourama.HANDLE_FAVOURAMA_REQUESTS");
        filter.addAction("com.parse.favourama.HANDLE_FAVOURAMA_MESSAGES");
        registerReceiver(msgReceiver, filter);


        //init
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        adapter = new ChatAdapter(ACTMsg.this, new ArrayList<ChatMessage>());
        messagesContainer.setAdapter(adapter);

        //dummy history
        //todo delete after testing
        ChatMessage msg0 = new ChatMessage();
        msg0.setId(1);
        msg0.setMe(false);
        msg0.setMessage("Hi");
        msg0.setDate(DateFormat.getDateTimeInstance().format(new Date()));

        ChatMessage msg1 = new ChatMessage();
        msg1.setId(2);
        msg1.setMe(false);
        msg1.setMessage("How r u doing???");
        msg1.setDate(DateFormat.getDateTimeInstance().format(new Date()));

        displayMessage(msg0);
        displayMessage(msg1);

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

        //get variables
        String username = new String();
        String content = new String();
        Long time = new Long(0);

        try{
            username = jsonObject.getString("username");
        }catch(org.json.JSONException e){
            e.printStackTrace();
        }

        try{
            content = jsonObject.getString("content");
        }catch(org.json.JSONException e){
            e.printStackTrace();
        }

        try{
            time = jsonObject.getLong("time");
        }catch(org.json.JSONException e){
            e.printStackTrace();
        }

        //write to file
        OutputStream out = null;
        try{
            out = new BufferedOutputStream(new FileOutputStream(msg_file));
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }

        try {
            writeJsonStream(out, jsonObject);
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            if(out != null){
                try {
                    out.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }


        //TODO: delete after testing
        String debug_username = new String();
        String debug_content = new String();
        Long debug_time = new Long(0);
        JSONObject message = new JSONObject();

        InputStream in = null;
        try{
            in = new BufferedInputStream(new FileInputStream(msg_file));
            message = readJsonStream(in);
        }catch(FileNotFoundException ex) {
            ex.printStackTrace();
        }catch(IOException ex){
            ex.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        try{
            debug_username = message.getString("username");
        }catch(org.json.JSONException e){
            e.printStackTrace();
        }

        try{
            debug_content = message.getString("content");
        }catch(org.json.JSONException e){
            e.printStackTrace();
        }

        try{
            debug_time = message.getLong("time");
        }catch(org.json.JSONException e) {
            e.printStackTrace();
        }


        Log.d("ACTmsg file", "the msg read is " + debug_username);
        Log.d("ACTmsg file", "the msg read is " + debug_content);
        Log.d("ACTmsg file", "the msg read is " + debug_time);

        //todo end of delete after testing

        //display message on screen


    }


    public void displayMessage(ChatMessage message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scroll();
    }

    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }

    public void writeJsonStream(OutputStream out, JSONObject message) throws IOException {
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
        writer.setIndent("  ");
        writeMessage(writer, message);
        writer.close();

    }


    public void writeMessage(JsonWriter writer, JSONObject message) throws IOException {
        writer.beginObject();

        String username = new String();
        String content = new String();
        Long time = new Long(0);

        try{
            username = message.getString("username");
        }catch(org.json.JSONException e){
            e.printStackTrace();
        }

        try{
            content = message.getString("content");
        }catch(org.json.JSONException e){
            e.printStackTrace();
        }

        try{
            time = message.getLong("time");
        }catch(org.json.JSONException e){
            e.printStackTrace();
        }

        writer.name("username").value(username);
        writer.name("content").value(content);
        writer.name("time").value(time);

        writer.endObject();
    }

    public JSONObject readJsonStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return readMessage(reader);
        }
            finally {
                reader.close();
            }
    }

    public JSONObject readMessage(JsonReader reader) throws IOException {
        String username = new String();
        String content = new String();
        Long time = new Long(0);

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("username")) {
                username = reader.nextString();
            } else if (name.equals("content")) {
                content = reader.nextString();
            } else if (name.equals("time")) {
                time = reader.nextLong();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("username", username);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            jsonObject.put("content", content);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            jsonObject.put("time", time);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
}
