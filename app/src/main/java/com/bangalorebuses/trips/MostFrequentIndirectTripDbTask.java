package com.bangalorebuses.trips;

import android.database.Cursor;
import android.os.AsyncTask;

import com.bangalorebuses.core.BusRoute;
import com.bangalorebuses.core.BusStop;
import com.bangalorebuses.utils.CommonMethods;

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

    @Override
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
