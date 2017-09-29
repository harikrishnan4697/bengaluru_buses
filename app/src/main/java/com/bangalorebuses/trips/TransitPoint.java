package com.bangalorebuses.trips;

import com.bangalorebuses.core.Bus;
import com.bangalorebuses.core.BusRoute;

import java.util.ArrayList;

public class TransitPoint
{
    private String transitPointName;
    private int numberOfRoutesBetweenOriginAndTransitPoint;
    private int numberOfRoutesBetweenTransitPointAndDestination;
    private int transitPointScore;
    private Bus fastestBusToTransitPoint;
    private Bus fastestBusFromTransitPoint;
    private ArrayList<BusRoute> busRoutesToTransitPoint;
    private ArrayList<BusRoute> busRoutesFromTransitPoint;
    private ArrayList<IndirectTrip> indirectTrips = new ArrayList<>();
    private int shortestTripDuration;

    public String getTransitPointName()
    {
        return transitPointName;
    }

    public void setTransitPointName(String transitPointName)
    {
        this.transitPointName = transitPointName;
    }

    public int getNumberOfRoutesBetweenOriginAndTransitPoint()
    {
        return numberOfRoutesBetweenOriginAndTransitPoint;
    }

    public void setNumberOfRoutesBetweenOriginAndTransitPoint(int numberOfRoutesBetweenOriginAndTransitPoint)
    {
        this.numberOfRoutesBetweenOriginAndTransitPoint = numberOfRoutesBetweenOriginAndTransitPoint;
    }

    public int getNumberOfRoutesBetweenTransitPointAndDestination()
    {
        return numberOfRoutesBetweenTransitPointAndDestination;
    }

    public void setNumberOfRoutesBetweenTransitPointAndDestination(int numberOfRoutesBetweenTransitPointAndDestination)
    {
        this.numberOfRoutesBetweenTransitPointAndDestination = numberOfRoutesBetweenTransitPointAndDestination;
    }

    public int getTransitPointScore()
    {
        return transitPointScore;
    }

    public void setTransitPointScore(int transitPointScore)
    {
        this.transitPointScore = transitPointScore;
    }

    public ArrayList<BusRoute> getBusRoutesToTransitPoint()
    {
        return busRoutesToTransitPoint;
    }

    public void setBusRoutesToTransitPoint(ArrayList<BusRoute> busRoutesToTransitPoint)
    {
        this.busRoutesToTransitPoint = busRoutesToTransitPoint;
    }

    public ArrayList<BusRoute> getBusRoutesFromTransitPoint()
    {
        return busRoutesFromTransitPoint;
    }

    public void setBusRoutesFromTransitPoint(ArrayList<BusRoute> busRoutesFromTransitPoint)
    {
        this.busRoutesFromTransitPoint = busRoutesFromTransitPoint;
    }

    public Bus getFastestBusToTransitPoint()
    {
        return fastestBusToTransitPoint;
    }

    public void setFastestBusToTransitPoint(Bus fastestBusToTransitPoint)
    {
        this.fastestBusToTransitPoint = fastestBusToTransitPoint;
    }

    public Bus getFastestBusFromTransitPoint()
    {
        return fastestBusFromTransitPoint;
    }

    public void setFastestBusFromTransitPoint(Bus fastestBusFromTransitPoint)
    {
        this.fastestBusFromTransitPoint = fastestBusFromTransitPoint;
    }

    public ArrayList<IndirectTrip> getIndirectTrips()
    {
        return indirectTrips;
    }

    public void setIndirectTrips(ArrayList<IndirectTrip> indirectTrips)
    {
        this.indirectTrips = indirectTrips;
    }

    public void addIndirectTrip(IndirectTrip indirectTrip)
    {
        indirectTrips.add(indirectTrip);
    }

    public int getShortestTripDuration()
    {
        return shortestTripDuration;
    }

    public void setShortestTripDuration(int shortestTripDuration)
    {
        this.shortestTripDuration = shortestTripDuration;
    }
}