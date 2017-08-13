package com.bangalorebuses;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

class BusNumberListCustomAdapterRouteNumber extends BaseAdapter implements Filterable
{
    public Activity context;
    private List<String> busRouteNumbers = null;
    private List<String> filteredBusRouteNumbers = null;
    private LayoutInflater inflater;
    private ItemFilter mFilter = new ItemFilter();

    BusNumberListCustomAdapterRouteNumber(Activity context, ArrayList<String> busRouteNumbers)
    {
        super();
        this.context = context;
        this.busRouteNumbers = busRouteNumbers;
        this.filteredBusRouteNumbers = busRouteNumbers;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount()
    {
        return filteredBusRouteNumbers.size();
    }

    public Object getItem(int position)
    {
        return filteredBusRouteNumbers.get(position);
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
            convertView = inflater.inflate(R.layout.bus_number_search_list_item, null);

            holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            holder.routeNumberTextView = (TextView) convertView.findViewById(R.id.routeNumberTextView);
            holder.routeServiceTypeNameTextView = (TextView) convertView.findViewById(R.id.routeServiceTypeTextView);

            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        if (filteredBusRouteNumbers.get(position).contains("KIAS-"))
        {
            holder.imageView.setImageResource(R.drawable.ic_flight_blue);
            holder.routeServiceTypeNameTextView.setText("Airport Shuttle");
        }
        else if (filteredBusRouteNumbers.get(position).length() > 2 &&
                filteredBusRouteNumbers.get(position).substring(0, 2).equals("V-"))
        {
            holder.imageView.setImageResource(R.drawable.ic_directions_bus_ac);
            holder.routeServiceTypeNameTextView.setText("A/C");
        }
        else if (filteredBusRouteNumbers.get(position).contains("MF-"))
        {
            holder.imageView.setImageResource(R.drawable.ic_directions_bus_special);
            holder.routeServiceTypeNameTextView.setText("Metro Feeder");
        }
        else
        {
            holder.imageView.setImageResource(R.drawable.ic_directions_bus_ordinary);
            holder.routeServiceTypeNameTextView.setText("Non A/C");
        }

        holder.routeNumberTextView.setText(filteredBusRouteNumbers.get(position));

        return convertView;
    }

    public Filter getFilter()
    {
        return mFilter;
    }

    private static class ViewHolder
    {
        ImageView imageView;
        TextView routeNumberTextView;
        TextView routeServiceTypeNameTextView;
    }

    private class ItemFilter extends Filter
    {
        @Override
        protected FilterResults performFiltering(CharSequence constraint)
        {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            int count = busRouteNumbers.size();
            final ArrayList<String> filtered_route_numbers = new ArrayList<>(count);

            String routeNumber;

            for (int i = 0; i < count; i++)
            {
                routeNumber = busRouteNumbers.get(i);
                if (routeNumber.toLowerCase().contains(filterString))
                {
                    filtered_route_numbers.add(routeNumber);
                }
            }

            results.values = filtered_route_numbers;
            results.count = filtered_route_numbers.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            filteredBusRouteNumbers = (ArrayList<String>) results.values;
            notifyDataSetChanged();
        }

    }
}