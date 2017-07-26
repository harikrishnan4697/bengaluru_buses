package com.bangalorebuses;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.bangalorebuses.Constants.DIRECTION_DOWN;
import static com.bangalorebuses.Constants.DIRECTION_UP;
import static com.bangalorebuses.Constants.NETWORK_QUERY_NO_ERROR;
import static com.bangalorebuses.Constants.SQL_CREATE_BUS_STOPS_ENTRIES;
import static com.bangalorebuses.Constants.SQL_CREATE_ROUTES_ENTRIES;
import static com.bangalorebuses.Constants.SQL_CREATE_ROUTE_STOPS_ENTRIES;
import static com.bangalorebuses.Constants.SQL_CREATE_ROUTE_TIMINGS_ENTRIES;

public class BengaluruBusesDbHelper extends SQLiteOpenHelper implements DbNetworkingHelper
{
    private static final int DATABASE_VERSION = 70;
    private static final String DATABASE_NAME = "BengaluruBuses.db";
    private Context context;
    private SQLiteDatabase db;
    private int numberOfRouteDetailsFound = 0;
    private int numberOfBusStopDetailsFound = 0;
    private int loopRouteCount = 0;
    private int loopBusStopCount = 0;
    private JSONArray busRoutesJsonArray;
    private JSONArray busStopsJsonArray;

    BengaluruBusesDbHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        this.db = db;

        db.execSQL(SQL_CREATE_ROUTES_ENTRIES);
        db.execSQL(SQL_CREATE_ROUTE_TIMINGS_ENTRIES);
        db.execSQL(SQL_CREATE_ROUTE_STOPS_ENTRIES);
        db.execSQL(SQL_CREATE_BUS_STOPS_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        this.db = db;

        db.execSQL("DROP TABLE IF EXISTS " + BengaluruBusesContract.RouteStops.TABLE_NAME);
        updateRouteStopsTable();

        db.execSQL("DROP TABLE IF EXISTS " + BengaluruBusesContract.Routes.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + BengaluruBusesContract.RouteTimings.TABLE_NAME);
        updateRouteAndRouteTimingTables();

        db.execSQL("DROP TABLE IF EXISTS " + BengaluruBusesContract.BusStops.TABLE_NAME);
        updateBusStopsTable();

        onCreate(db);
    }

    private void updateBusStopsTable()
    {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream;
        InputStreamReader inputStreamReader;

        try
        {
            inputStream = assetManager.open("bangalore_city_bus_stops.txt");
            inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null)
            {
                stringBuilder.append(line);
            }

            // Converts the asset to a JSON array
            busStopsJsonArray = new JSONArray(stringBuilder.toString());
            Toast.makeText(context, "Updating bus stops database...", Toast.LENGTH_SHORT).show();
            for (; loopBusStopCount < 5; loopBusStopCount++)
            {
                JSONObject jsonObject = busStopsJsonArray.getJSONObject(loopBusStopCount);
                new GetStopsWithNameDbTask(this, jsonObject.getString("StopName")).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        catch (Exception e)
        {
            Toast.makeText(context, "Uh oh something went wrong!", Toast.LENGTH_SHORT).show();
            appendLog("ERROR: " + e.toString());
            e.printStackTrace();
        }
    }

    private void updateRouteAndRouteTimingTables()
    {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream;
        InputStreamReader inputStreamReader;

        try
        {
            inputStream = assetManager.open("bangalore_city_bus_routes.txt");
            inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null)
            {
                stringBuilder.append(line);
            }

            // Converts the asset to a JSON array
            busRoutesJsonArray = new JSONArray(stringBuilder.toString());
            Toast.makeText(context, "Updating database...", Toast.LENGTH_SHORT).show();
            for (; loopRouteCount < 5; loopRouteCount++)
            {
                JSONObject jsonObject = busRoutesJsonArray.getJSONObject(loopRouteCount);
                Route route = new Route();
                route.setRouteNumber(jsonObject.getString("routename"));
                route.setServiceType(jsonObject.getString("service_type_name"));

                if (!route.getRouteNumber().contains(" "))
                {
                    new GetBusRouteDetailsDbTask(this, route).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                else
                {
                    route.setUpRouteId(jsonObject.getString("routeid"));
                    route.setDirection(DIRECTION_UP);
                    String requestBody = "routeNO=" + route.getRouteNumber() + "&" + "direction=" + DIRECTION_UP;
                    new GetBusesEnRouteDbTask(this, route, requestBody).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        }
        catch (Exception e)
        {
            Toast.makeText(context, "Uh oh something went wrong!", Toast.LENGTH_SHORT).show();
            appendLog("ERROR: " + e.toString());
            e.printStackTrace();
        }
    }

    private void updateRouteStopsTable()
    {

    }

    @Override
    public void onStopsWithNameDbTaskComplete(String errorMessage, String busStopNameToSearchFor, BusStop[] busStops)
    {
        numberOfBusStopDetailsFound++;
        if (errorMessage.equals(NETWORK_QUERY_NO_ERROR))
        {
            for (BusStop busStop : busStops)
            {
                if (busStop.getBusStopName().equals(busStopNameToSearchFor))
                {
                    ContentValues valuesUp = new ContentValues();
                    valuesUp.put(BengaluruBusesContract.BusStops.COLUMN_STOP_ID, busStop.getBusStopId());
                    valuesUp.put(BengaluruBusesContract.BusStops.COLUMN_STOP_NAME, busStop.getBusStopName());
                    valuesUp.put(BengaluruBusesContract.BusStops.COLUMN_STOP_LAT, busStop.getLatitude());
                    valuesUp.put(BengaluruBusesContract.BusStops.COLUMN_STOP_LONG, busStop.getLongitude());
                    valuesUp.put(BengaluruBusesContract.BusStops.COLUMN_STOP_DIRECTION_NAME, busStop.getBusStopDirectionName());

                    if (db.insert(BengaluruBusesContract.BusStops.TABLE_NAME, null, valuesUp) == -1)
                    {
                        Log.i("FAILED MISMATCH: ", Integer.toString(numberOfBusStopDetailsFound) + " " + busStopNameToSearchFor);
                        appendLog("FAILED MISMATCH: " + numberOfBusStopDetailsFound + " #" + busStopNameToSearchFor);
                    }
                    else
                    {
                        Log.i("SUCCESSFUL: ", Integer.toString(numberOfBusStopDetailsFound) + " " + busStopNameToSearchFor);
                    }
                }
            }
        }
        else
        {
            Log.e("FAILED: ", Integer.toString(numberOfBusStopDetailsFound) + " " + errorMessage + " " + busStopNameToSearchFor);
            appendLog("FAILED: " + numberOfBusStopDetailsFound + " " + errorMessage + " #" + busStopNameToSearchFor);
        }
    }

    @Override
    public void onBusRouteDetailsDbTaskComplete(String errorMessage, Route route)
    {
        numberOfRouteDetailsFound++;
        if (errorMessage.equals(Constants.NETWORK_QUERY_NO_ERROR))
        {
            ContentValues valuesUp = new ContentValues();
            valuesUp.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_ID, route.getUpRouteId());
            valuesUp.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_NUMBER, route.getRouteNumber());
            valuesUp.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_DIRECTION, "UP");
            valuesUp.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_DIRECTION_NAME, route.getUpRouteName());
            valuesUp.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_SERVICE_TYPE, route.getServiceType());
            if (db.insert(BengaluruBusesContract.Routes.TABLE_NAME, null, valuesUp) == -1)
            {
                String requestBody = "routeNO=" + route.getRouteNumber() + "&" + "direction=" + DIRECTION_UP;
                route.setDirection(DIRECTION_UP);
                new GetBusesEnRouteDbTask(this, route, requestBody).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return;
            }
            else
            {
                // Insert data into the route timings table
                if (!(route.getUpRouteId() == null || route.getUpRouteId().equals("")))
                {
                    ContentValues valuesUpTimeTable = new ContentValues();
                    for (int i = 0; i < route.getUpTimings().size(); i++)
                    {
                        valuesUpTimeTable.clear();
                        valuesUpTimeTable.put(BengaluruBusesContract.RouteTimings.COLUMN_ROUTE_ID, route.getUpRouteId());
                        valuesUpTimeTable.put(BengaluruBusesContract.RouteTimings.COLUMN_ROUTE_DEPARTURE_TIME, route.getUpTimings().get(i));
                        if (db.insert(BengaluruBusesContract.RouteTimings.TABLE_NAME, null, valuesUpTimeTable) == -1)
                        {
                            appendLog("ERROR INSERTING ROUTE TIMINGS FOR " + route.getRouteNumber() + " UP");
                        }
                    }
                }

                ContentValues valuesDown = new ContentValues();
                valuesDown.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_ID, route.getDownRouteId());
                valuesDown.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_NUMBER, route.getRouteNumber());
                valuesDown.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_DIRECTION, "DN");
                valuesDown.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_DIRECTION_NAME, route.getDownRouteName());
                valuesDown.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_SERVICE_TYPE, route.getServiceType());
                if (db.insert(BengaluruBusesContract.Routes.TABLE_NAME, null, valuesDown) == -1)
                {
                    String requestBody = "routeNO=" + route.getRouteNumber() + "&" + "direction=" + DIRECTION_DOWN;
                    route.setDirection(DIRECTION_DOWN);
                    new GetBusesEnRouteDbTask(this, route, requestBody).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    return;
                }
                else
                {
                    if (!(route.getDownRouteId() == null || route.getDownRouteId().equals("")))
                    {
                        ContentValues valuesDownTimeTable = new ContentValues();
                        for (int i = 0; i < route.getDownTimings().size(); i++)
                        {
                            valuesDownTimeTable.clear();
                            valuesDownTimeTable.put(BengaluruBusesContract.RouteTimings.COLUMN_ROUTE_ID, route.getDownRouteId());
                            valuesDownTimeTable.put(BengaluruBusesContract.RouteTimings.COLUMN_ROUTE_DEPARTURE_TIME, route.getDownTimings().get(i));
                            if (db.insert(BengaluruBusesContract.RouteTimings.TABLE_NAME, null, valuesDownTimeTable) == -1)
                            {
                                appendLog("ERROR INSERTING ROUTE TIMINGS FOR " + route.getRouteNumber() + " DOWN");
                            }
                        }
                    }
                }
                Log.i("SUCCESSFUL: ", Integer.toString(numberOfRouteDetailsFound) + " " + route.getRouteNumber());
            }

            try
            {
                synchronized (this)
                {
                    loopRouteCount++;
                    JSONObject jsonObject = busRoutesJsonArray.getJSONObject(loopRouteCount);
                    Route routeToGet = new Route();
                    routeToGet.setRouteNumber(jsonObject.getString("routename"));
                    routeToGet.setServiceType(jsonObject.getString("service_type_name"));
                    if (!routeToGet.getRouteNumber().contains(" "))
                    {
                        new GetBusRouteDetailsDbTask(this, routeToGet).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                    else
                    {
                        routeToGet.setUpRouteId(jsonObject.getString("routeid"));
                        routeToGet.setDirection(DIRECTION_UP);
                        String requestBody = "routeNO=" + routeToGet.getRouteNumber() + "&" + "direction=" + DIRECTION_UP;
                        new GetBusesEnRouteDbTask(this, routeToGet, requestBody).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
            }
            catch (JSONException e)
            {
                Log.e("ROUTE WISE FAILED: ", Integer.toString(numberOfRouteDetailsFound) + " " + errorMessage + " " + route.getRouteNumber());
                appendLog("ROUTE WISE FAILED: " + numberOfRouteDetailsFound + " " + errorMessage + " #" + route.getRouteNumber());
            }
        }
        else
        {
            Log.e("FAILED: ", Integer.toString(numberOfRouteDetailsFound) + " " + errorMessage + " " + route.getRouteNumber());
            appendLog("FAILED: " + numberOfRouteDetailsFound + " " + errorMessage + " #" + route.getRouteNumber());
        }

        if (numberOfRouteDetailsFound == busRoutesJsonArray.length())
        {
            Toast.makeText(context, "Got all route details successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStopsOnBusRouteDbTaskComplete(String errorMessage, Route route, BusStop[] busStops)
    {

    }

    @Override
    public void onBusesEnRouteDbTaskComplete(String errorMessage, Route route)
    {
        numberOfRouteDetailsFound++;
        if (errorMessage.equals(Constants.NETWORK_QUERY_NO_ERROR))
        {
            ContentValues valuesUp = new ContentValues();
            valuesUp.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_ID, route.getUpRouteId());
            valuesUp.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_NUMBER, route.getRouteNumber());
            valuesUp.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_DIRECTION, route.getDirection());
            valuesUp.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_DIRECTION_NAME, route.getUpRouteName());
            valuesUp.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_SERVICE_TYPE, route.getServiceType());
            if (db.insert(BengaluruBusesContract.Routes.TABLE_NAME, null, valuesUp) == -1)
            {
                appendLog("ROUTE WISE: FAILED " + numberOfRouteDetailsFound + " #" + route.getRouteNumber());
            }
            else
            {
                // Insert data into the route timings table
                if (!(route.getUpRouteId() == null || route.getUpRouteId().equals("")))
                {
                    ContentValues valuesUpTimeTable = new ContentValues();
                    for (int i = 0; i < route.getUpTimings().size(); i++)
                    {
                        valuesUpTimeTable.clear();
                        valuesUpTimeTable.put(BengaluruBusesContract.RouteTimings.COLUMN_ROUTE_ID, route.getUpRouteId());
                        valuesUpTimeTable.put(BengaluruBusesContract.RouteTimings.COLUMN_ROUTE_DEPARTURE_TIME, route.getUpTimings().get(i));
                        if (db.insert(BengaluruBusesContract.RouteTimings.TABLE_NAME, null, valuesUpTimeTable) == -1)
                        {
                            appendLog("ROUTE WISE: ERROR INSERTING ROUTE TIMINGS FOR " + route.getRouteNumber() + " UP");
                        }
                    }
                }
                Log.i("ROUTE WISE SUCCESSFUL: ", Integer.toString(numberOfRouteDetailsFound) + " " + route.getRouteNumber());
            }

            try
            {
                synchronized (this)
                {
                    loopRouteCount++;
                    JSONObject jsonObject = busRoutesJsonArray.getJSONObject(loopRouteCount);
                    Route routeToGet = new Route();
                    routeToGet.setRouteNumber(jsonObject.getString("routename"));
                    routeToGet.setServiceType(jsonObject.getString("service_type_name"));

                    if (!routeToGet.getRouteNumber().contains(" "))
                    {
                        new GetBusRouteDetailsDbTask(this, routeToGet).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                    else
                    {
                        routeToGet.setUpRouteId(jsonObject.getString("routeid"));
                        routeToGet.setDirection(DIRECTION_UP);
                        String requestBody = "routeNO=" + routeToGet.getRouteNumber() + "&" + "direction=" + DIRECTION_UP;
                        new GetBusesEnRouteDbTask(this, routeToGet, requestBody).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
            }
            catch (JSONException e)
            {
                Log.e("ROUTE WISE FAILED: ", Integer.toString(numberOfRouteDetailsFound) + " " + errorMessage + " " + route.getRouteNumber());
                appendLog("ROUTE WISE FAILED: " + numberOfRouteDetailsFound + " " + errorMessage + " #" + route.getRouteNumber());
            }
        }
        else
        {
            Log.e("ROUTE WISE FAILED: ", Integer.toString(numberOfRouteDetailsFound) + " " + errorMessage + " " + route.getRouteNumber());
            appendLog("ROUTE WISE FAILED: " + numberOfRouteDetailsFound + " " + errorMessage + " #" + route.getRouteNumber());
        }

        if (numberOfRouteDetailsFound == busRoutesJsonArray.length())
        {
            Toast.makeText(context, "Database update complete!", Toast.LENGTH_SHORT).show();
        }
    }

    private void appendLog(String text)
    {
        File logFile = new File("sdcard/db_update_error_logs.file");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
