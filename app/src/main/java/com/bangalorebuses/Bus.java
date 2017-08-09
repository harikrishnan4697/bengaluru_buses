package com.bangalorebuses;

/**
 * This class is used to keep track of all the characteristics
 * of a bus.
 *
 * @author Nihar Thakkar
 * @version 1.0
 * @since 23-6-2017
 */

class Bus extends BusRoute
{
    private String busRegistrationNumber;
    private String busLat;
    private String busLong;
    private int busETA;
    private int busRouteOrder;
    private boolean isDue;

    public String getBusRegistrationNumber()
    {
        return busRegistrationNumber;
    }

    public void setBusRegistrationNumber(String busRegistrationNumber)
    {
        this.busRegistrationNumber = busRegistrationNumber;
    }

    public String getBusLat()
    {
        return busLat;
    }

    public void setBusLat(String busLat)
    {
        this.busLat = busLat;
    }

    public String getBusLong()
    {
        return busLong;
    }

    public void setBusLong(String busLong)
    {
        this.busLong = busLong;
    }

    public int getBusETA()
    {
        return busETA;
    }

    public void setBusETA(int busETA)
    {
        this.busETA = busETA;
    }

    public int getBusRouteOrder()
    {
        return busRouteOrder;
    }

    public void setBusRouteOrder(int busRouteOrder)
    {
        this.busRouteOrder = busRouteOrder;
    }

    public boolean isDue()
    {
        return isDue;
    }

    public void setDue(boolean due)
    {
        isDue = due;
    }
}