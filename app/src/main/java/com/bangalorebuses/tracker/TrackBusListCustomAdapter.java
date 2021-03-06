package com.bangalorebuses.tracker;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bangalorebuses.R;
import com.bangalorebuses.core.Bus;

import java.util.ArrayList;

public class TrackBusListCustomAdapter extends BaseAdapter
{
    public Activity context;
    private ArrayList<Bus> buses = null;
    private LayoutInflater inflater;

    public TrackBusListCustomAdapter(Activity context, ArrayList<Bus> buses)
    {
        super();
        this.context = context;
        this.buses = buses;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount()
    {
        return buses.size();
    }

    public Object getItem(int position)
    {
        return buses.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        TrackBusListCustomAdapter.ViewHolder holder;
        if (convertView == null)
        {
            holder = new TrackBusListCustomAdapter.ViewHolder();
            convertView = inflater.inflate(R.layout.track_bus_list_item, null);
            holder.routeTypeImageView = (ImageView) convertView.findViewById(R.id.imageView);
            holder.busRouteNumberTextView = (TextView) convertView.findViewById(R.id.routeNumberTextView);
            holder.busETATextView = (TextView) convertView.findViewById(R.id.busETATextView);
            holder.currentlyNearTextView = (TextView) convertView.findViewById(R.id.currentlyNearTextView);
            holder.busRegistrationNumberTextView = (TextView) convertView.findViewById(R.id.registrationNumberTextView);
            convertView.setTag(holder);
        }
        else
        {
            holder = (TrackBusListCustomAdapter.ViewHolder) convertView.getTag();
        }

        if (buses.get(position).getBusRoute().getBusRouteNumber().length() > 5 &&
                buses.get(position).getBusRoute().getBusRouteNumber().contains("KIAS-"))
        {
            holder.routeTypeImageView.setImageResource(R.drawable.ic_flight_blue);
            holder.busRouteNumberTextView.setBackgroundResource(R.drawable
                    .blue_rounded_background_borderless);
        }
        else if (buses.get(position).getBusRoute().getBusRouteNumber().length() > 1 &&
                (buses.get(position).getBusRoute().getBusRouteNumber().substring(0, 2).equals("V-")
                        || buses.get(position).getBusRoute().getBusRouteNumber().substring(0, 2).equals("C-")))
        {
            holder.routeTypeImageView.setImageResource(R.drawable.ic_directions_bus_ac);
            holder.busRouteNumberTextView.setBackgroundResource(R.drawable
                    .blue_rounded_background_borderless);

        }
        else if (buses.get(position).getBusRoute().getBusRouteNumber().contains("MF-"))
        {
            holder.routeTypeImageView.setImageResource(R.drawable.ic_directions_bus_special);
            holder.busRouteNumberTextView.setBackgroundResource(R.drawable
                    .orange_rounded_background_borderless);
        }
        else
        {
            holder.routeTypeImageView.setImageResource(R.drawable.ic_directions_bus_ordinary);
            holder.busRouteNumberTextView.setBackgroundResource(R.drawable
                    .green_rounded_background_borderless);
        }

        holder.busRouteNumberTextView.setText(buses.get(position).getBusRoute().getBusRouteNumber());

        String busETA;
        if (buses.get(position).isDue())
        {
            busETA = "due";
        }
        else if (buses.get(position).getBusRouteOrder() == 1)
        {
            busETA = "at origin";
        }
        else
        {
            if (buses.get(position).getBusETA() >= 60)
            {
                int hours = buses.get(position).getBusETA() / 60;
                busETA = hours + " hr " + buses.get(position).getBusETA() % 60 + " min";
            }
            else
            {
                busETA = buses.get(position).getBusETA() + " min";
            }
        }

        holder.busETATextView.setText(busETA);
        holder.currentlyNearTextView.setText("Currently near - " + buses.get(position).getBusCurrentlyNearBusStop());
        holder.busRegistrationNumberTextView.setText("Registration number - " + buses.get(position).getBusRegistrationNumber());
        return convertView;
    }

    private static class ViewHolder
    {
        ImageView routeTypeImageView;
        TextView busRouteNumberTextView;
        TextView busETATextView;
        TextView currentlyNearTextView;
        TextView busRegistrationNumberTextView;
    }

}
