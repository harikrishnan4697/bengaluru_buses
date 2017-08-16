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
 * server for a list of all the possible transit points between
 * the origin bus stop and the destination bus stop.
 *
 * @author Nihar Thakkar
 * @version 1.0
 * @since 2-7-2017
 */

class GetTransitPointsTask extends AsyncTask<BusStop, Void, BusStop[]>
{
    private String errorMessage = Constants.NETWORK_QUERY_NO_ERROR;
    private TripPlannerHelper caller;

    GetTransitPointsTask(TripPlannerHelper caller)
    {
        this.caller = caller;
    }

    @Override
    protected BusStop[] doInBackground(BusStop... busStops)
    {
        URL requestURL;
        String requestBody;
        BusStop[] transitPoints;
        try
        {
            requestURL = new URL("http://bmtcmob.hostg.in/api/transitroute/transitpoint");
            requestBody = "startStop=" + busStops[0].getBusStopName() + "&endStop=" + busStops[1].getBusStopName();
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
            JSONObject jsonObject = new JSONObject(result.toString());
            JSONArray jsonArray = new JSONArray(jsonObject.getJSONArray("transit_points").toString());
            transitPoints = new BusStop[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject transitPoint = jsonArray.getJSONObject(i);
                transitPoints[i] = new BusStop();
                transitPoints[i].setBusStopName(transitPoint.getString("common_group_name"));
            }
        }
        catch (JSONException e)
        {
            errorMessage = Constants.NETWORK_QUERY_JSON_EXCEPTION;
            return null;
        }

        return transitPoints;
    }

    @Override
    protected void onPostExecute(BusStop[] transitPoints)
    {
        //caller.onTransitPointsFound(errorMessage, transitPoints);
    }
}