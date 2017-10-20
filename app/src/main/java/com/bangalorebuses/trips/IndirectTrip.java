package com.bangalorebuses.trips;

import com.bangalorebuses.core.BusRoute;

import java.util.ArrayList;

class IndirectTrip
{
    private int tripDuration;
    private DirectTrip directTripOnFirstLeg;
    private DirectTrip directTripOnSecondLeg;
    private ArrayList<DirectTrip> possibleDirectTripsOnSecondLeg =
            new ArrayList<>();
    private TransitPoint transitPoint;

    TransitPoint getTransitPoint()
    {
        return transitPoint;
    }

    void setTransitPoint(TransitPoint transitPoint)
    {
        this.transitPoint = transitPoint;
    }

    int getTripDuration()
    {
        return tripDuration;
    }

    void setTripDuration(int tripDuration)
    {
        this.tripDuration = tripDuration;
    }

    DirectTrip getDirectTripOnFirstLeg()
    {
        return directTripOnFirstLeg;
    }

    void setDirectTripOnFirstLeg(DirectTrip directTripOnFirstLeg)
    {
        this.directTripOnFirstLeg = directTripOnFirstLeg;
    }

    DirectTrip getDirectTripOnSecondLeg()
    {
        return directTripOnSecondLeg;
    }

    void setDirectTripOnSecondLeg(DirectTrip directTripOnSecondLeg)
    {
        this.directTripOnSecondLeg = directTripOnSecondLeg;
    }

    ArrayList<DirectTrip> getPossibleDirectTripsOnSecondLeg()
    {
        return possibleDirectTripsOnSecondLeg;
    }

    void setPossibleDirectTripsOnSecondLeg(ArrayList<DirectTrip> possibleDirectTripsOnSecondLeg)
    {
        this.possibleDirectTripsOnSecondLeg = possibleDirectTripsOnSecondLeg;
    }
}