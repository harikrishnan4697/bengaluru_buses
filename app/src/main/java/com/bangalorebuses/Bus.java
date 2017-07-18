package com.bangalorebuses;

/**
 * This class is used to keep track of all the characteristics
 * of a bus.
 *
 * @author Nihar Thakkar
 * @version 1.0
 * @since 23-6-2017
 */

class Bus
{
    private String routeNumber;
    private int serviceID;
    private String latitude;
    private String longitude;
    private int routeOrder;
    private String registrationNumber;
    private String timeToBus;
    private boolean isDue = false;
    private String nameOfStopBusIsAt;
    private boolean tripIsYetToBegin;
    private int ETA;
    private BusStop[] busStopsEnRoute;

    String getLatitude()
    {
        return latitude;
    }

    void setLatitude(String inputLatitude)
    {
        latitude = inputLatitude;
    }

    String getLongitude()
    {
        return longitude;
    }

    void setLongitude(String inputLongitude)
    {
        longitude = inputLongitude;
    }

    int getRouteOrder()
    {
        return routeOrder;
    }

    void setRouteOrder(int inputRouteOrder)
    {
        routeOrder = inputRouteOrder;
    }

    public String getRouteNumber()
    {
        return routeNumber;
    }

    public void setRouteNumber(String routeNumber)
    {
        this.routeNumber = routeNumber;
    }

    public int getServiceID()
    {
        return serviceID;
    }

    public void setServiceID(int serviceID)
    {
        this.serviceID = serviceID;
    }

    int getETA()
    {
        return ETA;
    }

    public void setETA(int ETA)
    {
        this.ETA = ETA;
    }

    boolean getIsDue()
    {
        return isDue;
    }

    void setIsDue(boolean isDue)
    {
        this.isDue = isDue;
    }

    boolean getTripIsYetToBegin()
    {
        return tripIsYetToBegin;
    }

    void setTripIsYetToBegin(boolean tripIsYetToBegin)
    {
        this.tripIsYetToBegin = tripIsYetToBegin;
    }

    String getRegistrationNumber()
    {
        return registrationNumber;
    }

    void setRegistrationNumber(String inputRegistrationNumber)
    {
        registrationNumber = inputRegistrationNumber;
    }

    String getTimeToBus()
    {
        return timeToBus;
    }

    void setTimeToBus(String inputTimeToBus)
    {
        timeToBus = inputTimeToBus;
    }

    String getNameOfStopBusIsAt()
    {
        return nameOfStopBusIsAt;
    }

    void setNameOfStopBusIsAt(String inputNameOfStopBusIsAt)
    {
        nameOfStopBusIsAt = inputNameOfStopBusIsAt;
    }

    public BusStop[] getBusStopsEnRoute()
    {
        return busStopsEnRoute;
    }

    public void setBusStopsEnRoute(BusStop[] busStopsEnRoute)
    {
        this.busStopsEnRoute = busStopsEnRoute;
    }
}