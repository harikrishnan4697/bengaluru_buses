package com.example.nihar.bangalorebuses;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetBusesAtStopTask extends AsyncTask<String, Void, JSONArray>
{
    private NetworkingCallback caller;
    private Context callerContext;
    private ProgressDialog progressDialog;
    private URL bussesAtStopURL;
    private boolean errorOccurred = false;

    GetBusesAtStopTask(NetworkingCallback aCaller, Context aContext)
    {
        caller = aCaller;
        callerContext = aContext;
    }

    @Override
    protected void onPreExecute()
    {
        //progressDialog = ProgressDialog.show(callerContext, "", "Getting buses...", true);
        errorOccurred = false;
        try
        {
            bussesAtStopURL = new URL("http://bmtcmob.hostg.in/api/itsstopwise/details");
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
            client = (HttpURLConnection) bussesAtStopURL.openConnection();
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
            JSONArray jsonArray = new JSONArray(result.toString());
            return jsonArray;
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
        //progressDialog.hide();
        caller.onBusesAtStopFound(errorOccurred, jsonArray);
    }
}
