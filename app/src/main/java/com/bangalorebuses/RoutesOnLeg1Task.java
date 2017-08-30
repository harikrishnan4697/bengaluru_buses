package com.bangalorebuses;

import android.os.AsyncTask;

import java.util.ArrayList;

class RoutesOnLeg1Task extends AsyncTask<Void, Void, ArrayList<IndirectTrip>>
{
    private IndirectTrip indirectTrip;
    private IndirectTripHelper caller;

    RoutesOnLeg1Task(IndirectTripHelper caller, IndirectTrip indirectTrip)
    {
        this.caller = caller;
        this.indirectTrip = indirectTrip;
    }

    @Override
    protected ArrayList<IndirectTrip> doInBackground(Void... params)
    {
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<IndirectTrip> indirectTrips)
    {
        super.onPostExecute(indirectTrips);
    }
}
