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
import java.util.Arrays;
import java.util.List;

class BusNumberListCustomAdapter extends BaseAdapter implements Filterable
{
    public Activity context;
    private List<BusRoute> busRoutes = null;
    private List<BusRoute> filteredBusRoutes = null;
    private LayoutInflater inflater;
    private ItemFilter mFilter = new ItemFilter();

    BusNumberListCustomAdapter(Activity context, ArrayList<BusRoute> busRoutes)
    {
        super();
        this.context = context;
        this.busRoutes = busRoutes;
        this.filteredBusRoutes = busRoutes;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount()
    {
        return filteredBusRoutes.size();
    }

    public Object getItem(int position)
    {
        return filteredBusRoutes.get(position);
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
            //holder.routeDirectionNameTextView = (TextView) convertView.findViewById(R.id.routeDirectionNameTextView);

            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        if (filteredBusRoutes.get(position).getBusRouteNumber().contains("KIAS-"))
        {
            holder.imageView.setImageResource(R.drawable.ic_flight_blue);
            holder.routeServiceTypeNameTextView.setText("Airport Shuttle");
        }
        else if (filteredBusRoutes.get(position).getBusRouteNumber().length() > 2 &&
                filteredBusRoutes.get(position).getBusRouteNumber().substring(0, 2).equals("V-"))
        {
            holder.imageView.setImageResource(R.drawable.ic_directions_bus_ac);
            holder.routeServiceTypeNameTextView.setText("A/C");
        }
        else if (filteredBusRoutes.get(position).getBusRouteNumber().contains("MF-"))
        {
            holder.imageView.setImageResource(R.drawable.ic_directions_bus_special);
            holder.routeServiceTypeNameTextView.setText("Metro Feeder");
        }
        else
        {
            holder.imageView.setImageResource(R.drawable.ic_directions_bus_ordinary);
            holder.routeServiceTypeNameTextView.setText("Non A/C");
        }

        holder.routeNumberTextView.setText(filteredBusRoutes.get(position).getBusRouteNumber());
        holder.routeDirectionNameTextView.setText(filteredBusRoutes.get(position).getBusRouteDirectionName());

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
        TextView routeDirectionNameTextView;
    }

    private class ItemFilter extends Filter
    {
        @Override
        protected FilterResults performFiltering(CharSequence constraint)
        {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            int count = busRoutes.size();
            final ArrayList<BusRoute> filtered_routes = new ArrayList<>(count);

            BusRoute busRoute;

            for (int i = 0; i < count; i++)
            {
                busRoute = busRoutes.get(i);
                if (busRoute.getBusRouteNumber().toLowerCase().contains(filterString))
                {
                    filtered_routes.add(busRoute);
                }
            }

            results.values = filtered_routes;
            results.count = filtered_routes.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            filteredBusRoutes = (ArrayList<BusRoute>) results.values;
            notifyDataSetChanged();
        }

    }
}