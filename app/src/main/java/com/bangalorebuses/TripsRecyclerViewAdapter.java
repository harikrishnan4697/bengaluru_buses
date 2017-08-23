package com.bangalorebuses;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
                R.layout.direct_trip_card_layout, parent, false));
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

        CardView cardView;
        TextView tripDurationTextView;
        TextView tripOriginBusStopNameTextView;
        ImageView busRouteServiceTypeImageView;
        TextView busRouteNumberTextView;
        TextView busETAsTextView;
        TextView numberOfStopsToTravelTextView;

        TripsViewHolder(View itemView)
        {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.directTripCardView);
            tripDurationTextView = (TextView) itemView.findViewById(R.id.totalTravelDurationTextView);
            tripOriginBusStopNameTextView = (TextView) itemView.findViewById(R.id.originBusStopNameTextView);
            busRouteServiceTypeImageView = (ImageView) itemView.findViewById(R.id.busServiceTypeImageView);
            busRouteNumberTextView = (TextView) itemView.findViewById(R.id.busRouteNumberTextView);
            busETAsTextView = (TextView) itemView.findViewById(R.id.busArrivalTimingsTextView);
            numberOfStopsToTravelTextView = (TextView) itemView.findViewById(R.id.rideTheBusTextView);
        }
    }
}