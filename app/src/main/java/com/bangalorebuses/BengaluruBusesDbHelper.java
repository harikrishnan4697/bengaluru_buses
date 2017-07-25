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
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.bangalorebuses.Constants.DIRECTION_DOWN;
import static com.bangalorebuses.Constants.DIRECTION_UP;
import static com.bangalorebuses.Constants.SQL_CREATE_BUS_STOPS_ENTRIES;
import static com.bangalorebuses.Constants.SQL_CREATE_ROUTES_ENTRIES;
import static com.bangalorebuses.Constants.SQL_CREATE_ROUTE_STOPS_ENTRIES;
import static com.bangalorebuses.Constants.SQL_CREATE_ROUTE_TIMINGS_ENTRIES;

public class BengaluruBusesDbHelper extends SQLiteOpenHelper implements NetworkingHelper
{
    private static final int DATABASE_VERSION = 31;
    private static final String DATABASE_NAME = "BengaluruBuses.db";
    private Context context;
    private SQLiteDatabase db;
    private int numberOfRouteDetailsFound = 0;
    private int i = 0;
    private JSONArray jsonArray;

    public BengaluruBusesDbHelper(Context context)
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
        onCreate(db);
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
            jsonArray = new JSONArray(stringBuilder.toString());

            for (; i < 1; i++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(2);
                Route route = new Route();
                route.setRouteNumber(jsonObject.getString("routename"));
                route.setServiceType(jsonObject.getString("service_type_name"));

                if (!route.getRouteNumber().contains(" "))
                {
                    new GetBusRouteDetailsTask(this, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, route);
                }
                else
                {
                    String requestBody = "routeNO=" + route.getRouteNumber() + "&" + "direction=" + DIRECTION_UP;
                    new GetBusesEnRouteTask(this, new BusStop(), route).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestBody);

                    requestBody = "routeNO=" + route.getRouteNumber() + "&" + "direction=" + DIRECTION_DOWN;
                    new GetBusesEnRouteTask(this, new BusStop(), route).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestBody);
                }
            }
        }
        catch (Exception e)
        {
            Toast.makeText(context, "Uh oh something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBusStopsFound(boolean isError, JSONArray busStopsArray)
    {

    }

    @Override
    public void onBusesAtStopFound(String errorMessage, JSONArray buses)
    {

    }

    @Override
    public void onBusRouteDetailsFound(String errorMessage, Route route, boolean isForBusList, String routeDirection)
    {
        numberOfRouteDetailsFound++;
        if (errorMessage.equals(Constants.NETWORK_QUERY_NO_ERROR))
        {
            Log.e("NO ERROR ", Integer.toString(numberOfRouteDetailsFound));
            ContentValues valuesUp = new ContentValues();
            valuesUp.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_ID, route.getUpRouteId());
            valuesUp.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_NUMBER, route.getRouteNumber());
            valuesUp.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_DIRECTION, "UP");
            valuesUp.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_DIRECTION_NAME, route.getUpRouteName());
            valuesUp.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_SERVICE_TYPE, route.getServiceType());
            db.insert(BengaluruBusesContract.Routes.TABLE_NAME, null, valuesUp);

            /*ContentValues valuesDown = new ContentValues();
            valuesDown.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_ID, route.getDownRouteId());
            valuesDown.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_NUMBER, route.getRouteNumber());
            valuesDown.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_DIRECTION, "DN");
            valuesDown.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_DIRECTION_NAME, route.getDownRouteName());
            valuesDown.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_SERVICE_TYPE, route.getServiceType());
            db.insert(BengaluruBusesContract.Routes.TABLE_NAME, null, valuesDown);

            i++;
            try
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Route routeToGet = new Route();
                route.setRouteNumber(jsonObject.getString("routename"));
                route.setServiceType(jsonObject.getString("service_type_name"));
                new GetBusRouteDetailsTask(this, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, route);
            }
            catch (JSONException e)
            {
                Log.e("ERROR ", errorMessage);
            }*/
        }
        else
        {
            Log.e("ERROR ", errorMessage);
        }

        if (numberOfRouteDetailsFound == 20)
        {
            Toast.makeText(context, "Got all route details successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStopsOnBusRouteFound(String errorMessage, BusStop[] busStops, Route route)
    {

    }

    @Override
    public void onBusesEnRouteFound(String errorMessage, Bus[] buses, int numberOfBusesFound, Route route, BusStop selectedBusStop)
    {
        numberOfRouteDetailsFound++;
        if (errorMessage.equals(Constants.NETWORK_QUERY_NO_ERROR))
        {
            Log.e("NO ERROR ", Integer.toString(numberOfRouteDetailsFound));
            ContentValues valuesUp = new ContentValues();
            valuesUp.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_ID, route.getUpRouteId());
            valuesUp.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_NUMBER, route.getRouteNumber());
            valuesUp.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_DIRECTION, "UP");
            valuesUp.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_DIRECTION_NAME, route.getUpRouteName());
            valuesUp.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_SERVICE_TYPE, route.getServiceType());
            db.insert(BengaluruBusesContract.Routes.TABLE_NAME, null, valuesUp);

            /*ContentValues valuesDown = new ContentValues();
            valuesDown.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_ID, route.getDownRouteId());
            valuesDown.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_NUMBER, route.getRouteNumber());
            valuesDown.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_DIRECTION, "DN");
            valuesDown.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_DIRECTION_NAME, route.getDownRouteName());
            valuesDown.put(BengaluruBusesContract.Routes.COLUMN_ROUTE_SERVICE_TYPE, route.getServiceType());
            db.insert(BengaluruBusesContract.Routes.TABLE_NAME, null, valuesDown);

            i++;
            try
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Route routeToGet = new Route();
                route.setRouteNumber(jsonObject.getString("routename"));
                route.setServiceType(jsonObject.getString("service_type_name"));
                new GetBusRouteDetailsTask(this, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, route);
            }
            catch (JSONException e)
            {
                Log.e("ERROR ", errorMessage);
            }*/
        }
        else
        {
            Log.e("ERROR ", errorMessage);
        }

        if (numberOfRouteDetailsFound == 20)
        {
            Toast.makeText(context, "Got all route details successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTimeToBusesFound(boolean isError, Bus[] buses)
    {

    }
}
