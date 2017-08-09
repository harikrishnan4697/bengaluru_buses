package com.bangalorebuses;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.bangalorebuses.Constants.DIRECTION_DOWN;
import static com.bangalorebuses.Constants.DIRECTION_UP;
import static com.bangalorebuses.Constants.NETWORK_QUERY_IO_EXCEPTION;
import static com.bangalorebuses.Constants.NETWORK_QUERY_NO_ERROR;
import static com.bangalorebuses.Constants.db;

public class BusesArrivingAtBusStopActivity extends AppCompatActivity implements NetworkingHelper
{
    private BusStop selectedBusStop = new BusStop();
    private GetBusesArrivingAtStopTask getBusesArrivingAtStopTask;
    private GetBusRoutesDetailsDbTask getBusRoutesDetailsDbTask;
    private GetStopsOnBusRouteDbTask getStopsOnBusRouteDbTask;
    private ArrayList<String> busNumbers = new ArrayList<>();
    private ArrayList<String> busDestinations = new ArrayList<>();
    private ArrayList<String> busETAs = new ArrayList<>();
    private int numberOfBusRoutesFound = 0;
    private int numberOfBusRouteTimingsFound = 0;
    private ListView listView;
    private boolean busStopHasTraceableBuses = false;
    private LinearLayout errorLinearLayout;
    private ImageView errorImageView;
    private TextView errorTextView;
    private TextView errorResolutionTextView;
    private LinearLayout updatingBusesProgressBarLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buses_arriving_at_bus_stop);

        // Initialize some variables
        updatingBusesProgressBarLinearLayout = (LinearLayout) findViewById(R.id.updatingBusesProgressBarLinearLayout);
        listView = (ListView) findViewById(R.id.busesArrivingAtBusStopListView);
        TextView busStopDirectionInfoTextView = (TextView) findViewById(R.id.busStopDirectionNameTextView);
        errorLinearLayout = (LinearLayout) findViewById(R.id.errorLinearLayout);
        errorImageView = (ImageView) findViewById(R.id.errorImageView);
        errorTextView = (TextView) findViewById(R.id.errorTextView);
        errorResolutionTextView = (TextView) findViewById(R.id.errorResolutionTextView);
        errorResolutionTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fixError();
            }
        });
        errorLinearLayout.setVisibility(View.GONE);
        selectedBusStop.setBusStopName(getIntent().getStringExtra("BUS_STOP_NAME"));
        selectedBusStop.setBusStopDirectionName(getIntent().getStringExtra("BUS_STOP_DIRECTION_NAME"));
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setTitle(selectedBusStop.getBusStopName());
            busStopDirectionInfoTextView.setText(selectedBusStop.getBusStopDirectionName());
        }
        selectedBusStop.setBusStopId(getIntent().getIntExtra("BUS_STOP_ID", 0));

        // Get buses scheduled to arrive at the selected bus stop
        if (isNetworkAvailable())
        {
            updatingBusesProgressBarLinearLayout.setVisibility(View.VISIBLE);
            busStopHasTraceableBuses = false;
            String requestBody = "stopID=" + Integer.toString(selectedBusStop.getBusStopId());
            getBusesArrivingAtStopTask = new GetBusesArrivingAtStopTask(this);
            getBusesArrivingAtStopTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestBody);
        }
        else
        {
            updatingBusesProgressBarLinearLayout.setVisibility(View.GONE);
            listView.setVisibility(View.GONE);
            setErrorLayoutContent(R.drawable.ic_cloud_off_black, "Uh oh! No data connection.", "Retry");
            errorLinearLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.buses_arriving_at_bus_stop_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                getBusesArrivingAtStopTask.cancel(true);
                finish();
                break;
            case R.id.busesArrivingAtBusStopRefresh:
                // Get buses scheduled to arrive at the selected bus stop
                if (isNetworkAvailable())
                {
                    updatingBusesProgressBarLinearLayout.setVisibility(View.VISIBLE);
                    busStopHasTraceableBuses = false;
                    String requestBody = "stopID=" + Integer.toString(selectedBusStop.getBusStopId());
                    getBusesArrivingAtStopTask = new GetBusesArrivingAtStopTask(this);
                    getBusesArrivingAtStopTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestBody);
                }
                else
                {
                    updatingBusesProgressBarLinearLayout.setVisibility(View.GONE);
                    listView.setVisibility(View.GONE);
                    setErrorLayoutContent(R.drawable.ic_cloud_off_black, "Uh oh! No data connection.", "Retry");
                    errorLinearLayout.setVisibility(View.VISIBLE);
                }
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void fixError()
    {
        errorLinearLayout.setVisibility(View.GONE);

        // Get buses scheduled to arrive at the selected bus stop
        if (isNetworkAvailable())
        {
            updatingBusesProgressBarLinearLayout.setVisibility(View.VISIBLE);
            busStopHasTraceableBuses = false;
            String requestBody = "stopID=" + Integer.toString(selectedBusStop.getBusStopId());
            getBusesArrivingAtStopTask = new GetBusesArrivingAtStopTask(this);
            getBusesArrivingAtStopTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestBody);
        }
        else
        {
            updatingBusesProgressBarLinearLayout.setVisibility(View.GONE);
            listView.setVisibility(View.GONE);
            setErrorLayoutContent(R.drawable.ic_cloud_off_black, "Uh oh! No data connection.", "Retry");
            errorLinearLayout.setVisibility(View.VISIBLE);
        }
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

    @Override
    public void onBusStopsFound(boolean isError, JSONArray busStopsArray)
    {

    }

    @Override
    public void onBusesAtStopFound(String errorMessage, JSONArray buses)
    {
        Set<String> busNumbersSet = new HashSet<>();

        if (errorMessage.equals(NETWORK_QUERY_NO_ERROR))
        {
            try
            {
                for (int i = 0; i < buses.length(); i++)
                {
                    busNumbersSet.add(buses.getJSONArray(i).get(3).toString().substring(buses.getJSONArray(i).get(3).toString().indexOf(":") + 1, buses.getJSONArray(i).get(3).toString().length()));
                }

                if (busNumbersSet.size() == 0)
                {
                    updatingBusesProgressBarLinearLayout.setVisibility(View.GONE);
                    listView.setVisibility(View.GONE);
                    setErrorLayoutContent(R.drawable.ic_directions_bus_black_big, "Oh no! Looks like there aren't any buses arriving at this bus stop any time soon...", "Retry");
                    errorLinearLayout.setVisibility(View.VISIBLE);
                }
                else
                {
                    ArrayList<String> busNumbers = new ArrayList<>();
                    Iterator<String> iterator = busNumbersSet.iterator();
                    while (iterator.hasNext())
                    {
                        busNumbers.add(iterator.next());
                    }
                    getBusRoutesDetailsDbTask = new GetBusRoutesDetailsDbTask();
                    getBusRoutesDetailsDbTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, busNumbers);
                }
            }
            catch (JSONException e)
            {
                updatingBusesProgressBarLinearLayout.setVisibility(View.GONE);
                listView.setVisibility(View.GONE);
                setErrorLayoutContent(R.drawable.ic_directions_bus_black_big, "Sorry! Something went wrong while trying to get buses arriving at this bus stop.", "Retry");
                errorLinearLayout.setVisibility(View.VISIBLE);
            }
        }
        else if (errorMessage.equals(NETWORK_QUERY_IO_EXCEPTION))
        {
            updatingBusesProgressBarLinearLayout.setVisibility(View.GONE);
            listView.setVisibility(View.GONE);
            setErrorLayoutContent(R.drawable.ic_directions_bus_black_big, "Oh no! Looks like there aren't any buses arriving at this bus stop any time soon...", "Retry");
            errorLinearLayout.setVisibility(View.VISIBLE);
        }
        else
        {
            updatingBusesProgressBarLinearLayout.setVisibility(View.GONE);
            if (isNetworkAvailable())
            {
                updatingBusesProgressBarLinearLayout.setVisibility(View.GONE);
                listView.setVisibility(View.GONE);
                setErrorLayoutContent(R.drawable.ic_directions_bus_black_big, "Sorry! Something went wrong while trying to get buses arriving at this bus stop.", "Retry");
                errorLinearLayout.setVisibility(View.VISIBLE);
            }
            else
            {
                updatingBusesProgressBarLinearLayout.setVisibility(View.GONE);
                listView.setVisibility(View.GONE);
                setErrorLayoutContent(R.drawable.ic_cloud_off_black, "Uh oh! No data connection.", "Retry");
                errorLinearLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void onBusRoutesDetailsFound(ArrayList<BusRoute> busRoutes)
    {
        getStopsOnBusRouteDbTask = new GetStopsOnBusRouteDbTask();
        getStopsOnBusRouteDbTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, busRoutes);
    }

    private void onStopsOnBusRoutesFound(ArrayList<BusRoute> busRoutes)
    {
        numberOfBusRoutesFound = 0;
        for (BusRoute busRoute : busRoutes)
        {
            for (BusStop busStop : busRoute.getBusRouteStops())
            {
                if (busStop.getBusStopId() == selectedBusStop.getBusStopId())
                {
                    numberOfBusRoutesFound++;
                    String requestBody = "routeNO=" + busRoute.getBusRouteNumber() + "&" + "direction=" + busRoute.getBusRouteDirection();
                    new GetBusesEnRouteTask(this, busStop, busRoute).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestBody);
                }
            }
        }
    }

    @Override
    public void onBusRouteDetailsFound(String errorMessage, BusRoute route, boolean isForBusList, String routeDirection)
    {

    }

    @Override
    public void onStopsOnBusRouteFound(String errorMessage, BusStop[] busStops, BusRoute route)
    {

    }

    @Override
    public void onBusesEnRouteFound(String errorMessage, Bus[] buses, int numberOfBusesFound, BusRoute route, BusStop selectedBusStop)
    {
        numberOfBusRouteTimingsFound++;
        if (errorMessage.equals(NETWORK_QUERY_NO_ERROR))
        {
            if (numberOfBusesFound != 0)
            {
                busStopHasTraceableBuses = true;

                if (buses[0].isDue())
                {
                    addBusDetails(route, "Due");
                }
                else if (buses[0].getBusRouteOrder() == 1)
                {
                    addBusDetails(route, "At origin");
                }
                else
                {
                    for (int i = 0; i < route.getBusRouteStops().size(); i++)
                    {
                        if (buses[0].getBusRouteOrder() == route.getBusRouteStops().get(i).getBusStopRouteOrder())
                        {
                            for (int j = i; j < route.getBusRouteStops().size(); j++)
                            {
                                if (route.getBusRouteStops().get(j).getBusStopRouteOrder() == selectedBusStop.getBusStopRouteOrder())
                                {
                                    addBusDetails(route, calculateTravelTime(j - i, route.getBusRouteNumber())); // TODO getBusRouteId()
                                }
                            }
                            break;
                        }
                    }
                }
                BusesArrivingAtStopListCustomAdapter customAdapter = new BusesArrivingAtStopListCustomAdapter(this, busNumbers, busDestinations, busETAs);
                customAdapter.notifyDataSetChanged();
                listView.setAdapter(customAdapter);
            }
        }

        // Check if all the bus timings have been calculated
        if (numberOfBusRouteTimingsFound == numberOfBusRoutesFound)
        {
            updatingBusesProgressBarLinearLayout.setVisibility(View.GONE);
            if (!busStopHasTraceableBuses)
            {
                updatingBusesProgressBarLinearLayout.setVisibility(View.GONE);
                listView.setVisibility(View.GONE);
                setErrorLayoutContent(R.drawable.ic_directions_bus_black_big, "Oh no! Looks like there aren't any buses arriving at this bus stop any time soon...", "Retry");
                errorLinearLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void addBusDetails(BusRoute route, String busETA)
    {
        synchronized (BusesArrivingAtBusStopActivity.this)
        {
            busNumbers.add(route.getBusRouteNumber());
            busDestinations.add(route.getBusRouteDirectionName().substring(route.getBusRouteDirectionName().indexOf(" To ") + 1, route.getBusRouteDirectionName().length()));
            busETAs.add(busETA);
        }
    }

    @Override
    public void onTimeToBusesFound(boolean isError, Bus[] buses)
    {

    }

    private String calculateTravelTime(int numberOfBusStopsToTravel, String routeNumber)
    {
        Calendar calendar = Calendar.getInstance();
        int travelTime;

        if ((calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) || (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY))
        {
            // Check if the bus is an airport shuttle (airport shuttles take longer to travel
            // from one bus stop to another as they don't stop at all bus stops)
            if (routeNumber.contains("KIAS-"))
            {
                travelTime = numberOfBusStopsToTravel * 4;  // 4 Minutes to get from a bus stop to another for the airport shuttle weekends
            }
            else
            {
                travelTime = numberOfBusStopsToTravel * 2;  // 2 Minutes to get from a bus stop to another for other buses during weekends
            }
        }
        else if ((calendar.get(Calendar.HOUR_OF_DAY) > 7 && calendar.get(Calendar.HOUR_OF_DAY) < 11) || (calendar.get(Calendar.HOUR_OF_DAY) > 16 && calendar.get(Calendar.HOUR_OF_DAY) < 21))
        {
            // Check if the bus is an airport shuttle (airport shuttles take longer to travel
            // from one bus stop to another as they don't stop at all bus stops)
            if (routeNumber.contains("KIAS-"))
            {
                travelTime = numberOfBusStopsToTravel * 5;  // 5 Minutes to get from a bus stop to another for the airport shuttle in peak-time
            }
            else
            {
                travelTime = numberOfBusStopsToTravel * 3;  // 3 Minutes to get from a bus stop to another for other buses in peak-time
            }
        }
        else
        {
            // Check if the bus is an airport shuttle (airport shuttles take longer to travel
            // from one bus stop to another as they don't stop at all bus stops)
            if (routeNumber.contains("KIAS-"))
            {
                travelTime = numberOfBusStopsToTravel * 4;  // 4 Minutes to get from a bus stop to another for the airport shuttle
            }
            else
            {
                travelTime = (int) (numberOfBusStopsToTravel * 2.5);  // 2.5 Minutes to get from a bus stop to another for other buses
            }
        }

        String travelTimeAsText;

        if (travelTime >= 60)
        {
            int hours = travelTime / 60;

            if (hours == 1)
            {
                travelTimeAsText = hours + " hour " + travelTime % 60 + " min";
            }
            else
            {
                travelTimeAsText = hours + " hours " + travelTime % 60 + " min";
            }
        }
        else
        {
            travelTimeAsText = travelTime + " min";
        }

        return travelTimeAsText;
    }

    private void setErrorLayoutContent(int drawableResId, String errorMessage, String resolutionButtonText)
    {
        errorImageView.setImageResource(drawableResId);
        errorTextView.setText(errorMessage);
        errorResolutionTextView.setText(resolutionButtonText);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (getBusesArrivingAtStopTask != null)
        {
            getBusesArrivingAtStopTask.cancel(true);
        }
        if (getBusRoutesDetailsDbTask != null)
        {
            getBusRoutesDetailsDbTask.cancel(true);
        }
        if (getStopsOnBusRouteDbTask != null)
        {
            getStopsOnBusRouteDbTask.cancel(true);
        }
        if (getBusesArrivingAtStopTask != null)
        {
            getBusesArrivingAtStopTask.cancel(true);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (getBusesArrivingAtStopTask != null)
        {
            getBusesArrivingAtStopTask.cancel(true);
        }
    }

    private class GetBusRoutesDetailsDbTask extends AsyncTask<ArrayList<String>, Void, ArrayList<BusRoute>>
    {
        @Override
        protected ArrayList<BusRoute> doInBackground(ArrayList<String>... routeNumbers)
        {
            ArrayList<BusRoute> busRoutes = new ArrayList<>();
            for (String routeNumberWithDirection : routeNumbers[0])
            {
                String routeNumber;
                String routeDirection;

                if (routeNumberWithDirection.length() > 2 && routeNumberWithDirection.substring(
                        routeNumberWithDirection.length() - 2, routeNumberWithDirection.length()).equals(DIRECTION_UP))
                {
                    routeNumber = routeNumberWithDirection.substring(0, routeNumberWithDirection.length() - 2);
                    routeDirection = DIRECTION_UP;
                }
                else if (routeNumberWithDirection.length() > 2 && routeNumberWithDirection.substring(
                        routeNumberWithDirection.length() - 2, routeNumberWithDirection.length()).equals(DIRECTION_DOWN))
                {
                    routeNumber = routeNumberWithDirection.substring(0, routeNumberWithDirection.length() - 2);
                    routeDirection = DIRECTION_DOWN;
                }
                else
                {
                    routeNumber = routeNumberWithDirection;
                    routeDirection = DIRECTION_UP;
                }

                for (BusRoute busRoute : DbQueries.getRoutesWithNumber(db, routeNumber))
                {
                    if (busRoute.getBusRouteDirection().equals(routeDirection))
                    {
                        busRoutes.add(busRoute);
                        break;
                    }
                }
            }
            return busRoutes;
        }

        @Override
        protected void onPostExecute(ArrayList<BusRoute> busRoutes)
        {
            super.onPostExecute(busRoutes);
            onBusRoutesDetailsFound(busRoutes);
        }
    }

    private class GetStopsOnBusRouteDbTask extends AsyncTask<ArrayList<BusRoute>, Void, ArrayList<BusRoute>>
    {
        @Override
        protected ArrayList<BusRoute> doInBackground(ArrayList<BusRoute>... busRoutes)
        {
            for (BusRoute busRoute : busRoutes[0])
            {
                busRoute.setBusRouteStops(DbQueries.getStopsOnRoute(db, busRoute.getBusRouteId()));
            }
            return busRoutes[0];
        }

        @Override
        protected void onPostExecute(ArrayList<BusRoute> busRoutes)
        {
            super.onPostExecute(busRoutes);
            onStopsOnBusRoutesFound(busRoutes);
        }
    }
}