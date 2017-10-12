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
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bangalorebuses.R;
import com.bangalorebuses.core.Bus;
import com.bangalorebuses.core.BusRoute;
import com.bangalorebuses.utils.CommonMethods;
import com.bangalorebuses.utils.Constants;
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
        SwipeRefreshLayout.OnRefreshListener, IndirectTripDetailsHelper
{
    // Bus stop names
    private String originBusStopName;
    private String transitPointBusStopName;
    private String destinationBusStopName;

    // Tasks
    private BusRoutesToAndFromTransitPointDbTask
            busRoutesToAndFromTransitPointDbTask;
    private BusETAsOnLeg1BusRouteTask
            busETAsOnLeg1BusRouteTask;

    // Variable for displaying errors
    private LinearLayout errorLinearLayout;
    private ImageView errorImageView;
    private TextView errorTextView;
    private TextView errorResolutionTextView;

    // Views to display the indirect trips
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    private int numberOfIndirectTripQueriesMade = 0;
    private int numberOfIndirectTripQueriesComplete = 0;
    private ArrayList<IndirectTrip> indirectTripsToDisplay
            = new ArrayList<>();
    IndirectTripsDetailsRecyclerViewAdapter indirectTripsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indirect_trip_details);

        // Initialise the action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setTitle(getIntent()
                    .getStringExtra(Constants.TRANSIT_POINT_BUS_STOP_NAME));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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
        errorLinearLayout.setVisibility(View.GONE);

        // Initialise the trip details variables
        originBusStopName = getIntent().getStringExtra(ORIGIN_BUS_STOP_NAME);
        transitPointBusStopName = getIntent().getStringExtra(TRANSIT_POINT_BUS_STOP_NAME);
        destinationBusStopName = getIntent().getStringExtra(DESTINATION_BUS_STOP_NAME);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorOrdinaryServiceBus,
                R.color.colorACServiceBus, R.color.colorSpecialServiceBus);
        swipeRefreshLayout.setOnRefreshListener(this);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setVisibility(View.GONE);

        findTrips();
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
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void findTrips()
    {
        errorLinearLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        swipeRefreshLayout.setRefreshing(true);

        if (originBusStopName != null && transitPointBusStopName != null
                && destinationBusStopName != null)
        {
            busRoutesToAndFromTransitPointDbTask = new BusRoutesToAndFromTransitPointDbTask(this,
                    originBusStopName, transitPointBusStopName, destinationBusStopName);
            busRoutesToAndFromTransitPointDbTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else
        {
            swipeRefreshLayout.setRefreshing(false);
            setErrorLayoutContent(ErrorImageResIds.ERROR_IMAGE_SOMETHING_WENT_WRONG,
                    "Sorry! Something went wrong. Please try again...", "Retry");
            errorLinearLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBusRoutesToAndFromTransitPointFound(TransitPoint transitPoint)
    {
        indirectTripsToDisplay.clear();
        numberOfIndirectTripQueriesMade = 0;
        numberOfIndirectTripQueriesComplete = 0;

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        indirectTripsAdapter = new
                IndirectTripsDetailsRecyclerViewAdapter(indirectTripsToDisplay);
        recyclerView.setAdapter(indirectTripsAdapter);

        if (transitPoint.getBusRoutesToTransitPoint().size() == 0 ||
                transitPoint.getBusRoutesFromTransitPoint().size() == 0)
        {
            swipeRefreshLayout.setRefreshing(false);
            setErrorLayoutContent(ErrorImageResIds.ERROR_IMAGE_NO_BUSES_IN_SERVICE,
                    "Uh oh! There don't seem to be any trips via " +
                            transitPointBusStopName + " right now.", "Retry");
            errorLinearLayout.setVisibility(View.VISIBLE);
            return;
        }

        for (int i = 0; i < 5; i++)
        {
            if (i < transitPoint.getBusRoutesToTransitPoint().size())
            {
                busETAsOnLeg1BusRouteTask = new BusETAsOnLeg1BusRouteTask(this,
                        transitPoint, transitPoint.getBusRoutesToTransitPoint().get(i));
                busETAsOnLeg1BusRouteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                numberOfIndirectTripQueriesMade++;
            }
            else
            {
                break;
            }
        }
    }

    @Override
    public void onBusETAsOnLeg1BusRouteFound(String errorMessage, BusRoute busRoute,
                                             TransitPoint transitPoint)
    {
        numberOfIndirectTripQueriesComplete++;

        if (errorMessage.equals(Constants.NETWORK_QUERY_NO_ERROR))
        {
            if (busRoute.getBusRouteBuses().size() != 0)
            {
                IndirectTrip indirectTrip = new IndirectTrip();
                indirectTrip.setTransitPoint(transitPoint);
                indirectTrip.setBusRouteOnFirstLeg(busRoute);
                indirectTrip.setOriginBusStop(busRoute
                        .getTripPlannerOriginBusStop());
                indirectTrip.setDestinationBusStop(busRoute
                        .getTripPlannerDestinationBusStop());

                int routeOrderOfFastestBusOnFirstLeg = busRoute
                        .getBusRouteBuses().get(0).getBusRouteOrder();
                int routeOrderOfTransitPointOnFirstLeg = busRoute
                        .getTripPlannerDestinationBusStop().getBusStopRouteOrder();

                // Set how long the next bus will take to get to the transit point
                busRoute.getBusRouteBuses().get(0).setBusETAToTransitPoint(CommonMethods
                        .calculateTravelTime(busRoute.getBusRouteId(), busRoute
                                        .getBusRouteNumber(), routeOrderOfFastestBusOnFirstLeg,
                                routeOrderOfTransitPointOnFirstLeg));

                indirectTrip.setBusOnFirstLeg(busRoute.getBusRouteBuses().get(0));
                indirectTrip.setBusOnSecondLeg(selectBestBusOnSecondLeg(transitPoint, busRoute
                        .getBusRouteBuses().get(0)));

                if (indirectTrip.getBusOnFirstLeg() != null &&
                        indirectTrip.getBusOnSecondLeg() != null)
                {
                    indirectTripsToDisplay.add(setIndirectTripTravelTime(indirectTrip));
                    sortIndirectTripsToDisplay();

                    indirectTripsAdapter.notifyDataSetChanged();
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        }

        synchronized (this)
        {
            if (numberOfIndirectTripQueriesComplete == numberOfIndirectTripQueriesMade)
            {
                swipeRefreshLayout.setRefreshing(false);

                if (indirectTripsToDisplay.size() == 0)
                {
                    setErrorLayoutContent(R.drawable.ic_directions_bus_black,
                            "Oh no! There aren't any trips right now...", "Retry");
                    errorLinearLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            }
        }
    }

    private Bus selectBestBusOnSecondLeg(TransitPoint transitPoint,
                                         Bus bestBusOnFirstLeg)
    {
        // Iterate through all the buses on the second leg to determine the best one
        Bus selectedBusFromTP = null;
        for (BusRoute busRouteFromTP : transitPoint.getBusRoutesFromTransitPoint())
        {
            for (Bus busFromTP : busRouteFromTP.getBusRouteBuses())
            {
                if (busFromTP.getBusETA() > (bestBusOnFirstLeg
                        .getBusETAToTransitPoint() + 2))
                {
                    if (selectedBusFromTP != null)
                    {
                        if (busFromTP.getBusETA() <
                                selectedBusFromTP.getBusETA())
                        {
                            selectedBusFromTP = busFromTP;
                        }
                    }
                    else
                    {
                        selectedBusFromTP = busFromTP;
                    }
                }
            }
        }

        return selectedBusFromTP;
    }

    private IndirectTrip setIndirectTripTravelTime(IndirectTrip
                                                           indirectTrip)
    {
        BusRoute busRouteOnSecondLeg = indirectTrip.getBusOnSecondLeg().getBusRoute();

        int travelTimeFromTPToDest = CommonMethods.calculateTravelTime(
                busRouteOnSecondLeg.getBusRouteId(), busRouteOnSecondLeg
                        .getBusRouteNumber(), busRouteOnSecondLeg
                        .getTripPlannerOriginBusStop().getBusStopRouteOrder(),
                busRouteOnSecondLeg.getTripPlannerDestinationBusStop()
                        .getBusStopRouteOrder());

        indirectTrip.setTripDuration(indirectTrip.getBusOnFirstLeg().getBusETAToTransitPoint() +
                (indirectTrip.getBusOnSecondLeg().getBusETA() - indirectTrip.getBusOnFirstLeg()
                        .getBusETAToTransitPoint()) + travelTimeFromTPToDest);

        return indirectTrip;
    }

    private void sortIndirectTripsToDisplay()
    {
        Collections.sort(indirectTripsToDisplay, new Comparator<IndirectTrip>()
        {
            @Override
            public int compare(IndirectTrip i1, IndirectTrip i2)
            {
                return i1.getTripDuration() - i2.getTripDuration();
            }
        });
    }

    private void cancelAllTasks()
    {
        if (busRoutesToAndFromTransitPointDbTask != null)
        {
            busRoutesToAndFromTransitPointDbTask.cancel(true);
        }

        if (busETAsOnLeg1BusRouteTask != null)
        {
            busETAsOnLeg1BusRouteTask.cancel(true);
        }
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