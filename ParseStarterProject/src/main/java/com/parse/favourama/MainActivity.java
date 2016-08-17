package com.parse.favourama;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.parse.ParseAnalytics;

import java.io.File;
import java.io.IOException;


public class MainActivity extends AppCompatActivity{

    private float x1,x2;
    static final int MIN_DISTANCE = 300;
    private ViewFlipper welcomeFlipper;
    int f_index;
    boolean doneSwapping;

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
            welcomeFlipper = (ViewFlipper) findViewById(R.id.welcome_flipper);
            f_index = 0;
            doneSwapping = false;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event){
        this.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(doneSwapping){
            doneSwapping = false;
            return super.onTouchEvent(event);
        }

        switch(event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                float deltaX = x2 - x1;
                if (x2 - x1 >= MIN_DISTANCE)
                {
                    /*Swipe to right, show previous page*/
                    welcomeFlipper.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left));
                    welcomeFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right));
                    welcomeFlipper.showPrevious();
                    f_index--;
                    if(f_index < 0){
                        f_index = 2;
                    }
                    highlightPoint(f_index);
                    doneSwapping = true;
                } else if(x2 - x1 <= -MIN_DISTANCE){
                    /*Swipe to left, show next page*/
                    welcomeFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
                    welcomeFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_left));
                    welcomeFlipper.showNext();
                    f_index++;
                    f_index%=3;
                    highlightPoint(f_index);
                    doneSwapping = true;
                }
                break;
        }

        return super.onTouchEvent(event);
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
        findViewById(R.id.license_agreement_container).setVisibility(View.VISIBLE);
    }

    public void highlightPoint(int index){
        switch (index){
            case 0:
                ((ImageView)findViewById(R.id.findex_i1)).setImageResource(R.drawable.f_intro_point_hl);
                ((ImageView)findViewById(R.id.findex_i2)).setImageResource(R.drawable.f_intro_point);
                ((ImageView)findViewById(R.id.findex_i3)).setImageResource(R.drawable.f_intro_point);
                break;
            case 1:
                ((ImageView)findViewById(R.id.findex_i1)).setImageResource(R.drawable.f_intro_point);
                ((ImageView)findViewById(R.id.findex_i2)).setImageResource(R.drawable.f_intro_point_hl);
                ((ImageView)findViewById(R.id.findex_i3)).setImageResource(R.drawable.f_intro_point);
                break;
            case 2:
                ((ImageView)findViewById(R.id.findex_i1)).setImageResource(R.drawable.f_intro_point);
                ((ImageView)findViewById(R.id.findex_i2)).setImageResource(R.drawable.f_intro_point);
                ((ImageView)findViewById(R.id.findex_i3)).setImageResource(R.drawable.f_intro_point_hl);
                break;
        }
    }
}