package com.bangalorebuses;

interface TripPlannerHelper
{

    /*
    void onDirectBusesFound(String errorMessage, Bus[] buses);

    void onIndirectBusesFound(String errorMessage, Bus[] buses, BusStop transitPoint, String routeMessage);

    void onTransitPointsFound(String errorMessage, BusStop[] transitPoints);

    void onTransitPointBusCountFound(String errorMessage, int originToTransitPointBusCount, int transitPointToDestinationBusCount, BusStop transitPoint);*/

    void onBusesEnDirectRouteFound(String errorMessage, BusRoute busRoute);
}