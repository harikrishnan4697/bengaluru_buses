package com.bangalorebuses.trips;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bangalorebuses.R;
import com.bangalorebuses.core.Bus;
import com.bangalorebuses.core.BusRoute;
import com.bangalorebuses.core.BusStop;
import com.bangalorebuses.search.SearchActivity;
import com.bangalorebuses.utils.Constants;
import com.bangalorebuses.utils.DbQueries;
import com.bangalorebuses.utils.ErrorImageResIds;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import static com.bangalorebuses.utils.Constants.FAVORITES_TYPE;
import static com.bangalorebuses.utils.Constants.FAVORITES_TYPE_BUS_STOP;
import static com.bangalorebuses.utils.Constants.NETWORK_QUERY_NO_ERROR;
import static com.bangalorebuses.utils.Constants.NUMBER_OF_ROUTES_TYPE_ORIGIN_TO_TRANSIT_POINT;
import static com.bangalorebuses.utils.Constants.NUMBER_OF_ROUTES_TYPE_TRANSIT_POINT_TO_DESTINATION;
import static com.bangalorebuses.utils.Constants.SEARCH_END_BUS_STOP_REQUEST_CODE;
import static com.bangalorebuses.utils.Constants.SEARCH_START_BUS_STOP_REQUEST_CODE;
import static com.bangalorebuses.utils.Constants.SEARCH_TYPE_BUS_STOP;
import static com.bangalorebuses.utils.Constants.db;

public class TripPlannerActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener, DirectTripHelper, IndirectTripHelper
{
    private TextView originSelectionTextView;
    private TextView destinationSelectionTextView;

    private Animation rotateOnceForward;
    private ImageView swapDirectionImageView;

    private String originBusStopName;
    private String destinationBusStopName;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TripsRecyclerViewAdapter recyclerViewAdapter;

    private LinearLayout errorLinearLayout;
    private ImageView errorImageView;
    private TextView errorTextView;
    private TextView errorResolutionTextView;

    private ArrayList<Trip> tripsToDisplay = new ArrayList<>();
    private ArrayList<Trip> tripsToQuery = new ArrayList<>();

    // Direct trip related variables
    private ArrayList<BusETAsOnDirectTripTask> busETAsOnDirectTripTasks = new ArrayList<>();
    private GetDirectTripsBetweenStops getDirectTripsBetweenStops;
    private int numberOfDirectTripQueriesMade = 0;
    private int numberOfDirectTripRouteBusesFound = 0;

    // Indirect trip related variables
    private ArrayList<TransitPoint> transitPoints = new ArrayList<>();
    private int numberOfTransitPointQueriesMade = 0;
    private int numberOfTransitPointQueriesComplete = 0;
    private int numberOfIndirectTripQueriesMade = 0;
    private int numberOfIndirectTripQueriesComplete = 0;
    private ArrayList<BusETAsOnLeg1BusRouteTask> busETAsOnLeg1BusRouteTasks = new ArrayList<>();
    private TransitPointsWithNumberOfRoutesDbTask transitPointsWithNumberOfRoutesDbTask1;
    private TransitPointsWithNumberOfRoutesDbTask transitPointsWithNumberOfRoutesDbTask2;
    private ArrayList<BusRoutesToAndFromTransitPointDbTask> busRoutesToAndFromTransitPointDbTasks =
            new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_planner);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }*/

        originSelectionTextView = (TextView) findViewById(R.id.origin_text_view);
        originSelectionTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                selectOriginBusStop();
            }
        });

        destinationSelectionTextView = (TextView) findViewById(R.id.destination_text_view);
        destinationSelectionTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                selectDestinationBusStop();
            }
        });

        rotateOnceForward = AnimationUtils.loadAnimation(this, R.anim.rotate_once_forward);

        //swapDirectionImageView = (ImageView) findViewById(R.id.swap_direction_image_view);
        /*swapDirectionImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                swapDirection();
            }
        });*/

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorOrdinaryServiceBus,
                R.color.colorACServiceBus, R.color.colorSpecialServiceBus);
        swipeRefreshLayout.setOnRefreshListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        errorLinearLayout = (LinearLayout) findViewById(R.id.errorLinearLayout);
        errorImageView = (ImageView) findViewById(R.id.errorImageView);
        errorTextView = (TextView) findViewById(R.id.errorTextView);
        errorResolutionTextView = (TextView) findViewById(R.id.errorResolutionTextView);
        errorResolutionTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (originBusStopName != null && destinationBusStopName != null)
                {
                    findDirectTrips();
                }
                else
                {
                    Toast.makeText(TripPlannerActivity.this,
                            "Please select a starting and ending bus stop!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        errorLinearLayout.setVisibility(View.GONE);

        if (originBusStopName == null)
        {
            selectOriginBusStop();
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.trip_planner_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_favorite:
                // TODO Favorite current trip
                break;
            default:
                return false;
        }
        return true;
    }

    private void selectOriginBusStop()
    {
        Intent searchOriginIntent = new Intent(this, SearchActivity.class);
        searchOriginIntent.putExtra("SEARCH_TYPE", SEARCH_TYPE_BUS_STOP);
        searchOriginIntent.putExtra("EDIT_TEXT_HINT", Constants.ORIGIN_BUS_STOP_SEARCH_HINT);
        searchOriginIntent.putExtra(FAVORITES_TYPE, FAVORITES_TYPE_BUS_STOP);
        startActivityForResult(searchOriginIntent, SEARCH_START_BUS_STOP_REQUEST_CODE);
    }

    private void selectDestinationBusStop()
    {
        Intent searchDestinationIntent = new Intent(this, SearchActivity.class);
        searchDestinationIntent.putExtra("SEARCH_TYPE", SEARCH_TYPE_BUS_STOP);
        searchDestinationIntent.putExtra("EDIT_TEXT_HINT", Constants.DESTINATION_BUS_STOP_SEARCH_HINT);
        searchDestinationIntent.putExtra(FAVORITES_TYPE, FAVORITES_TYPE_BUS_STOP);
        startActivityForResult(searchDestinationIntent, SEARCH_END_BUS_STOP_REQUEST_CODE);
    }

    private void swapDirection()
    {
        swapDirectionImageView.startAnimation(rotateOnceForward);

        String tempDestinationBusStopName = destinationBusStopName;

        destinationBusStopName = originBusStopName;
        originBusStopName = tempDestinationBusStopName;

        originSelectionTextView.setText(originBusStopName);
        destinationSelectionTextView.setText(destinationBusStopName);

        if (originBusStopName != null && destinationBusStopName != null)
        {
            findDirectTrips();
        }
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
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    @Override
    public void onRefresh()
    {
        if (originBusStopName != null && destinationBusStopName != null)
        {
            findDirectTrips();
        }
        else
        {
            swipeRefreshLayout.setRefreshing(false);
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
                case SEARCH_START_BUS_STOP_REQUEST_CODE:
                    originBusStopName = data.getStringExtra("BUS_STOP_NAME");
                    originSelectionTextView.setText(originBusStopName);

                    if (destinationBusStopName == null)
                    {
                        selectDestinationBusStop();
                    }

                    break;
                case SEARCH_END_BUS_STOP_REQUEST_CODE:
                    destinationBusStopName = data.getStringExtra("BUS_STOP_NAME");
                    destinationSelectionTextView.setText(destinationBusStopName);

                    if (originBusStopName == null)
                    {
                        selectOriginBusStop();
                    }

                    break;
                default:
                    break;
            }

            if (originBusStopName != null && destinationBusStopName != null)
            {
                findDirectTrips();
            }
        }
    }

    private void cancelAllPreviousTasks()
    {
        swipeRefreshLayout.setRefreshing(false);

        // Iterate over all the previous GetBusesEnDirectTripTasks to cancel each one of them
        for (BusETAsOnDirectTripTask task : busETAsOnDirectTripTasks)
        {
            if (task != null)
            {
                task.cancel(true);
            }
        }

        for (BusETAsOnLeg1BusRouteTask task : busETAsOnLeg1BusRouteTasks)
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

        if (transitPointsWithNumberOfRoutesDbTask1 != null)
        {
            transitPointsWithNumberOfRoutesDbTask1.cancel(true);
        }

        if (transitPointsWithNumberOfRoutesDbTask2 != null)
        {
            transitPointsWithNumberOfRoutesDbTask2.cancel(true);
        }

        for (BusRoutesToAndFromTransitPointDbTask task :
                busRoutesToAndFromTransitPointDbTasks)
        {
            if (task != null)
            {
                task.cancel(true);
            }
        }
    }

    // Everything to do with direct trips
    private void findDirectTrips()
    {
        cancelAllPreviousTasks();

        errorLinearLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(true);

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
            busETAsOnDirectTripTasks.clear();

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerViewAdapter = new TripsRecyclerViewAdapter(tripsToDisplay);
            recyclerView.setAdapter(recyclerViewAdapter);

            if (isNetworkAvailable())
            {
                for (; numberOfDirectTripQueriesMade < 10; numberOfDirectTripQueriesMade++)
                {
                    if (numberOfDirectTripQueriesMade < tripsToQuery.size())
                    {
                        BusETAsOnDirectTripTask task = new BusETAsOnDirectTripTask(this, ((DirectTrip) tripsToQuery.get(
                                numberOfDirectTripQueriesMade)));
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        busETAsOnDirectTripTasks.add(task);
                    }
                    else
                    {
                        break;
                    }
                }
            }
            else
            {
                recyclerView.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                setErrorLayoutContent(ErrorImageResIds.ERROR_IMAGE_NO_INTERNET, "Uh oh! No data connection.", "Retry");
                errorLinearLayout.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            swipeRefreshLayout.setRefreshing(false);
            findIndirectTrips(originBusStopName, destinationBusStopName);
        }
    }

    @Override
    public void onBusETAsOnDirectTripFound(String errorMessage, DirectTrip directTrip)
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

                ArrayList<Trip> tempTripsToDisplay = new ArrayList<>();
                for (int i = 0; i < 5; i++)
                {
                    if (i < tripsToDisplay.size())
                    {
                        tempTripsToDisplay.add(tripsToDisplay.get(i));
                    }
                    else
                    {
                        break;
                    }
                }

                tripsToDisplay.clear();
                for (int i = 0; i < 3; i++)
                {
                    if (i < tempTripsToDisplay.size())
                    {
                        tripsToDisplay.add(tempTripsToDisplay.get(i));
                    }
                    else
                    {
                        break;
                    }
                }

                recyclerViewAdapter.notifyDataSetChanged();
                recyclerView.setVisibility(View.VISIBLE);
            }
        }

        synchronized (this)
        {
            if (numberOfDirectTripQueriesMade < tripsToQuery.size())
            {
                BusETAsOnDirectTripTask task = new BusETAsOnDirectTripTask(this, ((DirectTrip)
                        tripsToQuery.get(numberOfDirectTripQueriesMade)));
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                busETAsOnDirectTripTasks.add(task);
                numberOfDirectTripQueriesMade++;
            }
        }

        if (numberOfDirectTripRouteBusesFound == numberOfDirectTripQueriesMade)
        {
            swipeRefreshLayout.setRefreshing(false);

            if (tripsToDisplay.size() == 0)
            {
                swipeRefreshLayout.setRefreshing(false);
                findIndirectTrips(originBusStopName, destinationBusStopName);
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
        swipeRefreshLayout.setRefreshing(true);
        transitPoints.clear();

        if (isNetworkAvailable())
        {
            transitPointsWithNumberOfRoutesDbTask1 = new TransitPointsWithNumberOfRoutesDbTask(this, originBusStopName,
                    destinationBusStopName, NUMBER_OF_ROUTES_TYPE_ORIGIN_TO_TRANSIT_POINT);
            transitPointsWithNumberOfRoutesDbTask1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            transitPointsWithNumberOfRoutesDbTask2 = new TransitPointsWithNumberOfRoutesDbTask(this, originBusStopName,
                    destinationBusStopName, NUMBER_OF_ROUTES_TYPE_TRANSIT_POINT_TO_DESTINATION);
            transitPointsWithNumberOfRoutesDbTask2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else
        {
            swipeRefreshLayout.setRefreshing(false);
            setErrorLayoutContent(ErrorImageResIds.ERROR_IMAGE_NO_INTERNET, "Uh oh! No data connection.", "Retry");
            errorLinearLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    // TODO make separate/unique connections to the db
    @Override
    public void onTransitPointsAndRouteCountOriginToTPFound(ArrayList<TransitPoint> transitPoints)
    {
        // Make sure that there are transit points.
        if (transitPoints.size() == 0)
        {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, "There aren't are direct or indirect trips...", Toast.LENGTH_SHORT).show();
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
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, "There aren't are direct or indirect trips...", Toast.LENGTH_SHORT).show();
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
        // For each transit point, set the score to the smaller number of routes
        // of the two legs.
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

        // Sort the transit points in descending order based on their scores
        Collections.sort(this.transitPoints, new Comparator<TransitPoint>()
        {
            @Override
            public int compare(TransitPoint tp1, TransitPoint tp2)
            {
                return tp2.getTransitPointScore() - tp1.getTransitPointScore();
            }
        });

        // Keep only the top 5 transit points based on their scores
        ArrayList<TransitPoint> tempTransitPoints = new ArrayList<>();

        for (int i = 0; i < 5; i++)
        {
            if (i < this.transitPoints.size())
            {
                tempTransitPoints.add(this.transitPoints.get(i));
            }
        }

        this.transitPoints = tempTransitPoints;

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerViewAdapter = new TripsRecyclerViewAdapter(tripsToDisplay);
        recyclerView.setAdapter(recyclerViewAdapter);
        tripsToDisplay.clear();
        numberOfIndirectTripQueriesMade = 0;
        numberOfIndirectTripQueriesComplete = 0;
        numberOfTransitPointQueriesMade = 0;
        numberOfTransitPointQueriesComplete = 0;
        busRoutesToAndFromTransitPointDbTasks.clear();
        busETAsOnLeg1BusRouteTasks.clear();

        for (TransitPoint transitPoint : this.transitPoints)
        {
            numberOfTransitPointQueriesMade++;

            BusRoutesToAndFromTransitPointDbTask task = new BusRoutesToAndFromTransitPointDbTask(this,
                    originBusStopName, transitPoint, destinationBusStopName);

            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            busRoutesToAndFromTransitPointDbTasks.add(task);
        }
    }

    @Override
    public void onBusRoutesToAndFromTransitPointFound(TransitPoint transitPoint)
    {
        numberOfTransitPointQueriesComplete++;

        for (int i = 0; i < 5; i++)
        {
            if (i < transitPoint.getBusRoutesToTransitPoint().size())
            {
                numberOfIndirectTripQueriesMade++;

                BusETAsOnLeg1BusRouteTask task = new BusETAsOnLeg1BusRouteTask(this,
                        transitPoint.getBusRoutesToTransitPoint().get(i), transitPoint);

                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                busETAsOnLeg1BusRouteTasks.add(task);
            }
            else
            {
                break;
            }
        }
    }

    @Override
    public void onBusETAsOnLeg1BusRouteFound(String errorMessage, BusRoute busRoute, TransitPoint transitPoint)
    {
        numberOfIndirectTripQueriesComplete++;

        synchronized (this)
        {
            if (errorMessage.equals(NETWORK_QUERY_NO_ERROR))
            {
                if (busRoute.getBusRouteBuses().size() != 0)
                {
                    IndirectTrip indirectTrip = new IndirectTrip();
                    indirectTrip.setOriginBusStop(busRoute.getTripPlannerOriginBusStop());
                    indirectTrip.setDestinationBusStopName(destinationBusStopName);

                    busRoute.getBusRouteBuses().get(0).setBusETA(calculateTravelTime(DbQueries.getNumberOfStopsBetweenRouteOrders(db,
                            busRoute.getBusRouteId(), busRoute.getBusRouteBuses().get(0).getBusRouteOrder(), busRoute.getTripPlannerOriginBusStop()
                                    .getBusStopRouteOrder()), busRoute.getBusRouteNumber()));

                    busRoute.getBusRouteBuses().get(0).setBusETAToTransitPoint(calculateTravelTime(DbQueries.getNumberOfStopsBetweenRouteOrders(db,
                            busRoute.getBusRouteId(), busRoute.getBusRouteBuses().get(0).getBusRouteOrder(), busRoute.getTripPlannerDestinationBusStop()
                                    .getBusStopRouteOrder()), busRoute.getBusRouteNumber()));

                    indirectTrip.setBusToTransitPoint(busRoute.getBusRouteBuses().get(0));
                    indirectTrip.setTransitPoint(transitPoint);

                    if (transitPoint.getBusRoutesFromTransitPoint().size() != 0)
                    {
                        indirectTrip.setBusFromTransitPoint(transitPoint.getBusRoutesFromTransitPoint().get(0)
                                .getBusRouteBuses().get(0));
                    }
                    else
                    {
                        return;
                    }

                    Bus bus = null;
                    for (BusRoute busRouteFromTP : transitPoint.getBusRoutesFromTransitPoint())
                    {
                        for (Bus busFromTP : busRouteFromTP.getBusRouteBuses())
                        {
                            if (busFromTP.getBusETA() > (indirectTrip.getBusToTransitPoint().getBusETAToTransitPoint() + 2))
                            {
                                if (bus != null)
                                {
                                    if (busFromTP.getBusETA() < bus.getBusETA())
                                    {
                                        bus = busFromTP;
                                    }
                                }
                                else
                                {
                                    bus = busFromTP;
                                }
                            }
                        }
                    }

                    indirectTrip.setBusFromTransitPoint(bus);

                    if (indirectTrip.getBusToTransitPoint() != null && indirectTrip.getBusFromTransitPoint() != null)
                    {
                        int travelTimeFromTPToDest = calculateTravelTime(DbQueries.getNumberOfStopsBetweenRouteOrders(db,
                                indirectTrip.getBusFromTransitPoint().getBusRoute().getBusRouteId(), indirectTrip.getBusFromTransitPoint()
                                        .getBusRoute().getTripPlannerOriginBusStop().getBusStopRouteOrder(), indirectTrip.getBusFromTransitPoint()
                                        .getBusRoute().getTripPlannerDestinationBusStop().getBusStopRouteOrder()), indirectTrip.getBusFromTransitPoint()
                                .getBusRoute().getBusRouteNumber());

                        indirectTrip.setTripDuration(indirectTrip.getBusToTransitPoint().getBusETAToTransitPoint() + (indirectTrip.getBusFromTransitPoint()
                                .getBusETA() - indirectTrip.getBusToTransitPoint().getBusETAToTransitPoint()) + travelTimeFromTPToDest);

                        tripsToDisplay.add(indirectTrip);

                        Collections.sort(tripsToDisplay, new Comparator<Trip>()
                        {
                            @Override
                            public int compare(Trip o1, Trip o2)
                            {
                                return ((IndirectTrip) o1).getTripDuration() - ((IndirectTrip) o2).getTripDuration();
                            }
                        });

                        ArrayList<Trip> tempTripsToDisplay = new ArrayList<>();
                        for (int i = 0; i < 3; i++)
                        {
                            if (i < tripsToDisplay.size())
                            {
                                tempTripsToDisplay.add(tripsToDisplay.get(i));
                            }
                            else
                            {
                                break;
                            }
                        }

                        tripsToDisplay.clear();
                        for (int i = 0; i < 5; i++)
                        {
                            if (i < tempTripsToDisplay.size())
                            {
                                tripsToDisplay.add(tempTripsToDisplay.get(i));
                            }
                            else
                            {
                                break;
                            }
                        }

                        recyclerViewAdapter.notifyDataSetChanged();
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                }
                else
                {
                    //TODO
                }
            }
            else
            {
                //TODO Handle errors
            }

            if (numberOfIndirectTripQueriesComplete == numberOfIndirectTripQueriesMade &&
                    numberOfTransitPointQueriesComplete == numberOfTransitPointQueriesMade)
            {
                swipeRefreshLayout.setRefreshing(false);

                if (tripsToDisplay.size() == 0)
                {
                    //TODO 'There aren't trips' message gets displayed and then displays trips...needs to be fixed
                    setErrorLayoutContent(R.drawable.ic_directions_bus_black, "Oh no! There aren't any trips right now...", "Retry");
                    errorLinearLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            }
        }
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
        cancelAllPreviousTasks();
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
