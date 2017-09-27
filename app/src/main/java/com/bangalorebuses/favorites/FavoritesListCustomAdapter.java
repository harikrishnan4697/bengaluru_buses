package com.bangalorebuses.favorites;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bangalorebuses.R;

import java.util.ArrayList;

public class FavoritesListCustomAdapter extends BaseAdapter
{
    private FavoritesHelper caller;
    private ArrayList<String> favorites;
    private LayoutInflater inflater;
    private boolean shouldAllowFavoriteDeletion;

    public FavoritesListCustomAdapter(Activity context, FavoritesHelper caller, ArrayList<String> favorites,
                                      boolean shouldAllowFavoriteDeletion)
    {
        this.caller = caller;
        this.favorites = favorites;
        this.shouldAllowFavoriteDeletion = shouldAllowFavoriteDeletion;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount()
    {
        return favorites.size();
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public Object getItem(int position)
    {
        return favorites.get(position);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        FavoritesListCustomAdapter.ViewHolder holder;

        if (convertView == null)
        {
            holder = new FavoritesListCustomAdapter.ViewHolder();
            convertView = inflater.inflate(R.layout.favourites_list_item, null);
            holder.favoriteTypeImageView = (ImageView) convertView.findViewById(R.id.favorite_type_image_view);
            holder.favoriteNameTextView = (TextView) convertView.findViewById(R.id.favorite_name_text_view);
            holder.deleteFavoriteImageView = (ImageView) convertView.findViewById(R.id.delete_favorite_image_view);
            convertView.setTag(holder);
        }
        else
        {
            holder = (FavoritesListCustomAdapter.ViewHolder) convertView.getTag();
        }

        if (favorites.get(position).substring(0, 3).equals("^%b"))
        {
            holder.favoriteTypeImageView.setImageResource(R.drawable.ic_directions_bus_black);
            holder.favoriteNameTextView.setText(favorites.get(position).substring(3, favorites.get(position).length()));
        }
        else if (favorites.get(position).substring(0, 3).equals("^%s"))
        {
            holder.favoriteTypeImageView.setImageResource(R.drawable.ic_location_on_black);

            String favoriteBusStopName = favorites.get(position).substring(favorites.get(position)
                    .indexOf("^%sn") + 4, favorites.get(position).indexOf("^%sd"));

            String favoriteBusStopDirectionName = favorites.get(position).substring(favorites
                    .get(position).indexOf("^%sd") + 4, favorites.get(position).length());

            if (!favoriteBusStopDirectionName.equals(""))
            {
                holder.favoriteNameTextView.setText(favoriteBusStopName + " (" +
                        favoriteBusStopDirectionName + ")");
            }
            else
            {
                holder.favoriteNameTextView.setText(favoriteBusStopName);
            }
        }
        else
        {
            holder.favoriteTypeImageView.setImageResource(R.drawable.ic_directions_black);
        }

        holder.favoriteNameTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                caller.onFavoriteClicked(favorites.get(position));
            }
        });

        if (shouldAllowFavoriteDeletion)
        {
            holder.deleteFavoriteImageView.setImageResource(R.drawable.ic_delete_black);
            holder.deleteFavoriteImageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    caller.onFavoriteDeleted(favorites.get(position));
                }
            });
            holder.deleteFavoriteImageView.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.deleteFavoriteImageView.setVisibility(View.GONE);
        }

        return convertView;
    }

    private static class ViewHolder
    {
        ImageView favoriteTypeImageView;
        TextView favoriteNameTextView;
        ImageView deleteFavoriteImageView;
    }
}
