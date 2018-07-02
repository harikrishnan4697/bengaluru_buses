package com.bangalorebuses.trips;

import com.bangalorebuses.core.BusRoute;
import com.bangalorebuses.core.BusStop;

public class DirectTrip
{
    private BusStop originBusStop;
    private BusStop destinationBusStop;
    private BusRoute busRoute;
    private int tripDuration;

    BusRoute getBusRoute()
    {
        return busRoute;
    }

    void setBusRoute(BusRoute busRoute)
    {
        this.busRoute = busRoute;
    }

    int getTripDuration()
    {
        return tripDuration;
    }

    void setTripDuration(int tripDuration)
    {
        this.tripDuration = tripDuration;
    }

    BusStop getOriginBusStop()
    {
        return originBusStop;
    }

    void setOriginBusStop(BusStop originBusStop)
    {
        this.originBusStop = originBusStop;
    }

    BusStop getDestinationBusStop()
    {
        return destinationBusStop;
    }

    void setDestinationBusStop(BusStop destinationBusStop)
    {
        this.destinationBusStop = destinationBusStop;
    }
}