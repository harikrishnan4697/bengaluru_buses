package com.bangalorebuses;

import android.database.sqlite.SQLiteDatabase;

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
    static final String SEARCH_TYPE_BUS_STOP_WITH_DIRECTION = "BUS_STOP_WITH_DIRECTION";
    static final String SEARCH_TYPE_BUS_STOP = "BUS_STOP";
    static final String SEARCH_TYPE_BUS_ROUTE = "BUS_ROUTE";
    static final int SEARCH_NEARBY_BUS_STOP_REQUEST_CODE = 7;
    static final int SEARCH_START_BUS_STOP_REQUEST_CODE = 20;
    static final int SEARCH_END_BUS_STOP_REQUEST_CODE = 47;
    static final int SEARCH_REQUEST_CODE = 13;
    static final int LOCATION_PERMISSION_REQUEST_CODE = 36;
    static final String ROUTE_SEARCH_HISTORY_FILENAME = "route_search_history";
    static final String TRIP_TYPE_DIRECT = "TYPE_DIRECT_TRIP";
    static final String TRIP_TYPE_INDIRECT = "TYPE_INDIRECT";
    // Constants for db query tasks
    static final String NUMBER_OF_ROUTES_TYPE_ORIGIN_TO_TRANSIT_POINT = "ORIGIN-TRANSIT_POINT";
    static final String NUMBER_OF_ROUTES_TYPE_TRANSIT_POINT_TO_DESTINATION = "TRANSIT_POINT-DESTINATION";
    static SQLiteDatabase db;
}