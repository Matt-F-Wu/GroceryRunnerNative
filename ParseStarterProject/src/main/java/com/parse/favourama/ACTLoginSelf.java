package com.parse.favourama;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SignUpCallback;


public class ACTLoginSelf extends AppCompatActivity {
    EditText username, password, cfpassword, phoneNum, email, addr1, addr2, addr3;
    TextView alertMsg;
    String uname, pw;
    TextInputLayout unameFloat;

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
                //Log.d("Log In Previous User", "SUCESSFUL");
                Intent mainP = new Intent(ACTLoginSelf.this, ACTRequest.class);
                startActivity(mainP);
            }
            //!!!
        } 
        
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        cfpassword = (EditText) findViewById(R.id.password_confirm);
        phoneNum = (EditText) findViewById(R.id.phone_num);
        addr1 = (EditText) findViewById(R.id.addr_one);
        email = (EditText) findViewById(R.id.email);
        alertMsg = (TextView) findViewById(R.id.alert_msg);
        addr2 = (EditText) findViewById(R.id.addr_two);
        addr3 = (EditText) findViewById(R.id.addr_three);
        unameFloat = (TextInputLayout) findViewById(R.id.uname_float);
        CheckBox checkBox = (CheckBox) findViewById(R.id.sign_in_use_email);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                                                @Override
                                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                    
                                                    if(isChecked) {
                                                        unameFloat.setHint("Email");
                                                    }else{
                                                        unameFloat.setHint("Username");
                                                    }

                                                }
                                            });
        cfpassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                /*Do nothing*/
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                /*Do nothing*/
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(password.getText().toString().equals(s.toString())){
                    ((TextView)findViewById(R.id.match_pwd)).setText("Passwords matched!");
                }else {
                    ((TextView)findViewById(R.id.match_pwd)).setText("Passwords do not match!");
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
        Toast.makeText(this, "Signing in...", Toast.LENGTH_SHORT).show();
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


        ParseUser.logInInBackground(uname, pw, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    // Hooray! The user is logged in. Go to the mainpage activity
                    if (!user.getBoolean("emailVerified")) {
                        /*The user has not confirmed his/her email address*/

                        waitForVerify();
                    } else {

                        Intent mainP = new Intent(ACTLoginSelf.this, ACTRequest.class);
                        startActivity(mainP);
                    }
                    //!!!!!!!!!!!!!!
                } else {
                    // Sign in failed. Look at the ParseException to see what happened. =======TBD=========
                    if (e.getCode() == 101) {
                        showError("Wrong username/password combination.");
                    } else {
                        showError("Sign In Failed: " + e.getMessage());
                    }
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
            showError("Please First Enter Your Email, Then Click 'RESET PASSWORD'");
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
        alertMsg.setText("");
        findViewById(R.id.ava_state_container).setVisibility(View.VISIBLE);

        /*Set up the check availability part*/
        ((TextView) findViewById(R.id.ava_state_text)).setText("");
        ((ImageView) findViewById(R.id.ava_state_img)).setImageDrawable(null);
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
            showError("Missing information, cannot proceed!");
            return;
        }

        /*Check if user accepted agreement*/
        if( ! ((CheckBox)findViewById(R.id.i_accept_la)).isChecked() ){
            /*User did not accept license agreement yet*/
            showError("Please accept License Agreement");
            return;
        }

        /* Check if password matched */
        if( ! password.getText().toString().equals(cfpassword.getText().toString())){
            showError("Passwords do not match");
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


        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    // Hooray! automatically sign in with the account that has just been created
                    if ( !ParseUser.getCurrentUser().getBoolean("emailVerified") ){
                        /*The user has not confirmed his/her email address*/

                        waitForVerify();
                    }else {

                        alertMsg.setText("Signed In");
                        //Go to the usermainpage activity
                        Intent mainP = new Intent(ACTLoginSelf.this, ACTRequest.class);
                        startActivity(mainP);
                    }

                } else {
                    // Sign up didn't succeed. Look at the ParseException
                    // to figure out what went wrong

                    showError("Cannot proceed: " + e.getMessage());
                    //report is there are duplicate accounts existing and such! ============TBD============
                }
            }
        });
    }


    public void flipToLogIn(View view) {
        ViewFlipper vf = (ViewFlipper) findViewById(R.id.viewFlipper);
        vf.setDisplayedChild(0);
        alertMsg.setText("");
        findViewById(R.id.ava_state_container).setVisibility(View.GONE);
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
        alertMsg.setText("");
        findViewById(R.id.ava_state_container).setVisibility(View.GONE);
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

    public void checkAvail(View view) {
        String uname = username.getText().toString().trim();
        if(uname.isEmpty()) return;

        final Context c = this;
        final ImageView imgView = (ImageView) findViewById(R.id.ava_state_img);
        final TextView textView = (TextView) findViewById(R.id.ava_state_text);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.ava_state_prog);

        /*final Handler mHandler = new Handler();

        new Thread(new Runnable() {
            public void run() {
                mHandler.post(new Runnable() {
                    public void run() {
                        progressBar.setProgress(100);
                    }
                });
            }
        }).start();*/

        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.whereEqualTo("username", uname);

        progressBar.setVisibility(View.VISIBLE);
        userQuery.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                progressBar.setVisibility(View.GONE);
                if(e != null && e.getCode() != 101) {
                    Toast.makeText(c, "An error has occured: "
                            + e.getMessage()
                            + " Sorry, please try again!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (parseUser == null){
                    /*The name is available*/
                    imgView.setImageResource(R.drawable.ic_check_black_24dp);
                    textView.setText("It's available!");
                }else{
                    imgView.setImageResource(R.drawable.ic_close_black_24dp);
                    textView.setText("Sorry, It's taken.");
                }
            }
        });
    }

    private void showErrorDialog(String message, FCallback callback){
        FragmentManager fm = this.getSupportFragmentManager();
        ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment();
        errorDialogFragment.setMsg(message);
        if (callback != null) {errorDialogFragment.setfCallback(callback);}
        errorDialogFragment.show(fm, "location_failure");
    }

    private void showError(String toshow){
        alertMsg.setText(toshow);
        showErrorDialog(toshow, null);
    }

    public void showLicAg(View view) {
        showErrorDialog(getString(R.string.eula_text), null);
    }
}
