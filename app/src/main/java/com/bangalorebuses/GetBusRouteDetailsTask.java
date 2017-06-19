package com.bangalorebuses;

import android.os.AsyncTask;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class GetBusRouteDetailsTask extends AsyncTask<String, Void, Void>
{
    private NetworkingManager caller;
    private URL busRouteTimetableURL;
    private boolean errorOccurred = false;
    private Route route;
    private boolean isForBusList = false;

    GetBusRouteDetailsTask(NetworkingManager caller, boolean isForBusList)
    {
        this.caller = caller;
        this.isForBusList = isForBusList;
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
            return null;
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
            return null;
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
        catch (org.json.JSONException e)
        {
            errorOccurred = true;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void params)
    {
        caller.onBusRouteDetailsFound(errorOccurred, route, isForBusList);
    }
}