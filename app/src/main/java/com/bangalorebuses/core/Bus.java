package com.bangalorebuses.core;

import java.io.Serializable;

/**
 * This class is used to keep track of all the characteristics
 * of a bus.
 *
 * @author Nihar Thakkar
 * @version 1.0
 * @since 23-6-2017
 */

public class Bus implements Serializable
{
    private String busRegistrationNumber;
    private int busETA;
    private int busRouteOrder;
    private boolean isDue;
    private BusRoute busRoute;
    private String busCurrentlyNearBusStop;

    public String getBusRegistrationNumber()
    {
        return busRegistrationNumber;
    }

    public void setBusRegistrationNumber(String busRegistrationNumber)
    {
        this.busRegistrationNumber = busRegistrationNumber;
    }

    public int getBusETA()
    {
        return busETA;
    }

    public void setBusETA(int busETA)
    {
        this.busETA = busETA;
    }

    public int getBusRouteOrder()
    {
        return busRouteOrder;
    }

    public void setBusRouteOrder(int busRouteOrder)
    {
        this.busRouteOrder = busRouteOrder;
    }

    public boolean isDue()
    {
        return isDue;
    }

    public void setDue(boolean due)
    {
        isDue = due;
    }

    public BusRoute getBusRoute()
    {
        return busRoute;
    }

    public void setBusRoute(BusRoute busRoute)
    {
        this.busRoute = busRoute;
    }

    public String getBusCurrentlyNearBusStop()
    {
        return busCurrentlyNearBusStop;
    }

    public void setBusCurrentlyNearBusStop(String busCurrentlyNearBusStop)
    {
        this.busCurrentlyNearBusStop = busCurrentlyNearBusStop;
    }
}