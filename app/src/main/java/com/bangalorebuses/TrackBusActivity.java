package com.bangalorebuses;

import android.app.ProgressDialog;
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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import static com.bangalorebuses.Constants.DIRECTION_DOWN;
import static com.bangalorebuses.Constants.DIRECTION_UP;
import static com.bangalorebuses.Constants.NETWORK_QUERY_NO_ERROR;
import static com.bangalorebuses.Constants.db;

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
    private GetBusesEnRouteTask getBusesEnRouteTask;
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
    private ProgressDialog progressDialog;
    private BusStop currentlySelectedBusStop;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setElevation(0);
        }
        setContentView(R.layout.activity_track_bus);

        // Initialise XML elements
        listView = (ListView) findViewById(R.id.listView);
        spinner = (Spinner) findViewById(R.id.route_stop_list_spinner);
        directionTextView = (TextView) findViewById(R.id.directionNameTextView);
        directionSwapImageView = (ImageView) findViewById(R.id.changeDirectionImageView);
        directionSwapAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_once);
        directionSwapAnimationBackwards = AnimationUtils.loadAnimation(this, R.anim.rotate_once_backwards);

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
                if (currentlySelectedBusStop != null)
                {
                    if (currentlySelectedDirection.equals(DIRECTION_UP))
                    {
                        //TODO Check for internet
                        getBusesEnRouteTask = new GetBusesEnRouteTask(this, currentlySelectedBusStop.getBusStopRouteOrder(), routeUp);
                        progressDialog = ProgressDialog.show(this, "Please wait", "Getting buses");
                        String requestParameters = "routeNO=" + routeUp.getBusRouteNumber() + "&" + "direction=" + DIRECTION_UP;
                        getBusesEnRouteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestParameters);
                    }
                    else if (currentlySelectedDirection.equals(DIRECTION_DOWN))
                    {
                        getBusesEnRouteTask = new GetBusesEnRouteTask(this, currentlySelectedBusStop.getBusStopRouteOrder(), routeDown);
                        progressDialog = ProgressDialog.show(this, "Please wait", "Getting buses");
                        String requestParameters = "routeNO=" + routeDown.getBusRouteNumber() + "&" + "direction=" + DIRECTION_DOWN;
                        getBusesEnRouteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestParameters);
                    }
                    else
                    {
                        // TODO No direction selected!
                    }
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        progressDialog.dismiss();
        progressDialog = ProgressDialog.show(this, "Please wait", "Getting buses...");
        if (currentlySelectedDirection.equals(DIRECTION_UP))
        {
            //TODO Check for internet
            currentlySelectedBusStop = routeUp.getBusRouteStops().get(position);
            getBusesEnRouteTask = new GetBusesEnRouteTask(this, routeUp.getBusRouteStops()
                    .get(position).getBusStopRouteOrder(), routeUp);
            String requestParameters = "routeNO=" + routeUp.getBusRouteNumber() + "&" + "direction=" + DIRECTION_UP;
            getBusesEnRouteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestParameters);
        }
        else if (currentlySelectedDirection.equals(DIRECTION_DOWN))
        {
            currentlySelectedBusStop = routeDown.getBusRouteStops().get(position);
            getBusesEnRouteTask = new GetBusesEnRouteTask(this, routeDown.getBusRouteStops()
                    .get(position).getBusStopRouteOrder(), routeDown);
            String requestParameters = "routeNO=" + routeDown.getBusRouteNumber() + "&" + "direction=" + DIRECTION_DOWN;
            getBusesEnRouteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestParameters);
        }
        else
        {
            // TODO No direction selected!
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
            progressDialog.dismiss();
            progressDialog = ProgressDialog.show(this, "Please wait", "Getting buses...");
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
                // TODO Select a default direction
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
        progressDialog = ProgressDialog.show(this, "Please wait", "Getting buses...");
        getRoutesWithNumberTask = new GetRoutesWithNumberTask();
        getRoutesWithNumberTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, routeNumber);
    }

    @Override
    public void onBusStopsFound(boolean isError, JSONArray busStopsArray)
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
            // TODO No direction selected!
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, routeStopNames);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onBusesEnRouteFound(String errorMessage, int busStopRouteOrder, ArrayList<Bus> buses, BusRoute busRoute)
    {
        if (errorMessage.equals(NETWORK_QUERY_NO_ERROR))
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

            TrackBusListCustomAdapter trackBusListCustomAdapter = new TrackBusListCustomAdapter(this, buses);
            listView.setAdapter(trackBusListCustomAdapter);
            progressDialog.dismiss();
            listView.setVisibility(View.VISIBLE);
        }
        else
        {
            //TODO
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

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (getBusesEnRouteTask != null &&
                getBusesEnRouteTask.getStatus().equals(AsyncTask.Status.FINISHED))
        {
            getBusesEnRouteTask.cancel(true);
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
            onRoutesWithNumberFound(busRoutes);
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
            onBusStopsOnRouteFound(busRoute);
        }
    }
}





































    /*private final String DIRECTION_UP = "UP";
    private final String DIRECTION_DOWN = "DN";
    private RadioGroup directionSelectionRadioGroup;
    private RadioButton upDirectionRadioButton;
    private RadioButton downDirectionRadioButton;
    private LinearLayout busStopSelectionLinearLayout;
    private Spinner stopsOnRouteSpinner;
    private TextView errorMessageTextView;
    private LinearLayout busDetailsLinearLayout1;
    private TextView busTimingTextView1;
    private TextView busIsAtTextView1;
    private LinearLayout busDetailsLinearLayout2;
    private TextView busTimingTextView2;
    private TextView busIsAtTextView2;
    private LinearLayout busDetailsLinearLayout3;
    private TextView busTimingTextView3;
    private TextView busIsAtTextView3;
    private LinearLayout busDetailsLinearLayout4;
    private TextView busTimingTextView4;
    private TextView busIsAtTextView4;
    private BusStop selectedBusStop;
    private BusRoute route;
    private ProgressDialog progressDialog;
    private int position;
    private BusStop[] busStopList;
    private Animation rotatingAnimation;
    private FloatingActionButton busTimingsRefreshFloatingActionButton;
    private boolean canRefresh = true;
    //private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setElevation(0);
        setContentView(R.layout.activity_track_bus);
        directionSelectionRadioGroup = (RadioGroup) findViewById(R.id.direction_selection_radio_group);
        upDirectionRadioButton = (RadioButton) findViewById(R.id.direction_up_radio_button);
        downDirectionRadioButton = (RadioButton) findViewById(R.id.direction_down_radio_button);
        busStopSelectionLinearLayout = (LinearLayout) findViewById(R.id.bus_stop_selection_linear_layout);
        errorMessageTextView = (TextView) findViewById(R.id.error_message_text_view);
        busTimingTextView1 = (TextView) findViewById(R.id.buses_text_view_1);
        busIsAtTextView1 = (TextView) findViewById(R.id.is_at_stop_text_view_1);
        busTimingTextView2 = (TextView) findViewById(R.id.buses_text_view_2);
        busIsAtTextView2 = (TextView) findViewById(R.id.is_at_stop_text_view_2);
        busTimingTextView3 = (TextView) findViewById(R.id.buses_text_view_3);
        busIsAtTextView3 = (TextView) findViewById(R.id.is_at_stop_text_view_3);
        busTimingTextView4 = (TextView) findViewById(R.id.buses_text_view_4);
        busIsAtTextView4 = (TextView) findViewById(R.id.is_at_stop_text_view_4);
        busDetailsLinearLayout1 = (LinearLayout) findViewById(R.id.bus_linear_layout_1);
        busDetailsLinearLayout2 = (LinearLayout) findViewById(R.id.bus_linear_layout_2);
        busDetailsLinearLayout3 = (LinearLayout) findViewById(R.id.bus_linear_layout_3);
        busDetailsLinearLayout4 = (LinearLayout) findViewById(R.id.bus_linear_layout_4);
        stopsOnRouteSpinner = (Spinner) findViewById(R.id.route_stop_list_spinner);
        rotatingAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        busTimingsRefreshFloatingActionButton = (FloatingActionButton) findViewById(R.id.floatingBusTimingsRefreshActionButton);
        route = new BusRoute();
        route.setBusRouteNumber(getIntent().getStringExtra("ROUTE_NUMBER"));
        route.setUpRouteId(getIntent().getStringExtra("UP_ROUTE_ID"));
        route.setDownRouteId(getIntent().getStringExtra("DOWN_ROUTE_ID"));
        route.setUpRouteName(getIntent().getStringExtra("UP_ROUTE_NAME"));
        route.setDownRouteName(getIntent().getStringExtra("DOWN_ROUTE_NAME"));
        route.setDirection(getIntent().getStringExtra("ROUTE_DIRECTION"));
        selectedBusStop = new BusStop();
        selectedBusStop.setBusStopName(getIntent().getStringExtra("STOP_NAME"));
        selectedBusStop.setLatitude(getIntent().getStringExtra("STOP_LAT"));
        selectedBusStop.setLongitude(getIntent().getStringExtra("STOP_LONG"));
        setTitle("Tracking " + route.getRouteNumber());
        trackBus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        //menuInflater.inflate(R.menu.track_bus_menu, menu);
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
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    public void trackBus()
    {
        errorMessageTextView.setVisibility(View.GONE);
        busDetailsLinearLayout1.setVisibility(View.GONE);
        busDetailsLinearLayout2.setVisibility(View.GONE);
        busDetailsLinearLayout3.setVisibility(View.GONE);
        busDetailsLinearLayout4.setVisibility(View.GONE);
        directionSelectionRadioGroup.setVisibility(View.GONE);
        busStopSelectionLinearLayout.setVisibility(View.GONE);

        if (route.getRouteNumber().equals("") || route.getRouteNumber().equals(" "))
        {
            Toast.makeText(this, "Please enter a valid bus number!", Toast.LENGTH_SHORT).show();
            finish();
        }
        else
        {
            upDirectionRadioButton.setText(route.getUpRouteName());
            downDirectionRadioButton.setText(route.getDownRouteName());
            directionSelectionRadioGroup.setVisibility(View.VISIBLE);
            if (route.getDownRouteId().equals(""))
            {
                downDirectionRadioButton.setVisibility(View.GONE);
            }

            if (route.getDirection().equals("UP"))
            {
                upDirectionRadioButton.callOnClick();
            }
            else if (route.getDirection().equals("DN"))
            {
                downDirectionRadioButton.callOnClick();
            }
            else
            {
                upDirectionRadioButton.callOnClick();
            }
        }
    }

    public void refresh(View view)
    {
        if (canRefresh)
        {
            if (isNetworkAvailable())
            {
                if (busStopList != null)
                {
                    downDirectionRadioButton.setEnabled(false);
                    upDirectionRadioButton.setEnabled(false);
                    stopsOnRouteSpinner.setEnabled(false);
                    errorMessageTextView.setVisibility(View.GONE);
                    busDetailsLinearLayout1.setVisibility(View.GONE);
                    busDetailsLinearLayout2.setVisibility(View.GONE);
                    busDetailsLinearLayout3.setVisibility(View.GONE);
                    busDetailsLinearLayout4.setVisibility(View.GONE);
                    busTimingsRefreshFloatingActionButton.setEnabled(false);
                    busTimingsRefreshFloatingActionButton.startAnimation(rotatingAnimation);
                    if (route.getDirection().equals(DIRECTION_UP))
                    {
                        new GetStopsOnBusRouteTask(this, route.getUpRouteId(), route).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                    else
                    {
                        new GetStopsOnBusRouteTask(this, route.getDownRouteId(), route).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
                else
                {
                    trackBus();
                }
            }
            else
            {
                errorMessageTextView.setText(R.string.error_connecting_to_the_internet_click_refresh_text);
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            busTimingsRefreshFloatingActionButton.setEnabled(false);
            busTimingsRefreshFloatingActionButton.startAnimation(rotatingAnimation);
            new CountDownTimer(2000, 2000)
            {
                @Override
                public void onTick(long millisUntilFinished)
                {

                }

                @Override
                public void onFinish()
                {
                    busTimingsRefreshFloatingActionButton.clearAnimation();
                    busTimingsRefreshFloatingActionButton.setEnabled(true);
                }
            }.start();
        }
    }

    public void directionChosen(View view)
    {
        if (view.getId() == R.id.direction_up_radio_button)
        {
            route.setDirection(DIRECTION_UP);
            directionSelectionRadioGroup.check(R.id.direction_up_radio_button);
            if (isNetworkAvailable())
            {
                progressDialog = ProgressDialog.show(this, "Please wait", "Locating buses...");
                new GetStopsOnBusRouteTask(this, route.getUpRouteId(), route).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
            else
            {
                errorMessageTextView.setText(R.string.error_connecting_to_the_internet_click_refresh_text);
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
        else if (view.getId() == R.id.direction_down_radio_button)
        {
            route.setDirection(DIRECTION_DOWN);
            directionSelectionRadioGroup.check(R.id.direction_down_radio_button);
            if (isNetworkAvailable())
            {
                progressDialog = ProgressDialog.show(this, "Please wait", "Locating buses...");
                new GetStopsOnBusRouteTask(this, route.getDownRouteId(), route).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
            else
            {
                errorMessageTextView.setText(R.string.error_connecting_to_the_internet_click_refresh_text);
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onBusesAtStopFound(String errorMessage, JSONArray buses)
    {

    }

    @Override
    public void onBusStopsFound(boolean isError, JSONArray busStopsArray)
    {

    }

    @Override
    public void onBusRouteDetailsFound(String errorMessage, BusRoute inputRoute, boolean isForList, String routeDirection)
    {

    }

    @Override
    public void onStopsOnBusRouteFound(String errorMessage, BusStop[] busStops, BusRoute route)
    {
        if (errorMessage.equals(NETWORK_QUERY_NO_ERROR))
        {
            busStopList = busStops;
            ArrayList<String> stopList = new ArrayList<>();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, stopList);
            int nearestBusStopIndex = 0;

            String selectedBusStopName = "";
            if (selectedBusStop != null && selectedBusStop.getBusStopName() != null)
            {
                selectedBusStopName = selectedBusStop.getBusStopName();

                if (selectedBusStopName.contains("("))
                {
                    selectedBusStopName = selectedBusStopName.substring(0, selectedBusStopName.indexOf("("));
                }
            }

            for (int i = 0; i < busStops.length; i++)
            {
                String busStopName = busStops[i].getBusStopName();
                // Check if the bus stop name has a parenthesis character in it. If yes, remove it and the direction that precedes.
                if (busStopName.contains("("))
                {
                    busStopName = busStopName.substring(0, busStopName.indexOf("(") - 1);
                }
                if (busStopName.equals(selectedBusStopName))
                {
                    nearestBusStopIndex = i;
                }
                stopList.add(busStopName);
                busStopList[i].setBusStopName(busStopName);
            }

            stopsOnRouteSpinner.setAdapter(adapter);
            stopsOnRouteSpinner.setOnItemSelectedListener(this);
            stopsOnRouteSpinner.setSelection(nearestBusStopIndex);
            busStopSelectionLinearLayout.setVisibility(View.VISIBLE);
        }
        else
        {
            progressDialog.dismiss();
            if (isNetworkAvailable())
            {
                errorMessageTextView.setText(R.string.error_getting_stop_details_text);
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
            else
            {
                errorMessageTextView.setText(R.string.error_connecting_to_the_internet_click_refresh_text);
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        this.position = position;
        progressDialog.dismiss();
        selectedBusStop.setBusStopName(busStopList[position].getBusStopName());
        selectedBusStop.setLatitude(busStopList[position].getLatitude());
        selectedBusStop.setLongitude(busStopList[position].getLongitude());
        selectedBusStop.setRouteOrder(busStopList[position].getRouteOrder());
        if (isNetworkAvailable())
        {
            progressDialog = ProgressDialog.show(this, "Please wait", "Locating buses...");
            errorMessageTextView.setVisibility(View.GONE);
            busDetailsLinearLayout1.setVisibility(View.GONE);
            busDetailsLinearLayout2.setVisibility(View.GONE);
            busDetailsLinearLayout3.setVisibility(View.GONE);
            busDetailsLinearLayout4.setVisibility(View.GONE);
            String requestBody = "routeNO=" + route.getRouteNumber() + "&" + "direction=" + route.getDirection();
            new GetBusesEnRouteTask(this, busStopList[position], route).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestBody);
        }
        else
        {
            errorMessageTextView.setText(R.string.error_connecting_to_the_internet_click_refresh_text);
            errorMessageTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
        Toast.makeText(this, "Please select a bus stop to track the bus!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBusesEnRouteFound(String errorMessage, Bus[] buses, int numberOfBusesFound, BusRoute route, BusStop selectedBusStop)
    {
        if (errorMessage.equals(NETWORK_QUERY_NO_ERROR))
        {
            if (!(numberOfBusesFound == 0))
            {
                // For each bus in the 'buses' Bus[]
                for (Bus bus : buses)
                {
                    if (bus.getRouteOrder() == 1)
                    {
                        bus.setTripIsYetToBegin(true);
                    }
                    else
                    {
                        bus.setTripIsYetToBegin(false);
                    }
                    bus.setNameOfStopBusIsAt("bus stop unknown");
                    for (int j = 0; j < busStopList.length; j++)
                    {
                        if (bus.getRouteOrder() == busStopList[j].getRouteOrder())
                        {
                            String stopName = busStopList[j].getBusStopName();
                            if (busStopList[j].getBusStopName().contains("("))
                            {
                                stopName = busStopList[j].getBusStopName().substring(0, busStopList[j].getBusStopName().indexOf("("));
                            }
                            bus.setNameOfStopBusIsAt(stopName);
                        }
                    }
                }

                // Compute the arrival time of each bus
                for (int i = 0; i < numberOfBusesFound; i++)
                {
                    buses[i].setTimeToBus("UNAVAILABLE");
                    for (int j = 0; j < busStopList.length; j++)
                    {
                        if (buses[i].getRouteOrder() == busStopList[j].getRouteOrder())
                        {
                            for (int k = j; k < busStopList.length; k++)
                            {
                                if (busStopList[position].getRouteOrder() == busStopList[k].getRouteOrder())
                                {
                                    Calendar calendar = Calendar.getInstance();
                                    int timeToBus;

                                    if ((calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) || (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY))
                                    {
                                        if (route.getRouteNumber().contains("KIAS-"))
                                        {
                                            timeToBus = (k - j) * 4;
                                        }
                                        else
                                        {
                                            timeToBus = (k - j) * 2;
                                        }
                                    }
                                    else if ((calendar.get(Calendar.HOUR_OF_DAY) > 7 && calendar.get(Calendar.HOUR_OF_DAY) < 11) || (calendar.get(Calendar.HOUR_OF_DAY) > 16 && calendar.get(Calendar.HOUR_OF_DAY) < 21))
                                    {
                                        if (route.getRouteNumber().contains("KIAS-"))
                                        {
                                            timeToBus = (k - j) * 5;
                                        }
                                        else
                                        {
                                            timeToBus = (k - j) * 3;
                                        }
                                    }
                                    else
                                    {
                                        if (route.getRouteNumber().contains("KIAS-"))
                                        {
                                            timeToBus = (k - j) * 4;
                                        }
                                        else
                                        {
                                            timeToBus = (int) ((k - j) * 2.5);
                                        }
                                    }
                                    int hours = timeToBus / 60;
                                    if (timeToBus >= 60)
                                    {
                                        if (hours == 1)
                                        {
                                            buses[i].setTimeToBus(hours + " hour " + timeToBus % 60 + " mins");
                                        }
                                        else
                                        {
                                            buses[i].setTimeToBus(hours + " hours " + timeToBus % 60 + " mins");
                                        }
                                    }
                                    else
                                    {
                                        buses[i].setTimeToBus(timeToBus + " mins");
                                    }
                                }
                            }
                        }
                    }
                }
                onTimeToBusesFound(false, buses);

                if (isNetworkAvailable())
                {
                    new GetTimeToBusesTask(this, busStopList[position], numberOfBusesFound).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, buses);
                }
                else
                {
                    errorMessageTextView.setText(R.string.error_connecting_to_the_internet_click_refresh_text);
                    errorMessageTextView.setVisibility(View.VISIBLE);
                }
            }
            else
            {
                progressDialog.dismiss();
                errorMessageTextView.setText("There aren't any " + route.getRouteNumber() + " buses arriving at " + busStopList[position].getBusStopName() + " in this direction. Please select another bus stop.");
                errorMessageTextView.setVisibility(View.VISIBLE);
                busTimingsRefreshFloatingActionButton.clearAnimation();
                busTimingsRefreshFloatingActionButton.setEnabled(true);
                downDirectionRadioButton.setEnabled(true);
                upDirectionRadioButton.setEnabled(true);
                stopsOnRouteSpinner.setEnabled(true);
            }
        }
        else
        {
            progressDialog.dismiss();
            busTimingsRefreshFloatingActionButton.clearAnimation();
            busTimingsRefreshFloatingActionButton.setEnabled(true);
            downDirectionRadioButton.setEnabled(true);
            upDirectionRadioButton.setEnabled(true);
            stopsOnRouteSpinner.setEnabled(true);
            if (isNetworkAvailable())
            {
                errorMessageTextView.setText("There aren't any " + route.getRouteNumber() + " buses arriving at " + busStopList[position].getBusStopName() + " in this direction. Please select another bus stop.");
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
            else
            {
                errorMessageTextView.setText(R.string.error_connecting_to_the_internet_click_refresh_text);
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
    }*/