package com.bangalorebuses.utils;

import android.database.sqlite.SQLiteDatabase;

/**
 * This class is used to store all the constant values in the app.
 *
 * @author Nihar Thakkar
 * @version 1.0
 * @since 21-6-2017
 */

public class Constants
{
    // Constants for networking tasks
    public static final int NETWORK_QUERY_CONNECT_TIMEOUT = 10000;
    public static final int NETWORK_QUERY_READ_TIMEOUT = 35000;
    public static final String NETWORK_QUERY_NO_ERROR = "NO_ERROR_OCCURRED";
    public static final String NETWORK_QUERY_URL_EXCEPTION = "ERROR_MALFORMED_URL";
    public static final String NETWORK_QUERY_REQUEST_TIMEOUT_EXCEPTION = "ERROR_REQUEST_TIMED_OUT";
    public static final String NETWORK_QUERY_IO_EXCEPTION = "ERROR_IO_EXCEPTION";
    public static final String NETWORK_QUERY_JSON_EXCEPTION = "ERROR_JSON_EXCEPTION";
    public static final String ROUTE_TYPE_ORIGIN_TO_TRANSIT_POINT = "ORIGIN_TO_TRANSIT_POINT";
    public static final String ROUTE_TYPE_TRANSIT_POINT_TO_DESTINATION = "TRANSIT_POINT_TO_DESTINATION";
    public static final String DIRECTION_UP = "UP";
    public static final String DIRECTION_DOWN = "DN";

    // Constants for fragments
    public static final String SEARCH_TYPE_BUS_STOP_WITH_DIRECTION = "BUS_STOP_WITH_DIRECTION";
    public static final String SEARCH_TYPE_BUS_STOP = "BUS_STOP";
    public static final String SEARCH_TYPE_BUS_ROUTE = "BUS_ROUTE";
    public static final int SEARCH_NEARBY_BUS_STOP_REQUEST_CODE = 7;
    public static final int SEARCH_START_BUS_STOP_REQUEST_CODE = 20;
    public static final int SEARCH_END_BUS_STOP_REQUEST_CODE = 47;
    public static final int SEARCH_REQUEST_CODE = 13;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 36;
    public static final String ROUTE_SEARCH_HISTORY_FILENAME = "route_search_history";

    // Constants for db query tasks
    public static final String NUMBER_OF_ROUTES_TYPE_ORIGIN_TO_TRANSIT_POINT = "ORIGIN-TRANSIT_POINT";
    public static final String NUMBER_OF_ROUTES_TYPE_TRANSIT_POINT_TO_DESTINATION = "TRANSIT_POINT-DESTINATION";
    // Constants for the Trip Planner Fragment
    public static final String TRIP_PLANNER_TITLE = "Trip Planner (Beta)";
    public static final String ORIGIN_BUS_STOP_SEARCH_HINT = "Starting bus stop";
    public static final String DESTINATION_BUS_STOP_SEARCH_HINT = "Ending bus stop";
    // Constants for the Bus Tracker Fragment
    public static final String BUS_TRACKER_TITLE = "Bus Tracker";
    // Constants for the Nearby Fragment
    public static final String NEARBY_TITLE = "Bus Stops Nearby";
    public static SQLiteDatabase db;

    // Favourites constants
    public static String FAVOURITES_FILE_NAME = "Favourites.txt";
}