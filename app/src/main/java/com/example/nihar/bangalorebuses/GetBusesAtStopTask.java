package com.example.nihar.bangalorebuses;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class GetBusesAtStopTask extends AsyncTask<String, Void, JSONArray>
{
    private NetworkingCallback caller;
    private URL busesAtStopURL;
    private boolean errorOccurred = false;

    GetBusesAtStopTask(NetworkingCallback aCaller)
    {
        caller = aCaller;
    }

    @Override
    protected void onPreExecute()
    {
        errorOccurred = false;
        try
        {
            busesAtStopURL = new URL("http://bmtcmob.hostg.in/api/itsstopwise/details");
        }
        catch (java.net.MalformedURLException e)
        {
            errorOccurred = true;
            e.printStackTrace();
        }
    }

    @Override
    protected JSONArray doInBackground(String... params)
    {
        HttpURLConnection client;
        String line;
        StringBuilder result = new StringBuilder();
        try
        {
            client = (HttpURLConnection) busesAtStopURL.openConnection();
            client.setRequestMethod("POST");
            client.setRequestProperty("Accept", "application/json");
            client.setDoOutput(true);
            client.connect();
            BufferedOutputStream writer = new BufferedOutputStream(client.getOutputStream());
            writer.write(params[0].getBytes());
            writer.flush();
            writer.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            while ((line = reader.readLine()) != null)
            {
                result.append(line);
            }
            reader.close();
        }
        catch (java.io.IOException e)
        {
            e.printStackTrace();
            errorOccurred = true;
        }

        try
        {
            return new JSONArray(result.toString());
        }
        catch (JSONException e)
        {
            errorOccurred = true;
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONArray jsonArray)
    {
        caller.onBusesAtStopFound(errorOccurred, jsonArray);
    }
}
