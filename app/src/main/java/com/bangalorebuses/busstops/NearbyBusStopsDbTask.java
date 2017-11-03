package com.bangalorebuses.busstops;

import android.database.Cursor;
import android.graphics.PointF;
import android.os.AsyncTask;

import com.bangalorebuses.core.BusStop;
import com.bangalorebuses.utils.Constants;

import java.util.ArrayList;

import static com.bangalorebuses.utils.Constants.db;

class NearbyBusStopsDbTask extends AsyncTask<Void, Void, ArrayList<BusStop>>
{
    private NearbyBusStopsDbHelper caller;
    private float currentLat;
    private float currentLong;
    private float radius;

    NearbyBusStopsDbTask(NearbyBusStopsDbHelper caller, float currentLat,
                         float currentLong, float radius)
    {
        this.caller = caller;
        this.currentLat = currentLat;
        this.currentLong = currentLong;
        this.radius = radius;
    }

    /**
     * Calculates the end-point from a given source at a given range (meters)
     * and bearing (degrees). This methods uses simple geometry equations to
     * calculate the end-point.
     *
     * @param point   Point of origin
     * @param range   Range in meters
     * @param bearing Bearing in degrees
     * @return End-point from the source given the desired range and bearing.
     */
    private static PointF calculateDerivedPosition(PointF point,
                                                   double range, double bearing)
    {
        double EarthRadius = 6371000; // m

        double latA = Math.toRadians(point.x);
        double lonA = Math.toRadians(point.y);
        double angularDistance = range / EarthRadius;
        double trueCourse = Math.toRadians(bearing);

        double lat = Math.asin(
                Math.sin(latA) * Math.cos(angularDistance) +
                        Math.cos(latA) * Math.sin(angularDistance)
                                * Math.cos(trueCourse));

        double dlon = Math.atan2(
                Math.sin(trueCourse) * Math.sin(angularDistance)
                        * Math.cos(latA),
                Math.cos(angularDistance) - Math.sin(latA) * Math.sin(lat));

        double lon = ((lonA + dlon + Math.PI) % (Math.PI * 2)) - Math.PI;

        lat = Math.toDegrees(lat);
        lon = Math.toDegrees(lon);

        return new PointF((float) lat, (float) lon);

    }

    private static boolean pointIsInCircle(PointF pointForCheck, PointF center,
                                           double radius)
    {
        return getDistanceBetweenTwoPoints(pointForCheck, center) <= radius;
    }

    private static double getDistanceBetweenTwoPoints(PointF p1, PointF p2)
    {
        double R = 6371000; // m
        double dLat = Math.toRadians(p2.x - p1.x);
        double dLon = Math.toRadians(p2.y - p1.y);
        double lat1 = Math.toRadians(p1.x);
        double lat2 = Math.toRadians(p2.x);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2)
                * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c;

        return d;
    }

    @Override
    protected ArrayList<BusStop> doInBackground(Void... voids)
    {
        PointF center = new PointF(currentLat, currentLong);
        final double mult = 1; // mult = 1.1; is more reliable
        PointF p1 = calculateDerivedPosition(center, mult * radius, 0);
        PointF p2 = calculateDerivedPosition(center, mult * radius, 90);
        PointF p3 = calculateDerivedPosition(center, mult * radius, 180);
        PointF p4 = calculateDerivedPosition(center, mult * radius, 270);

        Cursor cursor = db.rawQuery("select Stops.StopId, Stops.StopName, Stops.StopDirectionName," +
                " Stops.StopLat, Stops.StopLong from Stops where Stops.StopLat > " + String.valueOf(p3.x) +
                " and Stops.StopLat < " + String.valueOf(p1.x) + " and Stops.StopLong < " + String.valueOf(p2.y) +
                " and Stops.StopLong > " + String.valueOf(p4.y) + " limit 10", null);

        ArrayList<BusStop> nearbyBusStops = new ArrayList<>();
        while (cursor.moveToNext())
        {
            BusStop busStop = new BusStop();
            busStop.setBusStopId(cursor.getInt(0));
            busStop.setBusStopName(cursor.getString(1));
            busStop.setBusStopDirectionName(cursor.getString(2));
            busStop.setBusStopLat(cursor.getFloat(3));
            busStop.setBusStopLong(cursor.getFloat(4));
            nearbyBusStops.add(busStop);
        }

        ArrayList<BusStop> verifiedNearbyBusStops = new ArrayList<>();

        for (BusStop busStop : nearbyBusStops)
        {
            PointF pointF = new PointF(busStop.getBusStopLat(),
                    busStop.getBusStopLong());

            if (pointIsInCircle(pointF, center, Constants.NEARBY_BUS_STOPS_RANGE))
            {
                verifiedNearbyBusStops.add(busStop);
            }
        }

        cursor.close();
        return verifiedNearbyBusStops;
    }

    @Override
    protected void onPostExecute(ArrayList<BusStop> nearbyBusStops)
    {
        super.onPostExecute(nearbyBusStops);
        caller.onNearbyBusStopsFound(nearbyBusStops);
    }
}