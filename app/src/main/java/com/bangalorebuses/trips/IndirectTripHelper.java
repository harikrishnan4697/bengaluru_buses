package com.bangalorebuses.trips;

import com.bangalorebuses.core.BusRoute;

import java.util.ArrayList;

/**
 * /**
 * This interface is used for call-back methods from indirect trip related
 * Db tasks to the TripPlannerFragment.
 *
 * @author Nihar Thakkar
 * @version 1.0
 * @since 5-9-2017
 */

public interface IndirectTripHelper
{
    /**
     * This call-back method is called by TransitPointsWithNumberOfRoutesDbTask
     *
     * @param transitPoints This is an ArrayList of transit points with the number
     *                      of bus routes between the origin and transit point.
     */
    void onTransitPointsAndRouteCountOriginToTPFound(ArrayList<TransitPoint> transitPoints);

    /**
     * This call-back method is called by TransitPointsWithNumberOfRoutesDbTask
     *
     * @param transitPoints This is an ArrayList of transit points with the number
     *                      of bus routes between the transit point and the destination.
     */
    void onTransitPointsAndRouteCountTPToDestFound(ArrayList<TransitPoint> transitPoints);

    void onMostFrequentBusRouteFound(String transitPointBusStopName, BusRoute mostFrequentBusRouteOnFirstLeg,
                                     BusRoute mostFrequentBusRouteOnSecondLeg);
}