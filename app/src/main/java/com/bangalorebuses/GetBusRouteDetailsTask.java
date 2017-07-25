package com.bangalorebuses;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.bangalorebuses.Constants.NETWORK_QUERY_IO_EXCEPTION;
import static com.bangalorebuses.Constants.NETWORK_QUERY_JSON_EXCEPTION;
import static com.bangalorebuses.Constants.NETWORK_QUERY_NO_ERROR;
import static com.bangalorebuses.Constants.NETWORK_QUERY_REQUEST_TIMEOUT_EXCEPTION;
import static com.bangalorebuses.Constants.NETWORK_QUERY_URL_EXCEPTION;

class GetBusRouteDetailsTask extends AsyncTask<Route, Void, Void>
{
    private NetworkingHelper caller;
    private String errorMessage = NETWORK_QUERY_NO_ERROR;
    private Route route;
    private boolean isForBusList = false;
    private String routeDirection;

    GetBusRouteDetailsTask(NetworkingHelper caller, boolean isForBusList)
    {
        this.caller = caller;
        this.isForBusList = isForBusList;
    }

    @Override
    protected Void doInBackground(Route... routes)
    {
        URL busRouteTimetableURL;
        route = new Route();
        try
        {
            if (routes[0].getRouteNumber().contains("UP") || routes[0].getRouteNumber().contains("DN"))
            {
                if (routes[0].getRouteNumber().substring(routes[0].getRouteNumber().length() - 2, routes[0].getRouteNumber().length()).equals("UP"))
                {
                    routeDirection = "UP";
                    routes[0].setRouteNumber(routes[0].getRouteNumber().replace("UP", ""));
                }
                else
                {
                    routeDirection = "DN";
                    routes[0].setRouteNumber(routes[0].getRouteNumber().replace("DN", ""));
                }
            }
            busRouteTimetableURL = new URL("http://bmtcmob.hostg.in/index.php/api/routetiming/timedetails/service/ord/routeno/" + routes[0].getRouteNumber());
        }
        catch (java.net.MalformedURLException e)
        {
            errorMessage = NETWORK_QUERY_URL_EXCEPTION;
            return null;
        }

        StringBuilder result = new StringBuilder();
        try
        {
            Log.e("GetBusRouteDetailsTask", "Getting details for route: " + routes[0].getRouteNumber());
            HttpURLConnection httpURLConnection = (HttpURLConnection) busRouteTimetableURL.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setConnectTimeout(Constants.NETWORK_QUERY_CONNECT_TIMEOUT);
            httpURLConnection.setReadTimeout(Constants.NETWORK_QUERY_READ_TIMEOUT);
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null)
            {
                result.append(line);
            }
            reader.close();
        }
        catch (java.net.SocketTimeoutException e)
        {
            errorMessage = NETWORK_QUERY_REQUEST_TIMEOUT_EXCEPTION;
            return null;
        }
        catch (IOException e)
        {
            errorMessage = NETWORK_QUERY_IO_EXCEPTION;
            return null;
        }

        try
        {
            JSONArray jsonArray = new JSONArray(result.toString());
            route.setRouteNumber(routes[0].getRouteNumber());
            route.setServiceType(routes[0].getServiceType());
            route.setUpRouteName(jsonArray.getJSONObject(0).getString("upRouteName"));
            route.setDownRouteName((jsonArray.getJSONObject(0).getString("downRouteName")));
            route.setUpRouteId(jsonArray.getJSONArray(1).getJSONObject(0).getString("busRouteDetailId"));
            if (jsonArray.getJSONArray(2).length() == 0)
            {
                route.setDownRouteId("");
            }
            else
            {
                route.setDownRouteId(jsonArray.getJSONArray(2).getJSONObject(0).getString("busRouteDetailId"));
            }
        }
        catch (org.json.JSONException e)
        {
            errorMessage = NETWORK_QUERY_JSON_EXCEPTION;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void params)
    {
        caller.onBusRouteDetailsFound(errorMessage, route, isForBusList, routeDirection);
    }
}