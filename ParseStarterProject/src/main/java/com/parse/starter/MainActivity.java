/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ViewFlipper;

import com.parse.ParseAnalytics;
import java.io.File;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
      File file = new File(getFilesDir(), "FavouramaLicense");

      if(file.exists()){
          /*User already accepted license, move on*/
          ParseAnalytics.trackAppOpenedInBackground(getIntent());

          Intent i = new Intent(this, ACTLoginSelf.class);
          startActivity(i);
      }else {
          setContentView(R.layout.activity_main);
      }
  }

    public void acceptAndProceed(View view) {
        File file = new File(getFilesDir(), "FavouramaLicense");
        try {
            file.createNewFile();
            ParseAnalytics.trackAppOpenedInBackground(getIntent());

            Intent i = new Intent(this, ACTLoginSelf.class);
            startActivity(i);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showAgreement(View view) {
        ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.license_flipper);
        viewFlipper.setDisplayedChild(1);
    }
}
