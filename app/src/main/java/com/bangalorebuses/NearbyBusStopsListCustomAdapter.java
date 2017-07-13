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

class NearbyBusStopsListCustomAdapter extends BaseAdapter
{
    public Activity context;
    private List<String> busStopDistances = null;
    private LayoutInflater inflater;
    private List<String> busStopNames = null;

    NearbyBusStopsListCustomAdapter(Activity context, ArrayList<String> busStopNames, ArrayList<String> busStopDistances)
    {
        super();
        this.context = context;
        this.busStopNames = busStopNames;
        this.busStopDistances = busStopDistances;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount()
    {
        return busStopNames.size();
    }

    public Object getItem(int position)
    {
        return busStopNames.get(position);
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
            convertView = inflater.inflate(R.layout.nearby_bus_stop_list_item, null);

            holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            holder.busStopNameTextView = (TextView) convertView.findViewById(R.id.busStopNameTextView);
            holder.busStopDirectionTextView = (TextView) convertView.findViewById(R.id.busStopDirectionTextView);
            holder.busStopDistanceTextView = (TextView) convertView.findViewById(R.id.busStopDistTextView);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.imageView.setImageResource(R.drawable.ic_directions_walk_black);
        if (busStopNames.get(position).contains("(") && busStopNames.get(position).contains(")"))
        {
            holder.busStopNameTextView.setText(busStopNames.get(position).substring(0, busStopNames.get(position).indexOf("(")));
            holder.busStopDirectionTextView.setText(busStopNames.get(position).substring(busStopNames.get(position).indexOf("(") + 1, busStopNames.get(position).indexOf(")")));
        }
        else
        {
            holder.busStopNameTextView.setText(busStopNames.get(position));
        }
        holder.busStopDistanceTextView.setText((int)(Float.parseFloat(busStopDistances.get(position)) * 1000) + " metres away");
        return convertView;
    }

    private static class ViewHolder
    {
        ImageView imageView;
        TextView busStopNameTextView;
        TextView busStopDirectionTextView;
        TextView busStopDistanceTextView;
    }
}