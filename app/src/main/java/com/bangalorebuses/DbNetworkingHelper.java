package com.bangalorebuses;

/**
 * This is an interface used for callback methods.
 *
 * @author Nihar Thakkar
 * @version 1.0
 * @since 25-7-2017
 */

interface DbNetworkingHelper
{
    void onStopsWithNameDbTaskComplete(String errorMessage, String busStopNameToSearchFor, BusStop[] busStops);

    void onBusRouteDetailsDbTaskComplete(String errorMessage, Route route);

    void onStopsOnBusRouteDbTaskComplete(String errorMessage, Route route, BusStop[] busStops);

    void onBusesEnRouteDbTaskComplete(String errorMessage, Route route);
}