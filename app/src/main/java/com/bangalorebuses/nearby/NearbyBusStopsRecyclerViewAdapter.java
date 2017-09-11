package com.bangalorebuses.nearby;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
        TextView busStopNameTextView;
        TextView busStopDirectionNameTextView;
        TextView busStopDistanceTextView;
        LinearLayout busesArrivingAtBusStopLinearLayout;

        NearbyBusStopViewHolder(View itemView)
        {
            super(itemView);
            busStopNameTextView = (TextView) itemView.findViewById(R.id.nearby_bus_stop_name_text_view);
            busStopDirectionNameTextView = (TextView) itemView.findViewById(R.id.nearby_bus_stop_direction_text_view);
            busStopDistanceTextView = (TextView) itemView.findViewById(R.id.nearby_bus_stop_distance_text_view);
            busesArrivingAtBusStopLinearLayout = (LinearLayout) itemView.findViewById(R.id
                    .bus_routes_arriving_at_nearby_bus_stop_linear_layout);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            clickListener.onClick(v, getPosition());
        }
    }
}