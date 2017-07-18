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
    private List<String> routeTypes = null;
    private List<String> filteredRouteTypes = null;
    private LayoutInflater inflater;
    private ItemFilter mFilter = new ItemFilter();
    private List<String> routeNumbers = null;
    private List<String> filteredRouteNumbers = null;

    BusNumberListCustomAdapter(Activity context, String[] routeNumbers, String[] routeTypes)
    {
        super();
        this.context = context;
        this.routeTypes = Arrays.asList(routeTypes);
        this.filteredRouteTypes = Arrays.asList(routeTypes);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.routeNumbers = Arrays.asList(routeNumbers);
        this.filteredRouteNumbers = Arrays.asList(routeNumbers);
    }

    @Override
    public int getCount()
    {
        return filteredRouteNumbers.size();
    }

    public Object getItem(int position)
    {
        return filteredRouteNumbers.get(position);
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
            convertView = inflater.inflate(R.layout.search_list_item, null);

            holder.imgViewLogo = (ImageView) convertView.findViewById(R.id.imageView);
            holder.txtViewRouteNumber = (TextView) convertView.findViewById(R.id.routeNumberTextView);
            holder.txtViewRouteType = (TextView) convertView.findViewById(R.id.routeTypeTextView);

            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        if (filteredRouteTypes.get(position).equals("Vayu Vajra"))
        {
            holder.imgViewLogo.setImageResource(R.drawable.ic_flight_black);
        }
        else if (filteredRouteTypes.get(position).equals("Vajra"))
        {
            holder.imgViewLogo.setImageResource(R.drawable.ic_directions_bus_ac);
        }
        else if (filteredRouteTypes.get(position).equals("Ordinary"))
        {
            if (filteredRouteNumbers.get(position).contains("CHAKRA-") || filteredRouteNumbers.get(position).contains("MF-"))
            {
                holder.imgViewLogo.setImageResource(R.drawable.ic_directions_bus_special);
            }
            else
            {
                holder.imgViewLogo.setImageResource(R.drawable.ic_directions_bus_ordinary);
            }
        }
        else
        {
            holder.imgViewLogo.setImageResource(R.drawable.ic_directions_bus_special);
        }
        holder.txtViewRouteNumber.setText(filteredRouteNumbers.get(position));
        holder.txtViewRouteType.setText(filteredRouteTypes.get(position));

        return convertView;
    }

    public Filter getFilter()
    {
        return mFilter;
    }

    private static class ViewHolder
    {
        ImageView imgViewLogo;
        TextView txtViewRouteNumber;
        TextView txtViewRouteType;
    }

    private class ItemFilter extends Filter
    {
        @Override
        protected FilterResults performFiltering(CharSequence constraint)
        {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            int count = routeNumbers.size();
            final ArrayList<String> filtered_route_numbers = new ArrayList<>(count);
            final ArrayList<String> filtered_route_types = new ArrayList<>(count);

            String routeNumber;
            String routeType;

            for (int i = 0; i < count; i++)
            {
                routeNumber = routeNumbers.get(i);
                routeType = routeTypes.get(i);
                if (routeNumber.toLowerCase().contains(filterString))
                {
                    filtered_route_numbers.add(routeNumber);
                    filtered_route_types.add(routeType);
                }
            }

            filteredRouteTypes = filtered_route_types;
            results.values = filtered_route_numbers;
            results.count = filtered_route_numbers.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            filteredRouteNumbers = (ArrayList<String>) results.values;
            notifyDataSetChanged();
        }

    }
}