package com.bangalorebuses;

class Route
{
    private String routeNumber;
    private String upRouteId;
    private String downRouteId;
    private int numberOfBusesInService;
    private String direction;
    private String upRouteName;
    private String downRouteName;

    //Getter methods
    String getRouteNumber()
    {
        return routeNumber;
    }

    //Setter methods
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

    int getNumberOfBusesInService()
    {
        return numberOfBusesInService;
    }

    void setNumberOfBusesInService(int inputNumberOfBusesInService)
    {
        numberOfBusesInService = inputNumberOfBusesInService;
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
}