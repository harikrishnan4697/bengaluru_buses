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
    private BusStop nearestBusStop;
    private int numberOfBusesFound;
    private URL[] googleMapsURL = new URL[4];

    GetTimeToBusesTask(NetworkingCallback aCaller, BusStop aBusStop, int aNumberOfBusesFound)
    {
        caller = aCaller;
        nearestBusStop = aBusStop;
        numberOfBusesFound = aNumberOfBusesFound;
    }

    @Override
    protected Bus[] doInBackground(Bus... buses)
    {
        final String GOOGLE_MAPS_DISTANCE_MATRIX_API_KEY = "AIzaSyAw-TgEaB_2uGV5UhxjZVpvRdtQHF9HgIU";
        try
        {
            googleMapsURL[0] = new URL("https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + buses[0].getLatitude() + "," + buses[0].getLongitude() + "&destinations=" + nearestBusStop.getLatitude() + "," + nearestBusStop.getLongitude() + "&mode=transit&transit_mode=bus&key=" + GOOGLE_MAPS_DISTANCE_MATRIX_API_KEY);
            googleMapsURL[1] = new URL("https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + buses[1].getLatitude() + "," + buses[1].getLongitude() + "&destinations=" + nearestBusStop.getLatitude() + "," + nearestBusStop.getLongitude() + "&mode=transit&transit_mode=bus&key=" + GOOGLE_MAPS_DISTANCE_MATRIX_API_KEY);
            googleMapsURL[2] = new URL("https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + buses[2].getLatitude() + "," + buses[2].getLongitude() + "&destinations=" + nearestBusStop.getLatitude() + "," + nearestBusStop.getLongitude() + "&mode=transit&transit_mode=bus&key=" + GOOGLE_MAPS_DISTANCE_MATRIX_API_KEY);
            googleMapsURL[3] = new URL("https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + buses[3].getLatitude() + "," + buses[3].getLongitude() + "&destinations=" + nearestBusStop.getLatitude() + "," + nearestBusStop.getLongitude() + "&mode=transit&transit_mode=bus&key=" + GOOGLE_MAPS_DISTANCE_MATRIX_API_KEY);
        }
        catch (MalformedURLException r)
        {
            r.printStackTrace();
            errorOccurred = true;
        }

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
                mapsResult.delete(0, mapsResult.length());
                HttpURLConnection httpURLConnection = (HttpURLConnection) googleMapsURL[i].openConnection();
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
                e.printStackTrace();
                errorOccurred = true;
            }

            try
            {
                JSONObject jsonObject = new JSONObject(mapsResult.toString());
                JSONArray jsonArray = jsonObject.getJSONArray("rows");
                jsonObject = jsonArray.getJSONObject(0);
                jsonArray = jsonObject.getJSONArray("elements");
                jsonObject = jsonArray.getJSONObject(0);
                jsonObject = jsonObject.getJSONObject("duration");
                buses[i].setTimeToBus(jsonObject.getString("text"));

            }
            catch (org.json.JSONException b)
            {
                b.printStackTrace();
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
