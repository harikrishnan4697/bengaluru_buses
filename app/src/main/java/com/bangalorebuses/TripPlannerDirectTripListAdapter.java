package com.bangalorebuses;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class TripPlannerDirectTripListAdapter extends BaseAdapter
{
    public Activity context;
    private ArrayList<DirectTripOld> directTrips = null;
    private LayoutInflater inflater;

    TripPlannerDirectTripListAdapter(Activity context, ArrayList<DirectTripOld> directTrips)
    {
        super();
        this.context = context;
        this.directTrips = directTrips;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount()
    {
        return directTrips.size();
    }

    public Object getItem(int position)
    {
        return directTrips.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        /*TripPlannerDirectTripListAdapter.ViewHolder holder;
        if (convertView == null)
        {
            holder = new TripPlannerDirectTripListAdapter.ViewHolder();
            convertView = inflater.inflate(R.layout.trip_planner_direct_trip_list_item, null);

            holder.routeServiceTypeImageView = (ImageView) convertView.findViewById(R.id.busImageView);
            holder.travelTimeTextView = (TextView) convertView.findViewById(R.id.travelTimeTextView);
            holder.originBusStopNameTextView = (TextView) convertView.findViewById(R.id.originBusStopNameTextView);

            convertView.setTag(holder);
        }
        else
        {
            holder = (TripPlannerDirectTripListAdapter.ViewHolder) convertView.getTag();
        }
        if (directTrips.get(position).getRoute().getBusRouteNumber().length() > 5 &&
                directTrips.get(position).getRoute().getBusRouteNumber().contains("KIAS-"))
        {
            holder.routeServiceTypeImageView.setImageResource(R.drawable.ic_flight_blue);
        }
        else if (directTrips.get(position).getRoute().getBusRouteNumber().length() > 1 &&
                directTrips.get(position).getRoute().getBusRouteNumber().substring(0, 2).equals("V-"))
        {
            holder.routeServiceTypeImageView.setImageResource(R.drawable.ic_directions_bus_ac);
        }
        else if (directTrips.get(position).getRoute().getBusRouteNumber().contains("MF-"))
        {
            holder.routeServiceTypeImageView.setImageResource(R.drawable.ic_directions_bus_special);
        }
        else
        {
            holder.routeServiceTypeImageView.setImageResource(R.drawable.ic_directions_bus_ordinary);
        }


        String travelTimeAsText;
        if (directTrips.get(position).getTravelTime() >= 60)
        {
            int hours = directTrips.get(position).getTravelTime() / 60;
            travelTimeAsText = hours + " hr " + directTrips.get(position).getTravelTime() % 60 + " min";
        }
        else
        {
            travelTimeAsText = directTrips.get(position).getTravelTime() + " min";
        }
        holder.travelTimeTextView.setText(travelTimeAsText);

        if (directTrips.get(position).getOriginStop().getBusStopDirectionName().contains(")"))
        {
            holder.originBusStopNameTextView.setText("From " + directTrips.get(position).getOriginStop().getBusStopName()
                    + " " + directTrips.get(position).getOriginStop().getBusStopDirectionName().substring(0,
                    directTrips.get(position).getOriginStop().getBusStopDirectionName().indexOf(")") + 1));
        }
        else
        {
            holder.originBusStopNameTextView.setText("From " + directTrips.get(position).getOriginStop().getBusStopName()
                    + " " + directTrips.get(position).getOriginStop().getBusStopDirectionName());
        }*/
        return convertView;
    }

    private static class ViewHolder
    {
        ImageView routeServiceTypeImageView;
        TextView travelTimeTextView;
        TextView originBusStopNameTextView;
    }
}
