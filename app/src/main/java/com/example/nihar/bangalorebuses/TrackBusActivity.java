package com.example.nihar.bangalorebuses;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import org.json.JSONException;

import java.util.ArrayList;

public class TrackBusActivity extends AppCompatActivity implements NetworkingCallback, AdapterView.OnItemSelectedListener
{
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
    private BusStop[] busStopList;
    private int position;
    private JSONArray routeBusStopList;

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
        route = new Route();
        route.setRouteNumber(getIntent().getStringExtra("ROUTE_NUMBER"));
        route.setUpRouteId(getIntent().getStringExtra("UP_ROUTE_ID"));
        route.setDownRouteId(getIntent().getStringExtra("DOWN_ROUTE_ID"));
        route.setUpRouteName(getIntent().getStringExtra("UP_ROUTE_NAME"));
        route.setDownRouteName(getIntent().getStringExtra("DOWN_ROUTE_NAME"));
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
            case R.id.track_bus_action_refresh:
                refresh();
                break;
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
        busDetailsLinearLayout1.setVisibility(View.INVISIBLE);
        busDetailsLinearLayout2.setVisibility(View.INVISIBLE);
        busDetailsLinearLayout3.setVisibility(View.INVISIBLE);
        busDetailsLinearLayout4.setVisibility(View.INVISIBLE);
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
            directionSelectionRadioGroup.check(R.id.direction_up_radio_button);
            directionSelectionRadioGroup.setVisibility(View.VISIBLE);
            upDirectionRadioButton.callOnClick();
        }
    }

    private void refresh()
    {
        errorMessageTextView.setVisibility(View.GONE);
        busDetailsLinearLayout1.setVisibility(View.INVISIBLE);
        busDetailsLinearLayout2.setVisibility(View.INVISIBLE);
        busDetailsLinearLayout3.setVisibility(View.INVISIBLE);
        busDetailsLinearLayout4.setVisibility(View.INVISIBLE);

        if (isNetworkAvailable())
        {
            progressDialog = ProgressDialog.show(this, "Please wait", "Locating buses...");
            String requestBody = "routeNO=" + route.getRouteNumber() + "&" + "direction=" + route.getDirection();
            new GetBusesEnRouteTask(this, busStopList[position]).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestBody);
        }
        else
        {
            errorMessageTextView.setText(R.string.error_connecting_to_the_internet_text);
            errorMessageTextView.setVisibility(View.VISIBLE);
        }
    }

    public void directionChosen(View view)
    {
        if (isNetworkAvailable())
        {
            progressDialog = ProgressDialog.show(this, "Please wait", "Locating buses...");
            final String DIRECTION_UP = "UP";
            final String DIRECTION_DOWN = "DN";
            if (view.getId() == R.id.direction_up_radio_button)
            {
                route.setDirection(DIRECTION_UP);
                new GetStopsOnBusRouteTask(this, route.getUpRouteId()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
            else if (view.getId() == R.id.direction_down_radio_button)
            {
                route.setDirection(DIRECTION_DOWN);
                new GetStopsOnBusRouteTask(this, route.getDownRouteId()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        else
        {
            errorMessageTextView.setText(R.string.error_connecting_to_the_internet_text);
            errorMessageTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBusesAtStopFound(boolean isError, JSONArray buses)
    {

    }

    @Override
    public void onBusStopsFound(boolean isError, JSONArray busStopsArray)
    {

    }

    @Override
    public void onBusRouteDetailsFound(boolean isError, Route inputRoute, boolean isForList)
    {

    }

    @Override
    public void onStopsOnBusRouteFound(boolean isError, JSONArray stopListArray)
    {
        if (isError)
        {
            progressDialog.hide();
            errorMessageTextView.setText(R.string.error_getting_stop_details_text);
            errorMessageTextView.setVisibility(View.VISIBLE);
        }
        else
        {
            routeBusStopList = stopListArray;
            ArrayList<String> stopList = new ArrayList<>();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, stopList);
            int nearestBusStopIndex = 0;
            busStopList = new BusStop[stopListArray.length()];

            try
            {
                String selectedBusStopName = "";
                if (selectedBusStop != null && selectedBusStop.getBusStopName() != null)
                {
                    if (selectedBusStop.getBusStopName().contains("("))
                    {
                        selectedBusStopName = selectedBusStop.getBusStopName().substring(0, selectedBusStop.getBusStopName().indexOf("(") - 1);
                    }
                    else
                    {
                        selectedBusStopName = selectedBusStop.getBusStopName();
                    }
                }

                for (int i = 0; i < stopListArray.length(); i++)
                {
                    String busStopName = stopListArray.getJSONObject(i).getString("busStopName");
                    if (busStopName.contains("("))
                    {
                        busStopName = busStopName.substring(0, busStopName.indexOf("(") - 1);
                    }
                    if (busStopName.equals(selectedBusStopName))
                    {
                        nearestBusStopIndex = i;
                    }
                    stopList.add(busStopName);
                    busStopList[i] = new BusStop();
                    busStopList[i].setBusStopName(busStopName);
                    busStopList[i].setLatitude(stopListArray.getJSONObject(i).getString("lat"));
                    busStopList[i].setLongitude(stopListArray.getJSONObject(i).getString("lng"));
                    busStopList[i].setRouteOrder(stopListArray.getJSONObject(i).getInt("routeorder"));
                }

                stopsOnRouteSpinner.setAdapter(adapter);
                stopsOnRouteSpinner.setOnItemSelectedListener(this);
                stopsOnRouteSpinner.setSelection(nearestBusStopIndex);
                busStopSelectionLinearLayout.setVisibility(View.VISIBLE);
            }
            catch (JSONException r)
            {
                progressDialog.hide();
                r.printStackTrace();
                Toast.makeText(this, "Unknown error occurred!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        progressDialog.hide();
        if (isNetworkAvailable())
        {
            progressDialog = ProgressDialog.show(this, "Please wait", "Locating buses...");
            errorMessageTextView.setVisibility(View.GONE);
            busDetailsLinearLayout1.setVisibility(View.INVISIBLE);
            busDetailsLinearLayout2.setVisibility(View.INVISIBLE);
            busDetailsLinearLayout3.setVisibility(View.INVISIBLE);
            busDetailsLinearLayout4.setVisibility(View.INVISIBLE);
            this.position = position;
            String requestBody = "routeNO=" + route.getRouteNumber() + "&" + "direction=" + route.getDirection();
            new GetBusesEnRouteTask(this, busStopList[position]).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestBody);
        }
        else
        {
            errorMessageTextView.setText(R.string.error_connecting_to_the_internet_text);
            errorMessageTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
        Toast.makeText(this, "Please select a bus stop to track the bus!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBusesEnRouteFound(boolean isError, Bus[] buses, int numberOfBusesFound)
    {
        if (!isError)
        {
            if (!(numberOfBusesFound == 0))
            {
                for (int i = 0; i < buses.length; i++)
                {
                    for (int j = 0; j < routeBusStopList.length(); j++)
                    {
                        try
                        {
                            if (buses[i].getRouteOrder() == routeBusStopList.getJSONObject(j).getInt("routeorder"))
                            {
                                String stopName = routeBusStopList.getJSONObject(j).getString("busStopName");
                                if (routeBusStopList.getJSONObject(j).getString("busStopName").contains("("))
                                {
                                    stopName = routeBusStopList.getJSONObject(j).getString("busStopName").substring(0, routeBusStopList.getJSONObject(j).getString("busStopName").indexOf("("));
                                }
                                buses[i].setNameOfStopBusIsAt(stopName);
                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                if (isNetworkAvailable())
                {
                    new GetTimeToBusesTask(this, busStopList[position], numberOfBusesFound).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, buses);
                }
                else
                {
                    errorMessageTextView.setText(R.string.error_connecting_to_the_internet_text);
                    errorMessageTextView.setVisibility(View.VISIBLE);
                }
            }
            else
            {
                progressDialog.hide();
                errorMessageTextView.setText("There aren't any buses in this direction, please try again later...");
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            progressDialog.hide();
            errorMessageTextView.setText("There aren't any buses in this direction, please try again later...");
            errorMessageTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTimeToBusesFound(boolean isError, Bus[] buses)
    {
        ImageView busIcon1 = (ImageView) findViewById(R.id.bus_icon_1);
        ImageView busIcon2 = (ImageView) findViewById(R.id.bus_icon_2);
        ImageView busIcon3 = (ImageView) findViewById(R.id.bus_icon_3);
        ImageView busIcon4 = (ImageView) findViewById(R.id.bus_icon_4);
        if (route.getRouteNumber().contains("KIAS-"))
        {
            busIcon1.setImageResource(R.drawable.ic_flight_black);
            busIcon2.setImageResource(R.drawable.ic_flight_black);
            busIcon3.setImageResource(R.drawable.ic_flight_black);
            busIcon4.setImageResource(R.drawable.ic_flight_black);
        }
        else if (route.getRouteNumber().contains("V-"))
        {
            busIcon1.setImageResource(R.drawable.ic_directions_bus_ac);
            busIcon2.setImageResource(R.drawable.ic_directions_bus_ac);
            busIcon3.setImageResource(R.drawable.ic_directions_bus_ac);
            busIcon4.setImageResource(R.drawable.ic_directions_bus_ac);
        }
        else
        {
            busIcon1.setImageResource(R.drawable.ic_directions_bus_ordinary);
            busIcon2.setImageResource(R.drawable.ic_directions_bus_ordinary);
            busIcon3.setImageResource(R.drawable.ic_directions_bus_ordinary);
            busIcon4.setImageResource(R.drawable.ic_directions_bus_ordinary);
        }

        if (!isError)
        {
            int prevStopTime;
            int crntStopTime;
            int sorts;
            do
            {
                sorts = 0;
                if (buses[0] != null && buses[0].getTimeToBus() != null)
                {
                    if (buses[0].getTimeToBus().contains("hours"))
                    {
                        int hours = Integer.parseInt(buses[0].getTimeToBus().substring(0, buses[0].getTimeToBus().indexOf("h") - 1));
                        int minutes = 0;
                        if (buses[0].getTimeToBus().contains("min"))
                        {
                            minutes = Integer.parseInt(buses[0].getTimeToBus().substring(buses[0].getTimeToBus().indexOf("rs") + 3, buses[0].getTimeToBus().indexOf("m") - 1));
                        }
                        prevStopTime = (hours * 60) + minutes;
                    }
                    else if (buses[0].getTimeToBus().contains("hour"))
                    {
                        int hours = Integer.parseInt(buses[0].getTimeToBus().substring(0, buses[0].getTimeToBus().indexOf("h") - 1));
                        int minutes = 0;
                        if (buses[0].getTimeToBus().contains("min"))
                        {
                            minutes = Integer.parseInt(buses[0].getTimeToBus().substring(buses[0].getTimeToBus().indexOf("r") + 2, buses[0].getTimeToBus().indexOf("m") - 1));
                        }
                        prevStopTime = (hours * 60) + minutes;
                    }
                    else
                    {
                        prevStopTime = Integer.parseInt(buses[0].getTimeToBus().substring(0, buses[0].getTimeToBus().indexOf("m") - 1));
                    }
                }
                else
                {
                    break;
                }

                for (int i = 1; i < buses.length; i++)
                {
                    String timeToBus = buses[i].getTimeToBus();
                    if (buses[i] != null && timeToBus != null)
                    {
                        if (timeToBus.contains("hours"))
                        {
                            int hours = Integer.parseInt(timeToBus.substring(0, timeToBus.indexOf("h") - 1));
                            int minutes = 0;
                            if (timeToBus.contains("min"))
                            {
                                minutes = Integer.parseInt(timeToBus.substring(timeToBus.indexOf("rs") + 3, timeToBus.indexOf("m") - 1));
                            }
                            crntStopTime = (hours * 60) + minutes;
                        }
                        else if (timeToBus.contains("hour"))
                        {
                            int hours = Integer.parseInt(timeToBus.substring(0, timeToBus.indexOf("h") - 1));
                            int minutes = 0;
                            if (timeToBus.contains("min"))
                            {
                                minutes = Integer.parseInt(timeToBus.substring(timeToBus.indexOf("r") + 2, timeToBus.indexOf("m") - 1));
                            }
                            crntStopTime = (hours * 60) + minutes;
                        }
                        else
                        {
                            crntStopTime = Integer.parseInt(timeToBus.substring(0, timeToBus.indexOf("m") - 1));
                        }
                    }
                    else
                    {
                        break;
                    }

                    if (crntStopTime < prevStopTime)
                    {
                        sorts++;
                        buses[i - 1] = buses[i];
                        buses[i] = buses[i - 1];
                    }

                    if (buses[i] != null)
                    {
                        if (timeToBus.contains("hours"))
                        {
                            int hours = Integer.parseInt(timeToBus.substring(0, timeToBus.indexOf("h") - 1));
                            int minutes = 0;
                            if (timeToBus.contains("min"))
                            {
                                minutes = Integer.parseInt(timeToBus.substring(timeToBus.indexOf("rs") + 3, timeToBus.indexOf("m") - 1));
                            }
                            prevStopTime = (hours * 60) + minutes;
                        }
                        else if (timeToBus.contains("hour"))
                        {
                            int hours = Integer.parseInt(timeToBus.substring(0, timeToBus.indexOf("h") - 1));
                            int minutes = 0;
                            if (timeToBus.contains("min"))
                            {
                                minutes = Integer.parseInt(timeToBus.substring(timeToBus.indexOf("r") + 2, timeToBus.indexOf("m") - 1));
                            }
                            prevStopTime = (hours * 60) + minutes;
                        }
                        else
                        {
                            prevStopTime = Integer.parseInt(timeToBus.substring(0, timeToBus.indexOf("m") - 1));
                        }
                    }
                    else
                    {
                        break;
                    }
                }
            }
            while (sorts != 0);

            progressDialog.hide();
            if (buses[0].getTimeToBus() != null)
            {
                if (buses[0].getIsDue())
                {
                    busTimingTextView1.setText("is Due");
                }
                else
                {
                    busTimingTextView1.setText("in " + buses[0].getTimeToBus());
                }
                busIsAtTextView1.setText("currently near " + buses[0].getNameOfStopBusIsAt());
                busDetailsLinearLayout1.setVisibility(View.VISIBLE);
            }
            if (buses[1].getTimeToBus() != null)
            {
                if (buses[1].getIsDue())
                {
                    busTimingTextView2.setText("is Due");
                }
                else
                {
                    busTimingTextView2.setText("in " + buses[1].getTimeToBus());
                }
                busIsAtTextView2.setText("currently near " + buses[1].getNameOfStopBusIsAt());
                busDetailsLinearLayout2.setVisibility(View.VISIBLE);
            }
            if (buses[2].getTimeToBus() != null)
            {
                if (buses[2].getIsDue())
                {
                    busTimingTextView3.setText("is Due");
                }
                else
                {
                    busTimingTextView3.setText("in " + buses[2].getTimeToBus());
                }
                busIsAtTextView3.setText("currently near " + buses[2].getNameOfStopBusIsAt());
                busDetailsLinearLayout3.setVisibility(View.VISIBLE);
            }
            if (buses[3].getTimeToBus() != null)
            {
                if (buses[3].getIsDue())
                {
                    busTimingTextView4.setText("is Due");
                }
                else
                {
                    busTimingTextView4.setText("in " + buses[3].getTimeToBus());
                }
                busIsAtTextView4.setText("currently near " + buses[3].getNameOfStopBusIsAt());
                busDetailsLinearLayout4.setVisibility(View.VISIBLE);
            }
            if (buses[0].getTimeToBus() == null && buses[1].getTimeToBus() == null && buses[2].getTimeToBus() == null && buses[3].getTimeToBus() == null)
            {
                errorMessageTextView.setText("There are currently no buses in service! Please try again later.");
                errorMessageTextView.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            errorMessageTextView.setText("Could not locate buses! Please try again later...");
            errorMessageTextView.setVisibility(View.VISIBLE);
        }
        progressDialog.hide();
    }
}