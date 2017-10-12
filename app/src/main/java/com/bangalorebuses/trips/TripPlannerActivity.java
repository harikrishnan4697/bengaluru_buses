package com.bangalorebuses.trips;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import com.bangalorebuses.utils.CommonMethods;
import com.bangalorebuses.utils.Constants;
import com.bangalorebuses.utils.DbQueries;
import com.bangalorebuses.utils.ErrorImageResIds;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
    private FloatingActionButton favoritesFloatingActionButton;
    private boolean isFavorite = false;
    private TextView originSelectionTextView;
    private TextView destinationSelectionTextView;
    private Animation rotateOnceForward;
    private ImageView swapDirectionImageView;
    private String originBusStopName;
    private String destinationBusStopName;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private LinearLayout errorLinearLayout;
    private ImageView errorImageView;
    private TextView errorTextView;
    private TextView errorResolutionTextView;

    // Direct trip related variables
    private ArrayList<DirectTrip> directTripsToDisplay = new ArrayList<>();
    private ArrayList<Trip> tripsToQuery = new ArrayList<>();
    private ArrayList<BusETAsOnDirectTripTask> busETAsOnDirectTripTasks = new ArrayList<>();
    private GetDirectTripsBetweenStops getDirectTripsBetweenStops;
    private int numberOfDirectTripQueriesMade = 0;
    private int numberOfDirectTripRouteBusesFound = 0;

    // Indirect trip related variables
    private int numberOfMostFrequentBusRouteQueriesMade = 0;
    private int numberOfMostFrequentBusRouteQueriesComplete = 0;
    private ArrayList<TransitPoint> transitPointsToDisplay =
            new ArrayList<>();
    private ArrayList<TransitPoint> transitPoints = new ArrayList<>();
    private TransitPointsWithNumberOfRoutesDbTask transitPointsWithNumberOfRoutesDbTask1;
    private TransitPointsWithNumberOfRoutesDbTask transitPointsWithNumberOfRoutesDbTask2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_planner);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.trip_planner_title);
        }

        favoritesFloatingActionButton = (FloatingActionButton) findViewById(R.id
                .favorites_floating_action_button);
        favoritesFloatingActionButton.setVisibility(View.GONE);
        favoritesFloatingActionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                favoriteTrip(originBusStopName, destinationBusStopName);
            }
        });

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

        swapDirectionImageView = (ImageView) findViewById(R.id.swap_direction_image_view);
        swapDirectionImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                swapDirection();
            }
        });

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

        if (getIntent().getStringExtra(Constants.TRIP_ORIGIN_BUS_STOP_NAME) != null)
        {
            originBusStopName = getIntent().getStringExtra(Constants
                    .TRIP_ORIGIN_BUS_STOP_NAME);
            originSelectionTextView.setText(originBusStopName);
        }

        if (getIntent().getStringExtra(Constants.TRIP_DESTINATION_BUS_STOP_NAME) != null)
        {
            destinationBusStopName = getIntent().getStringExtra(Constants
                    .TRIP_DESTINATION_BUS_STOP_NAME);
            destinationSelectionTextView.setText(destinationBusStopName);
        }

        if (originBusStopName == null)
        {
            selectOriginBusStop();
        }
        else if (destinationBusStopName == null)
        {
            selectDestinationBusStop();
        }

        if (originBusStopName != null && destinationBusStopName != null)
        {
            findDirectTrips();
            checkIfTripIsFavorite(originBusStopName, destinationBusStopName);
            favoritesFloatingActionButton.setVisibility(View.VISIBLE);
        }
        else
        {
            favoritesFloatingActionButton.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
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
            checkIfTripIsFavorite(originBusStopName, destinationBusStopName);
            favoritesFloatingActionButton.setVisibility(View.VISIBLE);
        }
        else
        {
            favoritesFloatingActionButton.setVisibility(View.GONE);
        }
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
                checkIfTripIsFavorite(originBusStopName, destinationBusStopName);
                favoritesFloatingActionButton.setVisibility(View.VISIBLE);
            }
            else
            {
                favoritesFloatingActionButton.setVisibility(View.GONE);
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
            directTripsToDisplay.clear();
            tripsToQuery = trips;
            busETAsOnDirectTripTasks.clear();

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(linearLayoutManager);

            if (CommonMethods.checkNetworkConnectivity(this))
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
                    bus.setBusETA(CommonMethods.calculateTravelTime(directTrip.getBusRoute().getBusRouteId(), directTrip.getBusRoute().getBusRouteNumber(), bus.getBusRouteOrder(),
                            directTrip.getBusRoute().getTripPlannerOriginBusStop().getBusStopRouteOrder()));
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
                        .getBusRouteBuses().get(0).getBusETA() + CommonMethods.calculateTravelTime(directTrip.getBusRoute().getBusRouteId(),
                        directTrip.getBusRoute().getBusRouteNumber(), directTrip.getBusRoute().getTripPlannerOriginBusStop().getBusStopRouteOrder(),
                        directTrip.getBusRoute().getTripPlannerDestinationBusStop().getBusStopRouteOrder()));

                directTripsToDisplay.add(directTrip);
                Collections.sort(directTripsToDisplay, new Comparator<Trip>()
                {
                    @Override
                    public int compare(Trip o1, Trip o2)
                    {
                        return ((DirectTrip) o1).getBusRoute().getShortestOriginToDestinationTravelTime() -
                                ((DirectTrip) o2).getBusRoute().getShortestOriginToDestinationTravelTime();
                    }
                });

                ArrayList<DirectTrip> tempTripsToDisplay = new ArrayList<>();
                for (int i = 0; i < 5; i++)
                {
                    if (i < directTripsToDisplay.size())
                    {
                        tempTripsToDisplay.add(directTripsToDisplay.get(i));
                    }
                    else
                    {
                        break;
                    }
                }

                directTripsToDisplay.clear();
                for (int i = 0; i < 3; i++)
                {
                    if (i < tempTripsToDisplay.size())
                    {
                        directTripsToDisplay.add(tempTripsToDisplay.get(i));
                    }
                    else
                    {
                        break;
                    }
                }

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
                recyclerView.setLayoutManager(linearLayoutManager);
                DirectTripsRecyclerViewAdapter adapter = new
                        DirectTripsRecyclerViewAdapter(this, directTripsToDisplay);
                recyclerView.setAdapter(adapter);
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

            if (directTripsToDisplay.size() == 0)
            {
                swipeRefreshLayout.setRefreshing(false);
                findIndirectTrips(originBusStopName, destinationBusStopName);
            }
        }
    }

    // Everything to do with indirect trips
    private void findIndirectTrips(String originBusStopName, String destinationBusStopName)
    {
        errorLinearLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(true);
        transitPoints.clear();

        if (CommonMethods.checkNetworkConnectivity(this))
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
        if (transitPoints.size() == 0)
        {
            recyclerView.setVisibility(View.GONE);

            setErrorLayoutContent(ErrorImageResIds.ERROR_IMAGE_NO_BUSES_IN_SERVICE,
                    "Sorry! There aren't any direct or indirect trips.", "Retry");
            errorLinearLayout.setVisibility(View.VISIBLE);

            return;
        }

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

        transitPointsToDisplay.clear();
        numberOfMostFrequentBusRouteQueriesMade = 0;
        numberOfMostFrequentBusRouteQueriesComplete = 0;

        for (TransitPoint transitPoint : this.transitPoints)
        {
            new MostFrequentBusRouteDbTask(this, originBusStopName, transitPoint
                    .getTransitPointName(), destinationBusStopName).executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR);

            numberOfMostFrequentBusRouteQueriesMade++;
        }
    }

    @Override
    public void onMostFrequentBusRouteFound(String transitPointBusStopName,
                                            BusRoute mostFrequentBusRouteOnFirstLeg,
                                            BusRoute mostFrequentBusRouteOnSecondLeg)
    {
        numberOfMostFrequentBusRouteQueriesComplete++;

        int estimatedTotalWaitTimeForTrip = 15;

        int travelTimeOnFirstLeg = CommonMethods.calculateTravelTime(
                mostFrequentBusRouteOnFirstLeg.getBusRouteId(), mostFrequentBusRouteOnFirstLeg
                        .getBusRouteNumber(), mostFrequentBusRouteOnFirstLeg
                        .getTripPlannerOriginBusStop().getBusStopRouteOrder(),
                mostFrequentBusRouteOnFirstLeg.getTripPlannerDestinationBusStop()
                        .getBusStopRouteOrder());

        int travelTimeOnSecondLeg = CommonMethods.calculateTravelTime(
                mostFrequentBusRouteOnSecondLeg.getBusRouteId(), mostFrequentBusRouteOnSecondLeg
                        .getBusRouteNumber(), mostFrequentBusRouteOnSecondLeg
                        .getTripPlannerOriginBusStop().getBusStopRouteOrder(),
                mostFrequentBusRouteOnSecondLeg.getTripPlannerDestinationBusStop()
                        .getBusStopRouteOrder());

        TransitPoint transitPoint = new TransitPoint();
        transitPoint.setTransitPointName(transitPointBusStopName);
        transitPoint.setMostFrequentBusRouteToTransitPoint(mostFrequentBusRouteOnFirstLeg);
        transitPoint.setMostFrequentBusRouteFromTransitPoint(mostFrequentBusRouteOnSecondLeg);
        transitPoint.setShortestTripDuration(estimatedTotalWaitTimeForTrip + travelTimeOnFirstLeg
                + travelTimeOnSecondLeg);

        transitPointsToDisplay.add(transitPoint);
        transitPointsToDisplay.trimToSize();

        Collections.sort(transitPointsToDisplay, new Comparator<TransitPoint>()
        {
            @Override
            public int compare(TransitPoint t1, TransitPoint t2)
            {
                return t1.getShortestTripDuration() - t2.getShortestTripDuration();
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        IndirectTripsRecyclerViewAdapter adapter = new
                IndirectTripsRecyclerViewAdapter(this, transitPointsToDisplay);
        recyclerView.setAdapter(adapter);
        recyclerView.setVisibility(View.VISIBLE);

        if (numberOfMostFrequentBusRouteQueriesComplete ==
                numberOfMostFrequentBusRouteQueriesMade)
        {
            swipeRefreshLayout.setRefreshing(false);

            if (transitPointsToDisplay.size() == 0)
            {
                recyclerView.setVisibility(View.GONE);

                setErrorLayoutContent(ErrorImageResIds.ERROR_IMAGE_NO_BUSES_IN_SERVICE,
                        "Sorry! There aren't any direct or indirect trips.", "Retry");
                errorLinearLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    /*@Override
    public void onBusRoutesToAndFromTransitPointFound(TransitPoint transitPoint)
    {
        numberOfTransitPointQueriesComplete++;

        for (int i = 0; i < 5; i++)
        {
            if (i < transitPoint.getBusRoutesToTransitPoint().size())
            {
                numberOfIndirectTripQueriesMade++;

                BusETAsOnLeg1BusRouteTask task = new BusETAsOnLeg1BusRouteTask(this,
                        transitPoint, transitPoint.getBusRoutesToTransitPoint().get(i));
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
                    indirectTrip.setTransitPoint(transitPoint);
                    indirectTrip.setDestinationBusStopName(destinationBusStopName);

                    busRoute.getBusRouteBuses().get(0).setBusETAToTransitPoint(CommonMethods.calculateTravelTime(
                            busRoute.getBusRouteId(), busRoute.getBusRouteNumber(), busRoute
                                    .getBusRouteBuses().get(0).getBusRouteOrder(), busRoute
                                    .getTripPlannerDestinationBusStop().getBusStopRouteOrder()));

                    indirectTrip.setBusOnFirstLeg(busRoute.getBusRouteBuses().get(0));

                    if (transitPoint.getBusRoutesFromTransitPoint().size() != 0)
                    {
                        indirectTrip.setBusOnSecondLeg(transitPoint.getBusRoutesFromTransitPoint()
                                .get(0).getBusRouteBuses().get(0));
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
                            if (busFromTP.getBusETA() > (indirectTrip.getBusOnFirstLeg()
                                    .getBusETAToTransitPoint() + 2))
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

                    indirectTrip.setBusOnSecondLeg(bus);

                    if (indirectTrip.getBusOnFirstLeg() != null
                            && indirectTrip.getBusOnSecondLeg() != null)
                    {
                        BusRoute busRouteOnSecondLeg = indirectTrip.getBusOnSecondLeg().getBusRoute();

                        int travelTimeFromTPToDest = CommonMethods.calculateTravelTime(
                                busRouteOnSecondLeg.getBusRouteId(), busRouteOnSecondLeg
                                        .getBusRouteNumber(), busRouteOnSecondLeg.getTripPlannerOriginBusStop()
                                        .getBusStopRouteOrder(), busRouteOnSecondLeg
                                        .getTripPlannerDestinationBusStop().getBusStopRouteOrder());

                        indirectTrip.setTripDuration(indirectTrip.getBusOnFirstLeg().getBusETAToTransitPoint() +
                                (indirectTrip.getBusOnSecondLeg().getBusETA() - indirectTrip
                                        .getBusOnFirstLeg().getBusETAToTransitPoint()) + travelTimeFromTPToDest);

                        transitPoint.addIndirectTrip(indirectTrip);

                        boolean alreadyHasTransitPoint = false;
                        for (TransitPoint transitPointToDisplay : transitPointsToDisplay)
                        {
                            if (transitPointToDisplay.getTransitPointName()
                                    .equals(transitPoint.getTransitPointName()))
                            {
                                transitPointToDisplay.addIndirectTrip(indirectTrip);
                                alreadyHasTransitPoint = true;
                            }
                        }

                        if (!alreadyHasTransitPoint)
                        {
                            transitPointsToDisplay.add(transitPoint);
                        }

                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
                        recyclerView.setLayoutManager(linearLayoutManager);
                        IndirectTripsRecyclerViewAdapter adapter = new
                                IndirectTripsRecyclerViewAdapter(this, transitPointsToDisplay);
                        recyclerView.setAdapter(adapter);
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

                if (directTripsToDisplay.size() == 0)
                {
                    //TODO 'There aren't trips' message gets displayed and then displays trips...needs to be fixed
                    setErrorLayoutContent(R.drawable.ic_directions_bus_black, "Oh no! There aren't any trips right now...", "Retry");
                    errorLinearLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            }
        }
    }*/

    // Other stuff

    private void checkIfTripIsFavorite(String originBusStopName, String destinationBusStopName)
    {
        try
        {
            FileInputStream fileInputStream = openFileInput(Constants.FAVORITES_FILE_NAME);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            ArrayList<String> favorites = new ArrayList<>();
            String line;

            while ((line = bufferedReader.readLine()) != null)
            {
                favorites.add(line);
            }

            isFavorite = favorites.contains("^%t" + originBusStopName + "^%td" +
                    destinationBusStopName);

            favorites.trimToSize();
            fileInputStream.close();
            inputStreamReader.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (isFavorite)
        {
            favoritesFloatingActionButton.setImageResource(R.drawable.ic_favorite_white);
        }
        else
        {
            favoritesFloatingActionButton.setImageResource(R.drawable.ic_favorite_border_white);
        }
    }

    private void favoriteTrip(String originBusStopName, String destinationBusStopName)
    {
        if (!isFavorite)
        {
            try
            {
                FileOutputStream fileOutputStream = openFileOutput(Constants.FAVORITES_FILE_NAME,
                        MODE_APPEND);

                fileOutputStream.write(("^%t" + originBusStopName + "^%td" + destinationBusStopName +
                        "\n").getBytes());

                Toast.makeText(this, "Added trip to favourites.", Toast.LENGTH_SHORT)
                        .show();

                fileOutputStream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Toast.makeText(this, "Unknown error occurred! Couldn't favourite this trip...",
                        Toast.LENGTH_SHORT).show();
            }

            favoritesFloatingActionButton.setImageResource(R.drawable.ic_favorite_white);
            isFavorite = true;
        }
        else
        {
            try
            {
                FileInputStream fileInputStream = openFileInput(Constants.FAVORITES_FILE_NAME);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream,
                        "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                ArrayList<String> favorites = new ArrayList<>();
                String line;

                while ((line = bufferedReader.readLine()) != null)
                {
                    favorites.add(line);
                }

                favorites.remove("^%t" + originBusStopName + "^%td" + destinationBusStopName);

                Toast.makeText(this, "Removed trip from favourites.", Toast.LENGTH_SHORT)
                        .show();

                favorites.trimToSize();
                fileInputStream.close();
                inputStreamReader.close();

                FileOutputStream fileOutputStream = openFileOutput(Constants.FAVORITES_FILE_NAME,
                        MODE_PRIVATE);
                for (String favorite : favorites)
                {
                    fileOutputStream.write((favorite + "\n").getBytes());
                }
                fileOutputStream.close();

            }
            catch (Exception e)
            {
                e.printStackTrace();
                Toast.makeText(this, "Unknown error occurred! Couldn't un-favourite this trip...",
                        Toast.LENGTH_SHORT).show();
            }

            favoritesFloatingActionButton.setImageResource(R.drawable.ic_favorite_border_white);
            isFavorite = false;
        }
    }

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
