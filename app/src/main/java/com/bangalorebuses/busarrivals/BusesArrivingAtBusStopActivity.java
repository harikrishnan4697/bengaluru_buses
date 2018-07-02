package com.bangalorebuses.busarrivals;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bangalorebuses.R;
import com.bangalorebuses.core.Bus;
import com.bangalorebuses.core.BusRoute;
import com.bangalorebuses.core.BusStop;
import com.bangalorebuses.utils.BusETAsOnBusRouteTask;
import com.bangalorebuses.utils.CommonMethods;
import com.bangalorebuses.utils.NetworkingHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.bangalorebuses.utils.Constants.NETWORK_QUERY_NO_ERROR;
import static com.bangalorebuses.utils.Constants.db;
import static com.bangalorebuses.utils.Constants.favoritesHashMap;

/**
 * @author Nihar Thakkar
 * @version 1.0
 * @since 5-9-2017
 */

public class BusesArrivingAtBusStopActivity extends AppCompatActivity implements NetworkingHelper,
        BusesArrivingAtBusStopHelper, SwipeRefreshLayout.OnRefreshListener
{
    ArrayList<BusRoute> busRoutesToDisplay = new ArrayList<>();
    ArrayList<BusRoute> busRoutesToGetTimingsOf = new ArrayList<>();
    int numberOfBusRouteTimingQueriesMade = 0;

    private BusStop selectedBusStop = new BusStop();
    private ListView listView;

    private LinearLayout errorLinearLayout;
    private ImageView errorImageView;
    private TextView errorMessageTextView;
    private TextView errorResolutionTextView;

    private int numberOfBusRouteTimingsFound = 0;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean busStopHasTraceableBuses = false;
    private BusRoutesArrivingAtBusStopDbTask busRoutesArrivingAtBusStopDbTask;
    private ArrayList<BusETAsOnBusRouteTask> runningAsyncTasks = new ArrayList<>();
    private boolean isFavorite = false;
    private FloatingActionButton favoriteFloatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buses_arriving_at_bus_stop);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        favoriteFloatingActionButton = (FloatingActionButton) findViewById(R.id
                .favorites_floating_action_button);
        favoriteFloatingActionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                favoriteBusStop();
            }
        });

        if (db == null)
        {
            CommonMethods.initialiseDatabase(this);
        }

        // Initialize some variables
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorNonACBus, R.color.colorACBus,
                R.color.colorMetroFeederBus);
        swipeRefreshLayout.setOnRefreshListener(this);
        listView = (ListView) findViewById(R.id.busesArrivingAtBusStopListView);

        errorLinearLayout = (LinearLayout) findViewById(R.id.errorLinearLayout);
        errorImageView = (ImageView) findViewById(R.id.errorImageView);
        errorMessageTextView = (TextView) findViewById(R.id.errorTextView);
        errorResolutionTextView = (TextView) findViewById(R.id.errorResolutionTextView);
        errorResolutionTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fixError();
            }
        });
        errorLinearLayout.setVisibility(View.GONE);

        String busStopName = getIntent().getStringExtra("BUS_STOP_NAME");
        if (busStopName.substring(busStopName.length() - 1, busStopName.length()).equals(" "))
        {
            busStopName = busStopName.substring(0, busStopName.length() - 1);
        }

        selectedBusStop.setBusStopName(busStopName);

        String busStopDirectionName = getIntent().getStringExtra("BUS_STOP_DIRECTION_NAME");
        if (busStopDirectionName.contains("(") && busStopDirectionName.contains(")"))
        {
            busStopDirectionName = busStopDirectionName.substring(busStopDirectionName
                    .indexOf("(") + 1, busStopDirectionName.indexOf(")"));
        }

        selectedBusStop.setBusStopDirectionName(busStopDirectionName);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(busStopName);
            getSupportActionBar().setSubtitle(busStopDirectionName);
        }

        selectedBusStop.setBusStopId(getIntent().getIntExtra("BUS_STOP_ID", 0));

        initialiseFavorites();

        // Get buses scheduled to arrive at the selected bus stop
        if (CommonMethods.checkNetworkConnectivity(this))
        {
            swipeRefreshLayout.setRefreshing(true);
            busStopHasTraceableBuses = false;
            busRoutesArrivingAtBusStopDbTask = new BusRoutesArrivingAtBusStopDbTask(this);
            busRoutesArrivingAtBusStopDbTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                    selectedBusStop.getBusStopId());
        }
        else
        {
            swipeRefreshLayout.setRefreshing(false);
            listView.setVisibility(View.GONE);
            showError(R.drawable.ic_cloud_off_black,
                    R.string.error_message_internet_unavailable,
                    R.string.fix_error_retry);
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
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onRefresh()
    {
        fixError();
    }

    public void fixError()
    {
        errorLinearLayout.setVisibility(View.GONE);
        if (busRoutesArrivingAtBusStopDbTask != null)
        {
            busRoutesArrivingAtBusStopDbTask.cancel(true);
        }

        // Get buses scheduled to arrive at the selected bus stop
        if (CommonMethods.checkNetworkConnectivity(this))
        {

            swipeRefreshLayout.setRefreshing(true);
            busStopHasTraceableBuses = false;
            busRoutesArrivingAtBusStopDbTask = new BusRoutesArrivingAtBusStopDbTask(this);
            busRoutesArrivingAtBusStopDbTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, selectedBusStop.getBusStopId());
        }
        else
        {
            swipeRefreshLayout.setRefreshing(false);
            listView.setVisibility(View.GONE);
            showError(R.drawable.ic_cloud_off_black,
                    R.string.error_message_internet_unavailable,
                    R.string.fix_error_retry);
        }
    }

    private void initialiseFavorites()
    {
        isFavorite = favoritesHashMap.containsKey("^%s" + selectedBusStop
                .getBusStopName() + "^%sd" + selectedBusStop.getBusStopDirectionName());

        if (isFavorite)
        {
            favoriteFloatingActionButton.setImageResource(R.drawable.ic_favorite_white);
        }
        else
        {
            favoriteFloatingActionButton.setImageResource(R.drawable.ic_favorite_border_white);
        }
    }

    private void favoriteBusStop()
    {
        if (!isFavorite)
        {
            favoritesHashMap.put("^%s" + selectedBusStop.getBusStopName() +
                            "^%sd" + selectedBusStop.getBusStopDirectionName(),
                    String.valueOf(selectedBusStop.getBusStopId()));

            if (!CommonMethods.writeFavoritesHashMapToFile(this))
            {
                Toast.makeText(this, "Unknown error occurred! Couldn't favourite this Bus Stop...",
                        Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "Added Bus Stop to favourites.", Toast.LENGTH_SHORT)
                        .show();
            }

            favoriteFloatingActionButton.setImageResource(R.drawable.ic_favorite_white);
            isFavorite = true;
        }
        else
        {
            favoritesHashMap.remove("^%s" + selectedBusStop.getBusStopName() +
                    "^%sd" + selectedBusStop.getBusStopDirectionName());

            if (!CommonMethods.writeFavoritesHashMapToFile(this))
            {
                Toast.makeText(this, "Unknown error occurred! Couldn't un-favourite this Bus Stop...",
                        Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "Removed Bus Stop from favourites.", Toast.LENGTH_SHORT)
                        .show();
            }

            favoriteFloatingActionButton.setImageResource(R.drawable.ic_favorite_border_white);
            isFavorite = false;
        }
    }

    @Override
    public void onBusStopsNearbyFound(String errorMessage, ArrayList<BusStop> busStops)
    {

    }

    @Override
    public void onBusRoutesArrivingAtBusStopFound(ArrayList<BusRoute> busRoutes)
    {
        numberOfBusRouteTimingQueriesMade = 0;
        busRoutesToGetTimingsOf.clear();
        busRoutesToGetTimingsOf = busRoutes;
        busRoutesToDisplay.clear();
        numberOfBusRouteTimingsFound = 0;
        if (busRoutes.size() != 0)
        {
            if (CommonMethods.checkNetworkConnectivity(this))
            {
                for (; numberOfBusRouteTimingQueriesMade < 10; numberOfBusRouteTimingQueriesMade++)
                {
                    if (numberOfBusRouteTimingQueriesMade < busRoutesToGetTimingsOf.size())
                    {
                        BusStop busStop = new BusStop();
                        busStop.setBusStopRouteOrder(busRoutesToGetTimingsOf.get(numberOfBusRouteTimingQueriesMade).getSelectedBusStopRouteOrder());
                        String requestBody = "routeNO=" + busRoutesToGetTimingsOf.get(numberOfBusRouteTimingQueriesMade).getBusRouteNumber() + "&" + "direction=" + busRoutesToGetTimingsOf.get(numberOfBusRouteTimingQueriesMade).getBusRouteDirection();
                        BusETAsOnBusRouteTask busETAsOnBusRouteTask = new BusETAsOnBusRouteTask(this, busStop.getBusStopRouteOrder(), busRoutesToGetTimingsOf.get(numberOfBusRouteTimingQueriesMade));
                        busETAsOnBusRouteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestBody);
                        runningAsyncTasks.add(busETAsOnBusRouteTask);
                    }
                    else
                    {
                        break;
                    }
                }
            }
            else
            {
                swipeRefreshLayout.setRefreshing(false);
                listView.setVisibility(View.GONE);
                showError(R.drawable.ic_cloud_off_black,
                        R.string.error_message_internet_unavailable, R.string.fix_error_retry);
                errorLinearLayout.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            swipeRefreshLayout.setRefreshing(false);
            listView.setVisibility(View.GONE);
            showError(R.drawable.ic_directions_bus_black_big,
                    R.string.error_message_no_buses_arriving_soon, R.string.fix_error_no_fix);
            errorLinearLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBusETAsOnBusRouteFound(String errorMessage, int busStopRouteOrder, ArrayList<Bus> buses, BusRoute route)
    {
        numberOfBusRouteTimingsFound++;
        if (errorMessage.equals(NETWORK_QUERY_NO_ERROR))
        {
            if (buses.size() != 0)
            {
                ArrayList<Bus> busesOnRoute = new ArrayList<>();
                for (int i = 0; i < 3; i++)
                {
                    if (i >= buses.size())
                    {
                        break;
                    }

                    if (buses.get(i).isDue())
                    {
                        buses.get(i).setBusETA(0);
                        buses.get(i).setDue(true);
                    }
                    else if (buses.get(i).getBusRouteOrder() == 1)
                    {
                        break;
                    }
                    else
                    {
                        buses.get(i).setBusETA(CommonMethods.calculateTravelTime(
                                route.getBusRouteId(), route.getBusRouteNumber(),
                                buses.get(i).getBusRouteOrder(), busStopRouteOrder));
                    }
                    busesOnRoute.add(buses.get(i));
                }
                if (busesOnRoute.size() != 0)
                {
                    busStopHasTraceableBuses = true;
                    route.setBusRouteBuses(busesOnRoute);
                    busRoutesToDisplay.add(route);
                    Collections.sort(busRoutesToDisplay, new Comparator<BusRoute>()
                    {
                        @Override
                        public int compare(BusRoute busRoute1, BusRoute busRoute2)
                        {
                            return busRoute1.getBusRouteBuses().get(0).getBusETA() - busRoute2.getBusRouteBuses().get(0).getBusETA();
                        }

                    });
                    BusesArrivingAtBusStopListCustomAdapter customAdapter = new BusesArrivingAtBusStopListCustomAdapter(this, busRoutesToDisplay);
                    customAdapter.notifyDataSetChanged();
                    listView.setAdapter(customAdapter);
                    listView.setVisibility(View.VISIBLE);
                }
            }
        }

        synchronized (this)
        {
            if (numberOfBusRouteTimingQueriesMade < busRoutesToGetTimingsOf.size())
            {
                if (CommonMethods.checkNetworkConnectivity(this))
                {
                    BusStop busStop = new BusStop();
                    busStop.setBusStopRouteOrder(busRoutesToGetTimingsOf.get(numberOfBusRouteTimingQueriesMade).getSelectedBusStopRouteOrder());
                    String requestBody = "routeNO=" + busRoutesToGetTimingsOf.get(numberOfBusRouteTimingQueriesMade).getBusRouteNumber() + "&" + "direction=" + busRoutesToGetTimingsOf.get(numberOfBusRouteTimingQueriesMade).getBusRouteDirection();
                    BusETAsOnBusRouteTask busETAsOnBusRouteTask = new BusETAsOnBusRouteTask(this, busStop.getBusStopRouteOrder(), busRoutesToGetTimingsOf.get(numberOfBusRouteTimingQueriesMade));
                    busETAsOnBusRouteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestBody);
                    runningAsyncTasks.add(busETAsOnBusRouteTask);
                    numberOfBusRouteTimingQueriesMade++;
                }
                else
                {
                    swipeRefreshLayout.setRefreshing(false);
                    listView.setVisibility(View.GONE);
                    showError(R.drawable.ic_cloud_off_black,
                            R.string.error_message_internet_unavailable, R.string.fix_error_retry);
                    errorLinearLayout.setVisibility(View.VISIBLE);
                }
            }
        }

        // Check if all the bus timings have been calculated
        if (numberOfBusRouteTimingsFound == numberOfBusRouteTimingQueriesMade)
        {
            swipeRefreshLayout.setRefreshing(false);
            if (!busStopHasTraceableBuses)
            {
                listView.setVisibility(View.GONE);
                showError(R.drawable.ic_directions_bus_black_big,
                        R.string.error_message_no_buses_arriving_soon, R.string.fix_error_no_fix);
                errorLinearLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showError(int drawableResId, int errorMessageStringResId, int resolutionButtonStringResId)
    {
        swipeRefreshLayout.setRefreshing(false);
        errorImageView.setImageResource(drawableResId);
        errorMessageTextView.setText(errorMessageStringResId);
        errorResolutionTextView.setText(resolutionButtonStringResId);
        errorLinearLayout.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (busRoutesArrivingAtBusStopDbTask != null)
        {
            busRoutesArrivingAtBusStopDbTask.cancel(true);
        }

        for (BusETAsOnBusRouteTask busETAsOnBusRouteTask : runningAsyncTasks)
        {
            if (busETAsOnBusRouteTask != null)
            {
                busETAsOnBusRouteTask.cancel(true);
            }
        }
    }
}