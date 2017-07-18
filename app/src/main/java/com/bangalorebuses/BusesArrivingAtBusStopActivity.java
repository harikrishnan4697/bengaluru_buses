package com.bangalorebuses;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import static com.bangalorebuses.Constants.DIRECTION_DOWN;
import static com.bangalorebuses.Constants.DIRECTION_UP;
import static com.bangalorebuses.Constants.NETWORK_QUERY_NO_ERROR;

public class BusesArrivingAtBusStopActivity extends AppCompatActivity implements NetworkingManager
{
    private BusStop selectedBusStop = new BusStop();
    private ProgressBar progressBar;
    private GetBusesArrivingAtStopTask getBusesArrivingAtStopTask;
    private ArrayList<String> busNumbers = new ArrayList<>();
    private ArrayList<String> busDestinations = new ArrayList<>();
    private ArrayList<String> busETAs = new ArrayList<>();
    private int numberOfBusRoutesFound = 0;
    private int numberOfBusRouteDetailsFound = 0;
    private ListView listView;
    private TextView errorMessageTextView;
    private boolean busStopHasTraceableBuses = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buses_arriving_at_bus_stop);

        // Initialize some variables
        progressBar = (ProgressBar) findViewById(R.id.busesArrivingAtSelectedBusStopProgressBar);
        listView = (ListView) findViewById(R.id.busesArrivingAtBusStopListView);
        TextView busStopDirectionInfoTextView = (TextView) findViewById(R.id.busStopNameInfoTextView);
        errorMessageTextView = (TextView) findViewById(R.id.busesArrivingAtBusStopErrorMessageTextView);

        selectedBusStop.setBusStopName(getIntent().getStringExtra("BUS_STOP_NAME"));
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setElevation(0);
            if (selectedBusStop.getBusStopName().contains("(") && selectedBusStop.getBusStopName().contains(")"))
            {
                getSupportActionBar().setTitle(selectedBusStop.getBusStopName().substring(0, selectedBusStop.getBusStopName().indexOf("(")));
                busStopDirectionInfoTextView.setText(selectedBusStop.getBusStopName().substring(selectedBusStop.getBusStopName().indexOf("(") + 1, selectedBusStop.getBusStopName().indexOf(")")));
            }
            else
            {
                getSupportActionBar().setTitle(selectedBusStop.getBusStopName());
            }
        }
        selectedBusStop.setBusStopId(getIntent().getIntExtra("BUS_STOP_ID", 0));

        // Get buses scheduled to arrive at the selected bus stop
        if (isNetworkAvailable())
        {
            busStopHasTraceableBuses = false;
            errorMessageTextView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            String requestBody = "stopID=" + Integer.toString(selectedBusStop.getBusStopId());
            getBusesArrivingAtStopTask = new GetBusesArrivingAtStopTask(this);
            getBusesArrivingAtStopTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestBody);
        }
        else
        {
            errorMessageTextView.setText(R.string.error_connecting_to_the_internet_text);
            errorMessageTextView.setVisibility(View.VISIBLE);
        }
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
                numberOfBusRoutesFound = busNumbersSet.size();
                for (String busNumber : busNumbersSet)
                {
                    new GetBusRouteDetailsTask(this, true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, busNumber);
                }
            }
            catch (JSONException e)
            {
                errorMessageTextView.setText("Couldn't get buses arriving at this bus stop. Please try again later...");
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            progressBar.setVisibility(View.GONE);
            if (isNetworkAvailable())
            {
                errorMessageTextView.setText(R.string.error_getting_buses_at_stop);
            }
            else
            {
                errorMessageTextView.setText(R.string.error_connecting_to_the_internet_text);
            }
        }
    }

    @Override
    public void onBusRouteDetailsFound(boolean isError, Route route, boolean isForBusList, String routeDirection)
    {
        if (!isError)
        {
            if (routeDirection.equals(Constants.DIRECTION_UP))
            {
                route.setDirection(DIRECTION_UP);
                new GetStopsOnBusRouteTask(this, route.getUpRouteId(), route).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
            else
            {
                route.setDirection(DIRECTION_DOWN);
                new GetStopsOnBusRouteTask(this, route.getDownRouteId(), route).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    @Override
    public void onStopsOnBusRouteFound(String errorMessage, BusStop[] busStops, Route route)
    {
        if (errorMessage.equals(NETWORK_QUERY_NO_ERROR))
        {
            String selectedBusStopName = selectedBusStop.getBusStopName();
            if (selectedBusStop.getBusStopName().contains("(") && selectedBusStop.getBusStopName().contains(")"))
            {
                selectedBusStopName = selectedBusStop.getBusStopName().substring(0, selectedBusStop.getBusStopName().indexOf("("));
            }

            for (BusStop busStop : busStops)
            {
                if (busStop.getBusStopName().contains("("))
                {
                    if (busStop.getBusStopName().substring(0, busStop.getBusStopName().indexOf("(")).equals(selectedBusStopName))
                    {
                        String requestBody = "routeNO=" + route.getRouteNumber() + "&" + "direction=" + route.getDirection();
                        new GetBusesEnRouteTask(this, busStop, route).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestBody);
                    }
                }
                else
                {
                    if (busStop.getBusStopName().equals(selectedBusStopName))
                    {
                        String requestBody = "routeNO=" + route.getRouteNumber() + "&" + "direction=" + route.getDirection();
                        new GetBusesEnRouteTask(this, busStop, route).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestBody);
                    }
                }
            }
        }
    }

    @Override
    public void onBusesEnRouteFound(String errorMessage, Bus[] buses, int numberOfBusesFound, Route route, BusStop selectedBusStop)
    {
        numberOfBusRouteDetailsFound++;
        if (errorMessage.equals(NETWORK_QUERY_NO_ERROR))
        {
            busStopHasTraceableBuses = true;
            busNumbers.add(route.getRouteNumber());
            if (route.getDirection().equals(Constants.DIRECTION_UP))
            {
                busDestinations.add(route.getUpRouteName().substring(route.getUpRouteName().indexOf(" To ") + 1, route.getUpRouteName().length()));
            }
            else
            {
                busDestinations.add(route.getDownRouteName().substring(route.getDownRouteName().indexOf(" To ") + 1, route.getDownRouteName().length()));
            }

            if (buses[0].getRouteOrder() == 1)
            {
                busETAs.add("at depot");
            }
            else
            {
                for (int i = 0; i < route.getBusStopsEnRoute().length; i++)
                {
                    if (buses[0].getRouteOrder() == route.getBusStopsEnRoute()[i].getRouteOrder())
                    {
                        for (int j = i; j < route.getBusStopsEnRoute().length; j++)
                        {
                            if (route.getBusStopsEnRoute()[j].getRouteOrder() == selectedBusStop.getRouteOrder())
                            {
                                Calendar calendar = Calendar.getInstance();
                                int timeToBus;

                                if ((calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) || (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY))
                                {
                                    if (route.getRouteNumber().contains("KIAS-"))
                                    {
                                        timeToBus = (j - i) * 4;
                                    }
                                    else
                                    {
                                        timeToBus = (j - i) * 2;
                                    }
                                }
                                else if ((calendar.get(Calendar.HOUR_OF_DAY) > 7 && calendar.get(Calendar.HOUR_OF_DAY) < 11) || (calendar.get(Calendar.HOUR_OF_DAY) > 16 && calendar.get(Calendar.HOUR_OF_DAY) < 21))
                                {
                                    if (route.getRouteNumber().contains("KIAS-"))
                                    {
                                        timeToBus = (j - i) * 5;
                                    }
                                    else
                                    {
                                        timeToBus = (j - i) * 3;
                                    }
                                }
                                else
                                {
                                    if (route.getRouteNumber().contains("KIAS-"))
                                    {
                                        timeToBus = (j - i) * 4;
                                    }
                                    else
                                    {
                                        timeToBus = (int) ((j - i) * 2.5);
                                    }
                                }

                                int hours = timeToBus / 60;
                                if (timeToBus >= 60)
                                {
                                    if (hours == 1)
                                    {
                                        buses[0].setTimeToBus(hours + " hour " + timeToBus % 60 + " min");
                                    }
                                    else
                                    {
                                        buses[0].setTimeToBus(hours + " hours " + timeToBus % 60 + " min");
                                    }
                                    busETAs.add(buses[0].getTimeToBus());
                                }
                                else
                                {
                                    buses[0].setTimeToBus(timeToBus + " min");
                                    busETAs.add(buses[0].getTimeToBus());
                                }
                            }
                        }
                    }
                }
            }
        }

        if (numberOfBusRouteDetailsFound == numberOfBusRoutesFound)
        {
            progressBar.setVisibility(View.GONE);
            if (busStopHasTraceableBuses)
            {
                BusesArrivingAtStopListCustomAdapter customAdapter = new BusesArrivingAtStopListCustomAdapter(this, busNumbers, busDestinations, busETAs);
                customAdapter.notifyDataSetChanged();
                listView.setAdapter(customAdapter);
            }
            else
            {
                errorMessageTextView.setText(R.string.error_could_not_get_buses_at_stop_text);
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onTimeToBusesFound(boolean isError, Bus[] buses)
    {

    }

    @Override
    protected void onStop()
    {
        super.onStop();
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
}