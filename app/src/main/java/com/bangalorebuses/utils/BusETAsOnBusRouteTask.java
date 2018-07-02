package com.bangalorebuses.utils;

import android.os.AsyncTask;

import com.bangalorebuses.core.Bus;
import com.bangalorebuses.core.BusRoute;

import org.json.JSONArray;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static com.bangalorebuses.utils.Constants.NETWORK_QUERY_IO_EXCEPTION;
import static com.bangalorebuses.utils.Constants.NETWORK_QUERY_JSON_EXCEPTION;
import static com.bangalorebuses.utils.Constants.NETWORK_QUERY_NO_ERROR;
import static com.bangalorebuses.utils.Constants.NETWORK_QUERY_REQUEST_TIMEOUT_EXCEPTION;
import static com.bangalorebuses.utils.Constants.NETWORK_QUERY_URL_EXCEPTION;

public class BusETAsOnBusRouteTask extends AsyncTask<String, Void, Void>
{
    private NetworkingHelper caller;
    private String errorMessage = NETWORK_QUERY_NO_ERROR;
    private ArrayList<Bus> buses = new ArrayList<>();
    private BusRoute busRoute;
    private int busStopRouteOrder;


    public BusETAsOnBusRouteTask(NetworkingHelper caller, int busStopRouteOrder, BusRoute busRoute)
    {
        this.caller = caller;
        this.busStopRouteOrder = busStopRouteOrder;
        this.busRoute = busRoute;
    }

    @Override
    protected Void doInBackground(String... requestBody)
    {
        URL busesEnRouteURL;
        try
        {
            busesEnRouteURL = new URL("http://bmtcmob.hostg.in/api/itsroutewise/details");
        }
        catch (java.net.MalformedURLException e)
        {
            errorMessage = NETWORK_QUERY_URL_EXCEPTION;
            return null;
        }

        HttpURLConnection client;
        String line;
        StringBuilder busesEnRouteResult = new StringBuilder();
        try
        {
            client = (HttpURLConnection) busesEnRouteURL.openConnection();
            client.setRequestMethod("POST");
            client.setRequestProperty("Accept", "application/json");
            client.setDoOutput(true);
            client.setConnectTimeout(Constants.NETWORK_QUERY_CONNECT_TIMEOUT);
            client.setReadTimeout(Constants.NETWORK_QUERY_READ_TIMEOUT);
            client.connect();
            BufferedOutputStream writer = new BufferedOutputStream(client.getOutputStream());
            writer.write(requestBody[0].getBytes());
            writer.flush();
            writer.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            while ((line = reader.readLine()) != null)
            {
                busesEnRouteResult.append(line);
            }
            reader.close();
            client.disconnect();
        }
        catch (java.net.SocketTimeoutException e)
        {
            errorMessage = NETWORK_QUERY_REQUEST_TIMEOUT_EXCEPTION;
            return null;
        }
        catch (java.io.IOException e)
        {
            errorMessage = NETWORK_QUERY_IO_EXCEPTION;
            return null;
        }

        try
        {
            JSONArray jsonArray = new JSONArray(busesEnRouteResult.toString());
            for (int i = 0; i < jsonArray.length(); i++)
            {
                if (!isCancelled())
                {
                    if (Integer.parseInt(jsonArray.getJSONArray(i).getString(12).replace("routeorder:", "")) <= busStopRouteOrder)
                    {
                        for (int j = 0; j < jsonArray.length(); j++)
                        {
                            if ((i + j) < jsonArray.length())
                            {
                                Bus bus = new Bus();
                                if (Integer.parseInt(jsonArray.getJSONArray(i + j).getString(12).replace("routeorder:", "")) == busStopRouteOrder)
                                {
                                    bus.setDue(true);
                                }
                                bus.setBusRegistrationNumber(jsonArray.getJSONArray(i + j).getString(0).replace("vehicleno:", ""));
                                bus.setBusRouteOrder(Integer.parseInt(jsonArray.getJSONArray(i + j).getString(12).replace("routeorder:", "")));
                                bus.setBusRoute(busRoute);
                                buses.add(bus);
                            }
                            else
                            {
                                break;
                            }
                        }
                        break;
                    }
                }
                else
                {
                    break;
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
        if (!isCancelled())
        {
            caller.onBusETAsOnBusRouteFound(errorMessage, busStopRouteOrder, buses, busRoute);
        }
    }
}
