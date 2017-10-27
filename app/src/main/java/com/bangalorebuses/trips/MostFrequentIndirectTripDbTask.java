package com.bangalorebuses.trips;

import android.database.Cursor;
import android.os.AsyncTask;

import com.bangalorebuses.core.Bus;
import com.bangalorebuses.core.BusRoute;
import com.bangalorebuses.core.BusStop;
import com.bangalorebuses.utils.CommonMethods;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import static com.bangalorebuses.utils.Constants.db;

class MostFrequentIndirectTripDbTask extends AsyncTask<Void, Void, IndirectTrip>
{
    private TransitPointsHelper caller;

    private String originBusStopName;
    private String transitPointBusStopName;
    private String destinationBusStopName;

    MostFrequentIndirectTripDbTask(TransitPointsHelper caller, String originBusStopName,
                                   String transitPointBusStopName,
                                   String destinationBusStopName)
    {
        this.originBusStopName = originBusStopName;
        this.transitPointBusStopName = transitPointBusStopName;
        this.destinationBusStopName = destinationBusStopName;
        this.caller = caller;
    }

    /*@Override
    protected IndirectTrip doInBackground(Void... voids)
    {
        IndirectTrip indirectTrip = new IndirectTrip();

        DirectTrip directTripOnFirstLeg = getDirectTrip(
                originBusStopName, transitPointBusStopName);

        DirectTrip directTripOnSecondLeg = getDirectTrip(
                transitPointBusStopName, destinationBusStopName);

        indirectTrip.setDirectTripOnFirstLeg(directTripOnFirstLeg);
        indirectTrip.setDirectTripOnSecondLeg(directTripOnSecondLeg);

        TransitPoint transitPoint = new TransitPoint();
        transitPoint.setBusStopName(transitPointBusStopName);
        indirectTrip.setTransitPoint(transitPoint);

        if (directTripOnFirstLeg != null && directTripOnSecondLeg != null)
        {
            indirectTrip.setTripDuration(directTripOnFirstLeg.getTripDuration()
                    + directTripOnSecondLeg.getTripDuration());
        }

        return indirectTrip;
    }

    private DirectTrip getDirectTrip(String originBusStopName,
                                     String destinationBusStopName)
    {
        Cursor cursor = db.rawQuery("SELECT Routes.RouteId, Routes.RouteNumber, routesBetweenOriginAndTP" +
                ".originBusStopRouteOrder, routesBetweenOriginAndTP.destinationBusStopRouteOrder FROM Routes" +
                " JOIN ( SELECT sub1.RouteId, sub1.StopId," +
                " sub1.StopName, sub1.StopDirectionName, sub1.StopRouteOrder as originBusStopRouteOrder," +
                " sub2.StopRouteOrder as destinationBusStopRouteOrder FROM ( SELECT Stops.StopId, Stops" +
                ".StopName, Stops.StopDirectionName, RouteStops.StopRouteOrder, RouteStops.RouteId FROM" +
                " Stops JOIN RouteStops WHERE Stops.StopName = '" + originBusStopName + "' AND Stops.StopId = RouteStops" +
                ".StopId) sub1 JOIN ( SELECT RouteStops.RouteId, RouteStops.StopRouteOrder FROM Stops JOIN" +
                " RouteStops WHERE Stops.StopName = '" + destinationBusStopName + "' AND Stops.StopId = RouteStops" +
                ".StopId) sub2 WHERE sub1.RouteId = sub2.RouteId AND sub1.StopRouteOrder < sub2.StopRouteOrder)" +
                " routesBetweenOriginAndTP WHERE Routes.RouteId = routesBetweenOriginAndTP.RouteId order by" +
                " Routes.RouteDeparturesPerDay desc", null);

        if (cursor.moveToNext())
        {
            DirectTrip directTrip = new DirectTrip();

            BusRoute busRoute = new BusRoute();
            busRoute.setBusRouteId(cursor.getInt(0));
            busRoute.setBusRouteNumber(cursor.getString(1));

            BusStop originBusStop = new BusStop();
            BusStop destinationBusStop = new BusStop();

            originBusStop.setBusStopRouteOrder(cursor.getInt(2));
            destinationBusStop.setBusStopRouteOrder(cursor.getInt(3));

            directTrip.setBusRoute(busRoute);
            directTrip.setOriginBusStop(originBusStop);
            directTrip.setDestinationBusStop(destinationBusStop);

            int directTripWaitTime = 10;
            directTrip.setTripDuration(directTripWaitTime + CommonMethods
                    .calculateTravelTime(busRoute.getBusRouteId(), busRoute
                            .getBusRouteNumber(), originBusStop
                            .getBusStopRouteOrder(), destinationBusStop
                            .getBusStopRouteOrder()));

            return directTrip;
        }
        else
        {
            cursor.close();
            return null;
        }
    }*/

    @Override
    protected IndirectTrip doInBackground(Void... voids)
    {
        ArrayList<IndirectTrip> indirectTrips = new ArrayList<>();

        ArrayList<DirectTrip> possibleDirectTripsOnFirstLeg = getDirectTripsOnFirstLeg(
                originBusStopName, transitPointBusStopName);

        ArrayList<DirectTrip> possibleDirectTripsOnSecondLeg = getDirectTripsOnSecondLeg(
                transitPointBusStopName, destinationBusStopName);

        if (possibleDirectTripsOnFirstLeg.size() == 0 ||
                possibleDirectTripsOnSecondLeg.size() == 0)
        {
            return null;
        }

        for (DirectTrip directTripOnFirstLeg : possibleDirectTripsOnFirstLeg)

        {
            IndirectTrip indirectTrip = new IndirectTrip();
            indirectTrip.setDirectTripOnFirstLeg(directTripOnFirstLeg);

            TransitPoint transitPoint = new TransitPoint();
            transitPoint.setBusStopName(transitPointBusStopName);
            indirectTrip.setTransitPoint(transitPoint);

            indirectTrip.setPossibleDirectTripsOnSecondLeg(
                    possibleDirectTripsOnSecondLeg);

            if (indirectTrip.getDirectTripOnFirstLeg() != null &&
                    indirectTrip.getPossibleDirectTripsOnSecondLeg().size() != 0)
            {
                indirectTrip.setDirectTripOnSecondLeg(selectBestDirectTripOnSecondLeg(
                        indirectTrip));

                if (indirectTrip.getDirectTripOnSecondLeg() != null)
                {
                    indirectTrips.add(setIndirectTripTravelTime(indirectTrip));
                }
            }
        }

        if (indirectTrips.size() == 0)
        {
            return null;
        }

        Collections.sort(indirectTrips, new Comparator<IndirectTrip>()
        {
            @Override
            public int compare(IndirectTrip o1, IndirectTrip o2)
            {
                return o1.getTripDuration() - o2.getTripDuration();
            }
        });

        return indirectTrips.get(0);
    }

    private ArrayList<DirectTrip> getDirectTripsOnFirstLeg(String originBusStopName,
                                                           String transitPointBusStopName)
    {
        Cursor cursor = db.rawQuery("SELECT Routes.RouteId, Routes.RouteNumber, Routes.RouteDirection," +
                " routesBetweenOriginAndTP.originBusStopDirectionName," +
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
                " desc", null);

        ArrayList<DirectTrip> directTrips = new ArrayList<>();

        for (int i = 0; i < 10; i++)
        {
            if (cursor.moveToNext())
            {
                DirectTrip directTrip = new DirectTrip();

                BusStop originBusStop = new BusStop();
                originBusStop.setBusStopName(originBusStopName);
                originBusStop.setBusStopDirectionName(cursor.getString(3));
                originBusStop.setBusStopRouteOrder(cursor.getInt(4));
                directTrip.setOriginBusStop(originBusStop);

                BusStop destinationBusStop = new BusStop();
                destinationBusStop.setBusStopName(transitPointBusStopName);
                destinationBusStop.setBusStopRouteOrder(cursor.getInt(5));
                directTrip.setDestinationBusStop(destinationBusStop);

                BusRoute busRoute = new BusRoute();
                busRoute.setBusRouteId(cursor.getInt(0));
                busRoute.setBusRouteNumber(cursor.getString(1));
                busRoute.setBusRouteDirection(cursor.getString(2));
                busRoute.setBusRouteBuses(getScheduledBuses(busRoute, originBusStop));
                directTrip.setBusRoute(busRoute);

                if (directTrip.getBusRoute().getBusRouteBuses().size() > 0)
                {
                    int travelTimeOnFirstLeg = CommonMethods.calculateTravelTime(
                            busRoute.getBusRouteId(), busRoute.getBusRouteNumber(),
                            originBusStop.getBusStopRouteOrder(), destinationBusStop
                                    .getBusStopRouteOrder());

                    directTrip.setTripDuration(busRoute.getBusRouteBuses().get(0).getBusETA()
                            + travelTimeOnFirstLeg);

                    directTrips.add(directTrip);
                }
            }
            else
            {
                break;
            }
        }

        cursor.close();
        return directTrips;
    }

    private ArrayList<DirectTrip> getDirectTripsOnSecondLeg(String transitPointBusStopName,
                                                            String destinationBusStopName)
    {
        Cursor cursor = db.rawQuery("SELECT Routes.RouteId, Routes.RouteNumber, Routes.RouteDirection," +
                " routesBetweenOriginAndTP.originBusStopDirectionName," +
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
                " desc", null);

        ArrayList<DirectTrip> directTrips = new ArrayList<>();

        for (int i = 0; i < 10; i++)
        {
            if (cursor.moveToNext())
            {
                DirectTrip directTrip = new DirectTrip();

                BusStop originBusStop = new BusStop();
                originBusStop.setBusStopName(transitPointBusStopName);
                originBusStop.setBusStopDirectionName(cursor.getString(3));
                originBusStop.setBusStopRouteOrder(cursor.getInt(4));
                directTrip.setOriginBusStop(originBusStop);

                BusStop destinationBusStop = new BusStop();
                destinationBusStop.setBusStopName(destinationBusStopName);
                destinationBusStop.setBusStopRouteOrder(cursor.getInt(5));
                directTrip.setDestinationBusStop(destinationBusStop);

                BusRoute busRoute = new BusRoute();
                busRoute.setBusRouteId(cursor.getInt(0));
                busRoute.setBusRouteNumber(cursor.getString(1));
                busRoute.setBusRouteDirection(cursor.getString(2));
                busRoute.setBusRouteBuses(getScheduledBuses(busRoute, originBusStop));
                directTrip.setBusRoute(busRoute);

                if (directTrip.getBusRoute().getBusRouteBuses().size() > 0)
                {
                    directTrips.add(directTrip);
                }
            }
            else
            {
                break;
            }
        }

        cursor.close();
        return directTrips;
    }

    private ArrayList<Bus> getScheduledBuses(BusRoute busRoute, BusStop originBusStop)
    {
        ArrayList<Bus> buses = new ArrayList<>();

        Cursor cursor = db.rawQuery("select RouteTimings.RouteDepartureTime" +
                " from RouteTimings where RouteTimings.RouteId = " +
                busRoute.getBusRouteId(), null);

        while (cursor.moveToNext())
        {
            Bus bus = new Bus();

            int timeOfDayBusWillArrive = cursor.getInt(0) + CommonMethods
                    .calculateTravelTime(busRoute.getBusRouteId(), busRoute.getBusRouteNumber(),
                            1, originBusStop.getBusStopRouteOrder());

            Calendar calendar = Calendar.getInstance();

            int busETA = timeOfDayBusWillArrive - ((calendar.get(Calendar.HOUR_OF_DAY) * 60) +
                    calendar.get(Calendar.MINUTE));

            if (busETA > -1)
            {
                bus.setBusRoute(busRoute);
                bus.setBusETA(busETA);
                bus.setBusRouteOrder(1);
                buses.add(bus);
            }
        }

        cursor.close();

        // Sort the buses based on their ETA
        Collections.sort(buses, new Comparator<Bus>()
        {
            @Override
            public int compare(Bus o1, Bus o2)
            {
                return o1.getBusETA() - o2.getBusETA();
            }
        });

        return buses;
    }

    private DirectTrip selectBestDirectTripOnSecondLeg(IndirectTrip indirectTrip)
    {
        DirectTrip directTripOnFirstLeg = indirectTrip
                .getDirectTripOnFirstLeg();

        DirectTrip bestDirectTripOnSecondLeg = null;

        Bus bestBusOnSecondLeg = null;

        for (DirectTrip directTripOnSecondLeg :
                indirectTrip.getPossibleDirectTripsOnSecondLeg())
        {
            for (Bus busOnSecondLeg : directTripOnSecondLeg.getBusRoute()
                    .getBusRouteBuses())
            {
                if (busOnSecondLeg.getBusETA() > (directTripOnFirstLeg
                        .getTripDuration() + 2))
                {
                    if (bestBusOnSecondLeg != null)
                    {
                        if (busOnSecondLeg.getBusETA() <
                                bestBusOnSecondLeg.getBusETA())
                        {
                            bestBusOnSecondLeg = busOnSecondLeg;
                            bestDirectTripOnSecondLeg =
                                    directTripOnSecondLeg;
                        }
                    }
                    else
                    {
                        bestBusOnSecondLeg = busOnSecondLeg;
                        bestDirectTripOnSecondLeg =
                                directTripOnSecondLeg;
                    }
                }
            }
        }

        if (bestDirectTripOnSecondLeg != null)
        {
            ArrayList<Bus> busesOnBestDirectTripOnSecondLeg = new ArrayList<>();
            busesOnBestDirectTripOnSecondLeg.add(bestBusOnSecondLeg);

            bestDirectTripOnSecondLeg.getBusRoute().setBusRouteBuses(
                    busesOnBestDirectTripOnSecondLeg);

            BusRoute busRouteOnSecondLeg = bestDirectTripOnSecondLeg.getBusRoute();

            BusStop transitPointBusStop = bestDirectTripOnSecondLeg.getOriginBusStop();

            BusStop destinationBusStop = bestDirectTripOnSecondLeg.getDestinationBusStop();

            int travelTimeOnSecondLeg = CommonMethods.calculateTravelTime(busRouteOnSecondLeg
                    .getBusRouteId(), busRouteOnSecondLeg.getBusRouteNumber(), transitPointBusStop
                    .getBusStopRouteOrder(), destinationBusStop.getBusStopRouteOrder());

            bestDirectTripOnSecondLeg.setTripDuration(bestBusOnSecondLeg
                    .getBusETA() + travelTimeOnSecondLeg);
        }

        return bestDirectTripOnSecondLeg;
    }

    private IndirectTrip setIndirectTripTravelTime(IndirectTrip
                                                           indirectTrip)
    {
        DirectTrip directTripOnFirstLeg = indirectTrip.getDirectTripOnFirstLeg();
        DirectTrip directTripOnSecondLeg = indirectTrip.getDirectTripOnSecondLeg();
        Bus busOnSecondLeg = directTripOnSecondLeg.getBusRoute().getBusRouteBuses()
                .get(0);

        indirectTrip.setTripDuration(directTripOnFirstLeg.getTripDuration() + (
                busOnSecondLeg.getBusETA() - directTripOnFirstLeg.getTripDuration())
                + (directTripOnSecondLeg.getTripDuration() - busOnSecondLeg.getBusETA()));

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
