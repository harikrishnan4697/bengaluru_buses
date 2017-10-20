package com.bangalorebuses.trips;

import android.database.Cursor;
import android.os.AsyncTask;

import com.bangalorebuses.core.BusRoute;
import com.bangalorebuses.core.BusStop;

import java.util.ArrayList;

import static com.bangalorebuses.utils.Constants.db;

class DirectTripsDbTask extends AsyncTask<Void, Void, ArrayList<DirectTrip>>
{
    private DirectTripsHelper caller;
    private String originBusStopName;
    private String destinationBusStopName;

    DirectTripsDbTask(DirectTripsHelper caller, String originBusStopName,
                      String destinationBusStopName)
    {
        this.caller = caller;
        this.originBusStopName = originBusStopName;
        this.destinationBusStopName = destinationBusStopName;
    }

    @Override
    protected ArrayList<DirectTrip> doInBackground(Void... voids)
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
                originBusStop.setBusStopName(originBusStopName);
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
                directTrip.setBusRoute(busRoute);

                directTrips.add(directTrip);
            }
            else
            {
                break;
            }
        }

        cursor.close();
        return directTrips;
    }

    @Override
    protected void onPostExecute(ArrayList<DirectTrip> directTrips)
    {
        super.onPostExecute(directTrips);

        if (!isCancelled())
        {
            caller.onDirectTripsFound(directTrips);
        }
    }
}
