package com.bangalorebuses;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

class GetIndirectBusesTask extends AsyncTask<BusStop, Void, Void>
{
    private String errorMessage = Constants.NETWORK_QUERY_NO_ERROR;
    private NetworkingManager caller;
    private BusStop transitPoint;
    private Bus[] originToTransitPointBuses;
    private Bus[] transitPointToDestinationBuses;

    GetIndirectBusesTask(NetworkingManager caller)
    {
        this.caller = caller;
    }

    @Override
    protected Void doInBackground(BusStop... busStops)
    {
        transitPoint = busStops[1];
        URL requestURL;
        String requestBody;
        try
        {
            requestURL = new URL("http://bmtcmob.hostg.in/api/itstrips/direct");
            requestBody = "startID=" + busStops[0].getBusStopName() + "&endID=" + busStops[2].getBusStopName();
        }
        catch (MalformedURLException e)
        {
            errorMessage = Constants.NETWORK_QUERY_URL_EXCEPTION;
            return null;
        }

        HttpURLConnection client;
        String line;
        StringBuilder result = new StringBuilder();
        try
        {
            client = (HttpURLConnection) requestURL.openConnection();
            client.setRequestMethod("POST");
            client.setRequestProperty("Accept", "application/json");
            client.setDoOutput(true);
            client.setConnectTimeout(Constants.NETWORK_QUERY_CONNECT_TIMEOUT);
            client.connect();
            BufferedOutputStream writer = new BufferedOutputStream(client.getOutputStream());
            writer.write(requestBody.getBytes());
            writer.flush();
            writer.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            while ((line = reader.readLine()) != null)
            {
                result.append(line);
            }
            reader.close();
        }
        catch (java.net.SocketTimeoutException e)
        {
            errorMessage = Constants.NETWORK_QUERY_REQUEST_TIMEOUT_EXCEPTION;
            return null;
        }
        catch (java.io.IOException e)
        {
            errorMessage = Constants.NETWORK_QUERY_IO_EXCEPTION;
            return null;
        }

        try
        {
            JSONArray jsonArray = new JSONArray(result.toString());
            originToTransitPointBuses = new Bus[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject bus = jsonArray.getJSONObject(i);
                originToTransitPointBuses[i] = new Bus();
                originToTransitPointBuses[i].setRegistrationNumber(bus.getString("vehicleno"));
                originToTransitPointBuses[i].setRouteNumber(bus.getString("routeno"));
                originToTransitPointBuses[i].setServiceID(bus.getInt("serviceid"));
                originToTransitPointBuses[i].setETA(bus.getInt("ETA"));
                originToTransitPointBuses[i].setRouteOrder(bus.getInt("routeorder"));
            }
        }
        catch (JSONException e)
        {
            errorMessage = Constants.NETWORK_QUERY_JSON_EXCEPTION;
            return null;
        }

        try
        {
            requestURL = new URL("http://bmtcmob.hostg.in/api/itstrips/direct");
            requestBody = "startID=" + transitPoint.getBusStopName() + "&endID=" + busStops[2].getBusStopName();
        }
        catch (MalformedURLException e)
        {
            errorMessage = Constants.NETWORK_QUERY_URL_EXCEPTION;
            return null;
        }

        result = new StringBuilder();
        try
        {
            client = (HttpURLConnection) requestURL.openConnection();
            client.setRequestMethod("POST");
            client.setRequestProperty("Accept", "application/json");
            client.setDoOutput(true);
            client.setConnectTimeout(Constants.NETWORK_QUERY_CONNECT_TIMEOUT);
            client.connect();
            BufferedOutputStream writer = new BufferedOutputStream(client.getOutputStream());
            writer.write(requestBody.getBytes());
            writer.flush();
            writer.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            while ((line = reader.readLine()) != null)
            {
                result.append(line);
            }
            reader.close();
        }
        catch (java.net.SocketTimeoutException e)
        {
            errorMessage = Constants.NETWORK_QUERY_REQUEST_TIMEOUT_EXCEPTION;
            return null;
        }
        catch (java.io.IOException e)
        {
            errorMessage = Constants.NETWORK_QUERY_IO_EXCEPTION;
            return null;
        }

        try
        {
            JSONArray jsonArray = new JSONArray(result.toString());
            transitPointToDestinationBuses = new Bus[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject bus = jsonArray.getJSONObject(i);
                transitPointToDestinationBuses[i] = new Bus();
                transitPointToDestinationBuses[i].setRegistrationNumber(bus.getString("vehicleno"));
                transitPointToDestinationBuses[i].setRouteNumber(bus.getString("routeno"));
                transitPointToDestinationBuses[i].setServiceID(bus.getInt("serviceid"));
                transitPointToDestinationBuses[i].setETA(bus.getInt("ETA"));
                transitPointToDestinationBuses[i].setRouteOrder(bus.getInt("routeorder"));
            }
        }
        catch (JSONException e)
        {
            errorMessage = Constants.NETWORK_QUERY_JSON_EXCEPTION;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        caller.onIndirectBusesFound(errorMessage, transitPoint, originToTransitPointBuses, transitPointToDestinationBuses);
    }
}