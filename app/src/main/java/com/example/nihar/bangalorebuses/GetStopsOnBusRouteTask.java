package com.example.nihar.bangalorebuses;

import android.os.AsyncTask;
import android.util.Log;

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
    private NetworkingCallback caller;
    private URL stopsOnRouteURL;
    private String routeId;
    private boolean errorOccurred = false;

    GetStopsOnBusRouteTask(NetworkingCallback aCaller, String inputRouteId)
    {
        caller = aCaller;
        routeId = inputRouteId;
    }

    @Override
    protected void onPreExecute()
    {
        try
        {
            stopsOnRouteURL = new URL("http://bmtcmob.hostg.in/api/tripdetails/routestop/routeid/" + routeId);
        }
        catch (MalformedURLException b)
        {
            b.printStackTrace();
            errorOccurred = true;
        }
    }

    @Override
    protected JSONArray doInBackground(Void... params)
    {
        StringBuilder result = new StringBuilder();
        JSONArray jsonArray;
        try
        {
            HttpURLConnection httpURLConnection = (HttpURLConnection) stopsOnRouteURL.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Accept", "application/json");
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
            Log.e("Network Error", "Unable to retrieve data!");
            errorOccurred = true;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
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
