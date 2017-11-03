package com.bangalorebuses.core;

import java.io.Serializable;
import java.util.ArrayList;

public class BusStop implements Serializable
{
    private String busStopName;
    private int busStopId;
    private int busStopRouteOrder;
    private String busStopDirectionName;
    private String busStopDistance;
    private float busStopLat;
    private float busStopLong;
    private ArrayList<BusRoute> busesArrivingAtBusStop;

    public BusStop()
    {
    }

    public int getBusStopId()
    {
        return busStopId;
    }

    public void setBusStopId(int busStopId)
    {
        this.busStopId = busStopId;
    }

    public String getBusStopName()
    {
        return busStopName;
    }

    public void setBusStopName(String busStopName)
    {
        this.busStopName = busStopName;
    }

    public int getBusStopRouteOrder()
    {
        return busStopRouteOrder;
    }

    public void setBusStopRouteOrder(int busStopRouteOrder)
    {
        this.busStopRouteOrder = busStopRouteOrder;
    }

    public String getBusStopDirectionName()
    {
        return busStopDirectionName;
    }

    public void setBusStopDirectionName(String busStopDirectionName)
    {
        this.busStopDirectionName = busStopDirectionName;
    }

    public String getBusStopDistance()
    {
        return busStopDistance;
    }

    public void setBusStopDistance(String busStopDistance)
    {
        this.busStopDistance = busStopDistance;
    }

    public ArrayList<BusRoute> getBusesArrivingAtBusStop()
    {
        return busesArrivingAtBusStop;
    }

    public void setBusesArrivingAtBusStop(ArrayList<BusRoute> busesArrivingAtBusStop)
    {
        this.busesArrivingAtBusStop = busesArrivingAtBusStop;
    }

    public float getBusStopLat()
    {
        return busStopLat;
    }

    public void setBusStopLat(float busStopLat)
    {
        this.busStopLat = busStopLat;
    }

    public float getBusStopLong()
    {
        return busStopLong;
    }

    public void setBusStopLong(float busStopLong)
    {
        this.busStopLong = busStopLong;
    }
}