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

import static com.bangalorebuses.Constants.NETWORK_QUERY_NO_ERROR;
import static com.bangalorebuses.Constants.NETWORK_QUERY_URL_EXCEPTION;

class GetStopsOnBusRouteTask extends AsyncTask<Void, Void, Void>
{
    private NetworkingManager caller;
    private String routeId;
    private String errorMessage = NETWORK_QUERY_NO_ERROR;
    private BusStop[] busStops;
    private Route route;

    GetStopsOnBusRouteTask(NetworkingManager aCaller, String routeId, Route route)
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
            busStops = new BusStop[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++)
            {
                busStops[i] = new BusStop();
                busStops[i].setBusStopName(jsonArray.getJSONObject(i).getString("busStopName"));
                busStops[i].setLatitude(jsonArray.getJSONObject(i).getString("lat"));
                busStops[i].setLongitude(jsonArray.getJSONObject(i).getString("lng"));
                busStops[i].setRouteOrder(jsonArray.getJSONObject(i).getInt("routeorder"));
            }

            route.setBusStopsEnRoute(busStops);

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
        caller.onStopsOnBusRouteFound(errorMessage, busStops, route);
    }
}