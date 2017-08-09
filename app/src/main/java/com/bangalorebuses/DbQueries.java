package com.bangalorebuses;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

class DbQueries
{
    public static ArrayList<BusRoute> getAllRoutes(SQLiteDatabase db)
    {
        Cursor cursor = db.rawQuery("select Routes.* from Routes", null);
        ArrayList<BusRoute> routes = new ArrayList<>();
        while (cursor.moveToNext())
        {
            BusRoute route = new BusRoute();
            route.setBusRouteId(cursor.getInt(0));
            route.setBusRouteNumber(cursor.getString(1));
            route.setBusRouteServiceType(cursor.getString(2));
            route.setBusRouteDirection(cursor.getString(3));
            route.setBusRouteDirectionName(cursor.getString(4));
            routes.add(route);
        }
        cursor.close();
        return routes;
    }

    public static BusRoute getRouteDetails(SQLiteDatabase db, int routeId)
    {
        BusRoute route = new BusRoute();
        Cursor cursor = db.rawQuery("select Routes.RouteId, Routes.RouteNumber, Routes.RouteServiceType, Routes.RouteDirection," +
                " Routes.RouteDirectionName from Routes where Routes.RouteId = " + routeId, null);
        if (cursor.moveToNext())
        {
            route.setBusRouteId(cursor.getInt(0));
            route.setBusRouteNumber(cursor.getString(1));
            route.setBusRouteServiceType(cursor.getString(2));
            route.setBusRouteDirection(cursor.getString(3));
            route.setBusRouteDirectionName(cursor.getString(3));
            cursor.close();
            return route;
        }
        else
        {
            cursor.close();
            return null;
        }
    }

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
            route.setBusRouteServiceType(cursor.getString(2));
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
            busStop.setBusStopLat(cursor.getString(2));
            busStop.setBusStopLong(cursor.getString(3));
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
                " Routes.RouteDirection, Routes.RouteDirectionName from RouteStops join" +
                " Routes where RouteStops.RouteId = Routes.RouteId and RouteStops.StopId = " + stopId, null);
        ArrayList<BusRoute> routes = new ArrayList<>();
        while (cursor.moveToNext())
        {
            BusRoute route = new BusRoute();
            route.setBusRouteId(cursor.getInt(0));
            route.setBusRouteNumber(cursor.getString(1));
            route.setBusRouteServiceType(cursor.getString(2));
            route.setBusRouteDirection(cursor.getString(3));
            route.setBusRouteDirectionName(cursor.getString(4));
            routes.add(route);
        }
        cursor.close();
        return routes;
    }

    public static ArrayList<String> getRouteDepartureTimings(SQLiteDatabase db, int routeId)
    {
        Cursor cursor = db.rawQuery("select RouteTimings.RouteDepartureTime from RouteTimings" +
                " where RouteTimings.RouteId = " + routeId, null);
        ArrayList<String> departureTimings = new ArrayList<>();
        while (cursor.moveToNext())
        {
            departureTimings.add(cursor.getString(0));
        }
        cursor.close();
        return departureTimings;
    }

    public static ArrayList<DirectTrip> getDirectRoutesBetweenStops(SQLiteDatabase db, String originStopName, String destinationStopName)
    {
        Cursor cursor = db.rawQuery("select distinct sub1.RouteId, sub1.StopId," +
                " sub2.StopId from (select RouteStops.RouteId,RouteStops.StopId, RouteStops.StopRouteOrder" +
                " from Stops join RouteStops where Stops.StopName = '" + originStopName + "' and Stops.StopId = RouteStops.StopId)sub1" +
                " join (select RouteStops.RouteId, RouteStops.StopId, RouteStops.StopRouteOrder from Stops join RouteStops where" +
                " Stops.StopName = '" + destinationStopName + "' and Stops.StopId = RouteStops.StopId)sub2 where" +
                " sub1.RouteId = sub2.RouteId and sub1.StopRouteOrder < sub2.StopRouteOrder", null);
        ArrayList<DirectTrip> directTrips = new ArrayList<>();
        while (cursor.moveToNext())
        {
            DirectTrip directTrip = new DirectTrip();
            BusStop originStop = new BusStop();
            BusStop destinationStop = new BusStop();
            BusRoute route = new BusRoute();
            route.setBusRouteId(cursor.getInt(0));
            originStop.setBusStopId(cursor.getInt(1));
            destinationStop.setBusStopId(cursor.getInt(2));
            directTrips.add(directTrip);
        }
        cursor.close();
        return directTrips;
    }

    public static BusStop getStopDetails(SQLiteDatabase db, int stopId)
    {
        BusStop busStop = new BusStop();
        Cursor cursor = db.rawQuery("select Stops.StopId, Stops.StopName, Stops.StopLat, Stops.StopLong," +
                " Stops.StopDirectionName from Stops where Stops.StopId = " + stopId, null);
        if (cursor.moveToNext())
        {
            busStop.setBusStopId(cursor.getInt(0));
            busStop.setBusStopName(cursor.getString(1));
            busStop.setBusStopLat(cursor.getString(2));
            busStop.setBusStopLong(cursor.getString(3));
            busStop.setBusStopDirectionName(cursor.getString(4));
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
            busStop.setBusStopId(cursor.getInt(0));
            busStop.setBusStopName(cursor.getString(1));
            busStop.setBusStopLat(cursor.getString(2));
            busStop.setBusStopLong(cursor.getString(3));
            busStop.setBusStopDirectionName(cursor.getString(4));
            busStops.add(busStop);
        }
        cursor.close();
        return busStops;
    }

    public static ArrayList<BusStop> getAllStops(SQLiteDatabase db)
    {
        Cursor cursor = db.rawQuery("select Stops.* from Stops", null);
        ArrayList<BusStop> busStops = new ArrayList<>();
        while (cursor.moveToNext())
        {
            BusStop busStop = new BusStop();
            busStop.setBusStopId(cursor.getInt(0));
            busStop.setBusStopName(cursor.getString(1));
            busStop.setBusStopLat(cursor.getString(2));
            busStop.setBusStopLong(cursor.getString(3));
            busStop.setBusStopDirectionName(cursor.getString(4));
            busStops.add(busStop);
        }
        cursor.close();
        return busStops;
    }
}