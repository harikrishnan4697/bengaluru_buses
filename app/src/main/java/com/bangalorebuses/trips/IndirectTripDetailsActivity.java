package com.bangalorebuses.trips;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bangalorebuses.R;
import com.bangalorebuses.core.Bus;
import com.bangalorebuses.core.BusRoute;
import com.bangalorebuses.core.BusStop;
import com.bangalorebuses.utils.CommonMethods;
import com.bangalorebuses.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.bangalorebuses.utils.Constants.DESTINATION_BUS_STOP_NAME;
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
    private IndirectTripsDbTask indirectTripsDbTask;
    private ArrayList<BusETAsOnLeg1BusRouteTask> busETAsOnLeg1BusRouteTasks =
            new ArrayList<>();

    // Variable for displaying errors
    private LinearLayout errorLinearLayout;
    private ImageView errorImageView;
    private TextView errorMessageTextView;
    private TextView errorResolutionTextView;

    // Views to display the indirect trips
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    private int numberOfIndirectTripQueriesMade = 0;
    private int numberOfIndirectTripQueriesComplete = 0;
    private ArrayList<IndirectTrip> indirectTripsToDisplay
            = new ArrayList<>();
    private IndirectTripDetailsRecyclerViewAdapter indirectTripsAdapter;

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
            getSupportActionBar().setTitle(R.string.indirect_trip_details_title);

            getSupportActionBar().setSubtitle("Via " + getIntent()
                    .getStringExtra(Constants.TRANSIT_POINT_BUS_STOP_NAME));

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (db == null)
        {
            CommonMethods.initialiseDatabase(this);
        }

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        errorLinearLayout = (LinearLayout) findViewById(R.id.errorLinearLayout);
        errorImageView = (ImageView) findViewById(R.id.errorImageView);
        errorMessageTextView = (TextView) findViewById(R.id.errorTextView);
        errorResolutionTextView = (TextView) findViewById(R.id.errorResolutionTextView);

        errorResolutionTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                findIndirectTrips();
            }
        });
        errorLinearLayout.setVisibility(View.GONE);

        // Initialise the trip details variables
        originBusStopName = getIntent().getStringExtra(ORIGIN_BUS_STOP_NAME);
        transitPointBusStopName = getIntent().getStringExtra(TRANSIT_POINT_BUS_STOP_NAME);
        destinationBusStopName = getIntent().getStringExtra(DESTINATION_BUS_STOP_NAME);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorNonACBus,
                R.color.colorACBus, R.color.colorMetroFeederBus);
        swipeRefreshLayout.setOnRefreshListener(this);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setVisibility(View.GONE);

        findIndirectTrips();
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

    private void findIndirectTrips()
    {
        errorLinearLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        swipeRefreshLayout.setRefreshing(true);

        if (originBusStopName != null && transitPointBusStopName != null
                && destinationBusStopName != null)
        {
            indirectTripsDbTask = new IndirectTripsDbTask(this, originBusStopName,
                    transitPointBusStopName, destinationBusStopName);
            indirectTripsDbTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else
        {
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setVisibility(View.GONE);
            showError(R.drawable.ic_sad_face,
                    R.string.error_message_url_exception, R.string.fix_error_no_fix);
        }
    }

    @Override
    public void onIndirectTripsFound(ArrayList<IndirectTrip> indirectTrips)
    {
        indirectTripsToDisplay.clear();
        numberOfIndirectTripQueriesMade = 0;
        numberOfIndirectTripQueriesComplete = 0;
        busETAsOnLeg1BusRouteTasks.clear();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        indirectTripsAdapter = new
                IndirectTripDetailsRecyclerViewAdapter(indirectTripsToDisplay);
        recyclerView.setAdapter(indirectTripsAdapter);

        if (indirectTrips.size() == 0)
        {
            swipeRefreshLayout.setRefreshing(false);
            showStringError(R.drawable.ic_directions_bus_black_big,
                    "There aren't any Indirect Trips via " +
                            transitPointBusStopName + " right now.",
                    R.string.fix_error_no_fix);
            errorLinearLayout.setVisibility(View.VISIBLE);
            return;
        }

        for (IndirectTrip indirectTrip : indirectTrips)
        {
            BusETAsOnLeg1BusRouteTask task = new BusETAsOnLeg1BusRouteTask(this,
                    indirectTrip);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            busETAsOnLeg1BusRouteTasks.add(task);
            numberOfIndirectTripQueriesMade++;
        }
    }

    @Override
    public void onBusETAsOnLeg1BusRouteFound(String errorMessage,
                                             IndirectTrip indirectTrip)
    {
        numberOfIndirectTripQueriesComplete++;

        if (errorMessage.equals(Constants.NETWORK_QUERY_NO_ERROR))
        {
            if (indirectTrip.getDirectTripOnFirstLeg().getBusRoute()
                    .getBusRouteBuses().size() != 0)
            {
                BusRoute busRouteOnFirstLeg = indirectTrip.getDirectTripOnFirstLeg()
                        .getBusRoute();

                Bus busOnFirstLeg = busRouteOnFirstLeg.getBusRouteBuses().get(0);

                BusStop originBusStop = indirectTrip.getDirectTripOnFirstLeg()
                        .getOriginBusStop();

                BusStop transitPointBusStop = indirectTrip.getDirectTripOnFirstLeg()
                        .getDestinationBusStop();

                int travelTimeOnFirstLeg = CommonMethods.calculateTravelTime(busRouteOnFirstLeg
                        .getBusRouteId(), busRouteOnFirstLeg.getBusRouteNumber(), originBusStop
                        .getBusStopRouteOrder(), transitPointBusStop.getBusStopRouteOrder());

                indirectTrip.getDirectTripOnFirstLeg().setTripDuration(busOnFirstLeg
                        .getBusETA() + travelTimeOnFirstLeg);

                indirectTrip.setDirectTripOnSecondLeg(selectBestDirectTripOnSecondLeg(
                        indirectTrip));

                if (indirectTrip.getDirectTripOnSecondLeg() != null)
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
                    showStringError(R.drawable.ic_directions_bus_black_big,
                            "There aren't any Indirect Trips via " +
                                    transitPointBusStopName + " right now.",
                            R.string.fix_error_no_fix);
                }
            }
        }
    }

    private DirectTrip selectBestDirectTripOnSecondLeg(IndirectTrip indirectTrip)
    {
        DirectTrip directTripOnFirstLeg = indirectTrip
                .getDirectTripOnFirstLeg();

        DirectTrip bestDirectTripOnSecondLeg = null;

        Bus bestBusOnSecondLeg = null;

        for (DirectTrip directTripOnSecondLeg :
                indirectTrip.getPossibleDirectTripsOnSecondLeg())
        {
            for (Bus busOnSecondLeg : directTripOnSecondLeg.getBusRoute()
                    .getBusRouteBuses())
            {
                if (busOnSecondLeg.getBusETA() > (directTripOnFirstLeg
                        .getTripDuration() + 2))
                {
                    if (bestBusOnSecondLeg != null)
                    {
                        if (busOnSecondLeg.getBusETA() <
                                bestBusOnSecondLeg.getBusETA())
                        {
                            bestBusOnSecondLeg = busOnSecondLeg;
                            bestDirectTripOnSecondLeg =
                                    directTripOnSecondLeg;
                        }
                    }
                    else
                    {
                        bestBusOnSecondLeg = busOnSecondLeg;
                        bestDirectTripOnSecondLeg =
                                directTripOnSecondLeg;
                    }
                }
            }
        }

        if (bestDirectTripOnSecondLeg != null)
        {
            ArrayList<Bus> busesOnBestDirectTripOnSecondLeg = new ArrayList<>();
            busesOnBestDirectTripOnSecondLeg.add(bestBusOnSecondLeg);

            bestDirectTripOnSecondLeg.getBusRoute().setBusRouteBuses(
                    busesOnBestDirectTripOnSecondLeg);

            BusRoute busRouteOnSecondLeg = bestDirectTripOnSecondLeg.getBusRoute();

            BusStop transitPointBusStop = bestDirectTripOnSecondLeg.getOriginBusStop();

            BusStop destinationBusStop = bestDirectTripOnSecondLeg.getDestinationBusStop();

            int travelTimeOnSecondLeg = CommonMethods.calculateTravelTime(busRouteOnSecondLeg
                    .getBusRouteId(), busRouteOnSecondLeg.getBusRouteNumber(), transitPointBusStop
                    .getBusStopRouteOrder(), destinationBusStop.getBusStopRouteOrder());

            bestDirectTripOnSecondLeg.setTripDuration(bestBusOnSecondLeg
                    .getBusETA() + travelTimeOnSecondLeg);
        }

        return bestDirectTripOnSecondLeg;
    }

    private IndirectTrip setIndirectTripTravelTime(IndirectTrip
                                                           indirectTrip)
    {
        DirectTrip directTripOnFirstLeg = indirectTrip.getDirectTripOnFirstLeg();
        DirectTrip directTripOnSecondLeg = indirectTrip.getDirectTripOnSecondLeg();
        Bus busOnSecondLeg = directTripOnSecondLeg.getBusRoute().getBusRouteBuses()
                .get(0);

        indirectTrip.setTripDuration(directTripOnFirstLeg.getTripDuration() + (
                busOnSecondLeg.getBusETA() - directTripOnFirstLeg.getTripDuration())
                + (directTripOnSecondLeg.getTripDuration() - busOnSecondLeg.getBusETA()));

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
        if (indirectTripsDbTask != null)
        {
            indirectTripsDbTask.cancel(true);
        }

        for (BusETAsOnLeg1BusRouteTask task : busETAsOnLeg1BusRouteTasks)
        {
            if (task != null)
            {
                task.cancel(true);
            }
        }
    }

    @Override
    public void onRefresh()
    {
        findIndirectTrips();
    }

    private void showError(int drawableResId, int errorMessageStringResId,
                           int resolutionButtonStringResId)
    {
        swipeRefreshLayout.setRefreshing(false);
        errorImageView.setImageResource(drawableResId);
        errorMessageTextView.setText(errorMessageStringResId);
        errorResolutionTextView.setText(resolutionButtonStringResId);
        errorLinearLayout.setVisibility(View.VISIBLE);
    }

    private void showStringError(int drawableResId, String errorMessage,
                                 int resolutionButtonStringResId)
    {
        swipeRefreshLayout.setRefreshing(false);
        errorImageView.setImageResource(drawableResId);
        errorMessageTextView.setText(errorMessage);
        errorResolutionTextView.setText(resolutionButtonStringResId);
        errorLinearLayout.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        cancelAllTasks();
    }
}