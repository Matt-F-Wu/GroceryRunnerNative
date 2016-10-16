package com.parse.favourama;

import android.graphics.Matrix;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;

import java.io.File;
import java.io.IOException;

/**
 * Created by HaoWu on 10/12/2016.
 */
public class ACTmenu extends AppCompatActivity {
    private int action;
    static int ACTION_INVITE = 1;
    public static int ACTION_SETTING = 2;
    public static int ACTION_TUT = 3;
    public static String req_vib_enabled = "REQ_VIB_ENABLED.flag";
    public static String msg_vib_disabled = "MSG_VIB_DISABLED.flag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = this.getIntent().getExtras();
        if (b != null)
            action = b.getInt("ACTION");

        if(action == ACTION_INVITE){

            setContentView(R.layout.invite_page);

            Toolbar toolbar = (Toolbar) findViewById(R.id.act_menu_tb);
            setSupportActionBar(toolbar);

            getSupportActionBar().setTitle("Settings");

        }else if(action == ACTION_SETTING){

            setContentView(R.layout.settings_page);
            Toolbar toolbar = (Toolbar) findViewById(R.id.act_menu_tb);
            setSupportActionBar(toolbar);

            getSupportActionBar().setTitle("Settings");

            SwitchCompat s_req = (SwitchCompat)findViewById(R.id.req_vib_setting);
            s_req.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    File rve = new File(StarterApplication.getUserFilesDir(), req_vib_enabled);
                    if (isChecked) {
                        try {
                            rve.createNewFile();
                        } catch (IOException e) {
                            buttonView.setChecked(false);
                            Toast.makeText(getApplicationContext(), "Cannot perform this operation.", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    } else {
                        rve.delete();
                    }
                }
            });

            SwitchCompat s_msg = (SwitchCompat)findViewById(R.id.msg_vib_setting);
            s_msg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    File mvd = new File(StarterApplication.getUserFilesDir(), msg_vib_disabled);
                    if(isChecked){
                        mvd.delete();
                    }else{
                        try {
                            mvd.createNewFile();
                        } catch (IOException e) {
                            buttonView.setChecked(true);
                            Toast.makeText(getApplicationContext(), "Cannot perform this operation.", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }
            });

        }else if(action == ACTION_TUT){
            setContentView(R.layout.tut_layout);
        }else {
            /*Wrong intent*/
            finish();
        }

    }

    public void leaveACTmenu(View view) {
        finish();
    }

    public void showNextTut(View view) {
        String tag = view.getTag().toString();
        if(tag.equals("cont")){
            findViewById(R.id.tut_long_menu).setVisibility(View.VISIBLE);
            findViewById(R.id.tut_long_leg).setVisibility(View.VISIBLE);
            findViewById(R.id.tut_leg_1).setVisibility(View.GONE);
            findViewById(R.id.tut_leg_2).setVisibility(View.GONE);
            ImageView img = (ImageView) findViewById(R.id.tut_main_img);
            img.setScaleType(ImageView.ScaleType.MATRIX);
            final Matrix matrix = img.getImageMatrix();
            matrix.postTranslate(img.getWidth() * 0.4f, 0);
            img.setImageMatrix(matrix);
            view.setTag("watch");
            ((Button) view).setText("Need more help? Watch a Video!");
        }else if(tag.equals("watch")){
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://favourama.com/tutorials.html")));
            finish();
        }
    }

    public void deleteUser(View view){
        FragmentManager fm = this.getSupportFragmentManager();
        ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment();
        errorDialogFragment.setMsg("Are you sure you would like to delete your account?");
        errorDialogFragment.setPos("Yes");
        errorDialogFragment.setNag("Cancel");
        errorDialogFragment.setfCallback(new FCallback() {
            @Override
            public void callBack() {
                confirmDelete();
            }
        });
        errorDialogFragment.show(fm, "WARN");
    }

    private void confirmDelete(){
        FragmentManager fm = this.getSupportFragmentManager();
        ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment();
        errorDialogFragment.setMsg("Please confirm again, click yes to delete.");
        errorDialogFragment.setPos("Yes");
        errorDialogFragment.setNag("Cancel");
        errorDialogFragment.setfCallback(new FCallback() {
            @Override
            public void callBack() {
                try {
                    ParseUser.getCurrentUser().delete();
                } catch (ParseException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "Unable to delete account, please try again later", Toast.LENGTH_LONG)
                            .show();
                }
                finish();
            }
        });
        errorDialogFragment.show(fm, "location_failure");
    }

    public void sendEmail(View view) {
        String email = ((EditText) findViewById(R.id.f_email)).getText().toString().trim();
        if(email == null || email.isEmpty()) {
            Toast.makeText(getApplicationContext(),
                    "Email address is empty, please enter", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        intent.putExtra(Intent.EXTRA_SUBJECT, ParseUser.getCurrentUser().getUsername() + " invites you to join Favourama!");
        intent.putExtra(Intent.EXTRA_TEXT, "Hey friend,\n\n Favourama is a cool App. You should try it out!\n\nhttps://play.google.com/store/apps/details?id=com.parse.favourama&hl=en\n\n" + Html.fromHtml("<a href='https://play.google.com/store/apps/details?id=com.parse.favourama&hl=en'><div style=\"background:#c53a2a; color:white; margin: 10px; padding: 20px; font-size: 20px; display: inline-block\"><b>Favourama</b></div></a>"));

        startActivity(Intent.createChooser(intent, "Send Email Via:"));
    }

    public void shareSomewhere(View view) {
        String message = "This app is cool! Check it out: https://play.google.com/store/apps/details?id=com.parse.favourama&hl=en";
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, message);

        startActivity(Intent.createChooser(share, ""));
    }
}
