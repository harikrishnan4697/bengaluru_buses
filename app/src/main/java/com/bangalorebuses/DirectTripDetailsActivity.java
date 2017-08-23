package com.bangalorebuses;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;

public class DirectTripDetailsActivity extends AppCompatActivity
{
    private DirectTripOld directTrip;

    private TextView originBusStopNameTextView;
    private ImageView refreshArrivalTimingsImageView;
    private ImageView routeServiceTypeImageView;
    private TextView routeNumberTextView;
    private TextView busArrivalTimeTextView;

    private TextView numberOfStopToRideTextView;
    private TextView travelTimeTextView;

    private TextView destinationBusStopNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direct_trip_details);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setTitle("Direct trip");
        }

        originBusStopNameTextView = (TextView) findViewById(R.id.originBusStopNameTextView);
        refreshArrivalTimingsImageView = (ImageView) findViewById(R.id.refreshImageView);
        routeServiceTypeImageView = (ImageView) findViewById(R.id.routeServiceTypeImageView);
        routeNumberTextView = (TextView) findViewById(R.id.routeNumberTextView);
        busArrivalTimeTextView = (TextView) findViewById(R.id.busETATextView);

        numberOfStopToRideTextView = (TextView) findViewById(R.id.numberOfStopsTextView);
        travelTimeTextView = (TextView) findViewById(R.id.busTravelTimeTextView);

        destinationBusStopNameTextView = (TextView) findViewById(R.id.destinationBusStopNameTextView);

        refreshArrivalTimingsImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                refreshBusArrivalTimings();
            }
        });

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        directTrip = (DirectTripOld) bundle.getSerializable("DIRECT_TRIP");

        setOriginBusStopDetails();

        setBusTravelDetails();

        setDestinationBusStopDetails();
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

    private void refreshBusArrivalTimings()
    {

    }

    private void setOriginBusStopDetails()
    {
        /*String busStopDirectionName = directTrip.getOriginStop().getBusStopDirectionName();

        if (busStopDirectionName.contains("(") && busStopDirectionName.contains(")"))
        {
            busStopDirectionName = busStopDirectionName.substring(busStopDirectionName
                    .indexOf("("), busStopDirectionName.indexOf(")") + 1);
        }

        originBusStopNameTextView.setText(directTrip.getOriginStop().getBusStopName() + " " +
                busStopDirectionName);

        routeNumberTextView.setText(directTrip.getRoute().getBusRouteNumber());

        if (directTrip.getRoute().getBusRouteNumber().length() > 5 &&
                directTrip.getRoute().getBusRouteNumber().contains("KIAS-"))
        {
            routeServiceTypeImageView.setImageResource(R.drawable.ic_flight_blue);
        }
        else if (directTrip.getRoute().getBusRouteNumber().length() > 1 &&
                directTrip.getRoute().getBusRouteNumber().substring(0, 2).equals("V-"))
        {
            routeServiceTypeImageView.setImageResource(R.drawable.ic_directions_bus_ac);
        }
        else if (directTrip.getRoute().getBusRouteNumber().contains("MF-"))
        {
            routeServiceTypeImageView.setImageResource(R.drawable.ic_directions_bus_special);
        }
        else
        {
            routeServiceTypeImageView.setImageResource(R.drawable.ic_directions_bus_ordinary);
        }

        String travelTimeAsText;
        if (!directTrip.getRoute().getBusRouteBuses().get(0).isDue())
        {
            if (directTrip.getRoute().getBusRouteBuses().get(0).getBusETA() >= 60)
            {
                int hours = directTrip.getRoute().getBusRouteBuses().get(0).getBusETA() / 60;
                travelTimeAsText = hours + " hr " + directTrip.getRoute().getBusRouteBuses().get(0).getBusETA() % 60 + " min";
            }
            else
            {
                travelTimeAsText = directTrip.getRoute().getBusRouteBuses().get(0).getBusETA() + " min";
            }
        }
        else
        {
            travelTimeAsText = "due";
        }

        busArrivalTimeTextView.setText(travelTimeAsText);*/
    }

    private void setBusTravelDetails()
    {
        /*int numberOfStopsBetweenOriginAndDestination = DbQueries.getNumberOfStopsBetweenRouteOrders(db,
                directTrip.getRoute().getBusRouteId(), directTrip.getOriginStop().getBusStopRouteOrder(),
                directTrip.getDestinationStop().getBusStopRouteOrder());

        numberOfStopToRideTextView.setText(numberOfStopsBetweenOriginAndDestination + " stops");

        int travelTimeInMinutes = calculateTravelTime(numberOfStopsBetweenOriginAndDestination,
                directTrip.getRoute().getBusRouteNumber());
        String travelTime;

        if (travelTimeInMinutes >= 60)
        {
            int hours = travelTimeInMinutes / 60;
            travelTime = hours + " hr " + travelTimeInMinutes % 60 + " min";
        }
        else
        {
            travelTime = travelTimeInMinutes + " min";
        }

        travelTimeTextView.setText(travelTime);*/
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

    private void setDestinationBusStopDetails()
    {
        destinationBusStopNameTextView.setText(directTrip.getDestinationBusStopName());
    }
}