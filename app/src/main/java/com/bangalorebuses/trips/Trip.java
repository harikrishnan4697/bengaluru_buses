package com.bangalorebuses.trips;

import com.bangalorebuses.core.BusStop;

public abstract class Trip
{
    private BusStop originBusStop;
    private String destinationBusStopName;

    public BusStop getOriginBusStop()
    {
        return originBusStop;
    }

    public void setOriginBusStop(BusStop originBusStop)
    {
        this.originBusStop = originBusStop;
    }

    public String getDestinationBusStopName()
    {
        return destinationBusStopName;
    }

    public void setDestinationBusStopName(String destinationBusStopName)
    {
        this.destinationBusStopName = destinationBusStopName;
    }

    public abstract void showTrip(TripsRecyclerViewAdapter.TripsViewHolder holder);
}