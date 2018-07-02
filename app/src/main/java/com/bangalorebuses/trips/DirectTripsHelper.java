package com.bangalorebuses.trips;

import java.util.ArrayList;

/**
 * /**
 * This interface is used for call-back methods from direct trip related
 * Db tasks to the TripPlannerFragment.
 *
 * @author Nihar Thakkar
 * @version 1.0
 * @since 5-9-2017
 */

interface DirectTripsHelper
{
    void onDirectTripsFound(ArrayList<DirectTrip> directTrips);

    void onBusETAsOnDirectTripFound(String errorMessage, DirectTrip directTrip);
}