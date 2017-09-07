package com.bangalorebuses.trips;

import android.os.AsyncTask;

import com.bangalorebuses.core.Bus;
import com.bangalorebuses.utils.Constants;

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

public class BusETAsOnDirectTripTask extends AsyncTask<Void, Void, Void>
{
    private DirectTrip directTrip;
    private String errorMessage = NETWORK_QUERY_NO_ERROR;
    private DirectTripHelper caller;
    private ArrayList<Bus> buses = new ArrayList<>();

    public BusETAsOnDirectTripTask(DirectTripHelper caller, DirectTrip directTrip)
    {
        this.directTrip = directTrip;
        this.caller = caller;
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        String requestBody = "routeNO=" + directTrip.getBusRoute().getBusRouteNumber() +
                "&direction=" + directTrip.getBusRoute().getBusRouteDirection();
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

        HttpURLConnection httpURLConnection;
        String line;
        StringBuilder busesEnRouteResult = new StringBuilder();
        try
        {
            httpURLConnection = (HttpURLConnection) busesEnRouteURL.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setConnectTimeout(Constants.NETWORK_QUERY_CONNECT_TIMEOUT);
            httpURLConnection.setReadTimeout(Constants.NETWORK_QUERY_READ_TIMEOUT);
            httpURLConnection.connect();

            BufferedOutputStream writer = new BufferedOutputStream(httpURLConnection.getOutputStream());
            writer.write(requestBody.getBytes());
            writer.flush();
            writer.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            while ((line = reader.readLine()) != null)
            {
                busesEnRouteResult.append(line);
            }
            reader.close();

            httpURLConnection.disconnect();
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
                    if (Integer.parseInt(jsonArray.getJSONArray(i).getString(12).replace("routeorder:", "")) <=
                            directTrip.getBusRoute().getTripPlannerOriginBusStop().getBusStopRouteOrder())
                    {
                        for (int j = 0; j < jsonArray.length(); j++)
                        {
                            if ((i + j) < jsonArray.length())
                            {
                                Bus bus = new Bus();
                                if (Integer.parseInt(jsonArray.getJSONArray(i + j).getString(12).replace("routeorder:", ""))
                                        == directTrip.getBusRoute().getTripPlannerOriginBusStop().getBusStopRouteOrder())
                                {
                                    bus.setDue(true);
                                }
                                String nearestLatLong = jsonArray.getJSONArray(i + j).getString(6).replace("nearestlatlng:", "");
                                bus.setBusLat(nearestLatLong.substring(0, nearestLatLong.indexOf(",")));
                                bus.setBusLong(nearestLatLong.substring(nearestLatLong.indexOf(",") + 1, nearestLatLong.length() - 1));
                                bus.setBusRegistrationNumber(jsonArray.getJSONArray(i + j).getString(0).replace("vehicleno:", ""));
                                bus.setBusRouteOrder(Integer.parseInt(jsonArray.getJSONArray(i + j).getString(12).replace("routeorder:", "")));
                                bus.setBusRoute(directTrip.getBusRoute());
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

            directTrip.getBusRoute().setBusRouteBuses(buses);
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
        if (!isCancelled())
        {
            caller.onBusETAsOnDirectTripFound(errorMessage, directTrip);
        }
    }
}