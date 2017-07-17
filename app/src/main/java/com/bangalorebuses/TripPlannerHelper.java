package com.bangalorebuses;

/**
 * Created by nihar on 13/07/17.
 */

interface TripPlannerHelper
{

    /**
     * This is a callback method called by the GetDirectBusesTask.
     *
     * @param errorMessage This parameter is to convey an error message.
     * @param buses        This parameter is a Bus[] of buses from the origin
     *                     bus stop to the destination bus stop.
     */
    void onDirectBusesFound(String errorMessage, Bus[] buses);

    /**
     * This is a callback method called by the GetDirectBusesTask.
     *
     * @param errorMessage This parameter is to convey an error message.
     * @param buses        This parameter is a Bus[] of buses from the origin
     *                     to the destination.
     */
    void onIndirectBusesFound(String errorMessage, Bus[] buses, BusStop transitPoint, String routeMessage);

    /**
     * This is a callback method called by the GetTransitPointsTask.
     *
     * @param errorMessage  This parameter is to convey an error message.
     * @param transitPoints This parameter is a BusStop[] of transit points.
     */
    void onTransitPointsFound(String errorMessage, BusStop[] transitPoints);

    /**
     * This is a callback method called by the GetTransitPointsTask.
     *
     * @param errorMessage                      This parameter is to convey an error message.
     * @param originToTransitPointBusCount      This parameter is the bus count from origin to
     *                                          transit point.
     * @param transitPointToDestinationBusCount This parameter is the bus count from transit point
     *                                          to destination.
     */
    void onTransitPointBusCountFound(String errorMessage, int originToTransitPointBusCount, int transitPointToDestinationBusCount, BusStop transitPoint);

}
