package com.bangalorebuses;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This class extends AsyncTask and is used for querying the BMTC
 * server for a list of all the buses scheduled to arrive
 * at a particular bus stop.
 *
 * @author Nihar Thakkar
 * @version 1.0
 * @since 18-6-2017
 */

class GetBusesAtStopTask extends AsyncTask<String, Void, JSONArray>
{
    private NetworkingManager caller;
    private boolean errorOccurred = false;

    /**
     * This method is the constructor.
     *
     * @param aCaller This parameter is an instance of a class that
     *                implements the NetworkingManager interface.
     */
    GetBusesAtStopTask(NetworkingManager aCaller)
    {
        caller = aCaller;
    }

    /**
     * This method gets buses arriving at a specified bus stop using a separate
     * background thread.
     *
     * @param busStopIds This parameter is a String[] of bus stop ids to get buses at.
     *                   NOTE: In this case, only the 0th position parameter is used.
     * @return This returns a JSONArray of buses at the busStopIds[0] bus stop.
     */
    @Override
    protected JSONArray doInBackground(String... busStopIds)
    {
        // Create a new URL for the request
        URL busesAtStopURL;
        try
        {
            busesAtStopURL = new URL("http://bmtcmob.hostg.in/api/itsstopwise/details");
        }
        catch (java.net.MalformedURLException e)
        {
            errorOccurred = true;
            return null;
        }

        // Make the connection to the BMTC server and send the request
        HttpURLConnection client;
        String line;
        StringBuilder result = new StringBuilder();
        try
        {
            client = (HttpURLConnection) busesAtStopURL.openConnection();
            client.setRequestMethod("POST");
            client.setRequestProperty("Accept", "application/json");
            client.setDoOutput(true);
            client.setConnectTimeout(Constants.NETWORK_QUERY_CONNECT_TIMEOUT);
            client.setReadTimeout(Constants.NETWORK_QUERY_READ_TIMEOUT);
            client.connect();
            BufferedOutputStream writer = new BufferedOutputStream(client.getOutputStream());
            writer.write(busStopIds[0].getBytes());
            writer.flush();
            writer.close();
            // Store the result in 'result'
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            while ((line = reader.readLine()) != null)
            {
                result.append(line);
            }
            reader.close();
        }
        catch (java.net.SocketTimeoutException e)
        {
            errorOccurred = true;
            return null;
        }
        catch (java.io.IOException e)
        {
            errorOccurred = true;
            return null;
        }

        // Assign the result of the request to a JSONArray and then return the JSONArray
        try
        {
            return new JSONArray(result.toString());
        }
        catch (JSONException e)
        {
            errorOccurred = true;
        }
        return null;
    }

    /**
     * This method is called automatically by AsyncTask after doInBackground()
     * above is executed.
     *
     * @param jsonArray This parameter is returned by doInBackground() above
     *                  and is a JSONArray of the list of buses arriving at
     *                  a bus stop.
     */
    @Override
    protected void onPostExecute(JSONArray jsonArray)
    {
        /*
         Calls the onBusesAtStopFound() callback method defined in the
         NetworkingManager interface
        */
        caller.onBusesAtStopFound(errorOccurred, jsonArray);
    }
}
