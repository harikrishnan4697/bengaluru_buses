package com.bangalorebuses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * This class is used to keep all details about a bus route together.
 *
 * @author Nihar Thakkar
 * @version 1.0
 * @since 18-6-2017
 */

class BusRoute implements Serializable
{
    private int busRouteId;
    private String busRouteNumber;
    private String busRouteServiceType;
    private String busRouteDirection;
    private String busRouteDirectionName;
    private ArrayList<BusStop> busRouteStops;
    private ArrayList<Date> busRouteDepartureTimings;
    private ArrayList<Bus> busRouteBuses;
    private int selectedBusStopRouteOrder;
    private BusStop tripPlannerOriginBusStop;
    private BusStop tripPlannerDestinationBusStop;
    private int shortestOriginToDestinationTravelTime;

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

    public ArrayList<Date> getBusRouteDepartureTimings()
    {
        return busRouteDepartureTimings;
    }

    public void setBusRouteDepartureTimings(ArrayList<Date> busRouteDepartureTimings)
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

    public ArrayList<Bus> getBusRouteBuses()
    {
        return busRouteBuses;
    }

    public void setBusRouteBuses(ArrayList<Bus> busRouteBuses)
    {
        this.busRouteBuses = busRouteBuses;
    }

    public int getSelectedBusStopRouteOrder()
    {
        return selectedBusStopRouteOrder;
    }

    public void setSelectedBusStopRouteOrder(int selectedBusStopRouteOrder)
    {
        this.selectedBusStopRouteOrder = selectedBusStopRouteOrder;
    }

    public BusStop getTripPlannerOriginBusStop()
    {
        return tripPlannerOriginBusStop;
    }

    public void setTripPlannerOriginBusStop(BusStop tripPlannerOriginBusStop)
    {
        this.tripPlannerOriginBusStop = tripPlannerOriginBusStop;
    }

    public BusStop getTripPlannerDestinationBusStop()
    {
        return tripPlannerDestinationBusStop;
    }

    public void setTripPlannerDestinationBusStop(BusStop tripPlannerDestinationBusStop)
    {
        this.tripPlannerDestinationBusStop = tripPlannerDestinationBusStop;
    }

    public int getShortestOriginToDestinationTravelTime()
    {
        return shortestOriginToDestinationTravelTime;
    }

    public void setShortestOriginToDestinationTravelTime(int shortestOriginToDestinationTravelTime)
    {
        this.shortestOriginToDestinationTravelTime = shortestOriginToDestinationTravelTime;
    }
}