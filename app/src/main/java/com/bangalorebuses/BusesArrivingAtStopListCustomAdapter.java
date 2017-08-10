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

class BusesArrivingAtStopListCustomAdapter extends BaseAdapter
{
    public Activity context;
    private ArrayList<BusRoute> busRoutes = null;
    private LayoutInflater inflater;

    BusesArrivingAtStopListCustomAdapter(Activity context, ArrayList<BusRoute> busRoutes)
    {
        super();
        this.context = context;
        this.busRoutes = busRoutes;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount()
    {
        return busRoutes.size();
    }

    public Object getItem(int position)
    {
        return busRoutes.get(position);
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

        if (busRoutes.get(position).getBusRouteNumber().length() > 5 &&
                busRoutes.get(position).getBusRouteNumber().contains("KIAS-"))
        {
            holder.imgViewLogo.setImageResource(R.drawable.ic_flight_blue);
        }
        else if (busRoutes.get(position).getBusRouteNumber().length() > 1 &&
                busRoutes.get(position).getBusRouteNumber().substring(0, 2).equals("V-"))
        {
            holder.imgViewLogo.setImageResource(R.drawable.ic_directions_bus_ac);
        }
        else if (busRoutes.get(position).getBusRouteNumber().contains("MF-"))
        {
            holder.imgViewLogo.setImageResource(R.drawable.ic_directions_bus_special);
        }
        else
        {
            holder.imgViewLogo.setImageResource(R.drawable.ic_directions_bus_ordinary);
        }

        holder.txtViewRouteNumber.setText(busRoutes.get(position).getBusRouteNumber());
        String busRouteDestinationName = busRoutes.get(position).getBusRouteDirectionName();
        if (busRouteDestinationName.contains(" To "))
        {
            busRouteDestinationName = busRouteDestinationName.substring(busRouteDestinationName.indexOf("To "), busRouteDestinationName.length());
        }
        else if (busRouteDestinationName.contains(" to "))
        {
            busRouteDestinationName = busRouteDestinationName.substring(busRouteDestinationName.indexOf("to "), busRouteDestinationName.length());
        }
        holder.txtViewDestination.setText(busRouteDestinationName);

        String busETAs = "";
        for (Bus bus : busRoutes.get(position).getBusRouteBuses())
        {
            if (!busETAs.equals(""))
            {
                busETAs = busETAs + ", ";
            }
            String travelTimeAsText;

            if (!bus.isDue())
            {
                if (bus.getBusETA() >= 60)
                {
                    int hours = bus.getBusETA() / 60;
                    travelTimeAsText = hours + " hr " + bus.getBusETA() % 60 + " min";
                }
                else
                {
                    travelTimeAsText = bus.getBusETA() + " min";
                }
                busETAs = busETAs + travelTimeAsText;
            }
            else
            {
                busETAs = busETAs + "due";
            }
        }
        holder.txtViewETA.setText(busETAs);
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