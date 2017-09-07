package com.bangalorebuses.trips;

import android.view.View;

import com.bangalorebuses.R;
import com.bangalorebuses.core.BusRoute;
import com.bangalorebuses.utils.DbQueries;

import static com.bangalorebuses.utils.Constants.db;

public class DirectTrip extends Trip
{
    private BusRoute busRoute;

    public BusRoute getBusRoute()
    {
        return busRoute;
    }

    public void setBusRoute(BusRoute busRoute)
    {
        this.busRoute = busRoute;
    }

    @Override
    public void showTrip(TripsRecyclerViewAdapter.TripsViewHolder holder)
    {
        holder.tripDurationTextView.setText(getTravelTime(busRoute.getShortestOriginToDestinationTravelTime()));

        if (getOriginBusStop().getBusStopDirectionName().contains(")"))
        {
            holder.tripOriginBusStopNameTextView.setText("From " + getOriginBusStop().getBusStopName() +
                    " " + getOriginBusStop().getBusStopDirectionName().substring(0,
                    getOriginBusStop().getBusStopDirectionName().indexOf(")") + 1));
        }
        else
        {
            holder.tripDurationTextView.setText("From " + getOriginBusStop().getBusStopName() +
                    " " + getOriginBusStop().getBusStopDirectionName());
        }

        if (busRoute.getBusRouteNumber().length() > 5 && busRoute.getBusRouteNumber().contains("KIAS-"))
        {
            holder.firstLegBusRouteServiceTypeImageView.setImageResource(R.drawable.ic_flight_blue);
        }
        else if (busRoute.getBusRouteNumber().length() > 1 && busRoute.getBusRouteNumber().substring(0, 2).equals("V-"))
        {
            holder.firstLegBusRouteServiceTypeImageView.setImageResource(R.drawable.ic_directions_bus_ac);
        }
        else if (busRoute.getBusRouteNumber().contains("MF-"))
        {
            holder.firstLegBusRouteServiceTypeImageView.setImageResource(R.drawable.ic_directions_bus_special);
        }
        else
        {
            holder.firstLegBusRouteServiceTypeImageView.setImageResource(R.drawable.ic_directions_bus_ordinary);
        }

        holder.firstLegBusRouteNumberTextView.setText(busRoute.getBusRouteNumber());

        String nextThreeBuses = getTravelTime(busRoute.getBusRouteBuses().get(0).getBusETA());
        for (int busCount = 1; busCount < 3; busCount++)
        {
            if (busCount < busRoute.getBusRouteBuses().size())
            {
                nextThreeBuses = nextThreeBuses + ", " + getTravelTime(busRoute.getBusRouteBuses().get(busCount).getBusETA());
            }
            else
            {
                break;
            }
        }
        holder.firstLegBusETAsTextView.setText(nextThreeBuses);

        holder.firstLegRideTheBusTextView.setText("Ride the bus for " + String.valueOf
                (DbQueries.getNumberOfStopsBetweenRouteOrders(db, busRoute.getBusRouteId(), busRoute.getTripPlannerOriginBusStop()
                        .getBusStopRouteOrder(), busRoute.getTripPlannerDestinationBusStop()
                        .getBusStopRouteOrder())) + " stops");

        holder.transitPointInfoLinearLayout.setVisibility(View.GONE);
        holder.secondLegInfoRelativeLayout.setVisibility(View.GONE);
    }

    private String getTravelTime(int travelTimeInMinutes)
    {
        String travelTime;

        if (travelTimeInMinutes >= 60)
        {
            int hours = travelTimeInMinutes / 60;
            travelTime = hours + " hr " + travelTimeInMinutes % 60 + " min";
        }
        else if (travelTimeInMinutes == 0)
        {
            travelTime = "Due";
        }
        else
        {
            travelTime = travelTimeInMinutes + " min";
        }
        return travelTime;
    }
}