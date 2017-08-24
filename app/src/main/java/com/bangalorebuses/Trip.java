package com.bangalorebuses;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import static com.bangalorebuses.Constants.NETWORK_QUERY_NO_ERROR;
import static com.bangalorebuses.Constants.db;

abstract class Trip implements TripPlannerHelper
{
    private BusStop originBusStop;
    private String destinationBusStopName;
    private ArrayList<BusRoute> busRoutes = new ArrayList<>();
    private TripHelper tripHelper;
    private int numberOfBusRoutesQueried;
    private int numberOfBusRouteQueriesComplete;

    /**
     * This method is used to check if the user's device
     * has a Wi-Fi or Cellular data connection.
     *
     * @return boolean This returns true or false based on the status
     * of the Wi-Fi and Cellular data connection.
     */
    private boolean isNetworkAvailable(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    public BusStop getOriginBusStop()
    {
        return originBusStop;
    }

    public void setOriginBusStop(BusStop originBusStop)
    {
        this.originBusStop = originBusStop;
    }

    public String getDestinationBusStopName()
    {
        return destinationBusStopName;
    }

    public void setDestinationBusStopName(String destinationBusStopName)
    {
        this.destinationBusStopName = destinationBusStopName;
    }

    public ArrayList<BusRoute> getBusRoutes()
    {
        return busRoutes;
    }

    public void addBusRoute(BusRoute busRoute)
    {
        busRoutes.add(busRoute);
    }

    public void clearBusRoutes()
    {
        busRoutes.clear();
    }

    public abstract void showTrip(TripsRecyclerViewAdapter.TripsViewHolder holder);

    public int getBusesOnFirstBusRoute(TripHelper directTripHelper, Context context)
    {
        this.tripHelper = directTripHelper;
        if (isNetworkAvailable(context))
        {
            numberOfBusRoutesQueried = 0;
            numberOfBusRouteQueriesComplete = 0;
            new GetBusesEnDirectRouteTask(this, busRoutes.get(0))
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else
        {
            return -1;
        }

        return 0;
    }

    @Override
    public void onBusesInServiceFound(String errorMessage, BusRoute busRoute)
    {
        numberOfBusRouteQueriesComplete++;

        if (errorMessage.equals(NETWORK_QUERY_NO_ERROR))
        {
            for (Bus bus : busRoute.getBusRouteBuses())
            {
                bus.setBusETA(calculateTravelTime(DbQueries.getNumberOfStopsBetweenRouteOrders(db, busRoute.getBusRouteId(),
                        bus.getBusRouteOrder(), busRoute.getTripPlannerOriginBusStop().getBusStopRouteOrder()),
                        busRoute.getBusRouteNumber()));
            }

            Collections.sort(busRoute.getBusRouteBuses(), new Comparator<Bus>()
            {
                @Override
                public int compare(Bus o1, Bus o2)
                {
                    return o1.getBusETA() - o2.getBusETA();
                }
            });

            if (busRoute.getBusRouteBuses().size() > 0)
            {
                busRoute.setShortestOriginToDestinationTravelTime(calculateTravelTime(DbQueries.getNumberOfStopsBetweenRouteOrders(
                        db, busRoute.getBusRouteId(), busRoute.getBusRouteBuses().get(0).getBusRouteOrder(),
                        busRoute.getTripPlannerDestinationBusStop().getBusStopRouteOrder()),
                        busRoute.getBusRouteNumber()));
            }
        }

        if (tripHelper != null)
        {
            tripHelper.onBusesInServiceFound(errorMessage, this);
        }
    }

    private int calculateTravelTime(int numberOfBusStopsToTravel, String routeNumber)
    {
        Calendar calendar = Calendar.getInstance();
        int travelTime;

        if ((calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) || (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY))
        {
            // Check if the bus is an airport shuttle (airport shuttles take longer to travel
            // from one bus stop to another as they don't stop at all bus stops)
            if (routeNumber.contains("KIAS-"))
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
            if (routeNumber.contains("KIAS-"))
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
            if (routeNumber.contains("KIAS-"))
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
}
