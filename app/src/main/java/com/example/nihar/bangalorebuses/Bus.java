package com.example.nihar.bangalorebuses;

class Bus
{
    private String latitude;
    private String longitude;
    private int routeOrder;
    private String registrationNumber;
    private String timeToBus;
    private boolean isDue = false;
    private String nameOfStopBusIsAt;

    // Setter methods
    void setLatitude(String inputLatitude)
    {
        latitude = inputLatitude;
    }

    void setLongitude(String inputLongitude)
    {
        longitude = inputLongitude;
    }

    void setRouteOrder(int inputRouteOrder)
    {
        routeOrder = inputRouteOrder;
    }

    void setIsDue(boolean isDue)
    {
        this.isDue = isDue;
    }

    void setRegistrationNumber(String inputRegistrationNumber)
    {
        registrationNumber = inputRegistrationNumber;
    }

    void setNameOfStopBusIsAt(String inputNameOfStopBusIsAt)
    {
        nameOfStopBusIsAt = inputNameOfStopBusIsAt;
    }

    void setTimeToBus(String inputTimeToBus)
    {
        timeToBus = inputTimeToBus;
    }

    // Getter methods
    String getLatitude()
    {
        return latitude;
    }

    String getLongitude()
    {
        return longitude;
    }

    int getRouteOrder()
    {
        return routeOrder;
    }

    boolean getIsDue()
    {
        return isDue;
    }

    String getRegistrationNumber()
    {
        return registrationNumber;
    }

    String getTimeToBus()
    {
        return timeToBus;
    }

    String getNameOfStopBusIsAt()
    {
        return nameOfStopBusIsAt;
    }
}