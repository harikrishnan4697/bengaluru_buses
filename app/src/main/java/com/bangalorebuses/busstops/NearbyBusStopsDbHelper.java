package com.bangalorebuses.busstops;

import com.bangalorebuses.core.BusStop;

import java.util.ArrayList;

interface NearbyBusStopsDbHelper
{
    void onNearbyBusStopsFound(ArrayList<BusStop> busStops);
}
