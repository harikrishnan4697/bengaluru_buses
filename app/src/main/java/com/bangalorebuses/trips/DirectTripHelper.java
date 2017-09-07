package com.bangalorebuses.trips;

/**
 * /**
 * This interface is used for call-back methods from direct trip related
 * Db tasks to the TripPlannerFragment.
 *
 * @author Nihar Thakkar
 * @version 1.0
 * @since 5-9-2017
 */

public interface DirectTripHelper
{
    /**
     * This is a call-back method called by BusETAsOnDirectTripTask.
     *
     * @param errorMessage Message to convey if the task encountered an error.
     * @param directTrip   The direct trip with bus ETAs
     */
    void onBusETAsOnDirectTripFound(String errorMessage, DirectTrip directTrip);
}