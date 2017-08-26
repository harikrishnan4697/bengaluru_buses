package com.bangalorebuses;

import android.view.View;

import static com.bangalorebuses.Constants.db;

class DirectTrip extends Trip
{
    @Override
    public void showTrip(TripsRecyclerViewAdapter.TripsViewHolder holder)
    {
        holder.tripDurationTextView.setText(getTravelTime(getBusRoutes().get(0).getShortestOriginToDestinationTravelTime()));

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

        if (getBusRoutes().get(0).getBusRouteNumber().length() > 5 &&
                getBusRoutes().get(0).getBusRouteNumber().contains("KIAS-"))
        {
            holder.firstLegBusRouteServiceTypeImageView.setImageResource(R.drawable.ic_flight_blue);
        }
        else if (getBusRoutes().get(0).getBusRouteNumber().length() > 1 &&
                getBusRoutes().get(0).getBusRouteNumber().substring(0, 2).equals("V-"))
        {
            holder.firstLegBusRouteServiceTypeImageView.setImageResource(R.drawable.ic_directions_bus_ac);
        }
        else if (getBusRoutes().get(0).getBusRouteNumber().contains("MF-"))
        {
            holder.firstLegBusRouteServiceTypeImageView.setImageResource(R.drawable.ic_directions_bus_special);
        }
        else
        {
            holder.firstLegBusRouteServiceTypeImageView.setImageResource(R.drawable.ic_directions_bus_ordinary);
        }

        holder.firstLegBusRouteNumberTextView.setText(getBusRoutes().get(0).getBusRouteNumber());

        String nextThreeBuses = getETAAsString(getBusRoutes().get(0).getBusRouteBuses().get(0));
        for (int busCount = 1; busCount < 3; busCount++)
        {
            if (busCount < getBusRoutes().get(0).getBusRouteBuses().size())
            {
                nextThreeBuses = nextThreeBuses + ", " + getETAAsString(getBusRoutes().get(0).getBusRouteBuses().get(busCount));
            }
            else
            {
                break;
            }
        }
        holder.firstLegBusETAsTextView.setText(nextThreeBuses);

        holder.firstLegRideTheBusTextView.setText("Ride the bus for " + String.valueOf
                (DbQueries.getNumberOfStopsBetweenRouteOrders(db, getBusRoutes().get(0).getBusRouteId(),
                        getBusRoutes().get(0).getTripPlannerOriginBusStop()
                                .getBusStopRouteOrder(), getBusRoutes().get(0).getTripPlannerDestinationBusStop()
                                .getBusStopRouteOrder())) + " stops");

        holder.transitPointInfoLinearLayout.setVisibility(View.GONE);
        holder.secondLegInfoRelativeLayout.setVisibility(View.GONE);
    }

    private String getETAAsString(Bus bus)
    {
        if (!bus.isDue())
        {
            String travelTime;

            if (bus.getBusETA() >= 60)
            {
                int hours = bus.getBusETA() / 60;
                travelTime = hours + " hr " + bus.getBusETA() % 60 + " min";
            }
            else
            {
                travelTime = bus.getBusETA() + " min";
            }
            return travelTime;
        }
        else
        {
            return "due";
        }
    }

    private String getTravelTime(int travelTimeInMins)
    {
        String travelTime;

        if (travelTimeInMins >= 60)
        {
            int hours = travelTimeInMins / 60;
            travelTime = hours + " hr " + travelTimeInMins % 60 + " min";
        }
        else
        {
            travelTime = travelTimeInMins + " min";
        }
        return travelTime;
    }















        /*String travelTimeAsText;
        if (getShortestTravelTime() >= 60)
        {
            int hours = directTrips.get(i).getShortestTravelTime() / 60;
            travelTimeAsText = hours + " hr " + directTrips.get(i).getShortestTravelTime() % 60 + " min";
        }
        else
        {
            travelTimeAsText = directTrips.get(i).getShortestTravelTime() + " min";
        }

        directTripsViewHolder.tripDurationTextView.setText(travelTimeAsText);

        if (directTrips.get(i).getOriginStop().getBusStopDirectionName().contains(")"))
        {
            directTripsViewHolder.tripOriginBusStopNameTextView.setText("From " + directTrips.get(i).getOriginStop().getBusStopName()
                    + " " + directTrips.get(i).getOriginStop().getBusStopDirectionName().substring(0,
                    directTrips.get(i).getOriginStop().getBusStopDirectionName().indexOf(")") + 1));
        }
        else
        {
            directTripsViewHolder.tripOriginBusStopNameTextView.setText("From " + directTrips.get(i).getOriginStop().getBusStopName()
                    + " " + directTrips.get(i).getOriginStop().getBusStopDirectionName());
        }

        String nextThreeBuses = getETAAsString(directTrips.get(i).getBusesOnDirectTrip().get(0));
        for (int busCount = 1; busCount < 3; busCount++)
        {
            if (busCount < directTrips.get(i).getBusesOnDirectTrip().size())
            {
                nextThreeBuses = nextThreeBuses + ", " + getETAAsString(directTrips.get(i).getBusesOnDirectTrip().get(busCount));
            }
            else
            {
                break;
            }
        }
        directTripsViewHolder.busArrivalTimingsTextView.setText(nextThreeBuses);
    }

    private String getETAAsString(Bus bus)
    {
        if (!bus.isDue())
        {
            String travelTime;

            if (bus.getBusETA() >= 60)
            {
                int hours = bus.getBusETA() / 60;
                travelTime = hours + " hr " + bus.getBusETA() % 60 + " min";
            }
            else
            {
                travelTime = bus.getBusETA() + " min";
            }
            return travelTime;
        }
        else
        {
            return "due";
        }
    }*/
}