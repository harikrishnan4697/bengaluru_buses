package com.bangalorebuses.tracker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.bangalorebuses.utils.BusETAsOnBusRouteTask;
import com.bangalorebuses.utils.DbQueries;
import com.bangalorebuses.utils.NetworkingHelper;

import java.util.ArrayList;
import java.util.Calendar;
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

/**
 * This activity allows the user to track a bus route
 * selected or entered on the ChooseRouteActivity.
 *
 * @author Nihar Thakkar
 * @version 1.0
 * @since 18-6-2017
 */

public class TrackBusActivity extends AppCompatActivity implements NetworkingHelper, AdapterView.OnItemSelectedListener
{
    private GetRoutesWithNumberTask getRoutesWithNumberTask;
    private GetStopsOnRouteTask getStopsOnRouteTask;
    private BusETAsOnBusRouteTask busETAsOnBusRouteTask;
    private ListView listView;
    private Spinner spinner;
    private TextView directionTextView;
    private ImageView directionSwapImageView;
    private Animation directionSwapAnimation;
    private Animation directionSwapAnimationBackwards;
    private BusRoute routeUp;
    private BusRoute routeDown;
    private String currentlySelectedDirection = DIRECTION_UP;
    private boolean routeIsOnlyInOneDirection = false;
    //private ProgressDialog progressDialog;
    private BusStop currentlySelectedBusStop;
    private LinearLayout errorLinearLayout;
    private ImageView errorImageView;
    private TextView errorTextView;
    private TextView errorResolutionTextView;
    private LinearLayout updatingLinearLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setTitle("Tracking " + getIntent().getStringExtra("ROUTE_NUMBER"));
        }
        setContentView(R.layout.activity_track_bus);

        // Initialise XML elements
        listView = (ListView) findViewById(R.id.listView);
        spinner = (Spinner) findViewById(R.id.route_stop_list_spinner);
        directionTextView = (TextView) findViewById(R.id.directionNameTextView);
        directionSwapImageView = (ImageView) findViewById(R.id.changeDirectionImageView);
        directionSwapAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_once_forward);
        directionSwapAnimationBackwards = AnimationUtils.loadAnimation(this, R.anim.rotate_once_backwards);
        errorLinearLayout = (LinearLayout) findViewById(R.id.errorLinearLayout);
        errorImageView = (ImageView) findViewById(R.id.errorImageView);
        errorTextView = (TextView) findViewById(R.id.errorTextView);
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
        updatingLinearLayout = (LinearLayout) findViewById(R.id.updatingLinearLayout);
        updatingLinearLayout.setVisibility(View.GONE);

        trackBusesOnRoute(getIntent().getStringExtra("ROUTE_NUMBER"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.track_bus_activity_menu, menu);
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
            case R.id.trackBusActivityRefresh:
                refresh();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        cancelAllTasks();
        errorLinearLayout.setVisibility(View.GONE);

        if (isNetworkAvailable())
        {
            if (currentlySelectedDirection.equals(DIRECTION_UP))
            {
                updatingLinearLayout.setVisibility(View.VISIBLE);
                currentlySelectedBusStop = routeUp.getBusRouteStops().get(position);
                busETAsOnBusRouteTask = new BusETAsOnBusRouteTask(this, routeUp.getBusRouteStops()
                        .get(position).getBusStopRouteOrder(), routeUp);
                String requestParameters = "routeNO=" + routeUp.getBusRouteNumber() + "&" + "direction=" + DIRECTION_UP;
                busETAsOnBusRouteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestParameters);
            }
            else if (currentlySelectedDirection.equals(DIRECTION_DOWN))
            {
                updatingLinearLayout.setVisibility(View.VISIBLE);
                currentlySelectedBusStop = routeDown.getBusRouteStops().get(position);
                busETAsOnBusRouteTask = new BusETAsOnBusRouteTask(this, routeDown.getBusRouteStops()
                        .get(position).getBusStopRouteOrder(), routeDown);
                String requestParameters = "routeNO=" + routeDown.getBusRouteNumber() + "&" + "direction=" + DIRECTION_DOWN;
                busETAsOnBusRouteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestParameters);
            }
            else
            {
                Toast.makeText(this, R.string.please_select_a_direction_error, Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            updatingLinearLayout.setVisibility(View.GONE);
            setErrorLayoutContent(R.drawable.ic_cloud_off_black, "Uh oh! No data connection.", "Retry");
            errorLinearLayout.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }
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
            updatingLinearLayout.setVisibility(View.VISIBLE);
            directionSwapImageView.startAnimation(directionSwapAnimation);
            if (currentlySelectedDirection.equals(DIRECTION_UP))
            {
                directionTextView.setText(getBusRouteDestinationName(routeDown.getBusRouteDirectionName()));
                currentlySelectedDirection = DIRECTION_DOWN;
                getStopsOnRouteTask = new GetStopsOnRouteTask(routeDown);
                getStopsOnRouteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, routeDown.getBusRouteId());
            }
            else if (currentlySelectedDirection.equals(DIRECTION_DOWN))
            {
                directionTextView.setText(getBusRouteDestinationName(routeUp.getBusRouteDirectionName()));
                currentlySelectedDirection = DIRECTION_UP;
                getStopsOnRouteTask = new GetStopsOnRouteTask(routeUp);
                getStopsOnRouteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, routeUp.getBusRouteId());
            }
            else
            {
                Toast.makeText(this, R.string.please_select_a_direction_error, Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            directionSwapImageView.startAnimation(directionSwapAnimation);
            new CountDownTimer(200, 200)
            {
                @Override
                public void onTick(long millisUntilFinished)
                {

                }

                @Override
                public void onFinish()
                {
                    directionSwapImageView.startAnimation(directionSwapAnimationBackwards);
                }
            }.start();
            //Toast.makeText(this, "This bus runs only in one direction...", Toast.LENGTH_SHORT).show();
        }
    }

    private void trackBusesOnRoute(String routeNumber)
    {
        errorLinearLayout.setVisibility(View.GONE);
        updatingLinearLayout.setVisibility(View.VISIBLE);
        getRoutesWithNumberTask = new GetRoutesWithNumberTask();
        getRoutesWithNumberTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, routeNumber);
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

        if (direction.equals(DIRECTION_UP))
        {
            for (BusStop routeStop : routeUp.getBusRouteStops())
            {
                routeStopNames.add(routeStop.getBusStopName());
            }
        }
        else if (direction.equals(DIRECTION_DOWN))
        {
            for (BusStop routeStop : routeDown.getBusRouteStops())
            {
                routeStopNames.add(routeStop.getBusStopName());
            }
        }
        else
        {
            Toast.makeText(this, R.string.please_select_a_direction_error, Toast.LENGTH_SHORT).show();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, routeStopNames);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onBusETAsOnBusRouteFound(String errorMessage, int busStopRouteOrder, ArrayList<Bus> buses, BusRoute busRoute)
    {
        if (errorMessage.equals(NETWORK_QUERY_NO_ERROR))
        {
            if (buses.size() != 0)
            {
                for (Bus bus : buses)
                {
                    bus.setBusETA(calculateTravelTime(DbQueries.getNumberOfStopsBetweenRouteOrders(db, busRoute.getBusRouteId(),
                            bus.getBusRouteOrder(), busStopRouteOrder), busRoute.getBusRouteNumber()));

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

                updatingLinearLayout.setVisibility(View.GONE);
                TrackBusListCustomAdapter trackBusListCustomAdapter = new TrackBusListCustomAdapter(this, buses);
                listView.setAdapter(trackBusListCustomAdapter);
                errorLinearLayout.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
            }
            else
            {
                updatingLinearLayout.setVisibility(View.GONE);
                setErrorLayoutContent(R.drawable.ic_directions_bus_black, "Whoops! There don't seem to be any buses arriving " +
                        "at this stop anytime soon.", "Retry");
                errorLinearLayout.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
            }
        }
        else
        {
            if (isNetworkAvailable())
            {
                updatingLinearLayout.setVisibility(View.GONE);
                switch (errorMessage)
                {
                    case NETWORK_QUERY_IO_EXCEPTION:
                    {
                        setErrorLayoutContent(R.drawable.ic_sad_face, "Sorry! Something went wrong.", "Retry");
                        errorLinearLayout.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        break;
                    }
                    case NETWORK_QUERY_JSON_EXCEPTION:
                    {
                        setErrorLayoutContent(R.drawable.ic_directions_bus_black, "Whoops! There don't seem to be any buses arriving " +
                                "at this stop anytime soon.", "Retry");
                        errorLinearLayout.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        break;
                    }
                    case NETWORK_QUERY_REQUEST_TIMEOUT_EXCEPTION:
                    {
                        setErrorLayoutContent(R.drawable.ic_cloud_off_black, "Uh oh! Data connection seems to be slow. Couldn't track buses.", "Retry");
                        errorLinearLayout.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        break;
                    }
                    case NETWORK_QUERY_URL_EXCEPTION:
                    {
                        setErrorLayoutContent(R.drawable.ic_sad_face, "Sorry! Something went wrong.", "Retry");
                        errorLinearLayout.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        break;
                    }
                    default:
                    {
                        setErrorLayoutContent(R.drawable.ic_sad_face, "Sorry! Something went wrong.", "Retry");
                        errorLinearLayout.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        break;
                    }
                }
            }
            else
            {
                setErrorLayoutContent(R.drawable.ic_cloud_off_black, "Uh oh! No data connection.", "Retry");
                errorLinearLayout.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
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

        if (isNetworkAvailable())
        {
            if (currentlySelectedBusStop != null)
            {
                if (currentlySelectedDirection.equals(DIRECTION_UP))
                {
                    busETAsOnBusRouteTask = new BusETAsOnBusRouteTask(this, currentlySelectedBusStop.getBusStopRouteOrder(), routeUp);
                    updatingLinearLayout.setVisibility(View.VISIBLE);
                    String requestParameters = "routeNO=" + routeUp.getBusRouteNumber() + "&" + "direction=" + DIRECTION_UP;
                    busETAsOnBusRouteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestParameters);
                }
                else if (currentlySelectedDirection.equals(DIRECTION_DOWN))
                {
                    busETAsOnBusRouteTask = new BusETAsOnBusRouteTask(this, currentlySelectedBusStop.getBusStopRouteOrder(), routeDown);
                    updatingLinearLayout.setVisibility(View.VISIBLE);
                    String requestParameters = "routeNO=" + routeDown.getBusRouteNumber() + "&" + "direction=" + DIRECTION_DOWN;
                    busETAsOnBusRouteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestParameters);
                }
                else
                {
                    Toast.makeText(this, R.string.please_select_a_direction_error, Toast.LENGTH_SHORT).show();
                }
            }
        }
        else
        {
            updatingLinearLayout.setVisibility(View.GONE);
            setErrorLayoutContent(R.drawable.ic_cloud_off_black, "Uh oh! No data connection.", "Retry");
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
            busRouteDestinationName = busRouteDestinationName.substring(busRouteDestinationName.indexOf("To "), busRouteDestinationName.length());
        }
        else if (busRouteDestinationName.contains(" to "))
        {
            busRouteDestinationName = busRouteDestinationName.substring(busRouteDestinationName.indexOf("to "), busRouteDestinationName.length());
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