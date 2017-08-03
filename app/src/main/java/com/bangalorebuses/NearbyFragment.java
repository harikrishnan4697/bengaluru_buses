package com.bangalorebuses;

import android.Manifest;
import android.app.Activity;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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

import static android.content.Context.LOCATION_SERVICE;
import static com.bangalorebuses.Constants.LOCATION_PERMISSION_REQUEST_CODE;

public class NearbyFragment extends Fragment implements NetworkingHelper, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener
{
    private ListView busStopsNearbyListView;
    private LinearLayout updatingBusStopsProgressBarLinearLayout;
    private NearbyBusStopsListCustomAdapter busStopsNearbyListAdaptor;
    private ArrayList<BusStop> busStops = new ArrayList<>();
    private GetNearestBusStopsTask getNearestBusStopsTask;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private LocationManager locationManager;
    private boolean isRequestingLocationUpdates;
    private LinearLayout errorLinearLayout;
    private ImageView errorImageView;
    private TextView errorTextView;
    private Button errorResolutionButton;

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

        errorLinearLayout = (LinearLayout) view.findViewById(R.id.errorLinearLayout);
        errorImageView = (ImageView) view.findViewById(R.id.errorImageView);
        errorTextView = (TextView) view.findViewById(R.id.errorTextView);
        errorResolutionButton = (Button) view.findViewById(R.id.errorResolutionButton);
        errorResolutionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fixLocationError();
            }
        });
        errorLinearLayout.setVisibility(View.GONE);

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
                searchActivityIntent.putExtra("Search_Type", Constants.SEARCH_TYPE_BUS_STOP);
                startActivityForResult(searchActivityIntent, Constants.SEARCH_NEARBY_BUS_STOP_REQUEST_CODE);
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
        createLocationRequest();
    }

    /**
     * This method is automatically called by the
     * Google Api client when it's unable to connect
     * to the device's location service.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        Toast.makeText(getActivity(), "Couldn't get current location! Please try again later...", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(getActivity(), "Couldn't get current location! Please try again later...", Toast.LENGTH_SHORT).show();
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
        locationRequest.setInterval(60000);
        locationRequest.setFastestInterval(45000);
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
        updatingBusStopsProgressBarLinearLayout.setVisibility(View.GONE);
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
        try
        {
            if (isNetworkAvailable())
            {
                URL nearestBusStopURL = new URL("http://bmtcmob.hostg.in/api/busstops/stopnearby/lat/" + location.getLatitude() + "/lon/" + location.getLongitude() + "/rad/1.0");
                getNearestBusStopsTask = new GetNearestBusStopsTask(this);
                getNearestBusStopsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, nearestBusStopURL);
            }
            else
            {
                updatingBusStopsProgressBarLinearLayout.setVisibility(View.GONE);
                errorImageView.setImageResource(R.drawable.ic_cloud_off_black);
                errorTextView.setText("Uh oh! No data connection.");
                errorResolutionButton.setText("Try again");
                errorLinearLayout.setVisibility(View.VISIBLE);
                busStopsNearbyListView.setVisibility(View.GONE);
            }
        }
        catch (MalformedURLException e)
        {
            updatingBusStopsProgressBarLinearLayout.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Couldn't find bus stops nearby! Please try again later.", Toast.LENGTH_SHORT).show();
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
                errorResolutionButton.setText("Allow GPS access");
                errorLinearLayout.setVisibility(View.VISIBLE);
                busStopsNearbyListView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 1)
        {
            if (resultCode == Activity.RESULT_OK)
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
                errorResolutionButton.setText("Turn GPS on");
                errorLinearLayout.setVisibility(View.VISIBLE);
                busStopsNearbyListView.setVisibility(View.GONE);
            }
        }
    }

    public void fixLocationError()
    {
        errorLinearLayout.setVisibility(View.GONE);
        stopLocationUpdates();
        createLocationRequest();
    }

    // What to do once bus stops nearby have been found
    @Override
    public void onBusStopsFound(boolean isError, JSONArray busStopsArray)
    {
        errorLinearLayout.setVisibility(View.GONE);
        updatingBusStopsProgressBarLinearLayout.setVisibility(View.GONE);
        ArrayList<String> busStopNames = new ArrayList<>();
        ArrayList<String> busStopDistances = new ArrayList<>();
        busStops.clear();
        boolean hasSetNearestBusStop = false;

        if (!isError)
        {
            if (busStopsArray == null || busStopsArray.length() == 0)
            {
                errorImageView.setImageResource(R.drawable.ic_person_pin_circle_black);
                errorTextView.setText("Uh oh! There aren't any Bengaluru City bus stops nearby.");
                errorResolutionButton.setText("Check again");
                errorLinearLayout.setVisibility(View.VISIBLE);
                busStopsNearbyListView.setVisibility(View.GONE);
            }
            else
            {

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
                            if (!hasSetNearestBusStop)
                            {
                                Constants.nearestBusStop = busStop;
                                hasSetNearestBusStop = true;
                            }
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
                }
                catch (JSONException e)
                {
                    errorImageView.setImageResource(R.drawable.ic_person_pin_circle_black);
                    errorTextView.setText("Uh oh! There aren't any Bengaluru City bus stops nearby.");
                    errorResolutionButton.setText("Check again");
                    errorLinearLayout.setVisibility(View.VISIBLE);
                    busStopsNearbyListView.setVisibility(View.GONE);
                }
            }
        }
        else
        {
            errorImageView.setImageResource(R.drawable.ic_cloud_off_black);
            errorTextView.setText("Uh oh! No data connection.");
            errorResolutionButton.setText("Try again");
            errorLinearLayout.setVisibility(View.VISIBLE);
            busStopsNearbyListView.setVisibility(View.GONE);
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
    public void onResume()
    {
        super.onResume();
        if (googleApiClient != null && googleApiClient.isConnected() && !isRequestingLocationUpdates)
        {
            startLocationUpdates();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (getNearestBusStopsTask != null)
        {
            getNearestBusStopsTask.cancel(true);
        }
        stopLocationUpdates();
        isRequestingLocationUpdates = false;
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (googleApiClient != null)
        {
            googleApiClient.disconnect();
        }
    }
}