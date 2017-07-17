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
import java.util.HashSet;
import java.util.Set;

public class BusesArrivingAtBusStopActivity extends AppCompatActivity implements NetworkingManager
{
    private BusStop selectedBusStop = new BusStop();
    private ProgressBar progressBar;
    private GetBusesArrivingAtStopTask getBusesArrivingAtStopTask;
    private ArrayList<String> busNumbers = new ArrayList<>();
    private ArrayList<String> busDestinations = new ArrayList<>();
    private int numberOfBusRoutesFound = 0;
    private int numberOfBusRouteDetailsFound = 0;
    private ListView listView;
    private TextView errorMessageTextView;

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
        Set<String> busNumbers = new HashSet<>();

        if (errorMessage.equals(Constants.NETWORK_QUERY_NO_ERROR))
        {
            try
            {
                for (int i = 0; i < buses.length(); i++)
                {
                    busNumbers.add(buses.getJSONArray(i).get(3).toString().substring(buses.getJSONArray(i).get(3).toString().indexOf(":") + 1, buses.getJSONArray(i).get(3).toString().length()));
                }
                numberOfBusRoutesFound = busNumbers.size();
                for (String busNumber : busNumbers)
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
            // TODO display an error message
        }
    }

    @Override
    public void onBusRouteDetailsFound(boolean isError, Route route, boolean isForBusList, String routeDirection)
    {
        numberOfBusRouteDetailsFound++;
        if (!isError)
        {
            busNumbers.add(route.getRouteNumber());
            if (routeDirection.equals(Constants.DIRECTION_UP))
            {
                busDestinations.add(route.getUpRouteName().substring(route.getUpRouteName().indexOf(" To ") + 1, route.getUpRouteName().length()));
            }
            else
            {
                busDestinations.add(route.getDownRouteName().substring(route.getDownRouteName().indexOf(" To ") + 1, route.getDownRouteName().length()));
            }
        }
        else
        {
            // TODO handle error
        }

        if (numberOfBusRouteDetailsFound == numberOfBusRoutesFound)
        {
            NearbyBusListCustomAdapter customAdapter = new NearbyBusListCustomAdapter(this, busNumbers, busDestinations);
            customAdapter.notifyDataSetChanged();
            listView.setAdapter(customAdapter);
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStopsOnBusRouteFound(boolean isError, JSONArray stopListArray)
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