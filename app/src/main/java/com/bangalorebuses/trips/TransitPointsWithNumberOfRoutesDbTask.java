package com.bangalorebuses.trips;

import android.database.Cursor;
import android.os.AsyncTask;

import java.util.ArrayList;

import static com.bangalorebuses.utils.Constants.NUMBER_OF_ROUTES_TYPE_ORIGIN_TO_TRANSIT_POINT;
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

    private ArrayList<TransitPoint> getTransitPointsAndNumberOfBusRoutesOnFirstLeg()
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

        ArrayList<TransitPoint> transitPoints = new ArrayList<>();

        while (cursor.moveToNext())
        {
            TransitPoint transitPoint = new TransitPoint();
            transitPoint.setBusStopName(cursor.getString(0));
            //transitPoint.setNumberOfRoutesBetweenOriginAndTransitPoint(cursor.getInt(1));
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

    private ArrayList<TransitPoint> getTransitPointsAndNumberOfBusRoutesOnSecondLeg()
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

        ArrayList<TransitPoint> transitPoints = new ArrayList<>();

        while (cursor.moveToNext())
        {
            TransitPoint transitPoint = new TransitPoint();
            transitPoint.setBusStopName(cursor.getString(0));
            transitPoint.setNumberOfRoutesBetweenTransitPointAndDestination(cursor.getInt(1));
            transitPoints.add(transitPoint);
        }

        cursor.close();
        return transitPoints;
    }

    private int getTotalDeparturesPerDay(String originBusStopName, String destinationBusStopName)
    {
        Cursor cursor = db.rawQuery("SELECT sum(Routes.RouteDeparturesPerDay)" +
                " FROM Routes JOIN ( SELECT sub1.RouteId, sub1.StopId," +
                " sub1.StopName, sub1.StopDirectionName, sub1.StopRouteOrder" +
                " as originBusStopRouteOrder, sub2.StopRouteOrder as" +
                " destinationBusStopRouteOrder FROM ( SELECT Stops.StopId," +
                " Stops.StopName, Stops.StopDirectionName, RouteStops.StopRouteOrder," +
                " RouteStops.RouteId FROM Stops JOIN RouteStops WHERE Stops.StopName" +
                " = '" + originBusStopName + "' AND Stops.StopId = RouteStops.StopId) sub1 JOIN" +
                " ( SELECT RouteStops.RouteId, RouteStops.StopRouteOrder FROM Stops" +
                " JOIN RouteStops WHERE Stops.StopName = '" + destinationBusStopName + "' AND" +
                " Stops.StopId = RouteStops.StopId) sub2 WHERE sub1.RouteId = sub2.RouteId" +
                " AND sub1.StopRouteOrder < sub2.StopRouteOrder) routesBetweenOriginAndTP" +
                " WHERE Routes.RouteId = routesBetweenOriginAndTP.RouteId order by Routes" +
                ".RouteDeparturesPerDay desc", null);

        int totalDeparturesPerDay = 0;

        if (cursor.moveToNext())
        {
            totalDeparturesPerDay = cursor.getInt(0);
        }

        cursor.close();
        return totalDeparturesPerDay;
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