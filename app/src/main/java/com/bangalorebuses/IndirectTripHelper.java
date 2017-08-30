package com.bangalorebuses;

import java.util.ArrayList;

interface IndirectTripHelper
{
    void onTransitPointsWithNumberOfRoutesFound(ArrayList<TransitPoint> transitPoints, String numberOfRoutesType);

    void onRoutesOnLeg1Found(ArrayList<IndirectTrip> indirectTrips);

    void onBusesInServiceFound(String errorMessage, IndirectTrip indirectTrip);

    void onRoutesOnLeg2Found(ArrayList<IndirectTrip> indirectTrips);
}