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

class GetBusesEnRouteDbTask extends AsyncTask<Void, Void, Void>
{
    private DbNetworkingHelper caller;
    private String errorMessage = NETWORK_QUERY_NO_ERROR;
    private Route route;
    private String requestBody;


    GetBusesEnRouteDbTask(DbNetworkingHelper caller, Route route, String requestBody)
    {
        this.caller = caller;
        this.route = route;
        this.requestBody = requestBody;

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
            route.setUpRouteId(jsonArray.getJSONArray(0).getString(4).replace("routeid:", ""));
            route.setUpRouteName(jsonArray.getJSONArray(0).getString(7).replace("start_busstopname:", "") +
                    " To " + jsonArray.getJSONArray(0).getString(8).replace("end_busstopname:", ""));
            ArrayList<String> upRouteTimings = new ArrayList<>();
            for(int i = 0; i < jsonArray.length(); i++)
            {
                upRouteTimings.add(jsonArray.getJSONArray(i).getString(10).replace("ETA:", ""));
            }
            route.setUpTimings(upRouteTimings);
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
        caller.onBusesEnRouteDbTaskComplete(errorMessage, route);
    }
}
