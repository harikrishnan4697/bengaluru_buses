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
import java.util.ArrayList;

/**
 * This class extends AsyncTask and is used for querying the BMTC
 * server for a list of all the possible transit points between
 * the origin bus stop and the destination bus stop.
 *
 * @author Nihar Thakkar
 * @version 1.0
 * @since 2-7-2017
 */

class GetTransitPointsTask extends AsyncTask<Void, Void, ArrayList<TransitPoint>>
{
    private String errorMessage = Constants.NETWORK_QUERY_NO_ERROR;
    private IndirectTripHelper caller;
    private String originBusStopName;
    private String destinationBusStopName;

    GetTransitPointsTask(IndirectTripHelper caller, String originBusStopName, String destinationBusStopName)
    {
        this.caller = caller;
        this.originBusStopName = originBusStopName;
        this.destinationBusStopName = destinationBusStopName;
    }

    @Override
    protected ArrayList<TransitPoint> doInBackground(Void... voids)
    {
        URL requestURL;
        String requestBody;
        ArrayList<TransitPoint> transitPoints = new ArrayList<>();
        try
        {
            requestURL = new URL("http://bmtcmob.hostg.in/api/transitroute/transitpoint");
            requestBody = "startStop=" + originBusStopName + "&endStop=" + destinationBusStopName;
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
            client.disconnect();
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

            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject transitPointJSONObject = jsonArray.getJSONObject(i);

                TransitPoint transitPoint = new TransitPoint();
                transitPoint.setTransitPointName(transitPointJSONObject.getString("common_group_name"));
                transitPoints.add(transitPoint);
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
    protected void onPostExecute(ArrayList<TransitPoint> transitPoints)
    {
        super.onPostExecute(transitPoints);
        //caller.onTransitPointsWithNumberOfRoutesFound(transitPoints, "");
    }
}