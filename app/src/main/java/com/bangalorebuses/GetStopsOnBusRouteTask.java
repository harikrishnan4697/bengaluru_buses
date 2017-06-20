package com.bangalorebuses;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

class GetStopsOnBusRouteTask extends AsyncTask<Void, Void, JSONArray>
{
    private NetworkingManager caller;
    private URL stopsOnRouteURL;
    private String routeId;
    private boolean errorOccurred = false;

    GetStopsOnBusRouteTask(NetworkingManager aCaller, String inputRouteId)
    {
        caller = aCaller;
        routeId = inputRouteId;
    }

    @Override
    protected JSONArray doInBackground(Void... params)
    {
        try
        {
            stopsOnRouteURL = new URL("http://bmtcmob.hostg.in/api/tripdetails/routestop/routeid/" + routeId);
        }
        catch (MalformedURLException e)
        {
            errorOccurred = true;
            return null;
        }
        StringBuilder result = new StringBuilder();
        JSONArray jsonArray;
        try
        {
            HttpURLConnection httpURLConnection = (HttpURLConnection) stopsOnRouteURL.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setConnectTimeout(10000);
            httpURLConnection.setReadTimeout(30000);
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null)
            {
                result.append(line);
            }
            reader.close();
            jsonArray = new JSONArray(result.toString());
            return jsonArray;
        }
        catch (IOException e)
        {
            errorOccurred = true;
            return null;
        }
        catch (JSONException e)
        {
            errorOccurred = true;
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONArray jsonArray)
    {
        caller.onStopsOnBusRouteFound(errorOccurred, jsonArray);
    }
}