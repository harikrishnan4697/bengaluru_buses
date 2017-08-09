package com.bangalorebuses;

import java.util.ArrayList;

/**
 * This class is used to keep all details about a bus route together.
 *
 * @author Nihar Thakkar
 * @version 1.0
 * @since 18-6-2017
 */

class BusRoute
{
    private int busRouteId;
    private String busRouteNumber;
    private String busRouteServiceType;
    private String busRouteDirection;
    private String busRouteDirectionName;
    private ArrayList<BusStop> busRouteStops;
    private ArrayList<String> busRouteDepartureTimings;

    public int getBusRouteId()
    {
        return busRouteId;
    }

    public void setBusRouteId(int busRouteId)
    {
        this.busRouteId = busRouteId;
    }

    public String getBusRouteNumber()
    {
        return busRouteNumber;
    }

    public void setBusRouteNumber(String busRouteNumber)
    {
        this.busRouteNumber = busRouteNumber;
    }

    public String getBusRouteServiceType()
    {
        return busRouteServiceType;
    }

    public void setBusRouteServiceType(String busRouteServiceType)
    {
        this.busRouteServiceType = busRouteServiceType;
    }

    public String getBusRouteDirection()
    {
        return busRouteDirection;
    }

    public void setBusRouteDirection(String busRouteDirection)
    {
        this.busRouteDirection = busRouteDirection;
    }

    public String getBusRouteDirectionName()
    {
        return busRouteDirectionName;
    }

    public void setBusRouteDirectionName(String busRouteDirectionName)
    {
        this.busRouteDirectionName = busRouteDirectionName;
    }

    public ArrayList<String> getBusRouteDepartureTimings()
    {
        return busRouteDepartureTimings;
    }

    public void setBusRouteDepartureTimings(ArrayList<String> busRouteDepartureTimings)
    {
        this.busRouteDepartureTimings = busRouteDepartureTimings;
    }

    public ArrayList<BusStop> getBusRouteStops()
    {
        return busRouteStops;
    }

    public void setBusRouteStops(ArrayList<BusStop> busRouteStops)
    {
        this.busRouteStops = busRouteStops;
    }
}