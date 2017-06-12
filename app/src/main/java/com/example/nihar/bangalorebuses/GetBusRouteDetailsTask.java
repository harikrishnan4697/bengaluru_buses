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

class GetBusRouteDetailsTask extends AsyncTask<String, Void, Void>
{
    private NetworkingCallback caller;
    private Context callerContext;
    private URL busRouteTimetableURL;
    private ProgressDialog progressDialog;
    private boolean errorOccurred = false;
    private Route route;
    private boolean shouldShowProgressDialog;
    private boolean isForBusList = false;

    GetBusRouteDetailsTask(NetworkingCallback caller, Context context, Boolean shouldShowProgressDialog, boolean isForBusList)
    {
        this.caller = caller;
        this.callerContext = context;
        this.shouldShowProgressDialog = shouldShowProgressDialog;
        this.isForBusList = isForBusList;
    }

    @Override
    protected void onPreExecute()
    {
        if (shouldShowProgressDialog)
        {
            progressDialog = ProgressDialog.show(callerContext, "", "Getting route details...", true);
        }
        errorOccurred = false;
    }

    @Override
    protected Void doInBackground(String... routeNumber)
    {
        route = new Route();
        try
        {
            busRouteTimetableURL = new URL("http://bmtcmob.hostg.in/index.php/api/routetiming/timedetails/service/ord/routeno/" + routeNumber[0]);
        }
        catch (java.net.MalformedURLException e)
        {
            errorOccurred = true;
            e.printStackTrace();
        }
        StringBuilder result = new StringBuilder();
        try
        {
            HttpURLConnection httpURLConnection = (HttpURLConnection) busRouteTimetableURL.openConnection();
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
            route.setRouteNumber(routeNumber[0]);
            route.setUpRouteName(jsonArray.getJSONObject(0).getString("upRouteName"));
            route.setDownRouteName((jsonArray.getJSONObject(0).getString("downRouteName")));
            route.setUpRouteId(jsonArray.getJSONArray(1).getJSONObject(0).getString("busRouteDetailId"));
            if (jsonArray.getJSONArray(2).length() == 0)
            {
                route.setDownRouteId("");
            }
            else
            {
                route.setDownRouteId(jsonArray.getJSONArray(2).getJSONObject(0).getString("busRouteDetailId"));
            }
        }
        catch (org.json.JSONException i)
        {
            errorOccurred = true;
            i.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void params)
    {
        if (shouldShowProgressDialog)
        {
            progressDialog.dismiss();
        }
        caller.onBusRouteDetailsFound(errorOccurred, route, isForBusList);
    }
}