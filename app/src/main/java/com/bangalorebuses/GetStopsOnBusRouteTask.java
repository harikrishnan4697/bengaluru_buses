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
import java.util.ArrayList;

import static com.bangalorebuses.Constants.NETWORK_QUERY_NO_ERROR;
import static com.bangalorebuses.Constants.NETWORK_QUERY_URL_EXCEPTION;

class GetStopsOnBusRouteTask extends AsyncTask<Void, Void, Void>
{
    private NetworkingHelper caller;
    private String routeId;
    private String errorMessage = NETWORK_QUERY_NO_ERROR;
    private ArrayList<BusStop> busStops;
    private BusRoute route;

    GetStopsOnBusRouteTask(NetworkingHelper aCaller, String routeId, BusRoute route)
    {
        caller = aCaller;
        this.routeId = routeId;
        this.route = route;
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        URL stopsOnRouteURL;
        try
        {
            stopsOnRouteURL = new URL("http://bmtcmob.hostg.in/api/tripdetails/routestop/routeid/" + routeId);
        }
        catch (MalformedURLException e)
        {
            errorMessage = NETWORK_QUERY_URL_EXCEPTION;
            return null;
        }

        StringBuilder result = new StringBuilder();
        JSONArray jsonArray;
        try
        {
            HttpURLConnection httpURLConnection = (HttpURLConnection) stopsOnRouteURL.openConnection();
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

            jsonArray = new JSONArray(result.toString());
            busStops = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++)
            {
                BusStop busStop = new BusStop();
                busStop.setBusStopName(jsonArray.getJSONObject(i).getString("busStopName"));
                busStop.setBusStopLat(jsonArray.getJSONObject(i).getString("lat"));
                busStop.setBusStopLong(jsonArray.getJSONObject(i).getString("lng"));
                busStop.setBusStopRouteOrder(jsonArray.getJSONObject(i).getInt("routeorder"));
                busStops.add(busStop);
            }

            route.setBusRouteStops(busStops);

            return null;
        }
        catch (IOException e)
        {
            errorMessage = NETWORK_QUERY_URL_EXCEPTION;
            return null;
        }
        catch (JSONException e)
        {
            errorMessage = NETWORK_QUERY_URL_EXCEPTION;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void params)
    {
        BusStop[] busStops1 = new BusStop[0]; // Fake array to make the error go away
        caller.onStopsOnBusRouteFound(errorMessage, busStops1, route);
    }
}