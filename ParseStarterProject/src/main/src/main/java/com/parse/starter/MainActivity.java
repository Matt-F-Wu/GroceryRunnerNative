/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.maps.SupportMapFragment;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseInstallation;
import com.parse.ParseObject;


public class MainActivity extends AppCompatActivity {
  protected GoogleSignInAccount acct;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ParseAnalytics.trackAppOpenedInBackground(getIntent());
      //Let the app show some advertisement for a few seconds, need to implement later
      /*try {
          Thread.sleep(3000);
      } catch (InterruptedException e) {
          e.printStackTrace();
      }*/
        //Using google login
      /*Intent i = new Intent(this, ActLogin.class);
      startActivityForResult(i, 1);*/

      //Try our own login
      Intent i = new Intent(this, ACTLoginSelf.class);
      startActivity(i);


  }
  
}
