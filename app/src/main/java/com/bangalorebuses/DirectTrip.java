package com.bangalorebuses;

class DirectTrip
{
    private BusStop originStop = new BusStop();
    private BusStop destinationStop = new BusStop();
    private Route route = new Route();

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

    public Route getRoute()
    {
        return route;
    }

    public void setRoute(Route route)
    {
        this.route = route;
    }
}
