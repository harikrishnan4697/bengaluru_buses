package com.bangalorebuses;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

class TripsRecyclerViewAdapter extends RecyclerView.Adapter<TripsRecyclerViewAdapter.TripsViewHolder>
{
    private List<Trip> trips;

    TripsRecyclerViewAdapter(List<Trip> trips)
    {
        this.trips = trips;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public TripsViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new TripsViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.trip_card_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(TripsViewHolder holder, int position)
    {
        trips.get(position).showTrip(holder);
    }

    @Override
    public int getItemCount()
    {
        return trips.size();
    }

    class TripsViewHolder extends RecyclerView.ViewHolder
    {
        TextView tripDurationTextView;
        TextView tripOriginBusStopNameTextView;

        ImageView firstLegBusRouteServiceTypeImageView;
        TextView firstLegBusRouteNumberTextView;
        TextView firstLegBusETAsTextView;
        TextView firstLegRideTheBusTextView;

        LinearLayout transitPointInfoLinearLayout;
        TextView transitPointBusStopNameTextView;

        RelativeLayout secondLegInfoRelativeLayout;
        ImageView secondLegBusRouteServiceTypeImageView;
        TextView secondLegBusRouteNumberTextView;
        TextView secondLegBusETAsTextView;
        TextView secondLegRideTheBusTextView;

        TripsViewHolder(View itemView)
        {
            super(itemView);
            // TODO check if works without card view
            tripDurationTextView = (TextView) itemView.findViewById(R.id.totalTravelDurationTextView);
            tripOriginBusStopNameTextView = (TextView) itemView.findViewById(R.id.originBusStopNameTextView);

            firstLegBusRouteServiceTypeImageView = (ImageView) itemView.findViewById(R.id.firstLegBusServiceTypeImageView);
            firstLegBusRouteNumberTextView = (TextView) itemView.findViewById(R.id.firstLegBusRouteNumberTextView);
            firstLegBusETAsTextView = (TextView) itemView.findViewById(R.id.firstLegBusArrivalTimingsTextView);
            firstLegRideTheBusTextView = (TextView) itemView.findViewById(R.id.firstLegRideTheBusTextView);

            transitPointInfoLinearLayout = (LinearLayout) itemView.findViewById(R.id.transitPointInfoLinearLayout);
            transitPointBusStopNameTextView = (TextView) itemView.findViewById(R.id.transitPointBusStopNameTextView);

            secondLegInfoRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.tripSecondLegRelativeLayout);
            secondLegBusRouteServiceTypeImageView = (ImageView) itemView.findViewById(R.id.secondLegBusServiceTypeImageView);
            secondLegBusRouteNumberTextView = (TextView) itemView.findViewById(R.id.secondlegBusRouteNumberTextView);
            secondLegBusETAsTextView = (TextView) itemView.findViewById(R.id.secondLegBusArrivalTimingsTextView);
            secondLegRideTheBusTextView = (TextView) itemView.findViewById(R.id.secondLegRideTheBusTextView);
        }
    }
}