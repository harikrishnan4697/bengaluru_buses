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
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

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
    private GetTransitPointBusCountTask getTransitPointBusCountTask;
    private ListView routeOptionsListView;
    private RelativeLayout containerRelativeLayout;
    private int numberOfValidTransitPointsFound = 0;
    private int numberOfTransitPointsFound = 0;
    private List<HashMap<String, String>> inDirectRoutesList = new ArrayList<>();
    private HashMap<String, HashMap> inDirectRoutes = new HashMap<>();

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
        startBusStop = new BusStop();
        Intent searchActivityIntent = new Intent(getContext(), SearchActivity.class);
        searchActivityIntent.putExtra("Search_Type", Constants.SEARCH_TYPE_BUS_STOP);
        startActivityForResult(searchActivityIntent, Constants.SEARCH_START_BUS_STOP_REQUEST_CODE);
    }

    private void searchDestination()
    {
        endBusStop = new BusStop();
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
            errorMessageTextView.setVisibility(View.GONE);
            routeOptionsListView.setVisibility(View.GONE);
            if (startBusStop == null || endBusStop == null || startBusStop.getBusStopName() == null || endBusStop.getBusStopName() == null || startBusStop.getBusStopName().equals("") || endBusStop.getBusStopName().equals(""))
            {
                Toast.makeText(getContext(), "Please choose a start and end bus stop!", Toast.LENGTH_SHORT).show();
                errorMessageTextView.setVisibility(View.GONE);
            }
            else
            {
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

    @Override
    public void onDirectBusesFound(String errorMessage, Bus[] buses)
    {
        inDirectRoutesList.clear();
        if (errorMessage.equals(Constants.NETWORK_QUERY_NO_ERROR))
        {
            progressBar.setVisibility(View.GONE);
            originButton.setEnabled(true);
            destinationButton.setEnabled(true);
            List<HashMap<String, String>> aList = new ArrayList<>();
            int numberOfDirectBusesFound = 0;

            for (int i = 0; i < buses.length; i++)
            {
                if (buses[i].getRouteOrder() == 1)
                {
                    break;
                }
                numberOfDirectBusesFound++;
                buses[i].setRouteNumber(buses[i].getRouteNumber().replace("UP", ""));
                buses[i].setRouteNumber(buses[i].getRouteNumber().replace("DN", ""));
                HashMap<String, String> hm = new HashMap<>();
                hm.put("route_number", buses[i].getRouteNumber());
                if (buses[i].getETA() <= 180)
                {
                    hm.put("bus_eta", "Due");
                }
                else
                {
                    hm.put("bus_eta", Integer.toString(buses[i].getETA() / 60) + " min");
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
                if (errorMessage.equals(Constants.NETWORK_QUERY_IO_EXCEPTION))
                {
                    getTransitPointsTask = new GetTransitPointsTask(this);
                    getTransitPointsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, startBusStop, endBusStop);
                }
                else if (errorMessage.equals(Constants.NETWORK_QUERY_URL_EXCEPTION))
                {
                    progressBar.setVisibility(View.GONE);
                    originButton.setEnabled(true);
                    destinationButton.setEnabled(true);
                    errorMessageTextView.setText("Something went wrong! Please click try again later.");
                    errorMessageTextView.setVisibility(View.VISIBLE);
                }
                else if (errorMessage.equals(Constants.NETWORK_QUERY_REQUEST_TIMEOUT_EXCEPTION))
                {
                    progressBar.setVisibility(View.GONE);
                    originButton.setEnabled(true);
                    destinationButton.setEnabled(true);
                    errorMessageTextView.setText("Connection timed out! Please try again later.");
                    errorMessageTextView.setVisibility(View.VISIBLE);
                }
                else if (errorMessage.equals(Constants.NETWORK_QUERY_JSON_EXCEPTION))
                {
                    getTransitPointsTask = new GetTransitPointsTask(this);
                    getTransitPointsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, startBusStop, endBusStop);
                }
            }
            else
            {
                progressBar.setVisibility(View.GONE);
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
                numberOfTransitPointsFound = transitPoints.length;
                for (BusStop transitPoint : transitPoints)
                {
                    new GetTransitPointBusCountTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, startBusStop, transitPoint, endBusStop);
                }
            }
            else
            {
                progressBar.setVisibility(View.GONE);
                originButton.setEnabled(true);
                destinationButton.setEnabled(true);
                errorMessageTextView.setText("There aren't any direct or indirect buses from " + startBusStop.getBusStopName() + " to " + endBusStop.getBusStopName() + " right now.");
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            progressBar.setVisibility(View.GONE);
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
            if (originToTransitPointBusCount != 0 && transitPointToDestinationBusCount != 0)
            {
                numberOfValidTransitPointsFound++;
                Toast.makeText(getContext(), "Getting buses to and from " + transitPoint.getBusStopName(), Toast.LENGTH_LONG).show();
                new GetIndirectBusesTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, startBusStop, transitPoint, endBusStop);
            }
        }
        else
        {
            //TODO
        }
    }

    @Override
    public void onIndirectBusesFound(String errorMessage, BusStop transitPoint, Bus[] originToTransitPointBuses, Bus[] transitPointToDestinationBuses)
    {
        numberOfValidTransitPointsFound--;
        if (errorMessage.equals(Constants.NETWORK_QUERY_NO_ERROR))
        {
            originToTransitPointBuses[0].setRouteNumber(originToTransitPointBuses[0].getRouteNumber().replace("UP", ""));
            originToTransitPointBuses[0].setRouteNumber(originToTransitPointBuses[0].getRouteNumber().replace("DN", ""));
            transitPointToDestinationBuses[0].setRouteNumber(transitPointToDestinationBuses[0].getRouteNumber().replace("UP", ""));
            transitPointToDestinationBuses[0].setRouteNumber(transitPointToDestinationBuses[0].getRouteNumber().replace("DN", ""));

            HashMap<String, String> hashMap = new HashMap<>();

            hashMap.put("transit_point_name", "Change at " + transitPoint.getBusStopName());

            hashMap.put("origin_to_transit_point_route_number", originToTransitPointBuses[0].getRouteNumber());

            if (originToTransitPointBuses[0].getRouteOrder() != 1)
            {
                if (originToTransitPointBuses[0].getETA() <= 180)
                {
                    hashMap.put("origin_to_transit_point_bus_eta", "Due");
                }
                else
                {
                    hashMap.put("origin_to_transit_point_bus_eta", Integer.toString(originToTransitPointBuses[0].getETA() / 60) + " min");
                }
            }
            else
            {
                hashMap.put("origin_to_transit_point_bus_eta", "Trip is yet to begin");
            }

            hashMap.put("transit_point_to_destination_route_number", transitPointToDestinationBuses[0].getRouteNumber());
            if (transitPointToDestinationBuses[0].getRouteOrder() != 1)
            {
                if (transitPointToDestinationBuses[0].getETA() <= 180)
                {
                    hashMap.put("transit_point_to_destination_bus_eta", "Due");
                }
                else
                {
                    hashMap.put("transit_point_to_destination_bus_eta", Integer.toString(transitPointToDestinationBuses[0].getETA() / 60) + " min");
                }
            }
            else
            {
                hashMap.put("transit_point_to_destination_bus_eta", "Trip is yet to begin");
            }

            inDirectRoutesList.add(hashMap);


            String[] from = {"transit_point_name", "origin_to_transit_point_route_number", "origin_to_transit_point_bus_eta", "transit_point_to_destination_route_number", "transit_point_to_destination_bus_eta"};
            int[] to = {R.id.transit_point_text_view, R.id.bus_1_number_text_view, R.id.bus_1_eta_text_view, R.id.bus_2_number_text_view, R.id.bus_2_eta_text_view};

            SimpleAdapter adapter = new SimpleAdapter(getActivity().getBaseContext(), inDirectRoutesList, R.layout.trip_planner_route_option_list_item, from, to);
            routeOptionsListView.setAdapter(adapter);
            routeOptionsListView.setVisibility(View.VISIBLE);

            if (numberOfValidTransitPointsFound <= 0)
            {
                progressBar.setVisibility(View.GONE);
                originButton.setEnabled(true);
                destinationButton.setEnabled(true);
            }
        }
        else
        {
            //TODO Handle errors
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