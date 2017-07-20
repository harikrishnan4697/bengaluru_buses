package com.bangalorebuses;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LOCATION_SERVICE;
import static com.bangalorebuses.Constants.LOCATION_PERMISSION_REQUEST_CODE;

public class NearbyFragment extends Fragment implements NetworkingManager, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{
    private ListView busStopsNearbyListView;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private boolean isRequestingLocationUpdates = false;
    private TextView errorMessageTextView;
    private boolean locationIsToBeUpdated = false;
    private LocationManager locationManager;
    //private FloatingActionButton refreshFloatingActionButton;
    private Animation rotatingAnimation;
    private ProgressBar progressBar;
    private NearbyBusStopsListCustomAdapter busStopsNearbyListAdaptor;
    private ArrayList<BusStop> busStops = new ArrayList<>();
    private GetNearestBusStopsTask getNearestBusStopsTask;
    private boolean wasLocatingBusStopsNearby = false;
    private CountDownTimer countDownTimer;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.nearby_fragment, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.nearby_fragment_progress_bar);
        progressBar.setVisibility(View.GONE);
        countDownTimer = null;
        /*refreshFloatingActionButton = (FloatingActionButton) view.findViewById(R.id.floatingRefreshActionButton);
        refreshFloatingActionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                refresh();
            }
        });*/
        rotatingAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
        locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);

        busStopsNearbyListView = (ListView) view.findViewById(R.id.busStopsNearbyListView);
        if (busStopsNearbyListView != null && busStopsNearbyListAdaptor != null)
        {
            busStopsNearbyListView.setAdapter(busStopsNearbyListAdaptor);
            busStopsNearbyListView.setVisibility(View.VISIBLE);
            busStopsNearbyListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    Intent getBusesArrivingAtBusStopIntent = new Intent(getContext(), BusesArrivingAtBusStopActivity.class);
                    getBusesArrivingAtBusStopIntent.putExtra("BUS_STOP_NAME", busStops.get(position).getBusStopName());
                    getBusesArrivingAtBusStopIntent.putExtra("BUS_STOP_ID", busStops.get(position).getBusStopId());
                    startActivity(getBusesArrivingAtBusStopIntent);
                }
            });
        }

        errorMessageTextView = (TextView) view.findViewById(R.id.nearby_fragment_error_message_text_view);
        errorMessageTextView.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        // Connect to the Google api client for location services
        if (googleApiClient == null)
        {
            /*refreshFloatingActionButton.setAnimation(rotatingAnimation);
            refreshFloatingActionButton.setEnabled(false);*/
            progressBar.setVisibility(View.VISIBLE);
            locationIsToBeUpdated = true;
            googleApiClient = new GoogleApiClient.Builder(getContext()).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
            googleApiClient.connect();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.nearby_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // Handle clicks of the menu buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            /*case R.id.nearby_search:
                Intent searchActivityIntent = new Intent(getContext(), SearchActivity.class);
                searchActivityIntent.putExtra("Search_Type", Constants.SEARCH_TYPE_BUS_STOP);
                startActivityForResult(searchActivityIntent, Constants.SEARCH_NEARBY_BUS_STOP_REQUEST_CODE);
                break;*/
            default:
                return super.onOptionsItemSelected(item);
        }
        //return true;
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
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
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
        errorMessageTextView.setText("Couldn't get current location! Please try again later.");
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
        if (isRequestingLocationUpdates)
        {
            errorMessageTextView.setText("Couldn't get current location! Please try again later.");
            errorMessageTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This method creates the location request
     * required to get the user's current location.
     */
    protected void createLocationRequest()
    {
        final int REQUEST_CHECK_SETTINGS = 0x1;
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>()
        {
            @Override
            public void onResult(@NonNull LocationSettingsResult result)
            {
                final Status status = result.getStatus();
                switch (status.getStatusCode())
                {
                    case LocationSettingsStatusCodes.SUCCESS:
                        if (!isRequestingLocationUpdates)
                        {
                            int permissionCheck = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED)
                            {
                                startLocationUpdates();
                            }
                            else
                            {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                            }
                        }
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                        int permissionCheck = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
                        if (permissionCheck == PackageManager.PERMISSION_GRANTED)
                        {
                            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                            {
                                if (!isRequestingLocationUpdates)
                                {
                                    progressBar.setVisibility(View.VISIBLE);
                                    startLocationUpdates();
                                }
                            }
                            else
                            {
                                try
                                {
                                    status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                                }
                                catch (IntentSender.SendIntentException e)
                                {
                                    errorMessageTextView.setText("Failed to enable location services!");
                                    errorMessageTextView.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                        else
                        {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Toast.makeText(getContext(), "Device doesn't have a GPS! Can't get bus stops nearby...", Toast.LENGTH_LONG).show();
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
            if (googleApiClient.isConnected())
            {
                isRequestingLocationUpdates = true;
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            }
        }
        catch (SecurityException e)
        {
            errorMessageTextView.setText("Please grant the location permission in Settings > Apps > Bengaluru Buses.");
            errorMessageTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This method stops location updates from the device's
     * location service.
     */
    protected void stopLocationUpdates()
    {
        if (googleApiClient != null && googleApiClient.isConnected())
        {
            isRequestingLocationUpdates = false;
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
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
        isRequestingLocationUpdates = false;
        locationIsToBeUpdated = false;
        try
        {
            if (isNetworkAvailable())
            {
                wasLocatingBusStopsNearby = true;
                URL nearestBusStopURL = new URL("http://bmtcmob.hostg.in/api/busstops/stopnearby/lat/" + String.valueOf(location.getLatitude()) + "/lon/" + String.valueOf(location.getLongitude()) + "/rad/1.0");
                getNearestBusStopsTask = new GetNearestBusStopsTask(this);
                getNearestBusStopsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, nearestBusStopURL);
            }
            else
            {
                errorMessageTextView.setText(R.string.error_connecting_to_the_internet_click_refresh_text);
                errorMessageTextView.setVisibility(View.VISIBLE);
                /*refreshFloatingActionButton.clearAnimation();
                refreshFloatingActionButton.setEnabled(true);*/

            }
        }
        catch (MalformedURLException e)
        {
            Toast.makeText(getContext(), "Couldn't find bus stops nearby! Please try again later.", Toast.LENGTH_SHORT).show();
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
                /*refreshFloatingActionButton.clearAnimation();
                refreshFloatingActionButton.setEnabled(true);*/
                Toast.makeText(getContext(), "Location access was denied! Couldn't locate bus stops nearby...", Toast.LENGTH_LONG).show();
            }
        }
    }

    // What to do after the user has allowed or denied location services to be turned on
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == Constants.SEARCH_NEARBY_BUS_STOP_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                Intent getBusesArrivingAtBusStopIntent = new Intent(getContext(), BusesArrivingAtBusStopActivity.class);
                getBusesArrivingAtBusStopIntent.putExtra("BUS_STOP_NAME", data.getStringExtra("BUS_STOP_NAME"));
                getBusesArrivingAtBusStopIntent.putExtra("BUS_STOP_ID", data.getStringExtra("BUS_STOP_ID"));
                startActivity(getBusesArrivingAtBusStopIntent);
            }
        }
        else if (requestCode == 1)
        {
            if (resultCode == RESULT_OK)
            {
                if (!isRequestingLocationUpdates)
                {
                    progressBar.setVisibility(View.VISIBLE);
                    startLocationUpdates();

                }
            }
            else if (resultCode == 0)
            {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                {
                    if (!isRequestingLocationUpdates)
                    {
                        progressBar.setVisibility(View.VISIBLE);
                        startLocationUpdates();
                    }
                }
                else
                {
                    Toast.makeText(getContext(), "Can't find bus stops nearby without location...", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    // What to do when the floating refresh button has been clicked
    public void refresh()
    {
        if(isNetworkAvailable())
        {
            locationIsToBeUpdated = true;
            errorMessageTextView.setVisibility(View.GONE);
        /*refreshFloatingActionButton.startAnimation(rotatingAnimation);
        refreshFloatingActionButton.setEnabled(false);*/
            createLocationRequest();
        }
        else
        {
            countDownTimer = new CountDownTimer(45000, 45000)
            {
                @Override
                public void onTick(long millisUntilFinished)
                {

                }

                @Override
                public void onFinish()
                {
                    refresh();
                }
            }.start();
        }
    }

    // What to do once bus stops nearby have been found
    @Override
    public void onBusStopsFound(boolean isError, JSONArray busStopsArray)
    {
        progressBar.setVisibility(View.GONE);
        /*refreshFloatingActionButton.clearAnimation();
        refreshFloatingActionButton.setEnabled(true);*/
        wasLocatingBusStopsNearby = false;
        ArrayList<String> busStopNames = new ArrayList<>();
        ArrayList<String> busStopDistances = new ArrayList<>();
        busStops.clear();

        if (!isError)
        {
            if (busStopsArray == null || busStopsArray.length() == 0)
            {
                busStopsNearbyListView.setVisibility(View.INVISIBLE);
                errorMessageTextView.setText(R.string.error_no_bus_stops_found_text);
                errorMessageTextView.setVisibility(View.VISIBLE);
                return;
            }

            try
            {
                for (int i = 0; i < busStopsArray.length(); i++)
                {
                    String busStopName = busStopsArray.getJSONObject(i).getString("StopName");
                    if (!busStopName.contains("CS-"))
                    {
                        BusStop busStop = new BusStop();
                        busStop.setBusStopName(busStopName);
                        busStop.setBusStopId(busStopsArray.getJSONObject(i).getInt("StopId"));
                        busStopNames.add(busStopName);
                        busStopDistances.add(busStopsArray.getJSONObject(i).getString("StopDist"));
                        busStops.add(busStop);
                    }
                }

                busStopsNearbyListAdaptor = new NearbyBusStopsListCustomAdapter(getActivity(), busStopNames, busStopDistances);
                busStopsNearbyListView.setAdapter(busStopsNearbyListAdaptor);
                busStopsNearbyListAdaptor.notifyDataSetChanged();
                busStopsNearbyListView.setVisibility(View.VISIBLE);
                busStopsNearbyListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        Intent getBusesArrivingAtBusStopIntent = new Intent(getContext(), BusesArrivingAtBusStopActivity.class);
                        getBusesArrivingAtBusStopIntent.putExtra("BUS_STOP_NAME", busStops.get(position).getBusStopName());
                        getBusesArrivingAtBusStopIntent.putExtra("BUS_STOP_ID", busStops.get(position).getBusStopId());
                        startActivity(getBusesArrivingAtBusStopIntent);
                    }
                });

                countDownTimer = new CountDownTimer(45000, 45000)
                {
                    @Override
                    public void onTick(long millisUntilFinished)
                    {

                    }

                    @Override
                    public void onFinish()
                    {
                        refresh();
                    }
                }.start();
            }
            catch (JSONException e)
            {
                busStopsNearbyListView.setVisibility(View.INVISIBLE);
                errorMessageTextView.setText(R.string.error_no_bus_stops_found_text);
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            busStopsNearbyListView.setVisibility(View.INVISIBLE);
            errorMessageTextView.setText(R.string.error_connecting_to_the_internet_click_refresh_text);
            errorMessageTextView.setVisibility(View.VISIBLE);
        }
    }

    // What to do once buses arriving at the selected stop have been found
    @Override
    public void onBusesAtStopFound(String errorMessage, JSONArray buses)
    {

    }

    // What to do once bus route details have been found
    @Override
    public void onBusRouteDetailsFound(String errorMessage, final Route route, boolean isForList, final String routeDirection)
    {

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

    public void onStop()
    {
        if (googleApiClient != null)
        {
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onPause()
    {
        if (progressBar != null && isRequestingLocationUpdates)
        {
            progressBar.setVisibility(View.GONE);
        }
        if (getNearestBusStopsTask != null)
        {
            getNearestBusStopsTask.cancel(true);
        }
        stopLocationUpdates();
        if(countDownTimer != null)
        {
            countDownTimer.cancel();
        }
        super.onPause();
    }

    @Override
    public void onResume()
    {
        if ((googleApiClient != null && googleApiClient.isConnected() && locationIsToBeUpdated) || wasLocatingBusStopsNearby)
        {
            locationIsToBeUpdated = true;
            errorMessageTextView.setVisibility(View.GONE);
            /*refreshFloatingActionButton.startAnimation(rotatingAnimation);
            refreshFloatingActionButton.setEnabled(false);*/
            progressBar.setVisibility(View.VISIBLE);
            createLocationRequest();
        }
        refresh();
        super.onResume();
    }

    @Override
    public void onStart()
    {
        if (googleApiClient != null)
        {
            googleApiClient.connect();
        }
        else
        {
            googleApiClient = new GoogleApiClient.Builder(getContext()).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
            googleApiClient.connect();
        }
        super.onStart();
    }
}