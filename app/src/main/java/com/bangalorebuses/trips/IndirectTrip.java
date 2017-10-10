package com.bangalorebuses.trips;

import android.view.View;

import com.bangalorebuses.R;
import com.bangalorebuses.core.Bus;
import com.bangalorebuses.core.BusRoute;
import com.bangalorebuses.core.BusStop;
import com.bangalorebuses.utils.DbQueries;

import static com.bangalorebuses.utils.Constants.db;

public class IndirectTrip extends Trip
{
    private int tripDuration;
    private Bus busOnFirstLeg;
    private BusRoute busRouteOnFirstLeg;
    private Bus busOnSecondLeg;
    private BusRoute busRouteOnSecondLeg;
    private BusStop originBusStop;
    private TransitPoint transitPoint;
    private BusStop destinationBusStop;

    @Override
    public void showTrip(TripsRecyclerViewAdapter.TripsViewHolder holder)
    {

    }

    public Bus getBusOnFirstLeg()
    {
        return busOnFirstLeg;
    }

    public void setBusOnFirstLeg(Bus busOnFirstLeg)
    {
        this.busOnFirstLeg = busOnFirstLeg;
    }

    public Bus getBusOnSecondLeg()
    {
        return busOnSecondLeg;
    }

    public void setBusOnSecondLeg(Bus busOnSecondLeg)
    {
        this.busOnSecondLeg = busOnSecondLeg;
    }

    public TransitPoint getTransitPoint()
    {
        return transitPoint;
    }

    public void setTransitPoint(TransitPoint transitPoint)
    {
        this.transitPoint = transitPoint;
    }

    public int getTripDuration()
    {
        return tripDuration;
    }

    public void setTripDuration(int tripDuration)
    {
        this.tripDuration = tripDuration;
    }

    @Override
    public BusStop getOriginBusStop()
    {
        return originBusStop;
    }

    @Override
    public void setOriginBusStop(BusStop originBusStop)
    {
        this.originBusStop = originBusStop;
    }

    public BusStop getDestinationBusStop()
    {
        return destinationBusStop;
    }

    public void setDestinationBusStop(BusStop destinationBusStop)
    {
        this.destinationBusStop = destinationBusStop;
    }

    public BusRoute getBusRouteOnFirstLeg()
    {
        return busRouteOnFirstLeg;
    }

    public void setBusRouteOnFirstLeg(BusRoute busRouteOnFirstLeg)
    {
        this.busRouteOnFirstLeg = busRouteOnFirstLeg;
    }

    public BusRoute getBusRouteOnSecondLeg()
    {
        return busRouteOnSecondLeg;
    }

    public void setBusRouteOnSecondLeg(BusRoute busRouteOnSecondLeg)
    {
        this.busRouteOnSecondLeg = busRouteOnSecondLeg;
    }
}