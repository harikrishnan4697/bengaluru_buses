package com.bangalorebuses.utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.StringBuilderPrinter;

import com.bangalorebuses.R;
import com.bangalorebuses.core.BusStop;

import java.util.Calendar;

import static com.bangalorebuses.utils.Constants.db;

public class CommonMethods
{
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

    public static String convertMinutesToHoursAndMinutes(int minutes)
    {
        if (minutes < 0)
        {
            return null;
        }
        else
        {
            String hoursAndMinutes;

            if (minutes >= 60)
            {
                int hours = minutes / 60;
                hoursAndMinutes = hours + " hr " + minutes % 60 + " min";
            }
            else
            {
                hoursAndMinutes = minutes + " min";
            }

            return hoursAndMinutes;
        }
    }

    public static String getBusStopNameAndDirectionNameCombined(String busStopName,
                                                                String busStopDirectionName)
    {
        if (busStopDirectionName == null)
        {
            return busStopName;
        }
        else
        {
            String busStopNameAndDirectionNameCombined;

            if (busStopDirectionName.contains(")"))
            {
                busStopNameAndDirectionNameCombined = busStopName + " " +
                        busStopDirectionName.substring(0, busStopDirectionName
                                .indexOf(")") + 1);
            }
            else
            {
                busStopNameAndDirectionNameCombined = busStopName + " " +
                        busStopDirectionName;
            }

            return busStopNameAndDirectionNameCombined;
        }
    }

    public static int getBusRouteServiceTypeImageResId(String busRouteNumber)
    {
        int busRouteServiceTypeImageResId;

        if (busRouteNumber.length() > 5 && busRouteNumber.contains("KIAS-"))
        {
            busRouteServiceTypeImageResId = R.drawable.ic_flight_blue;
        }
        else if (busRouteNumber.length() > 1 && busRouteNumber.substring(0, 2)
                .equals("V-"))
        {
            busRouteServiceTypeImageResId = R.drawable.ic_directions_bus_ac;
        }
        else if (busRouteNumber.contains("MF-"))
        {
            busRouteServiceTypeImageResId = R.drawable.ic_directions_bus_special;
        }
        else
        {
            busRouteServiceTypeImageResId = R.drawable.ic_directions_bus_ordinary;
        }

        return busRouteServiceTypeImageResId;
    }

    public static boolean checkNetworkConnectivity(Activity context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
}
