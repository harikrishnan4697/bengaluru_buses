package com.bangalorebuses;

class TransitPoint
{
    private String transitPointName;
    private int numberOfRoutesBetweenOriginAndTransitPoint;
    private int numberOfRoutesBetweenTransitPointAndDestination;

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
}
