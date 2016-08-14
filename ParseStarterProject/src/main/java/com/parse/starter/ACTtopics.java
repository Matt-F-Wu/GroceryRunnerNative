package com.parse.starter;


import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.parse.ParseUser;

import java.io.File;
import java.io.IOException;


public class ACTtopics extends AppCompatActivity {
    private WebView webView;
    static String topics_notification_disabled = "TOPDISABLE";
    private boolean noti_topics_diabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topics_page);

        webView = (WebView) findViewById(R.id.topic_web_view);
        webView.loadUrl("http://henry-h-wu.github.io/");

        webView.setWebViewClient(new MyWebViewClient());

        /*Enable JavaScript*/
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        File file = new File(StarterApplication.getUserFilesDir(), topics_notification_disabled);
        
        if(file.exists()){
            noti_topics_diabled = true;
        }else{
            noti_topics_diabled = false;
        }
    }

    @Override
    public void onPause() {
        StarterApplication.activityPaused();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        StarterApplication.activityResumed();
        StarterApplication.setInMessage();
        StarterApplication.setToWhom("admin");
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    public void backToMain(View view) {
        finish();
    }

    public void backPage(View view) {
        onBackPressed();
    }

    public void forwardPage(View view) {
        if(webView.canGoForward()){
            webView.goForward();
        }
    }

    public void navButtonChangeState(){
        ImageButton forward = (ImageButton) findViewById(R.id.nav_forward_btn);
        if(webView.canGoForward()){
            forward.setImageResource(R.drawable.ic_forward_white);
        }else {
            forward.setImageResource(R.drawable.ic_forward_gray);
        }
    }

    public void showSetting(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.topics_settings, popupMenu.getMenu());
	  if (noti_topics_diabled){
	  	popupMenu.getMenu().getItem(0).setTitle("Turn On Notification");
	  }else{
	  	popupMenu.getMenu().getItem(0).setTitle("Turn Off Notification");
	  }
        popupMenu.show();
    }

    public void turnOffTopicsNT(MenuItem item) {
        File file = new File(StarterApplication.getUserFilesDir(), topics_notification_disabled);
        if(!noti_topics_diabled){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
		noti_topics_diabled = true;
		//item.setTitle("Turn On Notification");
        }else{
		noti_topics_diabled = false;
            file.delete();
		//item.setTitle("Turn Off Notification");
        }
    }

    public void applyToContribute(MenuItem item) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"admin@favourama.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Topics Contributor Application");
        intent.putExtra(Intent.EXTRA_TEXT, ParseUser.getCurrentUser().getUsername() + " : ");

        startActivity(Intent.createChooser(intent, "Send Email"));
    }


    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished (WebView view,
                             String url){
            navButtonChangeState();
            /*Hide the progress bar when the loading is done*/
            ProgressBar pb = (ProgressBar) findViewById(R.id.topics_progress_bar);
            pb.setVisibility(View.GONE);
        }

        @Override
        public void onPageStarted (WebView view,
                                   String url,
                                   Bitmap favicon){
            /*Show a progress bar when page starts to load*/
            ProgressBar pb = (ProgressBar) findViewById(R.id.topics_progress_bar);
            pb.setVisibility(View.VISIBLE);
        }
    }

}
