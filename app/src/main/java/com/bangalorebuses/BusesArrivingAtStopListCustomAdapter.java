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
import java.util.List;

class BusesArrivingAtStopListCustomAdapter extends BaseAdapter
{
    public Activity context;
    private List<String> destinations = null;
    private LayoutInflater inflater;
    private List<String> routeNumbers = null;
    private List<String> busETAs = null;

    BusesArrivingAtStopListCustomAdapter(Activity context, ArrayList<String> routeNumbers, ArrayList<String> destinations, ArrayList<String> busETAs)
    {
        super();
        this.context = context;
        this.routeNumbers = routeNumbers;
        this.destinations = destinations;
        this.busETAs = busETAs;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount()
    {
        return routeNumbers.size();
    }

    public Object getItem(int position)
    {
        return routeNumbers.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;
        if (convertView == null)
        {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.buses_arriving_at_stop_list_item, null);

            holder.imgViewLogo = (ImageView) convertView.findViewById(R.id.imageView);
            holder.txtViewRouteNumber = (TextView) convertView.findViewById(R.id.routeNumberTextView);
            holder.txtViewDestination = (TextView) convertView.findViewById(R.id.routeDestinationTextView);
            holder.txtViewETA = (TextView) convertView.findViewById(R.id.busETATextView);

            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        if (routeNumbers.get(position).contains("KIAS-"))
        {
            holder.imgViewLogo.setImageResource(R.drawable.ic_flight_black);
        }
        else if (routeNumbers.get(position).length() > 1 && routeNumbers.get(position).substring(0, 2).equals("V-"))
        {
            holder.imgViewLogo.setImageResource(R.drawable.ic_directions_bus_ac);
        }
        else if (routeNumbers.get(position).contains("MF-") || routeNumbers.get(position).contains("CHAKRA-"))
        {
            holder.imgViewLogo.setImageResource(R.drawable.ic_directions_bus_special);
        }
        else
        {
            holder.imgViewLogo.setImageResource(R.drawable.ic_directions_bus_ordinary);
        }

        holder.txtViewRouteNumber.setText(routeNumbers.get(position));
        holder.txtViewDestination.setText(destinations.get(position));
        holder.txtViewETA.setText(busETAs.get(position));
        return convertView;
    }

    private static class ViewHolder
    {
        ImageView imgViewLogo;
        TextView txtViewRouteNumber;
        TextView txtViewDestination;
        TextView txtViewETA;
    }
}