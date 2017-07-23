package com.bangalorebuses;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;

import static com.bangalorebuses.Constants.NETWORK_QUERY_NO_ERROR;

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
    private final String DIRECTION_UP = "UP";
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
    private Route route;
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
        /*MobileAds.initialize(this, "ca-app-pub-4515741125560154~6681035222");
        adView = (AdView) findViewById(R.id.track_bus_activity_footer_ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);*/
        stopsOnRouteSpinner = (Spinner) findViewById(R.id.route_stop_list_spinner);
        rotatingAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        busTimingsRefreshFloatingActionButton = (FloatingActionButton) findViewById(R.id.floatingBusTimingsRefreshActionButton);
        route = new Route();
        route.setRouteNumber(getIntent().getStringExtra("ROUTE_NUMBER"));
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
        menuInflater.inflate(R.menu.track_bus_menu, menu);
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
    public void onBusRouteDetailsFound(String errorMessage, Route inputRoute, boolean isForList, String routeDirection)
    {

    }

    @Override
    public void onStopsOnBusRouteFound(String errorMessage, BusStop[] busStops, Route route)
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
    public void onBusesEnRouteFound(String errorMessage, Bus[] buses, int numberOfBusesFound, Route route, BusStop selectedBusStop)
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

                /*if (isNetworkAvailable())
                {
                    new GetTimeToBusesTask(this, busStopList[position], numberOfBusesFound).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, buses);
                }
                else
                {
                    errorMessageTextView.setText(R.string.error_connecting_to_the_internet_click_refresh_text);
                    errorMessageTextView.setVisibility(View.VISIBLE);
                }*/
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
    }

    @Override
    public void onTimeToBusesFound(boolean isError, Bus[] buses)
    {
        downDirectionRadioButton.setEnabled(true);
        upDirectionRadioButton.setEnabled(true);
        stopsOnRouteSpinner.setEnabled(true);
        setBusIcons();
        if (!isError)
        {
            if (buses[0].getTimeToBus() != null)
            {
                if (!buses[0].getTripIsYetToBegin())
                {
                    if (buses[0].getIsDue())
                    {
                        busTimingTextView1.setText("is Due");
                    }
                    else if (buses[0].getTimeToBus().equals("UNAVAILABLE"))
                    {
                        busTimingTextView1.setText("arrival time is unavailable");
                    }
                    else
                    {
                        busTimingTextView1.setText("in " + buses[0].getTimeToBus());
                    }
                    busIsAtTextView1.setText("currently near " + buses[0].getNameOfStopBusIsAt());
                }
                else
                {
                    busTimingTextView1.setText("trip is yet to begin");
                    busIsAtTextView1.setText("currently at " + buses[0].getNameOfStopBusIsAt());
                }
                busDetailsLinearLayout1.setVisibility(View.VISIBLE);
            }

            if (buses[1].getTimeToBus() != null)
            {
                if (!buses[1].getTripIsYetToBegin())
                {
                    if (buses[1].getIsDue())
                    {
                        busTimingTextView2.setText("is Due");
                    }
                    else if (buses[1].getTimeToBus().equals("UNAVAILABLE"))
                    {
                        busTimingTextView2.setText("arrival time is unavailable");
                    }
                    else
                    {
                        busTimingTextView2.setText("in " + buses[1].getTimeToBus());
                    }
                    busIsAtTextView2.setText("currently near " + buses[1].getNameOfStopBusIsAt());
                }
                else
                {
                    busTimingTextView2.setText("trip is yet to begin");
                    busIsAtTextView2.setText("currently at " + buses[1].getNameOfStopBusIsAt());
                }
                busDetailsLinearLayout2.setVisibility(View.VISIBLE);
            }

            if (buses[2].getTimeToBus() != null)
            {
                if (!buses[2].getTripIsYetToBegin())
                {
                    if (buses[2].getIsDue())
                    {
                        busTimingTextView3.setText("is Due");
                    }
                    else if (buses[2].getTimeToBus().equals("UNAVAILABLE"))
                    {
                        busTimingTextView3.setText("arrival time is unavailable");
                    }
                    else
                    {
                        busTimingTextView3.setText("in " + buses[2].getTimeToBus());
                    }
                    busIsAtTextView3.setText("currently near " + buses[2].getNameOfStopBusIsAt());
                }
                else
                {
                    busTimingTextView3.setText("trip is yet to begin");
                    busIsAtTextView3.setText("currently at " + buses[2].getNameOfStopBusIsAt());
                }
                busDetailsLinearLayout3.setVisibility(View.VISIBLE);
            }

            if (buses[3].getTimeToBus() != null)
            {
                if (!buses[3].getTripIsYetToBegin())
                {
                    if (buses[3].getIsDue())
                    {
                        busTimingTextView4.setText("is Due");
                    }
                    else if (buses[3].getTimeToBus().equals("UNAVAILABLE"))
                    {
                        busTimingTextView4.setText("arrival time is unavailable");
                    }
                    else
                    {
                        busTimingTextView4.setText("in " + buses[3].getTimeToBus());
                    }
                    busIsAtTextView4.setText("currently near " + buses[3].getNameOfStopBusIsAt());
                }
                else
                {
                    busTimingTextView4.setText("trip is yet to begin");
                    busIsAtTextView4.setText("currently at " + buses[3].getNameOfStopBusIsAt());
                }
                busDetailsLinearLayout4.setVisibility(View.VISIBLE);
            }

            canRefresh = false;
            new CountDownTimer(5000, 5000)
            {
                @Override
                public void onTick(long millisUntilFinished)
                {

                }

                @Override
                public void onFinish()
                {
                    canRefresh = true;
                }
            }.start();
        }
        else
        {
            if (isNetworkAvailable())
            {
                errorMessageTextView.setText("Could not get bus arrival timings! Please try again later.");
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
            else
            {
                errorMessageTextView.setText(R.string.error_connecting_to_the_internet_click_refresh_text);
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
        busTimingsRefreshFloatingActionButton.clearAnimation();
        busTimingsRefreshFloatingActionButton.setEnabled(true);
        progressDialog.dismiss();
    }

    private void setBusIcons()
    {
        ImageView busIcon1 = (ImageView) findViewById(R.id.bus_icon_1);
        ImageView busIcon2 = (ImageView) findViewById(R.id.bus_icon_2);
        ImageView busIcon3 = (ImageView) findViewById(R.id.bus_icon_3);
        ImageView busIcon4 = (ImageView) findViewById(R.id.bus_icon_4);
        if (route.getRouteNumber().length() > 5 && route.getRouteNumber().substring(0, 5).equals("KIAS-"))
        {
            busIcon1.setImageResource(R.drawable.ic_flight_black);
            busIcon2.setImageResource(R.drawable.ic_flight_black);
            busIcon3.setImageResource(R.drawable.ic_flight_black);
            busIcon4.setImageResource(R.drawable.ic_flight_black);
        }
        else if (route.getRouteNumber().length() > 2 && route.getRouteNumber().substring(0, 2).equals("V-"))
        {
            busIcon1.setImageResource(R.drawable.ic_directions_bus_ac);
            busIcon2.setImageResource(R.drawable.ic_directions_bus_ac);
            busIcon3.setImageResource(R.drawable.ic_directions_bus_ac);
            busIcon4.setImageResource(R.drawable.ic_directions_bus_ac);
        }
        else if (route.getRouteNumber().contains("CHAKRA-") || route.getRouteNumber().contains("MF"))
        {
            busIcon1.setImageResource(R.drawable.ic_directions_bus_special);
            busIcon2.setImageResource(R.drawable.ic_directions_bus_special);
            busIcon3.setImageResource(R.drawable.ic_directions_bus_special);
            busIcon4.setImageResource(R.drawable.ic_directions_bus_special);
        }
        else
        {
            busIcon1.setImageResource(R.drawable.ic_directions_bus_ordinary);
            busIcon2.setImageResource(R.drawable.ic_directions_bus_ordinary);
            busIcon3.setImageResource(R.drawable.ic_directions_bus_ordinary);
            busIcon4.setImageResource(R.drawable.ic_directions_bus_ordinary);
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        /*if (adView != null)
        {
            adView.pause();
        }*/
    }

    @Override
    public void onResume()
    {
        /*if (adView != null)
        {
            adView.resume();
        }*/
        super.onResume();
    }

    @Override
    public void onDestroy()
    {
        /*if (adView != null)
        {
            adView.destroy();
        }*/
        super.onDestroy();
    }
}