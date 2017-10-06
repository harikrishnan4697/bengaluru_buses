package com.bangalorebuses.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Calendar;

import static com.bangalorebuses.utils.Constants.db;

public class CommonMethods
{
    /**
     * This method returns how long a bus on a particular bus route will
     * take to travel a certain number of bus stops (in minutes).
     *
     * @param busRouteId          How many bus stops the travel time
     *                            should be calculated for.
     * @param busRouteServiceType The service type of the bus route.
     * @return How many minutes traveling the specified number of bus
     * stops will take for the specified bus route service type.
     */
    public static int calculateTravelTime(int busRouteId, String busRouteNumber,
                                          int originBusStopRouteOrder, int destinationBusStopRouteOrder)
    {
        Calendar calendar = Calendar.getInstance();
        int travelTime;
        int numberOfBusStopsToTravel;

        Cursor cursor = db.rawQuery("select count(*) from RouteStops where RouteStops.RouteId = " + busRouteId +
                " and RouteStops.StopRouteOrder > " + originBusStopRouteOrder + " and RouteStops.StopRouteOrder <= "
                + destinationBusStopRouteOrder, null);
        if (cursor.moveToNext())
        {
            numberOfBusStopsToTravel = cursor.getInt(0);
            cursor.close();
        }
        else
        {
            cursor.close();
            return -1;
        }

        if ((calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) || (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY))
        {
            // Check if the bus is an airport shuttle (airport shuttles take longer to travel
            // from one bus stop to another as they don't stop at all bus stops)
            if (busRouteNumber.contains("KIAS-"))
            {
                travelTime = numberOfBusStopsToTravel * 4;  // 4 Minutes to get from a bus stop to another for the airport shuttle weekends
            }
            else
            {
                travelTime = numberOfBusStopsToTravel * 2;  // 2 Minutes to get from a bus stop to another for other buses during weekends
            }
        }
        else if ((calendar.get(Calendar.HOUR_OF_DAY) > 7 && calendar.get(Calendar.HOUR_OF_DAY) < 11) || (calendar.get(Calendar.HOUR_OF_DAY) > 16 && calendar.get(Calendar.HOUR_OF_DAY) < 21))
        {
            // Check if the bus is an airport shuttle (airport shuttles take longer to travel
            // from one bus stop to another as they don't stop at all bus stops)
            if (busRouteNumber.contains("KIAS-"))
            {
                travelTime = numberOfBusStopsToTravel * 5;  // 5 Minutes to get from a bus stop to another for the airport shuttle in peak-time
            }
            else
            {
                travelTime = numberOfBusStopsToTravel * 3;  // 3 Minutes to get from a bus stop to another for other buses in peak-time
            }
        }
        else
        {
            // Check if the bus is an airport shuttle (airport shuttles take longer to travel
            // from one bus stop to another as they don't stop at all bus stops)
            if (busRouteNumber.contains("KIAS-"))
            {
                travelTime = numberOfBusStopsToTravel * 4;  // 4 Minutes to get from a bus stop to another for the airport shuttle
            }
            else
            {
                travelTime = (int) (numberOfBusStopsToTravel * 2.5);  // 2.5 Minutes to get from a bus stop to another for other buses
            }
        }

        return travelTime;
    }

    public static int getNumberOfStopsBetweenRouteOrders(int routeId, int routeOrder1, int routeOrder2)
    {
        Cursor cursor = db.rawQuery("select count(*) from RouteStops where RouteStops.RouteId = " + routeId +
                " and RouteStops.StopRouteOrder > " + routeOrder1 + " and RouteStops.StopRouteOrder <= " + routeOrder2, null);
        if (cursor.moveToNext())
        {
            int numberOfStopsBetweenRouteOrders = cursor.getInt(0);
            cursor.close();
            return numberOfStopsBetweenRouteOrders;
        }
        else
        {
            cursor.close();
            return -1;
        }
    }
}
