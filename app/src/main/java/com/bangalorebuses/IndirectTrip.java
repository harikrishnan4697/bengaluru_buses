package com.bangalorebuses;

import android.view.View;

import static com.bangalorebuses.Constants.db;

class IndirectTrip extends Trip
{
    private int tripDuration;
    private Bus busToTransitPoint;
    private Bus busFromTransitPoint;

    private TransitPoint transitPoint;

    @Override
    public void showTrip(TripsRecyclerViewAdapter.TripsViewHolder holder)
    {
        holder.tripDurationTextView.setText(getTravelTime(tripDuration));

        if (getOriginBusStop().getBusStopDirectionName().contains(")"))
        {
            holder.tripOriginBusStopNameTextView.setText("From " + getOriginBusStop().getBusStopName() + " " + getOriginBusStop()
                    .getBusStopDirectionName().substring(0, getOriginBusStop().getBusStopDirectionName().indexOf(")") + 1));
        }
        else
        {
            holder.tripDurationTextView.setText("From " + getOriginBusStop().getBusStopName() + " " + getOriginBusStop().getBusStopDirectionName());
        }

        holder.firstLegBusRouteNumberTextView.setText(busToTransitPoint.getBusRoute().getBusRouteNumber());
        holder.secondLegBusRouteNumberTextView.setText(busFromTransitPoint.getBusRoute().getBusRouteNumber());

        holder.firstLegRideTheBusTextView.setText("Ride the bus for " + String.valueOf
                (DbQueries.getNumberOfStopsBetweenRouteOrders(db, busToTransitPoint.getBusRoute().getBusRouteId(), getOriginBusStop()
                        .getBusStopRouteOrder(), busToTransitPoint.getBusRoute().getTripPlannerDestinationBusStop().getBusStopRouteOrder())) + " stops");

        holder.secondLegRideTheBusTextView.setText("Ride the bus for " + String.valueOf
                (DbQueries.getNumberOfStopsBetweenRouteOrders(db, busFromTransitPoint.getBusRoute().getBusRouteId(), busFromTransitPoint
                        .getBusRoute().getTripPlannerOriginBusStop().getBusStopRouteOrder(), busFromTransitPoint.getBusRoute()
                        .getTripPlannerDestinationBusStop().getBusStopRouteOrder())) + " stops");

        if (busFromTransitPoint.getBusRoute().getTripPlannerOriginBusStop().getBusStopDirectionName().contains(")"))
        {
            holder.transitPointBusStopNameTextView.setText("Change buses at " + busFromTransitPoint.getBusRoute().getTripPlannerOriginBusStop().getBusStopName() + " " +
                    busFromTransitPoint.getBusRoute().getTripPlannerOriginBusStop().getBusStopDirectionName().substring(0, busFromTransitPoint
                            .getBusRoute().getTripPlannerOriginBusStop().getBusStopDirectionName().indexOf(")") + 1));
        }
        else
        {
            holder.transitPointBusStopNameTextView.setText("Change buses at " + busFromTransitPoint.getBusRoute().getTripPlannerOriginBusStop().getBusStopName() +
                    " " + busFromTransitPoint.getBusRoute().getTripPlannerOriginBusStop().getBusStopDirectionName());
        }


        if (busToTransitPoint.getBusRoute().getBusRouteNumber().length() > 5 && busToTransitPoint.getBusRoute().getBusRouteNumber().contains("KIAS-"))
        {
            holder.firstLegBusRouteServiceTypeImageView.setImageResource(R.drawable.ic_flight_blue);
        }
        else if (busToTransitPoint.getBusRoute().getBusRouteNumber().length() > 1 && busToTransitPoint.getBusRoute().getBusRouteNumber()
                .substring(0, 2).equals("V-"))
        {
            holder.firstLegBusRouteServiceTypeImageView.setImageResource(R.drawable.ic_directions_bus_ac);
        }
        else if (busToTransitPoint.getBusRoute().getBusRouteNumber().contains("MF-"))
        {
            holder.firstLegBusRouteServiceTypeImageView.setImageResource(R.drawable.ic_directions_bus_special);
        }
        else
        {
            holder.firstLegBusRouteServiceTypeImageView.setImageResource(R.drawable.ic_directions_bus_ordinary);
        }

        if (busFromTransitPoint.getBusRoute().getBusRouteNumber().length() > 5 && busFromTransitPoint.getBusRoute().getBusRouteNumber().contains("KIAS-"))
        {
            holder.secondLegBusRouteServiceTypeImageView.setImageResource(R.drawable.ic_flight_blue);
        }
        else if (busFromTransitPoint.getBusRoute().getBusRouteNumber().length() > 1 && busFromTransitPoint.getBusRoute().getBusRouteNumber()
                .substring(0, 2).equals("V-"))
        {
            holder.secondLegBusRouteServiceTypeImageView.setImageResource(R.drawable.ic_directions_bus_ac);
        }
        else if (busFromTransitPoint.getBusRoute().getBusRouteNumber().contains("MF-"))
        {
            holder.secondLegBusRouteServiceTypeImageView.setImageResource(R.drawable.ic_directions_bus_special);
        }
        else
        {
            holder.secondLegBusRouteServiceTypeImageView.setImageResource(R.drawable.ic_directions_bus_ordinary);
        }

        holder.firstLegBusETAsTextView.setText(String.valueOf(getTravelTime(busToTransitPoint.getBusETA())));
        holder.secondLegBusETAsTextView.setText(String.valueOf(getTravelTime(busFromTransitPoint.getBusETA())));

        holder.transitPointInfoLinearLayout.setVisibility(View.VISIBLE);
        holder.secondLegInfoRelativeLayout.setVisibility(View.VISIBLE);
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

    public Bus getBusFromTransitPoint()
    {
        return busFromTransitPoint;
    }

    public void setBusFromTransitPoint(Bus busFromTransitPoint)
    {
        this.busFromTransitPoint = busFromTransitPoint;
    }

    public TransitPoint getTransitPoint()
    {
        return transitPoint;
    }

    public void setTransitPoint(TransitPoint transitPoint)
    {
        this.transitPoint = transitPoint;
    }

    public Bus getBusToTransitPoint()
    {
        return busToTransitPoint;
    }

    public void setBusToTransitPoint(Bus busToTransitPoint)
    {
        this.busToTransitPoint = busToTransitPoint;
    }

    public int getTripDuration()
    {
        return tripDuration;
    }

    public void setTripDuration(int tripDuration)
    {
        this.tripDuration = tripDuration;
    }
}