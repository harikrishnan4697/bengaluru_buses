package com.bangalorebuses;

/**
 * This class is used to store all the constant values in the app.
 *
 * @author Nihar Thakkar
 * @version 1.0
 * @since 21-6-2017
 */

class Constants
{
    // Constants for networking tasks
    static final int NETWORK_QUERY_CONNECT_TIMEOUT = 10000;
    static final int NETWORK_QUERY_READ_TIMEOUT = 35000;
    static final String NETWORK_QUERY_NO_ERROR = "NO_ERROR_OCCURRED";
    static final String NETWORK_QUERY_URL_EXCEPTION = "ERROR_MALFORMED_URL";
    static final String NETWORK_QUERY_REQUEST_TIMEOUT_EXCEPTION = "ERROR_REQUEST_TIMED_OUT";
    static final String NETWORK_QUERY_IO_EXCEPTION = "ERROR_IO_EXCEPTION";
    static final String NETWORK_QUERY_JSON_EXCEPTION = "ERROR_JSON_EXCEPTION";
    static final String ROUTE_TYPE_ORIGIN_TO_TRANSIT_POINT = "ORIGIN_TO_TRANSIT_POINT";
    static final String ROUTE_TYPE_TRANSIT_POINT_TO_DESTINATION = "TRANSIT_POINT_TO_DESTINATION";
    static final String DIRECTION_UP = "UP";
    static final String DIRECTION_DOWN = "DN";

    // Constants for fragments
    static final String SEARCH_TYPE_BUS_STOP = "Bus_Stop";
    static final String SEARCH_TYPE_BUS_ROUTE = "Bus_Route";
    static final int SEARCH_NEARBY_BUS_STOP_REQUEST_CODE = 7;
    static final int SEARCH_START_BUS_STOP_REQUEST_CODE = 20;
    static final int SEARCH_END_BUS_STOP_REQUEST_CODE = 47;
    static final int SEARCH_REQUEST_CODE = 13;
    static final int LOCATION_PERMISSION_REQUEST_CODE = 36;
    static final String ROUTE_SEARCH_HISTORY_FILENAME = "route_search_history";
    static final String SQL_CREATE_ROUTES_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + BengaluruBusesContract.Routes.TABLE_NAME + " (" +
                    BengaluruBusesContract.Routes.COLUMN_ROUTE_ID + " INTEGER PRIMARY KEY," +
                    BengaluruBusesContract.Routes.COLUMN_ROUTE_NUMBER + " TEXT," +
                    BengaluruBusesContract.Routes.COLUMN_ROUTE_SERVICE_TYPE + " TEXT," +
                    BengaluruBusesContract.Routes.COLUMN_ROUTE_DIRECTION + " TEXT," +
                    BengaluruBusesContract.Routes.COLUMN_ROUTE_DIRECTION_NAME + " TEXT)";


    // Db table creation commands
    static final String SQL_CREATE_ROUTE_STOPS_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + BengaluruBusesContract.RouteStops.TABLE_NAME + " (" +
                    BengaluruBusesContract.RouteStops._ID + " INTEGER PRIMARY KEY," +
                    BengaluruBusesContract.RouteStops.COLUMN_ROUTE_ORDER + " INTEGER," +
                    BengaluruBusesContract.RouteStops.COLUMN_STOP_ID + " INTEGER REFERENCES " + BengaluruBusesContract.BusStops.TABLE_NAME + "(" + BengaluruBusesContract.BusStops.COLUMN_STOP_ID + ")," +
                    BengaluruBusesContract.RouteStops.COLUMN_ROUTE_ID + " INTEGER REFERENCES " + BengaluruBusesContract.Routes.TABLE_NAME + "(" + BengaluruBusesContract.Routes.COLUMN_ROUTE_ID + "))";
    static final String SQL_CREATE_ROUTE_TIMINGS_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + BengaluruBusesContract.RouteTimings.TABLE_NAME + " (" +
                    BengaluruBusesContract.RouteTimings._ID + " INTEGER PRIMARY KEY," +
                    BengaluruBusesContract.RouteTimings.COLUMN_ROUTE_DEPARTURE_TIME + " TEXT," +
                    BengaluruBusesContract.RouteTimings.COLUMN_ROUTE_ID + " INTEGER REFERENCES " + BengaluruBusesContract.Routes.TABLE_NAME + "(" + BengaluruBusesContract.Routes.COLUMN_ROUTE_ID + "))";
    static final String SQL_CREATE_BUS_STOPS_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + BengaluruBusesContract.BusStops.TABLE_NAME + " (" +
                    BengaluruBusesContract.BusStops.COLUMN_STOP_ID + " INTEGER PRIMARY KEY," +
                    BengaluruBusesContract.BusStops.COLUMN_STOP_NAME + " TEXT," +
                    BengaluruBusesContract.BusStops.COLUMN_STOP_LAT + " REAL," +
                    BengaluruBusesContract.BusStops.COLUMN_STOP_LONG + " REAL," +
                    BengaluruBusesContract.BusStops.COLUMN_STOP_DIRECTION_NAME + " TEXT)";
    // Nearest bus stop
    static BusStop nearestBusStop;
}