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

class GetTimeToBusesTask extends AsyncTask<Bus, Void, Bus[]>
{
    private NetworkingCallback caller;
    private boolean errorOccurred = false;
    private BusStop selectedBusStop;
    private int numberOfBusesFound;

    GetTimeToBusesTask(NetworkingCallback aCaller, BusStop aBusStop, int aNumberOfBusesFound)
    {
        caller = aCaller;
        selectedBusStop = aBusStop;
        numberOfBusesFound = aNumberOfBusesFound;
    }

    @Override
    protected Bus[] doInBackground(Bus... buses)
    {
        final String PART_2 = "gEaB_2uGV5";
        final String PART_1 = "AIzaSyAw-T";
        final String PART_4 = "tQHF9HgIU";
        final String PART_3 = "UhxjZVpvRd";
        final String GOOGLE_MAPS_DISTANCE_MATRIX_API_KEY = PART_1 + PART_2 + PART_3 + PART_4;
        StringBuilder mapsResult = new StringBuilder();

        for (int i = 0; i < numberOfBusesFound; i++)
        {
            if (buses[i].getIsDue())
            {
                buses[i].setTimeToBus("0 mins");
                continue;
            }
            try
            {
                URL googleMapsURL;
                try
                {
                    googleMapsURL = new URL("https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + buses[i].getLatitude() + "," + buses[i].getLongitude() + "&destinations=" + selectedBusStop.getLatitude() + "," + selectedBusStop.getLongitude() + "&key=" + GOOGLE_MAPS_DISTANCE_MATRIX_API_KEY);
                }
                catch (MalformedURLException e)
                {
                    errorOccurred = true;
                    return null;
                }

                mapsResult.delete(0, mapsResult.length());
                HttpURLConnection httpURLConnection = (HttpURLConnection) googleMapsURL.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("Accept", "application/json");
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                String mapsLine;
                while ((mapsLine = reader.readLine()) != null)
                {
                    mapsResult.append(mapsLine);
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
                JSONObject jsonObject = new JSONObject(mapsResult.toString());
                JSONArray jsonArray = jsonObject.getJSONArray("rows");
                jsonObject = jsonArray.getJSONObject(0);
                jsonArray = jsonObject.getJSONArray("elements");
                jsonObject = jsonArray.getJSONObject(0);
                if (jsonObject.getString("status").equals("OK"))
                {
                    jsonObject = jsonObject.getJSONObject("duration");
                    buses[i].setTimeToBus(jsonObject.getString("text"));
                }
                else
                {
                    buses[i].setTimeToBus("UNAVAILABLE");
                }

            }
            catch (org.json.JSONException e)
            {
                errorOccurred = true;
            }
        }

        return buses;
    }

    @Override
    protected void onPostExecute(Bus[] buses)
    {
        caller.onTimeToBusesFound(errorOccurred, buses);
    }
}
