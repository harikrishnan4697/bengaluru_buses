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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import static android.app.Activity.RESULT_OK;
import static com.bangalorebuses.Constants.NETWORK_QUERY_NO_ERROR;
import static com.bangalorebuses.Constants.NUMBER_OF_ROUTES_TYPE_ORIGIN_TO_TRANSIT_POINT;
import static com.bangalorebuses.Constants.NUMBER_OF_ROUTES_TYPE_TRANSIT_POINT_TO_DESTINATION;
import static com.bangalorebuses.Constants.SEARCH_END_BUS_STOP_REQUEST_CODE;
import static com.bangalorebuses.Constants.SEARCH_START_BUS_STOP_REQUEST_CODE;
import static com.bangalorebuses.Constants.SEARCH_TYPE_BUS_STOP;
import static com.bangalorebuses.Constants.db;

public class TripPlannerFragment extends Fragment implements DirectTripHelper, IndirectTripHelper
{
    private Animation swapAnimation;
    private ImageView swapDirectionImageView;

    private BusStop originBusStop = new BusStop();
    private BusStop destinationBusStop = new BusStop();

    private Button searchOriginButton;
    private Button searchDestinationButton;

    private LinearLayout updatingLinearLayout;

    private RecyclerView recyclerView;
    private TripsRecyclerViewAdapter recyclerViewAdapter;

    private ArrayList<Trip> tripsToDisplay = new ArrayList<>();
    private ArrayList<Trip> tripsToQuery = new ArrayList<>();
    private ArrayList<GetBusesEnDirectTripTask> getBusesEnDirectTripTasks = new ArrayList<>();
    private GetDirectTripsBetweenStops getDirectTripsBetweenStops;
    private int numberOfDirectTripQueriesMade = 0;
    private int numberOfDirectTripRouteBusesFound = 0;
    private boolean wasGettingDirectTrips = false;

    private ArrayList<TransitPoint> transitPoints = new ArrayList<>();

    private LinearLayout errorLinearLayout;
    private ImageView errorImageView;
    private TextView errorTextView;
    private TextView errorResolutionTextView;

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

        if (!wasGettingDirectTrips)
        {
            updateSearchButtonText();
        }
        else
        {
            originBusStop = new BusStop();
            destinationBusStop = new BusStop();
        }

        updatingLinearLayout = (LinearLayout) view.findViewById(R.id.updatingLinearLayout);
        updatingLinearLayout.setVisibility(View.GONE);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        if (recyclerViewAdapter != null && !wasGettingDirectTrips)
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
        if (getActivity() != null)
        {
            ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null;
        }
        return false;
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
                originBusStop.setBusStopName(data.getStringExtra("BUS_STOP_NAME"));
            }
            else if (requestCode == SEARCH_END_BUS_STOP_REQUEST_CODE)
            {
                destinationBusStop.setBusStopName(data.getStringExtra("BUS_STOP_NAME"));
            }

            updateSearchButtonText();

            if (originBusStop.getBusStopName() != null && destinationBusStop.getBusStopName() != null)
            {
                findDirectTrips(originBusStop.getBusStopName(), destinationBusStop.getBusStopName());
            }
        }
    }

    private void cancelAllPreviousTasks()
    {
        // Iterate over all the previous GetBusesEnDirectTripTasks to cancel each one of them
        for (GetBusesEnDirectTripTask task : getBusesEnDirectTripTasks)
        {
            if (task != null)
            {
                task.cancel(true);
            }
        }

        if (getDirectTripsBetweenStops != null)
        {
            getDirectTripsBetweenStops.cancel(true);
        }
    }

    // Everything to do with direct trips
    private void findDirectTrips(String originBusStopName, String destinationBusStopName)
    {
        cancelAllPreviousTasks();

        errorLinearLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        updatingLinearLayout.setVisibility(View.VISIBLE);

        getDirectTripsBetweenStops = new GetDirectTripsBetweenStops();
        getDirectTripsBetweenStops.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                originBusStopName, destinationBusStopName);
    }

    private void onDirectTripsFound(ArrayList<Trip> trips)
    {
        getDirectTripsBetweenStops = null;

        if (trips.size() != 0)
        {
            numberOfDirectTripQueriesMade = 0;
            numberOfDirectTripRouteBusesFound = 0;
            tripsToDisplay.clear();
            tripsToQuery = trips;
            getBusesEnDirectTripTasks.clear();

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
                        wasGettingDirectTrips = true;
                        GetBusesEnDirectTripTask task = new GetBusesEnDirectTripTask(this, ((DirectTrip) tripsToQuery.get(
                                numberOfDirectTripQueriesMade)));
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        getBusesEnDirectTripTasks.add(task);
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
                setErrorLayoutContent(R.drawable.ic_cloud_off_black, "Uh oh! No data connection.", "Retry");
                errorLinearLayout.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        }
        else
        {
            Toast.makeText(getContext(), "There aren't any direct trips from " +
                    originBusStop.getBusStopName() + " to " + destinationBusStop.getBusStopName() +
                    " ! Getting indirect trips...", Toast.LENGTH_SHORT).show();

            findIndirectTrips(originBusStop.getBusStopName(), destinationBusStop.getBusStopName());
        }
    }

    @Override
    public void onBusesInServiceFound(String errorMessage, DirectTrip directTrip)
    {
        numberOfDirectTripRouteBusesFound++;

        if (errorMessage.equals(NETWORK_QUERY_NO_ERROR))
        {
            if (directTrip.getBusRoute().getBusRouteBuses().size() != 0)
            {
                for (Bus bus : directTrip.getBusRoute().getBusRouteBuses())
                {
                    bus.setBusETA(calculateTravelTime(DbQueries.getNumberOfStopsBetweenRouteOrders
                                    (db, directTrip.getBusRoute().getBusRouteId(), bus.getBusRouteOrder(),
                                            directTrip.getBusRoute().getTripPlannerOriginBusStop().getBusStopRouteOrder()),
                            directTrip.getBusRoute().getBusRouteNumber()));
                }

                Collections.sort(directTrip.getBusRoute().getBusRouteBuses(), new Comparator<Bus>()
                {
                    @Override
                    public int compare(Bus o1, Bus o2)
                    {
                        return o1.getBusETA() - o2.getBusETA();
                    }
                });

                directTrip.getBusRoute().setShortestOriginToDestinationTravelTime(directTrip.getBusRoute()
                        .getBusRouteBuses().get(0).getBusETA() + calculateTravelTime(DbQueries
                                .getNumberOfStopsBetweenRouteOrders(db, directTrip.getBusRoute().getBusRouteId(),
                                        directTrip.getBusRoute().getTripPlannerOriginBusStop().getBusStopRouteOrder(),
                                        directTrip.getBusRoute().getTripPlannerDestinationBusStop().getBusStopRouteOrder()),
                        directTrip.getBusRoute().getBusRouteNumber()));

                tripsToDisplay.add(directTrip);
                Collections.sort(tripsToDisplay, new Comparator<Trip>()
                {
                    @Override
                    public int compare(Trip o1, Trip o2)
                    {
                        return ((DirectTrip) o1).getBusRoute().getShortestOriginToDestinationTravelTime() -
                                ((DirectTrip) o2).getBusRoute().getShortestOriginToDestinationTravelTime();
                    }
                });

                recyclerViewAdapter.notifyDataSetChanged();
                recyclerView.setVisibility(View.VISIBLE);
            }
        }

        synchronized (this)
        {
            if (numberOfDirectTripQueriesMade < tripsToQuery.size())
            {
                wasGettingDirectTrips = true;
                GetBusesEnDirectTripTask task = new GetBusesEnDirectTripTask(this, ((DirectTrip)
                        tripsToQuery.get(numberOfDirectTripQueriesMade)));
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                getBusesEnDirectTripTasks.add(task);
                numberOfDirectTripQueriesMade++;
            }
        }

        if (numberOfDirectTripRouteBusesFound == numberOfDirectTripQueriesMade)
        {
            wasGettingDirectTrips = false;
            updatingLinearLayout.setVisibility(View.GONE);

            if (tripsToDisplay.size() == 0)
            {
                findIndirectTrips(originBusStop.getBusStopName(), destinationBusStop.getBusStopName());
                Toast.makeText(getContext(), "There aren't any direct trips from " +
                        originBusStop.getBusStopName() + " to " + destinationBusStop.getBusStopName() +
                        " ! Getting indirect trips...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private int calculateTravelTime(int numberOfBusStopsToTravel, String routeNumber)
    {
        Calendar calendar = Calendar.getInstance();
        int travelTime;

        if ((calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) || (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY))
        {
            // Check if the bus is an airport shuttle (airport shuttles take longer to travel
            // from one bus stop to another as they don't stop at all bus stops)
            if (routeNumber.contains("KIAS-"))
            {
                travelTime = numberOfBusStopsToTravel * 4;  // 4 Minutes to get from a bus stop to another for the airport shuttle weekends
            }
            else
            {
                travelTime = numberOfBusStopsToTravel * 2;  // 2 Minutes to get from a bus stop to another for other buses during weekends
            }
        }
        else if ((calendar.get(Calendar.HOUR_OF_DAY) > 7 && calendar.get(Calendar.HOUR_OF_DAY) < 11) || (calendar.get(Calendar.HOUR_OF_DAY) > 16 && calendar.get(Calendar.HOUR_OF_DAY) < 21))
        {
            // Check if the bus is an airport shuttle (airport shuttles take longer to travel
            // from one bus stop to another as they don't stop at all bus stops)
            if (routeNumber.contains("KIAS-"))
            {
                travelTime = numberOfBusStopsToTravel * 5;  // 5 Minutes to get from a bus stop to another for the airport shuttle in peak-time
            }
            else
            {
                travelTime = numberOfBusStopsToTravel * 3;  // 3 Minutes to get from a bus stop to another for other buses in peak-time
            }
        }
        else
        {
            // Check if the bus is an airport shuttle (airport shuttles take longer to travel
            // from one bus stop to another as they don't stop at all bus stops)
            if (routeNumber.contains("KIAS-"))
            {
                travelTime = numberOfBusStopsToTravel * 4;  // 4 Minutes to get from a bus stop to another for the airport shuttle
            }
            else
            {
                travelTime = (int) (numberOfBusStopsToTravel * 2.5);  // 2.5 Minutes to get from a bus stop to another for other buses
            }
        }

        return travelTime;
    }

    // Everything to do with indirect trips
    private void findIndirectTrips(String originBusStopName, String destinationBusStopName)
    {
        errorLinearLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        updatingLinearLayout.setVisibility(View.VISIBLE);
        transitPoints.clear();

        if (isNetworkAvailable())
        {
            new TransitPointsWithNumberOfRoutesTask(this, originBusStopName, destinationBusStopName, NUMBER_OF_ROUTES_TYPE_ORIGIN_TO_TRANSIT_POINT)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            new TransitPointsWithNumberOfRoutesTask(this, originBusStopName, destinationBusStopName, NUMBER_OF_ROUTES_TYPE_TRANSIT_POINT_TO_DESTINATION)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else
        {
            updatingLinearLayout.setVisibility(View.GONE);
            setErrorLayoutContent(R.drawable.ic_cloud_off_black, "Uh oh! No data connection.", "Retry");
            errorLinearLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onTransitPointsAndRouteCountOriginToTPFound(ArrayList<TransitPoint> transitPoints)
    {
        // Make sure that there are transit points.
        if (transitPoints.size() == 0)
        {
            updatingLinearLayout.setVisibility(View.GONE);
            Toast.makeText(getContext(), "There aren't are direct or indirect trips...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (this.transitPoints.size() == 0)
        {
            this.transitPoints = transitPoints;
        }
        else
        {
            for (TransitPoint transitPoint : this.transitPoints)
            {
                for (TransitPoint tp : transitPoints)
                {
                    if (transitPoint.getTransitPointName().equals(tp.getTransitPointName()))
                    {
                        transitPoint.setNumberOfRoutesBetweenOriginAndTransitPoint(
                                tp.getNumberOfRoutesBetweenOriginAndTransitPoint());
                    }
                }
            }
        }

        if (this.transitPoints.get(0).getNumberOfRoutesBetweenOriginAndTransitPoint() != 0 &&
                this.transitPoints.get(0).getNumberOfRoutesBetweenTransitPointAndDestination() != 0)
        {
            onTransitPointsFound();
        }
    }

    @Override
    public void onTransitPointsAndRouteCountTPToDestFound(ArrayList<TransitPoint> transitPoints)
    {
        // Make sure that there are transit points.
        if (transitPoints.size() == 0)
        {
            updatingLinearLayout.setVisibility(View.GONE);
            Toast.makeText(getContext(), "There aren't are direct or indirect trips...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (this.transitPoints.size() == 0)
        {
            this.transitPoints = transitPoints;
        }
        else
        {
            for (TransitPoint transitPoint : this.transitPoints)
            {
                for (TransitPoint tp : transitPoints)
                {
                    if (transitPoint.getTransitPointName().equals(tp.getTransitPointName()))
                    {
                        transitPoint.setNumberOfRoutesBetweenTransitPointAndDestination(
                                tp.getNumberOfRoutesBetweenTransitPointAndDestination());
                    }
                }
            }
        }

        if (this.transitPoints.get(0).getNumberOfRoutesBetweenOriginAndTransitPoint() != 0 &&
                this.transitPoints.get(0).getNumberOfRoutesBetweenTransitPointAndDestination() != 0)
        {
            onTransitPointsFound();
        }
    }

    private void onTransitPointsFound()
    {
        for (TransitPoint transitPoint : this.transitPoints)
        {
            if (transitPoint.getNumberOfRoutesBetweenOriginAndTransitPoint() <
                    transitPoint.getNumberOfRoutesBetweenTransitPointAndDestination())
            {
                transitPoint.setTransitPointScore(transitPoint.getNumberOfRoutesBetweenOriginAndTransitPoint());
            }
            else
            {
                transitPoint.setTransitPointScore(transitPoint.getNumberOfRoutesBetweenTransitPointAndDestination());
            }
        }

        Collections.sort(this.transitPoints, new Comparator<TransitPoint>()
        {
            @Override
            public int compare(TransitPoint o1, TransitPoint o2)
            {
                return o2.getTransitPointScore() - o1.getTransitPointScore();
            }
        });

        ArrayList<TransitPoint> tempTransitPoints = new ArrayList<>();

        for (int i = 0; i < 5; i++)
        {
            if (i < this.transitPoints.size())
            {
                tempTransitPoints.add(this.transitPoints.get(i));
            }
        }

        this.transitPoints = tempTransitPoints;

        for (TransitPoint transitPoint : this.transitPoints)
        {
            new BusRoutesToAndFromTransitPointTask(this, originBusStop.getBusStopName(), transitPoint, destinationBusStop.getBusStopName())
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onRoutesToAndFromTransitPointFound(TransitPoint transitPoint)
    {
        transitPoint.getTransitPointName();
    }

    @Override
    public void onBusesInServiceFound(String errorMessage, TransitPoint transitPoint)
    {

    }

    // Other stuff
    private void setErrorLayoutContent(int drawableResId, String errorMessage, String resolutionButtonText)
    {
        errorImageView.setImageResource(drawableResId);
        errorTextView.setText(errorMessage);
        errorResolutionTextView.setText(resolutionButtonText);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        for (GetBusesEnDirectTripTask task : getBusesEnDirectTripTasks)
        {
            if (task != null)
            {
                task.cancel(true);
            }
        }
    }

    private class GetDirectTripsBetweenStops extends AsyncTask<String, Void, ArrayList<Trip>>
    {
        @Override
        protected ArrayList<Trip> doInBackground(String... busStopNames)
        {
            ArrayList<Trip> trips = DbQueries.getDirectTripsBetweenStops(db, busStopNames[0], busStopNames[1]);

            for (Trip trip : trips)
            {
                if (isCancelled())
                {
                    break;
                }

                trip.setOriginBusStop(DbQueries.getStopDetails(db, trip.getOriginBusStop().getBusStopId()));

                BusStop routeOriginBusStop = DbQueries.getStopDetails(db, ((DirectTrip) trip).getBusRoute()
                        .getTripPlannerOriginBusStop().getBusStopId());

                if (routeOriginBusStop != null)
                {
                    routeOriginBusStop.setBusStopRouteOrder(DbQueries.getStopRouteOrder(db,
                            ((DirectTrip) trip).getBusRoute().getBusRouteId(),
                            ((DirectTrip) trip).getBusRoute().getTripPlannerOriginBusStop().getBusStopId()));
                }

                BusStop routeDestinationBusStop = DbQueries.getStopDetails(db, ((DirectTrip) trip).getBusRoute()
                        .getTripPlannerDestinationBusStop().getBusStopId());

                if (routeDestinationBusStop != null)
                {
                    routeDestinationBusStop.setBusStopRouteOrder(DbQueries.getStopRouteOrder(db,
                            ((DirectTrip) trip).getBusRoute().getBusRouteId(),
                            ((DirectTrip) trip).getBusRoute().getTripPlannerDestinationBusStop().getBusStopId()));
                }

                ((DirectTrip) trip).setBusRoute(DbQueries.getRouteDetails(db,
                        ((DirectTrip) trip).getBusRoute().getBusRouteId()));

                ((DirectTrip) trip).getBusRoute().setTripPlannerOriginBusStop(routeOriginBusStop);
                ((DirectTrip) trip).getBusRoute().setTripPlannerDestinationBusStop(routeDestinationBusStop);
            }

            return trips;
        }

        @Override
        protected void onPostExecute(ArrayList<Trip> trips)
        {
            super.onPostExecute(trips);

            if (!isCancelled())
            {
                onDirectTripsFound(trips);
            }
        }
    }
}