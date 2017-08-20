package com.bangalorebuses;

import java.io.Serializable;

class BusStop implements Serializable
{
    private String busStopName;
    private String busStopLat;
    private String busStopLong;
    private int busStopId;
    private int busStopRouteOrder;
    private String busStopDirectionName;
    private String busStopDistance;
    private boolean isAirportShuttleStop = false;
    private boolean isMetroFeederStop = false;

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

    public String getBusStopLat()
    {
        return busStopLat;
    }

    public void setBusStopLat(String busStopLat)
    {
        this.busStopLat = busStopLat;
    }

    public String getBusStopLong()
    {
        return busStopLong;
    }

    public void setBusStopLong(String busStopLong)
    {
        this.busStopLong = busStopLong;
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

    public boolean isAirportShuttleStop()
    {
        return isAirportShuttleStop;
    }

    public void setAirportShuttleStop(boolean airportShuttleStop)
    {
        isAirportShuttleStop = airportShuttleStop;
    }

    public boolean isMetroFeederStop()
    {
        return isMetroFeederStop;
    }

    public void setMetroFeederStop(boolean metroFeederStop)
    {
        isMetroFeederStop = metroFeederStop;
    }
}