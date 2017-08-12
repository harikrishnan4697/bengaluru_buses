package com.bangalorebuses;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class TrackBusListCustomAdapter extends BaseAdapter
{
    public Activity context;
    private ArrayList<Bus> buses = null;
    private LayoutInflater inflater;

    TrackBusListCustomAdapter(Activity context, ArrayList<Bus> buses)
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
            holder.busRouteNumberTextView = (TextView) convertView.findViewById(R.id.busStopNameTextView);
            holder.busETATextView = (TextView) convertView.findViewById(R.id.busStopDirectionNameTextView);
            holder.currentlyNearTextView = (TextView) convertView.findViewById(R.id.currentlyNearTextView);
            holder.busRegistrationNumberTextView = (TextView) convertView.findViewById(R.id.registrationNumberTextView);
            convertView.setTag(holder);
        }
        else
        {
            holder = (TrackBusListCustomAdapter.ViewHolder) convertView.getTag();
        }

        holder.busRouteNumberTextView.setText(buses.get(position).getBusRoute().getBusRouteNumber());
        String busETA;
        if (!buses.get(position).isDue())
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
        else
        {
            busETA = "due";
        }
        holder.busETATextView.setText(busETA);
        holder.currentlyNearTextView.setText(buses.get(position).getBusCurrentlyNearBusStop());
        holder.busRegistrationNumberTextView.setText(buses.get(position).getBusRegistrationNumber());
        return convertView;
    }

    private static class ViewHolder
    {
        TextView busRouteNumberTextView;
        TextView busETATextView;
        TextView currentlyNearTextView;
        TextView busRegistrationNumberTextView;
    }

}
