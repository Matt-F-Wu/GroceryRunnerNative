package com.parse.starter;

/**
 * Created by HaoWu on 2/10/2016.
 */
import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class MyThreads {
    List<MsgThread> converThreads;
    List<String[]> headers;
    int numFile;
    HashSet<String> numChange;
    File dir;
    Context context;

    public MyThreads (Context context) {
        numFile = 0;
        dir = context.getFilesDir();
        this.context = context;
        converThreads = new LinkedList<>();
        headers = new ArrayList<>();
        numChange = new HashSet<>();
        String[] list = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches("MSG_.*");
            }
        });
        for(String fname : list){
            File file = new File(dir, fname);
            MsgThread th = new MsgThread(fname, file);
            converThreads.add(th);
            numFile++;
        }

        makeHeaderArray();
    }

    public boolean newConversation(String[] header){
        String uname = header[0];
        String fname = "MSG_" + uname + ".json";

        int index = 0;

        for (MsgThread mt : converThreads){
            if (mt.filename.equals(fname)){
                if(headers.get(index)[2].equals(header[2])){
					/*If the user is trying to respond to a request they already responded to,
					we don't create new file*/
                    return false;
                }
            }
            index++;
        }

        File file = new File(dir, fname);
        writeHeader(file, header);
        MsgThread mt = new MsgThread(fname, file);
        mt.setHeader(header); //this call would not be needed once write to file is integrated
        numFile++;
        converThreads.add(0, mt);

        headers.add(0, mt.spitHeader());
        return true;
    }

    private void writeHeader(File file, String[] header){
        //TBD: write header to file

    }


    public void fileChange(String fname, JSONObject data){
        for (MsgThread mt : converThreads){
            if (mt.filename.equals(fname)){
                mt.writeBody(data);
                mt.setTime();
            }
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
        headers.clear();


        for(MsgThread mt : converThreads){
            headers.add(mt.spitHeader());
        }
    }

    public List<String[]> getHeader(){
        return headers;
    }

    public static String toFile(String name){
        return "MSG_" + name + ".json";
    }

    public class MsgThread implements Comparable<MsgThread>{
        private String filename;
        private File file;
        private long time;
        /*Hao to Jeremy: This array member shouldn't exist after your implementation*/
        private String[] header;

        public MsgThread(String fname, Context context){
            this.filename = fname;
            this.file = new File(context.getFilesDir(), fname);
            this.time = file.lastModified();
        }

        public MsgThread(String fname, File file){
            this.filename = fname;
            this.file = file;
            this.time = file.lastModified();
        }

        public void setTime(){
            this.time = file.lastModified();
        }

        public void writeBody(JSONObject data){
            //TBD: write body, aka dialog,  to file
        }

        public String[] spitHeader(){
            /*Hao to Jeremy: should be like this:
            return parseJSON(filename, "HEADER");*/
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
        /*Hao to Jeremy: This methos sohuldn't exist after you implement the write to file*/
        public void setHeader(String[] h){
            this.header = h;
        }
    }

    //TBD: implement JSON parser, or use libraries like GSON, type can be either HEADER or BODY
    public String[] parseJSON(String fname, String type){
        return null;
    }

}

