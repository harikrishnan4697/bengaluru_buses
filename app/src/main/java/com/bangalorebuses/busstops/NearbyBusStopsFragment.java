package com.bangalorebuses.busstops;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bangalorebuses.R;
import com.bangalorebuses.busarrivals.BusesArrivingAtBusStopActivity;
import com.bangalorebuses.core.Bus;
import com.bangalorebuses.core.BusRoute;
import com.bangalorebuses.core.BusStop;
import com.bangalorebuses.utils.CommonMethods;
import com.bangalorebuses.utils.DbQueries;
import com.bangalorebuses.utils.NetworkingHelper;
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LOCATION_SERVICE;
import static com.bangalorebuses.utils.Constants.LOCATION_PERMISSION_REQUEST_CODE;
import static com.bangalorebuses.utils.Constants.NETWORK_QUERY_IO_EXCEPTION;
import static com.bangalorebuses.utils.Constants.NETWORK_QUERY_JSON_EXCEPTION;
import static com.bangalorebuses.utils.Constants.NETWORK_QUERY_NO_ERROR;
import static com.bangalorebuses.utils.Constants.NETWORK_QUERY_REQUEST_TIMEOUT_EXCEPTION;
import static com.bangalorebuses.utils.Constants.NETWORK_QUERY_URL_EXCEPTION;
import static com.bangalorebuses.utils.Constants.SEARCH_NEARBY_BUS_STOP_REQUEST_CODE;
import static com.bangalorebuses.utils.Constants.db;

public class NearbyBusStopsFragment extends Fragment implements NetworkingHelper, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, SwipeRefreshLayout.OnRefreshListener
{
    private ArrayList<BusStop> busStops = new ArrayList<>();
    private NearestBusStopsTask nearestBusStopsTask;
    private GetRoutesArrivingAtStopTask getRoutesArrivingAtStopTask;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private LocationManager locationManager;
    private boolean isRequestingLocationUpdates;
    private RecyclerView nearbyBusStopsRecyclerView;
    private boolean locationHasToBeUpdated = false;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NearbyBusStopsRecyclerViewAdapter adaptor;
    private LinearLayout errorLinearLayout;
    private ImageView errorImageView;
    private TextView errorMessageTextView;
    private TextView errorResolutionTextView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_nearby_bus_stops, container, false);
        nearbyBusStopsRecyclerView = view.findViewById(R.id.nearbyBusStopsRecyclerView);
        nearbyBusStopsRecyclerView.setVisibility(View.GONE);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorNonACBus, R.color.colorACBus, R.color.colorMetroFeederBus);

        errorLinearLayout = view.findViewById(R.id.errorLinearLayout);
        errorLinearLayout.setVisibility(View.GONE);
        errorImageView = view.findViewById(R.id.errorImageView);
        errorMessageTextView = view.findViewById(R.id.errorTextView);
        errorResolutionTextView = view.findViewById(R.id.errorResolutionTextView);

        errorResolutionTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                createLocationRequest();
            }
        });

        if (adaptor != null)
        {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            nearbyBusStopsRecyclerView.setLayoutManager(linearLayoutManager);
            nearbyBusStopsRecyclerView.setAdapter(adaptor);
            nearbyBusStopsRecyclerView.setVisibility(View.VISIBLE);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        swipeRefreshLayout.setOnRefreshListener(this);

        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        // Connect to the Google api client for location services
        if (googleApiClient == null)
        {
            locationHasToBeUpdated = true;
            googleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).build();
            googleApiClient.connect();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            default:
                return super.onOptionsItemSelected(item);
        }
        //return true;
    }

    /**
     * This method is automatically called by the
     * Google Api client when it's able to connect
     * to the device's location service.
     */
    @Override
    public void onConnected(Bundle connectionHint)
    {
        if (locationHasToBeUpdated)
        {
            createLocationRequest();
            locationHasToBeUpdated = false;
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
        swipeRefreshLayout.setVisibility(View.GONE);
        showError(R.drawable.ic_location_disabled_black,
                R.string.error_message_update_google_play_services,
                R.string.fix_error_no_fix);
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
        swipeRefreshLayout.setVisibility(View.GONE);
        showError(R.drawable.ic_location_disabled_black,
                R.string.error_message_update_google_play_services,
                R.string.fix_error_no_fix);
    }

    /**
     * This method creates the location request
     * required to get the user's current location.
     */
    protected void createLocationRequest()
    {
        errorLinearLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(true);
        final int REQUEST_CHECK_SETTINGS = 0x1;
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
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
                            int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
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

                        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
                        if (permissionCheck == PackageManager.PERMISSION_GRANTED)
                        {
                            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                            {
                                if (!isRequestingLocationUpdates)
                                {
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
                                    Toast.makeText(getActivity(), "Failed to enable location services! Please try again later...", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        else
                        {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Toast.makeText(getActivity(), "Device doesn't have a GPS! Can't locate bus stops nearby...", Toast.LENGTH_LONG).show();
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
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
                isRequestingLocationUpdates = true;
            }
        }
        catch (SecurityException e)
        {
            Toast.makeText(getContext(), "Unable to get your current location!" +
                    " Please enable the location permission in Settings > Apps " +
                    "> Bengaluru Buses.", Toast.LENGTH_LONG).show();
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
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            isRequestingLocationUpdates = false;
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
        stopLocationUpdates();
        isRequestingLocationUpdates = false;

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        nearbyBusStopsRecyclerView.setLayoutManager(linearLayoutManager);
        adaptor = new NearbyBusStopsRecyclerViewAdapter(getContext(), busStops);
        nearbyBusStopsRecyclerView.setAdapter(adaptor);

        try
        {
            if (CommonMethods.checkNetworkConnectivity(getActivity()))
            {
                URL nearestBusStopURL = new URL("http://bmtcmob.hostg.in/api/busstops/stopnearby/lat/" +
                        location.getLatitude() + "/lon/" + location.getLongitude() + "/rad/1.0");
                nearestBusStopsTask = new NearestBusStopsTask(this);
                nearestBusStopsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, nearestBusStopURL);
            }
            else
            {
                showError(R.drawable.ic_cloud_off_black, R.string.error_message_internet_unavailable,
                        R.string.fix_error_retry);
                nearbyBusStopsRecyclerView.setVisibility(View.GONE);
            }
        }
        catch (MalformedURLException e)
        {
            showError(R.drawable.ic_sad_face, R.string.error_message_url_exception,
                    R.string.fix_error_retry);
            nearbyBusStopsRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRefresh()
    {
        cancelAllTasks();
        createLocationRequest();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                createLocationRequest();
                errorLinearLayout.setVisibility(View.GONE);
            }
            else
            {
                showError(R.drawable.ic_location_disabled_black, R.string.error_message_gps_access_denied,
                        R.string.fix_error_allow_access);
                nearbyBusStopsRecyclerView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 1)
        {
            if (resultCode == RESULT_OK)
            {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                {
                    createLocationRequest();
                    errorLinearLayout.setVisibility(View.GONE);
                }
            }
            else
            {
                showError(R.drawable.ic_location_off_black, R.string.error_message_gps_off,
                        R.string.fix_error_turn_on);
                nearbyBusStopsRecyclerView.setVisibility(View.GONE);
            }
        }
        else if (resultCode == RESULT_OK && requestCode == SEARCH_NEARBY_BUS_STOP_REQUEST_CODE)
        {
            Intent getBusesArrivingAtBusStopIntent = new Intent(getContext(), BusesArrivingAtBusStopActivity.class);
            getBusesArrivingAtBusStopIntent.putExtra("BUS_STOP_NAME", data.getStringExtra("BUS_STOP_NAME"));
            getBusesArrivingAtBusStopIntent.putExtra("BUS_STOP_DIRECTION_NAME", data.getStringExtra("BUS_STOP_DIRECTION_NAME"));
            getBusesArrivingAtBusStopIntent.putExtra("BUS_STOP_ID", data.getIntExtra("BUS_STOP_ID", 0));
            startActivity(getBusesArrivingAtBusStopIntent);
        }
    }

    @Override
    public void onBusStopsNearbyFound(String errorMessage, ArrayList<BusStop> busStops)
    {
        this.busStops.clear();

        if (errorMessage.equals(NETWORK_QUERY_NO_ERROR))
        {
            if (busStops.size() == 0)
            {
                showError(R.drawable.ic_person_pin_circle_black, R.string.error_message_no_bus_stops_nearby,
                        R.string.fix_error_no_fix);
                nearbyBusStopsRecyclerView.setVisibility(View.GONE);
            }
            else
            {
                for (BusStop busStop : busStops)
                {
                    this.busStops.add(busStop);
                }

                getRoutesArrivingAtStopTask = new GetRoutesArrivingAtStopTask();
                getRoutesArrivingAtStopTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this.busStops);
            }
        }
        else
        {
            nearbyBusStopsRecyclerView.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            if (errorMessage.equals(NETWORK_QUERY_URL_EXCEPTION))
            {
                showError(R.drawable.ic_sad_face, R.string.error_message_url_exception,
                        R.string.fix_error_retry);
            }
            else if (errorMessage.equals(NETWORK_QUERY_IO_EXCEPTION))
            {
                showError(R.drawable.ic_cloud_off_black, R.string.error_message_io_exception,
                        R.string.fix_error_retry);
            }
            else if (errorMessage.equals(NETWORK_QUERY_REQUEST_TIMEOUT_EXCEPTION))
            {
                showError(R.drawable.ic_cloud_off_black, R.string.error_message_timeout_exception,
                        R.string.fix_error_retry);
            }
            else if (errorMessage.equals(NETWORK_QUERY_JSON_EXCEPTION))
            {
                showError(R.drawable.ic_person_pin_circle_black, R.string.error_message_no_bus_stops_nearby,
                        R.string.fix_error_no_fix);
            }
        }
    }

    @Override
    public void onBusETAsOnBusRouteFound(String errorMessage, int busStopRouteOrder, ArrayList<Bus> buses, BusRoute busRoute)
    {

    }

    private void showError(int drawableResId, int errorMessageStringResId, int resolutionButtonStringResId)
    {
        swipeRefreshLayout.setRefreshing(false);
        errorImageView.setImageResource(drawableResId);
        errorMessageTextView.setText(errorMessageStringResId);
        errorResolutionTextView.setText(resolutionButtonStringResId);
        errorLinearLayout.setVisibility(View.VISIBLE);
    }

    private void cancelAllTasks()
    {
        if (nearestBusStopsTask != null)
        {
            nearestBusStopsTask.cancel(true);
        }

        if (getRoutesArrivingAtStopTask != null)
        {
            getRoutesArrivingAtStopTask.cancel(true);
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if (googleApiClient != null)
        {
            googleApiClient.connect();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (isRequestingLocationUpdates)
        {
            locationHasToBeUpdated = true;
            stopLocationUpdates();
        }

        if (googleApiClient != null)
        {
            googleApiClient.disconnect();
        }

        cancelAllTasks();
    }

    private class GetRoutesArrivingAtStopTask extends AsyncTask<ArrayList<BusStop>,
            Void, ArrayList<BusStop>>
    {

        @Override
        protected ArrayList<BusStop> doInBackground(ArrayList<BusStop>... busStops)
        {
            for (BusStop busStop : busStops[0])
            {
                busStop.setBusesArrivingAtBusStop(DbQueries.getRoutesArrivingAtStop(db, busStop.getBusStopId()));
            }
            return busStops[0];
        }

        @Override
        protected void onPostExecute(ArrayList<BusStop> busStops)
        {
            super.onPostExecute(busStops);

            if (!isCancelled())
            {
                errorLinearLayout.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                adaptor.notifyDataSetChanged();
                nearbyBusStopsRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }
}