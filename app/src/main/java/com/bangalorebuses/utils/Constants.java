package com.bangalorebuses.utils;

import android.database.sqlite.SQLiteDatabase;

import java.util.LinkedHashMap;

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
    public static final int NETWORK_QUERY_READ_TIMEOUT = 15000;
    public static final String NETWORK_QUERY_NO_ERROR = "NO_ERROR_OCCURRED";
    public static final String NETWORK_QUERY_URL_EXCEPTION = "ERROR_MALFORMED_URL";
    public static final String NETWORK_QUERY_REQUEST_TIMEOUT_EXCEPTION = "ERROR_REQUEST_TIMED_OUT";
    public static final String NETWORK_QUERY_IO_EXCEPTION = "ERROR_IO_EXCEPTION";
    public static final String NETWORK_QUERY_JSON_EXCEPTION = "ERROR_JSON_EXCEPTION";
    public static final String DIRECTION_UP = "UP";
    public static final String DIRECTION_DOWN = "DN";

    // Request codes
    public static final int FAVORITES_REQUEST_CODE = 4;
    public static final int SEARCH_NEARBY_BUS_STOP_REQUEST_CODE = 7;
    public static final int SEARCH_START_BUS_STOP_REQUEST_CODE = 20;
    public static final int SEARCH_END_BUS_STOP_REQUEST_CODE = 47;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 36;

    // Constants for the Trip Planner Fragment
    public static final String ORIGIN_BUS_STOP_SEARCH_HINT = "Starting Bus Stop...";
    public static final String DESTINATION_BUS_STOP_SEARCH_HINT = "Ending Bus Stop...";

    // Favourites constants
    public static final String BUS_STOP_NAME = "BUS_STOP_NAME";
    public static final String TRIP_ORIGIN_BUS_STOP_NAME = "FAV_ORIGIN_BUS_STOP_NAME";
    public static final String TRIP_DESTINATION_BUS_STOP_NAME = "FAV_DESTINATION_BUS_STOP_NAME";

    // Constants for IndirectTripDetailsActivity
    public static final String ORIGIN_BUS_STOP_NAME = "PARAM_ORIGIN_BUS_STOP_NAME";
    public static final String TRANSIT_POINT_BUS_STOP_NAME = "PARAM_TRANSIT_POINT_BUS_STOP_NAME";
    public static final String DESTINATION_BUS_STOP_NAME = "PARAM_DESTINATION_BUS_STOP_NAME";
    // Nearby Bus Stop Constants
    public static final float NEARBY_BUS_STOPS_RANGE = 1000f;
    // Other not so constants
    public static SQLiteDatabase db;
    public static LinkedHashMap<String, String> favoritesHashMap = new LinkedHashMap<>();
    // MaiActivity Constants
    public static String MAIN_ACTIVITY_FORCEFULLY_KILLED = "MAIN_ACTIVITY_FORCEFULLY_KILLED";
}