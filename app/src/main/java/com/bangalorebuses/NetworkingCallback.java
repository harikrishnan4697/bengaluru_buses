package com.bangalorebuses;

import org.json.JSONArray;

interface NetworkingCallback
{
    void onBusStopsFound(boolean isError, JSONArray busStopsArray);

    void onBusRouteDetailsFound(boolean isError, Route route, boolean isForBusList);

    void onStopsOnBusRouteFound(boolean isError, JSONArray stopListArray);

    void onBusesEnRouteFound(boolean isError, Bus[] buses, int numberOfBusesFound);

    void onTimeToBusesFound(boolean isError, Bus[] buses);

    void onBusesAtStopFound(boolean isError, JSONArray buses);
}