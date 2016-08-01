package com.parse.starter;

/**
 * Created by HaoWu on 2/10/2016.
 */
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class MyThreads {
    List<MsgThread> converThreads;
    int numFile;
    HashSet<String> numChange;
    File dir;
    File conv_cata_file;
    Context context;
    static private String cList = "conversation_list.json";

    public MyThreads (Context context) {
        numFile = 0;
        dir = context.getFilesDir();
        this.context = context;
        converThreads = new LinkedList<>();
        numChange = new HashSet<>();
        
        LinkedList<JSONObject> allConvs = new LinkedList<>();
        conv_cata_file = new File(context.getFilesDir(), cList);
        if(!conv_cata_file.exists()) {
            try {
                conv_cata_file.createNewFile();
            } catch (IOException e) {
                /*If the conv cata file cannot be created*/
                return;
            }
        }
        readLine(conv_cata_file, allConvs, context);

        for (JSONObject convHeader : allConvs){
            String fname = toFile(convHeader.optString("username"));
            File file = new File(dir, fname);
            MsgThread th = new MsgThread(convHeader, file);
            converThreads.add(th);
            numFile++;
        }

        makeHeaderArray();
    }

    public boolean newConversation(String[] header){
        beta_test.logStringArray(header, "header");
        String uname = header[0];
        String fname = "MSG_" + uname + ".json";

        int index = 0;

        for (MsgThread mt : converThreads){
            if (mt.filename.equals(fname)){
                if(mt.header[2].equals(header[2])){
					/*If the user is trying to respond to a request they already responded to,
					we don't create new file*/
                    return false;
                }else{
                    /*Hao: now the user is responding to the same person under a different topic
                    * update conversation_list.json
                    * update convThreads*/
                    mt.setHeader(header);

                    try {
                        mt.jsonHeader.put("rating", header[1]);
                        mt.jsonHeader.put("note", header[2]);
                    }catch (JSONException e){
                        e.printStackTrace();
                        Log.d("Update Topic", "FAILED TO UPDATE");
                    }

                    Log.d("Intense file write", "Rewriting conversation_list.json");
                    
                    rewriteCList();

                    /*Rewrite the content of the conversation list*/
                    return true;
                }
            }
            index++;
        }

        File file = new File(dir, fname);
        JSONObject jsonHeader = writeConvList(header);
        if(jsonHeader == null) return false;

        MsgThread mt = new MsgThread(jsonHeader, file);
        numFile++;
        converThreads.add(0, mt);

        return true;
    }

    private JSONObject writeConvList(String[] header){
        //TBD: write username, rating, header to file
        JSONObject data = new JSONObject();

        try{
            data.put("username", header[0]);
            data.put("rating", header[1]);
            data.put("note", header[2]);
        }catch (JSONException e){
            e.printStackTrace();
            return null;
        }
        fileWrite(data, cList, context);
        return data;
    }


    public void fileChange(String fname, JSONObject data){
        boolean foundFile = false;
        for (MsgThread mt : converThreads){
            if (mt.filename.equals(fname)){
                mt.writeBody(data);
                mt.setTime();
                foundFile = true;
            }
        }

        /*If no previous conversation exist from the sender, we make a new conversation*/
        if(!foundFile){
            String[] header = {data.optString("username"), data.optString("rating"), "Your request"};
            newConversation(header);

            File file = new File(dir, fname);

            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            String[] namesToWrite = {"TYPE","content","ctype","time","username"};
            JSONObject jsonObjectToWrite = null;
            try {
                jsonObjectToWrite = new JSONObject(data, namesToWrite);
            }catch(JSONException e){
                e.printStackTrace();
            }

            //write to file
            MyThreads.fileWrite(jsonObjectToWrite, fname, context);
        }

        makeHeaderArray();
    }

    public void refresh(){
        for (MsgThread mt : converThreads){
            mt.setTime();
        }

        makeHeaderArray();
    }


    private void makeHeaderArray(){
        Collections.sort(converThreads);
    }

    public List<String[]> getHeader(){
        List<String[] > res = new LinkedList<>();

        for (MsgThread mt : converThreads){
            res.add(mt.spitHeader());
        }

        return res;
    }

    public static String toFile(String name){
        return "MSG_" + name + ".json";
    }

    public class MsgThread implements Comparable<MsgThread>{
        private String filename;
        private File file;
        private long time;
        private JSONObject jsonHeader;
        /*Hao to Jeremy: This array member shouldn't exist after your implementation*/
        private String[] header;

        public MsgThread(JSONObject convHeader, File file){
            this.jsonHeader = convHeader;
            this.file = file;
            this.time = file.lastModified();
            String name = convHeader.optString("username");
            this.filename = toFile(name);
            String rate = convHeader.optString("rating");
            String note = convHeader.optString("note");
            header = new String[]{name, rate, note};
            beta_test.logStringArray(header, "cheader");
        }

        public void setTime(){
            this.time = file.lastModified();
        }
        
        public String getFilename(){
            return this.filename;
        }

        public void writeBody(JSONObject data){
            //TBD: write body, aka dialog,  to file
            fileWrite(data, filename, context);
        }

        public String[] spitHeader(){
            return header;
        }

        @Override
        public int compareTo(MsgThread o) {
            if(this.time < o.time){
                return 1;
            }else if (this.time == o.time){
                return 0;
            }else{
                return -1;
            }
        }
        /*Hao: used to mutate header*/
        public void setHeader(String[] h){
            this.header = h;
        }
    }


    static void fileWrite(JSONObject jsonObject, String filename, Context context){
        try{
            FileOutputStream fos = context.openFileOutput(filename, context.MODE_APPEND);
            fos.write(jsonObject.toString().getBytes());
            fos.write('\n');
            fos.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    static void readLine(File fileName, List<JSONObject> jsonObjectArrayList, Context context){
        /*Hao: refactored to support more extensively*/
        InputStream in = null;
        BufferedReader reader;
        try {
            in = new BufferedInputStream(new FileInputStream(fileName));
            reader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            while((line=reader.readLine()) != null){
                try{
                    JSONObject jsonObject = new JSONObject(line);
                    jsonObjectArrayList.add(jsonObject);
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context, "Sorry >_< Favourama cannot find this conversation, please don't delete app files externally", Toast.LENGTH_LONG);
        } catch(IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Sorry >_< Favourama runinto a glitch, please try deleting this conversation thread", Toast.LENGTH_LONG);
        }finally{
            if (in != null) {
                try {
                    in.close();
                }catch (IOException ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    public boolean deleteFile(String fname){
        File clfile = new File(dir, fname);
        boolean res1 = false;


        for (int i = 0; i < numFile; i++){
            String ffname = converThreads.get(i).filename;
            if (ffname.equals(fname)) {
                MsgThread msgThread = converThreads.remove(i);
                if(msgThread.filename.equals(fname)) res1 = true;
                break;
            }
        }

        if (res1){
            rewriteCList();
            numFile--;
        }

        boolean res2 = clfile.delete();

        return res1 && res2;
    }

    private void rewriteCList(){
    	File clfile = new File(dir, cList);
        Log.d("conv_list", beta_test.fileRead(clfile));
        boolean deleted = clfile.delete();

	int i = 1; //already tried to delete once
        while (!deleted && i < 5){
            deleted = clfile.delete();
            i++;
        }

    	
        String dummy = new String();
        for (MsgThread mt : converThreads){
            if( !mt.jsonHeader.optString("username").equals(dummy) ) fileWrite(mt.jsonHeader, cList, context);
            dummy = mt.jsonHeader.optString("username");
        }
    }

    public String showFileAttribute(String fname){
        for (MsgThread msgThread : converThreads){
            if (msgThread.filename.equals(fname)) {
                Date date = new Date(msgThread.time);
                String res = "Conversation Last Modified at: " + date.toString() + "\n";
                res = res + "File size is: " + msgThread.file.length()/1024 + " KB\n\n"
                        + "Favourama \u00AE Native Messaging System\n";
                return res;
            }
        }
        return "Sorry, Cannot find any information. \n \n" +
                " Favourama ® Native Messaging System\n";
    }

}

