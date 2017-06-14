package com.example.nihar.bangalorebuses;

import android.os.AsyncTask;

import org.json.JSONArray;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class GetBusesEnRouteTask extends AsyncTask<String, Void, Void>
{
    private NetworkingCallback caller;
    private URL busesEnRouteURL;
    private boolean errorOccurred = false;
    private BusStop nearestBusStop;
    private int numberOfBusesFound;
    private Bus bus1;
    private Bus bus2;
    private Bus bus3;
    private Bus bus4;

    GetBusesEnRouteTask(NetworkingCallback aCaller, BusStop aBusStop)
    {
        caller = aCaller;
        nearestBusStop = aBusStop;
    }

    @Override
    protected void onPreExecute()
    {
        errorOccurred = false;
        try
        {
            busesEnRouteURL = new URL("http://bmtcmob.hostg.in/api/itsroutewise/details");
        }
        catch (java.net.MalformedURLException e)
        {
            errorOccurred = true;
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(String... requestBody)
    {
        bus1 = new Bus();
        bus2 = new Bus();
        bus3 = new Bus();
        bus4 = new Bus();
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
            e.printStackTrace();
            errorOccurred = true;
        }

        try
        {
            JSONArray jsonArray = new JSONArray(busesEnRouteResult.toString());
            for (int i = 0; i < jsonArray.length(); i++)
            {
                if (Integer.parseInt(jsonArray.getJSONArray(i).getString(12).replace("routeorder:", "")) <= nearestBusStop.getRouteOrder())
                {
                    if (Integer.parseInt(jsonArray.getJSONArray(i).getString(12).replace("routeorder:", "")) == nearestBusStop.getRouteOrder())
                    {
                        numberOfBusesFound++;
                        bus1.setLatitude(jsonArray.getJSONArray(i).getString(2).replace("vehiclelat:", ""));
                        bus1.setLongitude(jsonArray.getJSONArray(i).getString(3).replace("vehiclelng:", ""));
                        bus1.setIsDue(true);
                        bus1.setRegistrationNumber(jsonArray.getJSONArray(i).getString(0).replace("vehicleno:", ""));
                        bus1.setRouteOrder(Integer.parseInt(jsonArray.getJSONArray(i).getString(12).replace("routeorder:", "")));
                    }
                    else
                    {
                        numberOfBusesFound++;
                        bus1.setLatitude(jsonArray.getJSONArray(i).getString(2).replace("vehiclelat:", ""));
                        bus1.setLongitude(jsonArray.getJSONArray(i).getString(3).replace("vehiclelng:", ""));
                        bus1.setRegistrationNumber(jsonArray.getJSONArray(i).getString(0).replace("vehicleno:", ""));
                        bus1.setRouteOrder(Integer.parseInt(jsonArray.getJSONArray(i).getString(12).replace("routeorder:", "")));
                    }

                    if ((i + 1) < (jsonArray.length() - 1))
                    {
                        if (Integer.parseInt(jsonArray.getJSONArray(i + 1).getString(12).replace("routeorder:", "")) == nearestBusStop.getRouteOrder())
                        {
                            numberOfBusesFound++;
                            bus2.setIsDue(true);
                            bus2.setLatitude(jsonArray.getJSONArray(i + 1).getString(2).replace("vehiclelat:", ""));
                            bus2.setLongitude(jsonArray.getJSONArray(i + 1).getString(3).replace("vehiclelng:", ""));
                            bus2.setRegistrationNumber(jsonArray.getJSONArray(i + 1).getString(0).replace("vehicleno:", ""));
                            bus2.setRouteOrder(Integer.parseInt(jsonArray.getJSONArray(i + 1).getString(12).replace("routeorder:", "")));
                        }
                        else
                        {
                            numberOfBusesFound++;
                            bus2.setLatitude(jsonArray.getJSONArray(i + 1).getString(2).replace("vehiclelat:", ""));
                            bus2.setLongitude(jsonArray.getJSONArray(i + 1).getString(3).replace("vehiclelng:", ""));
                            bus2.setRegistrationNumber(jsonArray.getJSONArray(i + 1).getString(0).replace("vehicleno:", ""));
                            bus2.setRouteOrder(Integer.parseInt(jsonArray.getJSONArray(i + 1).getString(12).replace("routeorder:", "")));
                        }
                    }
                    if ((i + 2) < (jsonArray.length() - 1))
                    {
                        if (Integer.parseInt(jsonArray.getJSONArray(i + 2).getString(12).replace("routeorder:", "")) == nearestBusStop.getRouteOrder())
                        {
                            numberOfBusesFound++;
                            bus3.setIsDue(true);
                            bus3.setLatitude(jsonArray.getJSONArray(i + 2).getString(2).replace("vehiclelat:", ""));
                            bus3.setLongitude(jsonArray.getJSONArray(i + 2).getString(3).replace("vehiclelng:", ""));
                            bus3.setRegistrationNumber(jsonArray.getJSONArray(i + 2).getString(0).replace("vehicleno:", ""));
                            bus3.setRouteOrder(Integer.parseInt(jsonArray.getJSONArray(i + 2).getString(12).replace("routeorder:", "")));
                        }
                        else
                        {
                            numberOfBusesFound++;
                            bus3.setLatitude(jsonArray.getJSONArray(i + 2).getString(2).replace("vehiclelat:", ""));
                            bus3.setLongitude(jsonArray.getJSONArray(i + 2).getString(3).replace("vehiclelng:", ""));
                            bus3.setRegistrationNumber(jsonArray.getJSONArray(i + 2).getString(0).replace("vehicleno:", ""));
                            bus3.setRouteOrder(Integer.parseInt(jsonArray.getJSONArray(i + 2).getString(12).replace("routeorder:", "")));
                        }
                    }
                    if ((i + 3) < (jsonArray.length() - 1))
                    {
                        if (Integer.parseInt(jsonArray.getJSONArray(i + 3).getString(12).replace("routeorder:", "")) == nearestBusStop.getRouteOrder())
                        {
                            numberOfBusesFound++;
                            bus4.setIsDue(true);
                            bus4.setLatitude(jsonArray.getJSONArray(i + 3).getString(2).replace("vehiclelat:", ""));
                            bus4.setLongitude(jsonArray.getJSONArray(i + 3).getString(3).replace("vehiclelng:", ""));
                            bus4.setRegistrationNumber(jsonArray.getJSONArray(i + 3).getString(0).replace("vehicleno:", ""));
                            bus4.setRouteOrder(Integer.parseInt(jsonArray.getJSONArray(i + 3).getString(12).replace("routeorder:", "")));
                        }
                        else
                        {
                            numberOfBusesFound++;
                            bus4.setLatitude(jsonArray.getJSONArray(i + 3).getString(2).replace("vehiclelat:", ""));
                            bus4.setLongitude(jsonArray.getJSONArray(i + 3).getString(3).replace("vehiclelng:", ""));
                            bus4.setRegistrationNumber(jsonArray.getJSONArray(i + 3).getString(0).replace("vehicleno:", ""));
                            bus4.setRouteOrder(Integer.parseInt(jsonArray.getJSONArray(i + 3).getString(12).replace("routeorder:", "")));
                        }
                    }
                    break;
                }
            }
        }
        catch (org.json.JSONException e)
        {
            e.printStackTrace();
            errorOccurred = true;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        caller.onBusesEnRouteFound(errorOccurred, new Bus[]{bus1, bus2, bus3, bus4}, numberOfBusesFound);
    }
}
