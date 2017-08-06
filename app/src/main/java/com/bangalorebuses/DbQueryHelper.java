package com.bangalorebuses;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import static com.bangalorebuses.Constants.DIRECTION_UP;

class DbQueryHelper
{
    public static Route getRouteDetails(SQLiteDatabase db, int routeId)
    {
        Route route = new Route();
        Cursor cursor = db.rawQuery("select Routes.RouteId, Routes.RouteNumber, Routes.RouteServiceType, Routes.RouteDirection," +
                " Routes.RouteDirectionName from Routes where Routes.RouteId = " + routeId, null);
        if (cursor.moveToNext())
        {
            route.setRouteNumber(cursor.getString(cursor.getColumnIndex(BengaluruBusesContract.Routes.COLUMN_ROUTE_NUMBER)));
            route.setServiceType(cursor.getString(cursor.getColumnIndex(BengaluruBusesContract.Routes.COLUMN_ROUTE_SERVICE_TYPE)));
            route.setDirection(cursor.getString(cursor.getColumnIndex(BengaluruBusesContract.Routes.COLUMN_ROUTE_DIRECTION)));
            if (cursor.getString(cursor.getColumnIndex(BengaluruBusesContract.Routes.COLUMN_ROUTE_DIRECTION)).equals(DIRECTION_UP))
            {
                route.setUpRouteId(cursor.getString(cursor.getColumnIndex(BengaluruBusesContract.Routes.COLUMN_ROUTE_ID)));
                route.setUpRouteName(cursor.getString(cursor.getColumnIndex(BengaluruBusesContract.Routes.COLUMN_ROUTE_DIRECTION_NAME)));
            }
            else
            {
                route.setDownRouteId(cursor.getString(cursor.getColumnIndex(BengaluruBusesContract.Routes.COLUMN_ROUTE_ID)));
                route.setDownRouteName(cursor.getString(cursor.getColumnIndex(BengaluruBusesContract.Routes.COLUMN_ROUTE_DIRECTION_NAME)));
            }
            cursor.close();
            return route;
        }
        else
        {
            cursor.close();
            return null;
        }
    }

    public static ArrayList<Route> getRoutesWithNumber(SQLiteDatabase db, String routeName)
    {

    }

    public static ArrayList<BusStop> getStopsOnRoute(SQLiteDatabase db, int routeId)
    {

    }

    public static ArrayList<Route> getRoutesViaStop(SQLiteDatabase db, int stopId)
    {

    }

    public static ArrayList<String> getRouteDepartureTimings(SQLiteDatabase db, int routeId)
    {

    }

    public static BusStop getStopDetails(SQLiteDatabase db, int stopId)
    {
        BusStop busStop = new BusStop();
        Cursor cursor = db.rawQuery("select Stops.StopId, Stops.StopName, Stops.StopLat, Stops.StopLong," +
                " Stops.StopDirectionName from Stops where Stops.StopId = " + stopId, null);
        if (cursor.moveToNext())
        {
            busStop.setBusStopId(cursor.getInt(cursor.getColumnIndex(BengaluruBusesContract.BusStops.COLUMN_STOP_ID)));
            busStop.setBusStopName(cursor.getString(cursor.getColumnIndex(BengaluruBusesContract.BusStops.COLUMN_STOP_NAME)));
            busStop.setLatitude(cursor.getString(cursor.getColumnIndex(BengaluruBusesContract.BusStops.COLUMN_STOP_LAT)));
            busStop.setLongitude(cursor.getString(cursor.getColumnIndex(BengaluruBusesContract.BusStops.COLUMN_STOP_LONG)));
            busStop.setBusStopDirectionName(cursor.getString(cursor.getColumnIndex(BengaluruBusesContract.BusStops.COLUMN_STOP_DIRECTION_NAME)));
            return busStop;
        }
        else
        {
            cursor.close();
            return null;
        }
    }

    public static ArrayList<BusStop> getStopsWithName(SQLiteDatabase db, String stopName)
    {
        Cursor cursor = db.rawQuery("select Stops.StopId, Stops.StopName, Stops.StopLat, Stops.StopLong," +
                " Stops.StopDirectionName from Stops where Stops.StopName = '" + stopName + "'", null);
        ArrayList<BusStop> busStops = new ArrayList<>();
        while (cursor.moveToNext())
        {
            BusStop busStop = new BusStop();
            busStop.setBusStopId(cursor.getInt(cursor.getColumnIndex(BengaluruBusesContract.BusStops.COLUMN_STOP_ID)));
            busStop.setBusStopName(cursor.getString(cursor.getColumnIndex(BengaluruBusesContract.BusStops.COLUMN_STOP_NAME)));
            busStop.setLatitude(cursor.getString(cursor.getColumnIndex(BengaluruBusesContract.BusStops.COLUMN_STOP_LAT)));
            busStop.setLongitude(cursor.getString(cursor.getColumnIndex(BengaluruBusesContract.BusStops.COLUMN_STOP_LONG)));
            busStop.setBusStopDirectionName(cursor.getString(cursor.getColumnIndex(BengaluruBusesContract.BusStops.COLUMN_STOP_DIRECTION_NAME)));
            busStops.add(busStop);
        }
        cursor.close();
        return busStops;
    }
}