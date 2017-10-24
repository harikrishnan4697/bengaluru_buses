package com.bangalorebuses.utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.StringBuilderPrinter;
import android.widget.Toast;

import com.bangalorebuses.R;
import com.bangalorebuses.core.BusStop;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;
import static com.bangalorebuses.utils.Constants.db;
import static com.bangalorebuses.utils.Constants.favoritesHashMap;

public class CommonMethods
{
    public static final String FAVORITES_FILE_NAME = "Favourites.txt";

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

    public static String convertMinutesToBusArrivalTimings(int minutes)
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
            else if (minutes == 0)
            {
                hoursAndMinutes = "Due";
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
        else if (busRouteNumber.length() > 1 && (busRouteNumber.substring(0, 2)
                .equals("V-") || busRouteNumber.substring(0, 2).equals("C-")))
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

    public static int getBusRouteNumberBackgroundResId(String busRouteNumber)
    {
        int busRouteNumberBackgroundImageResId;

        if (busRouteNumber.length() > 5 && busRouteNumber.contains("KIAS-"))
        {
            busRouteNumberBackgroundImageResId = R.drawable.blue_rounded_background_borderless;
        }
        else if (busRouteNumber.length() > 1 && (busRouteNumber.substring(0, 2)
                .equals("V-") || busRouteNumber.substring(0, 2)
                .equals("C-")))
        {
            busRouteNumberBackgroundImageResId = R.drawable.blue_rounded_background_borderless;
        }
        else if (busRouteNumber.contains("MF-"))
        {
            busRouteNumberBackgroundImageResId = R.drawable.orange_rounded_background_borderless;
        }
        else
        {
            busRouteNumberBackgroundImageResId = R.drawable.green_rounded_background_borderless;
        }

        return busRouteNumberBackgroundImageResId;
    }

    public static boolean checkNetworkConnectivity(Activity context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    public static boolean readFavoritesFileToHashMap(Context context)
    {
        try
        {
            FileInputStream fileInputStream = context.openFileInput(FAVORITES_FILE_NAME);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            ArrayList<String> favorites = new ArrayList<>();
            String line;

            while ((line = bufferedReader.readLine()) != null)
            {
                favorites.add(line);
            }
            favorites.trimToSize();

            favoritesHashMap.clear();
            for (String favorite : favorites)
            {
                if (favorite.substring(0, 3).equals("^%b"))
                {
                    String busRouteNumber = favorite.substring(3,
                            favorite.indexOf("^%bd"));
                    String busRouteDirectionName = favorite.substring(favorite
                            .indexOf("^%bd") + 4, favorite.indexOf("^%bs"));
                    String busRouteStopId = favorite.substring(favorite
                            .indexOf("^%bs") + 4, favorite.length());

                    favoritesHashMap.put("^%b" + busRouteNumber + "^%bd" +
                            busRouteDirectionName, busRouteStopId);
                }
                else if (favorite.substring(0, 3).equals("^%s"))
                {
                    String busStopName = favorite.substring(3, favorite.indexOf("^%sd"));
                    String busStopDirectionName = favorite.substring(favorite
                            .indexOf("^%sd") + 4, favorite.indexOf("^%si"));
                    String busStopId = favorite.substring(favorite.indexOf("^%si") + 4,
                            favorite.length());

                    favoritesHashMap.put("^%s" + busStopName + "^%sd" +
                            busStopDirectionName, busStopId);
                }
                else if (favorite.substring(0, 3).equals("^%t"))
                {
                    String originBusStopName = favorite.substring(3,
                            favorite.indexOf("^%td"));
                    String destinationBusStopName = favorite.substring(favorite
                            .indexOf("^%td") + 4, favorite.length());

                    favoritesHashMap.put("^%t" + originBusStopName + "^%td" +
                            destinationBusStopName, destinationBusStopName);
                }
            }
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }

    public static boolean writeFavoritesHashMapToFile(Context context)
    {
        try
        {
            Set favoriteKeys = favoritesHashMap.keySet();

            FileOutputStream fileOutputStream = context.openFileOutput(
                    FAVORITES_FILE_NAME, MODE_PRIVATE);

            Iterator iterator = favoriteKeys.iterator();
            while (iterator.hasNext())
            {
                String key = (String) iterator.next();
                String value = favoritesHashMap.get(key);

                if (key.substring(0, 3).equals("^%b"))
                {
                    fileOutputStream.write((key + "^%bs" + value +
                            "\n").getBytes());
                }
                else if (key.substring(0, 3).equals("^%s"))
                {
                    fileOutputStream.write((key + "^%si" + value +
                            "\n").getBytes());
                }
                else if (key.substring(0, 3).equals("^%t"))
                {
                    fileOutputStream.write((key + "\n").getBytes());
                }
            }

            fileOutputStream.close();
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }
}
