package com.bangalorebuses;

import android.os.AsyncTask;

import org.json.JSONArray;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static com.bangalorebuses.Constants.NETWORK_QUERY_IO_EXCEPTION;
import static com.bangalorebuses.Constants.NETWORK_QUERY_JSON_EXCEPTION;
import static com.bangalorebuses.Constants.NETWORK_QUERY_NO_ERROR;
import static com.bangalorebuses.Constants.NETWORK_QUERY_REQUEST_TIMEOUT_EXCEPTION;
import static com.bangalorebuses.Constants.NETWORK_QUERY_URL_EXCEPTION;

class GetBusesEnDirectRouteTask extends AsyncTask<Void, Void, Void>
{
    private BusRoute busRoute;
    private String errorMessage = NETWORK_QUERY_NO_ERROR;
    private TripPlannerHelper caller;
    private ArrayList<Bus> buses = new ArrayList<>();

    GetBusesEnDirectRouteTask(TripPlannerHelper caller, BusRoute busRoute)
    {
        this.busRoute = busRoute;
        this.caller = caller;
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        String requestBody = "routeNO=" + busRoute.getBusRouteNumber() +
                "&direction=" + busRoute.getBusRouteDirection();
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
            writer.write(requestBody.getBytes());
            writer.flush();
            writer.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            while ((line = reader.readLine()) != null)
            {
                busesEnRouteResult.append(line);
            }
            reader.close();
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
                if (Integer.parseInt(jsonArray.getJSONArray(i).getString(12).replace("routeorder:", "")) <=
                        busRoute.getTripPlannerOriginBusStop().getBusStopRouteOrder())
                {
                    for (int j = 0; j < jsonArray.length(); j++)
                    {
                        if ((i + j) < jsonArray.length())
                        {
                            Bus bus = new Bus();
                            if (Integer.parseInt(jsonArray.getJSONArray(i + j).getString(12).replace("routeorder:", ""))
                                    == busRoute.getTripPlannerOriginBusStop().getBusStopRouteOrder())
                            {
                                bus.setDue(true);
                            }
                            String nearestLatLong = jsonArray.getJSONArray(i + j).getString(6).replace("nearestlatlng:", "");
                            bus.setBusLat(nearestLatLong.substring(0, nearestLatLong.indexOf(",")));
                            bus.setBusLong(nearestLatLong.substring(nearestLatLong.indexOf(",") + 1, nearestLatLong.length() - 1));
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
        super.onPostExecute(aVoid);
        busRoute.setBusRouteBuses(buses);
        caller.onBusesInServiceFound(errorMessage, busRoute);
    }
}