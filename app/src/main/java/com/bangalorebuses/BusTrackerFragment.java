package com.bangalorebuses;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;

import static android.app.Activity.RESULT_OK;

public class BusTrackerFragment extends Fragment implements NetworkingManager
{
    private ProgressDialog progressDialog;
    private Button busNumberSelectionButton;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.bus_tracker_fragment, container, false);
        busNumberSelectionButton = (Button) view.findViewById(R.id.choose_bus_number_button);
        busNumberSelectionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                searchBusNumber();
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

    /**
     * This method tracks a bus
     *
     * @param busNumberToTrack This parameter is the bus number to track
     */
    public void startTrackingBus(String busNumberToTrack)
    {
        View view = getActivity().getCurrentFocus();
        if (view != null)
        {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        if (isNetworkAvailable())
        {
            progressDialog = ProgressDialog.show(getContext(), "Please wait", "Getting bus details...");
            new GetBusRouteDetailsTask(this, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, busNumberToTrack);
        }
        else
        {
            // TODO
            /*errorMessageTextView.setText(R.string.error_connecting_to_the_internet_text);
            errorMessageTextView.setVisibility(View.VISIBLE);*/
        }
    }

    private void searchBusNumber()
    {
        Intent searchActivityIntent = new Intent(getContext(), SearchActivity.class);
        searchActivityIntent.putExtra("Search_Type", Constants.SEARCH_TYPE_BUS_ROUTE);
        startActivityForResult(searchActivityIntent, Constants.SEARCH_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK && requestCode == Constants.SEARCH_REQUEST_CODE)
        {
            busNumberSelectionButton.setText(data.getStringExtra("Selected_Item"));
            startTrackingBus(data.getStringExtra("Selected_Item"));
        }
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
        progressDialog.dismiss();
        if (!isError)
        {
            Intent trackBusIntent;
            //trackBusNumberEditText.setText("");
            trackBusIntent = new Intent(getContext(), TrackBusActivity.class);
            trackBusIntent.putExtra("ROUTE_NUMBER", route.getRouteNumber());
            trackBusIntent.putExtra("ROUTE_DIRECTION", "UP");
            trackBusIntent.putExtra("UP_ROUTE_ID", route.getUpRouteId());
            trackBusIntent.putExtra("UP_ROUTE_NAME", route.getUpRouteName());
            trackBusIntent.putExtra("DOWN_ROUTE_ID", route.getDownRouteId());
            trackBusIntent.putExtra("DOWN_ROUTE_NAME", route.getDownRouteName());
            startActivity(trackBusIntent);
        }
        else
        {
            if (isNetworkAvailable())
            {
                if (!isForBusList)
                {
                    Toast.makeText(getContext(), "This bus cannot be tracked.", Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                /*errorMessageTextView.setText(R.string.error_connecting_to_the_internet_click_refresh_text);
                errorMessageTextView.setVisibility(View.VISIBLE);*/
            }
        }
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
}