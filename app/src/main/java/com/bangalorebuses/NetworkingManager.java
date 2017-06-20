package com.bangalorebuses;

import org.json.JSONArray;

/**
 * This is an interface used for callback methods.
 *
 * @author Nihar Thakkar
 * @version 1.0
 * @since 18-6-2017
 */

interface NetworkingManager
{
    /**
     * This is a callback method called by the GetNearestBusStopsTask.
     *
     * @param isError       This parameter is to convey if the task encountered an error.
     * @param busStopsArray This parameter is a JSONArray of the bus stops the task
     *                      found.
     */
    void onBusStopsFound(boolean isError, JSONArray busStopsArray);

    /**
     * This is a callback method called by the GetBusRouteDetailsTask.
     *
     * @param isError        This parameter is to convey if the task encountered an error.
     * @param route          This parameter is a Route object with all the details set.
     * @param isForBusList   This parameter is returned back as it was passed to the
     *                       constructor. If true, the bus route details are for
     *                       the list of buses at route. Else, the bus route details
     *                       are for a route number the user entered manually.
     * @param routeDirection This parameter is to convey if the route number that was passed
     *                       to the task had a direction of UP or DN.
     */
    void onBusRouteDetailsFound(boolean isError, Route route, boolean isForBusList, String routeDirection);

    /**
     * This is a callback method called by the GetStopsOnBusRouteTask.
     *
     * @param isError       This parameter is to convey if the task encountered an error.
     * @param stopListArray This parameter is a JSONArray of all the bus stops
     *                      for a particular route id.
     */
    void onStopsOnBusRouteFound(boolean isError, JSONArray stopListArray);

    /**
     * This is a callback method called by the GetBusesEnRouteTask.
     *
     * @param isError            This parameter is to convey if the task encountered an error.
     * @param buses              This parameter is an array of buses en-route that the task found.
     * @param numberOfBusesFound This parameter is the number of en-route buses the task found.
     */
    void onBusesEnRouteFound(boolean isError, Bus[] buses, int numberOfBusesFound);

    /**
     * This is a callback method called by the GetTimeToBusesTask.
     *
     * @param isError This parameter is to convey if the task encountered an error.
     * @param buses   This parameter is an array of buses with their time to bus stop set.
     */
    void onTimeToBusesFound(boolean isError, Bus[] buses);

    /**
     * This is a callback method called by the GetBusesAtStopTask.
     *
     * @param isError This parameter is to convey if the task encountered an error.
     * @param buses   This parameter is a JSONArray of arriving at a bus stop.
     */
    void onBusesAtStopFound(boolean isError, JSONArray buses);
}