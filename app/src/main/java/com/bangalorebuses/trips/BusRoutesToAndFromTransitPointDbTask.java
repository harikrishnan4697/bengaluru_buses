package com.bangalorebuses.trips;

import android.database.Cursor;
import android.os.AsyncTask;

import com.bangalorebuses.core.Bus;
import com.bangalorebuses.core.BusRoute;
import com.bangalorebuses.core.BusStop;
import com.bangalorebuses.utils.DbQueries;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import static com.bangalorebuses.utils.Constants.db;

public class BusRoutesToAndFromTransitPointDbTask extends AsyncTask<Void, Void, Void>
{
    private TransitPoint transitPoint;
    private String originBusStopName;
    private String destinationBusStopName;
    private IndirectTripHelper caller;

    public BusRoutesToAndFromTransitPointDbTask(IndirectTripHelper caller, String originBusStopName, TransitPoint transitPoint,
                                         String destinationBusStopName)
    {
        this.caller = caller;
        this.originBusStopName = originBusStopName;
        this.transitPoint = transitPoint;
        this.destinationBusStopName = destinationBusStopName;
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        transitPoint.setBusRoutesToTransitPoint(getBusRoutesOnFirstLeg());
        transitPoint.setBusRoutesFromTransitPoint(getBusRoutesOnSecondLeg());

        return null;
    }

    private ArrayList<BusRoute> getBusRoutesOnFirstLeg()
    {
        // Get all the bus routes from the origin bus stop to the transit point bus stop
        ArrayList<BusRoute> originToTransitPointBusRoutes = getBusRoutes(originBusStopName,
                transitPoint.getTransitPointName());

        // Create a temporary list to store only the bus routes that currently have buses in service
        ArrayList<BusRoute> tempOriginToTransitPointBuses = new ArrayList<>();

        for (BusRoute busRoute : originToTransitPointBusRoutes)
        {
            // Get the buses in service on each of the bus routes
            busRoute.setBusRouteBuses(getScheduledBuses(busRoute));

            // Add the bus route to the temporary list only if there are buses in service and if
            // the list doesn't already contain it.
            if (busRoute.getBusRouteBuses() != null && busRoute.getBusRouteBuses().size() != 0)
            {
                boolean busRouteIsAlreadyInTempList = false;
                for (BusRoute busRouteAlreadyInTempList : tempOriginToTransitPointBuses)
                {
                    if (busRouteAlreadyInTempList.getBusRouteId() == busRoute.getBusRouteId())
                    {
                        busRouteIsAlreadyInTempList = true;
                    }
                }

                if (!busRouteIsAlreadyInTempList)
                {
                    tempOriginToTransitPointBuses.add(busRoute);
                }
            }
        }

        originToTransitPointBusRoutes = tempOriginToTransitPointBuses;
        originToTransitPointBusRoutes.trimToSize();

        // Sort the list of bus routes based on when their first bus will arrive
        Collections.sort(originToTransitPointBusRoutes, new Comparator<BusRoute>()
        {
            @Override
            public int compare(BusRoute o1, BusRoute o2)
            {
                return o1.getBusRouteBuses().get(0).getBusETA() - o2.getBusRouteBuses().get(0).getBusETA();
            }
        });

        return originToTransitPointBusRoutes;
    }

    private ArrayList<BusRoute> getBusRoutesOnSecondLeg()
    {
        // Get all the bus routes from the transit point bus stop to the destination bus stop
        ArrayList<BusRoute> transitPointToDestinationBusRoutes = getBusRoutes(transitPoint.getTransitPointName(),
                destinationBusStopName);

        // Create a temporary list to store only the bus routes that currently have buses in service
        ArrayList<BusRoute> tempTransitPointToDestinationBuses = new ArrayList<>();

        for (BusRoute busRoute : transitPointToDestinationBusRoutes)
        {
            // Get the buses in service on each of the bus routes
            busRoute.setBusRouteBuses(getScheduledBuses(busRoute));

            // Add the bus route to the temporary list only if there are buses in service and if
            // the list doesn't already contain it.
            if (busRoute.getBusRouteBuses() != null && busRoute.getBusRouteBuses().size() != 0)
            {
                boolean busRouteIsAlreadyInTempList = false;
                for (BusRoute busRouteAlreadyInTempList : tempTransitPointToDestinationBuses)
                {
                    if (busRouteAlreadyInTempList.getBusRouteId() == busRoute.getBusRouteId())
                    {
                        busRouteIsAlreadyInTempList = true;
                    }
                }

                if (!busRouteIsAlreadyInTempList)
                {
                    tempTransitPointToDestinationBuses.add(busRoute);
                }
            }
        }

        transitPointToDestinationBusRoutes = tempTransitPointToDestinationBuses;
        transitPointToDestinationBusRoutes.trimToSize();

        // Sort the list of bus routes based on when their first bus will arrive
        Collections.sort(transitPointToDestinationBusRoutes, new Comparator<BusRoute>()
        {
            @Override
            public int compare(BusRoute o1, BusRoute o2)
            {
                return o1.getBusRouteBuses().get(0).getBusETA() - o2.getBusRouteBuses().get(0).getBusETA();
            }
        });

        return transitPointToDestinationBusRoutes;
    }

    private ArrayList<BusRoute> getBusRoutes(String originBusStopName, String destinationBusStopName)
    {
        Cursor cursor = db.rawQuery("SELECT DISTINCT Routes.RouteId, Routes.RouteNumber, Routes.RouteDirection, " +
                "routesBetweenOriginAndTP.StopId, routesBetweenOriginAndTP.StopName, routesBetweenOriginAndTP.StopDirectionName," +
                " routesBetweenOriginAndTP.originBusStopRouteOrder, routesBetweenOriginAndTP.destinationBusStopRouteOrder" +
                " FROM Routes JOIN ( SELECT sub1.RouteId, sub1.StopId, sub1.StopName, sub1.StopDirectionName, sub1.StopRouteOrder" +
                " as originBusStopRouteOrder, sub2.StopRouteOrder as destinationBusStopRouteOrder FROM ( SELECT Stops.StopId," +
                " Stops.StopName, Stops.StopDirectionName, RouteStops.StopRouteOrder, RouteStops.RouteId FROM Stops JOIN RouteStops" +
                " WHERE Stops.StopName = '" + originBusStopName + "' AND Stops.StopId = RouteStops.StopId) sub1 JOIN" +
                " ( SELECT RouteStops.RouteId," +
                " RouteStops.StopRouteOrder FROM Stops JOIN RouteStops WHERE Stops.StopName = '" + destinationBusStopName + "' AND" +
                " Stops.StopId = RouteStops.StopId) sub2 WHERE sub1.RouteId = sub2.RouteId AND sub1.StopRouteOrder < sub2.StopRouteOrder)" +
                " routesBetweenOriginAndTP WHERE Routes.RouteId = routesBetweenOriginAndTP.RouteId", null);

        ArrayList<BusRoute> busRoutes = new ArrayList<>();

        while (cursor.moveToNext())
        {
            BusRoute busRoute = new BusRoute();
            BusStop originBusStop = new BusStop();
            BusStop destinationBusStop = new BusStop();

            busRoute.setBusRouteId(cursor.getInt(0));
            busRoute.setBusRouteNumber(cursor.getString(1));
            busRoute.setBusRouteDirection(cursor.getString(2));

            originBusStop.setBusStopId(cursor.getInt(3));
            originBusStop.setBusStopName(cursor.getString(4));
            originBusStop.setBusStopDirectionName(cursor.getString(5));
            originBusStop.setBusStopRouteOrder(cursor.getInt(6));

            destinationBusStop.setBusStopName(destinationBusStopName);
            destinationBusStop.setBusStopRouteOrder(cursor.getInt(7));

            busRoute.setTripPlannerOriginBusStop(originBusStop);
            busRoute.setTripPlannerDestinationBusStop(destinationBusStop);

            busRoutes.add(busRoute);
        }

        cursor.close();
        return busRoutes;
    }

    private ArrayList<Bus> getScheduledBuses(BusRoute busRoute)
    {
        ArrayList<Bus> busesOnBusRoute = new ArrayList<>();

        Cursor cursor = db.rawQuery("select RouteTimings.RouteDepartureTime from RouteTimings where RouteTimings.RouteId = " +
                busRoute.getBusRouteId(), null);

        while (cursor.moveToNext())
        {
            Bus bus = new Bus();

            int timeOfDayBusWillArrive = cursor.getInt(0) + calculateTravelTime(DbQueries.getNumberOfStopsBetweenRouteOrders(db,
                    busRoute.getBusRouteId(), 1, busRoute.getTripPlannerOriginBusStop().getBusStopRouteOrder()), busRoute.getBusRouteNumber());

            Date date = new Date();
            // TODO Use calendar class
            int busETA = timeOfDayBusWillArrive - ((date.getHours() * 60) + date.getMinutes());

            if (busETA > -1)
            {
                bus.setBusRoute(busRoute);
                bus.setBusETA(busETA);
                bus.setBusRouteOrder(1);
                busesOnBusRoute.add(bus);
            }
        }

        cursor.close();

        // Sort the buses based on their ETA
        Collections.sort(busesOnBusRoute, new Comparator<Bus>()
        {
            @Override
            public int compare(Bus o1, Bus o2)
            {
                return o1.getBusETA() - o2.getBusETA();
            }
        });

        return busesOnBusRoute;
    }

    // TODO Create a utils class and put this method there
    // Calculates how long it would take to travel a certain number of bus stops on a particular bus route
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

    @Override
    protected void onPostExecute(Void param)
    {
        super.onPostExecute(param);
        caller.onBusRoutesToAndFromTransitPointFound(transitPoint);
    }
}