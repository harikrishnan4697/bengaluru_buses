package com.bangalorebuses.busarrivals;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.bangalorebuses.utils.Constants;
import com.bangalorebuses.utils.DbQueries;
import com.bangalorebuses.utils.DbQueryHelper;
import com.bangalorebuses.utils.NetworkingHelper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import static com.bangalorebuses.utils.Constants.NETWORK_QUERY_NO_ERROR;
import static com.bangalorebuses.utils.Constants.db;

/**
 * @author Nihar Thakkar
 * @version 1.0
 * @since 5-9-2017
 */

public class BusesArrivingAtBusStopActivity extends AppCompatActivity implements NetworkingHelper,
        DbQueryHelper, SwipeRefreshLayout.OnRefreshListener
{
    ArrayList<BusRoute> busRoutesToDisplay = new ArrayList<>();
    ArrayList<BusRoute> busRoutesToGetTimingsOf = new ArrayList<>();
    int numberOfBusRouteTimingQueriesMade = 0;

    private BusStop selectedBusStop = new BusStop();
    private ListView listView;

    private LinearLayout errorLinearLayout;
    private ImageView errorImageView;
    private TextView errorTextView;
    private TextView errorResolutionTextView;

    private int numberOfBusRouteTimingsFound = 0;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean busStopHasTraceableBuses = false;
    private BusRoutesArrivingAtBusStopDbTask busRoutesArrivingAtBusStopDbTask;
    private ArrayList<BusETAsOnBusRouteTask> runningAsyncTasks = new ArrayList<>();
    private boolean isFavorite = false;
    private ImageView favoriteImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buses_arriving_at_bus_stop);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().hide();
        }

        // Initialize some variables
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorOrdinaryServiceBus, R.color.colorACServiceBus,
                R.color.colorSpecialServiceBus);
        swipeRefreshLayout.setOnRefreshListener(this);
        listView = (ListView) findViewById(R.id.busesArrivingAtBusStopListView);
        TextView busStopDirectionInfoTextView = (TextView) findViewById(R.id.busStopDirectionNameTextView);
        errorLinearLayout = (LinearLayout) findViewById(R.id.errorLinearLayout);
        errorImageView = (ImageView) findViewById(R.id.errorImageView);
        errorTextView = (TextView) findViewById(R.id.errorTextView);
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
            selectedBusStop.setBusStopName(busStopName.substring(0, busStopName.length() - 1));
        }
        else
        {
            selectedBusStop.setBusStopName(busStopName);
        }

        String busStopDirectionName = getIntent().getStringExtra("BUS_STOP_DIRECTION_NAME");
        if (busStopDirectionName.contains("(") && busStopDirectionName.contains(")"))
        {
            selectedBusStop.setBusStopDirectionName(busStopDirectionName.substring(busStopDirectionName
                    .indexOf("(") + 1, busStopDirectionName.indexOf(")")));
        }
        else
        {
            selectedBusStop.setBusStopDirectionName(busStopDirectionName);
        }

        TextView titleTextView = (TextView) findViewById(R.id.title_text_view);
        titleTextView.setText(selectedBusStop.getBusStopName());

        ImageView backButtonImageView = (ImageView) findViewById(R.id.back_button_image_view);
        backButtonImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        favoriteImageView = (ImageView) findViewById(R.id.favorite_image_view);
        favoriteImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                favoriteBusStop();
            }
        });

        busStopDirectionInfoTextView.setText(selectedBusStop.getBusStopDirectionName());

        selectedBusStop.setBusStopId(getIntent().getIntExtra("BUS_STOP_ID", 0));

        // Get buses scheduled to arrive at the selected bus stop
        if (isNetworkAvailable())
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
            setErrorLayoutContent(R.drawable.ic_cloud_off_black, "Uh oh! No data connection.", "Retry");
            errorLinearLayout.setVisibility(View.VISIBLE);
        }

        initialiseFavorites();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.buses_arriving_at_bus_stop_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                break;
            case R.id.busesArrivingAtBusStopRefresh:
                if (numberOfBusRouteTimingQueriesMade == busRoutesToGetTimingsOf.size() - 1)
                {
                    errorLinearLayout.setVisibility(View.GONE);
                    if (busRoutesArrivingAtBusStopDbTask != null)
                    {
                        busRoutesArrivingAtBusStopDbTask.cancel(true);
                    }

                    // Get buses scheduled to arrive at the selected bus stop
                    if (isNetworkAvailable())
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
                        setErrorLayoutContent(R.drawable.ic_cloud_off_black, "Uh oh! No data connection.", "Retry");
                        errorLinearLayout.setVisibility(View.VISIBLE);
                    }
                }
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
        if (isNetworkAvailable())
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
            setErrorLayoutContent(R.drawable.ic_cloud_off_black, "Uh oh! No data connection.", "Retry");
            errorLinearLayout.setVisibility(View.VISIBLE);
        }
    }

    private void initialiseFavorites()
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

            isFavorite = favorites.contains("^%s" + selectedBusStop.getBusStopId() + "^%sn" +
                    selectedBusStop.getBusStopName() + "^%sd" + selectedBusStop.getBusStopDirectionName());

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
            favoriteImageView.setImageResource(R.drawable.ic_favorite_white);
        }
        else
        {
            favoriteImageView.setImageResource(R.drawable.ic_favorite_border_white);
        }
    }

    private void favoriteBusStop()
    {
        if (!isFavorite)
        {
            try
            {
                FileOutputStream fileOutputStream = openFileOutput(Constants.FAVORITES_FILE_NAME, MODE_APPEND);

                fileOutputStream.write(("^%s" + selectedBusStop.getBusStopId() + "^%sn" +
                        selectedBusStop.getBusStopName() + "^%sd" + selectedBusStop.getBusStopDirectionName()
                        + "\n").getBytes());

                Toast.makeText(this, "Added " + selectedBusStop.getBusStopName() + " to favourites.", Toast.LENGTH_SHORT)
                        .show();

                fileOutputStream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Toast.makeText(this, "Unknown error occurred! Couldn't favourite this bus stop...", Toast.LENGTH_SHORT).show();
            }

            favoriteImageView.setImageResource(R.drawable.ic_favorite_white);
            isFavorite = true;
        }
        else
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

                favorites.remove("^%s" + selectedBusStop.getBusStopId() + "^%sn" +
                        selectedBusStop.getBusStopName() + "^%sd" + selectedBusStop.getBusStopDirectionName());

                Toast.makeText(this, "Removed " + selectedBusStop.getBusStopName() + " from favourites.", Toast.LENGTH_SHORT)
                        .show();

                favorites.trimToSize();
                fileInputStream.close();
                inputStreamReader.close();

                FileOutputStream fileOutputStream = openFileOutput(Constants.FAVORITES_FILE_NAME, MODE_PRIVATE);
                for (String favorite : favorites)
                {
                    fileOutputStream.write((favorite + "\n").getBytes());
                }
                fileOutputStream.close();

            }
            catch (Exception e)
            {
                e.printStackTrace();
                Toast.makeText(this, "Unknown error occurred! Couldn't un-favourite this bus stop...", Toast.LENGTH_SHORT).show();
            }

            favoriteImageView.setImageResource(R.drawable.ic_favorite_border_white);
            isFavorite = false;
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
            if (isNetworkAvailable())
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
                setErrorLayoutContent(R.drawable.ic_cloud_off_black, "Uh oh! No data connection.", "Retry");
                errorLinearLayout.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            swipeRefreshLayout.setRefreshing(false);
            listView.setVisibility(View.GONE);
            setErrorLayoutContent(R.drawable.ic_directions_bus_black_big, "Oh no! Looks like there aren't any buses arriving at this bus stop any time soon...", "Retry");
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
                        buses.get(i).setBusETA(calculateTravelTime(DbQueries.getNumberOfStopsBetweenRouteOrders(db,
                                route.getBusRouteId(), buses.get(i).getBusRouteOrder(), busStopRouteOrder),
                                route.getBusRouteNumber()));
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
                if (isNetworkAvailable())
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
                    setErrorLayoutContent(R.drawable.ic_cloud_off_black, "Uh oh! No data connection.", "Retry");
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
                setErrorLayoutContent(R.drawable.ic_directions_bus_black_big, "Oh no! Looks like there aren't any buses arriving at this bus stop any time soon...", "Retry");
                errorLinearLayout.setVisibility(View.VISIBLE);
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

    private void setErrorLayoutContent(int drawableResId, String errorMessage, String resolutionButtonText)
    {
        errorImageView.setImageResource(drawableResId);
        errorTextView.setText(errorMessage);
        errorResolutionTextView.setText(resolutionButtonText);
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