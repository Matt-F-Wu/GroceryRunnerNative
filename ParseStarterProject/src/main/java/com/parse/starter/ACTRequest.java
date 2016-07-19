package com.parse.starter;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SendCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class ACTRequest extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
	private CheckBox addr1, addr2, addr3, cate1, cate2, cate3, reward1, reward2, reward3;
	private EditText postEditText;
	private SeekBar radius;
	private String cateSelected, addrSelected, rewardSelected, user_name, request_purpose, file_long_clicked;
	private Button postButton;
    private TextView charCount;
    private ParseInstallation installation;
	public static int RADIUS_OFFSET = 100;
	public static int MAX_QUERY_RESULTS = 100;
    final Context context = this;
    private GoogleMap mMap;
    private static int UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static int FAST_INTERVAL_CEILING_IN_MILLISECONDS = 2500;
    private boolean realTime = true;
    private LocationRequest locationRequest;
    private GoogleApiClient locationClient;
    private Location currentLocation;
    private Location lastLocation;
    private ParseUser user;
    private BroadcastReceiver receiver;
    private int flipperIndex = 0;
    private List<String[]> r_values;
    private List<String[]> chatValues;
    private List<Integer> resources;
    private LinkedList<String> user_pics;
    private LinkedList<Integer> flipperStack;
    private MsgAdapter msgAdapterReq;
    private ThreadAdapter msgAdapterChat;
    private ListView listViewRequest, listViewChat;
    MyThreads convList;
    private HashSet<Integer> edittext_ids;



    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_main_page);
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.

        // Associate this user with this device
        installation = ParseInstallation.getCurrentInstallation();
        user = ParseUser.getCurrentUser();

        user.saveInBackground();

        user_name = user.getUsername();
        installation.put("username", user_name);
        //if( !installation.has("ImageChannel") ) installation.put("ImageChannel", ImageChannel.makeImageChannel(user_name, getResources()));

        installation.saveInBackground();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*Get rid of the default title*/
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        /*Hao to Jeremy: What should we do with the Logo? For now, I hide it*/
        toolbar.setLogo(R.drawable.new_logo);
        View logoView = getToolbarLogoIcon(toolbar);

        if (logoView != null) {
            logoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    flip(0);
                    Log.d("LOGO_CLICK", "Logo Click is Successfully Handled");
                }
            });
        }
        final Spinner spinner = (Spinner) findViewById(R.id.topbar_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.user_addresses, R.layout.spinner_user_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0) {
                    realTime = true;
                } else {
                    realTime = false;
                    installation = ParseInstallation.getCurrentInstallation();
                    String streetAddr = user.getString("addr" + position);
                    if (missInfo(streetAddr)){
                        realTime = true;
                        spinner.setSelection(0);
                        /*If the selection is not valid, automatically go back to selecting using current address*/
                        showErrorDialog("Location Conversion Failed", "Sorry, you have not provided this address to us" +
                                ", Please add it to your profile", new FCallback() {
                            @Override
                            public void callBack() {
                                flip(3);
                                populate_profile();
                            }
                        });
                    } else {
                        ParseGeoPoint locPoint = GeoAssistant.spitGeoPoint(GeoAssistant.getLocationFromAddress(streetAddr, context));
                        if (locPoint == null) {
                            realTime = true;
                            spinner.setSelection(0);
                        /*If the selection is not valid, automatically go back to selecting using current address*/
                            showErrorDialog("Location Conversion Failed", "Sorry, Could not " +
                                    "convert your selected address to latitude/longitude >_<," +
                                    " Please try to revise this address.", new FCallback() {
                                @Override
                                public void callBack() {
                                    flip(3);
                                    populate_profile();
                                }
                            });
                        } else {
                            installation.put("location", locPoint);
                            installation.saveInBackground();
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

        configureRequestView();

        ViewGroup request = (ViewGroup) findViewById(R.id.request_panel);

        configureMenu(request);

        configurePostButton(request);

        convList  = new MyThreads(this);

        configureChatView();

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
        TextView userEmail = (TextView) navHeader.findViewById(R.id.nav_user_email);


        userName.setText(user_name);
        userEmail.setText(user.getEmail());
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
                    }
                    fname = MyThreads.toFile(fname);
                    convList.fileChange(fname, jsonObject);
                    convList.numChange.add(fname);
                    updateCounter(convList.numChange.size());
                    //chatValues = convList.getHeader();
                    msgAdapterChat.notifyDataSetChanged();
                    //HAO: highlight this updated conversation
                    highLightConv();
                }

            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.parse.favourama.HANDLE_FAVOURAMA_REQUESTS");
        filter.addAction("com.parse.favourama.HANDLE_FAVOURAMA_MESSAGES");
        registerReceiver(receiver, filter);

        edittext_ids = new HashSet<>();
        setProfilePic(false);
	}

    @Override
    protected void onStart() {
        locationClient.connect();
        super.onStart();
        Log.d("START", "!!!!!!!!!!!!!!!!!!!!!!!! + " + GoogleApiAvailability.getInstance().GOOGLE_PLAY_SERVICES_VERSION_CODE);
    }


    private void addRequest(JSONObject jsonObject){
        RequestObject requestObject = new RequestObject(jsonObject);
        r_values.add(requestObject.spitValueList());
        user_pics.add(requestObject.getUserPic());
        if(requestObject.getPurpose().equals("ask")){
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
                flip(1);
                clearCounter();
            }
            
            notificationFileProcessing();
            
        }
    }
    
    private void notificationFileProcessing(){
            File notiFile = new File(getFilesDir(), "favouramaNotification.json");
            
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
                    Log.d("Push Receive Exception", "failed to retrieve type");
                    continue;
                }

                if(type.equals("REQUEST")){
                    addRequest(jsonObject);
                }else{
                    String uname = new String();
                    try{
                        uname = jsonObject.getString("username");
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String fname = MyThreads.toFile(uname);

                    convList.fileChange(fname, jsonObject);
                    convList.numChange.add(fname);
                    updateCounter(convList.numChange.size());
                    //chatValues = convList.getHeader();
                    msgAdapterChat.notifyDataSetChanged();
                    highLightConv();
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
        Log.d("CONNECT GOOGLEAPI", ">>>>>>>>>>>>>>>>>>>>>>>>>>>");
        //while (currentLocation == null) {
            currentLocation = getLocation();
        //}
        if(currentLocation != null) {
            Log.d("GET INSTALLATION", "ENTER");
            installation = ParseInstallation.getCurrentInstallation();
            installation.put("location", geoPointFromLocation(currentLocation));
            installation.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        Log.d("SAVE INSTALLATION", "SUCCESS");
                    } else {
                        Log.d("SAVE INSTALLATION", "FAIL " + e.getMessage() + " <><><><><><> Code: " + e.getCode());
                    }
                }
            });
        }
        Log.d("CONNECT GOOGLEAPI", "!!!!!!!!!!!!!!!!!!!!!!!!");
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
                    "Unable to connect to Google Service, sorry >_<" + connectionResult.getErrorMessage(), Toast.LENGTH_LONG)
                    .show();
            Log.d("CONNECTION FAIL", connectionResult.getErrorCode() + " Unable to connect to Google Service, sorry >_<");
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


	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		// Handle navigation view item clicks here.
		int id = item.getItemId();

		if (id == R.id.nav_manage) {
            flip(3);
            populate_profile();
		} else if (id == R.id.nav_contactus) {
            String url = "http://www.bodybuilding.com";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
		} else if(id == R.id.nav_tutorial){
            ImageView iv = (ImageView)findViewById(R.id.tut_appbar_blur);
            iv.setVisibility(View.VISIBLE);

            ImageView iv2 = (ImageView)findViewById(R.id.tut_main_user_space_blur);
            iv2.setVisibility(View.VISIBLE);

            LinearLayout layout1 = (LinearLayout)findViewById(R.id.tut_overlay_layout);
            layout1.setVisibility(View.VISIBLE);

            LinearLayout layout2 = (LinearLayout)findViewById(R.id.tut_overlay_layout_askfavour);
            layout2.setVisibility(View.VISIBLE);

            Button b1 = (Button)findViewById(R.id.tut_ask_favour);
            b1.setVisibility(View.VISIBLE);



        } else if (id == R.id.nav_delete){
            deleteUser();
        } else if(id == R.id.nav_logout){
            ParseUser.logOutInBackground();
            finish();
        }

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;

        mMap.setMyLocationEnabled(true);

		// Add a marker in Sydney and move the camera
		LatLng myhome = new LatLng(43.6647340, -79.3842040);

		mMap.addMarker(new MarkerOptions().position(myhome).title("Jeremy's Home"));
		mMap.moveCamera(CameraUpdateFactory.newLatLng(myhome));
		mMap.animateCamera(CameraUpdateFactory.zoomTo((float) 14.5));
	}


	public void onClickNewRequest(View v){

        flip(2);

        TextView favourTitle = (TextView) findViewById(R.id.favour_title);
        favourTitle.setText("Ask For a Favour");

        request_purpose = "ask";
		
	}

    public void onCheckboxClickedAddr (View v){
        CheckBox checkBox = (CheckBox) v;
        if (!checkBox.isChecked()) return;
        switch (v.getId()) {
            case R.id.addr_one:
                addr2.setChecked(false);
                addr3.setChecked(false);
                addrSelected = addr1.getText().toString();
                break;
            // We probably don't want the following buttons, and the visual should definitely be different,but I guess we can change it later
            case R.id.addr_two:
                addr1.setChecked(false);
                addr3.setChecked(false);
                addrSelected = addr2.getText().toString();
                break;
            case R.id.addr_three:
                addr1.setChecked(false);
                addr2.setChecked(false);
                addrSelected = addr3.getText().toString();
                break;
        }
    }

    public void onCheckboxClickedCate (View v){
        CheckBox checkBox = (CheckBox) v;
        if (!checkBox.isChecked()) return;
        switch (v.getId()) {
            case R.id.cate_one:
                cate2.setChecked(false);
                cate3.setChecked(false);
                cateSelected = cate1.getText().toString();
                break;
// We probably don't want the following buttons, and the visual should definitely be different,but I guess we can change it later
            case R.id.cate_two:
                cate1.setChecked(false);
                cate3.setChecked(false);
                cateSelected = cate2.getText().toString();
                break;
            case R.id.cate_three:
                cate1.setChecked(false);
                cate2.setChecked(false);
                cateSelected = cate3.getText().toString();
                break;
        }
    }

    public void onCheckboxClickedReward (View v){
        CheckBox checkBox = (CheckBox) v;
        if (!checkBox.isChecked()) return;
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

		addr1 = (CheckBox) pop.findViewById(R.id.addr_one);
        addr2 = (CheckBox) pop.findViewById(R.id.addr_two);
        addr3 = (CheckBox) pop.findViewById(R.id.addr_three);
        addrSelected = addr1.getText().toString();

        if(!missInfo(user.getString("addr1"))) {
            addr2.setText(user.getString("addr1"));
        }else{
            addr2.setVisibility(View.GONE);
        }
        if(!missInfo(user.getString("addr2"))) {
            addr3.setText(user.getString("addr2"));
        }else{
            addr3.setVisibility(View.GONE);
        }

        radius = (SeekBar) pop.findViewById(R.id.radius);

        cate1 = (CheckBox) pop.findViewById(R.id.cate_one);
        cate2 = (CheckBox) pop.findViewById(R.id.cate_two);
        cate3 = (CheckBox) pop.findViewById(R.id.cate_three);
        cateSelected = cate1.getText().toString();
		/*Address 2 and Adress 3 should be addresses from the user's account, implement later*/
        reward1 = (CheckBox) pop.findViewById(R.id.no_reward);
        reward2 = (CheckBox) pop.findViewById(R.id.material_reward);
        reward3 = (CheckBox) pop.findViewById(R.id.money_reward);
        rewardSelected = "No Reward"; //reward1.getText().toString();

	}
	
	private void configurePostButton(View pop){
		postButton = (Button) pop.findViewById(R.id.post_button);
		postButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (missInfo(cateSelected) || missInfo(addrSelected)) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                    builder1.setTitle("Missing Info");
                    builder1.setMessage("You did not choose address OR category! >_<");
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
                Toast.makeText(getApplicationContext(),
                        "Sending Request", Toast.LENGTH_LONG)
                        .show();
                post();
                //dismiss popup after posting request
                /*pwGlobal.dismiss();*/
            }
        });
	}
	
	private void post () {
		/*Getting all the information needed for this request*/
		//location variable needs to be retrieved from google map services
		ParseGeoPoint location = getPILocation();

        if(location == null){    //address is invalid, cannot obtain lat or long
            return;
        }

		String note = postEditText.getText().toString().trim();
		double rad = (double) (radius.getProgress() + RADIUS_OFFSET)/1000;

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
        Object object = user.get("userpic");
        if( object != null) params.put("userpic", ((ParseObject)object).getObjectId());

        ParseCloud.callFunctionInBackground("sendRequestToUser", params, new FunctionCallback<String>() {
            public void done(String success, ParseException e) {
                if (e == null) {
                    // Push sent successfully
                }else{
                    Log.d("RFAILED", "Request did not work at cloud " + e.getCode() + " " + e.getMessage());
                }
            }
        });
	    
	    flip(0);
	}
	
	private boolean missInfo (String s){
		if ( s == null || s.isEmpty() ){
		return true;
		}
		else{
		return false;
		}
	}


	private void queryAndSend(ParseGeoPoint loc, double rad, JSONObject post) {
		// Do a query among installations
		final ParseGeoPoint myPoint = new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
		
		ParseQuery insQuery = ParseInstallation.getQuery();
		insQuery.whereWithinKilometers("location", myPoint, rad);
				  
		//insQuery.include("user"); can uncomment this line if we want to retrieve the user object
        insQuery.orderByDescending("createdAt");
        //insQuery.setLimit(MAX_QUERY_RESULTS);
        Log.d("QUERY: ", insQuery.getClassName());

        ParsePush.sendDataInBackground(post, insQuery, new SendCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("push", "Request Sent!");
                } else {
                    Log.d("push", "Request failure >_<" + e.getMessage() + " <><><><><><> Code: " + e.getCode());
                }
            }
        });
    }

    private void showErrorDialog(String tag, String message, FCallback callback){
        FragmentManager fm = this.getSupportFragmentManager();
        ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment();
        errorDialogFragment.setMsg(message);
        if (callback != null) {errorDialogFragment.setfCallback(callback);}
        errorDialogFragment.show(fm, "location_failure");
    }

    private ParseGeoPoint getPILocation(){
        if(!addrSelected.equals("Current Address")){
            ParseGeoPoint useLocation = GeoAssistant.spitGeoPoint(GeoAssistant.getLocationFromAddress(addrSelected, this));

            if (useLocation == null) {
                showErrorDialog("location_failure", "Cannot obtain the latitude " +
                        "and longitude of the address selected, abort request." +
                        " Please try updating your address at profile management!", new FCallback() {
                    @Override
                    public void callBack() {
                        flip(3);
                        populate_profile();
                    }
                });
                return null;
            }else{
                Log.d("AlterLoc", "Latitdude: " + useLocation.getLatitude() + " Longitude: " + useLocation.getLongitude());
                return useLocation;
            }
        }

        installation = ParseInstallation.getCurrentInstallation();
        return (ParseGeoPoint) installation.get("location");
    }

    protected void startPeriodicUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(locationClient, locationRequest, this);
    }

    private void stopPeriodicUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(locationClient, this);
    }

    private Location getLocation() {
        Log.d("GET", "###### Service " + LocationServices.FusedLocationApi.getLocationAvailability(locationClient).toString());
        if(LocationServices.FusedLocationApi.getLocationAvailability(locationClient).isLocationAvailable()){
            return LocationServices.FusedLocationApi.getLastLocation(locationClient);
        }
        else{
            Location targetLocation = new Location("");
            targetLocation.setLatitude(43.7d);
            targetLocation.setLongitude(79.4d);
            return targetLocation;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(!realTime) return; //Hao: if the user is not using current address do not update his/her current address
        currentLocation = location;
        if (lastLocation != null
                && geoPointFromLocation(location)
                .distanceInKilometersTo(geoPointFromLocation(lastLocation)) < 0.01) {
            return;
        }
        lastLocation = location;

        // Update the display
        installation = ParseInstallation.getCurrentInstallation();
        installation.put("location", geoPointFromLocation(lastLocation));
        installation.saveInBackground();
    }




    private ParseGeoPoint geoPointFromLocation (Location loc){
        return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
    }

    public void onClickFlip(View view) {
        flip(0);
    }

    public void flip(int index){
        ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.main_flipper);
        /*Contract the keyboard when you go to a new flip*/
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(viewFlipper.getWindowToken(), 0);
        if (index == 0){
            viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left));
        }else if (index == 1){
            /*HAO to JEREMY: Maybe we want a different animation for this, we will see*/
            viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left));
        }else if (index == 2){
            Animation bottomUp = AnimationUtils.loadAnimation(this,
                    R.anim.slider_up);
            viewFlipper.setInAnimation(bottomUp);
        }else{
            Animation entrance = AnimationUtils.loadAnimation(this,
                    android.R.anim.slide_in_left);
            viewFlipper.setInAnimation(entrance);
        }

        /*flipperIndex records the current index of the flipper, and decides which fashion should the current flipper fade*/
        if (flipperIndex == 0){
            viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right));
        }else if (flipperIndex == 1){
            /*HAO to JEREMY: Maybe we want a different animation for this, we will see*/
            viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right));
        }else if (flipperIndex == 2){
            Animation topDown = AnimationUtils.loadAnimation(this,
                    R.anim.slider_down);
            viewFlipper.setOutAnimation(topDown);
        }else{
            Animation exit = AnimationUtils.loadAnimation(this,
                    android.R.anim.slide_out_right);
            viewFlipper.setInAnimation(exit);
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

    public void onClickNewOffer(View view) {
        flip(2);

        TextView favourTitle = (TextView) findViewById(R.id.favour_title);
        favourTitle.setText("Offer a Favour");

        request_purpose = "offer";
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
                        Log.d("File Deletion: ", "User deleted file " + file_long_clicked);
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

    public void editProfile(View view) {
        flip(3);
        populate_profile();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

    private void configureRequestView(){
        int[] fields = new int[]{R.id.r_list_uname, R.id.r_list_cate, R.id.r_list_note, R.id.r_list_reward};

        r_values= new ArrayList<String[]>();

        /*HAO to JENERMY: A warm welcoming messgae I designed, do you like it?*/
        r_values.add(new String[]{"Favourama Official", "Welcome" + installation.getString("username"),
                "We wish our service can make your life easier",
                "Best of luck!",
                "5"});

        resources = new ArrayList<>();
        resources.add(R.drawable.gray_req_bg);

        user_pics = new LinkedList<>();
        user_pics.add(null);

        msgAdapterReq = new MsgAdapter(this, R.layout.request_item, resources, user_pics, fields, r_values);

        listViewRequest = (ListView) findViewById(R.id.show_requests);
        listViewRequest.setAdapter(msgAdapterReq);

        listViewRequest.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String[] item = (String[]) parent.getItemAtPosition(position);

                if (item[0].equals("Favourama Official")) {
                    return;
                }


                String[] chatItem = new String[]{item[0], String.valueOf(r_values.get(position)[4]), item[2]};

                beta_test.logStringArray(chatItem, "chatItem");

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

        });

        charCount = (TextView) findViewById(R.id.character_count);
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
                view.setSelected(false);
                listViewChat.setItemChecked(position, false);
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
    }
    //top bar actions
    public void onChatButtonClicked (View v){
        clearCounter();
        flip(1);


    }

    private void updateCounter(int value){
        TextView countView = (TextView) findViewById(R.id.topbar_textview);
        countView.setText(String.valueOf(value));
    }

    private void clearCounter(){
        TextView countView = (TextView) findViewById(R.id.topbar_textview);
        countView.setText("0");
    }

    /*Hightlight conversations that have received an update/message*/
    private void highLightConv(){
        int index = 0;
        for (MyThreads.MsgThread mt : convList.converThreads){
            String[] s = mt.spitHeader();
            String gn = MyThreads.toFile(s[0]);
            if (convList.numChange.contains(gn)){
                listViewChat.setItemChecked(index, true);
            }
            index++;
        }
    }

    public void scrollToBottom(MsgAdapter adapter, ListView listView){
        listView.setSelection(adapter.getCount() - 1);
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
                                MyThreads.fileWrite(jsonObject, "profile_image_id.json", context);
                            } catch (JSONException e1) {
                                Log.d("I/OError", "Cannot write to profile_image_id.json");
                            }
                        }
                    }
                });

            }else{
                Toast.makeText(context,
                        "Image is too big, please try a smaller one! >_<", Toast.LENGTH_LONG)
                        .show();
                return;
            }
        }

        if (resultCode == Activity.RESULT_CANCELED) {
            //Write your code if there's no result
            /*HAO: Don't think this will ever happen to be honest*/
        }
    }//onActivityResult



    //tutorial Jeremy
    public void onClickTutNext(View v){

        ImageView iv = (ImageView)findViewById(R.id.tut_appbar_blur);
        iv.setVisibility(View.VISIBLE);

        ImageView iv2 = (ImageView)findViewById(R.id.tut_main_user_space_blur);
        iv2.setVisibility(View.VISIBLE);

        LinearLayout layout1 = (LinearLayout)findViewById(R.id.tut_overlay_layout);
        layout1.setVisibility(View.VISIBLE);

        LinearLayout layout2 = (LinearLayout)findViewById(R.id.tut_overlay_layout_askfavour);
        layout2.setVisibility(View.GONE);

        Button b1 = (Button)findViewById(R.id.tut_ask_favour);
        b1.setVisibility(View.INVISIBLE);

        LinearLayout layout3 = (LinearLayout)findViewById(R.id.tut_overlay_layout_offerfavour);
        layout3.setVisibility(View.VISIBLE);

        Button b2 = (Button)findViewById(R.id.tut_offer_favour);
        b2.setVisibility(View.VISIBLE);


    }

    public void onTutorialExit(View view){

        ImageView iv = (ImageView)findViewById(R.id.tut_appbar_blur);
        iv.setVisibility(View.INVISIBLE);

        ImageView iv2 = (ImageView)findViewById(R.id.tut_main_user_space_blur);
        iv2.setVisibility(View.INVISIBLE);

        LinearLayout layout1 = (LinearLayout)findViewById(R.id.tut_overlay_layout);
        layout1.setVisibility(View.INVISIBLE);

        LinearLayout layout2 = (LinearLayout)findViewById(R.id.tut_overlay_layout_askfavour);
        layout2.setVisibility(View.GONE);

        Button b1 = (Button)findViewById(R.id.tut_ask_favour);
        b1.setVisibility(View.INVISIBLE);

        LinearLayout layout3 = (LinearLayout)findViewById(R.id.tut_overlay_layout_offerfavour);
        layout3.setVisibility(View.INVISIBLE);

        Button b2 = (Button)findViewById(R.id.tut_offer_favour);
        b2.setVisibility(View.INVISIBLE);


    }

    public void updateUserProfile(View view) {
        EditText editEmail = (EditText) findViewById(R.id.Email_edit);
        EditText editPhone = (EditText) findViewById(R.id.phone_edit);
        EditText editAddr1 = (EditText) findViewById(R.id.edit_addr1);
        EditText editAddr2 = (EditText) findViewById(R.id.edit_addr2);
        EditText editAddr3 = (EditText) findViewById(R.id.edit_addr3);

        if (edittext_ids.contains(editEmail.getId())) {
            user.setEmail(editEmail.getText().toString());
        }

        if (edittext_ids.contains(editPhone.getId())) {
            user.put("phoneNumber", editPhone.getText().toString());
        }

        if (edittext_ids.contains(editAddr1.getId())) {
            user.put("addr1", editAddr1.getText().toString());
        }

        if (edittext_ids.contains(editAddr2.getId())) {
            user.put("addr2", editAddr2.getText().toString());
        }

        if (edittext_ids.contains(editAddr3.getId())) {
            user.put("addr3", editAddr3.getText().toString());
        }

        user.saveInBackground();

        edittext_ids.clear(); //empties the hashset

        flip(0);
    }

    public void populate_profile(){

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
            editPhone.setText("Not provided");
        }

        editEmail.addTextChangedListener(new TextWatcherExt(editEmail));
        editPhone.addTextChangedListener(new TextWatcherExt(editPhone));

        for(int i = 0; i < 3; i++){
            int j = i+1;
            if ( !invalid(user.getString("addr" + j)) ){
                addresses[i].setText(user.getString("addr" + j));
            }else{
                addresses[i].setText("Not provided");
            }
            addresses[i].addTextChangedListener(new TextWatcherExt(addresses[i]));
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


    private class TextWatcherExt implements TextWatcher {
        int id;

        public TextWatcherExt(EditText editText){
            id = editText.getId();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            edittext_ids.add(id);
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



    private void deleteUser(){
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
        errorDialogFragment.show(fm, "location_failure");
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
                    user.delete();
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

        Log.d("PIC_PATH", "picture_file_path" + picture_file_path);

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
        MyThreads.readLine(new File(context.getFilesDir(), "profile_image_id.json"), jsonObjectArrayList, context);
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

}

