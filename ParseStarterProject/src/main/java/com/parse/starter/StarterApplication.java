/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;
import com.parse.ParseInstallation;

import java.io.File;


public class StarterApplication extends Application {

    private static boolean activityVisible;
    private static boolean inMessage;
    private static String toWhom;
    private static File user_files_dir;

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        // Initialize Parse
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("favouramahj")
                .clientKey(null)
                .server("http://favourama.herokuapp.com/parse/")
                .build()
        );
        ParseInstallation.getCurrentInstallation().saveInBackground();
        ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();
        // Optionally enable public read access.
        defaultACL.setPublicReadAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    public static void setInMessage(){
        inMessage = true;
    }

    public static void setNotInMessage(){
        inMessage = false;
    }

    public static boolean isInMessage(){
        return inMessage;
    }

    public static void setToWhom(String name){
        toWhom = name;
    }

    public static String getToWhom(){
        return toWhom;
    }

    public static void setUserFilesDir(File dir){
        user_files_dir = dir;
    }

    public static File getUserFilesDir(){
        return user_files_dir;
    }
}
