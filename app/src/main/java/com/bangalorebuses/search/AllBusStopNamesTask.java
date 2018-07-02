package com.bangalorebuses.search;

import android.os.AsyncTask;

import com.bangalorebuses.utils.DbQueries;

import java.util.ArrayList;

import static com.bangalorebuses.utils.Constants.db;

class AllBusStopNamesTask extends AsyncTask<Void, Void, ArrayList<String>>
{
    private SearchDbQueriesHelper caller;

    AllBusStopNamesTask(SearchDbQueriesHelper caller)
    {
        this.caller = caller;
    }

    @Override
    protected ArrayList<String> doInBackground(Void... params)
    {
        ArrayList<String> busStopNames = new ArrayList<>();

        for (String busStopName : DbQueries.getAllDistinctStopNames(db))
        {
            if (!(busStopName.contains("CS-") || busStopName.contains("cs-")))
            {
                busStopNames.add(busStopName);
            }
        }

        return busStopNames;
    }

    @Override
    protected void onPostExecute(ArrayList<String> busStopNames)
    {
        super.onPostExecute(busStopNames);

        if (!isCancelled())
        {
            caller.onAllBusStopNamesFound(busStopNames);
        }
    }

}