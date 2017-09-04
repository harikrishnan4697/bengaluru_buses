package com.bangalorebuses;

import java.util.ArrayList;

interface IndirectTripHelper
{
    void onTransitPointsAndRouteCountOriginToTPFound(ArrayList<TransitPoint> transitPoints);

    void onTransitPointsAndRouteCountTPToDestFound(ArrayList<TransitPoint> transitPoints);

    void onRoutesToAndFromTransitPointFound(TransitPoint transitPoint);

    void onBusesInServiceFound(String errorMessage, BusRoute busRoute, TransitPoint transitPoint);
}