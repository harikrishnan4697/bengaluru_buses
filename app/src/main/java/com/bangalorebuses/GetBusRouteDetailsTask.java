package com.bangalorebuses;

import android.os.AsyncTask;

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

class GetBusRouteDetailsTask extends AsyncTask<String, Void, Void>
{
    private NetworkingManager caller;
    private String errorMessage = NETWORK_QUERY_NO_ERROR;
    private Route route;
    private boolean isForBusList = false;
    private String routeDirection;

    GetBusRouteDetailsTask(NetworkingManager caller, boolean isForBusList)
    {
        this.caller = caller;
        this.isForBusList = isForBusList;
    }

    @Override
    protected Void doInBackground(String... routeNumber)
    {
        URL busRouteTimetableURL;
        route = new Route();
        try
        {
            if (routeNumber[0].contains("UP") || routeNumber[0].contains("DN"))
            {
                if (routeNumber[0].substring(routeNumber[0].length() - 2, routeNumber[0].length()).equals("UP"))
                {
                    routeDirection = "UP";
                }
                else
                {
                    routeDirection = "DN";
                }
            }
            routeNumber[0] = routeNumber[0].replace("UP", "").replace("DN", "");
            busRouteTimetableURL = new URL("http://bmtcmob.hostg.in/index.php/api/routetiming/timedetails/service/ord/routeno/" + routeNumber[0]);
        }
        catch (java.net.MalformedURLException e)
        {
            errorMessage = NETWORK_QUERY_URL_EXCEPTION;
            return null;
        }

        StringBuilder result = new StringBuilder();
        try
        {
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
            route.setRouteNumber(routeNumber[0]);
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