package com.bangalorebuses.utils;

import com.bangalorebuses.core.BusRoute;

import java.util.ArrayList;

/**
 * This interface is used for call-back methods from Db tasks to Activities/Fragments.
 *
 * @author Nihar Thakkar
 * @version 1.0
 * @since 5-9-2017
 */

public interface DbQueryHelper
{
    /**
     * This is a call-back method called by BusRoutesArrivingAtBusStopTask.
     *
     * @param busRoutes An ArrayList of bus routes arriving at a bus stop.
     */
    void onBusRoutesArrivingAtBusStopFound(ArrayList<BusRoute> busRoutes);
}