package com.parse.starter;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class ACTMsg extends AppCompatActivity {
    private String[] header;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actmsg);

        Bundle b = this.getIntent().getExtras();
        if (b != null)
            header = b.getStringArray("ThreadHeader");

        TextView headerView = (TextView) findViewById(R.id.msg_thread_header);
        headerView.setText(header.toString());

    }

    public void onClickSend(View view) {
    }

    public void backToMain(View view) {
        finish();
    }
}
