package com.bangalorebuses;

import android.os.AsyncTask;

import org.json.JSONArray;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.bangalorebuses.Constants.NETWORK_QUERY_IO_EXCEPTION;
import static com.bangalorebuses.Constants.NETWORK_QUERY_JSON_EXCEPTION;
import static com.bangalorebuses.Constants.NETWORK_QUERY_NO_ERROR;
import static com.bangalorebuses.Constants.NETWORK_QUERY_REQUEST_TIMEOUT_EXCEPTION;
import static com.bangalorebuses.Constants.NETWORK_QUERY_URL_EXCEPTION;

class GetBusesEnRouteTask extends AsyncTask<String, Void, Void>
{
    private NetworkingHelper caller;
    private String errorMessage = NETWORK_QUERY_NO_ERROR;
    private BusStop selectedBusStop;
    private int numberOfBusesFound;
    private Bus[] buses = new Bus[4];
    private Route route;


    GetBusesEnRouteTask(NetworkingHelper caller, BusStop busStop, Route route)
    {
        this.caller = caller;
        this.selectedBusStop = busStop;
        this.route = route;
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

        buses[0] = new Bus();
        buses[1] = new Bus();
        buses[2] = new Bus();
        buses[3] = new Bus();
        numberOfBusesFound = 0;
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
                if (Integer.parseInt(jsonArray.getJSONArray(i).getString(12).replace("routeorder:", "")) <= selectedBusStop.getRouteOrder())
                {
                    for (int j = 0; j < 4; j++)
                    {
                        if ((i + j) < jsonArray.length())
                        {
                            if (Integer.parseInt(jsonArray.getJSONArray(i + j).getString(12).replace("routeorder:", "")) == selectedBusStop.getRouteOrder())
                            {
                                buses[j].setIsDue(true);
                            }
                            String nearestLatLong = jsonArray.getJSONArray(i + j).getString(6).replace("nearestlatlng:", "");
                            buses[j].setLatitude(nearestLatLong.substring(0, nearestLatLong.indexOf(",")));
                            buses[j].setLongitude(nearestLatLong.substring(nearestLatLong.indexOf(",") + 1, nearestLatLong.length() - 1));
                            buses[j].setRegistrationNumber(jsonArray.getJSONArray(i + j).getString(0).replace("vehicleno:", ""));
                            buses[j].setRouteOrder(Integer.parseInt(jsonArray.getJSONArray(i + j).getString(12).replace("routeorder:", "")));
                            buses[j].setServiceID(Integer.parseInt(jsonArray.getJSONArray(i + j).getString(5).replace("serviceid:", "")));
                            numberOfBusesFound++;
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
        caller.onBusesEnRouteFound(errorMessage, buses, numberOfBusesFound, route, selectedBusStop);
    }
}
