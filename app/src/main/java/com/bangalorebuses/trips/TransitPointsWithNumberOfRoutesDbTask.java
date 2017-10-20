package com.bangalorebuses.trips;

import android.database.Cursor;
import android.os.AsyncTask;

import java.util.ArrayList;

import static com.bangalorebuses.utils.Constants.NUMBER_OF_ROUTES_TYPE_ORIGIN_TO_TRANSIT_POINT;
import static com.bangalorebuses.utils.Constants.db;

public class TransitPointsWithNumberOfRoutesDbTask extends AsyncTask<Void, Void, ArrayList<TransitPoint>>
{
    private TransitPointsHelper caller;
    private String routeCountType;
    private String originBusStopName;
    private String destinationBusStopName;

    public TransitPointsWithNumberOfRoutesDbTask(TransitPointsHelper caller, String originBusStopName, String destinationBusStopName,
                                                 String routeCountType)
    {
        this.caller = caller;
        this.routeCountType = routeCountType;
        this.originBusStopName = originBusStopName;
        this.destinationBusStopName = destinationBusStopName;
    }

    @Override
    protected ArrayList<TransitPoint> doInBackground(Void... params)
    {
        ArrayList<TransitPoint> transitPoints = new ArrayList<>();

        if (routeCountType.equals(NUMBER_OF_ROUTES_TYPE_ORIGIN_TO_TRANSIT_POINT))
        {
            Cursor cursor = db.rawQuery("SELECT Stops.StopName, sum(routeCountPerTransitStopId.routeCount) FROM Stops JOIN" +
                    " ( SELECT count(routesArrivingAtOriginBusStop.RouteId) AS routeCount, routesArrivingAtDestinationBusStop.StopId" +
                    " FROM ( SELECT RouteStops.RouteId, RouteStops.StopRouteOrder FROM Stops JOIN RouteStops WHERE Stops.StopId =" +
                    " RouteStops.StopId AND Stops.StopName = '" + originBusStopName + "') routesArrivingAtOriginBusStop JOIN ( SELECT" +
                    " RouteStops.RouteId, Stops.StopId, RouteStops.StopRouteOrder FROM Stops JOIN RouteStops WHERE Stops.StopId =" +
                    " RouteStops.StopId AND Stops.StopName IN ( SELECT DISTINCT directlyAccessibleStopNamesFromOriginStop.StopName" +
                    " FROM ( SELECT DISTINCT Stops.StopName FROM ( SELECT RouteStops.StopId FROM ( SELECT RouteStops.RouteId," +
                    " RouteStops.StopRouteOrder FROM Stops JOIN RouteStops WHERE Stops.StopId = RouteStops.StopId AND Stops.StopName" +
                    " = '" + originBusStopName + "') routesArrivingAtOriginStop JOIN RouteStops WHERE RouteStops.RouteId =" +
                    " routesArrivingAtOriginStop.RouteId AND RouteStops.StopRouteOrder > routesArrivingAtOriginStop.StopRouteOrder)" +
                    " directlyAccessibleStopIdsFromOriginStop JOIN Stops WHERE Stops.StopId =" +
                    " directlyAccessibleStopIdsFromOriginStop.StopId) directlyAccessibleStopNamesFromOriginStop JOIN" +
                    " ( SELECT DISTINCT Stops.StopName FROM ( SELECT RouteStops.StopId FROM ( SELECT RouteStops.RouteId," +
                    " RouteStops.StopRouteOrder FROM Stops JOIN RouteStops WHERE Stops.StopId = RouteStops.StopId AND Stops.StopName " +
                    "= '" + destinationBusStopName + "') routesArrivingAtDestinationStop JOIN RouteStops WHERE RouteStops.RouteId = " +
                    "routesArrivingAtDestinationStop.RouteId AND RouteStops.StopRouteOrder <" +
                    " routesArrivingAtDestinationStop.StopRouteOrder) directlyAccessibleStopIdsFromDestinationStop JOIN Stops" +
                    " WHERE Stops.StopId = directlyAccessibleStopIdsFromDestinationStop.StopId)" +
                    " directlyAccessibleStopNamesFromDestinationStop WHERE directlyAccessibleStopNamesFromOriginStop.StopName" +
                    " = directlyAccessibleStopNamesFromDestinationStop.StopName)) routesArrivingAtDestinationBusStop WHERE" +
                    " routesArrivingAtOriginBusStop.RouteId = routesArrivingAtDestinationBusStop.RouteId AND" +
                    " routesArrivingAtOriginBusStop.StopRouteOrder < routesArrivingAtDestinationBusStop.StopRouteOrder GROUP BY" +
                    " routesArrivingAtDestinationBusStop.StopId) routeCountPerTransitStopId WHERE routeCountPerTransitStopId.StopId" +
                    " = Stops.StopId GROUP BY Stops.StopName", null);

            while (cursor.moveToNext())
            {
                TransitPoint transitPoint = new TransitPoint();
                transitPoint.setBusStopName(cursor.getString(0));
                transitPoint.setNumberOfRoutesBetweenOriginAndTransitPoint(cursor.getInt(1));
                transitPoints.add(transitPoint);
            }

            cursor.close();
        }
        else
        {
            Cursor cursor = db.rawQuery("SELECT Stops.StopName, sum(routeCountPerTransitStopId.routeCount) FROM Stops JOIN" +
                    " ( SELECT count(routesArrivingAtTransitBusStops.RouteId) AS routeCount, routesArrivingAtTransitBusStops.StopId FROM" +
                    " ( SELECT RouteStops.RouteId, RouteStops.StopId, RouteStops.StopRouteOrder FROM Stops JOIN RouteStops WHERE Stops.StopName" +
                    " IN ( SELECT DISTINCT directlyAccessibleStopNamesFromOriginStop.StopName FROM ( SELECT DISTINCT Stops.StopName FROM (" +
                    " SELECT RouteStops.StopId FROM ( SELECT RouteStops.RouteId, RouteStops.StopRouteOrder FROM Stops JOIN RouteStops WHERE Stops.StopName" +
                    " = '" + originBusStopName + "' AND Stops.StopId = RouteStops.StopId) routesArrivingAtOriginStop JOIN RouteStops WHERE RouteStops.RouteId =" +
                    " routesArrivingAtOriginStop.RouteId AND RouteStops.StopRouteOrder > routesArrivingAtOriginStop.StopRouteOrder)" +
                    " directlyAccessibleStopIdsFromOriginStop JOIN Stops WHERE Stops.StopId = directlyAccessibleStopIdsFromOriginStop.StopId)" +
                    " directlyAccessibleStopNamesFromOriginStop JOIN ( SELECT DISTINCT Stops.StopName FROM ( SELECT RouteStops.StopId FROM (" +
                    " SELECT RouteStops.RouteId, RouteStops.StopRouteOrder FROM Stops JOIN RouteStops WHERE Stops.StopName = '" + destinationBusStopName +
                    "' AND" +
                    " Stops.StopId = RouteStops.StopId) routesArrivingAtDestinationStop JOIN RouteStops WHERE RouteStops.RouteId =" +
                    " routesArrivingAtDestinationStop.RouteId AND RouteStops.StopRouteOrder < routesArrivingAtDestinationStop.StopRouteOrder)" +
                    " directlyAccessibleStopIdsFromDestinationStop JOIN Stops WHERE Stops.StopId = directlyAccessibleStopIdsFromDestinationStop.StopId)" +
                    " directlyAccessibleStopNamesFromDestinationStop WHERE directlyAccessibleStopNamesFromOriginStop.StopName =" +
                    " directlyAccessibleStopNamesFromDestinationStop.StopName)AND Stops.StopId = RouteStops.StopId) routesArrivingAtTransitBusStops JOIN" +
                    " ( SELECT RouteStops.RouteId, RouteStops.StopRouteOrder FROM Stops JOIN RouteStops WHERE Stops.StopName = '" + destinationBusStopName +
                    "' AND Stops.StopId" +
                    " = RouteStops.StopId) routesArrivingAtDestinationBusStop WHERE routesArrivingAtTransitBusStops.RouteId =" +
                    " routesArrivingAtDestinationBusStop.RouteId AND routesArrivingAtTransitBusStops.StopRouteOrder <" +
                    " routesArrivingAtDestinationBusStop.StopRouteOrder GROUP BY routesArrivingAtTransitBusStops.StopId) routeCountPerTransitStopId" +
                    " WHERE routeCountPerTransitStopId.StopId = Stops.StopId GROUP BY Stops.StopName", null);

            while (cursor.moveToNext())
            {
                TransitPoint transitPoint = new TransitPoint();
                transitPoint.setBusStopName(cursor.getString(0));
                transitPoint.setNumberOfRoutesBetweenTransitPointAndDestination(cursor.getInt(1));
                transitPoints.add(transitPoint);
            }

            cursor.close();
        }

        return transitPoints;
    }

    @Override
    protected void onPostExecute(ArrayList<TransitPoint> transitPoints)
    {
        super.onPostExecute(transitPoints);

        if (!isCancelled())
        {
            if (routeCountType.equals(NUMBER_OF_ROUTES_TYPE_ORIGIN_TO_TRANSIT_POINT))
            {
                caller.onTransitPointsAndRouteCountOriginToTPFound(transitPoints);
            }
            else
            {
                caller.onTransitPointsAndRouteCountTPToDestFound(transitPoints);
            }
        }
    }
}
