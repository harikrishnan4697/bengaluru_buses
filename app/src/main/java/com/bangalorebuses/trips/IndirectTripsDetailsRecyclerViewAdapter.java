package com.bangalorebuses.trips;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bangalorebuses.R;
import com.bangalorebuses.utils.CommonMethods;
import com.bangalorebuses.utils.Constants;

import org.w3c.dom.Text;

import java.util.ArrayList;

class IndirectTripsDetailsRecyclerViewAdapter extends RecyclerView
        .Adapter<IndirectTripsDetailsRecyclerViewAdapter.IndirectTripsDetailsViewHolder>
{
    private ArrayList<IndirectTrip> indirectTrips;

    public IndirectTripsDetailsRecyclerViewAdapter(ArrayList<IndirectTrip> indirectTrips)
    {
        this.indirectTrips = indirectTrips;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public IndirectTripsDetailsViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new IndirectTripsDetailsViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.indirect_trip_details_card_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(final IndirectTripsDetailsViewHolder holder, int position)
    {
        // Set the total indirect trip duration text view
        holder.tripDurationTextView.setText(CommonMethods.convertMinutesToHoursAndMinutes(
                indirectTrips.get(position).getTripDuration()));

        // Set the origin bus stop name and direction name text view
        String originBusStopName = indirectTrips.get(position)
                .getOriginBusStop().getBusStopName();
        String originBusStopDirectionName = indirectTrips.get(position)
                .getOriginBusStop().getBusStopDirectionName();

        holder.originBusStopNameTextView.setText(CommonMethods
                .getBusStopNameAndDirectionNameCombined(originBusStopName,
                        originBusStopDirectionName));

        // Set the transit point bus stop name and direction name text view
        String transitPointBusStopName = indirectTrips.get(position)
                .getTransitPoint().getTransitPointName();
        String transitPointBusStopDirectionName = indirectTrips.get(position)
                .getTransitPoint().getTransitPointDirectionName();

        holder.transitPointBusStopNameTextView.setText(CommonMethods
                .getBusStopNameAndDirectionNameCombined(transitPointBusStopName,
                        transitPointBusStopDirectionName));

        // Set the first leg bus route number text view
        holder.firstLegBusRouteNumberTextView.setText(indirectTrips.get(position)
                .getBusOnFirstLeg().getBusRoute().getBusRouteNumber());

        // Set the second leg bus route number text view
        holder.secondLegBusRouteNumberTextView.setText(indirectTrips.get(position)
                .getBusOnSecondLeg().getBusRoute().getBusRouteNumber());

        // Set the first leg bus ETA text view
        /*holder.firstLegBusRouteETATextView.setText(indirectTrips.get(position)
                .getBusOnFirstLeg().getBusETA());*/

        // TODO Set the second leg bus ETA text view

        // Set the first leg bus route service type image view
        holder.firstLegBusRouteServiceTypeImageView.setImageResource(CommonMethods
                .getBusRouteServiceTypeImageResId(indirectTrips.get(position)
                        .getBusOnFirstLeg().getBusRoute().getBusRouteNumber()));

        // Set the second leg bus route service type image view
        holder.secondLegBusRouteServiceTypeImageView.setImageResource(CommonMethods
                .getBusRouteServiceTypeImageResId(indirectTrips.get(position)
                        .getBusOnSecondLeg().getBusRoute().getBusRouteNumber()));
    }

    @Override
    public int getItemCount()
    {
        return indirectTrips.size();
    }

    class IndirectTripsDetailsViewHolder extends RecyclerView.ViewHolder
    {
        TextView tripDurationTextView;
        ImageView firstLegBusRouteServiceTypeImageView;
        TextView firstLegBusRouteNumberTextView;
        TextView firstLegBusRouteETATextView;
        ImageView secondLegBusRouteServiceTypeImageView;
        TextView secondLegBusRouteNumberTextView;
        TextView secondLegBusRouteETATextView;
        TextView transitPointBusStopNameTextView;
        TextView originBusStopNameTextView;


        IndirectTripsDetailsViewHolder(View itemView)
        {
            super(itemView);
            tripDurationTextView = (TextView) itemView.findViewById(R.id
                    .indirect_trip_duration_text_view);
            firstLegBusRouteServiceTypeImageView = (ImageView) itemView.findViewById(R.id
                    .first_leg_bus_service_type_image_view);
            firstLegBusRouteNumberTextView = (TextView) itemView.findViewById(R.id
                    .first_leg_bus_number_text_view);
            firstLegBusRouteETATextView = (TextView) itemView.findViewById(R.id
                    .first_leg_bus_eta_text_view);
            secondLegBusRouteServiceTypeImageView = (ImageView) itemView.findViewById(R.id
                    .second_leg_bus_service_type_image_view);
            secondLegBusRouteNumberTextView = (TextView) itemView.findViewById(R.id
                    .second_leg_bus_number_text_view);
            secondLegBusRouteETATextView = (TextView) itemView.findViewById(R.id
                    .second_leg_bus_eta_text_view);
            originBusStopNameTextView = (TextView) itemView.findViewById(R.id
                    .origin_bus_stop_name_text_view);
            transitPointBusStopNameTextView = (TextView) itemView.findViewById(R.id
                    .transit_point__bus_stop_name_text_view);
        }
    }
}