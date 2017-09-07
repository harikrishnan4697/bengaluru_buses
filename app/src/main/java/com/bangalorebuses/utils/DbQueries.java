package com.bangalorebuses.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bangalorebuses.core.BusRoute;
import com.bangalorebuses.core.BusStop;
import com.bangalorebuses.trips.DirectTrip;
import com.bangalorebuses.trips.Trip;

import java.util.ArrayList;

public class DbQueries
{
    public static ArrayList<String> getAllDistinctRouteNumbers(SQLiteDatabase db)
    {
        Cursor cursor = db.rawQuery("select distinct Routes.RouteNumber from Routes", null);
        ArrayList<String> busRouteNumbers = new ArrayList<>();
        while (cursor.moveToNext())
        {
            busRouteNumbers.add(cursor.getString(0));
        }
        cursor.close();
        return busRouteNumbers;
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
                " Routes.RouteDirection, Routes.RouteDirectionName, RouteStops.StopRouteOrder" +
                " from RouteStops join Routes where RouteStops.RouteId = Routes.RouteId and " +
                "RouteStops.StopId = " + stopId, null);
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

    public static int getStopRouteOrder(SQLiteDatabase db, int routeId, int stopId)
    {
        Cursor cursor = db.rawQuery("select RouteStops.StopRouteOrder from RouteStops where RouteStops.RouteId = "
                + routeId + " and RouteStops.StopId = " + stopId, null);

        int routeOrder = -1;
        if (cursor.moveToNext())
        {
            routeOrder = cursor.getInt(0);
        }
        cursor.close();
        return routeOrder;
    }

    public static ArrayList<Trip> getDirectTripsBetweenStops(SQLiteDatabase db, String originStopName, String destinationStopName)
    {
        Cursor cursor = db.rawQuery("select distinct sub1.RouteId, sub1.StopId," +
                " sub2.StopId from (select RouteStops.RouteId,RouteStops.StopId, RouteStops.StopRouteOrder" +
                " from Stops join RouteStops where Stops.StopName = '" + originStopName + "' and Stops.StopId = RouteStops.StopId)sub1" +
                " join (select RouteStops.RouteId, RouteStops.StopId, RouteStops.StopRouteOrder from Stops join RouteStops where" +
                " Stops.StopName = '" + destinationStopName + "' and Stops.StopId = RouteStops.StopId)sub2 where" +
                " sub1.RouteId = sub2.RouteId and sub1.StopRouteOrder < sub2.StopRouteOrder", null);
        ArrayList<Trip> directTrips = new ArrayList<>();
        while (cursor.moveToNext())
        {
            BusStop originStop = new BusStop();
            BusStop destinationStop = new BusStop();
            BusRoute busRoute = new BusRoute();
            DirectTrip directTrip = new DirectTrip();

            originStop.setBusStopId(cursor.getInt(1));
            destinationStop.setBusStopId(cursor.getInt(2));

            busRoute.setBusRouteId(cursor.getInt(0));
            busRoute.setTripPlannerOriginBusStop(originStop);
            busRoute.setTripPlannerDestinationBusStop(destinationStop);

            directTrip.setOriginBusStop(originStop);
            directTrip.setBusRoute(busRoute);
            directTrip.setDestinationBusStopName(destinationStopName);

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

    public static ArrayList<String> getAllDistinctStopNames(SQLiteDatabase db)
    {
        Cursor cursor = db.rawQuery("select distinct Stops.StopName from Stops", null);
        ArrayList<String> stopNames = new ArrayList<>();
        while (cursor.moveToNext())
        {
            stopNames.add(cursor.getString(0));
        }
        cursor.close();
        return stopNames;
    }

    public static int getNumberOfStopsBetweenRouteOrders(SQLiteDatabase db, int routeId, int routeOrder1, int routeOrder2)
    {
        Cursor cursor = db.rawQuery("select count(*) from RouteStops where RouteStops.RouteId = " + routeId +
                " and RouteStops.StopRouteOrder > " + routeOrder1 + " and RouteStops.StopRouteOrder <= " + routeOrder2, null);
        if (cursor.moveToNext())
        {
            int numberOfStopsBetweenRouteOrders = cursor.getInt(0);
            cursor.close();
            return numberOfStopsBetweenRouteOrders;
        }
        else
        {
            cursor.close();
            return -1;
        }
    }
}