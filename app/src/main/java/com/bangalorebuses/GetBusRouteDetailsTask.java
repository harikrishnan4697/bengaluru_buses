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

class GetBusRouteDetailsTask extends AsyncTask<BusRoute, Void, Void>
{
    private NetworkingHelper caller;
    private String errorMessage = NETWORK_QUERY_NO_ERROR;
    private BusRoute route;
    private boolean isForBusList = false;
    private String routeDirection;

    GetBusRouteDetailsTask(NetworkingHelper caller, boolean isForBusList)
    {
        this.caller = caller;
        this.isForBusList = isForBusList;
    }

    @Override
    protected Void doInBackground(BusRoute... routes)
    {
        URL busRouteTimetableURL;
        route = new BusRoute();
        try
        {
            if (routes[0].getBusRouteNumber().contains("UP") || routes[0].getBusRouteNumber().contains("DN"))
            {
                if (routes[0].getBusRouteNumber().substring(routes[0].getBusRouteNumber().length() - 2, routes[0].getBusRouteNumber().length()).equals("UP"))
                {
                    routeDirection = "UP";
                    routes[0].setBusRouteNumber(routes[0].getBusRouteNumber().replace("UP", ""));
                }
                else
                {
                    routeDirection = "DN";
                    routes[0].setBusRouteNumber(routes[0].getBusRouteNumber().replace("DN", ""));
                }
            }
            busRouteTimetableURL = new URL("http://bmtcmob.hostg.in/index.php/api/routetiming/timedetails/service/ord/routeno/" + routes[0].getBusRouteNumber());
        }
        catch (java.net.MalformedURLException e)
        {
            errorMessage = NETWORK_QUERY_URL_EXCEPTION;
            return null;
        }

        StringBuilder result = new StringBuilder();
        try
        {
            Log.e("GetBusRouteDetailsTask", "Getting details for route: " + routes[0].getBusRouteNumber());
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
            route.setBusRouteNumber(routes[0].getBusRouteNumber());
            route.setBusRouteServiceType(routes[0].getBusRouteServiceType());
            route.setBusRouteDirectionName(jsonArray.getJSONObject(0).getString("upRouteName"));
            //route.set((jsonArray.getJSONObject(0).getString("downRouteName")));
            route.setBusRouteId(jsonArray.getJSONArray(1).getJSONObject(0).getInt("busRouteDetailId"));
            /*if (jsonArray.getJSONArray(2).length() == 0)
            {
                route.setDownRouteId("");
            }
            else
            {
                route.setDownRouteId(jsonArray.getJSONArray(2).getJSONObject(0).getString("busRouteDetailId"));
            }*/
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
        //caller.onBusRouteDetailsFound(errorMessage, route, isForBusList, routeDirection);
    }
}