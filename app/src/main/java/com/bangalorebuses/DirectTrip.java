package com.bangalorebuses;

import java.io.Serializable;
import java.util.ArrayList;

class DirectTrip implements Serializable
{
    private BusStop originStop = new BusStop();
    private BusStop destinationStop = new BusStop();
    private ArrayList<BusRoute> busRoutes = new ArrayList<>();
    private int shortestTravelTime;
    private String nextThreeBusArrivals;

    public BusStop getOriginStop()
    {
        return originStop;
    }

    public void setOriginStop(BusStop originStop)
    {
        this.originStop = originStop;
    }

    public BusStop getDestinationStop()
    {
        return destinationStop;
    }

    public void setDestinationStop(BusStop destinationStop)
    {
        this.destinationStop = destinationStop;
    }

    public ArrayList<BusRoute> getBusRoutes()
    {
        return busRoutes;
    }

    public void setBusRoutes(ArrayList<BusRoute> busRoutes)
    {
        this.busRoutes = busRoutes;
    }

    public void addBusRoute(BusRoute busRoute)
    {
        this.busRoutes.add(busRoute);
    }

    public int getShortestTravelTime()
    {
        return shortestTravelTime;
    }

    public void setShortestTravelTime(int shortestTravelTime)
    {
        this.shortestTravelTime = shortestTravelTime;
    }

    public String getNextThreeBusArrivals()
    {
        return nextThreeBusArrivals;
    }

    public void setNextThreeBusArrivals(String next3BusArrivals)
    {
        this.nextThreeBusArrivals = next3BusArrivals;
    }
}