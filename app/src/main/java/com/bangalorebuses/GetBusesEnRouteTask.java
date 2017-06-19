package com.bangalorebuses;

import android.os.AsyncTask;

import org.json.JSONArray;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class GetBusesEnRouteTask extends AsyncTask<String, Void, Void>
{
    private NetworkingManager caller;
    private URL busesEnRouteURL;
    private boolean errorOccurred = false;
    private BusStop nearestBusStop;
    private int numberOfBusesFound;
    private Bus[] buses = new Bus[4];

    GetBusesEnRouteTask(NetworkingManager aCaller, BusStop aBusStop)
    {
        caller = aCaller;
        nearestBusStop = aBusStop;
    }

    @Override
    protected void onPreExecute()
    {
        try
        {
            busesEnRouteURL = new URL("http://bmtcmob.hostg.in/api/itsroutewise/details");
        }
        catch (java.net.MalformedURLException e)
        {
            errorOccurred = true;
            cancel(true);
        }
    }

    @Override
    protected Void doInBackground(String... requestBody)
    {
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
        catch (java.io.IOException e)
        {
            errorOccurred = true;
            return null;
        }

        try
        {
            JSONArray jsonArray = new JSONArray(busesEnRouteResult.toString());
            for (int i = 0; i < jsonArray.length(); i++)
            {
                if (Integer.parseInt(jsonArray.getJSONArray(i).getString(12).replace("routeorder:", "")) <= nearestBusStop.getRouteOrder())
                {
                    for (int j = 0; j < 4; j++)
                    {
                        if ((i + j) < jsonArray.length())
                        {
                            if (Integer.parseInt(jsonArray.getJSONArray(i + j).getString(12).replace("routeorder:", "")) == nearestBusStop.getRouteOrder())
                            {
                                buses[j].setIsDue(true);
                            }
                            String nearestLatLong = jsonArray.getJSONArray(i + j).getString(6).replace("nearestlatlng:", "");
                            buses[j].setLatitude(nearestLatLong.substring(0, nearestLatLong.indexOf(",")));
                            buses[j].setLongitude(nearestLatLong.substring(nearestLatLong.indexOf(",") + 1, nearestLatLong.length() - 1));
                            buses[j].setRegistrationNumber(jsonArray.getJSONArray(i + j).getString(0).replace("vehicleno:", ""));
                            buses[j].setRouteOrder(Integer.parseInt(jsonArray.getJSONArray(i + j).getString(12).replace("routeorder:", "")));
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
            errorOccurred = true;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        caller.onBusesEnRouteFound(errorOccurred, buses, numberOfBusesFound);
    }
}
