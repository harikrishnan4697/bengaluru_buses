package com.bangalorebuses.nearby;

import android.os.AsyncTask;

import com.bangalorebuses.core.BusStop;
import com.bangalorebuses.utils.Constants;
import com.bangalorebuses.utils.NetworkingHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static com.bangalorebuses.utils.Constants.NETWORK_QUERY_IO_EXCEPTION;
import static com.bangalorebuses.utils.Constants.NETWORK_QUERY_JSON_EXCEPTION;
import static com.bangalorebuses.utils.Constants.NETWORK_QUERY_NO_ERROR;
import static com.bangalorebuses.utils.Constants.NETWORK_QUERY_REQUEST_TIMEOUT_EXCEPTION;

public class NearestBusStopsTask extends AsyncTask<URL, Void, Void>
{
    private NetworkingHelper caller;
    private String errorMessage = NETWORK_QUERY_NO_ERROR;
    private ArrayList<BusStop> busStopsNearby = new ArrayList<>();

    public NearestBusStopsTask(NetworkingHelper aCaller)
    {
        caller = aCaller;
    }

    @Override
    protected Void doInBackground(URL... urls)
    {
        StringBuilder result = new StringBuilder();
        try
        {
            HttpURLConnection httpURLConnection = (HttpURLConnection) urls[0].openConnection();
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
            httpURLConnection.disconnect();
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

            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                if (!jsonObject.getString("StopName").contains("CS-"))
                {
                    BusStop busStop = new BusStop();
                    busStop.setBusStopId(jsonObject.getInt("StopId"));
                    busStop.setBusStopLat(jsonObject.getString("StopLat"));
                    busStop.setBusStopLong(jsonObject.getString("StopLong"));
                    busStop.setBusStopDistance((int)(Double.parseDouble(jsonObject.getString("StopDist"))
                            * 1000) + " metres away");

                    String busStopName = jsonObject.getString("StopName");
                    if (busStopName.contains("("))
                    {
                        busStop.setBusStopName(busStopName.substring(0, busStopName.indexOf("(")));
                    }

                    if (busStopName.contains("(") && busStopName.contains(")"))
                    {
                        busStop.setBusStopDirectionName(busStopName.substring(busStopName.indexOf("(") + 1,
                                busStopName.indexOf(")")));
                    }

                    busStopsNearby.add(busStop);
                }
            }
        }
        catch (org.json.JSONException i)
        {
            errorMessage = NETWORK_QUERY_JSON_EXCEPTION;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void params)
    {
        super.onPostExecute(params);
        if (!isCancelled())
        {
            caller.onBusStopsNearbyFound(errorMessage, busStopsNearby);
        }
    }
}