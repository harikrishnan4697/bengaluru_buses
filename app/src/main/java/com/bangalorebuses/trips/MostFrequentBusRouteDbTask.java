package com.bangalorebuses.trips;

import android.database.Cursor;
import android.os.AsyncTask;

import com.bangalorebuses.core.Bus;
import com.bangalorebuses.core.BusRoute;
import com.bangalorebuses.core.BusStop;

import static com.bangalorebuses.utils.Constants.db;

class MostFrequentBusRouteDbTask extends AsyncTask<Void, Void, Void>
{
    private IndirectTripHelper caller;

    private String originBusStopName;
    private String transitPointBusStopName;
    private String destinationBusStopName;

    private BusRoute mostFrequentBusRouteOnFirstLeg;
    private BusRoute mostFrequentBusRouteOnSecondLeg;

    MostFrequentBusRouteDbTask(IndirectTripHelper caller, String originBusStopName,
                               String transitPointBusStopName,
                               String destinationBusStopName)
    {
        this.originBusStopName = originBusStopName;
        this.transitPointBusStopName = transitPointBusStopName;
        this.destinationBusStopName = destinationBusStopName;
        this.caller = caller;
    }

    @Override
    protected Void doInBackground(Void... voids)
    {
        mostFrequentBusRouteOnFirstLeg = getMostFrequentBusRoute(
                originBusStopName, transitPointBusStopName);

        mostFrequentBusRouteOnSecondLeg = getMostFrequentBusRoute(
                transitPointBusStopName, destinationBusStopName);

        return null;
    }

    private BusRoute getMostFrequentBusRoute(String originBusStopName,
                                             String destinationBusStopName)
    {
        Cursor cursor = db.rawQuery("SELECT Routes.RouteId, Routes.RouteNumber, Routes.RouteDeparturesPerDay," +
                " routesBetweenOriginAndTP.originBusStopRouteOrder, routesBetweenOriginAndTP" +
                ".destinationBusStopRouteOrder FROM Routes JOIN ( SELECT sub1.RouteId, sub1.StopId," +
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
            BusRoute busRoute = new BusRoute();
            busRoute.setBusRouteId(cursor.getInt(0));
            busRoute.setBusRouteNumber(cursor.getString(1));

            BusStop originBusStop = new BusStop();
            BusStop destinationBusStop = new BusStop();

            originBusStop.setBusStopRouteOrder(cursor.getInt(3));
            destinationBusStop.setBusStopRouteOrder(cursor.getInt(4));

            busRoute.setTripPlannerOriginBusStop(originBusStop);
            busRoute.setTripPlannerDestinationBusStop(destinationBusStop);

            return busRoute;
        }
        else
        {
            cursor.close();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        super.onPostExecute(aVoid);

        if (!isCancelled())
        {
            caller.onMostFrequentBusRouteFound(transitPointBusStopName,
                    mostFrequentBusRouteOnFirstLeg, mostFrequentBusRouteOnSecondLeg);
        }
    }
}
