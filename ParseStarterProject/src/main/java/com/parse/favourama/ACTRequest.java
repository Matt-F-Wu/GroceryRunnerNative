package com.parse.favourama;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.support.v7.widget.SwitchCompat;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class ACTRequest extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
	private EditText postEditText;
	private String cateSelected, addrSelected, rewardSelected, user_name, request_purpose, file_long_clicked;
    private String[] request_long_clicked;
	private Button postButton;
    private TextView charCount;
    private ParseInstallation installation;
	public static int RADIUS_OFFSET = 100;
    final Context context = this;
    private GoogleMap mMap;
    private static int UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static int FAST_INTERVAL_CEILING_IN_MILLISECONDS = 2500;
    private static String show_topic_file = "SHOWTOPICS";
    public static String REQ_SAVED_BODY = "FavReqBody";
    public static String REQ_SAVED_PICS = "FavReqPics";
    public static String REQ_SAVED_RES = "FavReqRes";
    public static String REQ_SAVED_SIZE = "FavReqSize";
    public static String SAVED_FLIPPER_INDEX = "FavSavedFlipIndex";
    public static String CONV_TO_HIGHLIGHT = "FavouramaCONVH.json";
    private boolean realTime = true;
    private boolean onSeekbar = false;
    private float x1,x2,prevX, prevY, initY;
    static final int MIN_DISTANCE = 300;
    private LocationRequest locationRequest;
    private GoogleApiClient locationClient;
    private Location currentLocation;
    private Location lastLocation;
    private ParseUser user;
    private BroadcastReceiver receiver;
    private int flipperIndex = 0;
    private List<String[]> r_values;
    private List<Integer> resources;
    private LinkedList<String> user_pics;
    private LinkedList<Integer> flipperStack;
    private MsgAdapter msgAdapterReq;
    private ThreadAdapter msgAdapterChat;
    private ListView listViewRequest, listViewChat;
    MyThreads convList;
    private HashSet<String> blocked_users;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_main_page);
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
	
        // Associate this user with this device
        installation = ParseInstallation.getCurrentInstallation();
        user = ParseUser.getCurrentUser();
		
	if(user == null || user.getUsername() == null){
		/*User did not login*/
		Intent i = new Intent(this, ACTLoginSelf.class);
	    startActivity(i);
	    finish();
        return;
	}

        helpSetUserDir(user);

        user_name = user.getUsername();
        installation.put("username", user_name);
        //if( !installation.has("ImageChannel") ) installation.put("ImageChannel", ImageChannel.makeImageChannel(user_name, getResources()));

        installation.saveInBackground();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*Get rid of the default title*/
        //getSupportActionBar().setDisplayShowTitleEnabled(false);

        final Spinner spinner = (Spinner) findViewById(R.id.topbar_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.user_addresses, R.layout.spinner_user_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0) {
                    setRealTime();
                } else {
                    if(!isNetworkAvailable()){
                        showErrorDialog(null,"Please connect to the Internet!",null);
                        setRealTime();
                        spinner.setSelection(0);
                    }else{
                        realTime = false;
                        String streetAddr = user.getString("addr" + position);
                        if (missInfo(streetAddr)) {
                            setRealTime();
                            spinner.setSelection(0);
                            /*If the selection is not valid, automatically go back to selecting using current address*/
                            showErrorDialog(null, "Sorry, you have not provided this address to us" +
                                    ", Please add it to your profile", new FCallback() {
                                @Override
                                public void callBack() {

                                    checkUpdateProfile();
                                }
                            });
                        } else {
                            ParseGeoPoint locPoint = GeoAssistant.spitGeoPoint(GeoAssistant.getLocationFromAddress(streetAddr, context));
                            if (locPoint == null) {
                                setRealTime();
                                spinner.setSelection(0);
                            /*If the selection is not valid, automatically go back to selecting using current address*/
                                addressError();
                            } else {
                                installation.put("location", locPoint);
                                installation.saveInBackground();
                            }
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Hao: for now we don't do anything
            }

        });

        flipperStack = new LinkedList<>();
        flipperStack.add(new Integer(0));

        configureRequestView(savedInstanceState);

        ViewGroup request = (ViewGroup) findViewById(R.id.request_panel);

        configureMenu(request);

        configurePostButton(request);

        convList  = new MyThreads(this);

        configureChatView();
        
	    populate_profile();
        if (savedInstanceState != null) {
            int last_index = savedInstanceState.getInt(SAVED_FLIPPER_INDEX);

            if(last_index == 3){
                checkUpdateProfile();
            }else{
                flip(last_index);
                /*Silently populate profile*/
                user.fetchInBackground();
            }

        }else {
            /*Silently populate profile*/
            user.fetchInBackground();
        }

        /*Hao:  opened by clicking notification, we handle the intent's data here*/
        onNewIntent(getIntent());


		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

        View navHeader = navigationView.getHeaderView(0);
        TextView userName = (TextView) navHeader.findViewById(R.id.nav_username);
        /*TextView userEmail = (TextView) navHeader.findViewById(R.id.nav_user_email);*/

        userName.setText(user_name);
        setProfilePic(false);
        /*userEmail.setText(user.getEmail());*/
        // Create a new global location parameters object
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        // Create a new location client, using the enclosing class to handle callbacks.

        if (locationClient == null) {
            locationClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //... update ui here
                String action = intent.getAction();


                JSONObject jsonObject;
                String content = intent.getExtras().getString("CONTENT");
                try {
                    jsonObject = new JSONObject(content);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                if(action.equals("com.parse.favourama.HANDLE_FAVOURAMA_REQUESTS")){
                    addRequest(jsonObject);
                    //scrollToBottom(msgAdapterReq, listViewRequest);
                }
                else if (action.equals("com.parse.favourama.HANDLE_FAVOURAMA_MESSAGES")){
                    /* HAO
                    * Update the count at the top bar
                    * Write message to corresponding files, messgaeObject => file
                    * */
                    String fname = new String();
                    try{
                        fname = jsonObject.getString("username");
                    }catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                    fname = MyThreads.toFile(fname);
                    convList.fileChange(fname, jsonObject);
                    if( !StarterApplication.isInMessage() ){
	                    try{
	                        String chat_content = jsonObject.getString("content");
	                        String txt_type = jsonObject.getString("ctype");
	                        if (txt_type.equals(ChatMessage.PICTURE_TYPE)){
	                            ImageChannel.saveImageToFile(chat_content, getApplicationContext(), null);
	                        }
	                    }catch(org.json.JSONException e){
	                        e.printStackTrace();
	                    }
                    }
                    msgAdapterChat.notifyDataSetChanged();

                    if(!StarterApplication.isInMessage()) {
                        convList.numChange.add(fname);
                        updateCounter(convList.numChange.size());

                        //HAO: highlight this updated conversation
                        highLightConv();
                    }
                } else if (action.equals("com.parse.favourama.HANDLE_FAVOURAMA_TOPICS")){
                        handleTopics(jsonObject);
                }

            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.parse.favourama.HANDLE_FAVOURAMA_REQUESTS");
        filter.addAction("com.parse.favourama.HANDLE_FAVOURAMA_MESSAGES");
        filter.addAction("com.parse.favourama.HANDLE_FAVOURAMA_TOPICS");
        registerReceiver(receiver, filter);


        //radius seekbar
        SeekBar radius = (SeekBar) findViewById(R.id.radius);
        radius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double rad;
                int raw = seekBar.getProgress();
                if(raw <= 1900) {
                    rad = (double) (raw + RADIUS_OFFSET) / 1000;
                }else{
                    rad = 2.0 + ((double)(raw - 1900) * 9)/1000;
                }
                String rad_str = new DecimalFormat("#.# km").format(rad);
                ((TextView) findViewById(R.id.radius_display)).setText(rad_str);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                onSeekbar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                onSeekbar = false;
            }
        });

	}

    @Override
    protected void onStart() {
        locationClient.connect();
        super.onStart();

    }

    private void setRealTime(){
        realTime = true;
        if(lastLocation != null) {
            installation.put("location", geoPointFromLocation(lastLocation));
            installation.saveInBackground();
        }
    }


    private void addRequest(JSONObject jsonObject){
        RequestObject requestObject = new RequestObject(jsonObject);

        Long reqTime = Long.parseLong(requestObject.getTime());
        Long currentTime = System.currentTimeMillis();

        if(currentTime - reqTime > 5 * 60 * 60 * 1000){
            //The request is too old, it is longer than 5 hours, not showing it
            Toast.makeText(this, "Some requests expired and are not shown!", Toast.LENGTH_LONG).show();
            String imgId = requestObject.getUserPic();
            File iFile = new File(StarterApplication.getUserFilesDir(), DownloadImageTask.reqImgPrefix + imgId);
            iFile.delete();
            return;
        }

        MyThreads.fileWrite(jsonObject, "reqSaved.json");

        /*If the user is blocked, don't show his/her request*/
        String uname = requestObject.getUser();
        if(blocked_users.contains(uname)){
            return;
        }

        /*Maximum list of 100 requests*/
        if(r_values.size() > 100){
            /*removes the oldest one*/
            r_values.remove(0);
            String imgToRemove = user_pics.remove(0);
            File iFile = new File(StarterApplication.getUserFilesDir(), DownloadImageTask.reqImgPrefix + imgToRemove);
            iFile.delete();
        }

        r_values.add(requestObject.spitValueList());
        user_pics.add(requestObject.getUserPic());
        if(requestObject.getPurpose().equals("seek")){
            //alignment.add(-50);
            resources.add(R.drawable.gray_req_bg); /*speech__bubble_white*/
        }else{
            //alignment.add(50);
            resources.add(R.drawable.red_req_bg);
        }
        msgAdapterReq.notifyDataSetChanged();
    }

    @Override
    protected void onNewIntent(Intent intent){
        /*Handle the intent sent from notifications*/
        if (intent != null && intent.getBooleanExtra("notification", false)) {
	    int enter = intent.getIntExtra("enter", 1);
            if(enter == 1){
                flip(0);
            }else if(enter == 2){
                flip(2);
                //clearCounter();
            }
            
            notificationFileProcessing();
            
        }
    }
    
    private void notificationFileProcessing(){
            File notiFile = new File(StarterApplication.getUserFilesDir(), "favouramaNotification.json");
            
            //If the file doesn't exist, just return
            if( !notiFile.exists() ) return;
            
            LinkedList<JSONObject> notificationList = new LinkedList<>();
            MyThreads.readLine(notiFile, notificationList, this);

            String type;
            for (JSONObject jsonObject : notificationList){
                try {
                    type = jsonObject.getString("TYPE");
                } catch (JSONException e) {
                    e.printStackTrace();
                    //Log.d("Push Receive Exception", "failed to retrieve type");
                    continue;
                }

                if(type.equals("REQUEST")){
                    addRequest(jsonObject);
                }else if(type.equals("MESSAGE")){
                    String uname = new String();
                    try{
                        uname = jsonObject.getString("username");
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String fname = MyThreads.toFile(uname);
		    /*Download images*/
                    try{
                        String chat_content = jsonObject.getString("content");
                        String txt_type = jsonObject.getString("ctype");
                        if (txt_type.equals(ChatMessage.PICTURE_TYPE)){
                            ImageChannel.saveImageToFile(chat_content, getApplicationContext(), null);
                        }
                    }catch(org.json.JSONException e){
                        e.printStackTrace();
                    }

                    convList.fileChange(fname, jsonObject);
                    convList.numChange.add(fname);
                    updateCounter(convList.numChange.size());
                    //chatValues = convList.getHeader();
                    msgAdapterChat.notifyDataSetChanged();
                    highLightConv();
                } else if(type.equals("TOPICS")){
                    handleTopics(jsonObject);
                }
            }
            notiFile.delete(); /*Empty notifications stored*/
    }

    @Override
    public void onStop() {
        if (locationClient.isConnected()) {
            stopPeriodicUpdates();
        }
        locationClient.disconnect();

        super.onStop();
    }

    @Override
    public void onDestroy(){

        if(convList!=null)
            saveCounter(convList.numChange);

        if(receiver!=null)
            unregisterReceiver(receiver);


        super.onDestroy();
    }

    @Override
    public void onPause() {
        if (locationClient.isConnected()) {
            stopPeriodicUpdates();
        }
        locationClient.disconnect();
        StarterApplication.activityPaused();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (locationClient.isConnected()) {
            startPeriodicUpdates();
        }
        StarterApplication.activityResumed();
        StarterApplication.setNotInMessage();
        
        //Process possible notifications
        notificationFileProcessing();
    }

    @Override
    public void onConnected(Bundle bundle) {
        currentLocation = getLocation();

        if(currentLocation != null && realTime) {
            installation = ParseInstallation.getCurrentInstallation();
            installation.put("location", geoPointFromLocation(currentLocation));
            installation.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        //Log.d("SAVE INSTALLATION", "SUCCESS");
                    } else {
                        String msg;
                        if (e.getCode() == 100) {
                        /*Poor internet connection*/
                            msg = "please check your internet connection.";
                        } else {
                            msg = e.getMessage();
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        startPeriodicUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        locationClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(ACTRequest.this, 6);
            } catch (IntentSender.SendIntentException e) {
            }
        } else {
            //Connection failure cannot be resolved
            Toast.makeText(getApplicationContext(),
                    "Unable to connect to Google Service, sorry! " + connectionResult.getErrorMessage(), Toast.LENGTH_LONG)
                    .show();
            //Log.d("CONNECTION FAIL", connectionResult.getErrorCode() + " Unable to connect to Google Service, sorry >_<");
        }
    }

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
            if(flipperStack.size() > 1){
                flipback();
            }else {
                super.onBackPressed();
            }
		}
	}

    @Override
    public boolean dispatchTouchEvent(MotionEvent event){
        if(!onSeekbar) this.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){

        switch(event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                prevX = x1;
                prevY = event.getY();
                initY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float curX = event.getX();
                float curY = event.getY();

                if(Math.abs(curX - prevX) > Math.abs(curY - prevY)){
                    /*Mostly horizontally moving*/
                    event.setLocation(curX, initY);
                    prevX = curX;
                    prevY = curY;
                }else {
                    prevX = curX;
                    prevY = curY;
                    initY = curY;
                }

                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();

                if (x2 - x1 >= MIN_DISTANCE)
                {
                    /*Swipe to right, show previous page*/
                    if(flipperIndex != 0) {
                        event.setLocation(x2, prevY);

                        flip(flipperIndex - 1);
                    }
                } else if(x2 - x1 <= -MIN_DISTANCE){
                    /*Swipe to left, show next page*/
                    if(flipperIndex != 3){
                        event.setLocation(x2, prevY);

                        flip(flipperIndex + 1);
                    }
                }
                break;
        }

        return super.onTouchEvent(event);
    }

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		// Handle navigation view item clicks here.
		int id = item.getItemId();

		if (id == R.id.nav_manage) {

            Bundle b = new Bundle();
            b.putInt("ACTION", ACTmenu.ACTION_SETTING);
            Intent i = new Intent(ACTRequest.this, ACTmenu.class);
            i.putExtras(b);
            startActivity(i);

		} else if(id == R.id.nav_invite){

            Bundle b = new Bundle();
            b.putInt("ACTION", ACTmenu.ACTION_INVITE);
            Intent i = new Intent(ACTRequest.this, ACTmenu.class);
            i.putExtras(b);
            startActivity(i);

        } else if (id == R.id.nav_contactus) {
            String url = "http://favourama.com/";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
		} else if(id == R.id.nav_tutorial){
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            Bundle b = new Bundle();
            b.putInt("ACTION", ACTmenu.ACTION_TUT);
            Intent i = new Intent(ACTRequest.this, ACTmenu.class);
            i.putExtras(b);
            startActivity(i);

        } else if(id == R.id.nav_logout){
            ParseUser.logOut();
            Intent i = new Intent(this, ACTLoginSelf.class);
            startActivity(i);
            finish();
        }

		return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;

        mMap.setMyLocationEnabled(true);


		LatLng myhome = new LatLng(43.6647340, -79.3842040);

		mMap.addMarker(new MarkerOptions().position(myhome).title("Jeremy's Home"));
		mMap.moveCamera(CameraUpdateFactory.newLatLng(myhome));
		mMap.animateCamera(CameraUpdateFactory.zoomTo((float) 14.5));
	}


	public void onClickNewRequest(View v){
        flip(1);
	}

    public void onCheckboxClickedAddr (View v){
        CheckBox addr1, addr2, addr3, addr4;
        addr1 = (CheckBox) findViewById(R.id.addr_one);
        addr2 = (CheckBox) findViewById(R.id.addr_two);
        addr3 = (CheckBox) findViewById(R.id.addr_three);
        addr4 = (CheckBox) findViewById(R.id.addr_four);
        CheckBox checkBox = (CheckBox) v;
        if (!checkBox.isChecked()) {
            checkBox.setChecked(true);
            return;
        }

        switch (v.getId()) {
            case R.id.addr_one:
                addr2.setChecked(false);
                addr3.setChecked(false);
                addr4.setChecked(false);
                addrSelected = addr1.getText().toString();
                break;
            // We probably don't want the following buttons, and the visual should definitely be different,but I guess we can change it later
            case R.id.addr_two:
                addr1.setChecked(false);
                addr3.setChecked(false);
                addr4.setChecked(false);
                addrSelected = addr2.getText().toString();
                break;
            case R.id.addr_three:
                addr1.setChecked(false);
                addr2.setChecked(false);
                addr4.setChecked(false);
                addrSelected = addr3.getText().toString();
                break;
            case R.id.addr_four:
                addr1.setChecked(false);
                addr2.setChecked(false);
                addr3.setChecked(false);
                addrSelected = addr4.getText().toString();
                break;
        }
    }

    public void onCheckboxClickedCate (View v){

        CheckedTextView checkBox = (CheckedTextView) v;
        if (checkBox.isChecked()) {
            return;
	    }

        checkBox.setChecked(true);

        cateSelected = checkBox.getText().toString();

        Resources resources = getResources();
        String pname = getPackageName();

        for(int i = 1; i <= 11; i++){
            int resID = resources.getIdentifier("cate_" + i, "id", pname);
            if(v.getId() != resID){
                ((CheckedTextView)findViewById(resID)).setChecked(false);
            }
        }
    }

    public void onCheckboxClickedReward (View v){
        CheckBox reward1, reward2, reward3;
        reward1 = (CheckBox) findViewById(R.id.no_reward);
        reward2 = (CheckBox) findViewById(R.id.material_reward);
        reward3 = (CheckBox) findViewById(R.id.money_reward);
        CheckBox checkBox = (CheckBox) v;
        if (!checkBox.isChecked()) {
	    checkBox.setChecked(true);
	    return;
	}

        switch (v.getId()) {
            case R.id.no_reward:
                reward2.setChecked(false);
                reward3.setChecked(false);
                //rewardSelected = reward1.getText().toString();
                rewardSelected = "No Reward";
                break;
// We probably don't want the following buttons, and the visual should definitely be different,but I guess we can change it later
            case R.id.material_reward:
                reward1.setChecked(false);
                reward3.setChecked(false);
                //rewardSelected = reward2.getText().toString();
                rewardSelected = "Gift Reward";
                break;
            case R.id.money_reward:
                reward1.setChecked(false);
                reward2.setChecked(false);
                //rewardSelected = reward3.getText().toString();
                rewardSelected = "Money Reward";
                break;
        }
    }

	private void configureMenu(View pop){
	    //get view members
        CheckedTextView cate1;
        CheckBox addr2, addr3, addr4;
        final View p = pop;
	    postEditText = (EditText) pop.findViewById(R.id.r_description);
        postEditText.addTextChangedListener(new TextWatcher(){
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int aft)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                // this will show characters remaining

                charCount.setText(255 - postEditText.getText().toString().length() + "/255");
            }
        });

        addr2 = (CheckBox) pop.findViewById(R.id.addr_two);
        addr3 = (CheckBox) pop.findViewById(R.id.addr_three);
        addr4 = (CheckBox) pop.findViewById(R.id.addr_four);
        addrSelected = "Current Address";

        makeAddressSelectable(addr2, "addr1");
        makeAddressSelectable(addr3, "addr2");
        makeAddressSelectable(addr4, "addr3");

        SwitchCompat p_switch = (SwitchCompat) findViewById(R.id.favour_purpose);
        p_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    request_purpose = "offer";
                }else{
                    request_purpose = "seek";
                }
            }
        });

        request_purpose = "seek";

        cate1 = (CheckedTextView) pop.findViewById(R.id.cate_1);
        cateSelected = cate1.getText().toString();
		/*Address 2 and Adress 3 should be addresses from the user's account, implement later*/
        rewardSelected = "No Reward"; //reward1.getText().toString();

	}

    private void makeAddressSelectable(CheckBox addr, String value){
        if(!missInfo(user.getString(value))) {
            addr.setVisibility(View.VISIBLE);
            addr.setText(user.getString(value));
        }else{
            addr.setVisibility(View.GONE);
        }
    }
	
	private void configurePostButton(View pop){
		postButton = (Button) pop.findViewById(R.id.post_button);
		postButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (missInfo(cateSelected) || missInfo(addrSelected)) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                    builder1.setTitle("Missing Info");
                    builder1.setMessage("You did not choose address OR category!");
                    builder1.setCancelable(true);
                    builder1.setPositiveButton("Okay",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert1 = builder1.create();
                    alert1.show();
                    // Show Alert if the user did not provide all necessary information
                }

                if(!LocationServices.FusedLocationApi.getLocationAvailability(locationClient).isLocationAvailable() && addrSelected.equals("Current Address")){
                    showErrorDialog(null,"Please enable GPS service!",null);
                }else if(!isNetworkAvailable()) {
                    showErrorDialog(null,"Please connect to the Internet!",null);
                }else{
                    post();
                }

                //dismiss popup after posting request
                /*pwGlobal.dismiss();*/
            }
        });
	}
	
	private void post () {
		/*Getting all the information needed for this request*/
		//location variable needs to be retrieved from google map services
        SeekBar radius = (SeekBar) findViewById(R.id.radius);

        double rad;
        int raw = radius.getProgress();
        if(raw <= 1900) {
            rad = (double) (raw + RADIUS_OFFSET) / 1000;
        }else{
            rad = 2.0 + ((double)(raw - 1900) * 9)/1000;
        }


        //if(beta_test.debug){
            /*For debugging purposes, the radius is set to fixed 1000km*/
           // rad = 1000;
        //}
        ParseGeoPoint location = getPILocation(rad);

        if(location == null){    //address is invalid, cannot obtain lat or long
            return;
        }

		String note = postEditText.getText().toString().trim();

		if(missInfo(note)) {
			Toast.makeText(this,
                                    "Request is empty, please edit", Toast.LENGTH_SHORT)
                                    .show();
			return;
		}

        Toast.makeText(getApplicationContext(),
                "Sending Request", Toast.LENGTH_LONG)
                .show();

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("TYPE", "REQUEST");
        params.put("purpose", request_purpose);
        params.put("latitude", location.getLatitude());
        params.put("longitude", location.getLongitude());
        params.put("note", note);
        params.put("rad", rad);
        params.put("category", cateSelected);
        params.put("reward", rewardSelected);
        params.put("address", addrSelected);
        params.put("username", user_name);
        params.put("rating", user.get("Rating"));
        params.put("time", System.currentTimeMillis());
        Object object = user.get("userpic");
        if( object != null) params.put("userpic", ((ParseObject)object).getObjectId());

        ParseCloud.callFunctionInBackground("sendRequestToUser", params, new FunctionCallback<String>() {
            public void done(String success, ParseException e) {
                if (e == null) {
                    // Push sent successfully
                }else{
                    String msg;
                    if(e.getCode() == 100){
                        /*Poor internet connection*/
                        msg = "please check your internet connection.";
                    }else{
                        msg = e.getMessage();
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                }
            }
        });
	    
	    flip(0);
	}
	
	public static boolean missInfo (String s){
		if ( s == null || s.isEmpty() ){
		return true;
		}
		else{
		return false;
		}
	}


    private void showErrorDialog(String nag, String message, FCallback callback){
        FragmentManager fm = this.getSupportFragmentManager();
        ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment();
        errorDialogFragment.setMsg(message);
        if (nag != null) {errorDialogFragment.setNag(nag);}
        if (callback != null) {errorDialogFragment.setfCallback(callback);}
        errorDialogFragment.show(fm, "location_failure");
    }

    private void addressError(){
        showErrorDialog(null, "Sorry, Could not " +
                "recognize selected address\n" +
                "Please try to:\n1. revise this address\n" +
                "2. Use an adjacent address to your desired location instead", new FCallback() {
            @Override
            public void callBack() {

                checkUpdateProfile();
            }
        });
    }

    private ParseGeoPoint getPILocation(double rad){
	ParseGeoPoint tLocation;
        if(!addrSelected.equals("Current Address")){
            ParseGeoPoint useLocation = GeoAssistant.spitGeoPoint(GeoAssistant.getLocationFromAddress(addrSelected, this));

            if (useLocation == null) {
                addressError();
                return null;
            }else{
                tLocation = useLocation;
            }
        }else {
            tLocation = geoPointFromLocation(lastLocation);
        }
	if(tLocation.distanceInKilometersTo((ParseGeoPoint) installation.get("location")) > rad){
	    JSONObject jsonObject = new JSONObject();
	    try {
            jsonObject.put("address",addrSelected);
            jsonObject.put("category",cateSelected);
            jsonObject.put("latitude",tLocation.getLatitude());
            jsonObject.put("longitude",tLocation.getLongitude());
            jsonObject.put("rad",rad);
            jsonObject.put("username",user_name);
            jsonObject.put("note",postEditText.getText().toString().trim());
            jsonObject.put("purpose", request_purpose);
            jsonObject.put("reward",rewardSelected);
            jsonObject.put("rating", user.get("Rating"));
            Object object = user.get("userpic");
            if( object != null){
                jsonObject.put("userpic", ((ParseObject)object).getObjectId());
            }
            jsonObject.put("time", System.currentTimeMillis());
	        addRequest(jsonObject);
	    } catch (JSONException e) {
		e.printStackTrace();
	    }
	}
	return tLocation;
    }

    protected void startPeriodicUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(locationClient, locationRequest, this);
    }

    private void stopPeriodicUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(locationClient, this);
    }

    private Location getLocation() {

        if(LocationServices.FusedLocationApi.getLocationAvailability(locationClient).isLocationAvailable()){
            return LocationServices.FusedLocationApi.getLastLocation(locationClient);
        }
        else{
            showErrorDialog(null,"Please enable GPS service!",null);
            Location targetLocation = new Location("");
            targetLocation.setLatitude(43.7d);
            targetLocation.setLongitude(-79.4d);
            return targetLocation;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        if (lastLocation != null
                && geoPointFromLocation(location)
                .distanceInKilometersTo(geoPointFromLocation(lastLocation)) < 0.01) {
            return;
        }
        lastLocation = location;

        // Update the location of the installation only when the user choose to receive at current address
        if(realTime) {
            installation.put("location", geoPointFromLocation(lastLocation));
            installation.saveInBackground();
        }
    }




    private ParseGeoPoint geoPointFromLocation (Location loc){
        return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
    }

    public void onClickFlip(View view) {
        flip(0);
    }

    public void flip(int index){
	  if(index == flipperIndex) return;

        ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.main_flipper);
        /*Contract the keyboard when you go to a new flip*/
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(viewFlipper.getWindowToken(), 0);

        if ( index > flipperIndex){
            viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
		    viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_left));
        }else{
            viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_left));
		    viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_right));
        }

        Resources res = getResources();
        String pname = getPackageName();

        for(int i = 0; i <= 3; i++){
            int resID = res.getIdentifier("btab" + i, "id", pname);
            if(i == index) {
                findViewById(resID).setBackgroundResource(R.color.red_tint);
            }else{
                findViewById(resID).setBackgroundResource(R.color.white_trans);
            }
        }

        flipperIndex = index;
        //Remove the index from the flipper stack if it already exists, and add it to tali
        if(flipperStack.contains(index)) flipperStack.remove(new Integer(index));
        
        flipperStack.add(new Integer(index));
        viewFlipper.setDisplayedChild(index);
    }

    public void flipback(){
        int index = flipperStack.get(flipperStack.size() - 2);
        flipperStack.removeLast();
        flip(index);
    }

    public void removeConv(MenuItem item) {
        /*Hao to Self:
        *
        * Probably should consider adding a confirmation step, like are you sure you want to delete?
        *
        * And then the user has to confirm to actually delete anything.
        *
        * */
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to delete this conversation?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Actually Delete
                        if (!file_long_clicked.isEmpty()) convList.deleteFile(file_long_clicked);

                        //chatValues = convList.getHeader();
                        msgAdapterChat.notifyDataSetChanged();

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Do nothing
                    }
                });
        // Create the AlertDialog object and return it
        builder.create().show();
    }

    public void showAttr(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(convList.showFileAttribute(file_long_clicked))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Do nothing
                    }
                });
        builder.create().show();
    }

    public void checkUpdateProfile(){
	    flip(3);
	    findViewById(R.id.profile_progressbar).setVisibility(View.VISIBLE);
	    findViewById(R.id.profile_page).setBackgroundColor(0x80c0c0c0);
            user.fetchInBackground(new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser parseUser, ParseException e) {
                    /*Fills with new data*/
                    if (e == null) {
                        populate_profile();
                    } else {
                        String msg = "Could not retrieve profile: ";
                        if (e.getCode() == 100) {
	                        /*Poor internet connection*/
                            msg += "please check your internet connection.";
                        } else {
                            msg += e.getMessage();
                        }
                        showErrorDialog(null, msg, null);
                    }
                    findViewById(R.id.profile_progressbar).setVisibility(View.GONE);
                    findViewById(R.id.profile_page).setBackgroundColor(0x00ffffff);
                }
            });
    }

    public void editProfile(View view) {

        checkUpdateProfile();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    /*Creates a directory for this user, and set it gobally for easy access*/
    private void helpSetUserDir(ParseUser user){
        String name = user.getUsername();
        File user_dir = new File(getFilesDir(), name);
        if(!user_dir.exists()){
            user_dir.mkdir();
        }

        StarterApplication.setUserFilesDir(user_dir);
    }

    private void configureRequestView(Bundle savedInstanceState){
        int[] fields = new int[]{R.id.r_list_uname, R.id.r_list_cate, R.id.r_list_note, R.id.r_list_reward};

        r_values= new ArrayList<String[]>();
        resources = new ArrayList<>();
        user_pics = new LinkedList<>();

        blocked_users = new HashSet<>();

        msgAdapterReq = new MsgAdapter(this, R.layout.request_item, resources, user_pics, fields, r_values, installation);

        listViewRequest = (ListView) findViewById(R.id.show_requests);
        listViewRequest.setAdapter(msgAdapterReq);

        listViewRequest.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                /*Construct intent uri containing user location*/
                request_long_clicked = r_values.get(position);

                PopupMenu popupMenu = new PopupMenu(context, view);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.request_longclick_menu, popupMenu.getMenu());
                popupMenu.show();

                return true;
            }
        });

        /*HAO to JENERMY: A warm welcoming messgae I designed, do you like it?*/
        r_values.add(new String[]{"Favourama Official", "Welcome! " + installation.getString("username"),
                "We wish our service can make your life easier",
                "Best of luck!",
                "5", "43.6628917", "-79.3956564", String.valueOf(System.currentTimeMillis())});

        resources.add(R.drawable.gray_req_bg);

        user_pics.add(null);

        File reqSaved = new File(StarterApplication.getUserFilesDir(), "reqSaved.json");
        if (reqSaved.exists()) {
            // Restore value of members from saved state
            LinkedList<JSONObject> requestList = new LinkedList<>();
            MyThreads.readLine(reqSaved, requestList, this);
            /*First clean the file then add requests back*/
            reqSaved.delete();

            for (JSONObject req : requestList){
                //add one line for debug
                Log.d("REQC", req.toString());
                addRequest(req);
            }
        }

        charCount = (TextView) findViewById(R.id.character_count);

        /*Update the timestamp and distance every 2 minutes*/
        final Handler handler = new Handler();
        handler.postDelayed( new Runnable() {

            @Override
            public void run() {
                msgAdapterReq.notifyDataSetChanged();
                handler.postDelayed( this, 120 * 1000 );
            }
        }, 120 * 1000 );
    }

    public void respond(MenuItem m) {

        if (request_long_clicked[0].equals("Favourama Official")) {
            showErrorDialog("Cancel", "Clicking on requests made by Favourama Official will" +
                    " take you to Favourama Topics, do you want to proceed?", new FCallback() {
                @Override
                public void callBack() {
                    startActivity(new Intent(ACTRequest.this, ACTtopics.class));
                }
            });
            return;
        }

        String[] chatItem = new String[]{request_long_clicked[0], String.valueOf(request_long_clicked[4]), request_long_clicked[2]};

        if (convList.newConversation(chatItem)) {
            //chatValues = convList.getHeader();
            msgAdapterChat.notifyDataSetChanged();
        }


        Bundle b = new Bundle();
        b.putStringArray("ThreadHeader", chatItem);
        Intent i = new Intent(ACTRequest.this, ACTMsg.class);
        i.putExtras(b);
        startActivityForResult(i, 0);
    }

    private void configureChatView(){
        int[] fields = new int[]{R.id.thread_uname, R.id.thread_rating, R.id.thread_description};

        //chatValues = convList.getHeader();

        msgAdapterChat = new ThreadAdapter(this, R.layout.threads_row_layout, fields, convList.converThreads);

        listViewChat = (ListView) findViewById(R.id.threads);
        listViewChat.setAdapter(msgAdapterChat);

        listViewChat.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String[] item = ((MyThreads.MsgThread) parent.getItemAtPosition(position)).spitHeader();

                String fname = MyThreads.toFile(item[0]);
                convList.numChange.remove(fname);
                updateCounter(convList.numChange.size());
                highLightConv();
                Bundle b = new Bundle();
                b.putStringArray("ThreadHeader", item);
                Intent i = new Intent(ACTRequest.this, ACTMsg.class);
                i.putExtras(b);
                startActivityForResult(i, 0);
            }

        });

        listViewChat.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final String[] item = ((MyThreads.MsgThread) parent.getItemAtPosition(position)).spitHeader();
                file_long_clicked = MyThreads.toFile(item[0]);
                PopupMenu popupMenu = new PopupMenu(context, view);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.conv_thread_menu, popupMenu.getMenu());
                popupMenu.show();
                return true;
            }
        });
        /*Recover counter value from previous instance*/
        recoverCounter();
        File file = new File(StarterApplication.getUserFilesDir(), show_topic_file);
        if(file.exists()){
            findViewById(R.id.topics_board_container).setVisibility(View.VISIBLE);
            /*Retreive title and display*/
            LinkedList<JSONObject> body = new LinkedList<>();
            MyThreads.readLine(file, body, this);
            if(body.size() > 0) {
                String title = body.get(0).optString("text");
                if (!title.isEmpty())
                    ((TextView) findViewById(R.id.topics_board_brief)).setText(title);
            }
            /*Retrieve Img and display*/
            File imgTP = new File(StarterApplication.getUserFilesDir(), "TOPICSBoardImg.png");
            if(imgTP.exists()){
                /*If there was previously a header img saved, use it as background*/
                Bitmap bmTP = BitmapFactory.decodeFile(StarterApplication.getUserFilesDir().getPath() + File.separator + "TOPICSBoardImg.png");
                LinearLayout container = (LinearLayout) findViewById(R.id.topics_board_container);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    container.setBackground(new BitmapDrawable(context.getResources(), bmTP));
                }else{
                    container.setBackgroundDrawable(new BitmapDrawable(context.getResources(), bmTP));
                }
            }
        }
    }
    //top bar actions
    public void onChatButtonClicked (View v){
        //clearCounter();
        flip(2);


    }

    private void updateCounter(int value){
        TextView countView = (TextView) findViewById(R.id.topbar_textview);
        countView.setText(String.valueOf(value));
    }

    private void recoverCounter(){
        File cthFile = new File(StarterApplication.getUserFilesDir(), CONV_TO_HIGHLIGHT);
        if(cthFile.exists()) {
            LinkedList<JSONObject> cthList = new LinkedList<>();
            MyThreads.readLine(cthFile, cthList, this);
            for (JSONObject j : cthList){
                String cth = j.optString("fname");
                if(!cth.isEmpty()){
                    convList.numChange.add(cth);
                }
            }
            updateCounter(convList.numChange.size());
            highLightConv();
            cthFile.delete();
        }
    }

    /*Hightlight conversations that have received an update/message*/
    private void highLightConv(){
        int index = 0;
        for (MyThreads.MsgThread mt : convList.converThreads){
            String gn = mt.getFilename();
            if (convList.numChange.contains(gn)){
                listViewChat.setItemChecked(index, true);
            }else{
                listViewChat.setItemChecked(index, false);
            }
            index++;
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);


            Intent i = new Intent(ACTRequest.this, ACTImgCrop.class);
            i.putExtra("imageUri", imageUri.toString());
            startActivityForResult(i, 2);
            //set 2 to be the request code to identify ACTImgCrop
        }


        if (requestCode == 2 && resultCode == 300) {
            //delete cache, read back picture from local phone storage
            deleteCache(this);

            LinkedList<String> image = ImageChannel.BitMapToStringPNG(setProfilePic(true));
            if (image != null) {
                /*ParseFile file = new ParseFile(user_name + "_profile_picture.png", image);
                file.saveInBackground();*/
                final ParseObject parseObject = new ParseObject("ImageBox");

                ImageChannel.fillImageBox(parseObject, image, false);
                parseObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Toast.makeText(context,
                                    "Could not send image, please try later! " + e.getMessage(), Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            // Create a column named "userPic" and insert the image
                            ImageChannel.eraseImageFromCloud(user.get("userpic"));
                            user.put("userpic", parseObject);
                            String poID = parseObject.getObjectId();
                            user.put("userpicID", poID);
                            user.saveInBackground();
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("userpicID", poID);
                                MyThreads.fileWrite(jsonObject, "profile_image_id.json");
                            } catch (JSONException e1) {
                                //Log.d("I/OError", "Cannot write to profile_image_id.json");
                                Toast.makeText(context,"Cannot write to file", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

            }else{
                Toast.makeText(context,
                        "Image is too big, please try a smaller one!", Toast.LENGTH_LONG)
                        .show();
                return;
            }
        }

        if (resultCode == Activity.RESULT_CANCELED) {
            //Write your code if there's no result
            /*HAO: Don't think this will ever happen to be honest*/
        }
    }//onActivityResult



    public void updateUserProfile(View view) {
        EditText editEmail = (EditText) findViewById(R.id.Email_edit);
        EditText editPhone = (EditText) findViewById(R.id.phone_edit);
        EditText editAddr1 = (EditText) findViewById(R.id.edit_addr1);
        EditText editAddr2 = (EditText) findViewById(R.id.edit_addr2);
        EditText editAddr3 = (EditText) findViewById(R.id.edit_addr3);
        CheckBox addr2 = (CheckBox) findViewById(R.id.addr_two);
        CheckBox addr3 = (CheckBox) findViewById(R.id.addr_three);
        CheckBox addr4 = (CheckBox) findViewById(R.id.addr_four);
        if (!user.getEmail().equals(editEmail.getText().toString().trim())) {
            user.setEmail(editEmail.getText().toString());
        }

        if (missInfo(user.getString("phoneNumber")) || !user.getString("phoneNumber").equals(editPhone.getText().toString())) {
            user.put("phoneNumber", editPhone.getText().toString());
        }

        updateUserFields("phoneNumber", editPhone);
        if(updateUserFields("addr1", editAddr1)) makeAddressSelectable(addr2, "addr1");
        if(updateUserFields("addr2", editAddr2)) makeAddressSelectable(addr3, "addr2");
        if(updateUserFields("addr3", editAddr3)) makeAddressSelectable(addr4, "addr3");

        user.saveInBackground();

        flip(0);
    }

    private boolean updateUserFields(String key, EditText editText){
        if (missInfo(user.getString(key)) || !user.getString("phoneNumber").equals(editText.getText().toString().trim())) {
            user.put(key, editText.getText().toString().trim());
            return true;
        }else {
            return false;
        }
    }

    public void populate_profile(){
        /*Refresh from disk*/
        user = ParseUser.getCurrentUser();

        EditText editEmail = (EditText) findViewById(R.id.Email_edit);
        EditText editPhone = (EditText) findViewById(R.id.phone_edit);
        EditText editAddr1 = (EditText) findViewById(R.id.edit_addr1);
        EditText editAddr2 = (EditText) findViewById(R.id.edit_addr2);
        EditText editAddr3 = (EditText) findViewById(R.id.edit_addr3);

        EditText []addresses = {editAddr1, editAddr2, editAddr3};

        TextView name = (TextView)findViewById(R.id.name_edit);
        name.setText(user_name);

        editEmail.setText(user.getEmail());
        if ( !invalid(user.getString("phoneNumber"))) {
            editPhone.setText(user.getString("phoneNumber"));
        }else{
            editPhone.setText("");
        }

        for(int i = 0; i < 3; i++){
            int j = i+1;
            if ( !invalid(user.getString("addr" + j)) ){
                addresses[i].setText(user.getString("addr" + j));
            }else{
                addresses[i].setText("");
            }
        }

        RatingBar ratingBar = (RatingBar) findViewById(R.id.rb_profile);
        ratingBar.setRating((float) user.getDouble("Rating"));

    }

    public void cancelAndLeave(View view) {
        //flip back
        flip(0);

        /*Hao: Maybe I should implement a tack to track where I should flip to, always flipping to page 0 seems a bit lazy*/
    }

    private boolean invalid(String s) {
        if (s == null || s.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public void showLocationOnMap(MenuItem item) {
        Uri gmmIntentUri = Uri.parse("geo:" + request_long_clicked[5] + ", " + request_long_clicked[6] + "?q=" + request_long_clicked[5] + ", " + request_long_clicked[6]);

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }else{
            showErrorDialog(null, "Sorry we cannot find a map app to display the user's location, please install Google Maps.", null);
        }
    }

    public void blockUser(MenuItem item) {
        String user_to_block = request_long_clicked[0];
        if(!invalid(user_to_block)){
            blocked_users.add(user_to_block);
        }
    }

    public void reportSpam(MenuItem item) {
        ParseQuery query = new ParseQuery("SpamReport");
        final String bUser = request_long_clicked[0];
        final String reason = request_long_clicked[2];
        query.whereEqualTo("culprit", bUser);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (parseObject == null) {
                    ParseObject report = new ParseObject("SpamReport");
                    report.put("culprit", bUser);
                    report.put("reason1", reason);
                    report.put("num", 1);
                    report.saveInBackground();
                } else {
                    int num = parseObject.getInt("num");
                    num++;
                    parseObject.put("reason" + num, reason);
                    parseObject.put("num", num);
                    parseObject.saveInBackground();
                }
            }

        });

    }

    public void goToTopics(View view) {
        view.findViewById(R.id.new_topics_alert).setVisibility(View.GONE);
        startActivity(new Intent(ACTRequest.this, ACTtopics.class));
    }

    public void clearReqBodyText(View view) {
        ((EditText) findViewById(R.id.r_description)).getText().clear();
    }

    public void showOnMap(View view) {

        if(!isNetworkAvailable()) {
            showErrorDialog(null,"Please connect to the Internet!",null);
        }else {

            switch (view.getId()) {
                case R.id.show_map_icon_1:
                    showOnMapHelper(R.id.edit_addr1);
                    break;
                case R.id.show_map_icon_2:
                    showOnMapHelper(R.id.edit_addr2);
                    break;
                case R.id.show_map_icon_3:
                    showOnMapHelper(R.id.edit_addr3);
                    break;
            }
        }

    }

    private void showOnMapHelper(int id){
        EditText editText = (EditText) findViewById(id);
        String addr = editText.getText().toString();
        if (addr == null || addr.isEmpty()){
            showErrorDialog(null, "Address not provided", null);
            return;
        }
        ParseGeoPoint loc = GeoAssistant.spitGeoPoint(GeoAssistant.getLocationFromAddress(addr, this));
        if(loc == null){
            showErrorDialog(null,"Location cannot be verified, please revise your address or check if your google play service is outdated.",null);
            return;
        }else{
            Uri gmmIntentUri = Uri.parse("geo:" + loc.getLatitude() + ", " + loc.getLongitude() + "?q=" + loc.getLatitude() + ", " + loc.getLongitude());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            }else{
                showErrorDialog(null, "Sorry we cannot find a map app to display the user's location, please install Google Maps.", null);
            }
        }
    }


    public static View getToolbarLogoIcon(Toolbar toolbar){
        //check if contentDescription previously was set
        boolean hadNoContentDescription = TextUtils.isEmpty(toolbar.getLogoDescription());
        String contentDescription = String.valueOf(!hadNoContentDescription ? toolbar.getLogoDescription() : "logoContentDescription");
        toolbar.setLogoDescription(contentDescription);
        ArrayList<View> potentialViews = new ArrayList<View>();
        //find the view based on it's content description, set programatically or with android:contentDescription
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            toolbar.findViewsWithText(potentialViews,contentDescription, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
        }else{
            //For API < 14
            int totalChild = toolbar.getChildCount();
            for (int i=0; i < totalChild; i++){
                View child = toolbar.getChildAt(i);
                if (child.getContentDescription().toString().equals(contentDescription)){
                    potentialViews.add(child);
                }
            }
        }
        //Nav icon is always instantiated at this point because calling setLogoDescription ensures its existence
        View logoIcon = null;
        if(potentialViews.size() > 0){
            logoIcon = potentialViews.get(0);
        }
        //Clear content description if not previously present
        if(hadNoContentDescription)
            toolbar.setLogoDescription(null);
        return logoIcon;
    }

    //image_cropping methods
    public void onProfile_upload_picture(View view){

            CropImage.startPickImageActivity(this);

    }


    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        }
        else if(dir!= null && dir.isFile())
            return dir.delete();
        else {
            return false;
        }
    }

    private Bitmap setProfilePic(Boolean update){
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        View navHeader = navigationView.getHeaderView(0);

        final ImageView profile_user_pic = (ImageView) findViewById(R.id.profile_user_pic);
        final ImageView nav_user_pic = (ImageView) navHeader.findViewById(R.id.nav_user_pic);
        String picture_filename = "profile_picture.jpg";

        String picture_file_path = Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files/" + picture_filename;

        //Log.d("PIC_PATH", "picture_file_path" + picture_file_path);

        final Bitmap bmImg = ImageChannel.decodeScaledDownBitmap(picture_file_path);

        if( bmImg != null) {
            if(update) {
                /*update with proper picture*/
                profile_user_pic.setImageBitmap(bmImg);
                nav_user_pic.setImageBitmap(bmImg);
            }else{
                loadProfilePicture(profile_user_pic, nav_user_pic, bmImg);
            }
        }else{
            if(update) return null;
            // Hao; Set to the default image, I use the logo for now, but change this to something later
            loadProfilePicture(profile_user_pic, nav_user_pic, bmImg);
        }
        return bmImg;
    }

    public void loadProfilePicture(final ImageView profile_user_pic, final ImageView nav_user_pic, final Bitmap bmImg){
        Object cloudImg = user.get("userpic");
        final String imageID = user.getString("userpicID");
        if (cloudImg != null) {
            ParseObject imageObj = (ParseObject) cloudImg;
            imageObj.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    String imgData = ImageChannel.getImageString(parseObject);
                    if (imgData != null) {
                        Bitmap bmp = ImageChannel.StringToBitMap(imgData);
                        if (bmp != null) {
                                    /*See if the local image is up to date*/
                            if (checkProfilePictureIDSndSetImage(profile_user_pic, nav_user_pic, imageID, bmImg)) {
                                //return bmImg;
                            } else {
                                        /*Not storing locally, a trade off between larger code/apk and larger data usage*/
                                profile_user_pic.setImageBitmap(bmp);
                                nav_user_pic.setImageBitmap(bmp);
                            }
                        }
                    }
                }
            });
        } else {
            profile_user_pic.setImageResource(R.drawable.logo);
        }
    }

    public Boolean checkProfilePictureIDSndSetImage(ImageView profile_user_pic, ImageView nav_user_pic, String imageID, Bitmap bmImg){
        LinkedList<JSONObject> jsonObjectArrayList = new LinkedList<>();

        File local_profile_pic = new File(StarterApplication.getUserFilesDir(), "profile_image_id.json");

        if(!local_profile_pic.exists()){
            /*No local profile picture found*/
            return false;
        }

        MyThreads.readLine(local_profile_pic, jsonObjectArrayList, context);
        try {
            JSONObject imgj = jsonObjectArrayList.poll();
            if(imgj != null) {
                if(imgj.getString("userpicID").equals(imageID)){
                    profile_user_pic.setImageBitmap(bmImg);
                    nav_user_pic.setImageBitmap(bmImg);
                    return true;
                }else{
                    return false;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void handleTopics(JSONObject jsonObject){
        String ctype = jsonObject.optString("ctype");
        File file = new File(StarterApplication.getUserFilesDir(), show_topic_file);
        if(ctype.equals("INIT")){
            findViewById(R.id.topics_board_container).setVisibility(View.VISIBLE);
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else if(ctype.equals("PROMO")){
            final LinearLayout container = (LinearLayout) findViewById(R.id.topics_board_container);
            final String img_link = jsonObject.optString("content");
            new ImageChannel.DownloadTPBGTask(container, this, true).execute(img_link);
            final String text = jsonObject.optString("username");
            if(!text.isEmpty()) ((TextView)findViewById(R.id.topics_board_brief)).setText(text);

            file.delete();
            JSONObject tbody = new JSONObject();
            try {
                tbody.put("text", text);
                MyThreads.fileWrite(tbody, show_topic_file);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else if(ctype.equals("DEACT")){
            findViewById(R.id.topics_board_container).setVisibility(View.GONE);
            file.delete();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putInt(SAVED_FLIPPER_INDEX, flipperIndex);

        saveCounter(convList.numChange);
    }

    public void saveCounter(HashSet<String> numChange){
        File cthFile = new File(StarterApplication.getUserFilesDir(), CONV_TO_HIGHLIGHT);
        /*Add a gaurd just to be safe*/
        if(cthFile.exists()){
            cthFile.delete();

        }
        Object[] pArray = numChange.toArray();

        if(pArray != null){
            for (Object o : pArray){
                JSONObject j = new JSONObject();
                try {
                    j.put("fname", o);
                    MyThreads.fileWrite(j, CONV_TO_HIGHLIGHT, StarterApplication.getUserFilesDir());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void SCrespond(View view) {
        View cur = view;
        while(cur.getId() != R.id.request_row_container){
            cur = (View) cur.getParent();
        }
        /*get the overall row*/
        cur = (View) cur.getParent();
        /*Get the listview*/
        ListView listView = (ListView) cur.getParent();
        final int position = listView.getPositionForView(cur);

        request_long_clicked = r_values.get(position);
        respond(null);
    }

    public void SCpinOnMap(View view) {
        View cur = view;
        while(cur.getId() != R.id.request_row_container){
            cur = (View) cur.getParent();
        }
        /*get the overall row*/
        cur = (View) cur.getParent();
        /*Get the listview*/
        ListView listView = (ListView) cur.getParent();
        final int position = listView.getPositionForView(cur);

        request_long_clicked = r_values.get(position);
        showLocationOnMap(null);
    }
}

