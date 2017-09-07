package com.bangalorebuses.busarrivals;

import android.database.Cursor;
import android.os.AsyncTask;

import com.bangalorebuses.core.BusRoute;
import com.bangalorebuses.utils.DbQueryHelper;

import java.util.ArrayList;

import static com.bangalorebuses.utils.Constants.db;

/**
 * This is a Db query task used to get a list of all the bus routes arriving
 * at a particular bus stop.
 *
 * @author Nihar Thakkar
 * @version 1.0
 * @since 5-9-2017
 */

public class BusRoutesArrivingAtBusStopDbTask extends AsyncTask<Integer, Void,
        ArrayList<BusRoute>>
{
    private DbQueryHelper dbQueryHelper;

    public BusRoutesArrivingAtBusStopDbTask(DbQueryHelper dbQueryHelper)
    {
        this.dbQueryHelper = dbQueryHelper;
    }

    @Override
    protected ArrayList<BusRoute> doInBackground(Integer... stopId)
    {
        // Query the database for a list of all the bus routes arriving at the stopId.
        Cursor cursor = db.rawQuery("select Routes.RouteId, Routes.RouteNumber, Routes.RouteServiceType," +
                " Routes.RouteDirection, Routes.RouteDirectionName, RouteStops.StopRouteOrder" +
                " from RouteStops join Routes where RouteStops.RouteId = Routes.RouteId and " +
                "RouteStops.StopId = " + stopId[0], null);

        ArrayList<BusRoute> busRoutes = new ArrayList<>();
        while (cursor.moveToNext())
        {
            // Put all the info about the bus routes into a BusRoute object.
            BusRoute route = new BusRoute();
            route.setBusRouteId(cursor.getInt(0));
            route.setBusRouteNumber(cursor.getString(1));
            route.setBusRouteServiceType(cursor.getString(2));
            route.setBusRouteDirection(cursor.getString(3));
            route.setBusRouteDirectionName(cursor.getString(4));
            route.setSelectedBusStopRouteOrder(cursor.getInt(5));

            // Add the BusRoute object to an ArrayList of BusRoutes.
            busRoutes.add(route);
        }
        cursor.close();
        return busRoutes;
    }

    @Override
    protected void onPostExecute(ArrayList<BusRoute> busRoutes)
    {
        super.onPostExecute(busRoutes);

        // Call the callback method so that the calling class can process the ArrayList.
        dbQueryHelper.onBusRoutesArrivingAtBusStopFound(busRoutes);
    }
}