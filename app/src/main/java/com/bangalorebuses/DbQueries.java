package com.bangalorebuses;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import static com.bangalorebuses.Constants.DIRECTION_UP;

class DbQueries
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

    public static ArrayList<BusStop> getStopsOnRoute(SQLiteDatabase db, int routeId)
    {
        Cursor cursor = db.rawQuery("select RouteStops.StopId, Stops.StopName,Stops.StopLat," +
                " Stops.StopLong, Stops.StopDirectionName, RouteStops.StopRouteOrder from RouteStops " +
                "join Stops where RouteStops.RouteId = " + routeId + " and RouteStops.StopId  = Stops.StopId", null);
        ArrayList<BusStop> stopsOnRoute = new ArrayList<>();
        while(cursor.moveToNext())
        {
            BusStop busStop = new BusStop();
            busStop.setBusStopId(cursor.getInt(0));
            busStop.setBusStopName(cursor.getString(1));
            busStop.setLatitude(cursor.getString(2));
            busStop.setLongitude(cursor.getString(3));
            busStop.setBusStopDirectionName(cursor.getString(4));
            busStop.setRouteOrder(cursor.getInt(5));
            stopsOnRoute.add(busStop);
        }
        cursor.close();
        return stopsOnRoute;
    }

    public static ArrayList<String> getRouteDepartureTimings(SQLiteDatabase db, int routeId)
    {
        Cursor cursor = db.rawQuery("select RouteTimings.RouteDepartureTime from RouteTimings" +
                " where RouteTimings.RouteId = " + routeId, null);
        ArrayList<String> departureTimings = new ArrayList<>();
        while(cursor.moveToNext())
        {
            departureTimings.add(cursor.getString(cursor.getColumnIndex(BengaluruBusesContract.RouteTimings.COLUMN_ROUTE_DEPARTURE_TIME)));
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
            Route route = new Route();
            route.setUpRouteId(cursor.getString(0));
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