package com.bangalorebuses.tracker;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bangalorebuses.R;
import com.bangalorebuses.core.Bus;
import com.bangalorebuses.core.BusRoute;
import com.bangalorebuses.core.BusStop;
import com.bangalorebuses.utils.Animations;
import com.bangalorebuses.utils.BusETAsOnBusRouteTask;
import com.bangalorebuses.utils.CommonMethods;
import com.bangalorebuses.utils.DbQueries;
import com.bangalorebuses.utils.NetworkingHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.bangalorebuses.utils.Constants.DIRECTION_DOWN;
import static com.bangalorebuses.utils.Constants.DIRECTION_UP;
import static com.bangalorebuses.utils.Constants.NETWORK_QUERY_IO_EXCEPTION;
import static com.bangalorebuses.utils.Constants.NETWORK_QUERY_JSON_EXCEPTION;
import static com.bangalorebuses.utils.Constants.NETWORK_QUERY_NO_ERROR;
import static com.bangalorebuses.utils.Constants.NETWORK_QUERY_REQUEST_TIMEOUT_EXCEPTION;
import static com.bangalorebuses.utils.Constants.NETWORK_QUERY_URL_EXCEPTION;
import static com.bangalorebuses.utils.Constants.db;
import static com.bangalorebuses.utils.Constants.favoritesHashMap;

/**
 * This activity allows the user to track a bus route
 * selected or entered on the ChooseRouteActivity.
 *
 * @author Nihar Thakkar
 * @version 1.0
 * @since 18-6-2017
 */

public class TrackBusActivity extends AppCompatActivity implements NetworkingHelper, AdapterView.OnItemSelectedListener,
        SwipeRefreshLayout.OnRefreshListener
{
    private GetRoutesWithNumberTask getRoutesWithNumberTask;
    private GetStopsOnRouteTask getStopsOnRouteTask;
    private BusETAsOnBusRouteTask busETAsOnBusRouteTask;
    private ListView listView;
    private Spinner spinner;
    private TextView directionTextView;
    private ImageView directionSwapImageView;
    private BusRoute routeUp;
    private BusRoute routeDown;
    private String currentlySelectedDirection = DIRECTION_UP;
    private boolean routeIsOnlyInOneDirection = false;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BusStop currentlySelectedBusStop;
    private LinearLayout errorLinearLayout;
    private ImageView errorImageView;
    private TextView errorMessageTextView;
    private TextView errorResolutionTextView;
    private FloatingActionButton favoritesFloatingActionButton;
    private boolean isFavorite = false;
    private String favoriteRouteStopId;
    private String favoriteRouteDirectionName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_bus);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setTitle(getIntent().getStringExtra(
                    "ROUTE_NUMBER"));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (db == null)
        {
            CommonMethods.initialiseDatabase(this);
        }

        favoritesFloatingActionButton = (FloatingActionButton) findViewById(R.id
                .favorites_floating_action_button);
        favoritesFloatingActionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                favoriteBusRoute();
            }
        });

        listView = (ListView) findViewById(R.id.listView);
        spinner = (Spinner) findViewById(R.id.route_stop_list_spinner);
        directionTextView = (TextView) findViewById(R.id.directionNameTextView);
        directionSwapImageView = (ImageView) findViewById(R.id.changeDirectionImageView);
        errorLinearLayout = (LinearLayout) findViewById(R.id.errorLinearLayout);
        errorImageView = (ImageView) findViewById(R.id.errorImageView);
        errorMessageTextView = (TextView) findViewById(R.id.errorTextView);
        errorResolutionTextView = (TextView) findViewById(R.id.errorResolutionTextView);
        errorResolutionTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                refresh();
            }
        });
        errorLinearLayout.setVisibility(View.GONE);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        // Set the colors to be used for the swipe refresh layout
        swipeRefreshLayout.setColorSchemeResources(R.color.colorNonACBus, R.color.colorACBus,
                R.color.colorMetroFeederBus);

        if (getIntent().getStringExtra("ROUTE_DIRECTION") != null)
        {
            favoriteRouteDirectionName = getIntent().getStringExtra("ROUTE_DIRECTION");
            favoriteRouteStopId = getIntent().getStringExtra("ROUTE_STOP_ID");
        }

        trackBusesOnRoute(getIntent().getStringExtra("ROUTE_NUMBER"));
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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        cancelAllTasks();
        errorLinearLayout.setVisibility(View.GONE);

        if (CommonMethods.checkNetworkConnectivity(this))
        {
            if (currentlySelectedDirection.equals(DIRECTION_UP))
            {
                swipeRefreshLayout.setRefreshing(true);
                currentlySelectedBusStop = routeUp.getBusRouteStops().get(position);
                busETAsOnBusRouteTask = new BusETAsOnBusRouteTask(this, routeUp.getBusRouteStops()
                        .get(position).getBusStopRouteOrder(), routeUp);
                String requestParameters = "routeNO=" + routeUp.getBusRouteNumber() + "&" + "direction=" + DIRECTION_UP;
                busETAsOnBusRouteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestParameters);
            }
            else if (currentlySelectedDirection.equals(DIRECTION_DOWN))
            {
                swipeRefreshLayout.setRefreshing(true);
                currentlySelectedBusStop = routeDown.getBusRouteStops().get(position);
                busETAsOnBusRouteTask = new BusETAsOnBusRouteTask(this, routeDown.getBusRouteStops()
                        .get(position).getBusStopRouteOrder(), routeDown);
                String requestParameters = "routeNO=" + routeDown.getBusRouteNumber() + "&" + "direction=" + DIRECTION_DOWN;
                busETAsOnBusRouteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestParameters);
            }
            else
            {
                Toast.makeText(this, "Please select a direction!", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            swipeRefreshLayout.setRefreshing(false);
            showError(R.drawable.ic_cloud_off_black,
                    R.string.error_message_internet_unavailable, R.string.fix_error_retry);
            errorLinearLayout.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }

        initialiseFavorites();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }

    public void swapDirection(View view)
    {
        if (!routeIsOnlyInOneDirection)
        {
            cancelAllTasks();
            errorLinearLayout.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(true);
            directionSwapImageView.startAnimation(Animations.rotateForwardOnce(this));
            if (currentlySelectedDirection.equals(DIRECTION_UP) && routeDown != null)
            {
                directionTextView.setText(getBusRouteDestinationName(routeDown.getBusRouteDirectionName()));
                currentlySelectedDirection = DIRECTION_DOWN;

                if (routeDown.getBusRouteStops().size() != 0)
                {
                    updateSpinner(DIRECTION_DOWN);
                }
                else
                {
                    getStopsOnRouteTask = new GetStopsOnRouteTask(routeDown);
                    getStopsOnRouteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                            routeDown.getBusRouteId());
                }
            }
            else if (currentlySelectedDirection.equals(DIRECTION_DOWN) && routeUp != null)
            {
                directionTextView.setText(getBusRouteDestinationName(routeUp.getBusRouteDirectionName()));
                currentlySelectedDirection = DIRECTION_UP;

                if (routeUp.getBusRouteStops().size() != 0)
                {
                    updateSpinner(DIRECTION_UP);
                }
                else
                {
                    getStopsOnRouteTask = new GetStopsOnRouteTask(routeUp);
                    getStopsOnRouteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                            routeUp.getBusRouteId());
                }
            }
            else
            {
                Toast.makeText(this, "Loading directions...", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(this, "This bus is only in one direction...", Toast.LENGTH_SHORT).show();
            directionSwapImageView.startAnimation(Animations.rotateForwardOnce(this));
            new CountDownTimer(200, 200)
            {
                @Override
                public void onTick(long millisUntilFinished)
                {

                }

                @Override
                public void onFinish()
                {
                    directionSwapImageView.startAnimation(Animations
                            .rotateBackwardOnce(TrackBusActivity.this));
                }
            }.start();
        }
    }

    private void trackBusesOnRoute(String routeNumber)
    {
        errorLinearLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(true);
        getRoutesWithNumberTask = new GetRoutesWithNumberTask();
        getRoutesWithNumberTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, routeNumber);
    }

    private void initialiseFavorites()
    {
        try
        {
            String key;
            if (currentlySelectedDirection.equals(DIRECTION_UP))
            {
                key = "^%b" + routeUp.getBusRouteNumber() + "^%bd" +
                        getBusRouteDestinationName(routeUp.getBusRouteDirectionName());
            }
            else
            {
                key = "^%b" + routeDown.getBusRouteNumber() + "^%bd" +
                        getBusRouteDestinationName(routeDown.getBusRouteDirectionName());
            }

            isFavorite = favoritesHashMap.containsKey(key) && favoritesHashMap.get(key)
                    .equals(String.valueOf(currentlySelectedBusStop.getBusStopId()));

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

    private void favoriteBusRoute()
    {
        if (!isFavorite)
        {
            if (currentlySelectedDirection.equals(DIRECTION_UP))
            {
                favoritesHashMap.remove("^%b" + routeUp.getBusRouteNumber() + "^%bd" +
                        getBusRouteDestinationName(routeUp.getBusRouteDirectionName()));

                favoritesHashMap.put("^%b" + routeUp.getBusRouteNumber() +
                                "^%bd" + getBusRouteDestinationName(
                        routeUp.getBusRouteDirectionName()),
                        String.valueOf(currentlySelectedBusStop.getBusStopId()));
            }
            else
            {
                favoritesHashMap.remove("^%b" + routeDown.getBusRouteNumber() + "^%bd" +
                        getBusRouteDestinationName(routeDown.getBusRouteDirectionName()));

                favoritesHashMap.put("^%b" + routeDown.getBusRouteNumber() +
                                "^%bd" + getBusRouteDestinationName(
                        routeDown.getBusRouteDirectionName()),
                        String.valueOf(currentlySelectedBusStop.getBusStopId()));
            }

            if (!CommonMethods.writeFavoritesHashMapToFile(this))
            {
                Toast.makeText(this, "Unknown error occurred! Couldn't favourite this Bus...",
                        Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "Added Bus to favourites.", Toast.LENGTH_SHORT)
                        .show();
            }

            favoritesFloatingActionButton.setImageResource(R.drawable.ic_favorite_white);
            isFavorite = true;
        }
        else
        {
            if (currentlySelectedDirection.equals(DIRECTION_UP))
            {
                favoritesHashMap.remove("^%b" + routeUp.getBusRouteNumber() + "^%bd" +
                        getBusRouteDestinationName(routeUp.getBusRouteDirectionName()));
            }
            else
            {
                favoritesHashMap.remove("^%b" + routeDown.getBusRouteNumber() + "^%bd" +
                        getBusRouteDestinationName(routeDown.getBusRouteDirectionName()));
            }

            if (!CommonMethods.writeFavoritesHashMapToFile(this))
            {
                Toast.makeText(this, "Unknown error occurred! Couldn't un-favourite this Bus...",
                        Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "Removed Bus from favourites.", Toast.LENGTH_SHORT)
                        .show();
            }

            favoritesFloatingActionButton.setImageResource(R.drawable.ic_favorite_border_white);
            isFavorite = false;
        }
    }

    @Override
    public void onBusStopsNearbyFound(String errorMessage, ArrayList<BusStop> busStops)
    {

    }

    private void onRoutesWithNumberFound(ArrayList<BusRoute> busRoutes)
    {
        for (BusRoute busRoute : busRoutes)
        {
            if (busRoute.getBusRouteDirection().equals(DIRECTION_UP))
            {
                routeUp = busRoute;
            }
            else if (busRoute.getBusRouteDirection().equals(DIRECTION_DOWN))
            {
                routeDown = busRoute;
            }
        }

        if (favoriteRouteDirectionName != null)
        {
            if (routeUp != null && getBusRouteDestinationName(routeUp
                    .getBusRouteDirectionName()).equals(favoriteRouteDirectionName))
            {
                directionTextView.setText(getBusRouteDestinationName(routeUp.getBusRouteDirectionName()));
                currentlySelectedDirection = DIRECTION_UP;

                if (routeDown == null)
                {
                    routeIsOnlyInOneDirection = true;
                }

                getStopsOnRouteTask = new GetStopsOnRouteTask(routeUp);
                getStopsOnRouteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, routeUp.getBusRouteId());
                return;
            }
            else if (routeDown != null && getBusRouteDestinationName(routeDown
                    .getBusRouteDirectionName()).equals(favoriteRouteDirectionName))
            {
                directionTextView.setText(getBusRouteDestinationName(routeDown.getBusRouteDirectionName()));
                currentlySelectedDirection = DIRECTION_DOWN;

                if (routeUp == null)
                {
                    routeIsOnlyInOneDirection = true;
                }

                getStopsOnRouteTask = new GetStopsOnRouteTask(routeDown);
                getStopsOnRouteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, routeDown.getBusRouteId());
                return;
            }
        }

        if (routeUp != null)
        {

            directionTextView.setText(getBusRouteDestinationName(routeUp.getBusRouteDirectionName()));
            currentlySelectedDirection = DIRECTION_UP;

            if (routeDown == null)
            {
                routeIsOnlyInOneDirection = true;
            }

            getStopsOnRouteTask = new GetStopsOnRouteTask(routeUp);
            getStopsOnRouteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, routeUp.getBusRouteId());
        }

        if (routeDown != null)
        {
            if (routeUp == null)
            {
                directionTextView.setText(getBusRouteDestinationName(routeDown.getBusRouteDirectionName()));
                currentlySelectedDirection = DIRECTION_DOWN;
                routeIsOnlyInOneDirection = true;
                getStopsOnRouteTask = new GetStopsOnRouteTask(routeDown);
                getStopsOnRouteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, routeDown.getBusRouteId());
            }
        }
    }

    private void onBusStopsOnRouteFound(BusRoute busRoute)
    {
        if (busRoute.getBusRouteDirection().equals(DIRECTION_UP))
        {
            routeUp = busRoute;
            updateSpinner(DIRECTION_UP);
        }
        else if (busRoute.getBusRouteDirection().equals(DIRECTION_DOWN))
        {
            routeDown = busRoute;
            updateSpinner(DIRECTION_DOWN);
        }
    }

    private void updateSpinner(String direction)
    {
        ArrayList<String> routeStopNames = new ArrayList<>();
        int favoriteRouteStopItemCount = -1;

        if (direction.equals(DIRECTION_UP))
        {
            if (favoriteRouteStopId != null)
            {
                for (int i = 0; i < routeUp.getBusRouteStops().size(); i++)
                {
                    if (routeUp.getBusRouteStops().get(i).getBusStopId()
                            == Integer.parseInt(favoriteRouteStopId))
                    {
                        favoriteRouteStopItemCount = i;
                        break;
                    }
                }
            }

            for (BusStop routeStop : routeUp.getBusRouteStops())
            {
                routeStopNames.add(routeStop.getBusStopName());
            }
        }
        else if (direction.equals(DIRECTION_DOWN))
        {
            if (favoriteRouteStopId != null)
            {
                for (int i = 0; i < routeDown.getBusRouteStops().size(); i++)
                {
                    if (routeDown.getBusRouteStops().get(i).getBusStopId()
                            == Integer.parseInt(favoriteRouteStopId))
                    {
                        favoriteRouteStopItemCount = i;
                        break;
                    }
                }
            }

            for (BusStop routeStop : routeDown.getBusRouteStops())
            {
                routeStopNames.add(routeStop.getBusStopName());
            }
        }
        else
        {
            Toast.makeText(this, "Please select a direction!", Toast.LENGTH_SHORT).show();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, routeStopNames);
        spinner.setAdapter(adapter);

        if (favoriteRouteStopId != null && favoriteRouteStopItemCount > -1 &&
                favoriteRouteStopItemCount < routeStopNames.size())
        {
            spinner.setSelection(favoriteRouteStopItemCount);
        }
        else
        {
            spinner.setSelection(routeStopNames.size() - 1);
        }

        favoriteRouteStopId = null;
        favoriteRouteDirectionName = null;
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onRefresh()
    {
        refresh();
    }

    @Override
    public void onBusETAsOnBusRouteFound(String errorMessage, int busStopRouteOrder,
                                         ArrayList<Bus> buses, BusRoute busRoute)
    {
        if (errorMessage.equals(NETWORK_QUERY_NO_ERROR))
        {
            if (buses.size() != 0)
            {
                for (Bus bus : buses)
                {
                    bus.setBusETA(CommonMethods.calculateTravelTime(busRoute.getBusRouteId(),
                            busRoute.getBusRouteNumber(), bus.getBusRouteOrder(),
                            busStopRouteOrder));

                    String currentlyNearBusStop = "Unknown";
                    for (BusStop busStop : busRoute.getBusRouteStops())
                    {
                        if (busStop.getBusStopRouteOrder() == bus.getBusRouteOrder())
                        {
                            currentlyNearBusStop = busStop.getBusStopName();
                        }
                    }

                    bus.setBusCurrentlyNearBusStop(currentlyNearBusStop);
                }

                swipeRefreshLayout.setRefreshing(false);
                TrackBusListCustomAdapter trackBusListCustomAdapter = new TrackBusListCustomAdapter(this, buses);
                listView.setAdapter(trackBusListCustomAdapter);
                errorLinearLayout.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
            }
            else
            {
                swipeRefreshLayout.setRefreshing(false);
                showError(R.drawable.ic_directions_bus_black,
                        R.string.error_message_track_bus_no_buses_arriving_soon, R.string.fix_error_no_fix);
                listView.setVisibility(View.GONE);
            }
        }
        else
        {
            if (CommonMethods.checkNetworkConnectivity(this))
            {
                swipeRefreshLayout.setRefreshing(false);
                switch (errorMessage)
                {
                    case NETWORK_QUERY_IO_EXCEPTION:
                    {
                        showError(R.drawable.ic_cloud_off_black,
                                R.string.error_message_io_exception, R.string.fix_error_retry);
                        listView.setVisibility(View.GONE);
                        break;
                    }
                    case NETWORK_QUERY_JSON_EXCEPTION:
                    {
                        showError(R.drawable.ic_directions_bus_black,
                                R.string.error_message_track_bus_no_buses_arriving_soon,
                                R.string.fix_error_no_fix);
                        listView.setVisibility(View.GONE);
                        break;
                    }
                    case NETWORK_QUERY_REQUEST_TIMEOUT_EXCEPTION:
                    {
                        showError(R.drawable.ic_cloud_off_black,
                                R.string.error_message_timeout_exception, R.string.fix_error_retry);
                        listView.setVisibility(View.GONE);
                        break;
                    }
                    case NETWORK_QUERY_URL_EXCEPTION:
                    {
                        showError(R.drawable.ic_sad_face,
                                R.string.error_message_url_exception, R.string.fix_error_retry);
                        listView.setVisibility(View.GONE);
                        break;
                    }
                    default:
                    {
                        showError(R.drawable.ic_sad_face,
                                R.string.error_message_url_exception, R.string.fix_error_retry);
                        listView.setVisibility(View.GONE);
                        break;
                    }
                }
            }
            else
            {
                showError(R.drawable.ic_cloud_off_black,
                        R.string.error_message_internet_unavailable, R.string.fix_error_retry);
                errorLinearLayout.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
            }
        }
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

    private void cancelAllTasks()
    {
        if (getRoutesWithNumberTask != null)
        {
            getRoutesWithNumberTask.cancel(true);
        }

        if (getStopsOnRouteTask != null)
        {
            getStopsOnRouteTask.cancel(true);
        }

        if (busETAsOnBusRouteTask != null)
        {
            busETAsOnBusRouteTask.cancel(true);
        }
    }

    private void refresh()
    {
        cancelAllTasks();
        errorLinearLayout.setVisibility(View.GONE);

        if (CommonMethods.checkNetworkConnectivity(this))
        {
            if (currentlySelectedBusStop != null)
            {
                if (currentlySelectedDirection.equals(DIRECTION_UP))
                {
                    busETAsOnBusRouteTask = new BusETAsOnBusRouteTask(this, currentlySelectedBusStop.getBusStopRouteOrder(), routeUp);
                    swipeRefreshLayout.setRefreshing(true);
                    String requestParameters = "routeNO=" + routeUp.getBusRouteNumber() + "&" + "direction=" + DIRECTION_UP;
                    busETAsOnBusRouteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestParameters);
                }
                else if (currentlySelectedDirection.equals(DIRECTION_DOWN))
                {
                    busETAsOnBusRouteTask = new BusETAsOnBusRouteTask(this, currentlySelectedBusStop.getBusStopRouteOrder(), routeDown);
                    swipeRefreshLayout.setRefreshing(true);
                    String requestParameters = "routeNO=" + routeDown.getBusRouteNumber() + "&" + "direction=" + DIRECTION_DOWN;
                    busETAsOnBusRouteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestParameters);
                }
                else
                {
                    Toast.makeText(this, "Please select a direction!", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else
        {
            swipeRefreshLayout.setRefreshing(false);
            showError(R.drawable.ic_cloud_off_black,
                    R.string.error_message_internet_unavailable, R.string.fix_error_retry);
            errorLinearLayout.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (busETAsOnBusRouteTask != null &&
                busETAsOnBusRouteTask.getStatus().equals(AsyncTask.Status.FINISHED))
        {
            busETAsOnBusRouteTask.cancel(true);
        }
        if (getRoutesWithNumberTask != null &&
                getRoutesWithNumberTask.getStatus().equals(AsyncTask.Status.FINISHED))
        {
            getRoutesWithNumberTask.cancel(true);
        }
        if (getStopsOnRouteTask != null &&
                getStopsOnRouteTask.getStatus().equals(AsyncTask.Status.FINISHED))
        {
            getStopsOnRouteTask.cancel(true);
        }
    }

    private String getBusRouteDestinationName(String directionName)
    {
        String busRouteDestinationName = directionName;
        if (busRouteDestinationName.contains(" To "))
        {
            busRouteDestinationName = busRouteDestinationName.substring(busRouteDestinationName.indexOf("To ") + 3, busRouteDestinationName.length());
        }
        else if (busRouteDestinationName.contains(" to "))
        {
            busRouteDestinationName = busRouteDestinationName.substring(busRouteDestinationName.indexOf("to ") + 3, busRouteDestinationName.length());
        }
        return busRouteDestinationName;
    }

    private class GetRoutesWithNumberTask extends AsyncTask<String, Void, ArrayList<BusRoute>>
    {
        @Override
        protected ArrayList<BusRoute> doInBackground(String... routeNumber)
        {
            return DbQueries.getRoutesWithNumber(db, routeNumber[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<BusRoute> busRoutes)
        {
            super.onPostExecute(busRoutes);
            if (!isCancelled())
            {
                onRoutesWithNumberFound(busRoutes);
            }
        }
    }

    private class GetStopsOnRouteTask extends AsyncTask<Integer, Void, ArrayList<BusStop>>
    {
        private BusRoute busRoute;

        GetStopsOnRouteTask(BusRoute busRoute)
        {
            this.busRoute = busRoute;
        }

        @Override
        protected ArrayList<BusStop> doInBackground(Integer... routeId)
        {
            ArrayList<BusStop> busStops = DbQueries.getStopsOnRoute(db, routeId[0]);
            Collections.sort(busStops, new Comparator<BusStop>()
            {
                @Override
                public int compare(BusStop busStop1, BusStop busStop2)
                {
                    return busStop1.getBusStopRouteOrder() - busStop2.getBusStopRouteOrder();
                }

            });
            return busStops;
        }

        @Override
        protected void onPostExecute(ArrayList<BusStop> busStops)
        {
            super.onPostExecute(busStops);
            busRoute.setBusRouteStops(busStops);
            if (!isCancelled())
            {
                onBusStopsOnRouteFound(busRoute);
            }
        }
    }
}