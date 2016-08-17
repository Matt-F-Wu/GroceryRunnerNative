package com.parse.favourama;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by HaoWu on 5/25/2016.
 */
/*A file containing global flags and variables to assist testing*/
public class beta_test {
    public static boolean debug = true;
    public static boolean debug_notification = false;

    public static void logStringArray(String[] arr, String tag){
        String res = "";
        for(String s : arr){
            res += (s + "  ");
        }
        Log.d(tag, res);
    }


    public static String fileRead(File file_read){
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

}
