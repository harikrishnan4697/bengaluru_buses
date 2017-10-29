package com.bangalorebuses.trips;

import android.database.Cursor;
import android.os.AsyncTask;

import com.bangalorebuses.core.Bus;
import com.bangalorebuses.core.BusRoute;
import com.bangalorebuses.core.BusStop;
import com.bangalorebuses.utils.CommonMethods;

import java.util.ArrayList;
import java.util.Calendar;

import static com.bangalorebuses.utils.Constants.db;

class MostFrequentIndirectTripDbTask extends AsyncTask<Void, Void, IndirectTrip>
{
    private TransitPointsHelper caller;
    private String originBusStopName;
    private String transitPointBusStopName;
    private String destinationBusStopName;
    private final Calendar calendar = Calendar.getInstance();
    private final int currentTimeInMinutesSinceMidnight = ((calendar.get(Calendar
            .HOUR_OF_DAY) * 60) + calendar.get(Calendar.MINUTE));

    MostFrequentIndirectTripDbTask(TransitPointsHelper caller, String originBusStopName,
                                   String transitPointBusStopName,
                                   String destinationBusStopName)
    {
        this.originBusStopName = originBusStopName;
        this.transitPointBusStopName = transitPointBusStopName;
        this.destinationBusStopName = destinationBusStopName;
        this.caller = caller;
    }

    @Override
    protected IndirectTrip doInBackground(Void... voids)
    {
        DirectTrip directTripOnFirstLeg = getDirectTripOnFirstLeg(originBusStopName,
                transitPointBusStopName);

        if (directTripOnFirstLeg == null)
        {
            return null;
        }

        DirectTrip directTripOnSecondLeg = getDirectTripOnSecondLeg(transitPointBusStopName,
                destinationBusStopName, directTripOnFirstLeg.getTripDuration());

        if (directTripOnSecondLeg == null)
        {
            return null;
        }

        IndirectTrip indirectTrip = new IndirectTrip();
        indirectTrip.setDirectTripOnFirstLeg(directTripOnFirstLeg);
        indirectTrip.setDirectTripOnSecondLeg(directTripOnSecondLeg);

        TransitPoint transitPoint = new TransitPoint();
        transitPoint.setBusStopName(transitPointBusStopName);
        indirectTrip.setTransitPoint(transitPoint);

        indirectTrip = setIndirectTripTravelTime(indirectTrip);

        return indirectTrip;
    }

    private DirectTrip getDirectTripOnFirstLeg(String originBusStopName,
                                               String transitPointBusStopName)
    {
        Cursor cursor = db.rawQuery("SELECT Routes.RouteId, Routes.RouteNumber," +
                " routesBetweenOriginAndTP.originBusStopRouteOrder, routesBetweenOriginAndTP" +
                ".destinationBusStopRouteOrder FROM Routes JOIN ( SELECT sub1.RouteId," +
                " sub1.StopId, sub1.StopDirectionName as originBusStopDirectionName, sub1" +
                ".StopRouteOrder as originBusStopRouteOrder, sub2.StopRouteOrder as" +
                " destinationBusStopRouteOrder FROM ( SELECT Stops.StopId, Stops.StopName," +
                " Stops.StopDirectionName, RouteStops.StopRouteOrder, RouteStops.RouteId FROM" +
                " Stops JOIN RouteStops WHERE Stops.StopName = '" + originBusStopName + "' AND Stops" +
                ".StopId = RouteStops.StopId) sub1 JOIN ( SELECT RouteStops.RouteId, RouteStops" +
                ".StopRouteOrder FROM Stops JOIN RouteStops WHERE Stops.StopName = '" + transitPointBusStopName +
                "' AND Stops.StopId = RouteStops.StopId) sub2 WHERE sub1.RouteId = sub2.RouteId AND" +
                " sub1.StopRouteOrder < sub2.StopRouteOrder) routesBetweenOriginAndTP WHERE Routes" +
                ".RouteId = routesBetweenOriginAndTP.RouteId order by Routes.RouteDeparturesPerDay" +
                " desc limit 10", null);

        DirectTrip fastestDirectTrip = null;

        while (cursor.moveToNext())
        {
            if (fastestDirectTrip == null)
            {
                ArrayList<Bus> buses = getScheduledBuses(cursor.getInt(0),
                        cursor.getString(1), cursor.getInt(2),
                        currentTimeInMinutesSinceMidnight);

                if (buses != null)
                {
                    int travelTimeOnFirstLeg = CommonMethods.calculateTravelTime(
                            cursor.getInt(0), cursor.getString(1),
                            cursor.getInt(2), cursor.getInt(3));

                    fastestDirectTrip = new DirectTrip();

                    BusStop originBusStop = new BusStop();
                    originBusStop.setBusStopName(originBusStopName);
                    originBusStop.setBusStopRouteOrder(cursor.getInt(2));
                    fastestDirectTrip.setOriginBusStop(originBusStop);

                    BusStop destinationBusStop = new BusStop();
                    destinationBusStop.setBusStopName(transitPointBusStopName);
                    destinationBusStop.setBusStopRouteOrder(cursor.getInt(3));
                    fastestDirectTrip.setDestinationBusStop(destinationBusStop);

                    BusRoute busRoute = new BusRoute();
                    busRoute.setBusRouteId(cursor.getInt(0));
                    busRoute.setBusRouteNumber(cursor.getString(1));
                    busRoute.setBusRouteBuses(buses);
                    fastestDirectTrip.setBusRoute(busRoute);

                    fastestDirectTrip.setTripDuration(buses.get(0).getBusETA() + travelTimeOnFirstLeg);
                }
            }
            else
            {
                ArrayList<Bus> buses = getScheduledBuses(cursor.getInt(0),
                        cursor.getString(1), cursor.getInt(2),
                        currentTimeInMinutesSinceMidnight);

                if (buses != null)
                {
                    int travelTimeOnFirstLeg = CommonMethods.calculateTravelTime(
                            cursor.getInt(0), cursor.getString(1),
                            cursor.getInt(2), cursor.getInt(3));

                    if ((buses.get(0).getBusETA() + travelTimeOnFirstLeg) <
                            fastestDirectTrip.getTripDuration())
                    {
                        BusStop originBusStop = new BusStop();
                        originBusStop.setBusStopName(originBusStopName);
                        originBusStop.setBusStopRouteOrder(cursor.getInt(2));
                        fastestDirectTrip.setOriginBusStop(originBusStop);

                        BusStop destinationBusStop = new BusStop();
                        destinationBusStop.setBusStopName(transitPointBusStopName);
                        destinationBusStop.setBusStopRouteOrder(cursor.getInt(3));
                        fastestDirectTrip.setDestinationBusStop(destinationBusStop);

                        BusRoute busRoute = new BusRoute();
                        busRoute.setBusRouteId(cursor.getInt(0));
                        busRoute.setBusRouteNumber(cursor.getString(1));
                        busRoute.setBusRouteBuses(buses);
                        fastestDirectTrip.setBusRoute(busRoute);

                        fastestDirectTrip.setTripDuration(buses.get(0).getBusETA() + travelTimeOnFirstLeg);
                    }
                }
            }

        }
        cursor.close();
        return fastestDirectTrip;
    }

    private DirectTrip getDirectTripOnSecondLeg(String transitPointBusStopName,
                                                String destinationBusStopName,
                                                int firstLegTripDuration)
    {
        Cursor cursor = db.rawQuery("SELECT Routes.RouteId, Routes.RouteNumber," +
                " routesBetweenOriginAndTP.originBusStopRouteOrder, routesBetweenOriginAndTP" +
                ".destinationBusStopRouteOrder FROM Routes JOIN ( SELECT sub1.RouteId," +
                " sub1.StopId, sub1.StopDirectionName as originBusStopDirectionName, sub1" +
                ".StopRouteOrder as originBusStopRouteOrder, sub2.StopRouteOrder as" +
                " destinationBusStopRouteOrder FROM ( SELECT Stops.StopId, Stops.StopName," +
                " Stops.StopDirectionName, RouteStops.StopRouteOrder, RouteStops.RouteId FROM" +
                " Stops JOIN RouteStops WHERE Stops.StopName = '" + transitPointBusStopName + "' AND Stops" +
                ".StopId = RouteStops.StopId) sub1 JOIN ( SELECT RouteStops.RouteId, RouteStops" +
                ".StopRouteOrder FROM Stops JOIN RouteStops WHERE Stops.StopName = '" + destinationBusStopName +
                "' AND Stops.StopId = RouteStops.StopId) sub2 WHERE sub1.RouteId = sub2.RouteId AND" +
                " sub1.StopRouteOrder < sub2.StopRouteOrder) routesBetweenOriginAndTP WHERE Routes" +
                ".RouteId = routesBetweenOriginAndTP.RouteId order by Routes.RouteDeparturesPerDay" +
                " desc limit 10", null);

        DirectTrip fastestDirectTrip = null;

        while (cursor.moveToNext())
        {
            if (fastestDirectTrip == null)
            {
                ArrayList<Bus> buses = getScheduledBuses(cursor.getInt(0),
                        cursor.getString(1), cursor.getInt(2),
                        currentTimeInMinutesSinceMidnight + firstLegTripDuration + 2);

                if (buses != null)
                {
                    int travelTimeOnSecondLeg = CommonMethods.calculateTravelTime(
                            cursor.getInt(0), cursor.getString(1),
                            cursor.getInt(2), cursor.getInt(3));

                    fastestDirectTrip = new DirectTrip();

                    BusStop originBusStop = new BusStop();
                    originBusStop.setBusStopName(transitPointBusStopName);
                    originBusStop.setBusStopRouteOrder(cursor.getInt(2));
                    fastestDirectTrip.setOriginBusStop(originBusStop);

                    BusStop destinationBusStop = new BusStop();
                    destinationBusStop.setBusStopName(destinationBusStopName);
                    destinationBusStop.setBusStopRouteOrder(cursor.getInt(3));
                    fastestDirectTrip.setDestinationBusStop(destinationBusStop);

                    BusRoute busRoute = new BusRoute();
                    busRoute.setBusRouteId(cursor.getInt(0));
                    busRoute.setBusRouteNumber(cursor.getString(1));
                    busRoute.setBusRouteBuses(buses);
                    fastestDirectTrip.setBusRoute(busRoute);

                    fastestDirectTrip.setTripDuration(buses.get(0).getBusETA() + travelTimeOnSecondLeg);
                }
            }
            else
            {
                ArrayList<Bus> buses = getScheduledBuses(cursor.getInt(0),
                        cursor.getString(1), cursor.getInt(2),
                        currentTimeInMinutesSinceMidnight + firstLegTripDuration + 2);

                if (buses != null)
                {
                    int travelTimeOnSecondLeg = CommonMethods.calculateTravelTime(
                            cursor.getInt(0), cursor.getString(1),
                            cursor.getInt(2), cursor.getInt(3));

                    if ((buses.get(0).getBusETA() + travelTimeOnSecondLeg) <
                            fastestDirectTrip.getTripDuration())
                    {
                        BusStop originBusStop = new BusStop();
                        originBusStop.setBusStopName(transitPointBusStopName);
                        originBusStop.setBusStopRouteOrder(cursor.getInt(2));
                        fastestDirectTrip.setOriginBusStop(originBusStop);

                        BusStop destinationBusStop = new BusStop();
                        destinationBusStop.setBusStopName(destinationBusStopName);
                        destinationBusStop.setBusStopRouteOrder(cursor.getInt(3));
                        fastestDirectTrip.setDestinationBusStop(destinationBusStop);

                        BusRoute busRoute = new BusRoute();
                        busRoute.setBusRouteId(cursor.getInt(0));
                        busRoute.setBusRouteNumber(cursor.getString(1));
                        busRoute.setBusRouteBuses(buses);
                        fastestDirectTrip.setBusRoute(busRoute);

                        fastestDirectTrip.setTripDuration(buses.get(0).getBusETA() + travelTimeOnSecondLeg);
                    }
                }
            }

        }
        cursor.close();
        return fastestDirectTrip;
    }

    private ArrayList<Bus> getScheduledBuses(int busRouteId, String busRouteNumber,
                                             int originBusStopRouteOrder, int currentTime)
    {
        int travelTimeToOrigin = CommonMethods.calculateTravelTime(busRouteId,
                busRouteNumber, 1, originBusStopRouteOrder);

        Cursor cursor = db.rawQuery("select RouteTimings.RouteDepartureTime" +
                        " from RouteTimings where RouteTimings.RouteId = " + busRouteId +
                        " and RouteTimings.RouteDepartureTime + " + travelTimeToOrigin +
                        " >= " + currentTime + " order by RouteTimings" +
                        ".RouteDepartureTime asc limit 1",
                null);

        if (cursor.moveToNext())
        {
            int timeOfDayBusWillArrive = cursor.getInt(0) + travelTimeToOrigin;

            int busETA = timeOfDayBusWillArrive - currentTimeInMinutesSinceMidnight;

            Bus bus = new Bus();
            bus.setBusETA(busETA);
            bus.setBusRouteOrder(1);

            cursor.close();
            ArrayList<Bus> buses = new ArrayList<>();
            buses.add(bus);
            return buses;
        }
        else
        {
            cursor.close();
            return null;
        }
    }

    private IndirectTrip setIndirectTripTravelTime(IndirectTrip indirectTrip)
    {
        Bus busOnSecondLeg = indirectTrip.getDirectTripOnSecondLeg().getBusRoute().getBusRouteBuses()
                .get(0);

        indirectTrip.setTripDuration(indirectTrip.getDirectTripOnFirstLeg().getTripDuration() + (
                busOnSecondLeg.getBusETA() - indirectTrip.getDirectTripOnFirstLeg().getTripDuration())
                + (indirectTrip.getDirectTripOnSecondLeg().getTripDuration() - busOnSecondLeg.getBusETA()));

        return indirectTrip;
    }

    @Override
    protected void onPostExecute(IndirectTrip indirectTrip)
    {
        super.onPostExecute(indirectTrip);

        if (!isCancelled())
        {
            caller.onIndirectTripFound(indirectTrip);
        }
    }
}
