package com.bangalorebuses;

class DirectTrip
{
    private BusStop originStop = new BusStop();
    private BusStop destinationStop = new BusStop();
    private BusRoute route = new BusRoute();
    private int travelTime;

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

    public BusRoute getRoute()
    {
        return route;
    }

    public void setRoute(BusRoute route)
    {
        this.route = route;
    }

    public int getTravelTime()
    {
        return travelTime;
    }

    public void setTravelTime(int travelTime)
    {
        this.travelTime = travelTime;
    }
}