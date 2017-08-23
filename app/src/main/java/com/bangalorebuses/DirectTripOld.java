package com.bangalorebuses;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import static com.bangalorebuses.Constants.NETWORK_QUERY_NO_ERROR;
import static com.bangalorebuses.Constants.db;

class DirectTripOld implements Serializable, TripPlannerHelper
{
    private BusStop originStop = new BusStop();
    private String destinationBusStopName;
    private ArrayList<BusRoute> busRoutes = new ArrayList<>();
    private int shortestTravelTime;
    private int numberOfBusRoutesQueried;
    private int numberOfBusRouteQueriesComplete;
    private TripHelper directTripHelper;
    private ArrayList<BusRoute> busRoutesWithBuses = new ArrayList<>();
    private ArrayList<Bus> busesOnDirectTrip = new ArrayList<>();

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

    public BusStop getOriginStop()
    {
        return originStop;
    }

    public void setOriginStop(BusStop originStop)
    {
        this.originStop = originStop;
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

    public void setBusRoutes(ArrayList<BusRoute> busRoutes)
    {
        this.busRoutes = busRoutes;
    }

    public void addBusRoute(BusRoute busRoute)
    {
        this.busRoutes.add(busRoute);
    }

    public int getShortestTravelTime()
    {
        return shortestTravelTime;
    }

    public void setShortestTravelTime(int shortestTravelTime)
    {
        this.shortestTravelTime = shortestTravelTime;
    }

    public int getBusesOnBusRoutes(TripHelper directTripHelper, Context context)
    {
        this.directTripHelper = directTripHelper;
        if (isNetworkAvailable(context))
        {
            numberOfBusRoutesQueried = 0;
            busRoutesWithBuses.clear();
            numberOfBusRouteQueriesComplete = 0;

            for (; numberOfBusRoutesQueried < 10; numberOfBusRoutesQueried++)
            {
                if (numberOfBusRoutesQueried < busRoutes.size())
                {
                    new GetBusesEnDirectRouteTask(this, busRoutes.get(numberOfBusRoutesQueried))
                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                else
                {
                    break;
                }
            }
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

            if (busRoute.getBusRouteBuses().size() > 0)
            {
                busRoutesWithBuses.add(busRoute);
            }
        }
        else
        {
            // TODO Handle error
        }

        synchronized (this)
        {
            if (numberOfBusRoutesQueried < busRoutes.size())
            {
                new GetBusesEnDirectRouteTask(this, busRoutes.get(numberOfBusRoutesQueried))
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                numberOfBusRoutesQueried++;
            }
        }

        if (numberOfBusRouteQueriesComplete == numberOfBusRoutesQueried)
        {
            busRoutes = busRoutesWithBuses;

            if (directTripHelper != null)
            {
                //directTripHelper.onBusesInServiceFound(this);
            }
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

    public ArrayList<Bus> getBusesOnDirectTrip()
    {
        return busesOnDirectTrip;
    }

    public void setBusesOnDirectTrip(ArrayList<Bus> busesOnDirectTrip)
    {
        this.busesOnDirectTrip = busesOnDirectTrip;
    }
}