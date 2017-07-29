package com.bangalorebuses;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.bangalorebuses.Constants.NETWORK_QUERY_IO_EXCEPTION;
import static com.bangalorebuses.Constants.NETWORK_QUERY_JSON_EXCEPTION;
import static com.bangalorebuses.Constants.NETWORK_QUERY_NO_ERROR;
import static com.bangalorebuses.Constants.NETWORK_QUERY_REQUEST_TIMEOUT_EXCEPTION;
import static com.bangalorebuses.Constants.NETWORK_QUERY_URL_EXCEPTION;

class GetStopsWithNameDbTask extends AsyncTask<Void, Void, Void>
{
    private DbNetworkingHelper caller;
    private String errorMessage = NETWORK_QUERY_NO_ERROR;
    private BusStop busStopToSearchFor;
    private BusStop[] busStops;

    GetStopsWithNameDbTask(DbNetworkingHelper caller, BusStop busStopToSearchFor)
    {
        this.caller = caller;
        this.busStopToSearchFor = busStopToSearchFor;
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        URL stopsWithNameURL;
        try
        {
            stopsWithNameURL = new URL("http://bmtcmob.hostg.in/api/busstops/stopsearch/name/" + busStopToSearchFor.getBusStopName().replace(" ", "%20"));

        }
        catch (MalformedURLException e)
        {
            errorMessage = NETWORK_QUERY_URL_EXCEPTION;
            return null;
        }

        StringBuilder result = new StringBuilder();
        try
        {
            HttpURLConnection httpURLConnection = (HttpURLConnection) stopsWithNameURL.openConnection();
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
            errorMessage = NETWORK_QUERY_REQUEST_TIMEOUT_EXCEPTION;
            return null;
        }
        catch (IOException e)
        {
            errorMessage = NETWORK_QUERY_IO_EXCEPTION;
            return null;
        }

        try
        {
            JSONArray jsonArray = new JSONArray(result.toString());
            busStops = new BusStop[jsonArray.length()];

            if (jsonArray.length() == 0)
            {
                errorMessage = NETWORK_QUERY_JSON_EXCEPTION;
            }
            else
            {
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String busStopName = jsonObject.getString("StopName");
                    busStops[i] = new BusStop();
                    if (busStopName.contains("("))
                    {
                        busStops[i].setBusStopName(busStopName.substring(0, busStopName.indexOf("(")));
                        if (!busStops[i].getBusStopName().equals("") && busStops[i].getBusStopName().substring(busStops[i].getBusStopName().length() - 1, busStops[i].getBusStopName().length()).equals(" "))
                        {
                            busStops[i].setBusStopName(busStopName.substring(0, busStops[i].getBusStopName().length() - 1));
                        }
                        busStops[i].setBusStopDirectionName(busStopName.substring(busStopName.indexOf("("), busStopName.length()));
                    }
                    else
                    {
                        busStops[i].setBusStopName(busStopName);
                        if (!busStops[i].getBusStopName().equals("") && busStops[i].getBusStopName().substring(busStops[i].getBusStopName().length() - 1, busStops[i].getBusStopName().length()).equals(" "))
                        {
                            busStops[i].setBusStopName(busStopName.substring(0, busStops[i].getBusStopName().length() - 1));
                        }
                        busStops[i].setBusStopDirectionName("Unknown");
                    }
                    busStops[i].setBusStopId(jsonObject.getInt("StopId"));
                    busStops[i].setLatitude(jsonObject.getString("StopLat"));
                    busStops[i].setLongitude(jsonObject.getString("StopLong"));
                }
            }
        }
        catch (org.json.JSONException e)
        {
            errorMessage = NETWORK_QUERY_JSON_EXCEPTION;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        caller.onStopsWithNameDbTaskComplete(errorMessage, busStopToSearchFor, busStops);
    }
}