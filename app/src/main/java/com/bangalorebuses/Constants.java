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

    // Constants for fragments
    static final String SEARCH_TYPE_BUS_STOP = "Bus_Stop";
    static final String SEARCH_TYPE_BUS_ROUTE = "Bus_Route";
    static final int SEARCH_START_BUS_STOP_REQUEST_CODE = 20;
    static final int SEARCH_END_BUS_STOP_REQUEST_CODE = 47;
    static final int SEARCH_REQUEST_CODE = 13;
    static final int LOCATION_PERMISSION_REQUEST_CODE = 36;
}