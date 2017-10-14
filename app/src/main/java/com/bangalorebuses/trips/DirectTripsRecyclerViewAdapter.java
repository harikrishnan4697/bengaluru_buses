package com.bangalorebuses.trips;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bangalorebuses.R;
import com.bangalorebuses.utils.Constants;

import java.util.ArrayList;

class DirectTripsRecyclerViewAdapter extends RecyclerView
        .Adapter<DirectTripsRecyclerViewAdapter.DirectTripsViewHolder>
{
    private Activity context;
    private ArrayList<DirectTrip> directTrips;

    public DirectTripsRecyclerViewAdapter(Activity context, ArrayList<DirectTrip> directTrips)
    {
        this.context = context;
        this.directTrips = directTrips;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public DirectTripsViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new DirectTripsViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.direct_trip_card_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(final DirectTripsViewHolder holder, int position)
    {
        int travelTime = directTrips.get(position).getBusRoute()
                .getShortestOriginToDestinationTravelTime();
        String travelTimeAsText;
        if (travelTime >= 60)
        {
            int hours = travelTime
                    / 60;
            travelTimeAsText = hours + " hr " + travelTime % 60 + " min";
        }
        else
        {
            travelTimeAsText = travelTime + " min";
        }
        holder.tripDurationTextView.setText(travelTimeAsText);

        String busRouteNumber = directTrips.get(position)
                .getBusRoute().getBusRouteNumber();
        holder.busNumberTextView.setText(busRouteNumber);
        if (busRouteNumber.length() > 5 && busRouteNumber.contains("KIAS-"))
        {
            holder.busServiceTypeImageView.setImageResource(R.drawable
                    .ic_flight_blue);
            holder.busNumberTextView.setBackgroundResource(R.drawable
                    .blue_rounded_background_borderless);
        }
        else if (busRouteNumber.length() > 1 && (busRouteNumber.substring(0, 2)
                .equals("V-") || busRouteNumber.substring(0, 2).equals("C-")))
        {
            holder.busServiceTypeImageView.setImageResource(R.drawable
                    .ic_directions_bus_ac);
            holder.busNumberTextView.setBackgroundResource(R.drawable
                    .blue_rounded_background_borderless);
        }
        else if (busRouteNumber.contains("MF-"))
        {
            holder.busServiceTypeImageView.setImageResource(R.drawable
                    .ic_directions_bus_special);
            holder.busNumberTextView.setBackgroundResource(R.drawable
                    .orange_rounded_background_borderless);
        }
        else
        {
            holder.busServiceTypeImageView.setImageResource(R.drawable
                    .ic_directions_bus_ordinary);
            holder.busNumberTextView.setBackgroundResource(R.drawable
                    .green_rounded_background_borderless);
        }

        String nextThreeBuses = getTravelTime(directTrips.get(position).getBusRoute()
                .getBusRouteBuses().get(0).getBusETA());
        for (int busCount = 1; busCount < 3; busCount++)
        {
            if (busCount < directTrips.get(position).getBusRoute()
                    .getBusRouteBuses().size())
            {
                nextThreeBuses = nextThreeBuses + ", " + getTravelTime(
                        directTrips.get(position).getBusRoute().getBusRouteBuses()
                                .get(busCount).getBusETA());
            }
            else
            {
                break;
            }
        }
        holder.busETATextView.setText(nextThreeBuses);

        if (directTrips.get(position).getOriginBusStop().getBusStopDirectionName().contains(")"))
        {
            holder.originBusStopNameTextView.setText(directTrips.get(position)
                    .getOriginBusStop().getBusStopName() + " " + directTrips.get(position)
                    .getOriginBusStop().getBusStopDirectionName().substring(0, directTrips
                            .get(position).getOriginBusStop().getBusStopDirectionName()
                            .indexOf(")") + 1));
        }
        else
        {
            holder.originBusStopNameTextView.setText(directTrips.get(position)
                    .getOriginBusStop().getBusStopName() + " " + directTrips.get(position)
                    .getOriginBusStop().getBusStopDirectionName());
        }
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

    @Override
    public int getItemCount()
    {
        return directTrips.size();
    }

    class DirectTripsViewHolder extends RecyclerView.ViewHolder
    {
        CardView cardView;
        TextView tripDurationTextView;
        TextView originBusStopNameTextView;
        TextView busNumberTextView;
        TextView busETATextView;
        ImageView busServiceTypeImageView;

        DirectTripsViewHolder(View itemView)
        {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.indirect_trip_card_layout);
            tripDurationTextView = (TextView) itemView.findViewById(R.id.direct_trip_duration_text_view);
            originBusStopNameTextView = (TextView) itemView.findViewById(R.id.origin_bus_stop_name_text_view);
            busNumberTextView = (TextView) itemView.findViewById(R.id.bus_number_text_view);
            busETATextView = (TextView) itemView.findViewById(R.id.bus_eta_text_view);
            busServiceTypeImageView = (ImageView) itemView.findViewById(R.id.bus_service_type_image_view);
        }
    }
}