package com.bangalorebuses;

class BusStop
{
    private String busStopName;
    private String latitude;
    private String longitude;
    private int busStopId;
    private int routeOrder;
    private String busStopDirectionName;

    String getBusStopName()
    {
        return busStopName;
    }

    void setBusStopName(String inputBusStopName)
    {
        busStopName = inputBusStopName;
    }

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

    int getBusStopId()
    {
        return busStopId;
    }

    void setBusStopId(int inputBusStopId)
    {
        busStopId = inputBusStopId;
    }

    int getRouteOrder()
    {
        return routeOrder;
    }

    void setRouteOrder(int inputRouteOrder)
    {
        routeOrder = inputRouteOrder;
    }

    public String getBusStopDirectionName()
    {
        return busStopDirectionName;
    }

    public void setBusStopDirectionName(String busStopDirectionName)
    {
        this.busStopDirectionName = busStopDirectionName;
    }
}