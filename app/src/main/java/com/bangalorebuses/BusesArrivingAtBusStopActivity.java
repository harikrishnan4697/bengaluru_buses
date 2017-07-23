package com.bangalorebuses;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import static com.bangalorebuses.Constants.DIRECTION_DOWN;
import static com.bangalorebuses.Constants.DIRECTION_UP;
import static com.bangalorebuses.Constants.NETWORK_QUERY_IO_EXCEPTION;
import static com.bangalorebuses.Constants.NETWORK_QUERY_NO_ERROR;

public class BusesArrivingAtBusStopActivity extends AppCompatActivity implements NetworkingHelper
{
    private BusStop selectedBusStop = new BusStop();
    private ProgressDialog progressDialog;
    private GetBusesArrivingAtStopTask getBusesArrivingAtStopTask;
    private ArrayList<String> busNumbers = new ArrayList<>();
    private ArrayList<String> busDestinations = new ArrayList<>();
    private ArrayList<String> busETAs = new ArrayList<>();
    private int numberOfBusRoutesFound = 0;
    private int numberOfBusRouteTimingsFound = 0;
    private ListView listView;
    private TextView errorMessageTextView;
    private boolean busStopHasTraceableBuses = false;
    private LinearLayout updatingBusesArrivingAtBusStopProgressBarLinearLayout;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buses_arriving_at_bus_stop);

        // Initialize some variables
        countDownTimer = null;
        updatingBusesArrivingAtBusStopProgressBarLinearLayout = (LinearLayout) findViewById(R.id.updatingBusesArrivingAtStopProgressBarLinearLayout);
        updatingBusesArrivingAtBusStopProgressBarLinearLayout.setVisibility(View.GONE);
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
            progressDialog = ProgressDialog.show(this, "Please wait", "Getting buses...");
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
        numberOfBusRoutesFound = 0;
        numberOfBusRouteTimingsFound = 0;
        busNumbers.clear();
        busDestinations.clear();
        busETAs.clear();

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
                    errorMessageTextView.setText(R.string.error_no_buses_arriving_at_stop_text);
                    errorMessageTextView.setVisibility(View.VISIBLE);
                }
                else
                {
                    for (String busNumber : busNumbersSet)
                    {
                        appendLog(busNumber);
                        new GetBusRouteDetailsTask(this, true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, busNumber);
                    }
                }
            }
            catch (JSONException e)
            {
                errorMessageTextView.setText(R.string.error_getting_buses_at_stop);
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
        else if (errorMessage.equals(NETWORK_QUERY_IO_EXCEPTION))
        {
            progressDialog.dismiss();
            errorMessageTextView.setText(R.string.error_no_buses_arriving_at_stop_text);
            errorMessageTextView.setVisibility(View.VISIBLE);
        }
        else
        {
            progressDialog.dismiss();
            if (isNetworkAvailable())
            {
                errorMessageTextView.setText(R.string.error_getting_buses_at_stop);
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
            else
            {
                errorMessageTextView.setText(R.string.error_connecting_to_the_internet_text);
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onBusRouteDetailsFound(String errorMessage, Route route, boolean isForBusList, String routeDirection)
    {
        if (errorMessage.equals(NETWORK_QUERY_NO_ERROR))
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
        numberOfBusRoutesFound++;
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
        numberOfBusRouteTimingsFound++;

        if (errorMessage.equals(NETWORK_QUERY_NO_ERROR))
        {
            progressDialog.dismiss();
            if (numberOfBusesFound != 0)
            {
                busStopHasTraceableBuses = true;

                if (buses[0].getRouteOrder() == 1)
                {
                    addBusDetails(route, "At origin");
                }
                else if (buses[0].getIsDue())
                {
                    addBusDetails(route, "Due");
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
                                    addBusDetails(route, calculateTravelTime(j - i, buses[0].getServiceID()));
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
            updatingBusesArrivingAtBusStopProgressBarLinearLayout.setVisibility(View.GONE);
            if (!busStopHasTraceableBuses)
            {
                errorMessageTextView.setText(R.string.error_no_buses_arriving_at_stop_text);
                errorMessageTextView.setVisibility(View.VISIBLE);
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
                        updatingBusesArrivingAtBusStopProgressBarLinearLayout.setVisibility(View.VISIBLE);
                        String requestBody = "stopID=" + Integer.toString(BusesArrivingAtBusStopActivity.this.selectedBusStop.getBusStopId());
                        getBusesArrivingAtStopTask = new GetBusesArrivingAtStopTask(BusesArrivingAtBusStopActivity.this);
                        getBusesArrivingAtStopTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestBody);
                    }
                }.start();
            }
        }
    }

    private void addBusDetails(Route route, String busETA)
    {
        synchronized (BusesArrivingAtBusStopActivity.this)
        {
            busNumbers.add(route.getRouteNumber());
            if (route.getDirection().equals(Constants.DIRECTION_UP))
            {
                busDestinations.add(route.getUpRouteName().substring(route.getUpRouteName().indexOf(" To ") + 1, route.getUpRouteName().length()));
            }
            else
            {
                busDestinations.add(route.getDownRouteName().substring(route.getDownRouteName().indexOf(" To ") + 1, route.getDownRouteName().length()));
            }
            busETAs.add(busETA);
        }
    }

    @Override
    public void onTimeToBusesFound(boolean isError, Bus[] buses)
    {

    }

    private String calculateTravelTime(int numberOfBusStopsToTravel, int serviceId)
    {
        Calendar calendar = Calendar.getInstance();
        int travelTime;

        if ((calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) || (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY))
        {
            // Check if the bus is an airport shuttle (airport shuttles take longer to travel
            // from one bus stop to another as they don't stop at all bus stops)
            if (serviceId == 2)
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
            if (serviceId == 2)
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
            if (serviceId == 2)
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

    public void appendLog(String text)
    {
        File logFile = new File("sdcard/log.file");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause()
    {
        if(countDownTimer != null)
        {
            countDownTimer.cancel();
        }
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