package com.example.nihar.bangalorebuses;

class BusStop
{
    private String busStopName;
    private String latitude;
    private String longitude;
    private int busStopId;
    private int routeOrder;

    //Setter methods
    void setBusStopName(String inputBusStopName)
    {
        busStopName = inputBusStopName;
    }

    void setLatitude(String inputLatitude)
    {
        latitude = inputLatitude;
    }

    void setLongitude(String inputLongitude)
    {
        longitude = inputLongitude;
    }

    void setBusStopId(int inputBusStopId)
    {
        busStopId = inputBusStopId;
    }

    void setRouteOrder(int inputRouteOrder)
    {
        routeOrder = inputRouteOrder;
    }

    //Getter methods
    String getBusStopName()
    {
        return busStopName;
    }

    String getLatitude()
    {
        return latitude;
    }

    String getLongitude()
    {
        return longitude;
    }

    int getBusStopId()
    {
        return busStopId;
    }

    int getRouteOrder()
    {
        return routeOrder;
    }
}