package com.bangalorebuses.search;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.bangalorebuses.R;
import com.bangalorebuses.core.BusStop;

import java.util.ArrayList;

public class AllBusStopSearchListCustomAdaptor extends BaseAdapter implements Filterable
{
    public Activity context;
    private ArrayList<BusStop> busStops = null;
    private ArrayList<BusStop> filteredBusStops = null;
    private LayoutInflater inflater;
    private AllBusStopSearchListCustomAdaptor.ItemFilter mFilter = new AllBusStopSearchListCustomAdaptor.ItemFilter();

    public AllBusStopSearchListCustomAdaptor(Activity context, ArrayList<BusStop> busStops)
    {
        super();
        this.context = context;
        this.busStops = busStops;
        this.filteredBusStops = busStops;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount()
    {
        return filteredBusStops.size();
    }

    public Object getItem(int position)
    {
        return filteredBusStops.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        AllBusStopSearchListCustomAdaptor.ViewHolder holder;
        if (convertView == null)
        {
            holder = new AllBusStopSearchListCustomAdaptor.ViewHolder();
            convertView = inflater.inflate(R.layout.all_bus_stops_search_list_item, null);
            holder.busStopNameTextView = (TextView) convertView.findViewById(R.id.busStopNameTextView);
            holder.busStopDirectionNameTextView = (TextView) convertView.findViewById(R.id.busStopDirectionNameTextView);
            convertView.setTag(holder);
        }
        else
        {
            holder = (AllBusStopSearchListCustomAdaptor.ViewHolder) convertView.getTag();
        }

        holder.busStopNameTextView.setText(filteredBusStops.get(position).getBusStopName());
        String busStopDirectionName = filteredBusStops.get(position).getBusStopDirectionName();
        if (busStopDirectionName.contains("(") && busStopDirectionName.contains(")"))
        {
            holder.busStopDirectionNameTextView.setText(busStopDirectionName.substring(busStopDirectionName
                    .indexOf("(") + 1, busStopDirectionName.indexOf(")")));
        }
        else
        {
            holder.busStopDirectionNameTextView.setText(busStopDirectionName);
        }
        return convertView;
    }

    public Filter getFilter()
    {
        return mFilter;
    }

    private static class ViewHolder
    {
        TextView busStopNameTextView;
        TextView busStopDirectionNameTextView;
    }

    private class ItemFilter extends Filter
    {
        @Override
        protected FilterResults performFiltering(CharSequence constraint)
        {

            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();

            int count = busStops.size();
            final ArrayList<BusStop> filtered_bus_stops = new ArrayList<>(count);

            BusStop busStop;

            for (int i = 0; i < count; i++)
            {
                busStop = busStops.get(i);
                if (busStop.getBusStopName().toLowerCase().contains(filterString))
                {
                    filtered_bus_stops.add(busStop);
                }
            }
            results.values = filtered_bus_stops;
            results.count = filtered_bus_stops.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            filteredBusStops = (ArrayList<BusStop>) results.values;
            notifyDataSetChanged();
        }

    }
}
