package com.bangalorebuses.trips;

import com.bangalorebuses.core.BusRoute;

import java.util.ArrayList;

public class TransitPoint
{
    private String transitPointName;
    private int numberOfRoutesBetweenOriginAndTransitPoint;
    private int numberOfRoutesBetweenTransitPointAndDestination;
    private int transitPointScore;
    private ArrayList<BusRoute> busRoutesToTransitPoint;
    private ArrayList<BusRoute> busRoutesFromTransitPoint;

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
}