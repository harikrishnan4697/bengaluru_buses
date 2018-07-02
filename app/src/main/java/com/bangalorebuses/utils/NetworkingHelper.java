package com.bangalorebuses.utils;

import com.bangalorebuses.core.Bus;
import com.bangalorebuses.core.BusRoute;
import com.bangalorebuses.core.BusStop;

import java.util.ArrayList;

/**
 * This interface is used for call-back methods from networking tasks
 * to Activities/Fragments
 *
 * @author Nihar Thakkar
 * @version 2.0
 * @since 18-6-2017
 */

public interface NetworkingHelper
{
    /**
     * This call-back method is called by NearestBusStopsTask.
     *
     * @param errorMessage This is a message to convey if the task encountered an error.
     * @param busStops     This is an ArrayList of nearby bus stops.
     */
    void onBusStopsNearbyFound(String errorMessage, ArrayList<BusStop> busStops);

    /**
     * This call-back method is called by BusETAsOnBusRouteTask.
     *
     * @param errorMessage      This is a message to convey if the task encountered an error.
     * @param busStopRouteOrder This is the route order of the selected bus stop.
     * @param buses             This is an ArrayList of buses in service.
     * @param busRoute          This is the bus route that the buses are on.¬
     */
    void onBusETAsOnBusRouteFound(String errorMessage, int busStopRouteOrder, ArrayList<Bus> buses,
                                  BusRoute busRoute);
}