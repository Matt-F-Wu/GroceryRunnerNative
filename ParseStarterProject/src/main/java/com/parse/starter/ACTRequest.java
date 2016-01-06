package com.parse.starter;

import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
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
	private CheckBox addr1, addr2, addr3, cate1, cate2, cate3;
	private EditText postEditText;
	private SeekBar radius;
	private String cateSelected, addrSelected;
	private Button postButton;
    private ParseInstallation installation;
	public static int RADIUS_OFFSET = 100;
	public static int MAX_QUERY_RESULTS = 100;
    final Context context = this;
    PopupWindow pwGlobal;
    private GoogleMap mMap;
    private static int UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static int FAST_INTERVAL_CEILING_IN_MILLISECONDS = 2500;
    private LocationRequest locationRequest;
    private GoogleApiClient locationClient;
    private Location currentLocation;
    private Location lastLocation;


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
        curIns.put("username", ParseUser.getCurrentUser().getUsername());
        curIns.saveInBackground();

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.logo);

        Spinner spinner = (Spinner) findViewById(R.id.topbar_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.user_addresses, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);



		/*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
						.setAction("Action", null).show();
			}
		});*/

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

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
    public void onPause() {
        if (locationClient.isConnected()) {
            stopPeriodicUpdates();
        }
        locationClient.disconnect();

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (locationClient.isConnected()) {
            startPeriodicUpdates();
        }
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
	    LayoutInflater inflater = (LayoutInflater)
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
                    return false;/* Hndle events happening inside the popup, may not need to implement anything*/
                }
            }
        });

        pwGlobal = pw;

	    configureMenu(request);

		configurePostButton(request);
		
	}

    public void onCheckboxClickedAddr (View v){
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

	private void configureMenu(View pop){
	    //get view members
        final View p = pop;
	    postEditText = (EditText) pop.findViewById(R.id.r_description);
		addr1 = (CheckBox) pop.findViewById(R.id.addr_one);
        addr2 = (CheckBox) pop.findViewById(R.id.addr_two);
        addr3 = (CheckBox) pop.findViewById(R.id.addr_three);

        radius = (SeekBar) pop.findViewById(R.id.radius);

        cate1 = (CheckBox) pop.findViewById(R.id.cate_one);
        cate2 = (CheckBox) pop.findViewById(R.id.cate_two);
        cate3 = (CheckBox) pop.findViewById(R.id.cate_three);
		/*Address 2 and Adress 3 should be addresses from the user's account, implement later*/

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
                pwGlobal.dismiss();
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
            post.put("address", addrSelected);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            post.put("username", ParseUser.getCurrentUser().getUsername());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Query users in vicinity, and send the post to them via cloud
		queryAndSend(location, rad, post);
	    
	    
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


    }

}

