package com.bangalorebuses.busstops;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bangalorebuses.R;
import com.bangalorebuses.busarrivals.BusesArrivingAtBusStopActivity;
import com.bangalorebuses.core.BusStop;

import java.util.List;

public class NearbyBusStopsRecyclerViewAdapter extends RecyclerView.Adapter<NearbyBusStopsRecyclerViewAdapter.NearbyBusStopViewHolder> {
    private List<BusStop> nearbyBusStops;
    private Context context;

    public NearbyBusStopsRecyclerViewAdapter(Context context, List<BusStop> nearbyBusStops) {
        this.nearbyBusStops = nearbyBusStops;
        this.context = context;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public NearbyBusStopViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.nearby_bus_stop_card_layout, viewGroup, false);
        return new NearbyBusStopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NearbyBusStopViewHolder nearbyBusStopsViewHolder, final int position) {
        nearbyBusStopsViewHolder.busStopNameTextView.setText(nearbyBusStops.get(position).getBusStopName());
        nearbyBusStopsViewHolder.busStopDirectionNameTextView.setText(nearbyBusStops.get(position).getBusStopDirectionName());
        nearbyBusStopsViewHolder.busStopDistanceTextView.setText(nearbyBusStops.get(position).getBusStopDistance());

        nearbyBusStopsViewHolder.busesArrivingAtBusStopLinearLayout.removeAllViews();
        nearbyBusStopsViewHolder.busesArrivingAtBusStopLinearLayout.setVisibility(View.GONE);
        for (int i = 0; i < 5; i++) {
            if (i < nearbyBusStops.get(position).getBusesArrivingAtBusStop().size()) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 5, 0);

                TextView textView = new TextView(context);
                textView.setBackgroundResource(R.drawable.green_rounded_background_borderless);
                textView.setTypeface(null, Typeface.BOLD);
                textView.setText(nearbyBusStops.get(position).getBusesArrivingAtBusStop().get(i)
                        .getBusRouteNumber());
                textView.setEllipsize(TextUtils.TruncateAt.END);
                textView.setTextColor(Color.WHITE);
                textView.setMaxLines(1);
                textView.setLayoutParams(params);
                nearbyBusStopsViewHolder.busesArrivingAtBusStopLinearLayout.addView(textView);
                nearbyBusStopsViewHolder.busesArrivingAtBusStopLinearLayout.setVisibility(View.VISIBLE);
            }
        }

        nearbyBusStopsViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getBusesArrivingAtBusStopIntent = new Intent(context, BusesArrivingAtBusStopActivity.class);
                getBusesArrivingAtBusStopIntent.putExtra("BUS_STOP_NAME", nearbyBusStops.get(position).getBusStopName());
                getBusesArrivingAtBusStopIntent.putExtra("BUS_STOP_DIRECTION_NAME", nearbyBusStops.get(position).getBusStopDirectionName());
                getBusesArrivingAtBusStopIntent.putExtra("BUS_STOP_ID", nearbyBusStops.get(position).getBusStopId());
                context.startActivity(getBusesArrivingAtBusStopIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (nearbyBusStops == null) {
            return 0;
        }

        return nearbyBusStops.size();
    }

    class NearbyBusStopViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView busStopNameTextView;
        TextView busStopDirectionNameTextView;
        TextView busStopDistanceTextView;
        LinearLayout busesArrivingAtBusStopLinearLayout;

        NearbyBusStopViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.nearbyBusStopCardView);
            busStopNameTextView = itemView.findViewById(R.id.nearby_bus_stop_name_text_view);
            busStopDirectionNameTextView = itemView.findViewById(R.id.nearby_bus_stop_direction_text_view);
            busStopDistanceTextView = itemView.findViewById(R.id.nearby_bus_stop_distance_text_view);
            busesArrivingAtBusStopLinearLayout = itemView.findViewById(R.id
                    .bus_routes_arriving_at_nearby_bus_stop_linear_layout);
        }
    }
}