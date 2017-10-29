package com.bangalorebuses.core;

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

public class BusRoute implements Serializable
{
    private int busRouteId;
    private String busRouteNumber;
    private String busRouteDirection;
    private String busRouteDirectionName;
    private ArrayList<BusStop> busRouteStops = new ArrayList<>();
    private ArrayList<Bus> busRouteBuses = new ArrayList<>();
    private int selectedBusStopRouteOrder;

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

    public void addBusToBusRouteBuses(Bus bus)
    {
        busRouteBuses.add(bus);
    }

    public int getSelectedBusStopRouteOrder()
    {
        return selectedBusStopRouteOrder;
    }

    public void setSelectedBusStopRouteOrder(int selectedBusStopRouteOrder)
    {
        this.selectedBusStopRouteOrder = selectedBusStopRouteOrder;
    }
}