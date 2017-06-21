package com.bangalorebuses;

import android.os.AsyncTask;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class GetNearestBusStopsTask extends AsyncTask<URL, Void, JSONArray>
{
    private NetworkingManager caller;
    private boolean errorOccurred = false;

    GetNearestBusStopsTask(NetworkingManager aCaller)
    {
        caller = aCaller;
    }

    @Override
    protected JSONArray doInBackground(URL... urls)
    {
        StringBuilder result = new StringBuilder();
        try
        {
            HttpURLConnection httpURLConnection = (HttpURLConnection) urls[0].openConnection();
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
            errorOccurred = true;
            return null;
        }
        catch (IOException e)
        {
            errorOccurred = true;
            return null;
        }

        try
        {
            return new JSONArray(result.toString());
        }
        catch (org.json.JSONException i)
        {
            errorOccurred = true;
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONArray busStops)
    {
        caller.onBusStopsFound(errorOccurred, busStops);
    }
}