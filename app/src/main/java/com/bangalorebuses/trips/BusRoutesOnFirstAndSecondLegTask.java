package com.bangalorebuses.trips;

import android.database.Cursor;
import android.os.AsyncTask;

import com.bangalorebuses.core.BusRoute;
import com.bangalorebuses.core.BusStop;

import java.util.ArrayList;

import static com.bangalorebuses.utils.Constants.db;

class BusRoutesOnFirstAndSecondLegTask extends AsyncTask<Void, Void, TransitPoint>
{
    private IndirectTripDetailsHelper caller;
    private String originBusStopName;
    private String transitPointBusStopName;
    private String destinationBusStopName;

    BusRoutesOnFirstAndSecondLegTask(IndirectTripDetailsHelper caller,
                                     String originBusStopName,
                                     String transitPointBusStopName,
                                     String destinationBusStopName)
    {
        this.caller = caller;
        this.originBusStopName = originBusStopName;
        this.transitPointBusStopName = transitPointBusStopName;
        this.destinationBusStopName = destinationBusStopName;
    }

    @Override
    protected TransitPoint doInBackground(Void... voids)
    {
        TransitPoint transitPoint = new TransitPoint();
        transitPoint.setTransitPointName(transitPointBusStopName);
        transitPoint.setBusRoutesToTransitPoint(getBusRoutesOnFirstLeg());

        return transitPoint;
    }

    private ArrayList<BusRoute> getBusRoutesOnFirstLeg()
    {
        Cursor cursor = db.rawQuery("SELECT Routes.RouteNumber, Routes.RouteDirection," +
                " routesBetweenOriginAndTP.originBusStopDirectionName, routesBetweenOriginAndTP" +
                ".originBusStopRouteOrder, routesBetweenOriginAndTP.destinationBusStopRouteOrder" +
                " FROM Routes JOIN ( SELECT sub1.RouteId, sub1.StopId, sub1.StopDirectionName as" +
                " originBusStopDirectionName, sub1.StopRouteOrder as originBusStopRouteOrder, sub2" +
                ".StopRouteOrder as destinationBusStopRouteOrder FROM ( SELECT Stops.StopId, Stops" +
                ".StopName, Stops.StopDirectionName, RouteStops.StopRouteOrder, RouteStops.RouteId" +
                " FROM Stops JOIN RouteStops WHERE Stops.StopName = '" + originBusStopName + "' AND Stops.StopId" +
                " = RouteStops.StopId) sub1 JOIN ( SELECT RouteStops.RouteId, RouteStops.StopRouteOrder" +
                " FROM Stops JOIN RouteStops WHERE Stops.StopName = '" + transitPointBusStopName + "' AND Stops.StopId =" +
                " RouteStops.StopId) sub2 WHERE sub1.RouteId = sub2.RouteId AND sub1.StopRouteOrder < sub2" +
                ".StopRouteOrder) routesBetweenOriginAndTP WHERE Routes.RouteId = routesBetweenOriginAndTP" +
                ".RouteId order by Routes.RouteDeparturesPerDay desc", null);

        ArrayList<BusRoute> busRoutes = new ArrayList<>();

        while (cursor.moveToNext())
        {
            BusRoute busRoute = new BusRoute();
            busRoute.setBusRouteNumber(cursor.getString(0));
            busRoute.setBusRouteDirection(cursor.getString(1));

            BusStop originBusStop = new BusStop();
            originBusStop.setBusStopName(originBusStopName);
            originBusStop.setBusStopDirectionName(cursor.getString(2));
            originBusStop.setBusStopRouteOrder(cursor.getInt(3));

            BusStop transitPointBusStop = new BusStop();
            transitPointBusStop.setBusStopName(transitPointBusStopName);
            transitPointBusStop.setBusStopRouteOrder(cursor.getInt(4));

            busRoute.setTripPlannerOriginBusStop(originBusStop);
            busRoute.setTripPlannerDestinationBusStop(transitPointBusStop);

            busRoutes.add(busRoute);
        }

        cursor.close();
        return busRoutes;
    }

    @Override
    protected void onPostExecute(TransitPoint transitPoint)
    {
        super.onPostExecute(transitPoint);

        caller.onBusRoutesToAndFromTransitPointFound(transitPoint);
    }
}
