package com.bangalorebuses.trips;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bangalorebuses.R;
import com.bangalorebuses.utils.CommonMethods;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

class IndirectTripDetailsRecyclerViewAdapter extends RecyclerView
        .Adapter<IndirectTripDetailsRecyclerViewAdapter.IndirectTripsDetailsViewHolder>
{
    private ArrayList<IndirectTrip> indirectTrips;

    IndirectTripDetailsRecyclerViewAdapter(ArrayList<IndirectTrip> indirectTrips)
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
                .getDirectTripOnFirstLeg().getOriginBusStop().getBusStopName();
        String originBusStopDirectionName = indirectTrips.get(position)
                .getDirectTripOnFirstLeg().getOriginBusStop().getBusStopDirectionName();

        holder.originBusStopNameTextView.setText(CommonMethods
                .getBusStopNameAndDirectionNameCombined(originBusStopName,
                        originBusStopDirectionName));

        // Set the transit point bus stop name and direction name text view
        String transitPointBusStopName = indirectTrips.get(position)
                .getDirectTripOnSecondLeg().getOriginBusStop().getBusStopName();
        String transitPointBusStopDirectionName = indirectTrips.get(position)
                .getDirectTripOnSecondLeg().getOriginBusStop().getBusStopDirectionName();

        holder.transitPointBusStopNameTextView.setText(CommonMethods
                .getBusStopNameAndDirectionNameCombined(transitPointBusStopName,
                        transitPointBusStopDirectionName));

        // Set the first leg bus route number text view
        holder.firstLegBusRouteNumberTextView.setText(indirectTrips.get(position)
                .getDirectTripOnFirstLeg().getBusRoute().getBusRouteNumber());

        holder.firstLegBusRouteNumberTextView.setBackgroundResource(CommonMethods
                .getBusRouteNumberBackgroundResId(indirectTrips.get(position)
                        .getDirectTripOnFirstLeg().getBusRoute().getBusRouteNumber()));

        // Set the first leg bus route service type image view
        holder.firstLegBusRouteServiceTypeImageView.setImageResource(CommonMethods
                .getBusRouteServiceTypeImageResId(indirectTrips.get(position)
                        .getDirectTripOnFirstLeg().getBusRoute().getBusRouteNumber()));

        // Set the first leg bus ETA text view
        holder.firstLegBusRouteETATextView.setText(CommonMethods.convertMinutesToBusArrivalTimings(
                indirectTrips.get(position).getDirectTripOnFirstLeg().getBusRoute().getBusRouteBuses()
                        .get(0).getBusETA()));

        // Set the second leg bus route number text view
        holder.secondLegBusRouteNumberTextView.setText(indirectTrips.get(position)
                .getDirectTripOnSecondLeg().getBusRoute().getBusRouteNumber());

        holder.secondLegBusRouteNumberTextView.setBackgroundResource(CommonMethods
                .getBusRouteNumberBackgroundResId(indirectTrips.get(position)
                        .getDirectTripOnSecondLeg().getBusRoute().getBusRouteNumber()));

        // Set the second leg bus route service type image view
        holder.secondLegBusRouteServiceTypeImageView.setImageResource(CommonMethods
                .getBusRouteServiceTypeImageResId(indirectTrips.get(position)
                        .getDirectTripOnSecondLeg().getBusRoute().getBusRouteNumber()));

        Calendar calendar = Calendar.getInstance();

        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);
        int currentTimeInMinutesSinceMidnight = (currentHour * 60) + currentMinute;

        int secondLegBusETAInMinutesSinceMidnight = currentTimeInMinutesSinceMidnight
                + indirectTrips.get(position).getDirectTripOnSecondLeg().getBusRoute()
                .getBusRouteBuses().get(0).getBusETA();
        int secondLegBusETAHour = secondLegBusETAInMinutesSinceMidnight / 60;
        int secondLegBusETAMinute = secondLegBusETAInMinutesSinceMidnight % 60;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, secondLegBusETAHour);
        cal.set(Calendar.MINUTE, secondLegBusETAMinute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String secondLegBusETA = sdf.format(cal.getTime());

        // Set the second leg bus ETA text view
        holder.secondLegBusRouteETATextView.setText(secondLegBusETA);
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
                    .transit_point_bus_stop_name_text_view);
        }
    }
}