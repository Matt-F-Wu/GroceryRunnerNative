package com.parse.starter;

import android.app.TabActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TabHost;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

/**
 * Created by Administrator on 2015/12/2.
 */
public class ActUserPage extends TabActivity {

    protected GoogleSignInAccount acct;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_main_page);

        Bundle b = this.getIntent().getExtras();
        if (b != null)
            acct = b.getParcelable("userGoogleInfo");
        //set up the tabs
        TabHost tabHost = getTabHost();
        tabHost.setup();

        TabHost.TabSpec tabSpec1 = tabHost.newTabSpec("activities");
        tabSpec1.setContent(R.id.tabActivities);
        tabSpec1.setIndicator("Activities");

        TabHost.TabSpec tabSpec2 = tabHost.newTabSpec("profile");
        tabSpec2.setContent(R.id.tabProfile);
        tabSpec2.setIndicator("Profile");

        TabHost.TabSpec tabSpec3 = tabHost.newTabSpec("map");
        tabSpec3.setContent(R.id.tabMap);
        tabSpec3.setIndicator("Map");

        tabHost.addTab(tabSpec1);
        tabHost.addTab(tabSpec2);
        tabHost.addTab(tabSpec3);

        tabHost.setCurrentTab(1);
    }
}
