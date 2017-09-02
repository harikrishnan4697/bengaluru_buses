package com.bangalorebuses;

import android.os.AsyncTask;

import java.util.ArrayList;

import static com.bangalorebuses.Constants.NETWORK_QUERY_NO_ERROR;

class IndirectTripBusesInServiceTask extends AsyncTask<Void, Void, IndirectTrip>
{
    private IndirectTripHelper caller;
    private String errorMessage = NETWORK_QUERY_NO_ERROR;
    private ArrayList<Bus> buses = new ArrayList<>();
    private IndirectTrip indirectTrip;


    IndirectTripBusesInServiceTask(IndirectTripHelper caller, IndirectTrip indirectTrip)
    {
        this.caller = caller;
        this.indirectTrip = indirectTrip;
    }

    @Override
    protected IndirectTrip doInBackground(Void... voids)
    {
        /*URL busesEnRouteURL;
        try
        {
            busesEnRouteURL = new URL("http://bmtcmob.hostg.in/api/itsroutewise/details");
        }
        catch (java.net.MalformedURLException e)
        {
            errorMessage = NETWORK_QUERY_URL_EXCEPTION;
            return null;
        }

        String re

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
                else
                {
                    break;
                }
            }
        }
        catch (org.json.JSONException e)
        {
            errorMessage = NETWORK_QUERY_JSON_EXCEPTION;
        }*/
        return null;
    }

}
