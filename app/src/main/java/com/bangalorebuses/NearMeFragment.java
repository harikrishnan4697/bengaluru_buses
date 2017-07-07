package com.bangalorebuses;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static com.bangalorebuses.Constants.LOCATION_PERMISSION_REQUEST_CODE;

public class NearMeFragment extends Fragment implements NetworkingManager, AdapterView.OnItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{
    private BusStop nearestBusStops[] = new BusStop[8];
    private int selectedBusStopPosition = 0;
    private Set<String> busesArrivingAtSelectedStopSet;
    private LinearLayout busDetailsLinearLayout;
    private Intent trackBusIntent;
    private ProgressDialog progressDialog;
    private String FILENAME = "buses_at_bus_stop";
    private Spinner nearestBusStopsSpinner;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private boolean isRequestingLocationUpdates = false;
    private TextView errorMessageTextView;
    private boolean locationIsToBeUpdated = true;
    private LocationManager locationManager;
    private boolean updateBusList = false;
    private FloatingActionButton refreshFloatingActionButton;
    private Animation rotatingAnimation;
    private int numberOfRefreshIconRotationsRemaining = 0;
    private boolean busesAtStopListHasTraceableBuses = false;
    private TextView busesArrivingAtStopListDescriptionTextView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.near_me_fragment, container, false);
        refreshFloatingActionButton = (FloatingActionButton) view.findViewById(R.id.floatingRefreshActionButton);
        refreshFloatingActionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                refresh();
            }
        });
        rotatingAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
        locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
        busesArrivingAtSelectedStopSet = new HashSet<>();
        busDetailsLinearLayout = (LinearLayout) view.findViewById(R.id.buses_arriving_at_bus_stop_linear_layout);
        errorMessageTextView = (TextView) view.findViewById(R.id.nearby_fragment_error_message_text_view);
        nearestBusStopsSpinner = (Spinner) view.findViewById(R.id.nearest_bus_stop_list_spinner);
        busesArrivingAtStopListDescriptionTextView = (TextView) view.findViewById(R.id.info_text_view_2);
        errorMessageTextView.setVisibility(View.GONE);
        updateBusList = false;
        locationIsToBeUpdated = true;

        // Connect to the Google api client for location services
        /*if (googleApiClient == null)
        {
            googleApiClient = new GoogleApiClient.Builder(getContext()).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
            googleApiClient.connect();
        }*/
        return view;
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
        busDetailsLinearLayout.setVisibility(View.INVISIBLE);
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
                                progressDialog = ProgressDialog.show(getContext(), "Please wait", "Getting your location...", true);
                                startLocationUpdates();
                            }
                            else
                            {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
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
                                    progressDialog = ProgressDialog.show(getContext(), "Please wait", "Getting your location...", true);
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
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
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
        if (progressDialog != null)
        {
            progressDialog.dismiss();
        }
        try
        {
            if (isNetworkAvailable())
            {
                progressDialog = ProgressDialog.show(getContext(), "Please wait", "Locating bus stops nearby...", true);
                URL nearestBusStopURL = new URL("http://bmtcmob.hostg.in/api/busstops/stopnearby/lat/" + String.valueOf(location.getLatitude()) + "/lon/" + String.valueOf(location.getLongitude()) + "/rad/1.0");
                new GetNearestBusStopsTask(this).execute(nearestBusStopURL);
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
                refreshFloatingActionButton.clearAnimation();
                refreshFloatingActionButton.setEnabled(true);
                Toast.makeText(getContext(), "Location access was denied! Couldn't locate bus stops nearby...", Toast.LENGTH_LONG).show();
            }
        }
    }


    // What to do after the user has allowed or denied location services to be turned on
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 1)
        {
            if (resultCode == -1)
            {
                if (!isRequestingLocationUpdates)
                {
                    progressDialog = ProgressDialog.show(getContext(), "Please wait", "Getting your location...", true);
                    startLocationUpdates();

                }
            }
            else if (resultCode == 0)
            {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                {
                    if (!isRequestingLocationUpdates)
                    {
                        progressDialog = ProgressDialog.show(getContext(), "Please wait", "Getting your location...", true);
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

    // What to do when a bus stop has been selected or not selected from the dropdown of nearest bus stops
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        this.selectedBusStopPosition = position;
        busDetailsLinearLayout.removeAllViews();
        errorMessageTextView.setVisibility(View.GONE);
        busesArrivingAtStopListDescriptionTextView.setText("Buses arriving at " + nearestBusStops[position].getBusStopName());
        if (position == 0 && !updateBusList)
        {
            try
            {
                FileInputStream fileInputStream = getActivity().openFileInput(FILENAME);
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
                            progressDialog = ProgressDialog.show(getContext(), "Please wait", "Getting buses...");
                            JSONArray jsonArray = new JSONArray(stringBuilder.toString());
                            onBusesAtStopFound(false, jsonArray);
                            nearestBusStopsSpinner.setEnabled(false);
                            refreshFloatingActionButton.setEnabled(false);
                            refreshFloatingActionButton.startAnimation(rotatingAnimation);
                            String requestBody = "stopID=" + Integer.toString(nearestBusStops[position].getBusStopId());
                            busesArrivingAtSelectedStopSet.clear();
                            new GetBusesAtStopTask(this).execute(requestBody);
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
                            progressDialog = ProgressDialog.show(getContext(), "Please wait", "Getting buses...");
                            String requestBody = "stopID=" + Integer.toString(nearestBusStops[position].getBusStopId());
                            busesArrivingAtSelectedStopSet.clear();
                            new GetBusesAtStopTask(this).execute(requestBody);
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
                    Toast.makeText(getContext(), "Unknown error occurred! Please try again later...", Toast.LENGTH_SHORT).show();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
                if (isNetworkAvailable())
                {
                    progressDialog = ProgressDialog.show(getContext(), "Please wait", "Getting buses...");
                    String requestBody = "stopID=" + Integer.toString(nearestBusStops[position].getBusStopId());
                    busesArrivingAtSelectedStopSet.clear();
                    new GetBusesAtStopTask(this).execute(requestBody);
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
                Toast.makeText(getContext(), "Unknown error occurred! Please try again later...", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            if (isNetworkAvailable())
            {
                progressDialog = ProgressDialog.show(getContext(), "Please wait", "Getting buses...");
                String requestBody = "stopID=" + Integer.toString(nearestBusStops[position].getBusStopId());
                busesArrivingAtSelectedStopSet.clear();
                new GetBusesAtStopTask(this).execute(requestBody);
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
        Toast.makeText(getContext(), "Please choose a bus stop!", Toast.LENGTH_LONG).show();
    }

    // What to do when the floating refresh button has been clicked
    public void refresh()
    {
        refreshFloatingActionButton.startAnimation(rotatingAnimation);
        refreshFloatingActionButton.setEnabled(false);
        errorMessageTextView.setVisibility(View.GONE);
        createLocationRequest();
    }

    @Override
    public void onDirectBusesFound(String errorMessage, Bus[] buses)
    {

    }

    @Override
    public void onTransitPointsFound(String errorMessage, BusStop[] transitPoints)
    {

    }

    @Override
    public void onTransitPointBusCountFound(String errorMessage, int originToTransitPointBusCount, int transitPointToDestinationBusCount, BusStop transitPoint)
    {

    }

    @Override
    public void onIndirectBusesFound(String errorMessage, Bus[] buses, BusStop transitPoint, String routeMessage)
    {

    }

    // What to do once bus stops nearby have been found
    @Override
    public void onBusStopsFound(boolean isError, JSONArray busStopsArray)
    {
        progressDialog.dismiss();
        ArrayList<String> stopList = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, stopList);
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
                            if (busStopName.charAt(busStopName.indexOf("(") - 1) == ' ')
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
                nearestBusStopsSpinner.setAdapter(adapter);
                nearestBusStopsSpinner.setOnItemSelectedListener(this);
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
            errorMessageTextView.setText(R.string.error_connecting_to_the_internet_text);
            errorMessageTextView.setVisibility(View.VISIBLE);
        }
    }

    // What to do once buses arriving at the selected stop have been found
    @Override
    public void onBusesAtStopFound(boolean isError, JSONArray buses)
    {
        if (!isError)
        {
            busesAtStopListHasTraceableBuses = false;
            busDetailsLinearLayout.removeAllViews();
            busesArrivingAtSelectedStopSet.clear();
            try
            {
                if (selectedBusStopPosition == 0)
                {
                    try
                    {
                        getActivity().deleteFile(FILENAME);
                        FileOutputStream fileOutputStream = getActivity().openFileOutput(FILENAME, MODE_PRIVATE);
                        fileOutputStream.write((nearestBusStops[selectedBusStopPosition].getBusStopName() + "\n").getBytes());
                        fileOutputStream.write(buses.toString().getBytes());
                        fileOutputStream.close();
                        nearestBusStopsSpinner.setEnabled(true);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                for (int i = 0; i < buses.length(); i++)
                {
                    busesArrivingAtSelectedStopSet.add(buses.getJSONArray(i).get(3).toString().substring(buses.getJSONArray(i).get(3).toString().indexOf(":") + 1, buses.getJSONArray(i).get(3).toString().length()));
                }
                if (isNetworkAvailable())
                {
                    numberOfRefreshIconRotationsRemaining = 0;
                    for (String bus : busesArrivingAtSelectedStopSet)
                    {
                        numberOfRefreshIconRotationsRemaining++;
                        new GetBusRouteDetailsTask(this, true).execute(bus);
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
                progressDialog.dismiss();
                errorMessageTextView.setText("Could not get buses arriving at selected bus stop!");
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            progressDialog.dismiss();
            refreshFloatingActionButton.clearAnimation();
            refreshFloatingActionButton.setEnabled(true);
            nearestBusStopsSpinner.setEnabled(true);
            if (isNetworkAvailable())
            {
                errorMessageTextView.setText(R.string.error_could_not_get_buses_at_stop_text);
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
    public void onBusRouteDetailsFound(boolean isError, final Route route, boolean isForList, final String routeDirection)
    {
        progressDialog.dismiss();
        if (!isError)
        {
            busesAtStopListHasTraceableBuses = true;
            LinearLayout busRouteDetailsRowLinearLayout = new LinearLayout(getContext());
            busRouteDetailsRowLinearLayout.removeAllViews();
            busRouteDetailsRowLinearLayout.setMinimumHeight(100);
            busRouteDetailsRowLinearLayout.setBackgroundColor(Color.WHITE);
            TextView routeNumberTextView = new TextView(getContext());
            TextView routeDirectionTextView = new TextView(getContext());
            View separatorView = new View(getContext());
            ImageView imageView = new ImageView(getContext());

            imageView.setAdjustViewBounds(true);
            imageView.setPadding(10, 20, 0, 0);
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

            busRouteDetailsRowLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            busRouteDetailsRowLinearLayout.setClickable(true);
            busRouteDetailsRowLinearLayout.addView(imageView);
            busRouteDetailsRowLinearLayout.addView(routeNumberTextView);
            busRouteDetailsRowLinearLayout.addView(routeDirectionTextView);
            busDetailsLinearLayout.addView(busRouteDetailsRowLinearLayout);
            busDetailsLinearLayout.addView(separatorView);
            busDetailsLinearLayout.setVisibility(View.VISIBLE);
            busRouteDetailsRowLinearLayout.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    trackBusIntent = new Intent(getContext(), TrackBusActivity.class);
                    if (nearestBusStops[selectedBusStopPosition] != null)
                    {
                        trackBusIntent.putExtra("STOP_NAME", nearestBusStops[selectedBusStopPosition].getBusStopName());
                        trackBusIntent.putExtra("STOP_LAT", nearestBusStops[selectedBusStopPosition].getLatitude());
                        trackBusIntent.putExtra("STOP_LONG", nearestBusStops[selectedBusStopPosition].getLongitude());
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
            if (!isNetworkAvailable())
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
            if (nearestBusStopsSpinner.isEnabled())
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


    public void onStart()
    {
        updateBusList = false;
        if (googleApiClient != null)
        {
            googleApiClient.connect();
        }
        super.onStart();
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
        super.onPause();
        if (progressDialog != null && isRequestingLocationUpdates)
        {
            progressDialog.dismiss();
        }
        stopLocationUpdates();
        isRequestingLocationUpdates = false;
    }

    @Override
    public void onResume()
    {
        if (googleApiClient != null && googleApiClient.isConnected() && locationIsToBeUpdated)
        {
            int permissionCheck = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED)
            {
                createLocationRequest();
            }
        }
        super.onResume();
    }
}