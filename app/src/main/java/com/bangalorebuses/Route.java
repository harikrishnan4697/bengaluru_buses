package com.bangalorebuses;

import java.util.ArrayList;

/**
 * This class is used to keep all details about a bus route together.
 *
 * @author Nihar Thakkar
 * @version 1.0
 * @since 18-6-2017
 */

class Route
{
    private String routeNumber;
    private String upRouteId;
    private String downRouteId;
    private String direction;
    private String upRouteName;
    private String downRouteName;
    private BusStop[] busStopsEnRoute;
    private String serviceType;
    private ArrayList<String> upTimings;
    private ArrayList<String> downTimings;

    String getRouteNumber()
    {
        return routeNumber;
    }

    void setRouteNumber(String inputRouteNumber)
    {
        routeNumber = inputRouteNumber;
    }


    String getUpRouteId()
    {
        return upRouteId;
    }

    void setUpRouteId(String inputUpRouteId)
    {
        upRouteId = inputUpRouteId;
    }


    String getDownRouteId()
    {
        return downRouteId;
    }

    void setDownRouteId(String inputDownRouteId)
    {
        downRouteId = inputDownRouteId;
    }


    String getDirection()
    {
        return direction;
    }

    void setDirection(String inputDirection)
    {
        direction = inputDirection;
    }


    String getUpRouteName()
    {
        return upRouteName;
    }

    void setUpRouteName(String inputUpRouteName)
    {
        upRouteName = inputUpRouteName;
    }


    String getDownRouteName()
    {
        return downRouteName;
    }

    void setDownRouteName(String inputDownRouteName)
    {
        downRouteName = inputDownRouteName;
    }

    BusStop[] getBusStopsEnRoute()
    {
        return busStopsEnRoute;
    }

    void setBusStopsEnRoute(BusStop[] busStopsEnRoute)
    {
        this.busStopsEnRoute = busStopsEnRoute;
    }

    String getServiceType()
    {
        return serviceType;
    }

    void setServiceType(String serviceType)
    {
        this.serviceType = serviceType;
    }

    public ArrayList<String> getUpTimings()
    {
        return upTimings;
    }

    public void setUpTimings(ArrayList<String> upTimings)
    {
        this.upTimings = upTimings;
    }

    public ArrayList<String> getDownTimings()
    {
        return downTimings;
    }

    public void setDownTimings(ArrayList<String> downTimings)
    {
        this.downTimings = downTimings;
    }
}