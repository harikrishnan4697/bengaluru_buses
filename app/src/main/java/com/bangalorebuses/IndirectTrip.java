package com.bangalorebuses;

import android.os.AsyncTask;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.bangalorebuses.Constants.db;

class IndirectTrip extends Trip
{
    private TransitPoint transitPoint;
    private int numberOfBusStopsBetweenOriginAndDestination;
    private int score;

    @Override
    public void showTrip(TripsRecyclerViewAdapter.TripsViewHolder holder)
    {

    }

    public TransitPoint getTransitPoint()
    {
        return transitPoint;
    }

    public void setTransitPoint(TransitPoint transitPoint)
    {
        this.transitPoint = transitPoint;
    }

    public int getNumberOfBusStopsBetweenOriginAndDestination()
    {
        return numberOfBusStopsBetweenOriginAndDestination;
    }

    public void setNumberOfBusStopsBetweenOriginAndDestination(int numberOfBusStopsBetweenOriginAndDestination)
    {
        this.numberOfBusStopsBetweenOriginAndDestination = numberOfBusStopsBetweenOriginAndDestination;
    }

    public int getScore()
    {
        return score;
    }

    public void setScore(int score)
    {
        this.score = score;
    }

    private void onRoutesBetweenBusStopFound(ArrayList<BusRoute> busRoutes)
    {

    }

    class GetRoutesBetweenBusStops extends AsyncTask<Void, Void, ArrayList<BusRoute>>
    {
        private String originBusStopName;
        private String destinationBusStopName;

        GetRoutesBetweenBusStops(String originBusStopName, String destinationBusStopName)
        {
            this.originBusStopName = originBusStopName;
            this.destinationBusStopName = destinationBusStopName;
        }

        @Override
        protected ArrayList<BusRoute> doInBackground(Void... params)
        {
            ArrayList<BusRoute> busRoutes = DbQueries.getRoutesBetweenStops(db, originBusStopName, destinationBusStopName);

            for (int i = 0; i < busRoutes.size(); i++)
            {
                // Get all details about the route
                busRoutes.set(i, DbQueries.getRouteDetails(db, busRoutes.get(i).getBusRouteId()));

                // Get all bus stops on the route
                busRoutes.get(i).setBusRouteStops(DbQueries.getStopsOnRoute(db, busRoutes.get(i).getBusRouteId()));

                // Get all the route's departure timings
                ArrayList<String> busRouteDepartureTimings = DbQueries.getRouteDepartureTimings
                        (db, busRoutes.get(i).getBusRouteId());

                ArrayList<Date> busRouteDepartureTimingsAsDates = new ArrayList<>();

                for (String busRouteDepartureTiming : busRouteDepartureTimings)
                {
                    DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());

                    try
                    {
                        Date date = dateFormat.parse(busRouteDepartureTiming);
                        busRouteDepartureTimingsAsDates.add(date);
                    }
                    catch (ParseException e)
                    {
                        // TODO fatal error occurred
                    }
                }

                busRoutes.get(i).setBusRouteDepartureTimings(busRouteDepartureTimingsAsDates);

                // Get all origin bus stop details
                busRoutes.get(i).setTripPlannerOriginBusStop(DbQueries.getStopDetails
                        (db, busRoutes.get(i).getTripPlannerOriginBusStop().getBusStopId()));

                // get all destination bus stop details
                busRoutes.get(i).setTripPlannerDestinationBusStop(DbQueries.getStopDetails
                        (db, busRoutes.get(i).getTripPlannerDestinationBusStop().getBusStopId()));
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<BusRoute> busRoutes)
        {
            super.onPostExecute(busRoutes);
            onRoutesBetweenBusStopFound(busRoutes);
        }
    }
}