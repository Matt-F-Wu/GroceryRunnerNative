package com.parse.starter;

import android.app.Application;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SendCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ACTRequest extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
	private CheckBox addr1, addr2, addr3, cate1, cate2, cate3, reward1, reward2, reward3;
	private EditText postEditText;
	private SeekBar radius;
	private String cateSelected, addrSelected, rewardSelected, user_name;
	private Button postButton;
    private ParseInstallation installation;
	public static int RADIUS_OFFSET = 100;
	public static int MAX_QUERY_RESULTS = 100;
    final Context context = this;
    //PopupWindow pwGlobal;
    private GoogleMap mMap;
    private static int UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static int FAST_INTERVAL_CEILING_IN_MILLISECONDS = 2500;
    private LocationRequest locationRequest;
    private GoogleApiClient locationClient;
    private Location currentLocation;
    private Location lastLocation;
    private ParseUser user;
    private BroadcastReceiver receiver;
    private int flipperIndex = 0;
    private List<String[]> r_values;
    private MsgAdapter msgAdapterReq;
    private ListView listViewRequest;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_main_page);
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.

        //I commented out this map because it is causing runtime failures.
		/*SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);*/

        // Associate this user with this device
        ParseInstallation curIns = ParseInstallation.getCurrentInstallation();
        user = ParseUser.getCurrentUser();

        curIns.put("username", user.getUsername());
        curIns.saveInBackground();

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.logo);

        Spinner spinner = (Spinner) findViewById(R.id.topbar_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.user_addresses, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        int[] fields = new int[]{R.id.r_list_uname, R.id.r_list_cate, R.id.r_list_note, R.id.r_list_reward};

        r_values= new ArrayList<String[]>();

        /*HAO to JENERMY: A warm welcoming messgae I designed, do you like it?*/
        r_values.add(new String[]{"Favourama Official", "Welcome" + curIns.getString("username"),
                "We wish our service can make your life easier",
                "Best of luck!"});

        msgAdapterReq = new MsgAdapter(this, R.layout.request_item, fields, r_values);

        listViewRequest = (ListView) findViewById(R.id.show_requests);
        listViewRequest.setAdapter(msgAdapterReq);

        /*Note to self: Need to implement onclick listener later to listView*/

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

        user_name = user.getUsername();
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


                JSONObject jsonObject = null;
                String content = intent.getExtras().getString("CONTENT");
                try {
                    jsonObject = new JSONObject(content);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                if(action.equals("com.parse.favourama.HANDLE_FAVOURAMA_REQUESTS")){
                    RequestObject requestObject = new RequestObject(jsonObject);
                    r_values.add(requestObject.spitValueList());
                    msgAdapterReq.notifyDataSetChanged();
                    //scrollToBottom(msgAdapterReq, listViewRequest);
                }
                else if (action.equals("com.parse.favourama.HANDLE_FAVOURAMA_MESSAGES")){
                    /* HAO to JEREMY
                    *Everytime you receive a message, you write to the message file and notify there has been changes made to the files
                    * Make subclass MessageObject and make a constructor and constructs from JSONObject
                    *
                    * MessageObject messageObject = new MessageObject(data);
                    * description = messageObject.getNote();
                    * */
                    /*Update the count at the top bar*/
                    updateCounter();
                }

            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.parse.favourama.HANDLE_FAVOURAMA_REQUESTS");
        filter.addAction("com.parse.favourama.HANDLE_FAVOURAMA_MESSAGES");
        registerReceiver(receiver, filter);
		
	}

    @Override
    protected void onStart() {
        locationClient.connect();
        super.onStart();
        Log.d("START", "!!!!!!!!!!!!!!!!!!!!!!!! + " + GoogleApiAvailability.getInstance().GOOGLE_PLAY_SERVICES_VERSION_CODE);
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
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);

		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_notification) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		// Handle navigation view item clicks here.
		int id = item.getItemId();

		if (id == R.id.nav_camera) {
			// Handle the camera action
		} else if (id == R.id.nav_gallery) {

		} else if (id == R.id.nav_slideshow) {

		} else if (id == R.id.nav_manage) {

		} else if (id == R.id.nav_share) {

		} else if (id == R.id.nav_send) {

		} else if(id == R.id.nav_settings){

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
	    /*LayoutInflater inflater = (LayoutInflater)
	    this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View request = inflater.inflate(R.layout.request, null, false);
	    request.measure(View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
	    
	    //Display the popupwindow
	    final PopupWindow pw = new PopupWindow(
	       request,
	       800, //request.getMeasuredHeight(),
	       1000, //request.getMeasuredWidth(),
	       true);
        //Set the parent view of this popup to be the main user space of the user page !!!
	    pw.showAtLocation(this.findViewById(R.id.main_user_space), Gravity.CENTER, 0, 0);
	    
	    //configure the popupwindow
	    pw.setOutsideTouchable(true);
        pw.setTouchable(true);

        pw.setBackgroundDrawable(new ColorDrawable());
        pw.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    //if clicked outside of the popup, the popup closes
                    pw.dismiss();
                    return true;
                } else {
                    return false;*//* Hndle events happening inside the popup, may not need to implement anything*//*
                }
            }
        });*/

        /*pwGlobal = pw;*/

        flip(2);

        ViewGroup request = (ViewGroup) findViewById(R.id.request_panel);

	    configureMenu(request);

		configurePostButton(request);
		
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
		addr1 = (CheckBox) pop.findViewById(R.id.addr_one);
        addr2 = (CheckBox) pop.findViewById(R.id.addr_two);
        addr3 = (CheckBox) pop.findViewById(R.id.addr_three);
        addrSelected = addr1.getText().toString();

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
		ParseGeoPoint location = getPILocation(); //
		String note = postEditText.getText().toString().trim();
		final JSONObject post = new JSONObject();
		double rad = (double) (radius.getProgress() + RADIUS_OFFSET)/1000;
        try {
            post.put("TYPE", "REQUEST");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            post.put("latitude", location.getLatitude());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            post.put("longitude", location.getLongitude());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            post.put("note", note);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            post.put("rad", rad);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            post.put("category", cateSelected);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            post.put("reward", rewardSelected);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            post.put("address", addrSelected);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            post.put("username", user_name);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Query users in vicinity, and send the post to them via cloud
		queryAndSend(location, rad, post);
	    
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

    private ParseGeoPoint getPILocation(){
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
        }

        flipperIndex = index;
        viewFlipper.setDisplayedChild(index);
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

    //top bar actions
    public void onChatButtonClicked (View v){
        flip(1);

        int[] fields = new int[]{R.id.thread_uname, R.id.thread_rating, R.id.thread_description};

        List<String[]> values = new ArrayList<String[]>();
        values.add(new String[]{"Hao Wu", "4", "Can someone please lend me a iPad Charger, thank you!"});
        values.add(new String[]{"J. Ma", "3", "Will any one pass by Starbucks on their way to Robarts? Can you grep me some coffee?"});

        MsgAdapter msgAdapter = new MsgAdapter(this, R.layout.threads_row_layout, fields, values);

        ListView listView = (ListView) findViewById(R.id.threads);
        listView.setAdapter(msgAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String[] item = (String[]) parent.getItemAtPosition(position);
                Bundle b = new Bundle();
                b.putStringArray("ThreadHeader", item);
                Intent i = new Intent(ACTRequest.this, ACTMsg.class);
                i.putExtras(b);
                startActivity(i);
            }

        });
    }

    private void updateCounter(){
        TextView countView = (TextView) findViewById(R.id.topbar_textview);
        int count = Integer.parseInt(countView.getText().toString());
        count = count + 1;
        countView.setText(String.valueOf(count));
    }

    public void scrollToBottom(MsgAdapter adapter, ListView listView){
        listView.setSelection(adapter.getCount() - 1);
    }

}

