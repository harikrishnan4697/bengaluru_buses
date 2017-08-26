package com.bangalorebuses;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static android.app.Activity.RESULT_OK;
import static com.bangalorebuses.Constants.SEARCH_END_BUS_STOP_REQUEST_CODE;
import static com.bangalorebuses.Constants.SEARCH_START_BUS_STOP_REQUEST_CODE;
import static com.bangalorebuses.Constants.SEARCH_TYPE_BUS_STOP;
import static com.bangalorebuses.Constants.db;

public class TripPlannerFragment extends Fragment implements TripHelper
{
    private Animation swapAnimation;
    private BusStop originBusStop = new BusStop();
    private BusStop destinationBusStop = new BusStop();
    private ImageView swapDirectionImageView;
    private Button searchOriginButton;
    private Button searchDestinationButton;
    private GetTripsBetweenStops getTripsBetweenStops;
    private GetTripDetails getTripDetails;
    private LinearLayout updatingLinearLayout;
    private RecyclerView recyclerView;
    private ArrayList<Trip> tripsToDisplay = new ArrayList<>();
    private ArrayList<Trip> tripsToQuery = new ArrayList<>();
    private int numberOfDirectTripQueriesMade = 0;
    private int numberOfDirectTripRouteBusesFound = 0;
    private TripsRecyclerViewAdapter recyclerViewAdapter;
    private LinearLayout errorLinearLayout;
    private ImageView errorImageView;
    private TextView errorTextView;
    private TextView errorResolutionTextView;
    private ArrayList<IndirectTrip> indirectTrips = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.trip_planner_fragment, container, false);

        swapAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_once);
        swapDirectionImageView = (ImageView) view.findViewById(R.id.swapDirectionImageView);
        swapDirectionImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                swapDirection();
            }
        });

        searchOriginButton = (Button) view.findViewById(R.id.trip_planner_origin_button);
        searchOriginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                searchOrigin();
            }
        });

        searchDestinationButton = (Button) view.findViewById(R.id.trip_planner_destination_button);
        searchDestinationButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                searchDestination();
            }
        });

        updateSearchButtonText();

        updatingLinearLayout = (LinearLayout) view.findViewById(R.id.updatingLinearLayout);
        updatingLinearLayout.setVisibility(View.GONE);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        if (recyclerViewAdapter != null)
        {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setAdapter(recyclerViewAdapter);
            recyclerView.setVisibility(View.VISIBLE);
        }
        else
        {
            recyclerView.setVisibility(View.GONE);
        }

        errorLinearLayout = (LinearLayout) view.findViewById(R.id.errorLinearLayout);
        errorImageView = (ImageView) view.findViewById(R.id.errorImageView);
        errorTextView = (TextView) view.findViewById(R.id.errorTextView);
        errorResolutionTextView = (TextView) view.findViewById(R.id.errorResolutionTextView);
        errorResolutionTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (originBusStop.getBusStopName() != null && destinationBusStop.getBusStopName() != null)
                {
                    findDirectTrips(originBusStop.getBusStopName(), destinationBusStop.getBusStopName());
                }
                else
                {
                    Toast.makeText(getContext(), "Please select a starting and ending bus stop!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        errorLinearLayout.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.trip_planner_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_refresh:
                if (originBusStop.getBusStopName() != null && destinationBusStop.getBusStopName() != null)
                {
                    findDirectTrips(originBusStop.getBusStopName(), destinationBusStop.getBusStopName());
                }
                else
                {
                    Toast.makeText(getContext(), "Please select a starting and ending bus stop!", Toast.LENGTH_SHORT).show();
                }
                break;
            /*case R.id.action_clear:
                clearAll();
                break;*/
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    /**
     * This method is used to check if the user's device
     * has a Wi-Fi or Cellular data connection.
     *
     * @return boolean This returns true or false based on the status
     * of the Wi-Fi and Cellular data connection.
     */
    private boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private void clearAll()
    {
        errorLinearLayout.setVisibility(View.GONE);
        updatingLinearLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        originBusStop = new BusStop();
        destinationBusStop = new BusStop();
        tripsToDisplay.clear();
        numberOfDirectTripQueriesMade = 0;
        numberOfDirectTripRouteBusesFound = 0;

        if (getTripsBetweenStops != null)
        {
            getTripsBetweenStops.cancel(true);
        }

        if (getTripDetails != null)
        {
            getTripDetails.cancel(true);
        }

        updateSearchButtonText();
    }

    private void searchOrigin()
    {
        Intent searchOriginIntent = new Intent(getContext(), SearchActivity.class);
        searchOriginIntent.putExtra("SEARCH_TYPE", SEARCH_TYPE_BUS_STOP);
        startActivityForResult(searchOriginIntent, SEARCH_START_BUS_STOP_REQUEST_CODE);
    }

    private void searchDestination()
    {
        Intent searchDestinationIntent = new Intent(getContext(), SearchActivity.class);
        searchDestinationIntent.putExtra("SEARCH_TYPE", SEARCH_TYPE_BUS_STOP);
        startActivityForResult(searchDestinationIntent, SEARCH_END_BUS_STOP_REQUEST_CODE);
    }

    private void swapDirection()
    {
        if (getTripsBetweenStops != null)
        {
            getTripsBetweenStops.cancel(true);
        }

        if (getTripDetails != null)
        {
            getTripDetails.cancel(true);
        }

        swapDirectionImageView.startAnimation(swapAnimation);

        BusStop tempDestinationBusStop = destinationBusStop;
        destinationBusStop = originBusStop;
        originBusStop = tempDestinationBusStop;

        updateSearchButtonText();

        if (originBusStop.getBusStopName() != null && destinationBusStop.getBusStopName() != null)
        {
            findDirectTrips(originBusStop.getBusStopName(), destinationBusStop.getBusStopName());
        }
    }

    private void updateSearchButtonText()
    {
        if (originBusStop != null && searchOriginButton != null)
        {
            searchOriginButton.setText(originBusStop.getBusStopName());
        }

        if (destinationBusStop != null && searchDestinationButton != null)
        {
            searchDestinationButton.setText(destinationBusStop.getBusStopName());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            if (requestCode == SEARCH_START_BUS_STOP_REQUEST_CODE)
            {
                originBusStop.setBusStopName(data.getStringExtra("BUS_STOP_NAME" ));
            }
            else if (requestCode == SEARCH_END_BUS_STOP_REQUEST_CODE)
            {
                destinationBusStop.setBusStopName(data.getStringExtra("BUS_STOP_NAME" ));
            }

            updateSearchButtonText();

            if (originBusStop.getBusStopName() != null && destinationBusStop.getBusStopName() != null)
            {
                findDirectTrips(originBusStop.getBusStopName(), destinationBusStop.getBusStopName());
            }
        }
    }

    private void findDirectTrips(String originBusStopName, String destinationBusStopName)
    {
        searchOriginButton.setEnabled(false);
        searchDestinationButton.setEnabled(false);
        swapDirectionImageView.setEnabled(false);

        if (getTripsBetweenStops != null)
        {
            getTripsBetweenStops.cancel(true);
        }

        if (getTripDetails != null)
        {
            getTripDetails.cancel(true);
        }

        errorLinearLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        updatingLinearLayout.setVisibility(View.VISIBLE);

        if (isNetworkAvailable())
        {
            getTripsBetweenStops = new GetTripsBetweenStops();
            getTripsBetweenStops.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                    originBusStopName, destinationBusStopName);
        }
        else
        {
            updatingLinearLayout.setVisibility(View.GONE);
            setErrorLayoutContent(R.drawable.ic_cloud_off_black, "Uh oh! No data connection.", "Retry" );
            errorLinearLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void onTripsFound(ArrayList<Trip> trips)
    {
        if (trips.size() != 0)
        {
            if (isNetworkAvailable())
            {
                getTripDetails = new GetTripDetails();
                getTripDetails.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, trips);
            }
            else
            {
                updatingLinearLayout.setVisibility(View.GONE);
                setErrorLayoutContent(R.drawable.ic_cloud_off_black, "Uh oh! No data connection.", "Retry" );
                errorLinearLayout.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        }
        else
        {
            Toast.makeText(getContext(), "No direct trips found!", Toast.LENGTH_SHORT).show();
            //findIndirectTrips(originBusStop.getBusStopName(), destinationBusStop.getBusStopName());
            /*swapDirectionImageView.setEnabled(true);
            searchOriginButton.setEnabled(true);
            searchDestinationButton.setEnabled(true);
            updatingLinearLayout.setVisibility(View.GONE);
            setErrorLayoutContent(R.drawable.ic_directions_black, "Uh oh! There aren't any direct trips from " +
                    originBusStop.getBusStopName() + " to " + destinationBusStop.getBusStopName(), "Retry");
            errorLinearLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);*/
        }
    }

    private void onNumberOfRoutesBetweenBusStopsFound(ArrayList<TransitPoint> transitPoints)
    {
        Collections.sort(transitPoints, new Comparator<TransitPoint>()
        {
            @Override
            public int compare(TransitPoint o1, TransitPoint o2)
            {
                return o2.getNumberOfRoutesBetweenOriginAndTransitPoint() - o1.getNumberOfRoutesBetweenOriginAndTransitPoint();
            }
        });

        transitPoints.size();
    }

    private void onTripDetailsFound(ArrayList<Trip> trips)
    {
        numberOfDirectTripQueriesMade = 0;
        numberOfDirectTripRouteBusesFound = 0;
        tripsToDisplay.clear();
        tripsToQuery = trips;

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerViewAdapter = new TripsRecyclerViewAdapter(tripsToDisplay);
        recyclerView.setAdapter(recyclerViewAdapter);

        if (isNetworkAvailable())
        {
            for (; numberOfDirectTripQueriesMade < 10; numberOfDirectTripQueriesMade++)
            {
                if (numberOfDirectTripQueriesMade < tripsToQuery.size())
                {
                    tripsToQuery.get(numberOfDirectTripQueriesMade).getBusesOnFirstBusRoute(this, getContext());
                }
                else
                {
                    break;
                }
            }
        }
        else
        {
            swapDirectionImageView.setEnabled(true);
            searchOriginButton.setEnabled(true);
            searchDestinationButton.setEnabled(true);
            updatingLinearLayout.setVisibility(View.GONE);
            setErrorLayoutContent(R.drawable.ic_cloud_off_black, "Uh oh! No data connection.", "Retry" );
            errorLinearLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void findIndirectTrips(String originBusStopName, String destinationBusStopName)
    {
        Log.i("Test", "Getting transit points..." );
        new GetTransitPoints().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, originBusStopName, destinationBusStopName);
    }

    private void onTransitPointsFound(ArrayList<IndirectTrip> indirectTrips)
    {
        Log.i("Test", "Found transit points!" );
        Toast.makeText(getContext(), "Found transit points! Getting scores...", Toast.LENGTH_SHORT).show();
        new CalculateScoresForIndirectTripsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, indirectTrips);
    }

    private void onIndirectTripScoresFound(ArrayList<IndirectTrip> indirectTrips)
    {
        Collections.sort(indirectTrips, new Comparator<IndirectTrip>()
        {
            @Override
            public int compare(IndirectTrip o1, IndirectTrip o2)
            {
                return o2.getScore() - o1.getScore();
            }
        });

        this.indirectTrips.clear();
        for (int i = 0; i < 5; i++)
        {
            if (i < indirectTrips.size())
            {
                this.indirectTrips.add(indirectTrips.get(i));
            }
        }
        Toast.makeText(getContext(), "Found the top 5 transit points successfully!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBusesInServiceFound(String errorMessage, Trip trip)
    {
        numberOfDirectTripRouteBusesFound++;

        if (trip.getBusRoutes().get(0).getBusRouteBuses().size() > 0)
        {
            tripsToDisplay.add(trip);
        }

        Collections.sort(tripsToDisplay, new Comparator<Trip>()
        {
            @Override
            public int compare(Trip o1, Trip o2)
            {
                return o1.getBusRoutes().get(0).getShortestOriginToDestinationTravelTime() -
                        o2.getBusRoutes().get(0).getShortestOriginToDestinationTravelTime();
            }
        });

        recyclerViewAdapter.notifyDataSetChanged();
        recyclerView.setVisibility(View.VISIBLE);

        if (numberOfDirectTripQueriesMade < tripsToQuery.size())
        {
            tripsToQuery.get(numberOfDirectTripQueriesMade).getBusesOnFirstBusRoute(this, getContext());
            numberOfDirectTripQueriesMade++;
        }

        if (numberOfDirectTripRouteBusesFound == numberOfDirectTripQueriesMade)
        {
            updatingLinearLayout.setVisibility(View.GONE);
            swapDirectionImageView.setEnabled(true);
            searchOriginButton.setEnabled(true);
            searchDestinationButton.setEnabled(true);

            if (tripsToDisplay.size() == 0)
            {
                setErrorLayoutContent(R.drawable.ic_directions_black, "Whoops! There don't seem to be any direct trips right now...", "Retry" );
                errorLinearLayout.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        }
    }

    private void setErrorLayoutContent(int drawableResId, String errorMessage, String resolutionButtonText)
    {
        errorImageView.setImageResource(drawableResId);
        errorTextView.setText(errorMessage);
        errorResolutionTextView.setText(resolutionButtonText);
    }

    private class GetTripsBetweenStops extends AsyncTask<String, Void, ArrayList<Trip>>
    {
        @Override
        protected ArrayList<Trip> doInBackground(String... busStopNames)
        {
            return DbQueries.getDirectTripsBetweenStops(db, busStopNames[0], busStopNames[1]);
        }

        @Override
        protected void onPostExecute(ArrayList<Trip> trips)
        {
            super.onPostExecute(trips);
            onTripsFound(trips);
        }
    }

    private class GetTripDetails extends AsyncTask<ArrayList<Trip>, Void, ArrayList<Trip>>
    {
        @Override
        protected ArrayList<Trip> doInBackground(ArrayList<Trip>... trips)
        {
            for (Trip trip : trips[0])
            {
                trip.setOriginBusStop(DbQueries.getStopDetails(db, trip.getOriginBusStop().getBusStopId()));

                for (int i = 0; i < trip.getBusRoutes().size(); i++)
                {
                    BusStop routeOriginBusStop = DbQueries.getStopDetails(db, trip.getBusRoutes()
                            .get(i).getTripPlannerOriginBusStop().getBusStopId());

                    if (routeOriginBusStop != null)
                    {
                        routeOriginBusStop.setBusStopRouteOrder(DbQueries.getStopRouteOrder(db,
                                trip.getBusRoutes().get(i).getBusRouteId(),
                                trip.getBusRoutes().get(i).getTripPlannerOriginBusStop().getBusStopId()));
                    }

                    BusStop routeDestinationBusStop = DbQueries.getStopDetails(db, trip.getBusRoutes().get(i)
                            .getTripPlannerDestinationBusStop().getBusStopId());

                    if (routeDestinationBusStop != null)
                    {
                        routeDestinationBusStop.setBusStopRouteOrder(DbQueries.getStopRouteOrder(db,
                                trip.getBusRoutes().get(i).getBusRouteId(),
                                trip.getBusRoutes().get(i).getTripPlannerDestinationBusStop().getBusStopId()));
                    }

                    trip.getBusRoutes().set(i, (DbQueries.getRouteDetails(db,
                            trip.getBusRoutes().get(i).getBusRouteId())));

                    trip.getBusRoutes().get(i).setTripPlannerOriginBusStop(routeOriginBusStop);
                    trip.getBusRoutes().get(i).setTripPlannerDestinationBusStop(routeDestinationBusStop);
                }
            }

            return trips[0];
        }

        @Override
        protected void onPostExecute(ArrayList<Trip> trips)
        {
            super.onPostExecute(trips);
            onTripDetailsFound(trips);
        }
    }

    private class GetTransitPoints extends AsyncTask<String, Void, ArrayList<IndirectTrip>>
    {
        @Override
        protected ArrayList<IndirectTrip> doInBackground(String... busStopNames)
        {
            return DbQueries.getTransitPoints(db, busStopNames[0], busStopNames[1]);
        }

        @Override
        protected void onPostExecute(ArrayList<IndirectTrip> indirectTrips)
        {
            super.onPostExecute(indirectTrips);
            onTransitPointsFound(indirectTrips);
        }
    }

    private class GetNumberOfRoutesBetweenBusStops extends AsyncTask<Void, Void, Void>
    {
        private String destinationBusStopName;
        private ArrayList<TransitPoint> transitPoints;

        GetNumberOfRoutesBetweenBusStops(ArrayList<TransitPoint> transitPoints, String destinationBusStopName)
        {
            this.destinationBusStopName = destinationBusStopName;
            this.transitPoints = transitPoints;
        }

        @Override
        protected Void doInBackground(Void... voids)
        {
            for (TransitPoint transitPoint : transitPoints)
            {
                transitPoint.setNumberOfRoutesBetweenOriginAndTransitPoint(DbQueries.getNumberOfRoutesBetweenStops(db,
                        transitPoint.getTransitPointName(), destinationBusStopName));
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void params)
        {
            super.onPostExecute(params);
            onNumberOfRoutesBetweenBusStopsFound(transitPoints);
        }
    }

    private class CalculateScoresForIndirectTripsTask extends AsyncTask<ArrayList<IndirectTrip>, Void, ArrayList<IndirectTrip>>
    {
        @Override
        protected ArrayList<IndirectTrip> doInBackground(ArrayList<IndirectTrip>... params)
        {
            for (IndirectTrip indirectTrip : params[0])
            {
                int numberScheduledRoutesInLeg1 = DbQueries.getNumberOfScheduledRoutes(db, originBusStop.getBusStopName(),
                        indirectTrip.getTransitPoint().getTransitPointName());

                int numberScheduledRoutesInLeg2 = DbQueries.getNumberOfScheduledRoutes(db, indirectTrip.getTransitPoint()
                        .getTransitPointName(), destinationBusStop.getBusStopName());

                if (numberScheduledRoutesInLeg1 < numberScheduledRoutesInLeg2)
                {
                    indirectTrip.setScore(numberScheduledRoutesInLeg1);
                }
                else
                {
                    indirectTrip.setScore(numberScheduledRoutesInLeg2);
                }
            }

            return params[0];
        }

        @Override
        protected void onPostExecute(ArrayList<IndirectTrip> indirectTrips)
        {
            super.onPostExecute(indirectTrips);
            onIndirectTripScoresFound(indirectTrips);
        }
    }
}