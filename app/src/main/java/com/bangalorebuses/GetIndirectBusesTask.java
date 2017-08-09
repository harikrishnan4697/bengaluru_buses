package com.bangalorebuses;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

class GetIndirectBusesTask extends AsyncTask<BusStop, Void, Void>
{
    private String errorMessage = Constants.NETWORK_QUERY_NO_ERROR;
    private TripPlannerHelper caller;
    private Bus[] buses;
    private BusStop transitPoint;
    private String routeMessage;

    GetIndirectBusesTask(TripPlannerHelper caller, String routeMessage)
    {
        this.caller = caller;
        this.routeMessage = routeMessage;
    }

    @Override
    protected Void doInBackground(BusStop... busStops)
    {
        transitPoint = busStops[2];
        URL requestURL;
        String requestBody;
        try
        {
            requestURL = new URL("http://bmtcmob.hostg.in/api/itstrips/direct");
            requestBody = "startID=" + busStops[0].getBusStopName() + "&endID=" + busStops[1].getBusStopName();
        }
        catch (MalformedURLException e)
        {
            errorMessage = Constants.NETWORK_QUERY_URL_EXCEPTION;
            return null;
        }

        HttpURLConnection client;
        String line;
        StringBuilder result = new StringBuilder();
        try
        {
            client = (HttpURLConnection) requestURL.openConnection();
            client.setRequestMethod("POST");
            client.setRequestProperty("Accept", "application/json");
            client.setDoOutput(true);
            client.setConnectTimeout(Constants.NETWORK_QUERY_CONNECT_TIMEOUT);
            client.connect();
            BufferedOutputStream writer = new BufferedOutputStream(client.getOutputStream());
            writer.write(requestBody.getBytes());
            writer.flush();
            writer.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            while ((line = reader.readLine()) != null)
            {
                result.append(line);
            }
            reader.close();
        }
        catch (java.net.SocketTimeoutException e)
        {
            errorMessage = Constants.NETWORK_QUERY_REQUEST_TIMEOUT_EXCEPTION;
            return null;
        }
        catch (java.io.IOException e)
        {
            errorMessage = Constants.NETWORK_QUERY_IO_EXCEPTION;
            return null;
        }

        try
        {
            JSONArray jsonArray = new JSONArray(result.toString());
            buses = new Bus[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject bus = jsonArray.getJSONObject(i);
                buses[i] = new Bus();
                buses[i].setBusRegistrationNumber(bus.getString("vehicleno"));
                buses[i].setBusRouteNumber(bus.getString("routeno"));
                buses[i].setBusRouteServiceType(bus.getString("serviceid"));
                buses[i].setBusETA(bus.getInt("ETA"));
                buses[i].setBusRouteOrder(bus.getInt("routeorder"));
            }
        }
        catch (JSONException e)
        {
            errorMessage = Constants.NETWORK_QUERY_JSON_EXCEPTION;
            return null;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        caller.onIndirectBusesFound(errorMessage, buses, transitPoint, routeMessage);
    }
}