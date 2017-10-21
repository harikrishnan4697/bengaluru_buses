package com.bangalorebuses.trips;

import com.bangalorebuses.core.BusStop;

class TransitPoint extends BusStop
{
    private int numberOfRoutesBetweenOriginAndTransitPoint;
    private int numberOfRoutesBetweenTransitPointAndDestination;
    private int transitPointScore;

    int getNumberOfRoutesBetweenOriginAndTransitPoint()
    {
        return numberOfRoutesBetweenOriginAndTransitPoint;
    }

    void setNumberOfRoutesBetweenOriginAndTransitPoint(int numberOfRoutesBetweenOriginAndTransitPoint)
    {
        this.numberOfRoutesBetweenOriginAndTransitPoint = numberOfRoutesBetweenOriginAndTransitPoint;
    }

    int getNumberOfRoutesBetweenTransitPointAndDestination()
    {
        return numberOfRoutesBetweenTransitPointAndDestination;
    }

    void setNumberOfRoutesBetweenTransitPointAndDestination(int numberOfRoutesBetweenTransitPointAndDestination)
    {
        this.numberOfRoutesBetweenTransitPointAndDestination = numberOfRoutesBetweenTransitPointAndDestination;
    }

    int getTransitPointScore()
    {
        return transitPointScore;
    }

    void setTransitPointScore(int transitPointScore)
    {
        this.transitPointScore = transitPointScore;
    }
}