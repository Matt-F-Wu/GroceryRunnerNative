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
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;


public class MainActivity extends ActionBarActivity {
  protected GoogleSignInAccount acct;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ParseAnalytics.trackAppOpenedInBackground(getIntent());
      ParseObject testObject = new ParseObject("TestObject");
      testObject.put("foo", "bar");
      testObject.saveInBackground();

      //Let the app show some advertisement for a few seconds, need to implement later
      /*try {
          Thread.sleep(3000);
      } catch (InterruptedException e) {
          e.printStackTrace();
      }*/

      Intent i = new Intent(this, ActLogin.class);
      startActivityForResult(i, 1);

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == 1){
      if(resultCode == Activity.RESULT_OK){
         acct = data.getParcelableExtra("result");
          Intent i = new Intent(this, ActUserPage.class);
          Bundle b = new Bundle();
          b.putParcelable("userGoogleInfo", acct);
          i.putExtras(b);
          startActivity(i);
      } else if (resultCode == Activity.RESULT_CANCELED){
        //This shuold never happen
      }
    }
  }
}
