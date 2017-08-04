package com.bangalorebuses;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import static com.bangalorebuses.Constants.DIRECTION_UP;

class DbQueryHelper
{
    public static Route getRouteDetails(SQLiteDatabase db, int routeId)
    {
        Route route = new Route();
        Cursor cursor = db.rawQuery("select Routes.RouteId, Routes.RouteNumber, Routes.RouteServiceType, Routes.RouteDirection," +
                " Routes.RouteDirectionName from Routes where Routes.RouteId = " + routeId, null);
        if (cursor.moveToNext())
        {
            route.setRouteNumber(cursor.getString(cursor.getColumnIndex(BengaluruBusesContract.Routes.COLUMN_ROUTE_NUMBER)));
            route.setServiceType(cursor.getString(cursor.getColumnIndex(BengaluruBusesContract.Routes.COLUMN_ROUTE_SERVICE_TYPE)));
            route.setDirection(cursor.getString(cursor.getColumnIndex(BengaluruBusesContract.Routes.COLUMN_ROUTE_DIRECTION)));
            if (cursor.getString(cursor.getColumnIndex(BengaluruBusesContract.Routes.COLUMN_ROUTE_DIRECTION)).equals(DIRECTION_UP))
            {
                route.setUpRouteId(cursor.getString(cursor.getColumnIndex(BengaluruBusesContract.Routes.COLUMN_ROUTE_ID)));
                route.setUpRouteName(cursor.getString(cursor.getColumnIndex(BengaluruBusesContract.Routes.COLUMN_ROUTE_DIRECTION_NAME)));
            }
            else
            {
                route.setDownRouteId(cursor.getString(cursor.getColumnIndex(BengaluruBusesContract.Routes.COLUMN_ROUTE_ID)));
                route.setDownRouteName(cursor.getString(cursor.getColumnIndex(BengaluruBusesContract.Routes.COLUMN_ROUTE_DIRECTION_NAME)));
            }
            cursor.close();
            return route;
        }
        else
        {
            cursor.close();
            return null;
        }
    }

    public static BusStop getStopDetails(SQLiteDatabase db, int stopId)
    {
        BusStop busStop = new BusStop();
        db.rawQuery("", null);
        return busStop;
    }
}
