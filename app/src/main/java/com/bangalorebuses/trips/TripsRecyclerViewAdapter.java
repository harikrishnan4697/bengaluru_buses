package com.bangalorebuses.trips;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bangalorebuses.R;

import java.util.List;

public class TripsRecyclerViewAdapter extends RecyclerView.Adapter<TripsRecyclerViewAdapter.TripsViewHolder>
{
    private List<Trip> trips;

    public TripsRecyclerViewAdapter(List<Trip> trips)
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

    public class TripsViewHolder extends RecyclerView.ViewHolder
    {
        public TextView tripDurationTextView;
        public TextView tripOriginBusStopNameTextView;

        public ImageView firstLegBusRouteServiceTypeImageView;
        public TextView firstLegBusRouteNumberTextView;
        public TextView firstLegBusETAsTextView;
        public TextView firstLegRideTheBusTextView;

        public LinearLayout transitPointInfoLinearLayout;
        public TextView transitPointBusStopNameTextView;

        public RelativeLayout secondLegInfoRelativeLayout;
        public ImageView secondLegBusRouteServiceTypeImageView;
        public TextView secondLegBusRouteNumberTextView;
        public TextView secondLegBusETAsTextView;
        public TextView secondLegRideTheBusTextView;

        public TripsViewHolder(View itemView)
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
            secondLegBusRouteNumberTextView = (TextView) itemView.findViewById(R.id.secondLegBusRouteNumberTextView);
            secondLegBusETAsTextView = (TextView) itemView.findViewById(R.id.secondLegBusArrivalTimingsTextView);
            secondLegRideTheBusTextView = (TextView) itemView.findViewById(R.id.secondLegRideTheBusTextView);
        }
    }
}