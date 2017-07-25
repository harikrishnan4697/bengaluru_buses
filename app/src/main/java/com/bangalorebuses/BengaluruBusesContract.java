package com.bangalorebuses;

import android.provider.BaseColumns;

public class BengaluruBusesContract
{
    public static class Routes
    {
        public static final String  TABLE_NAME = "Routes";
        public static final String  COLUMN_ROUTE_ID = "RouteId";
        public static final String  COLUMN_ROUTE_NUMBER = "RouteNumber";
        public static final String  COLUMN_ROUTE_SERVICE_TYPE = "RouteServiceType";     // Ordinary, Vajra, Vayu Vajra...
        public static final String  COLUMN_ROUTE_DIRECTION = "RouteDirection";
        public static final String  COLUMN_ROUTE_DIRECTION_NAME = "RouteDirectionName";
    }

    public static class RouteTimings implements BaseColumns
    {
        public static final String  TABLE_NAME = "RouteTimings";
        public static final String  COLUMN_ROUTE_ID = "RouteId";
        public static final String  COLUMN_ROUTE_DEPARTURE_TIME = "RouteDepartureTime";
    }

    public static class RouteStops implements BaseColumns
    {
        public static final String  TABLE_NAME = "RouteStops";
        public static final String  COLUMN_ROUTE_ID = "RouteId";
        public static final String  COLUMN_STOP_ID = "StopId";
        public static final String  COLUMN_ROUTE_ORDER = "RouteOrder";
    }

    public static class BusStops
    {
        public static final String  TABLE_NAME = "BusStops";
        public static final String  COLUMN_STOP_ID = "StopId";
        public static final String  COLUMN_STOP_NAME = "StopName";
        public static final String  COLUMN_STOP_LAT = "StopLat";
        public static final String  COLUMN_STOP_LONG = "StopLong";
        public static final String  COLUMN_STOP_DIRECTION_NAME = "StopDirectionName";
    }
}
