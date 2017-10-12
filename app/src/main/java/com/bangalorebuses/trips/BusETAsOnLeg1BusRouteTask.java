package com.bangalorebuses.trips;

import android.os.AsyncTask;

import com.bangalorebuses.core.Bus;
import com.bangalorebuses.core.BusRoute;
import com.bangalorebuses.utils.CommonMethods;
import com.bangalorebuses.utils.Constants;
import com.bangalorebuses.utils.DbQueries;

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
import static com.bangalorebuses.utils.Constants.db;

public class BusETAsOnLeg1BusRouteTask extends AsyncTask<Void, Void, Void>
{
    private IndirectTripDetailsHelper caller;
    private String errorMessage = NETWORK_QUERY_NO_ERROR;
    private ArrayList<Bus> buses = new ArrayList<>();
    private TransitPoint transitPoint;
    private BusRoute busRoute;


    public BusETAsOnLeg1BusRouteTask(IndirectTripDetailsHelper caller, TransitPoint transitPoint,
                                     BusRoute busRoute)
    {
        this.caller = caller;
        this.transitPoint = transitPoint;
        this.busRoute = busRoute;
    }

    @Override
    protected Void doInBackground(Void... voids)
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

        String requestParameters = "routeNO=" + busRoute.getBusRouteNumber() + "&" + "direction=" + busRoute.getBusRouteDirection();

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
            writer.write(requestParameters.getBytes());
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
                    if (Integer.parseInt(jsonArray.getJSONArray(i).getString(12).replace("routeorder:", "")) <=
                            busRoute.getTripPlannerOriginBusStop().getBusStopRouteOrder())
                    {
                        for (int j = 0; j < jsonArray.length(); j++)
                        {
                            if ((i + j) < jsonArray.length())
                            {
                                Bus bus = new Bus();
                                if (Integer.parseInt(jsonArray.getJSONArray(i + j).getString(12).replace("routeorder:", "")) ==
                                        busRoute.getTripPlannerOriginBusStop().getBusStopRouteOrder())
                                {
                                    bus.setDue(true);
                                }
                                String nearestLatLong = jsonArray.getJSONArray(i + j).getString(6).replace("nearestlatlng:", "");
                                bus.setBusLat(nearestLatLong.substring(0, nearestLatLong.indexOf(",")));
                                bus.setBusLong(nearestLatLong.substring(nearestLatLong.indexOf(",") + 1, nearestLatLong.length() - 1));
                                bus.setBusRegistrationNumber(jsonArray.getJSONArray(i + j).getString(0).replace("vehicleno:", ""));
                                bus.setBusRouteOrder(Integer.parseInt(jsonArray.getJSONArray(i + j).getString(12).replace("routeorder:", "")));
                                bus.setBusRoute(busRoute);

                                if (bus.getBusRouteOrder() != 1 || busRoute.getTripPlannerOriginBusStop()
                                        .getBusStopRouteOrder() == 1)
                                {
                                    bus.setBusETA(CommonMethods.calculateTravelTime(busRoute.getBusRouteId(),
                                            busRoute.getBusRouteNumber(), bus.getBusRouteOrder(),
                                            busRoute.getTripPlannerOriginBusStop().getBusStopRouteOrder()));
                                    buses.add(bus);
                                }
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
        busRoute.setBusRouteBuses(buses);
        return null;
    }

    @Override
    protected void onPostExecute(Void param)
    {
        super.onPostExecute(param);

        if (!isCancelled())
        {
            caller.onBusETAsOnLeg1BusRouteFound(errorMessage, busRoute, transitPoint);
        }
    }
}
