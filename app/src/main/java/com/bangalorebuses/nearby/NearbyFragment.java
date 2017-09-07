package com.bangalorebuses.nearby;

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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.bangalorebuses.search.SearchActivity;
import com.bangalorebuses.utils.Constants;
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
import static com.bangalorebuses.utils.Constants.NETWORK_QUERY_NO_ERROR;
import static com.bangalorebuses.utils.Constants.SEARCH_NEARBY_BUS_STOP_REQUEST_CODE;
import static com.bangalorebuses.utils.Constants.db;

public class NearbyFragment extends Fragment implements NetworkingHelper, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, NearbyBusStopsRecyclerViewAdapter.ItemClickListener
{
    private LinearLayout updatingBusStopsProgressBarLinearLayout;
    private ArrayList<BusStop> busStops = new ArrayList<>();
    private NearestBusStopsTask nearestBusStopsTask;
    private GetRoutesArrivingAtStopTask getRoutesArrivingAtStopTask;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private LocationManager locationManager;
    private boolean isRequestingLocationUpdates;
    private LinearLayout errorLinearLayout;
    private ImageView errorImageView;
    private TextView errorTextView;
    private TextView errorResolutionTextView;
    private RecyclerView nearbyBusStopsRecyclerView;
    private boolean locationHasToBeUpdated = false;
    private NearbyBusStopsRecyclerViewAdapter adaptor;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.nearby_fragment, container, false);
        updatingBusStopsProgressBarLinearLayout = (LinearLayout) view.findViewById(R.id.updatingBusStopsProgressBarLinearLayout);
        updatingBusStopsProgressBarLinearLayout.setVisibility(View.GONE);
        nearbyBusStopsRecyclerView = (RecyclerView) view.findViewById(R.id.nearbyBusStopsRecyclerView);
        errorLinearLayout = (LinearLayout) view.findViewById(R.id.errorLinearLayout);
        errorImageView = (ImageView) view.findViewById(R.id.errorImageView);
        errorTextView = (TextView) view.findViewById(R.id.errorTextView);
        errorResolutionTextView = (TextView) view.findViewById(R.id.errorResolutionTextView);
        errorResolutionTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fixLocationError();
            }
        });
        errorLinearLayout.setVisibility(View.GONE);
        nearbyBusStopsRecyclerView.setVisibility(View.GONE);
        if (adaptor != null)
        {
            errorLinearLayout.setVisibility(View.GONE);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            nearbyBusStopsRecyclerView.setLayoutManager(linearLayoutManager);
            nearbyBusStopsRecyclerView.setAdapter(adaptor);
            adaptor.setClickListener(NearbyFragment.this);
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

        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        // Connect to the Google api client for location services
        if (googleApiClient == null)
        {
            locationHasToBeUpdated = true;
            googleApiClient = new GoogleApiClient.Builder(getActivity()).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.nearby_search:
                Intent searchActivityIntent = new Intent(getContext(), SearchActivity.class);
                searchActivityIntent.putExtra("SEARCH_TYPE", Constants.SEARCH_TYPE_BUS_STOP_WITH_DIRECTION);
                startActivityForResult(searchActivityIntent, Constants.SEARCH_NEARBY_BUS_STOP_REQUEST_CODE);
                break;
            case R.id.nearby_refresh:
                createLocationRequest();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
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
        updatingBusStopsProgressBarLinearLayout.setVisibility(View.GONE);
        errorImageView.setImageResource(R.drawable.ic_location_off_black);
        errorTextView.setText("Couldn't get current location! Please make sure you have the latest version of the Google Play Services...");
        errorResolutionTextView.setText("Retry");
        errorLinearLayout.setVisibility(View.VISIBLE);
        nearbyBusStopsRecyclerView.setVisibility(View.GONE);
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
        updatingBusStopsProgressBarLinearLayout.setVisibility(View.GONE);
        errorImageView.setImageResource(R.drawable.ic_location_off_black);
        errorTextView.setText("Something went wrong! Couldn't get current location...");
        errorResolutionTextView.setText("Retry");
        errorLinearLayout.setVisibility(View.VISIBLE);
        nearbyBusStopsRecyclerView.setVisibility(View.GONE);
    }

    /**
     * This method creates the location request
     * required to get the user's current location.
     */
    protected void createLocationRequest()
    {
        updatingBusStopsProgressBarLinearLayout.setVisibility(View.VISIBLE);
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
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
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
                                    Toast.makeText(getActivity(), "Failed to enable location services!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        else
                        {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
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
            e.printStackTrace();
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
        try
        {
            if (isNetworkAvailable())
            {
                updatingBusStopsProgressBarLinearLayout.setVisibility(View.VISIBLE);
                URL nearestBusStopURL = new URL("http://bmtcmob.hostg.in/api/busstops/stopnearby/lat/" + location.getLatitude() + "/lon/" + location.getLongitude() + "/rad/1.0");
                nearestBusStopsTask = new NearestBusStopsTask(this);
                nearestBusStopsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, nearestBusStopURL);
            }
            else
            {
                updatingBusStopsProgressBarLinearLayout.setVisibility(View.GONE);
                setErrorLayoutContent(R.drawable.ic_cloud_off_black, "Uh oh! No data connection.", "Retry");
                errorLinearLayout.setVisibility(View.VISIBLE);
                nearbyBusStopsRecyclerView.setVisibility(View.GONE);
            }
        }
        catch (MalformedURLException e)
        {
            updatingBusStopsProgressBarLinearLayout.setVisibility(View.GONE);
            setErrorLayoutContent(R.drawable.ic_person_pin_circle_black, "Couldn't find bus stops nearby! Please try again later.", "Retry");
            errorLinearLayout.setVisibility(View.VISIBLE);
            nearbyBusStopsRecyclerView.setVisibility(View.GONE);
        }
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
                updatingBusStopsProgressBarLinearLayout.setVisibility(View.GONE);
                errorImageView.setImageResource(R.drawable.ic_location_disabled_black);
                errorTextView.setText("Can't find bus stops nearby without access to GPS.");
                errorResolutionTextView.setText("Allow GPS access");
                errorLinearLayout.setVisibility(View.VISIBLE);
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
                updatingBusStopsProgressBarLinearLayout.setVisibility(View.GONE);
                errorImageView.setImageResource(R.drawable.ic_location_off_black);
                errorTextView.setText("Can't find bus stops nearby because GPS is turned off.");
                errorResolutionTextView.setText("Turn GPS on");
                errorLinearLayout.setVisibility(View.VISIBLE);
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

    public void fixLocationError()
    {
        errorLinearLayout.setVisibility(View.GONE);
        stopLocationUpdates();
        createLocationRequest();
    }

    @Override
    public void onClick(View view, int position)
    {
        Intent getBusesArrivingAtBusStopIntent = new Intent(getContext(), BusesArrivingAtBusStopActivity.class);
        getBusesArrivingAtBusStopIntent.putExtra("BUS_STOP_NAME", busStops.get(position).getBusStopName());
        getBusesArrivingAtBusStopIntent.putExtra("BUS_STOP_DIRECTION_NAME", busStops.get(position).getBusStopDirectionName());
        getBusesArrivingAtBusStopIntent.putExtra("BUS_STOP_ID", busStops.get(position).getBusStopId());
        startActivity(getBusesArrivingAtBusStopIntent);
    }

    // What to do once bus stops nearby have been found

    @Override
    public void onBusStopsNearbyFound(String errorMessage, ArrayList<BusStop> busStops)
    {
        this.busStops.clear();

        if (errorMessage.equals(NETWORK_QUERY_NO_ERROR))
        {
            if (busStops.size() == 0)
            {
                updatingBusStopsProgressBarLinearLayout.setVisibility(View.GONE);
                setErrorLayoutContent(R.drawable.ic_person_pin_circle_black, "Uh oh! There aren't any Bengaluru City bus stops nearby.", "Retry");
                errorLinearLayout.setVisibility(View.VISIBLE);
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
            // TODO Use String errorMessage instead of boolean isError to improve error messages to the user
            updatingBusStopsProgressBarLinearLayout.setVisibility(View.GONE);
            setErrorLayoutContent(R.drawable.ic_cloud_off_black, "Uh oh! No data connection.", "Retry");
            errorLinearLayout.setVisibility(View.VISIBLE);
            nearbyBusStopsRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBusETAsOnBusRouteFound(String errorMessage, int busStopRouteOrder, ArrayList<Bus> buses, BusRoute busRoute)
    {

    }

    private void setErrorLayoutContent(int drawableResId, String errorMessage, String resolutionButtonText)
    {
        errorImageView.setImageResource(drawableResId);
        errorTextView.setText(errorMessage);
        errorResolutionTextView.setText(resolutionButtonText);
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
    public void onPause()
    {
        super.onPause();
        if (isRequestingLocationUpdates)
        {
            locationHasToBeUpdated = true;
            stopLocationUpdates();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (googleApiClient != null)
        {
            googleApiClient.disconnect();
        }
    }

    private class GetRoutesArrivingAtStopTask extends AsyncTask<ArrayList<BusStop>, Void, ArrayList<BusStop>>
    {

        @Override
        protected ArrayList<BusStop> doInBackground(ArrayList<BusStop>... busStops)
        {
            for (BusStop busStop : busStops[0])
            {
                ArrayList<BusRoute> busRoutes = DbQueries.getRoutesArrivingAtStop(db, busStop.getBusStopId());
                for (BusRoute busRoute : busRoutes)
                {
                    if (busRoute.getBusRouteNumber().contains("KIAS-"))
                    {
                        busStop.setAirportShuttleStop(true);
                    }
                    else if (busRoute.getBusRouteNumber().contains("MF-"))
                    {
                        busStop.setMetroFeederStop(true);
                    }
                }
            }
            return busStops[0];
        }

        @Override
        protected void onPostExecute(ArrayList<BusStop> busStops)
        {
            super.onPostExecute(busStops);
            errorLinearLayout.setVisibility(View.GONE);
            updatingBusStopsProgressBarLinearLayout.setVisibility(View.GONE);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            nearbyBusStopsRecyclerView.setLayoutManager(linearLayoutManager);
            adaptor = new NearbyBusStopsRecyclerViewAdapter(busStops);
            nearbyBusStopsRecyclerView.setAdapter(adaptor);
            adaptor.setClickListener(NearbyFragment.this);
            nearbyBusStopsRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}