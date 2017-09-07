package com.bangalorebuses.nearby;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bangalorebuses.R;
import com.bangalorebuses.core.BusStop;

import java.util.List;

public class NearbyBusStopsRecyclerViewAdapter extends RecyclerView.Adapter<NearbyBusStopsRecyclerViewAdapter.NearbyBusStopViewHolder>
{
    private List<BusStop> nearbyBusStops;
    private ItemClickListener clickListener;

    public NearbyBusStopsRecyclerViewAdapter(List<BusStop> nearbyBusStops)
    {
        this.nearbyBusStops = nearbyBusStops;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public NearbyBusStopViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.nearby_bus_stop_card_layout, viewGroup, false);
        return new NearbyBusStopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NearbyBusStopViewHolder nearbyBusStopsViewHolder, int i)
    {
        nearbyBusStopsViewHolder.busStopNameTextView.setText(nearbyBusStops.get(i).getBusStopName());
        nearbyBusStopsViewHolder.busStopDirectionNameTextView.setText(nearbyBusStops.get(i).getBusStopDirectionName());
        nearbyBusStopsViewHolder.busStopDistanceTextView.setText(nearbyBusStops.get(i).getBusStopDistance());

        if (nearbyBusStops.get(i).isAirportShuttleStop())
        {
            nearbyBusStopsViewHolder.airportShuttleImageView.setVisibility(View.VISIBLE);
        }
        else
        {
            nearbyBusStopsViewHolder.airportShuttleImageView.setVisibility(View.GONE);
        }

        if (nearbyBusStops.get(i).isMetroFeederStop())
        {
            nearbyBusStopsViewHolder.metroFeederImageView.setVisibility(View.VISIBLE);
        }
        else
        {
            nearbyBusStopsViewHolder.metroFeederImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount()
    {
        return nearbyBusStops.size();
    }

    public void setClickListener(ItemClickListener itemClickListener)
    {
        this.clickListener = itemClickListener;
    }

    public interface ItemClickListener
    {
        void onClick(View view, int position);
    }

    public class NearbyBusStopViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        CardView cardView;
        TextView busStopNameTextView;
        TextView busStopDirectionNameTextView;
        TextView busStopDistanceTextView;
        ImageView airportShuttleImageView;
        ImageView metroFeederImageView;

        NearbyBusStopViewHolder(View itemView)
        {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.nearbyBusStopCardView);
            busStopNameTextView = (TextView) itemView.findViewById(R.id.busStopNameTextView);
            busStopDirectionNameTextView = (TextView) itemView.findViewById(R.id.busStopDirectionTextView);
            busStopDistanceTextView = (TextView) itemView.findViewById(R.id.busStopDistanceTextView);
            airportShuttleImageView = (ImageView) itemView.findViewById(R.id.airportShuttleImageView);
            metroFeederImageView = (ImageView) itemView.findViewById(R.id.metroFeederImageView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            clickListener.onClick(v, getPosition());
        }
    }
}