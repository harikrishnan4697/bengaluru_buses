package com.bangalorebuses.trips;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bangalorebuses.R;
import com.bangalorebuses.core.Bus;
import com.bangalorebuses.core.BusRoute;
import com.bangalorebuses.utils.DbQueries;
import com.bangalorebuses.utils.ErrorImageResIds;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.bangalorebuses.utils.Constants.DESTINATION_BUS_STOP_NAME;
import static com.bangalorebuses.utils.Constants.NETWORK_QUERY_NO_ERROR;
import static com.bangalorebuses.utils.Constants.NUMBER_OF_ROUTES_TYPE_ORIGIN_TO_TRANSIT_POINT;
import static com.bangalorebuses.utils.Constants.NUMBER_OF_ROUTES_TYPE_TRANSIT_POINT_TO_DESTINATION;
import static com.bangalorebuses.utils.Constants.ORIGIN_BUS_STOP_NAME;
import static com.bangalorebuses.utils.Constants.TRANSIT_POINT_BUS_STOP_NAME;
import static com.bangalorebuses.utils.Constants.db;

public class IndirectTripDetailsActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener, IndirectTripHelper
{
    // Bus stop names
    private String originBusStopName;
    private String transitPointBusStopName;
    private String destinationBusStopName;

    // Tasks
    private BusRoutesToAndFromTransitPointDbTask
            busRoutesToAndFromTransitPointDbTask;

    // Variable for displaying errors
    private LinearLayout errorLinearLayout;
    private ImageView errorImageView;
    private TextView errorTextView;
    private TextView errorResolutionTextView;

    // Views to display the indirect trips
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    private ArrayList<IndirectTrip> indirectTrips = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indirect_trip_details);

        // Initialise the action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialise the back button
        ImageView backButtonImageView = (ImageView) findViewById(R.id.back_button_image_view);
        backButtonImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        errorLinearLayout = (LinearLayout) findViewById(R.id.errorLinearLayout);
        errorImageView = (ImageView) findViewById(R.id.errorImageView);
        errorTextView = (TextView) findViewById(R.id.errorTextView);
        errorResolutionTextView = (TextView) findViewById(R.id.errorResolutionTextView);

        errorResolutionTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                findTrips();
            }
        });

        // Initialise the trip details variables
        originBusStopName = getIntent().getStringExtra(ORIGIN_BUS_STOP_NAME);
        transitPointBusStopName = getIntent().getStringExtra(TRANSIT_POINT_BUS_STOP_NAME);
        destinationBusStopName = getIntent().getStringExtra(DESTINATION_BUS_STOP_NAME);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorOrdinaryServiceBus,
                R.color.colorACServiceBus, R.color.colorSpecialServiceBus);
        swipeRefreshLayout.setOnRefreshListener(this);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        findTrips();
    }

    private void findTrips()
    {
        swipeRefreshLayout.setRefreshing(true);

        if (originBusStopName != null && transitPointBusStopName != null
                && destinationBusStopName != null)
        {
            TransitPoint transitPoint = new TransitPoint();
            transitPoint.setTransitPointName(transitPointBusStopName);

            // Execute the task that will query the db and return a list of bus routes
            // to the transit point and from the transit point.
            busRoutesToAndFromTransitPointDbTask = new BusRoutesToAndFromTransitPointDbTask(this,
                    originBusStopName, transitPoint, destinationBusStopName);
            busRoutesToAndFromTransitPointDbTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else
        {
            setErrorLayoutContent(ErrorImageResIds.ERROR_IMAGE_SOMETHING_WENT_WRONG,
                    "Sorry! Something went wrong. Please try again...", "Retry");
            errorLinearLayout.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void onTransitPointsFound()
    {
        TransitPoint transitPoint = new TransitPoint();
        transitPoint.setTransitPointName(transitPointBusStopName);

        BusRoutesToAndFromTransitPointDbTask task = new BusRoutesToAndFromTransitPointDbTask(this,
                originBusStopName, transitPoint, destinationBusStopName);

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onBusRoutesToAndFromTransitPointFound(TransitPoint transitPoint)
    {
        for (int i = 0; i < 5; i++)
        {
            if (i < transitPoint.getBusRoutesToTransitPoint().size())
            {
                /*BusETAsOnLeg1BusRouteTask task = new BusETAsOnLeg1BusRouteTask(this,
                        transitPoint.getBusRoutesToTransitPoint().get(i), transitPoint);*/

                //task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        /*synchronized (this)
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
        }*/
    }

    @Override
    public void onTransitPointsAndRouteCountOriginToTPFound(ArrayList<TransitPoint> transitPoints)
    {

    }

    @Override
    public void onTransitPointsAndRouteCountTPToDestFound(ArrayList<TransitPoint> transitPoints)
    {

    }

    private void cancelAllTasks()
    {
        busRoutesToAndFromTransitPointDbTask.cancel(true);
    }

    @Override
    public void onRefresh()
    {
        findTrips();
    }

    private void setErrorLayoutContent(int drawableResId, String errorMessage,
                                       String resolutionButtonText)
    {
        errorImageView.setImageResource(drawableResId);
        errorTextView.setText(errorMessage);
        errorResolutionTextView.setText(resolutionButtonText);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        cancelAllTasks();
    }
}