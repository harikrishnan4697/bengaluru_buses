package com.bangalorebuses.trips;

import com.bangalorebuses.core.BusRoute;

interface IndirectTripDetailsHelper
{
    /**
     * This call-back is called by BusRoutesToAndFromTransitPointDbTask.
     *
     * @param transitPoint This is a transit point with a list of bus routes
     *                     arriving from the origin and departing to the
     *                     destination.
     */
    void onBusRoutesToAndFromTransitPointFound(TransitPoint transitPoint);

    /**
     * This call-back method is called by BusETAsOnLeg1Task.
     *
     * @param errorMessage This is a message to convey if the task encountered
     *                     an error.
     * @param busRoute     This is the bus route that the task got bus ETAs of.
     * @param transitPoint This is the transit point that the bus route will
     *                     go to.
     */
    void onBusETAsOnLeg1BusRouteFound(String errorMessage, BusRoute busRoute,
                                      TransitPoint transitPoint);
}
