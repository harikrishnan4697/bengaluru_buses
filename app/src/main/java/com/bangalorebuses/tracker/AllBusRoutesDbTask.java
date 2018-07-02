package com.bangalorebuses.tracker;

import android.database.Cursor;
import android.os.AsyncTask;

import java.util.ArrayList;

import static com.bangalorebuses.utils.Constants.db;

class AllBusRoutesDbTask extends AsyncTask<Void, Void, ArrayList<String>>
{
    private BusesDbQueriesHelper caller;

    AllBusRoutesDbTask(BusesDbQueriesHelper caller)
    {
        this.caller = caller;
    }

    @Override
    protected ArrayList<String> doInBackground(Void... params)
    {
        Cursor cursor = db.rawQuery("select distinct Routes.RouteNumber from Routes order" +
                " by Routes.RouteNumber asc", null);

        ArrayList<String> busRouteNumbers = new ArrayList<>();

        while (cursor.moveToNext())
        {
            String busRouteNumber = cursor.getString(0);

            if (!(busRouteNumber.contains("CS-") || busRouteNumber.contains("cs-")))
            {
                busRouteNumbers.add(busRouteNumber);
            }
        }

        cursor.close();

        return busRouteNumbers;
    }

    @Override
    protected void onPostExecute(ArrayList<String> busRouteNumbers)
    {
        super.onPostExecute(busRouteNumbers);

        if (!isCancelled())
        {
            caller.onAllBusRoutesFound(busRouteNumbers);
        }
    }
}
