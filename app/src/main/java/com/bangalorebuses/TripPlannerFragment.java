package com.bangalorebuses;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import static android.app.Activity.RESULT_OK;
import static com.bangalorebuses.Constants.NETWORK_QUERY_NO_ERROR;
import static com.bangalorebuses.Constants.SEARCH_END_BUS_STOP_REQUEST_CODE;
import static com.bangalorebuses.Constants.SEARCH_START_BUS_STOP_REQUEST_CODE;
import static com.bangalorebuses.Constants.SEARCH_TYPE_BUS_STOP;
import static com.bangalorebuses.Constants.db;

public class TripPlannerFragment extends Fragment implements TripPlannerHelper
{
    private Animation swapAnimation;
    private BusStop originBusStop = new BusStop();
    private BusStop destinationBusStop = new BusStop();
    private ImageView swapDirectionImageView;
    private Button searchOriginButton;
    private Button searchDestinationButton;
    private GetDirectRoutesBetweenStops getDirectRoutesBetweenStops;
    private GetDirectTripDetails getDirectTripDetails;
    private ArrayList<DirectTrip> directTrips = new ArrayList<>();
    private int numberOfDirectTripBusesFound = 0;
    private ArrayList<DirectTrip> directTripsToQuery = new ArrayList<>();
    private int numberOfDirectTripQueriesMade = 0;
    private ListView listView;
    private ProgressBar progressBar;
    private DirectTrip fastestDirectTrip;

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
        listView = (ListView) view.findViewById(R.id.listView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        originBusStop = new BusStop();
        destinationBusStop = new BusStop();
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
            findDirectRoutes(originBusStop.getBusStopName(), destinationBusStop.getBusStopName());
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
                findDirectRoutes(originBusStop.getBusStopName(), destinationBusStop.getBusStopName());
            }
        }
    }

    private void findDirectRoutes(String originBusStopName, String destinationBusStopName)
    {
        //listView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        getDirectRoutesBetweenStops = new GetDirectRoutesBetweenStops();
        getDirectRoutesBetweenStops.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                originBusStopName, destinationBusStopName);
    }

    private void onDirectRoutesFound(ArrayList<DirectTrip> directTrips)
    {
        if (directTrips.size() != 0)
        {
            getDirectTripDetails = new GetDirectTripDetails();
            getDirectTripDetails.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, directTrips);
        }
        else
        {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getActivity(), "There aren't any direct routes...", Toast.LENGTH_SHORT).show();
            //TODO No Direct Trips Found
        }
    }

    private void onDirectTripDetailsFound(ArrayList<DirectTrip> directTrips)
    {
        //TODO Check for internet connection
        numberOfDirectTripQueriesMade = 0;
        numberOfDirectTripBusesFound = 0;
        directTripsToQuery = directTrips;
        this.directTrips.clear();

        for (; numberOfDirectTripQueriesMade < 20; numberOfDirectTripQueriesMade++)
        {
            if (numberOfDirectTripQueriesMade < directTripsToQuery.size())
            {
                //Log.i("TripPlanner", "Querying " + directTripsToQuery.get(numberOfDirectTripQueriesMade).getRoute().getBusRouteNumber());
                new GetBusesEnDirectRouteTask(this, directTripsToQuery.get(numberOfDirectTripQueriesMade)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    @Override
    public void onBusesEnDirectRouteFound(String errorMessage, DirectTrip directTrip)
    {
        //Log.i("TripPlanner", directTrip.getRoute().getBusRouteNumber() + " queried. " + numberOfDirectTripBusesFound + "/" + numberOfDirectTripQueriesMade + " complete.");
        numberOfDirectTripBusesFound++;
        if (errorMessage.equals(NETWORK_QUERY_NO_ERROR))
        {
            /*if (directTrip.getRoute().getBusRouteBuses().size() != 0)
            {
                directTrip.getRoute().getBusRouteBuses().get(0).setBusETA(calculateTravelTime(
                        DbQueries.getNumberOfStopsBetweenRouteOrders(db, directTrip.getRoute().getBusRouteId(),
                                directTrip.getRoute().getBusRouteBuses().get(0).getBusRouteOrder(),
                                directTrip.getOriginStop().getBusStopRouteOrder()), directTrip.getRoute().getBusRouteNumber()));

                int travelTimeFromOriginToDestination = calculateTravelTime(
                        DbQueries.getNumberOfStopsBetweenRouteOrders(db, directTrip.getRoute().getBusRouteId(),
                                directTrip.getOriginStop().getBusStopRouteOrder(),
                                directTrip.getDestinationStop().getBusStopRouteOrder()), directTrip.getRoute().getBusRouteNumber());

                directTrip.setTravelTime(travelTimeFromOriginToDestination + directTrip.getRoute().getBusRouteBuses().get(0).getBusETA());
                directTrips.add(directTrip);
            }*/
        }
        else
        {
            //TODO
        }

        synchronized (this)
        {
            if (numberOfDirectTripQueriesMade < directTripsToQuery.size())
            {
                //TODO check for internet connection
                //Log.i("TripPlanner", "Querying " + directTripsToQuery.get(numberOfDirectTripQueriesMade).getRoute().getBusRouteNumber());
                new GetBusesEnDirectRouteTask(this, directTripsToQuery.get(numberOfDirectTripQueriesMade)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                numberOfDirectTripQueriesMade++;
            }
        }

        if (numberOfDirectTripBusesFound == directTripsToQuery.size())
        {
            progressBar.setVisibility(View.GONE);
            fastestDirectTrip = null;
            for (DirectTrip directTripToCompare : directTrips)
            {
                if (fastestDirectTrip != null)
                {
                    /*if (directTripToCompare.getTravelTime() != 0 &&
                            directTripToCompare.getTravelTime() < fastestDirectTrip.getTravelTime())
                    {
                        fastestDirectTrip = directTripToCompare;
                    }*/
                }
                else
                {
                    /*if (directTripToCompare.getTravelTime() != 0)
                    {
                        fastestDirectTrip = directTripToCompare;
                    }*/
                }
            }

            ArrayList<DirectTrip> directTripsToDisplay = new ArrayList<>();
            if (fastestDirectTrip != null)
            {
                directTripsToDisplay.add(fastestDirectTrip);
                TripPlannerDirectTripListAdapter adapter = new TripPlannerDirectTripListAdapter(getActivity(), directTripsToDisplay);
                listView.setVisibility(View.VISIBLE);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        Intent directTripDetailsIntent = new Intent(getContext(), DirectTripDetailsActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("DIRECT_TRIP", fastestDirectTrip);
                        directTripDetailsIntent.putExtras(bundle);
                        startActivity(directTripDetailsIntent);
                    }
                });
            }
            else
            {
                // TODO No routes right now
                Toast.makeText(getActivity(), "There aren't any direct routes right now", Toast.LENGTH_SHORT).show();
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

    private class GetDirectRoutesBetweenStops extends AsyncTask<String, Void, ArrayList<DirectTrip>>
    {
        @Override
        protected ArrayList<DirectTrip> doInBackground(String... busStopNames)
        {
            return DbQueries.getDirectRoutesBetweenStops(db, busStopNames[0], busStopNames[1]);
        }

        @Override
        protected void onPostExecute(ArrayList<DirectTrip> directTrips)
        {
            super.onPostExecute(directTrips);
            onDirectRoutesFound(directTrips);
        }
    }

    private class GetDirectTripDetails extends AsyncTask<ArrayList<DirectTrip>, Void, ArrayList<DirectTrip>>
    {
        @Override
        protected ArrayList<DirectTrip> doInBackground(ArrayList<DirectTrip>... directTrips)
        {
            for (DirectTrip directTrip : directTrips[0])
            {
                //directTrip.setRoute(DbQueries.getRouteDetails(db, directTrip.getRoute().getBusRouteId()));

                directTrip.setOriginStop(DbQueries.getStopDetails(db, directTrip.getOriginStop().getBusStopId()));
                /*directTrip.getOriginStop().setBusStopRouteOrder(DbQueries.getStopRouteOrder(db, directTrip.getRoute().getBusRouteId(),
                        directTrip.getOriginStop().getBusStopId()));*/

                directTrip.setDestinationStop(DbQueries.getStopDetails(db, directTrip.getDestinationStop().getBusStopId()));
                /*directTrip.getDestinationStop().setBusStopRouteOrder(DbQueries.getStopRouteOrder(db, directTrip.getRoute().getBusRouteId(),
                        directTrip.getDestinationStop().getBusStopId()));*/
            }

            return directTrips[0];
        }

        @Override
        protected void onPostExecute(ArrayList<DirectTrip> directTrips)
        {
            super.onPostExecute(directTrips);
            onDirectTripDetailsFound(directTrips);
        }
    }
}