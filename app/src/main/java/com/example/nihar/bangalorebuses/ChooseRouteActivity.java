package com.example.nihar.bangalorebuses;

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
import java.util.Iterator;
import java.util.Set;

public class ChooseRouteActivity extends AppCompatActivity implements NetworkingCallback, AdapterView.OnItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{
    private final int LOCATION_PERMISSION_REQUEST_CODE = 36;
    private BusStop nearestBusStops[] = new BusStop[4];
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
    private int numberOfBusesArrivingAtNearestStop = 0;
    private ListView routeNumberListView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setElevation(0);
        actionBar.setCustomView(R.layout.track_bus_edit_text);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        setContentView(R.layout.activity_choose_route);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        rotatingAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        routeNumberEditText = (EditText) findViewById(R.id.action_bar_edit_text);
        routeNumberEditText.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER))
                {
                    trackBus(routeNumberEditText.getText().toString());
                    return true;
                }
                return false;
            }
        });
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        busesSet = new HashSet<>();
        busDetailsLinearLayout = (LinearLayout) findViewById(R.id.bus_details_linear_layout);
        errorMessageTextView = (TextView) findViewById(R.id.choose_route_activity_error_message_text_view);
        errorMessageTextView.setVisibility(View.GONE);
        nearestStopListSpinner = (Spinner) findViewById(R.id.stop_list_spinner);
        updateBusList = false;
        locationIsToBeUpdated = true;
        if (mGoogleApiClient == null)
        {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        }
        initialiseRouteNumberList();
        refreshFloatingActionButton = (FloatingActionButton) findViewById(R.id.floatingRefreshActionButton);
        refreshFloatingActionButton.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                hardRefresh(v);
                return true;
            }
        });
    }

    private void initialiseRouteNumberList()
    {
        routeNumberListView = (ListView) findViewById(R.id.bus_route_list_view);
        AssetManager assetManager = getAssets();
        InputStream inputStream;
        InputStreamReader inputStreamReader;
        routeNumberListView.setVisibility(View.GONE);

        try
        {
            inputStream = assetManager.open("bangalore_city_bus_routes.txt");
            inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null)
            {
                stringBuilder.append(line);
            }

            JSONArray jsonArray = new JSONArray(stringBuilder.toString());
            String[] listViewAdapterContent = new String[jsonArray.length()];

            for (int i = 0; i < jsonArray.length(); i++)
            {
                listViewAdapterContent[i] = jsonArray.getJSONObject(i).getString("routename");
            }

            listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, listViewAdapterContent);

            routeNumberListView.setAdapter(listAdapter);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        routeNumberListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                trackBus(parent.getItemAtPosition(position).toString());
                routeNumberListView.setVisibility(View.GONE);
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
                }
                else
                {
                    listAdapter.getFilter().filter(s);
                    routeNumberListView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {
            }
        });
    }

    @Override
    public void onConnected(Bundle connectionHint)
    {
        if (locationIsToBeUpdated)
        {
            createLocationRequest();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        Toast.makeText(this, "Couldn't get current location! Please try again later...", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                createLocationRequest();
            }
            else
            {
                Toast.makeText(this, "Location access was denied! Couldn't locate bus stops nearby...", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        mRequestingLocationUpdates = false;
        try
        {
            if (isNetworkAvailable())
            {
                URL nearestBusStopURL = new URL("http://bmtcmob.hostg.in/api/busstops/stopnearby/lat/" + String.valueOf(location.getLatitude()) + "/lon/" + String.valueOf(location.getLongitude()) + "/rad/0.6");
                new GetNearestBusStopsTask(this, this).execute(nearestBusStopURL);
            }
            else
            {
                errorMessageTextView.setText(R.string.error_connecting_to_the_internet_text);
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
            Toast.makeText(this, "Error generating URL! Error code: BB-CRA-URL-01", Toast.LENGTH_LONG).show();
        }
        stopLocationUpdates();
        progressDialog.hide();

    }

    protected void startLocationUpdates()
    {
        try
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        catch (SecurityException e)
        {
            Toast.makeText(this, "Please grant the location permission in app info...", Toast.LENGTH_LONG).show();
        }
    }

    protected void createLocationRequest()
    {
        busDetailsLinearLayout.setVisibility(View.INVISIBLE);
        final int REQUEST_CHECK_SETTINGS = 0x1;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>()
        {
            @Override
            public void onResult(LocationSettingsResult result)
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
                                mRequestingLocationUpdates = true;
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
                                    mRequestingLocationUpdates = true;

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
                                    // Ignore the error.
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
                    mRequestingLocationUpdates = true;

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
                        mRequestingLocationUpdates = true;
                    }
                }
                else
                {
                    Toast.makeText(this, "Can't find bus stops nearby without location...", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.choose_route_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.choose_route_action_track_entered_bus:
                if (!routeNumberEditText.getText().toString().equals(""))
                {
                    trackBus(routeNumberEditText.getText().toString());
                }
                else
                {
                    Toast.makeText(this, "Please enter a bus number!", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onBusStopsFound(boolean isError, JSONArray busStopsArray)
    {
        ArrayList<String> stopList = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, stopList);
        busDetailsLinearLayout.removeAllViews();

        if (!isError)
        {
            if (busStopsArray == null || busStopsArray.length() == 0)
            {
                progressDialog.hide();
                errorMessageTextView.setText(R.string.error_no_bus_stops_found_text);
                errorMessageTextView.setVisibility(View.VISIBLE);
                return;
            }

            nearestBusStops[0] = new BusStop();
            nearestBusStops[1] = new BusStop();
            nearestBusStops[2] = new BusStop();
            nearestBusStops[3] = new BusStop();
            int i = 0;

            try
            {
                for (int h = 0; h < 4; h++)
                {
                    for (; i < busStopsArray.length(); i++)
                    {
                        String busStopName = busStopsArray.getJSONObject(i).getString("StopName");
                        if (!(busStopName.contains("CS-")))
                        {
                            if (busStopName.substring(busStopName.indexOf("(") - 1, busStopName.indexOf("(")).equals(" "))
                            {
                                busStopName = busStopsArray.getJSONObject(i).getString("StopName").replace(" (", "(");
                            }
                            if (!stopList.contains(busStopName.substring(0, busStopName.indexOf("("))))
                            {
                                nearestBusStops[h].setBusStopName(busStopName.substring(0, busStopName.indexOf("(")));
                                nearestBusStops[h].setLatitude(busStopsArray.getJSONObject(i).getString("StopLat"));
                                nearestBusStops[h].setLongitude(busStopsArray.getJSONObject(i).getString("StopLong"));
                                nearestBusStops[h].setBusStopId(busStopsArray.getJSONObject(i).getInt("StopId"));
                                stopList.add(nearestBusStops[h].getBusStopName());
                                i++;
                                break;
                            }
                        }
                        else
                        {
                            i++;
                        }
                    }
                }
                nearestStopListSpinner.setAdapter(adapter);
                nearestStopListSpinner.setOnItemSelectedListener(this);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
                progressDialog.hide();
                errorMessageTextView.setText(R.string.error_no_bus_stops_found_text);
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            progressDialog.hide();
            errorMessageTextView.setText(R.string.error_connecting_to_the_internet_text);
            errorMessageTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBusesAtStopFound(boolean isError, JSONArray buses)
    {
        if (!isError)
        {
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
                    busesSet.add(buses.getJSONArray(i).get(3).toString().substring(buses.getJSONArray(i).get(3).toString().indexOf(":") + 1, buses.getJSONArray(i).get(3).toString().length()).replace("DN", "").replace("UP", ""));
                }
                if (isNetworkAvailable())
                {
                    numberOfBusesArrivingAtNearestStop = 0;
                    Iterator<String> busesSetIterator = busesSet.iterator();
                    while (busesSetIterator.hasNext())
                    {
                        numberOfBusesArrivingAtNearestStop++;
                        new GetBusRouteDetailsTask(this, this, false, true).execute(busesSetIterator.next());
                    }
                }
                else
                {
                    errorMessageTextView.setText(R.string.error_connecting_to_the_internet_text);
                    errorMessageTextView.setVisibility(View.VISIBLE);
                }
            }
            catch (JSONException e)
            {
                progressDialog.hide();
                e.printStackTrace();
            }
        }
        else
        {
            progressDialog.hide();
            errorMessageTextView.setText(R.string.error_could_not_get_buses_at_stop_text);
            errorMessageTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        this.position = position;
        busDetailsLinearLayout.removeAllViews();
        errorMessageTextView.setVisibility(View.GONE);
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
                            onBusesAtStopFound(false, jsonArray);
                            nearestStopListSpinner.setEnabled(false);
                            String requestBody = "stopID=" + Integer.toString(nearestBusStops[position].getBusStopId());
                            busesSet.clear();
                            new GetBusesAtStopTask(this, this).execute(requestBody);
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
                            new GetBusesAtStopTask(this, this).execute(requestBody);
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
                    Toast.makeText(this, "Unknown error occurred! Please try again later...", Toast.LENGTH_LONG).show();
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
                    new GetBusesAtStopTask(this, this).execute(requestBody);
                }
                else
                {
                    errorMessageTextView.setText(R.string.error_connecting_to_the_internet_text);
                    errorMessageTextView.setVisibility(View.VISIBLE);
                }
            }
            catch (JSONException e)
            {
                progressDialog.hide();
                e.printStackTrace();
            }
        }
        else
        {
            if (isNetworkAvailable())
            {
                progressDialog = ProgressDialog.show(this, "Please wait", "Getting buses...");
                String requestBody = "stopID=" + Integer.toString(nearestBusStops[position].getBusStopId());
                busesSet.clear();
                new GetBusesAtStopTask(this, this).execute(requestBody);
                updateBusList = false;
            }
            else
            {
                errorMessageTextView.setText(R.string.error_connecting_to_the_internet_text);
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void softRefresh(View view)
    {
        if (isNetworkAvailable())
        {
            refreshFloatingActionButton.setEnabled(false);
            refreshFloatingActionButton.startAnimation(rotatingAnimation);
            String requestBody = "stopID=" + Integer.toString(nearestBusStops[position].getBusStopId());
            busesSet.clear();
            new GetBusesAtStopTask(this, this).execute(requestBody);
        }
        else
        {
            errorMessageTextView.setText(R.string.error_connecting_to_the_internet_text);
            errorMessageTextView.setVisibility(View.VISIBLE);
        }
    }

    public void hardRefresh(View view)
    {
        createLocationRequest();
    }

    public void trackBus(String busNumberToTrack)
    {
        if (isNetworkAvailable())
        {
            progressDialog = ProgressDialog.show(this, "Please wait", "Getting bus details...");
            new GetBusRouteDetailsTask(this, this, false, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, busNumberToTrack);
        }
        else
        {
            errorMessageTextView.setText(R.string.error_connecting_to_the_internet_text);
            errorMessageTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
        Toast.makeText(this, "Please choose a bus stop!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBusRouteDetailsFound(boolean isError, final Route route, boolean isForList)
    {
        progressDialog.hide();
        if (numberOfBusesArrivingAtNearestStop != 1)
        {
            numberOfBusesArrivingAtNearestStop--;
        }
        else
        {
            refreshFloatingActionButton.clearAnimation();
            refreshFloatingActionButton.setEnabled(true);
        }
        if (!isError)
        {
            if (isForList)
            {
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

                if (route.getRouteNumber().contains("KIAS-"))
                {
                    imageView.setImageResource(R.drawable.ic_flight_black);
                }
                else if (route.getRouteNumber().contains("V-"))
                {
                    imageView.setImageResource(R.drawable.ic_directions_bus_ac);
                }
                else if (route.getRouteNumber().contains("CHAKRA-") || route.getRouteNumber().contains("MF"))
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

                routeDirectionTextView.setText(route.getUpRouteName());
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
                trackBusIntent.putExtra("UP_ROUTE_ID", route.getUpRouteId());
                trackBusIntent.putExtra("UP_ROUTE_NAME", route.getUpRouteName());
                trackBusIntent.putExtra("DOWN_ROUTE_ID", route.getDownRouteId());
                trackBusIntent.putExtra("DOWN_ROUTE_NAME", route.getDownRouteName());
                startActivity(trackBusIntent);
            }
        }
        else
        {
            if (!isForList)
            {
                Toast.makeText(this, "This bus cannot be tracked.", Toast.LENGTH_SHORT).show();
                routeNumberListView.setVisibility(View.VISIBLE);

            }
        }
    }

    @Override
    public void onStopsOnBusRouteFound(boolean isError, final JSONArray stopListArray)
    {

    }

    @Override
    public void onBusesEnRouteFound(boolean isError, Bus[] buses, int numberOfBusesFound)
    {

    }

    @Override
    public void onTimeToBusesFound(boolean isError, Bus[] buses)
    {

    }

    protected void onStart()
    {
        updateBusList = false;
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop()
    {
        locationIsToBeUpdated = false;
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        stopLocationUpdates();
        mRequestingLocationUpdates = false;
        locationIsToBeUpdated = false;
    }

    protected void stopLocationUpdates()
    {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }
}
