package com.parse.starter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
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
        
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null && currentUser.getUsername() != null) {
            // Go to User Main Page directly if user already signed up
            //
            if( !currentUser.getBoolean("emailVerified") ){
                waitForVerify();
            }else {
                Log.d("Log In Previous User", "SUCESSFUL" + currentUser.getUsername());
                Intent mainP = new Intent(ACTLoginSelf.this, ACTRequest.class);
                startActivity(mainP);
            }
            //!!!
        } 
        
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        phoneNum = (EditText) findViewById(R.id.phone_num);
        addr1 = (EditText) findViewById(R.id.addr_one);
        email = (EditText) findViewById(R.id.email);
        alertMsg = (TextView) findViewById(R.id.alert_msg);
        addr2 = (EditText) findViewById(R.id.addr_two);
        addr3 = (EditText) findViewById(R.id.addr_three);
        
        CheckBox checkBox = (CheckBox) findViewById(R.id.sign_in_use_email);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                                                @Override
                                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                    
                                                    if(isChecked) {
                                                        username.setHint("Email");
                                                    }else{
                                                        username.setHint("Username");
                                                    }

                                                }
                                            });
    }

    private boolean missInfo(String s) {
        if (s == null || s.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }
    
    public void reportErrorMessage(ParseException e){
        Toast.makeText(getApplicationContext(),
                "Sorry, " + e.getMessage(), Toast.LENGTH_LONG)
                .show();
    }

    public void onClickSignIn(View v) {
        Toast.makeText(this, "Signing in...", Toast.LENGTH_LONG).show();
        alertMsg.setText("");
        uname = username.getText().toString().trim();
        CheckBox checkBox = (CheckBox) findViewById(R.id.sign_in_use_email);
        if(checkBox.isChecked()){
            //The user intends to sign in with email
            ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
            userQuery.whereEqualTo("email", uname);
            ParseUser user = null;

            try{
                user = userQuery.getFirst();
            } catch (ParseException e) {
                e.printStackTrace();
                reportErrorMessage(e);
                return;
            }

            if(user == null) return;

            uname = user.getUsername();
            
        }
        pw = password.getText().toString();
        Log.d("Username", uname);
        Log.d("Password", pw);
        ParseUser.logInInBackground(uname, pw, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    // Hooray! The user is logged in. Go to the mainpage activity
                    if (!user.getBoolean("emailVerified")) {
                        /*The user has not confirmed his/her email address*/

                        waitForVerify();
                    } else {
                        Log.d("Log In", "SUCESSFUL");
                        Intent mainP = new Intent(ACTLoginSelf.this, ACTRequest.class);
                        startActivity(mainP);
                    }
                    //!!!!!!!!!!!!!!
                } else {
                    // Signup failed. Look at the ParseException to see what happened. =======TBD=========
                    alertMsg.setText("Sign In Failed, please try again >_< " + e.getMessage());
                }
            }
        });

    }

    public void onClickForgetPassword(View v) {
        alertMsg.setText("");
        EditText email_forget = (EditText) findViewById(R.id.email_forget_password);
        String email_s = email_forget.getText().toString().trim();
        if (!missInfo(email_s)) {
            ParseUser.requestPasswordResetInBackground(email_s,
                    new RequestPasswordResetCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(getApplicationContext(),
                                    "Please check you email in a few minutes for reset link.", Toast.LENGTH_LONG)
                                    .show();
                            } else {
                                reportErrorMessage(e);
                            }
                        }
                    });
        } else {
            //send an alert saying that the email is not entered or something
            alertMsg.setText("Please Enter Your Email, Then Click the 'RESET PASSWORD'");

        }
    }

    //This button isn't actually for signing up, it just brings up the sign up form
    public void onClickSignUpInit(View v) {
        CheckBox checkBox = (CheckBox) findViewById(R.id.sign_in_use_email);
        if(checkBox.isChecked()) {
            checkBox.setChecked(false);
        }
        ViewFlipper vf = (ViewFlipper) findViewById(R.id.viewFlipper);
        vf.showNext();
    }

    public void onClickSignUp(View v) {
        Toast.makeText(this, "Signing up...", Toast.LENGTH_LONG).show();
        alertMsg.setText("");
        String email_s = email.getText().toString().trim();
        uname = username.getText().toString().trim();
        pw = password.getText().toString();
        String phone = phoneNum.getText().toString().trim();
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
        user.put("NumRating", 1);
        user.put("Rating", 2.5);
        //setting up extra information associated with the user

        if (!missInfo(phone)) user.put("phoneNumber", phone);
        if (!missInfo(addr1_s)) user.put("addr1", addr1_s);
        if (!missInfo(addr2_s)) user.put("addr2", addr2_s);
        if (!missInfo(addr3_s)) user.put("addr3", addr3_s);
        Log.d("SIGNING UP", uname + " PLEASE WAIT");

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    // Hooray! automatically sign in with the account that has just been created
                    if ( !ParseUser.getCurrentUser().getBoolean("emailVerified") ){
                        /*The user has not confirmed his/her email address*/

                        waitForVerify();
                    }else {
                        Log.d("DONE SIGNUP", "JUMP");
                        alertMsg.setText("Signed In");
                        //Go to the usermainpage activity
                        Intent mainP = new Intent(ACTLoginSelf.this, ACTRequest.class);
                        startActivity(mainP);
                    }

                } else {
                    // Sign up didn't succeed. Look at the ParseException
                    // to figure out what went wrong
                    Log.d("FAILED", "AHA, DIDN'T SEE THIS COMING!" + e.getMessage());
                    alertMsg.setText("Something went wrong, cannot proceed: " + e.getMessage());
                    //report is there are duplicate accounts existing and such! ============TBD============
                }
            }
        });
    }


    public void flipToLogIn(View view) {
        ViewFlipper vf = (ViewFlipper) findViewById(R.id.viewFlipper);
        vf.setDisplayedChild(0);
    }

    public void onClickForgetPasswordDisplay(View view) {
        ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.forget_password_flipper);
        viewFlipper.setDisplayedChild(1);
        alertMsg.setText("");
    }

    public void flipTwiceToLogIn(View view) {
        ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.forget_password_flipper);
        viewFlipper.setDisplayedChild(0);

        ViewFlipper vf = (ViewFlipper) findViewById(R.id.viewFlipper);
        vf.setDisplayedChild(0);
    }

    public void onClickSendVerification(View view) {
        String email_new = ((EditText) findViewById(R.id.email_revise_resend)).getText().toString().trim();
        ParseUser user = ParseUser.getCurrentUser();
        user.setEmail(email_new);
        user.saveInBackground();
        //Maybe we should just save in UI thread, since the UI has nothing elase to do.
        // but this way we give user immediate feed back
        Toast.makeText(this, "New email verification sent!", Toast.LENGTH_LONG);
        findViewById(R.id.resend_email_display).setVisibility(View.GONE);
    }

    public void showResend(View view) {
        findViewById(R.id.resend_email_display).setVisibility(View.VISIBLE);
        String cur_email = email.getText().toString().trim();
        if(cur_email == null || cur_email.isEmpty()){
            cur_email = ParseUser.getCurrentUser().getEmail();
        }
        ((EditText) findViewById(R.id.email_revise_resend)).setText(cur_email);
    }

    public void waitForVerify(){
        ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.forget_password_flipper);
        viewFlipper.setDisplayedChild(2);
    }

    public void checkAndLogin(View view) {
        ParseUser user = ParseUser.getCurrentUser();
        try {
            user.fetch();
            if(user.getBoolean("emailVerified")){
                Intent mainP = new Intent(ACTLoginSelf.this, ACTRequest.class);
                startActivity(mainP);
            }else{
                Toast.makeText(this, "You have not verified your email!", Toast.LENGTH_SHORT).show();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
