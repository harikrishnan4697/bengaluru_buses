package com.example.nihar.bangalorebuses;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class GetNearestBusStopsTask extends AsyncTask<URL, Void, JSONArray>
{
    private NetworkingCallback caller;
    private Context callerContext;
    private ProgressDialog progressDialog;
    private boolean errorOccurred = false;

    GetNearestBusStopsTask(NetworkingCallback aCaller, Context aContext)
    {
        caller = aCaller;
        callerContext = aContext;
    }

    @Override
    protected void onPreExecute()
    {
        progressDialog = ProgressDialog.show(callerContext, "Please wait", "Locating bus stops nearby...", true);
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null)
            {
                result.append(line);
            }
            reader.close();
        }
        catch (IOException e)
        {
            errorOccurred = true;
            Log.e("Network Error", "Unable to retrieve data!");
        }

        try
        {
            JSONArray jsonArray = new JSONArray(result.toString());
            return  jsonArray;
            /*busStop1.setBusStopName(jsonArray.getJSONObject(0).getString("StopName"));
            busStop1.setLatitude(jsonArray.getJSONObject(0).getString("StopLat"));
            busStop1.setLongitude(jsonArray.getJSONObject(0).getString("StopLong"));
            busStop2.setBusStopName(jsonArray.getJSONObject(1).getString("StopName"));
            busStop2.setLatitude(jsonArray.getJSONObject(1).getString("StopLat"));
            busStop2.setLongitude(jsonArray.getJSONObject(1).getString("StopLong"));
            busStop3.setBusStopName(jsonArray.getJSONObject(2).getString("StopName"));
            busStop3.setLatitude(jsonArray.getJSONObject(2).getString("StopLat"));
            busStop3.setLongitude(jsonArray.getJSONObject(2).getString("StopLong"));
            busStop4.setBusStopName(jsonArray.getJSONObject(3).getString("StopName"));
            busStop4.setLatitude(jsonArray.getJSONObject(3).getString("StopLat"));
            busStop4.setLongitude(jsonArray.getJSONObject(3).getString("StopLong"));*/
        }
        catch (org.json.JSONException i)
        {
            errorOccurred = true;
            i.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONArray busStops)
    {
        progressDialog.hide();
        caller.onBusStopsFound(errorOccurred, busStops);
    }
}