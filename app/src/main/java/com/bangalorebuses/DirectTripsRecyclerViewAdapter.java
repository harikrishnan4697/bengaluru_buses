package com.bangalorebuses;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

class DirectTripsRecyclerViewAdapter extends RecyclerView.Adapter<DirectTripsRecyclerViewAdapter.DirectTripsViewHolder>
{
    private List<DirectTrip> directTrips;
    private ItemClickListener clickListener;

    DirectTripsRecyclerViewAdapter(List<DirectTrip> directTrips)
    {
        this.directTrips = directTrips;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public DirectTripsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.direct_trip_card_layout, viewGroup, false);
        return new DirectTripsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DirectTripsViewHolder directTripsViewHolder, int i)
    {
        String travelTimeAsText;
        if (directTrips.get(i).getShortestTravelTime() >= 60)
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

        directTripsViewHolder.busArrivalTimingsTextView.setText(directTrips.get(i).getNextThreeBusArrivals());
    }

    @Override
    public int getItemCount()
    {
        return directTrips.size();
    }

    void setClickListener(ItemClickListener itemClickListener)
    {
        this.clickListener = itemClickListener;
    }

    interface ItemClickListener
    {
        void onClick(View view, int position);
    }

    class DirectTripsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        CardView cardView;
        TextView tripDurationTextView;
        TextView tripOriginBusStopNameTextView;
        TextView busArrivalTimingsTextView;

        DirectTripsViewHolder(View itemView)
        {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.directTripCardView);
            tripDurationTextView = (TextView) itemView.findViewById(R.id.travelTimeTextView);
            tripOriginBusStopNameTextView = (TextView) itemView.findViewById(R.id.tripOriginStopNameTextView);
            busArrivalTimingsTextView = (TextView) itemView.findViewById(R.id.busArrivalTimingsTextView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            clickListener.onClick(v, getPosition());
        }
    }
}