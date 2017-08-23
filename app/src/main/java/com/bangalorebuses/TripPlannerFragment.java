package com.bangalorebuses;

import android.content.Intent;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
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
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private ArrayList<Trip> tripsToDisplay = new ArrayList<>();
    private int numberOfDirectTripsFound = 0;
    private int numberOfDirectTripRouteBusesFound = 0;

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
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

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
                break;
            case R.id.action_clear:
                clearAll();
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
        clearAll();
    }

    private void clearAll()
    {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        originBusStop = new BusStop();
        destinationBusStop = new BusStop();
        tripsToDisplay.clear();
        numberOfDirectTripsFound = 0;
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
        searchOriginButton.setText(originBusStop.getBusStopName());
        searchDestinationButton.setText(destinationBusStop.getBusStopName());
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

    private void findDirectTrips(String originBusStopName, String destinationBusStopName)
    {
        if (getTripsBetweenStops != null)
        {
            getTripsBetweenStops.cancel(true);
        }

        if (getTripDetails != null)
        {
            getTripDetails.cancel(true);
        }

        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        getTripsBetweenStops = new GetTripsBetweenStops();
        getTripsBetweenStops.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                originBusStopName, destinationBusStopName);
    }

    private void onTripsFound(ArrayList<Trip> trips)
    {
        if (trips.size() != 0)
        {
            getTripDetails = new GetTripDetails();
            getTripDetails.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, trips);
        }
        else
        {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getActivity(), "There aren't any trips...", Toast.LENGTH_SHORT).show();
            //TODO No Trips Found
        }
    }

    private void onTripDetailsFound(ArrayList<Trip> trips)
    {
        numberOfDirectTripsFound = trips.size();
        numberOfDirectTripRouteBusesFound = 0;
        tripsToDisplay.clear();
        for (Trip aTrip : trips)
        {
            aTrip.getBusesOnFirstBusRoute(this, getContext());
        }
    }

    @Override
    public void onBusesInServiceFound(Trip trip)
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

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        TripsRecyclerViewAdapter adapter = new TripsRecyclerViewAdapter(tripsToDisplay);
        recyclerView.setAdapter(adapter);
        recyclerView.setVisibility(View.VISIBLE);

        if (numberOfDirectTripRouteBusesFound == numberOfDirectTripsFound)
        {
            progressBar.setVisibility(View.GONE);
        }
    }

    /*@Override
    public void onDirectTripBusesEnRoutesFound(DirectTripOld directTrip)
    {
        numberOfDirectTripRouteBusesFound++;

        Collections.sort(directTrip.getBusRoutes(), new Comparator<BusRoute>()
        {
            @Override
            public int compare(BusRoute o1, BusRoute o2)
            {
                return o1.getShortestOriginToDestinationTravelTime() - o2.getShortestOriginToDestinationTravelTime();
            }
        });

        int shortestTravelTime = 0;

        for (BusRoute busRoute : directTrip.getBusRoutes())
        {
            if (shortestTravelTime != 0)
            {
                if ((busRoute.getShortestOriginToDestinationTravelTime() < shortestTravelTime) &&
                        busRoute.getShortestOriginToDestinationTravelTime() != 0)
                {
                    shortestTravelTime = busRoute.getShortestOriginToDestinationTravelTime();
                }
            }
            else
            {
                shortestTravelTime = busRoute.getShortestOriginToDestinationTravelTime();
            }
        }
        directTrip.setShortestTravelTime(shortestTravelTime);

        if (directTrip.getShortestTravelTime() != 0)
        {
            ArrayList<Bus> busesOnDirectTrip = new ArrayList<>();

            for (BusRoute busRoute: directTrip.getBusRoutes())
            {
                for (Bus bus: busRoute.getBusRouteBuses())
                {
                    busesOnDirectTrip.add(bus);
                }
            }

            Collections.sort(busesOnDirectTrip, new Comparator<Bus>()
            {
                @Override
                public int compare(Bus o1, Bus o2)
                {
                    return o1.getBusETA() - o2.getBusETA();
                }
            });

            directTrip.setBusesOnDirectTrip(busesOnDirectTrip);

            directTripsToDisplay.add(directTrip);
        }

        if (numberOfDirectTripRouteBusesFound == numberOfDirectTripsFound)
        {
            Collections.sort(directTripsToDisplay, new Comparator<DirectTripOld>()
            {
                @Override
                public int compare(DirectTripOld o1, DirectTripOld o2)
                {
                    return o1.getShortestTravelTime() - o2.getShortestTravelTime();
                }
            });

            progressBar.setVisibility(View.GONE);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(linearLayoutManager);
            DirectTripsRecyclerViewAdapter adapter = new DirectTripsRecyclerViewAdapter(directTripsToDisplay);
            recyclerView.setAdapter(adapter);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }*/

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
}