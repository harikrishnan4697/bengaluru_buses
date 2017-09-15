package com.bangalorebuses.busstops;

import android.database.Cursor;
import android.os.AsyncTask;

import com.bangalorebuses.core.BusStop;

import java.util.ArrayList;

import static com.bangalorebuses.utils.Constants.db;

class AllBusStopsDbTask extends AsyncTask<Void, Void, ArrayList<BusStop>>
{
    private BusStopsDbQueriesHelper caller;

    AllBusStopsDbTask(BusStopsDbQueriesHelper caller)
    {
        this.caller = caller;
    }

    @Override
    protected ArrayList<BusStop> doInBackground(Void... params)
    {
        Cursor cursor = db.rawQuery("select Stops.StopId, Stops.StopName," +
                " Stops.StopDirectionName from Stops order by Stops.StopName" +
                " asc", null);

        ArrayList<BusStop> busStops = new ArrayList<>();

        while (cursor.moveToNext())
        {
            BusStop busStop = new BusStop();
            busStop.setBusStopId(cursor.getInt(0));
            busStop.setBusStopName(cursor.getString(1));
            busStop.setBusStopDirectionName(cursor.getString(2));

            busStops.add(busStop);
        }

        cursor.close();

        return busStops;
    }

    @Override
    protected void onPostExecute(ArrayList<BusStop> busStops)
    {
        super.onPostExecute(busStops);

        if (!isCancelled())
        {
            caller.onAllBusStopsFound(busStops);
        }
    }
}