package com.example.nihar.bangalorebuses;

class Route
{
    private String routeNumber;
    private String upRouteId;
    private String downRouteId;
    private int numberOfBusesInService;
    private String direction;
    private String upRouteName;
    private String downRouteName;

    //Setter methods
    void setRouteNumber(String inputRouteNumber)
    {
        routeNumber = inputRouteNumber;
    }

    void setUpRouteId(String inputUpRouteId)
    {
        upRouteId = inputUpRouteId;
    }

    void setDownRouteId(String inputDownRouteId)
    {
        downRouteId = inputDownRouteId;
    }

    void setNumberOfBusesInService(int inputNumberOfBusesInService)
    {
        numberOfBusesInService = inputNumberOfBusesInService;
    }

    void setDirection(String inputDirection)
    {
        direction = inputDirection;
    }

    void setUpRouteName(String inputUpRouteName)
    {
        upRouteName = inputUpRouteName;
    }

    void setDownRouteName(String inputDownRouteName)
    {
        downRouteName = inputDownRouteName;
    }

    //Getter methods
    String getRouteNumber()
    {
        return routeNumber;
    }

    String getUpRouteId()
    {
        return upRouteId;
    }

    String getDownRouteId()
    {
        return downRouteId;
    }

    int getNumberOfBusesInService()
    {
        return numberOfBusesInService;
    }

    String getDirection()
    {
        return direction;
    }

    String getUpRouteName()
    {
        return upRouteName;
    }

    String getDownRouteName()
    {
        return downRouteName;
    }
}