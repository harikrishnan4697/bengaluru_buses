package com.bangalorebuses;

import android.os.AsyncTask;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static com.bangalorebuses.Constants.NETWORK_QUERY_IO_EXCEPTION;
import static com.bangalorebuses.Constants.NETWORK_QUERY_JSON_EXCEPTION;
import static com.bangalorebuses.Constants.NETWORK_QUERY_NO_ERROR;
import static com.bangalorebuses.Constants.NETWORK_QUERY_REQUEST_TIMEOUT_EXCEPTION;
import static com.bangalorebuses.Constants.NETWORK_QUERY_URL_EXCEPTION;

class GetBusRouteDetailsDbTask extends AsyncTask<Void, Void, Void>
{
    private DbNetworkingHelper caller;
    private String errorMessage = NETWORK_QUERY_NO_ERROR;
    private Route route;

    GetBusRouteDetailsDbTask(DbNetworkingHelper caller, Route route)
    {
        this.caller = caller;
        this.route = route;
    }

    @Override
    protected Void doInBackground(Void... voids)
    {
        URL busRouteTimetableURL;
        try
        {
            busRouteTimetableURL = new URL("http://bmtcmob.hostg.in/index.php/api/routetiming/timedetails/service/ord/routeno/" + route.getRouteNumber());
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
            route.setUpRouteName(jsonArray.getJSONObject(0).getString("upRouteName"));
            route.setDownRouteName((jsonArray.getJSONObject(0).getString("downRouteName")));

            route.setUpRouteId(jsonArray.getJSONArray(1).getJSONObject(0).getString("busRouteDetailId"));
            ArrayList<String> upRouteTimings = new ArrayList<>();
            for(int i = 0; i < jsonArray.getJSONArray(1).length(); i++)
            {
                upRouteTimings.add(jsonArray.getJSONArray(1).getJSONObject(i).getString("time"));
            }
            route.setUpTimings(upRouteTimings);

            if (jsonArray.getJSONArray(2).length() == 0)
            {
                route.setDownRouteId("");
            }
            else
            {
                route.setDownRouteId(jsonArray.getJSONArray(2).getJSONObject(0).getString("busRouteDetailId"));
                ArrayList<String> downRouteTimings = new ArrayList<>();
                for(int i = 0; i < jsonArray.getJSONArray(2).length(); i++)
                {
                    downRouteTimings.add(jsonArray.getJSONArray(2).getJSONObject(i).getString("time"));
                }
                route.setDownTimings(downRouteTimings);
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
        caller.onBusRouteDetailsDbTaskComplete(errorMessage, route);
    }
}