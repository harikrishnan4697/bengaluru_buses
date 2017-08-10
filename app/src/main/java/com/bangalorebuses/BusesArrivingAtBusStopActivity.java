package com.bangalorebuses;

import android.content.Context;
import android.database.Cursor;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.bangalorebuses.Constants.NETWORK_QUERY_IO_EXCEPTION;
import static com.bangalorebuses.Constants.NETWORK_QUERY_NO_ERROR;
import static com.bangalorebuses.Constants.db;

public class BusesArrivingAtBusStopActivity extends AppCompatActivity implements NetworkingHelper
{
    ArrayList<BusRoute> busRoutesToDisplay = new ArrayList<>();
    private BusStop selectedBusStop = new BusStop();
    private GetBusesArrivingAtStopTask getBusesArrivingAtStopTask;
    private GetRoutesArrivingAtStopTask getRoutesArrivingAtStopTask;
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
            getRoutesArrivingAtStopTask = new GetRoutesArrivingAtStopTask();
            getRoutesArrivingAtStopTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, selectedBusStop.getBusStopId());
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
                finish();
                break;
            case R.id.busesArrivingAtBusStopRefresh:
                errorLinearLayout.setVisibility(View.GONE);
                if (getBusesArrivingAtStopTask != null)
                {
                    getBusesArrivingAtStopTask.cancel(true);
                }
                // Get buses scheduled to arrive at the selected bus stop
                if (isNetworkAvailable())
                {

                    updatingBusesProgressBarLinearLayout.setVisibility(View.VISIBLE);
                    busStopHasTraceableBuses = false;
                    getRoutesArrivingAtStopTask = new GetRoutesArrivingAtStopTask();
                    getRoutesArrivingAtStopTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, selectedBusStop.getBusStopId());
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
        if (getBusesArrivingAtStopTask != null)
        {
            getBusesArrivingAtStopTask.cancel(true);
        }
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
        busRoutesToDisplay.clear();
        numberOfBusRoutesFound = 0;
        numberOfBusRouteTimingsFound = 0;
        for (BusRoute busRoute : busRoutes)
        {
            BusStop busStop = new BusStop();
            busStop.setBusStopRouteOrder(busRoute.getSelectedBusStopRouteOrder());
            numberOfBusRoutesFound++;
            String requestBody = "routeNO=" + busRoute.getBusRouteNumber() + "&" + "direction=" + busRoute.getBusRouteDirection();
            new GetBusesEnRouteTask(this, busStop, busRoute).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestBody);
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
    public void onBusesEnRouteFound(String errorMessage, ArrayList<Bus> buses, int numberOfBusesFound, BusRoute route, BusStop selectedBusStop)
    {
        numberOfBusRouteTimingsFound++;
        if (errorMessage.equals(NETWORK_QUERY_NO_ERROR))
        {
            if (numberOfBusesFound != 0)
            {
                ArrayList<Bus> busesOnRoute = new ArrayList<>();
                for (int i = 0; i < 1; i++)
                {
                    if (i >= buses.size())
                    {
                        break;
                    }

                    if (buses.get(i).isDue())
                    {
                        buses.get(i).setBusETA(0);
                        buses.get(i).setDue(true);
                    }
                    else if (buses.get(i).getBusRouteOrder() == 1)
                    {
                        break;
                    }
                    else
                    {
                        buses.get(i).setBusETA(calculateTravelTime(DbQueries.getNumberOfStopsBetweenRouteOrders(db,
                                route.getBusRouteId(), buses.get(i).getBusRouteOrder(),
                                selectedBusStop.getBusStopRouteOrder()), route.getBusRouteNumber()));
                    }
                    busesOnRoute.add(buses.get(i));
                }
                if (busesOnRoute.size() != 0)
                {
                    busStopHasTraceableBuses = true;
                    route.setBusRouteBuses(busesOnRoute);
                    busRoutesToDisplay.add(route);
                    Collections.sort(busRoutesToDisplay, new Comparator<BusRoute>()
                    {
                        @Override
                        public int compare(BusRoute busRoute1, BusRoute busRoute2)
                        {
                            return busRoute1.getBusRouteBuses().get(0).getBusETA() - busRoute2.getBusRouteBuses().get(0).getBusETA();
                        }

                    });
                    BusesArrivingAtStopListCustomAdapter customAdapter = new BusesArrivingAtStopListCustomAdapter(this, busRoutesToDisplay);
                    customAdapter.notifyDataSetChanged();
                    listView.setAdapter(customAdapter);
                }
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

    @Override
    public void onTimeToBusesFound(boolean isError, Bus[] buses)
    {

    }

    private int calculateTravelTime(int numberOfBusStopsToTravel, String routeNumber)
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

        return travelTime;
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
        if (getRoutesArrivingAtStopTask != null)
        {
            getRoutesArrivingAtStopTask.cancel(true);
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

    private class GetRoutesArrivingAtStopTask extends AsyncTask<Integer, Void, ArrayList<BusRoute>>
    {

        @Override
        protected ArrayList<BusRoute> doInBackground(Integer... stopId)
        {
            Cursor cursor = db.rawQuery("select Routes.RouteId, Routes.RouteNumber, Routes.RouteServiceType," +
                    " Routes.RouteDirection, Routes.RouteDirectionName, RouteStops.StopRouteOrder" +
                    " from RouteStops join Routes where RouteStops.RouteId = Routes.RouteId and " +
                    "RouteStops.StopId = " + stopId[0], null);
            ArrayList<BusRoute> routes = new ArrayList<>();
            while (cursor.moveToNext())
            {
                BusRoute route = new BusRoute();
                route.setBusRouteId(cursor.getInt(0));
                route.setBusRouteNumber(cursor.getString(1));
                route.setBusRouteServiceType(cursor.getString(2));
                route.setBusRouteDirection(cursor.getString(3));
                route.setBusRouteDirectionName(cursor.getString(4));
                route.setSelectedBusStopRouteOrder(cursor.getInt(5));
                routes.add(route);
            }
            cursor.close();
            return routes;
        }

        @Override
        protected void onPostExecute(ArrayList<BusRoute> busRoutes)
        {
            super.onPostExecute(busRoutes);
            onBusRoutesDetailsFound(busRoutes);
        }
    }
}