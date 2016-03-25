package com.parse.starter;

import android.app.Activity;
import android.app.LauncherActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
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
import android.widget.ListView;
import android.widget.PopupMenu;
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
	private String cateSelected, addrSelected, rewardSelected, user_name, request_purpose, file_long_clicked;
	private Button postButton;
    private TextView charCount;
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
    private List<String[]> chatValues;
    private List<Integer> resources, alignment;
    private MsgAdapter msgAdapterReq, msgAdapterChat;
    private ListView listViewRequest, listViewChat;
    MyThreads convList;

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
        installation = ParseInstallation.getCurrentInstallation();
        user = ParseUser.getCurrentUser();

        user.saveInBackground();

        installation.put("username", user.getUsername());
        installation.saveInBackground();

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.new_logo);

        Spinner spinner = (Spinner) findViewById(R.id.topbar_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.user_addresses, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        configureRequestView();

        ViewGroup request = (ViewGroup) findViewById(R.id.request_panel);

        configureMenu(request);

        configurePostButton(request);

        /*Note to self: chatValues is just a reference to the actual header list, we can bypass it*/
        convList  = new MyThreads(this);
        chatValues = convList.getHeader();

        /*For testing purposes*/
        /*chatValues.add(new String[]{"Hao Wu", "4", "Can someone please lend me a iPad Charger, thank you!"});
        chatValues.add(new String[]{"J. Ma", "3", "Will any one pass by Starbucks on their way to Robarts? Can you grep me some coffee?"});*/


        configureChatView();

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


                JSONObject jsonObject;
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
                    if(requestObject.getPurpose().equals("ask")){
                        alignment.add(-50);
                        resources.add(R.drawable.speech__bubble_white);
                    }else{
                        alignment.add(50);
                        resources.add(R.drawable.speech__bubble_red);
                    }
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
                    * Update the count at the top bar
                    * Write message to corresponding files, messgaeObject => file
                    * */
                    updateCounter(1);
                    String fname = new String();
                    try{
                        fname = jsonObject.getString("username");
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                    fname = MyThreads.toFile(fname);
                    convList.fileChange(fname, jsonObject);
                    convList.numChange.add(fname);

                    highLightConv(); //HAO: highlight this updated conversation

                    msgAdapterChat.notifyDataSetChanged();
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

		if (id == R.id.nav_manage) {

		} else if (id == R.id.nav_contactus) {
            String url = "http://www.bodybuilding.com";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
		} else if(id == R.id.nav_tutorial){
            ImageView iv = (ImageView)findViewById(R.id.tutorial_cam);
            if(iv.getVisibility()==View.VISIBLE){
                iv.setVisibility(View.INVISIBLE);
            }else{
                iv.setVisibility(View.VISIBLE);
            }
        } else if(id == R.id.nav_delete){

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
            post.put("purpose", request_purpose);
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
        try {
            post.put("rating", user.get("Rating"));
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
        resources.add(R.drawable.speech__bubble_white);

        alignment = new ArrayList<>();
        alignment.add(-50);

        msgAdapterReq = new MsgAdapter(this, R.layout.request_item, resources, alignment, fields, r_values);

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

                /*boolean thread_exist = false;
                for (String[] s : chatValues) {
                    if (s[0].equals(item[0]) && s[2].equals(item[2])) {
                        thread_exist = true;
                    }
                }*/

                String[] chatItem = new String[]{item[0], String.valueOf(r_values.get(position)[4]), item[2]};

                if (convList.newConversation(chatItem)) {
                    msgAdapterChat.notifyDataSetChanged();
                }

                /*if (!thread_exist) {
                    chatValues.add(chatItem);
                    msgAdapterChat.notifyDataSetChanged();
                }*/


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

        msgAdapterChat = new MsgAdapter(this, R.layout.threads_row_layout, fields, chatValues);

        listViewChat = (ListView) findViewById(R.id.threads);
        listViewChat.setAdapter(msgAdapterChat);

        listViewChat.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String[] item = (String[]) parent.getItemAtPosition(position);
                view.setSelected(false);
                listViewChat.setItemChecked(position, false);
                String fname = MyThreads.toFile(item[0]);
                convList.numChange.remove(fname);
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
                final String[] item = (String[]) parent.getItemAtPosition(position);
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

    private void updateCounter(int increment){
        TextView countView = (TextView) findViewById(R.id.topbar_textview);
        int count = Integer.parseInt(countView.getText().toString());
        count = count + increment;
        countView.setText(String.valueOf(count));
    }

    private void clearCounter(){
        TextView countView = (TextView) findViewById(R.id.topbar_textview);
        countView.setText("0");
    }

    /*Hightlight conversations that have received an update/message*/
    private void highLightConv(){
        int index = 0;
        for (String[] s : chatValues){
            System.out.println("array length" + s.length);
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
        if(resultCode == Activity.RESULT_OK){
            ArrayList<String> requestCollected = data.getStringArrayListExtra("RequestCollection");
            if ( !requestCollected.isEmpty() ){
                for ( String s : requestCollected){
                    JSONObject jobject;
                    try {
                        jobject = new JSONObject(s);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        continue;
                    }
                    RequestObject requestObject = new RequestObject(jobject);
                    r_values.add(requestObject.spitValueList());
                }
            }
            msgAdapterReq.notifyDataSetChanged();
            int additionalCounter = data.getIntExtra("CounterValue", 0);
            updateCounter(additionalCounter);
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            //Write your code if there's no result
            /*HAO: Don't think this will ever happen to be honest*/
        }
    }//onActivityResult

}

