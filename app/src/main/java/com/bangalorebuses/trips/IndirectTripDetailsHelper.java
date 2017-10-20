package com.bangalorebuses.trips;

import java.util.ArrayList;

interface IndirectTripDetailsHelper
{
    void onBusETAsOnLeg1BusRouteFound(String errorMessage, IndirectTrip indirectTrip);

    void onIndirectTripsFound(ArrayList<IndirectTrip> indirectTrips);
}
