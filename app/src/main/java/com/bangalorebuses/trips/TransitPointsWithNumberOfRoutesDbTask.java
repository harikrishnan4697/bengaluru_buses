package com.bangalorebuses.trips;

import android.database.Cursor;
import android.os.AsyncTask;

import java.util.ArrayList;

import static com.bangalorebuses.utils.Constants.db;

class TransitPointsWithNumberOfRoutesDbTask extends AsyncTask<Void, Void, ArrayList<TransitPoint>>
{
    private TransitPointsHelper caller;
    private String originBusStopName;
    private String destinationBusStopName;

    TransitPointsWithNumberOfRoutesDbTask(TransitPointsHelper caller, String originBusStopName,
                                          String destinationBusStopName)
    {
        this.caller = caller;
        this.originBusStopName = originBusStopName;
        this.destinationBusStopName = destinationBusStopName;
    }

    @Override
    protected ArrayList<TransitPoint> doInBackground(Void... params)
    {
        ArrayList<TransitPoint> transitPointsWithNumberOfBusRoutesOnFirstLeg =
                getTransitPointsAndFrequencyOfBusRoutesOnFirstLeg();

        ArrayList<TransitPoint> transitPointsWithNumberOfBusRoutesOnSecondLeg =
                getTransitPointsAndFrequencyOfBusRoutesOnSecondLeg();

        for (TransitPoint transitPoint1 :
                transitPointsWithNumberOfBusRoutesOnFirstLeg)
        {
            for (TransitPoint transitPoint2 :
                    transitPointsWithNumberOfBusRoutesOnSecondLeg)
            {
                if (transitPoint1.getBusStopName().equals(
                        transitPoint2.getBusStopName()))
                {
                    transitPoint1.setNumberOfRoutesBetweenTransitPointAndDestination(
                            transitPoint2.getNumberOfRoutesBetweenTransitPointAndDestination());
                    break;
                }
            }
        }
        return transitPointsWithNumberOfBusRoutesOnFirstLeg;
    }

    private ArrayList<TransitPoint> getTransitPointsAndFrequencyOfBusRoutesOnFirstLeg()
    {
        Cursor cursor = db.rawQuery("SELECT sum(Routes.RouteDeparturesPerDay)," +
                " routesBetweenOriginAndTP.StopName FROM Routes JOIN ( SELECT" +
                " sub2.StopName, sub1.RouteId FROM ( SELECT RouteStops.StopRouteOrder," +
                " RouteStops.RouteId FROM Stops JOIN RouteStops WHERE Stops.StopName" +
                " = '" + originBusStopName + "' AND Stops.StopId = RouteStops.StopId) sub1 JOIN" +
                " ( SELECT RouteStops.RouteId, RouteStops.StopRouteOrder, Stops.StopName" +
                " FROM Stops JOIN RouteStops WHERE Stops.StopId = RouteStops.StopId AND" +
                " Stops.StopName IN ( SELECT DISTINCT directlyAccessibleStopNamesFromOriginStop" +
                ".StopName FROM ( SELECT DISTINCT Stops.StopName FROM ( SELECT DISTINCT" +
                " RouteStops.StopId FROM ( SELECT RouteStops.RouteId, RouteStops" +
                ".StopRouteOrder FROM Stops JOIN RouteStops WHERE Stops.StopName" +
                " = '" + originBusStopName + "' AND Stops.StopId = RouteStops.StopId) routesArrivingAtOriginStop" +
                " JOIN RouteStops WHERE RouteStops.RouteId = routesArrivingAtOriginStop.RouteId" +
                " AND RouteStops.StopRouteOrder > routesArrivingAtOriginStop.StopRouteOrder)" +
                " directlyAccessibleStopIdsFromOriginStop JOIN Stops WHERE Stops.StopId " +
                "= directlyAccessibleStopIdsFromOriginStop.StopId)" +
                " directlyAccessibleStopNamesFromOriginStop JOIN ( SELECT DISTINCT Stops" +
                ".StopName FROM ( SELECT DISTINCT RouteStops.StopId FROM ( SELECT RouteStops" +
                ".RouteId, RouteStops.StopRouteOrder FROM Stops JOIN RouteStops WHERE Stops" +
                ".StopName = '" + destinationBusStopName + "' AND Stops.StopId = RouteStops.StopId)" +
                " routesArrivingAtDestinationStop JOIN RouteStops WHERE RouteStops.RouteId" +
                " = routesArrivingAtDestinationStop.RouteId AND RouteStops.StopRouteOrder " +
                "< routesArrivingAtDestinationStop.StopRouteOrder) directlyAccessibleStopIdsFromDestinationStop" +
                " JOIN Stops WHERE Stops.StopId = directlyAccessibleStopIdsFromDestinationStop.StopId)" +
                " directlyAccessibleStopNamesFromDestinationStop WHERE" +
                " directlyAccessibleStopNamesFromOriginStop.StopName =" +
                " directlyAccessibleStopNamesFromDestinationStop.StopName)) sub2 WHERE sub1" +
                ".RouteId = sub2.RouteId AND sub1.StopRouteOrder < sub2.StopRouteOrder)" +
                " routesBetweenOriginAndTP WHERE Routes.RouteId = routesBetweenOriginAndTP" +
                ".RouteId GROUP BY routesBetweenOriginAndTP.StopName", null);

        ArrayList<TransitPoint> transitPoints = new ArrayList<>();

        while (cursor.moveToNext())
        {
            TransitPoint transitPoint = new TransitPoint();
            transitPoint.setNumberOfRoutesBetweenOriginAndTransitPoint(
                    cursor.getInt(0));
            transitPoint.setBusStopName(cursor.getString(1));
            transitPoints.add(transitPoint);
        }

        cursor.close();
        return transitPoints;
    }

    private ArrayList<TransitPoint> getTransitPointsAndFrequencyOfBusRoutesOnSecondLeg()
    {
        Cursor cursor = db.rawQuery("SELECT sum(Routes.RouteDeparturesPerDay)," +
                " routesBetweenOriginAndTP.StopName FROM Routes JOIN (" +
                " SELECT sub1.StopName, sub1.RouteId FROM ( SELECT Stops.StopName," +
                " RouteStops.StopRouteOrder, RouteStops.RouteId FROM Stops JOIN" +
                " RouteStops WHERE Stops.StopId = RouteStops.StopId AND Stops.StopName" +
                " IN ( SELECT DISTINCT directlyAccessibleStopNamesFromOriginStop.StopName" +
                " FROM ( SELECT DISTINCT Stops.StopName FROM ( SELECT DISTINCT RouteStops" +
                ".StopId FROM ( SELECT RouteStops.RouteId, RouteStops.StopRouteOrder FROM" +
                " Stops JOIN RouteStops WHERE Stops.StopName = '" + originBusStopName + "' AND Stops" +
                ".StopId = RouteStops.StopId) routesArrivingAtOriginStop JOIN RouteStops" +
                " WHERE RouteStops.RouteId = routesArrivingAtOriginStop.RouteId AND RouteStops" +
                ".StopRouteOrder > routesArrivingAtOriginStop.StopRouteOrder)" +
                " directlyAccessibleStopIdsFromOriginStop JOIN Stops WHERE Stops.StopId =" +
                " directlyAccessibleStopIdsFromOriginStop.StopId) directlyAccessibleStopNamesFromOriginStop" +
                " JOIN ( SELECT DISTINCT Stops.StopName FROM ( SELECT DISTINCT RouteStops.StopId" +
                " FROM ( SELECT RouteStops.RouteId, RouteStops.StopRouteOrder FROM Stops JOIN" +
                " RouteStops WHERE Stops.StopName = '" + destinationBusStopName + "' AND Stops.StopId = RouteStops" +
                ".StopId) routesArrivingAtDestinationStop JOIN RouteStops WHERE RouteStops.RouteId" +
                " = routesArrivingAtDestinationStop.RouteId AND RouteStops.StopRouteOrder <" +
                " routesArrivingAtDestinationStop.StopRouteOrder) directlyAccessibleStopIdsFromDestinationStop" +
                " JOIN Stops WHERE Stops.StopId = directlyAccessibleStopIdsFromDestinationStop.StopId)" +
                " directlyAccessibleStopNamesFromDestinationStop WHERE directlyAccessibleStopNamesFromOriginStop" +
                ".StopName = directlyAccessibleStopNamesFromDestinationStop.StopName)) sub1 JOIN" +
                " ( SELECT RouteStops.RouteId, RouteStops.StopRouteOrder FROM Stops JOIN" +
                " RouteStops WHERE Stops.StopName = '" + destinationBusStopName + "' AND Stops.StopId = RouteStops" +
                ".StopId) sub2 WHERE sub1.RouteId = sub2.RouteId AND sub1.StopRouteOrder <" +
                " sub2.StopRouteOrder) routesBetweenOriginAndTP WHERE Routes.RouteId =" +
                " routesBetweenOriginAndTP.RouteId GROUP BY routesBetweenOriginAndTP.StopName", null);

        ArrayList<TransitPoint> transitPoints = new ArrayList<>();

        while (cursor.moveToNext())
        {
            TransitPoint transitPoint = new TransitPoint();
            transitPoint.setNumberOfRoutesBetweenTransitPointAndDestination(
                    cursor.getInt(0));
            transitPoint.setBusStopName(cursor.getString(1));
            transitPoints.add(transitPoint);
        }

        cursor.close();
        return transitPoints;
    }

    @Override
    protected void onPostExecute(ArrayList<TransitPoint> transitPoints)
    {
        super.onPostExecute(transitPoints);

        if (!isCancelled())
        {
            caller.onTransitPointsFound(transitPoints);
        }
    }
}