package com.bangalorebuses;

class Bus
{
    private String latitude;
    private String longitude;
    private int routeOrder;
    private String registrationNumber;
    private String timeToBus;
    private boolean isDue = false;
    private String nameOfStopBusIsAt;

    // Getter methods
    String getLatitude()
    {
        return latitude;
    }

    // Setter methods
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

    boolean getIsDue()
    {
        return isDue;
    }

    void setIsDue(boolean isDue)
    {
        this.isDue = isDue;
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
}