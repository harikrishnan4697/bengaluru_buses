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

/**
 * This class extends AsyncTask and is used for querying the BMTC
 * server for a list of all the buses currently available from
 * an origin bus stop to a destination bus stop.
 *
 * @author Nihar Thakkar
 * @version 1.0
 * @since 1-7-2017
 */

class GetDirectBusesTask extends AsyncTask<BusStop, Void, Bus[]>
{
    private String errorMessage = Constants.NETWORK_QUERY_NO_ERROR;
    private TripPlannerHelper caller;

    GetDirectBusesTask(TripPlannerHelper caller)
    {
        this.caller = caller;
    }

    @Override
    protected Bus[] doInBackground(BusStop... busStops)
    {
        URL requestURL;
        String requestBody;
        Bus[] buses;
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
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                buses[i] = new Bus();
                buses[i].setBusRegistrationNumber(jsonObject.getString("vehicleno"));
                buses[i].setBusRouteNumber(jsonObject.getString("routeno"));
                buses[i].setBusLat(jsonObject.getString("vehiclelat"));
                buses[i].setBusLong(jsonObject.getString("vehiclelng"));
                buses[i].setBusRouteOrder(jsonObject.getInt("routeorder"));
                buses[i].setBusETA(jsonObject.getInt("ETA"));
            }
        }
        catch (JSONException e)
        {
            errorMessage = Constants.NETWORK_QUERY_JSON_EXCEPTION;
            return null;
        }

        return buses;
    }

    @Override
    protected void onPostExecute(Bus[] buses)
    {
        caller.onDirectBusesFound(errorMessage, buses);
    }
}