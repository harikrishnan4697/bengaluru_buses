package com.bangalorebuses;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class TripPlannerFragment extends Fragment implements NetworkingManager
{
    private BusStop startBusStop = new BusStop();
    private BusStop endBusStop = new BusStop();
    private Button originButton;
    private Button destinationButton;
    private ProgressBar progressBar;
    private TextView errorMessageTextView;
    private GetDirectBusesTask getDirectBusesTask;
    private GetTransitPointsTask getTransitPointsTask;
    private GetIndirectBusesTask originToTransitPointIndirectBuses;
    private GetIndirectBusesTask transitPointToDestinationIndirectBuses;
    private ListView routeOptionsListView;
    private RelativeLayout containerRelativeLayout;
    private int numberOfNetworkRequestsMade = 0;
    private int numberOfTransitPointsFound = 0;
    private List<HashMap<String, String>> inDirectRoutesList = new ArrayList<>();
    private HashMap<String, HashMap> inDirectRoutes = new HashMap<>();
    private ImageView switchBusStopsImageView;
    private Animation rotateOnce;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.trip_planner_fragment, container, false);
        containerRelativeLayout = (RelativeLayout) view.findViewById(R.id.trip_planner_container_relative_layout);
        routeOptionsListView = (ListView) view.findViewById(R.id.route_options_list_view);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        errorMessageTextView = (TextView) view.findViewById(R.id.trip_planner_fragment_error_message_text_view);
        originButton = (Button) view.findViewById(R.id.trip_planner_origin_button);
        originButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                searchOrigin();
            }
        });
        destinationButton = (Button) view.findViewById(R.id.trip_planner_destination_button);
        destinationButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                searchDestination();
            }
        });
        rotateOnce = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_once);
        switchBusStopsImageView = (ImageView) view.findViewById(R.id.switch_bus_stops_image_view);
        switchBusStopsImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switchStartAndEndBusStops();
            }
        });
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

    private void searchOrigin()
    {
        Intent searchActivityIntent = new Intent(getContext(), SearchActivity.class);
        searchActivityIntent.putExtra("Search_Type", Constants.SEARCH_TYPE_BUS_STOP);
        startActivityForResult(searchActivityIntent, Constants.SEARCH_START_BUS_STOP_REQUEST_CODE);
    }

    private void searchDestination()
    {
        Intent searchActivityIntent = new Intent(getContext(), SearchActivity.class);
        searchActivityIntent.putExtra("Search_Type", Constants.SEARCH_TYPE_BUS_STOP);
        startActivityForResult(searchActivityIntent, Constants.SEARCH_END_BUS_STOP_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK)
        {
            if (requestCode == Constants.SEARCH_START_BUS_STOP_REQUEST_CODE)
            {
                startBusStop.setBusStopName(data.getStringExtra("Selected_Item"));
                originButton.setText(data.getStringExtra("Selected_Item"));
            }
            else if (requestCode == Constants.SEARCH_END_BUS_STOP_REQUEST_CODE)
            {
                endBusStop.setBusStopName(data.getStringExtra("Selected_Item"));
                destinationButton.setText(data.getStringExtra("Selected_Item"));
            }

            if (originButton != null && !originButton.getText().equals("") && destinationButton != null && !destinationButton.getText().equals(""))
            {
                getPossibleRoutes();
            }
        }
    }

    private void getPossibleRoutes()
    {
        if (isNetworkAvailable())
        {
            inDirectRoutes.clear();
            if (originToTransitPointIndirectBuses != null && transitPointToDestinationIndirectBuses != null)
            {
                originToTransitPointIndirectBuses.cancel(true);
                transitPointToDestinationIndirectBuses.cancel(true);
            }
            errorMessageTextView.setVisibility(View.GONE);
            routeOptionsListView.setVisibility(View.GONE);
            if (startBusStop == null || endBusStop == null || startBusStop.getBusStopName() == null || endBusStop.getBusStopName() == null || startBusStop.getBusStopName().equals("") || endBusStop.getBusStopName().equals(""))
            {
                Toast.makeText(getContext(), "Please choose a start and end bus stop!", Toast.LENGTH_SHORT).show();
                errorMessageTextView.setVisibility(View.GONE);
            }
            else
            {
                appendLog("\n\n-- Getting routes from " + startBusStop.getBusStopName() + " to " + endBusStop.getBusStopName() + " --");
                switchBusStopsImageView.setEnabled(false);
                originButton.setEnabled(false);
                destinationButton.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                getDirectBusesTask = new GetDirectBusesTask(this);
                getDirectBusesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, startBusStop, endBusStop);
            }
        }
        else
        {
            errorMessageTextView.setText(R.string.error_connecting_to_the_internet_text);
            errorMessageTextView.setVisibility(View.VISIBLE);
        }
    }

    private void switchStartAndEndBusStops()
    {
        switchBusStopsImageView.startAnimation(rotateOnce);
        BusStop backupStartBusStop = startBusStop;
        startBusStop = endBusStop;
        originButton.setText(startBusStop.getBusStopName());
        endBusStop = backupStartBusStop;
        destinationButton.setText(endBusStop.getBusStopName());
        getPossibleRoutes();
    }

    @Override
    public void onDirectBusesFound(String errorMessage, Bus[] buses)
    {
        inDirectRoutesList.clear();
        if (errorMessage.equals(Constants.NETWORK_QUERY_NO_ERROR))
        {
            appendLog("Found " + buses.length + " direct buses from " + startBusStop.getBusStopName() + " to " + endBusStop.getBusStopName());
            progressBar.setVisibility(View.GONE);
            switchBusStopsImageView.setEnabled(true);
            originButton.setEnabled(true);
            destinationButton.setEnabled(true);
            List<HashMap<String, String>> aList = new ArrayList<>();
            int numberOfDirectBusesFound = 0;

            for (int i = 0; i < buses.length; i++)
            {
                numberOfDirectBusesFound++;
                buses[i].setRouteNumber(buses[i].getRouteNumber().replace("UP", ""));
                buses[i].setRouteNumber(buses[i].getRouteNumber().replace("DN", ""));
                HashMap<String, String> hm = new HashMap<>();
                hm.put("route_number", buses[i].getRouteNumber());
                if (buses[i].getRouteOrder() == 1)
                {
                    hm.put("bus_eta", "Trip is yet to begin");
                }
                else
                {
                    if (buses[i].getETA() <= 180)
                    {
                        hm.put("bus_eta", "Due");
                    }
                    else
                    {
                        hm.put("bus_eta", Integer.toString(buses[i].getETA() / 60) + " min");
                    }
                }
                aList.add(hm);
            }

            String[] from = {"route_number", "bus_eta"};
            int[] to = {R.id.bus_number_text_view, R.id.bus_eta_text_view};
            SimpleAdapter adapter = new SimpleAdapter(getActivity().getBaseContext(), aList, R.layout.trip_planner_direct_buses_list_item, from, to);
            routeOptionsListView.setAdapter(adapter);
            routeOptionsListView.setVisibility(View.VISIBLE);
            Snackbar.make(containerRelativeLayout, "Found " + numberOfDirectBusesFound + " direct buses", Snackbar.LENGTH_LONG).show();
        }
        else
        {
            if (isNetworkAvailable())
            {
                appendLog("There aren't any direct buses from " + startBusStop.getBusStopName() + " to " + endBusStop.getBusStopName() + ". Checking for indirect routes...");
                if (errorMessage.equals(Constants.NETWORK_QUERY_IO_EXCEPTION))
                {
                    errorMessageTextView.setText("There aren't any direct routes. Looking for indirect routes...");
                    errorMessageTextView.setVisibility(View.VISIBLE);
                    getTransitPointsTask = new GetTransitPointsTask(this);
                    getTransitPointsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, startBusStop, endBusStop);
                }
                else if (errorMessage.equals(Constants.NETWORK_QUERY_URL_EXCEPTION))
                {
                    progressBar.setVisibility(View.GONE);
                    switchBusStopsImageView.setEnabled(true);
                    originButton.setEnabled(true);
                    destinationButton.setEnabled(true);
                    errorMessageTextView.setText("Something went wrong! Please click try again later.");
                    errorMessageTextView.setVisibility(View.VISIBLE);
                }
                else if (errorMessage.equals(Constants.NETWORK_QUERY_REQUEST_TIMEOUT_EXCEPTION))
                {
                    progressBar.setVisibility(View.GONE);
                    switchBusStopsImageView.setEnabled(true);
                    originButton.setEnabled(true);
                    destinationButton.setEnabled(true);
                    errorMessageTextView.setText("Connection timed out! Please try again later.");
                    errorMessageTextView.setVisibility(View.VISIBLE);
                }
                else if (errorMessage.equals(Constants.NETWORK_QUERY_JSON_EXCEPTION))
                {
                    errorMessageTextView.setText("There aren't any direct routes. Looking for indirect routes...");
                    errorMessageTextView.setVisibility(View.VISIBLE);
                    getTransitPointsTask = new GetTransitPointsTask(this);
                    getTransitPointsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, startBusStop, endBusStop);
                }
            }
            else
            {
                progressBar.setVisibility(View.GONE);
                switchBusStopsImageView.setEnabled(true);
                originButton.setEnabled(true);
                destinationButton.setEnabled(true);
                errorMessageTextView.setText(R.string.error_connecting_to_the_internet_text);
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onTransitPointsFound(String errorMessage, BusStop[] transitPoints)
    {
        if (errorMessage.equals(Constants.NETWORK_QUERY_NO_ERROR))
        {
            if (transitPoints.length != 0)
            {
                appendLog("\nFound transit points:");
                errorMessageTextView.setText("Found transit points. Locating buses...");
                errorMessageTextView.setVisibility(View.VISIBLE);
                numberOfTransitPointsFound = transitPoints.length;
                numberOfNetworkRequestsMade = 0;
                for (BusStop transitPoint : transitPoints)
                {
                    appendLog(transitPoint.getBusStopName() + ", ");
                    progressBar.setVisibility(View.VISIBLE);
                    switchBusStopsImageView.setEnabled(false);
                    originButton.setEnabled(false);
                    destinationButton.setEnabled(false);
                    inDirectRoutes.put(transitPoint.getBusStopName(), new HashMap<String, String>());
                    numberOfNetworkRequestsMade = numberOfNetworkRequestsMade + 2;
                    //Toast.makeText(getContext(), "Getting buses to and from " + transitPoint.getBusStopName(), Toast.LENGTH_SHORT).show();
                    originToTransitPointIndirectBuses = new GetIndirectBusesTask(this, Constants.ROUTE_TYPE_ORIGIN_TO_TRANSIT_POINT);
                    originToTransitPointIndirectBuses.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, startBusStop, transitPoint, transitPoint);
                    transitPointToDestinationIndirectBuses = new GetIndirectBusesTask(this, Constants.ROUTE_TYPE_TRANSIT_POINT_TO_DESTINATION);
                    transitPointToDestinationIndirectBuses.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, transitPoint, endBusStop, transitPoint);

                }
                appendLog("\nLocating buses from origin to transit point and transit point to destination...");
            }
            else
            {
                progressBar.setVisibility(View.GONE);
                switchBusStopsImageView.setEnabled(true);
                originButton.setEnabled(true);
                destinationButton.setEnabled(true);
                errorMessageTextView.setText("There aren't any direct or indirect buses from " + startBusStop.getBusStopName() + " to " + endBusStop.getBusStopName() + " right now.");
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            progressBar.setVisibility(View.GONE);
            switchBusStopsImageView.setEnabled(true);
            originButton.setEnabled(true);
            destinationButton.setEnabled(true);
            if (isNetworkAvailable())
            {
                if (errorMessage.equals(Constants.NETWORK_QUERY_IO_EXCEPTION))
                {
                    errorMessageTextView.setText("Something went wrong! Please try again later.");
                    errorMessageTextView.setVisibility(View.VISIBLE);
                }
                else if (errorMessage.equals(Constants.NETWORK_QUERY_URL_EXCEPTION))
                {
                    errorMessageTextView.setText("Something went wrong! Please try again later.");
                    errorMessageTextView.setVisibility(View.VISIBLE);
                }
                else if (errorMessage.equals(Constants.NETWORK_QUERY_REQUEST_TIMEOUT_EXCEPTION))
                {
                    errorMessageTextView.setText("Connection timed out! Please try again later.");
                    errorMessageTextView.setVisibility(View.VISIBLE);
                }
                else if (errorMessage.equals(Constants.NETWORK_QUERY_JSON_EXCEPTION))
                {
                    errorMessageTextView.setText("There aren't any direct or indirect buses from " + startBusStop.getBusStopName() + " to " + endBusStop.getBusStopName() + " right now.");
                    errorMessageTextView.setVisibility(View.VISIBLE);
                }
            }
            else
            {
                errorMessageTextView.setText(R.string.error_connecting_to_the_internet_text);
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onTransitPointBusCountFound(String errorMessage, int originToTransitPointBusCount, int transitPointToDestinationBusCount, BusStop transitPoint)
    {
        if (errorMessage.equals(Constants.NETWORK_QUERY_NO_ERROR))
        {
            numberOfTransitPointsFound--;
            if (originToTransitPointBusCount != 0 && transitPointToDestinationBusCount != 0)
            {
                inDirectRoutes.put(transitPoint.getBusStopName(), new HashMap<String, String>());
                numberOfNetworkRequestsMade++;
                Toast.makeText(getContext(), "Getting buses to and from " + transitPoint.getBusStopName(), Toast.LENGTH_SHORT).show();
                originToTransitPointIndirectBuses = new GetIndirectBusesTask(this, Constants.ROUTE_TYPE_ORIGIN_TO_TRANSIT_POINT);
                originToTransitPointIndirectBuses.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, startBusStop, transitPoint, transitPoint);
                transitPointToDestinationIndirectBuses = new GetIndirectBusesTask(this, Constants.ROUTE_TYPE_TRANSIT_POINT_TO_DESTINATION);
                transitPointToDestinationIndirectBuses.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, transitPoint, endBusStop, transitPoint);
            }
            else if (numberOfTransitPointsFound == 0 && numberOfNetworkRequestsMade == 0)
            {
                progressBar.setVisibility(View.GONE);
                switchBusStopsImageView.setEnabled(true);
                originButton.setEnabled(true);
                destinationButton.setEnabled(true);
                errorMessageTextView.setText("There aren't any direct or indirect buses from " + startBusStop.getBusStopName() + " to " + endBusStop.getBusStopName() + " right now.");
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            //TODO
        }
    }

    @Override
    public void onIndirectBusesFound(String errorMessage, Bus[] buses, BusStop transitPoint, String routeMessage)
    {
        appendLog(numberOfNetworkRequestsMade + " request responses remaining");
        numberOfNetworkRequestsMade--;
        if (errorMessage.equals(Constants.NETWORK_QUERY_NO_ERROR))
        {
            buses[0].setRouteNumber(buses[0].getRouteNumber().replace("UP", ""));
            buses[0].setRouteNumber(buses[0].getRouteNumber().replace("DN", ""));
            buses[0].setRouteNumber(buses[0].getRouteNumber().replace("UP", ""));
            buses[0].setRouteNumber(buses[0].getRouteNumber().replace("DN", ""));

            HashMap<String, String> hashMap = inDirectRoutes.get(transitPoint.getBusStopName());

            if (routeMessage.equals(Constants.ROUTE_TYPE_ORIGIN_TO_TRANSIT_POINT))
            {
                hashMap.put("transit_point_name", "Change at " + transitPoint.getBusStopName());

                hashMap.put("origin_to_transit_point_route_number", buses[0].getRouteNumber());

                if (buses[0].getRouteOrder() != 1)
                {
                    if (buses[0].getETA() <= 180)
                    {
                        hashMap.put("origin_to_transit_point_bus_eta", "Due");
                    }
                    else
                    {
                        hashMap.put("origin_to_transit_point_bus_eta", Integer.toString(buses[0].getETA() / 60) + " min");
                    }
                }
                else
                {
                    hashMap.put("origin_to_transit_point_bus_eta", "Trip is yet to begin");
                }
            }
            else if (routeMessage.equals(Constants.ROUTE_TYPE_TRANSIT_POINT_TO_DESTINATION))
            {
                hashMap.put("transit_point_to_destination_route_number", buses[0].getRouteNumber());

                if (buses[0].getRouteOrder() != 1)
                {
                    if (buses[0].getETA() <= 180)
                    {
                        hashMap.put("transit_point_to_destination_bus_eta", "Due");
                    }
                    else
                    {
                        hashMap.put("transit_point_to_destination_bus_eta", Integer.toString(buses[0].getETA() / 60) + " min");
                    }
                }
                else
                {
                    hashMap.put("transit_point_to_destination_bus_eta", "Trip is yet to begin");
                }
            }

            if (hashMap.containsKey("transit_point_name") && hashMap.containsKey("origin_to_transit_point_route_number") && hashMap.containsKey("transit_point_to_destination_route_number"))
            {
                appendLog("Found a valid indirect route");
                inDirectRoutesList.add(hashMap);
                String[] from = {"transit_point_name", "origin_to_transit_point_route_number", "origin_to_transit_point_bus_eta", "transit_point_to_destination_route_number", "transit_point_to_destination_bus_eta"};
                int[] to = {R.id.transit_point_text_view, R.id.bus_1_number_text_view, R.id.bus_1_eta_text_view, R.id.bus_2_number_text_view, R.id.bus_2_eta_text_view};
                SimpleAdapter adapter;
                if (getActivity() != null)
                {
                    adapter = new SimpleAdapter(getActivity().getBaseContext(), inDirectRoutesList, R.layout.trip_planner_route_option_list_item, from, to);
                    errorMessageTextView.setVisibility(View.GONE);
                    routeOptionsListView.setAdapter(adapter);
                    routeOptionsListView.setVisibility(View.VISIBLE);
                }
                else
                {
                    progressBar.setVisibility(View.GONE);
                    switchBusStopsImageView.setEnabled(true);
                    originButton.setEnabled(true);
                    destinationButton.setEnabled(true);
                    errorMessageTextView.setText("getActivity() returned null!");
                    errorMessageTextView.setVisibility(View.VISIBLE);
                }
            }
        }
        else
        {
            //TODO Handle errors
        }

        if (numberOfNetworkRequestsMade <= 0)
        {
            progressBar.setVisibility(View.GONE);
            switchBusStopsImageView.setEnabled(true);
            originButton.setEnabled(true);
            destinationButton.setEnabled(true);
        }
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

    /**
     * This is a callback method called by the GetNearestBusStopsTask.
     *
     * @param isError       This parameter is to convey if the task encountered an error.
     * @param busStopsArray This parameter is a JSONArray of the bus stops the task
     *                      found.
     */
    public void onBusStopsFound(boolean isError, JSONArray busStopsArray)
    {

    }

    /**
     * This is a callback method called by the GetBusRouteDetailsTask.
     *
     * @param isError        This parameter is to convey if the task encountered an error.
     * @param route          This parameter is a Route object with all the details set.
     * @param isForBusList   This parameter is returned back as it was passed to the
     *                       constructor. If true, the bus route details are for
     *                       the list of buses at route. Else, the bus route details
     *                       are for a route number the user entered manually.
     * @param routeDirection This parameter is to convey if the route number that was passed
     *                       to the task had a direction of UP or DN.
     */
    public void onBusRouteDetailsFound(boolean isError, Route route, boolean isForBusList, String routeDirection)
    {

    }

    /**
     * This is a callback method called by the GetStopsOnBusRouteTask.
     *
     * @param isError       This parameter is to convey if the task encountered an error.
     * @param stopListArray This parameter is a JSONArray of all the bus stops
     *                      for a particular route id.
     */
    public void onStopsOnBusRouteFound(boolean isError, JSONArray stopListArray)
    {

    }

    /**
     * This is a callback method called by the GetBusesEnRouteTask.
     *
     * @param isError            This parameter is to convey if the task encountered an error.
     * @param buses              This parameter is an array of buses en-route that the task found.
     * @param numberOfBusesFound This parameter is the number of en-route buses the task found.
     */
    public void onBusesEnRouteFound(boolean isError, Bus[] buses, int numberOfBusesFound)
    {

    }

    /**
     * This is a callback method called by the GetTimeToBusesTask.
     *
     * @param isError This parameter is to convey if the task encountered an error.
     * @param buses   This parameter is an array of buses with their time to bus stop set.
     */
    public void onTimeToBusesFound(boolean isError, Bus[] buses)
    {

    }

    /**
     * This is a callback method called by the GetBusesAtStopTask.
     *
     * @param isError This parameter is to convey if the task encountered an error.
     * @param buses   This parameter is a JSONArray of arriving at a bus stop.
     */
    public void onBusesAtStopFound(boolean isError, JSONArray buses)
    {

    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (getDirectBusesTask != null)
        {
            getDirectBusesTask.cancel(true);
        }
        if (getTransitPointsTask != null)
        {
            getTransitPointsTask.cancel(true);
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (getDirectBusesTask != null)
        {
            getDirectBusesTask.cancel(true);
        }
        if (getTransitPointsTask != null)
        {
            getTransitPointsTask.cancel(true);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (getDirectBusesTask != null)
        {
            getDirectBusesTask.cancel(true);
        }
        if (getTransitPointsTask != null)
        {
            getTransitPointsTask.cancel(true);
        }
    }
}