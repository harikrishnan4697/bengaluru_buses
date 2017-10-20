package com.bangalorebuses.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bangalorebuses.core.BusRoute;
import com.bangalorebuses.core.BusStop;
import com.bangalorebuses.trips.DirectTrip;

import java.util.ArrayList;

public class DbQueries
{
    public static ArrayList<BusRoute> getRoutesWithNumber(SQLiteDatabase db, String routeNumber)
    {
        Cursor cursor = db.rawQuery("select Routes.RouteId, Routes.RouteNumber, Routes.RouteServiceType," +
                " Routes.RouteDirection, Routes.RouteDirectionName from Routes where Routes.RouteNumber =" +
                " '" + routeNumber + "'", null);
        ArrayList<BusRoute> busRoutes = new ArrayList<>();
        while (cursor.moveToNext())
        {
            BusRoute route = new BusRoute();
            route.setBusRouteId(cursor.getInt(0));
            route.setBusRouteNumber(cursor.getString(1));
            route.setBusRouteDirection(cursor.getString(3));
            route.setBusRouteDirectionName(cursor.getString(4));
            busRoutes.add(route);
        }
        cursor.close();
        return busRoutes;
    }

    public static ArrayList<BusStop> getStopsOnRoute(SQLiteDatabase db, int routeId)
    {
        Cursor cursor = db.rawQuery("select RouteStops.StopId, Stops.StopName,Stops.StopLat," +
                " Stops.StopLong, Stops.StopDirectionName, RouteStops.StopRouteOrder from RouteStops " +
                "join Stops where RouteStops.RouteId = " + routeId + " and RouteStops.StopId  = Stops.StopId", null);
        ArrayList<BusStop> stopsOnRoute = new ArrayList<>();
        while (cursor.moveToNext())
        {
            BusStop busStop = new BusStop();
            busStop.setBusStopId(cursor.getInt(0));
            busStop.setBusStopName(cursor.getString(1));
            busStop.setBusStopDirectionName(cursor.getString(4));
            busStop.setBusStopRouteOrder(cursor.getInt(5));
            stopsOnRoute.add(busStop);
        }
        cursor.close();
        return stopsOnRoute;
    }

    public static ArrayList<BusRoute> getRoutesArrivingAtStop(SQLiteDatabase db, int stopId)
    {
        Cursor cursor = db.rawQuery("select Routes.RouteId, Routes.RouteNumber, Routes.RouteServiceType," +
                " Routes.RouteDirection, Routes.RouteDirectionName, RouteStops.StopRouteOrder" +
                " from RouteStops join Routes where RouteStops.RouteId = Routes.RouteId and " +
                "RouteStops.StopId = " + stopId + " order by Routes.RouteNumber asc", null);
        ArrayList<BusRoute> routes = new ArrayList<>();
        while (cursor.moveToNext())
        {
            BusRoute route = new BusRoute();
            route.setBusRouteId(cursor.getInt(0));
            route.setBusRouteNumber(cursor.getString(1));
            route.setBusRouteDirection(cursor.getString(3));
            route.setBusRouteDirectionName(cursor.getString(4));
            routes.add(route);
        }
        cursor.close();
        return routes;
    }

    public static ArrayList<String> getAllDistinctStopNames(SQLiteDatabase db)
    {
        Cursor cursor = db.rawQuery("select distinct Stops.StopName from Stops order by Stops.StopName asc", null);
        ArrayList<String> stopNames = new ArrayList<>();
        while (cursor.moveToNext())
        {
            stopNames.add(cursor.getString(0));
        }
        cursor.close();
        return stopNames;
    }
}