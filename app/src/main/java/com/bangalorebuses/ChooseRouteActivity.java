package com.bangalorebuses;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.bangalorebuses.Constants.NETWORK_QUERY_NO_ERROR;

/**
 * This is the main activity of the application.
 * This activity allows the user to select a bus route number
 * or search for a bus route number to track it.
 *
 * @author Nihar Thakkar
 * @version 1.0
 * @since 18-6-2017
 */

public class ChooseRouteActivity extends AppCompatActivity implements NetworkingHelper, AdapterView.OnItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{
    private final int LOCATION_PERMISSION_REQUEST_CODE = 36;
    private BusStop nearestBusStops[] = new BusStop[8];
    private int position = 0;
    private EditText routeNumberEditText;
    private Set<String> busesSet;
    private LinearLayout busDetailsLinearLayout;
    private Intent trackBusIntent;
    private ProgressDialog progressDialog;
    private String FILENAME = "buses_at_bus_stop";
    private TextView errorMessageTextView;
    private Spinner nearestStopListSpinner;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates = false;
    private boolean locationIsToBeUpdated = true;
    private LocationManager locationManager;
    private boolean updateBusList = false;
    private ArrayAdapter<String> listAdapter;
    private FloatingActionButton refreshFloatingActionButton;
    private Animation rotatingAnimation;
    private int numberOfRefreshIconRotationsRemaining = 0;
    private ListView routeNumberListView;
    //private AdView adView;
    private boolean routeListIsVisible = false;
    private LinearLayout nearestBusStopSelectionLinearLayout;
    private boolean busesAtStopListHasTraceableBuses = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.hide();
        }
        setContentView(R.layout.splash_screen);
        new CountDownTimer(2000, 2000)
        {
            @Override
            public void onTick(long millisUntilFinished)
            {

            }

            @Override
            public void onFinish()
            {
                setContentView(R.layout.activity_choose_route);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                // Configure the actionbar
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null)
                {
                    actionBar.show();
                    actionBar.setElevation(0);
                    actionBar.setCustomView(R.layout.track_bus_edit_text);
                    actionBar.setDisplayShowTitleEnabled(false);
                    actionBar.setDisplayHomeAsUpEnabled(false);
                    actionBar.setDisplayShowCustomEnabled(true);
                }

                // Start the Google AdMob service
                /*MobileAds.initialize(ChooseRouteActivity.this, "ca-app-pub-4515741125560154~6681035222");
                adView = (AdView) findViewById(R.id.choose_route_activity_footer_ad);
                AdRequest adRequest = new AdRequest.Builder().build();
                adView.loadAd(adRequest);*/

                // Initialize and setup some instance variables
                routeNumberEditText = (EditText) findViewById(R.id.action_bar_edit_text);
                routeNumberEditText.setOnKeyListener(new View.OnKeyListener()
                {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event)
                    {
                        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER))
                        {
                            if (!routeNumberEditText.getText().toString().equals(""))
                            {
                                startTrackingBus(routeNumberEditText.getText().toString());
                            }
                            else
                            {
                                View view = ChooseRouteActivity.this.getCurrentFocus();
                                if (view != null)
                                {
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                }
                                Toast.makeText(ChooseRouteActivity.this, "Please enter a bus number!", Toast.LENGTH_SHORT).show();
                            }
                            return true;
                        }
                        return false;
                    }
                });
                nearestBusStopSelectionLinearLayout = (LinearLayout) findViewById(R.id.nearest_bus_stop_selection_linear_layout);
                refreshFloatingActionButton = (FloatingActionButton) findViewById(R.id.floatingRefreshActionButton);
                rotatingAnimation = AnimationUtils.loadAnimation(ChooseRouteActivity.this, R.anim.rotate);
                locationManager = (LocationManager) ChooseRouteActivity.this.getSystemService(LOCATION_SERVICE);
                busesSet = new HashSet<>();
                busDetailsLinearLayout = (LinearLayout) findViewById(R.id.bus_details_linear_layout);
                errorMessageTextView = (TextView) findViewById(R.id.choose_route_activity_error_message_text_view);
                nearestStopListSpinner = (Spinner) findViewById(R.id.stop_list_spinner);
                errorMessageTextView.setVisibility(View.GONE);
                updateBusList = false;
                locationIsToBeUpdated = true;

                // Connect to the Google api client for location services
                if (mGoogleApiClient == null)
                {
                    mGoogleApiClient = new GoogleApiClient.Builder(ChooseRouteActivity.this).addConnectionCallbacks(ChooseRouteActivity.this).addOnConnectionFailedListener(ChooseRouteActivity.this).addApi(LocationServices.API).build();
                    mGoogleApiClient.connect();
                }

                // Initialise the list of bus routes for search results
                initialiseRouteNumberList();
            }
        }.start();
    }


    /**
     * This method is used to check if the user's device
     * has a Wi-Fi or Cellular data connection.
     *
     * @return boolean This returns true or false based on the status
     * of the Wi-Fi and Cellular data connection.
     */
    private boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    /**
     * This method is used to Handle the route number list that
     * filters bus routes as the user starts typing in the search box.
     */
    private void initialiseRouteNumberList()
    {
        routeNumberListView = (ListView) findViewById(R.id.bus_route_list_view);
        AssetManager assetManager = getAssets();
        InputStream inputStream;
        InputStreamReader inputStreamReader;
        routeNumberListView.setVisibility(View.GONE);
        refreshFloatingActionButton.setVisibility(View.VISIBLE);
        routeListIsVisible = false;
        nearestBusStopSelectionLinearLayout.setVisibility(View.VISIBLE);

        try
        {
            // Reads the bus route list asset
            inputStream = assetManager.open("bangalore_city_bus_routes.txt");
            inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null)
            {
                stringBuilder.append(line);
            }

            // Converts it to a JSON array
            JSONArray jsonArray = new JSONArray(stringBuilder.toString());
            String[] listViewAdapterContent = new String[jsonArray.length()];

            // Puts the bus route names into a list
            for (int i = 0; i < jsonArray.length(); i++)
            {
                listViewAdapterContent[i] = jsonArray.getJSONObject(i).getString("routename");
            }

            listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, listViewAdapterContent);

            routeNumberListView.setAdapter(listAdapter);
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Couldn't load bus number suggestions!", Toast.LENGTH_SHORT).show();
        }

        routeNumberListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                routeNumberListView.setVisibility(View.GONE);
                refreshFloatingActionButton.setVisibility(View.VISIBLE);
                routeListIsVisible = false;
                nearestBusStopSelectionLinearLayout.setVisibility(View.VISIBLE);
                startTrackingBus(parent.getItemAtPosition(position).toString());
            }
        });
        routeNumberEditText.addTextChangedListener(new TextWatcher()
        {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (routeNumberEditText.getText().toString().equals(""))
                {
                    routeNumberListView.setVisibility(View.GONE);
                    refreshFloatingActionButton.setVisibility(View.VISIBLE);
                    routeListIsVisible = false;
                    nearestBusStopSelectionLinearLayout.setVisibility(View.VISIBLE);
                }
                else
                {
                    listAdapter.getFilter().filter(s);
                    routeNumberListView.setVisibility(View.VISIBLE);
                    refreshFloatingActionButton.setVisibility(View.GONE);
                    routeListIsVisible = true;
                    nearestBusStopSelectionLinearLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {
            }
        });
    }


    /**
     * This method is automatically called by the
     * Google Api client when it's able to connect
     * to the device's location service.
     */
    @Override
    public void onConnected(Bundle connectionHint)
    {
        if (locationIsToBeUpdated)
        {
            createLocationRequest();
        }
    }

    /**
     * This method is automatically called by the
     * Google Api client when it's unable to connect
     * to the device's location service.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        errorMessageTextView.setText("Couldn't get current location! Please click the refresh button below to try again.");
        errorMessageTextView.setVisibility(View.VISIBLE);
    }

    /**
     * This method is automatically called by the
     * Google Api client when it's connection to
     * the device's location service has been
     * suspended.
     */
    @Override
    public void onConnectionSuspended(int i)
    {
        if (mRequestingLocationUpdates)
        {
            errorMessageTextView.setText("Couldn't get current location! Please click the refresh button below to try again.");
            errorMessageTextView.setVisibility(View.VISIBLE);
        }
    }


    /**
     * This method creates the location request
     * required to get the user's current location.
     */
    protected void createLocationRequest()
    {
        busDetailsLinearLayout.setVisibility(View.INVISIBLE);
        final int REQUEST_CHECK_SETTINGS = 0x1;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>()
        {
            @Override
            public void onResult(@NonNull LocationSettingsResult result)
            {
                final Status status = result.getStatus();
                switch (status.getStatusCode())
                {
                    case LocationSettingsStatusCodes.SUCCESS:
                        if (!mRequestingLocationUpdates)
                        {
                            int permissionCheck = ContextCompat.checkSelfPermission(ChooseRouteActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED)
                            {
                                progressDialog = ProgressDialog.show(ChooseRouteActivity.this, "Please wait", "Getting your location...", true);
                                startLocationUpdates();
                            }
                            else
                            {
                                ActivityCompat.requestPermissions(ChooseRouteActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                            }
                        }
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                        int permissionCheck = ContextCompat.checkSelfPermission(ChooseRouteActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
                        if (permissionCheck == PackageManager.PERMISSION_GRANTED)
                        {
                            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                            {
                                if (!mRequestingLocationUpdates)
                                {
                                    progressDialog = ProgressDialog.show(ChooseRouteActivity.this, "Please wait", "Getting your location...", true);
                                    startLocationUpdates();
                                }
                            }
                            else
                            {
                                try
                                {
                                    status.startResolutionForResult(ChooseRouteActivity.this, REQUEST_CHECK_SETTINGS);
                                }
                                catch (IntentSender.SendIntentException e)
                                {
                                    errorMessageTextView.setText("Failed to enable location services! Please click the refresh button below to try again.");
                                    errorMessageTextView.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                        else
                        {
                            ActivityCompat.requestPermissions(ChooseRouteActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Toast.makeText(ChooseRouteActivity.this, "Device doesn't have a GPS! Can't get bus stops nearby...", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }

    /**
     * This method starts location updates from the
     * device's location service. Location updates
     * are required even if you want to get the device's
     * current location just once.
     */
    protected void startLocationUpdates()
    {
        try
        {
            if (mGoogleApiClient.isConnected())
            {
                mRequestingLocationUpdates = true;
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        }
        catch (SecurityException e)
        {
            errorMessageTextView.setText("Please grant the location permission in Settings > Apps > Bengaluru Buses. Then, click the refresh button below to try again.");
            errorMessageTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This method stops location updates from the device's
     * location service.
     */
    protected void stopLocationUpdates()
    {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
        {
            mRequestingLocationUpdates = false;
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    /**
     * This method gets called automatically by the device's
     * location service once a location update has been received.
     * <p>
     * NOTE: This method only gets called by the location service
     * if location updates were started.
     */
    @Override
    public void onLocationChanged(Location location)
    {
        mRequestingLocationUpdates = false;
        locationIsToBeUpdated = false;
        if (progressDialog != null)
        {
            progressDialog.dismiss();
        }
        try
        {
            if (isNetworkAvailable())
            {
                progressDialog = ProgressDialog.show(this, "Please wait", "Locating bus stops nearby...", true);
                URL nearestBusStopURL = new URL("http://bmtcmob.hostg.in/api/busstops/stopnearby/lat/" + String.valueOf(location.getLatitude()) + "/lon/" + String.valueOf(location.getLongitude()) + "/rad/1.0");
                new GetNearestBusStopsTask(ChooseRouteActivity.this).execute(nearestBusStopURL);
            }
            else
            {
                errorMessageTextView.setText(R.string.error_connecting_to_the_internet_click_refresh_text);
                errorMessageTextView.setVisibility(View.VISIBLE);
                refreshFloatingActionButton.clearAnimation();
                refreshFloatingActionButton.setEnabled(true);

            }
        }
        catch (MalformedURLException e)
        {
            Toast.makeText(this, "Couldn't find bus stops nearby! Please try again later.", Toast.LENGTH_SHORT).show();
        }
        stopLocationUpdates();
    }

    // What to do after the user has granted or denied permission to use their device's location service
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                createLocationRequest();
            }
            else
            {
                refreshFloatingActionButton.clearAnimation();
                refreshFloatingActionButton.setEnabled(true);
                Toast.makeText(this, "Location access was denied! Couldn't locate bus stops nearby...", Toast.LENGTH_LONG).show();
            }
        }
    }


    // What to do after the user has allowed or denied location services to be turned on
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 1)
        {
            if (resultCode == -1)
            {
                if (!mRequestingLocationUpdates)
                {
                    progressDialog = ProgressDialog.show(this, "Please wait", "Getting your location...", true);
                    startLocationUpdates();

                }
            }
            else if (resultCode == 0)
            {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                {
                    if (!mRequestingLocationUpdates)
                    {
                        progressDialog = ProgressDialog.show(this, "Please wait", "Getting your location...", true);
                        startLocationUpdates();
                    }
                }
                else
                {
                    Toast.makeText(this, "Can't find bus stops nearby without location...", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    // Inflate the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.choose_route_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Handle clicks of the menu buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.choose_route_action_track_entered_bus:
                if (!routeNumberEditText.getText().toString().equals(""))
                {
                    startTrackingBus(routeNumberEditText.getText().toString());
                }
                else
                {
                    View view = this.getCurrentFocus();
                    if (view != null)
                    {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    Toast.makeText(this, "Please enter a bus number!", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }


    // What to do when a bus stop has been selected or not selected from the dropdown of nearest bus stops
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        this.position = position;
        busDetailsLinearLayout.removeAllViews();
        errorMessageTextView.setVisibility(View.GONE);
        TextView busesArrivingAtStopListDescriptionTextView = (TextView) findViewById(R.id.buses_at_stop_list_description);
        busesArrivingAtStopListDescriptionTextView.setText("Buses arriving at " + nearestBusStops[position].getBusStopName());
        if (position == 0 && !updateBusList)
        {
            try
            {
                FileInputStream fileInputStream = openFileInput(FILENAME);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                if ((line = bufferedReader.readLine()) != null)
                {
                    if (line.equals(nearestBusStops[position].getBusStopName()))
                    {
                        while ((line = bufferedReader.readLine()) != null)
                        {
                            stringBuilder.append(line);
                        }
                        if (isNetworkAvailable())
                        {
                            progressDialog = ProgressDialog.show(this, "Please wait", "Getting buses...");
                            JSONArray jsonArray = new JSONArray(stringBuilder.toString());
                            onBusesAtStopFound(Constants.NETWORK_QUERY_NO_ERROR, jsonArray);
                            nearestStopListSpinner.setEnabled(false);
                            refreshFloatingActionButton.setEnabled(false);
                            refreshFloatingActionButton.startAnimation(rotatingAnimation);
                            String requestBody = "stopID=" + Integer.toString(nearestBusStops[position].getBusStopId());
                            busesSet.clear();
                            new GetBusesArrivingAtStopTask(this).execute(requestBody);
                        }
                        else
                        {
                            errorMessageTextView.setText(R.string.error_connecting_to_the_internet_text);
                            errorMessageTextView.setVisibility(View.VISIBLE);
                        }
                    }
                    else
                    {
                        if (isNetworkAvailable())
                        {
                            progressDialog = ProgressDialog.show(this, "Please wait", "Getting buses...");
                            String requestBody = "stopID=" + Integer.toString(nearestBusStops[position].getBusStopId());
                            busesSet.clear();
                            new GetBusesArrivingAtStopTask(this).execute(requestBody);
                        }
                        else
                        {
                            errorMessageTextView.setText(R.string.error_connecting_to_the_internet_text);
                            errorMessageTextView.setVisibility(View.VISIBLE);
                        }
                    }
                }
                else
                {
                    Toast.makeText(this, "Unknown error occurred! Please try again later...", Toast.LENGTH_SHORT).show();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
                if (isNetworkAvailable())
                {
                    progressDialog = ProgressDialog.show(this, "Please wait", "Getting buses...");
                    String requestBody = "stopID=" + Integer.toString(nearestBusStops[position].getBusStopId());
                    busesSet.clear();
                    new GetBusesArrivingAtStopTask(this).execute(requestBody);
                }
                else
                {
                    errorMessageTextView.setText(R.string.error_connecting_to_the_internet_text);
                    errorMessageTextView.setVisibility(View.VISIBLE);
                }
            }
            catch (JSONException e)
            {
                progressDialog.dismiss();
                Toast.makeText(this, "Unknown error occurred! Please try again later...", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            if (isNetworkAvailable())
            {
                progressDialog = ProgressDialog.show(this, "Please wait", "Getting buses...");
                String requestBody = "stopID=" + Integer.toString(nearestBusStops[position].getBusStopId());
                busesSet.clear();
                new GetBusesArrivingAtStopTask(this).execute(requestBody);
                updateBusList = false;
            }
            else
            {
                errorMessageTextView.setText(R.string.error_connecting_to_the_internet_text);
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
        Toast.makeText(this, "Please select a bus stop!", Toast.LENGTH_LONG).show();
    }


    // Track a bus
    public void startTrackingBus(String busNumberToTrack)
    {
        View view = this.getCurrentFocus();
        if (view != null)
        {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        if (isNetworkAvailable())
        {
            progressDialog = ProgressDialog.show(this, "Please wait", "Getting bus details...");
            Route route = new Route();
            route.setRouteNumber(busNumberToTrack);
            new GetBusRouteDetailsTask(this, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, route);
        }
        else
        {
            errorMessageTextView.setText(R.string.error_connecting_to_the_internet_text);
            errorMessageTextView.setVisibility(View.VISIBLE);
        }
    }

    // What to do when the floating refresh button has been clicked
    public void refresh(View view)
    {
        refreshFloatingActionButton.startAnimation(rotatingAnimation);
        refreshFloatingActionButton.setEnabled(false);
        errorMessageTextView.setVisibility(View.GONE);
        updateBusList = true;
        locationIsToBeUpdated = true;
        createLocationRequest();
    }

    // What to do once bus stops nearby have been found
    @Override
    public void onBusStopsFound(boolean isError, JSONArray busStopsArray)
    {
        progressDialog.dismiss();
        ArrayList<String> stopList = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, stopList);
        busDetailsLinearLayout.removeAllViews();

        if (!isError)
        {
            if (busStopsArray == null || busStopsArray.length() == 0)
            {
                progressDialog.dismiss();
                errorMessageTextView.setText(R.string.error_no_bus_stops_found_text);
                errorMessageTextView.setVisibility(View.VISIBLE);
                refreshFloatingActionButton.clearAnimation();
                refreshFloatingActionButton.setEnabled(true);
                return;
            }

            nearestBusStops[0] = new BusStop();
            nearestBusStops[1] = new BusStop();
            nearestBusStops[2] = new BusStop();
            nearestBusStops[3] = new BusStop();
            nearestBusStops[4] = new BusStop();
            nearestBusStops[5] = new BusStop();
            nearestBusStops[6] = new BusStop();
            nearestBusStops[7] = new BusStop();
            int i = 0;

            try
            {
                for (int h = 0; h < 8; h++)
                {
                    for (; i < busStopsArray.length(); i++)
                    {
                        String busStopName = busStopsArray.getJSONObject(i).getString("StopName");
                        if (!(busStopName.contains("CS-")))
                        {
                            if (busStopName.contains("(") && busStopName.charAt(busStopName.indexOf("(") - 1) == ' ')
                            {
                                busStopName = busStopName.replace(" (", "(");
                            }
                            if (busStopName.contains("Towards"))
                            {
                                busStopName = busStopName.replace("Towards", "-->");
                            }
                            else if (busStopName.contains("towards -"))
                            {
                                busStopName = busStopName.replace("towards -", "-->");
                            }
                            nearestBusStops[h].setBusStopName(busStopName.substring(0, busStopName.indexOf(")") + 1));
                            nearestBusStops[h].setLatitude(busStopsArray.getJSONObject(i).getString("StopLat"));
                            nearestBusStops[h].setLongitude(busStopsArray.getJSONObject(i).getString("StopLong"));
                            nearestBusStops[h].setBusStopId(busStopsArray.getJSONObject(i).getInt("StopId"));
                            stopList.add(nearestBusStops[h].getBusStopName());
                            i++;
                            break;
                        }
                    }
                }
                nearestStopListSpinner.setAdapter(adapter);
                nearestStopListSpinner.setOnItemSelectedListener(this);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
                progressDialog.dismiss();
                errorMessageTextView.setText(R.string.error_no_bus_stops_found_text);
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            progressDialog.dismiss();
            refreshFloatingActionButton.clearAnimation();
            refreshFloatingActionButton.setEnabled(true);
            errorMessageTextView.setText(R.string.error_connecting_to_the_internet_click_refresh_text);
            errorMessageTextView.setVisibility(View.VISIBLE);
        }
    }

    // What to do once buses arriving at the selected stop have been found
    @Override
    public void onBusesAtStopFound(String errorMessage, JSONArray buses)
    {
        if (errorMessage.equals(Constants.NETWORK_QUERY_NO_ERROR))
        {
            busesAtStopListHasTraceableBuses = false;
            busDetailsLinearLayout.removeAllViews();
            busesSet.clear();
            try
            {
                if (position == 0)
                {
                    try
                    {
                        deleteFile(FILENAME);
                        FileOutputStream fileOutputStream = openFileOutput(FILENAME, MODE_PRIVATE);
                        fileOutputStream.write((nearestBusStops[position].getBusStopName() + "\n").getBytes());
                        fileOutputStream.write(buses.toString().getBytes());
                        fileOutputStream.close();
                        nearestStopListSpinner.setEnabled(true);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                for (int i = 0; i < buses.length(); i++)
                {
                    busesSet.add(buses.getJSONArray(i).get(3).toString().substring(buses.getJSONArray(i).get(3).toString().indexOf(":") + 1, buses.getJSONArray(i).get(3).toString().length()));
                }
                if (isNetworkAvailable())
                {
                    numberOfRefreshIconRotationsRemaining = 0;
                    for (String bus : busesSet)
                    {
                        numberOfRefreshIconRotationsRemaining++;
                        Route route = new Route();
                        route.setRouteNumber(bus);
                        new GetBusRouteDetailsTask(this, true).execute(route);
                    }
                }
                else
                {
                    errorMessageTextView.setText(R.string.error_connecting_to_the_internet_click_refresh_text);
                    errorMessageTextView.setVisibility(View.VISIBLE);
                }
            }
            catch (JSONException e)
            {
                progressDialog.dismiss();
                errorMessageTextView.setText("Could not get buses arriving at selected bus stop! Please click the refresh button below to try again.");
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            progressDialog.dismiss();
            refreshFloatingActionButton.clearAnimation();
            refreshFloatingActionButton.setEnabled(true);
            nearestStopListSpinner.setEnabled(true);
            if (isNetworkAvailable())
            {
                errorMessageTextView.setText(R.string.error_getting_buses_at_stop);
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
            else
            {
                errorMessageTextView.setText(R.string.error_connecting_to_the_internet_click_refresh_text);
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    // What to do once bus route details have been found
    @Override
    public void onBusRouteDetailsFound(String errorMessage, final Route route, boolean isForList, final String routeDirection)
    {
        progressDialog.dismiss();
        if (errorMessage.equals(NETWORK_QUERY_NO_ERROR))
        {
            if (isForList)
            {
                busesAtStopListHasTraceableBuses = true;
                LinearLayout busDetailsRowLinearLayout = new LinearLayout(this);
                busDetailsRowLinearLayout.removeAllViews();
                busDetailsRowLinearLayout.setMinimumHeight(100);
                busDetailsRowLinearLayout.setBackgroundColor(Color.WHITE);
                TextView routeNumberTextView = new TextView(this);
                TextView routeDirectionTextView = new TextView(this);
                View separatorView = new View(this);
                ImageView imageView = new ImageView(this);

                imageView.setPadding(10, 20, 0, 0);
                imageView.setAdjustViewBounds(true);
                imageView.setMinimumHeight(80);
                imageView.setMinimumWidth(80);
                imageView.setMaxHeight(80);
                imageView.setMaxWidth(80);

                if (route.getRouteNumber().length() > 5 && route.getRouteNumber().substring(0, 5).equals("KIAS-"))
                {
                    imageView.setImageResource(R.drawable.ic_flight_black);
                }
                else if (route.getRouteNumber().length() > 2 && route.getRouteNumber().substring(0, 2).equals("V-"))
                {
                    imageView.setImageResource(R.drawable.ic_directions_bus_ac);
                }
                else if (route.getRouteNumber().contains("CHAKRA-") || route.getRouteNumber().contains("MF-"))
                {
                    imageView.setImageResource(R.drawable.ic_directions_bus_special);
                }
                else
                {
                    imageView.setImageResource(R.drawable.ic_directions_bus_ordinary);
                }
                routeNumberTextView.setText(route.getRouteNumber());
                routeNumberTextView.setTextColor(Color.BLACK);
                routeNumberTextView.setPadding(20, 20, 20, 20);
                routeNumberTextView.setMinLines(3);
                routeNumberTextView.setMinEms(5);
                routeNumberTextView.setMaxWidth(30);
                routeNumberTextView.setTextSize(16);

                if (routeDirection.equals("UP"))
                {
                    routeDirectionTextView.setText(route.getUpRouteName());
                }
                else
                {
                    routeDirectionTextView.setText(route.getDownRouteName());
                }
                routeDirectionTextView.setPadding(20, 20, 20, 20);
                routeDirectionTextView.setMinLines(3);
                routeDirectionTextView.setTextSize(14);

                separatorView.setMinimumHeight(1);
                separatorView.setBackgroundColor(Color.BLACK);

                busDetailsRowLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                busDetailsRowLinearLayout.setClickable(true);
                busDetailsRowLinearLayout.addView(imageView);
                busDetailsRowLinearLayout.addView(routeNumberTextView);
                busDetailsRowLinearLayout.addView(routeDirectionTextView);
                busDetailsLinearLayout.addView(busDetailsRowLinearLayout);
                busDetailsLinearLayout.addView(separatorView);
                busDetailsLinearLayout.setVisibility(View.VISIBLE);
                busDetailsRowLinearLayout.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        trackBusIntent = new Intent(ChooseRouteActivity.this, TrackBusActivity.class);
                        if (nearestBusStops[position] != null)
                        {
                            trackBusIntent.putExtra("STOP_NAME", nearestBusStops[position].getBusStopName());
                            trackBusIntent.putExtra("STOP_LAT", nearestBusStops[position].getLatitude());
                            trackBusIntent.putExtra("STOP_LONG", nearestBusStops[position].getLongitude());
                        }
                        trackBusIntent.putExtra("ROUTE_NUMBER", route.getRouteNumber());
                        trackBusIntent.putExtra("ROUTE_DIRECTION", routeDirection);
                        trackBusIntent.putExtra("UP_ROUTE_ID", route.getUpRouteId());
                        trackBusIntent.putExtra("UP_ROUTE_NAME", route.getUpRouteName());
                        trackBusIntent.putExtra("DOWN_ROUTE_ID", route.getDownRouteId());
                        trackBusIntent.putExtra("DOWN_ROUTE_NAME", route.getDownRouteName());
                        startActivity(trackBusIntent);
                    }
                });
            }
            else
            {
                routeNumberEditText.setText("");
                trackBusIntent = new Intent(ChooseRouteActivity.this, TrackBusActivity.class);
                if (nearestBusStops[position] != null)
                {
                    trackBusIntent.putExtra("STOP_NAME", nearestBusStops[position].getBusStopName());
                    trackBusIntent.putExtra("STOP_LAT", nearestBusStops[position].getLatitude());
                    trackBusIntent.putExtra("STOP_LONG", nearestBusStops[position].getLongitude());
                }
                trackBusIntent.putExtra("ROUTE_NUMBER", route.getRouteNumber());
                trackBusIntent.putExtra("ROUTE_DIRECTION", "UP");
                trackBusIntent.putExtra("UP_ROUTE_ID", route.getUpRouteId());
                trackBusIntent.putExtra("UP_ROUTE_NAME", route.getUpRouteName());
                trackBusIntent.putExtra("DOWN_ROUTE_ID", route.getDownRouteId());
                trackBusIntent.putExtra("DOWN_ROUTE_NAME", route.getDownRouteName());
                startActivity(trackBusIntent);
            }
        }
        else
        {
            if (isNetworkAvailable())
            {
                if (!isForList)
                {
                    Toast.makeText(this, "This bus cannot be tracked.", Toast.LENGTH_SHORT).show();
                    routeNumberListView.setVisibility(View.VISIBLE);
                    routeListIsVisible = true;
                    refreshFloatingActionButton.setVisibility(View.GONE);
                }
            }
            else
            {
                errorMessageTextView.setText(R.string.error_connecting_to_the_internet_click_refresh_text);
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
        if (numberOfRefreshIconRotationsRemaining != 1)
        {
            numberOfRefreshIconRotationsRemaining--;
        }
        else
        {
            if (nearestStopListSpinner.isEnabled())
            {
                refreshFloatingActionButton.clearAnimation();
                refreshFloatingActionButton.setEnabled(true);
                if (!busesAtStopListHasTraceableBuses)
                {
                    errorMessageTextView.setText("Cannot get buses arriving at this bus stop! Please select another bus stop and try again.");
                    errorMessageTextView.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void onStopsOnBusRouteFound(String errorMessage, BusStop[] busStops, Route route)
    {

    }

    @Override
    public void onBusesEnRouteFound(String errorMessage, Bus[] buses, int numberOfBusesFound, Route route, BusStop selectedBusStop)
    {

    }

    @Override
    public void onTimeToBusesFound(boolean isError, Bus[] buses)
    {

    }


    protected void onStart()
    {
        updateBusList = false;
        if (mGoogleApiClient != null)
        {
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    protected void onStop()
    {
        if (mGoogleApiClient != null)
        {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (progressDialog != null && mRequestingLocationUpdates)
        {
            progressDialog.dismiss();
        }
        /*if (adView != null)
        {
            adView.pause();
        }*/
        stopLocationUpdates();
        mRequestingLocationUpdates = false;
    }

    @Override
    public void onResume()
    {
        /*if (adView != null)
        {
            adView.resume();
        }*/
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && locationIsToBeUpdated)
        {
            int permissionCheck = ContextCompat.checkSelfPermission(ChooseRouteActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED)
            {
                createLocationRequest();
            }
        }
        super.onResume();
    }

    @Override
    public void onDestroy()
    {
        /*if (adView != null)
        {
            adView.destroy();
        }*/
        super.onDestroy();
    }


    // What to do when the Android back button is pressed
    @Override
    public void onBackPressed()
    {
        // If the list of bus routes for the search feature is visible, hide the list instead of exiting the app
        if (routeListIsVisible)
        {
            routeNumberListView.setVisibility(View.GONE);
            refreshFloatingActionButton.setVisibility(View.VISIBLE);
            routeListIsVisible = false;
            nearestBusStopSelectionLinearLayout.setVisibility(View.VISIBLE);
            routeNumberEditText.setText("");
        }
        else
        {
            super.onBackPressed(); // Else, exit the app as usual
        }
    }
}
