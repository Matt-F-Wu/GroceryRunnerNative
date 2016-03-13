package com.parse.starter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SignUpCallback;

public class ACTLoginSelf extends AppCompatActivity {
    EditText username, password, phoneNum, email, addr1, addr2, addr3;
    TextView alertMsg;
    String uname, pw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_self);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        phoneNum = (EditText) findViewById(R.id.phone_num);
        addr1 = (EditText) findViewById(R.id.addr_one);
        email = (EditText) findViewById(R.id.email);
        alertMsg = (TextView) findViewById(R.id.alert_msg);
        alertMsg.setTextColor(Color.parseColor("#ff0000"));
        addr2 = null;
        addr3 = null;
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null && currentUser.getUsername() != null) {
            // Go to User Main Page directly
            //
            Log.d("Log In Previous User", "SUCESSFUL" + currentUser.getUsername());
            Intent mainP = new Intent(ACTLoginSelf.this, ACTRequest.class);
            startActivity(mainP);
            //!!!
        } else {
            // show the signup or login screen
        }
    }

    private boolean missInfo(String s) {
        if (s == null || s.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public void onClickSignIn(View v) {
        uname = username.getText().toString();
        pw = password.getText().toString();
        Log.d("Username", uname);
        Log.d("Password", pw);
        ParseUser.logInInBackground(uname, pw, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    // Hooray! The user is logged in. Go to the mainpage activity
                    Log.d("Log In", "SUCESSFUL");
                    Intent mainP = new Intent(ACTLoginSelf.this, ACTRequest.class);
                    startActivity(mainP);
                    //!!!!!!!!!!!!!!
                } else {
                    // Signup failed. Look at the ParseException to see what happened. =======TBD=========
                    alertMsg.setText("Sign In Failed, please try again >_< " + e.getMessage());
                }
            }
        });

    }

    public void onClickForgetPassword(View v) {
        String email_s = email.getText().toString();
        if (!missInfo(email_s)) {
            ParseUser.requestPasswordResetInBackground(email_s,
                    new RequestPasswordResetCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                requestedSuccessfully();
                            } else {
                                requestDidNotSucceed();
                            }
                        }
                    });
        } else {
            //send an alert saying that the email is not entered or something
            alertMsg.setTextColor(Color.parseColor("#ff0000"));
            alertMsg.setText("Please Enter Your Email First, Then Click the 'Forget My Password'.");

        }
    }

    //This button isn't actually for signing up, it just brings up the sign up form
    public void onClickSignUpInit(View v) {
        ViewFlipper vf = (ViewFlipper) findViewById(R.id.viewFlipper);
        vf.showNext();
    }

    public void onClickSignUp(View v) {

        String email_s = email.getText().toString();
        uname = username.getText().toString();
        pw = password.getText().toString();
        String phone = phoneNum.getText().toString();
        String addr1_s = addr1.getText().toString();
        String addr2_s = null;
        String addr3_s = null;

        //Check if the user chose to add more addresses or not
        if (addr2 != null) addr2_s = addr2.getText().toString();
        if (addr3 != null) addr3_s = addr3.getText().toString();

        if (missInfo(uname) || missInfo(pw) || missInfo(email_s)) {
            //if the user didn't type in their username and password, we alert them

            alertMsg.setText("Missing information, cannot proceed!");
            return;
        }

        ParseUser user = new ParseUser();

        user.setUsername(uname);
        user.setPassword(pw);
        user.setEmail(email_s);
        //We need to later determine if user.emailVerified == true, This means that our user is not a robot or something.
        user.put("NumRating", 2.5);
        user.put("Rating", 1);
        //setting up extra information associated with the user

        if (!missInfo(phone)) user.put("phoneNumber", phone);
        if (!missInfo(addr1_s)) user.put("addr1", GeoAssistant.getLocationFromAddress(addr1_s, this));
        if (!missInfo(addr2_s)) user.put("addr2", GeoAssistant.getLocationFromAddress(addr2_s, this));
        if (!missInfo(addr3_s)) user.put("addr3", GeoAssistant.getLocationFromAddress(addr3_s, this));
        Log.d("SIGNING UP", uname + " PLEASE WAIT");

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    // Hooray! Now sign in with the account that has just been created

                    Log.d("DONE SIGNUP", "JUMP");
                    alertMsg.setText("Signed In");
                    //Go to the usermainpage activity
                    Intent mainP = new Intent(ACTLoginSelf.this, ACTRequest.class);
                    startActivity(mainP);

                } else {
                    // Sign up didn't succeed. Look at the ParseException
                    // to figure out what went wrong
                    Log.d("FAILED", "AHA, DIDN'T SEE THIS COMING!" + e.getMessage());
                    alertMsg.setText("Missing information, cannot proceed! " + e.getMessage());
                    //report is there are duplicate accounts existing and such! ============TBD============
                }
            }
        });
    }


    public void requestedSuccessfully() {
        Toast.makeText(getApplicationContext(),
                "Please check you email in a few minutes for reset link.", Toast.LENGTH_LONG)
                .show();
    }

    public void requestDidNotSucceed() {
        Toast.makeText(getApplicationContext(),
                "Sorry our server is having a glitch, please try later!", Toast.LENGTH_LONG)
                .show();
    }
}