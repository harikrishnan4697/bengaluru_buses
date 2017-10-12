package com.bangalorebuses.trips;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bangalorebuses.R;
import com.bangalorebuses.utils.CommonMethods;
import com.bangalorebuses.utils.Constants;

import java.util.ArrayList;
import java.util.List;

class IndirectTripsRecyclerViewAdapter extends RecyclerView
        .Adapter<IndirectTripsRecyclerViewAdapter.IndirectTripsViewHolder>
{
    private Activity context;
    private ArrayList<TransitPoint> transitPoints;

    public IndirectTripsRecyclerViewAdapter(Activity context, ArrayList<TransitPoint> transitPoints)
    {
        this.context = context;
        this.transitPoints = transitPoints;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public IndirectTripsViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new IndirectTripsViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.indirect_trip_card_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(final IndirectTripsViewHolder holder, final int position)
    {
        String mostFrequentBusRouteOnFirstLeg = transitPoints.get(position)
                .getMostFrequentBusRouteToTransitPoint().getBusRouteNumber();

        setTextViewBackgroundColor(holder.firstLegBusRouteNumberTextView,
                mostFrequentBusRouteOnFirstLeg);

        holder.firstLegBusRouteNumberTextView.setText(mostFrequentBusRouteOnFirstLeg);

        String mostFrequentBusRouteOnSecondLeg = transitPoints.get(position)
                .getMostFrequentBusRouteFromTransitPoint().getBusRouteNumber();

        setTextViewBackgroundColor(holder.secondLegBusRouteNumberTextView,
                mostFrequentBusRouteOnSecondLeg);

        holder.secondLegBusRouteNumberTextView.setText(mostFrequentBusRouteOnSecondLeg);

        holder.tripDurationTextView.setText(CommonMethods.convertMinutesToHoursAndMinutes(
                transitPoints.get(position).getShortestTripDuration()));

        holder.transitPointBusStopNameTextView.setText(transitPoints.get(position)
                .getTransitPointName());

        holder.cardView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent indirectTripDetailsActivityIntent = new Intent(
                        context, IndirectTripDetailsActivity.class);
                indirectTripDetailsActivityIntent.putExtra(Constants.ORIGIN_BUS_STOP_NAME, "");
                indirectTripDetailsActivityIntent.putExtra(Constants.TRANSIT_POINT_BUS_STOP_NAME, "");
                indirectTripDetailsActivityIntent.putExtra(Constants.DESTINATION_BUS_STOP_NAME, "");
                context.startActivity(indirectTripDetailsActivityIntent);
            }
        });
    }

    private void setTextViewBackgroundColor(TextView textView,
                                            String busRouteNumber)
    {
        if (busRouteNumber.length() > 5 && busRouteNumber.contains("KIAS-"))
        {
            textView.setBackgroundResource(R.drawable
                    .blue_rounded_background_borderless);
        }
        else if (busRouteNumber.length() > 1 &&
                busRouteNumber.substring(0, 2).equals("V-"))
        {
            textView.setBackgroundResource(R.drawable
                    .blue_rounded_background_borderless);
        }
        else if (busRouteNumber.contains("MF-"))
        {
            textView.setBackgroundResource(R.drawable
                    .orange_rounded_background_borderless);
        }
        else
        {
            textView.setBackgroundResource(R.drawable
                    .green_rounded_background_borderless);
        }
    }

    @Override
    public int getItemCount()
    {
        return transitPoints.size();
    }

    class IndirectTripsViewHolder extends RecyclerView.ViewHolder
    {
        CardView cardView;
        TextView tripDurationTextView;
        TextView firstLegBusRouteNumberTextView;
        TextView transitPointBusStopNameTextView;
        TextView secondLegBusRouteNumberTextView;

        IndirectTripsViewHolder(View itemView)
        {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.indirect_trip_card_layout);
            tripDurationTextView = (TextView) itemView.findViewById(R.id.indirect_trip_duration_text_view);
            firstLegBusRouteNumberTextView = (TextView) itemView.findViewById(R.id.segment1_bus_text_view);
            transitPointBusStopNameTextView = (TextView) itemView.findViewById(R.id.transit_point_name_text_view);
            secondLegBusRouteNumberTextView = (TextView) itemView.findViewById(R.id.segment2_bus_text_view);
        }
    }
}