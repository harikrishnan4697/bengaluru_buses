package com.bangalorebuses.favorites;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.bangalorebuses.R;
import com.bangalorebuses.utils.Constants;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Stack;

import static com.bangalorebuses.utils.Constants.BUS_STOP_NAME;
import static com.bangalorebuses.utils.Constants.FAVORITES_TYPE;
import static com.bangalorebuses.utils.Constants.FAVORITES_TYPE_BUS_ROUTE;
import static com.bangalorebuses.utils.Constants.FAVORITES_TYPE_BUS_STOP;

public class FavoritesActivity extends AppCompatActivity implements FavoritesHelper
{
    private ListView favoritesListView;
    private String favoritesType;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setTitle(R.string.favourites_title);
        }

        favoritesType = getIntent()
                .getStringExtra(FAVORITES_TYPE);

        favoritesListView = (ListView) findViewById(R.id.favorites_list_view);
        favoritesListView.setVisibility(View.GONE);

        initialiseFavorites();
    }

    private void initialiseFavorites()
    {
        if (favoritesListView != null)
        {
            try
            {
                FileInputStream fileInputStream = openFileInput(Constants.FAVORITES_FILE_NAME);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                ArrayList<String> favorites = new ArrayList<>();
                String line;

                while ((line = bufferedReader.readLine()) != null)
                {
                    favorites.add(line);
                }

                Stack<String> favoritesBackwards = new Stack<>();
                ArrayList<String> favoritesForwards = new ArrayList<>();

                for (String favorite : favorites)
                {
                    favoritesBackwards.push(favorite);
                }

                if (favoritesType.equals(FAVORITES_TYPE_BUS_ROUTE))
                {
                    while (!favoritesBackwards.isEmpty())
                    {
                        String favorite = favoritesBackwards.pop();

                        if (favorite.substring(0, 3).equals("^%b"))
                        {
                            favoritesForwards.add(favorite);
                        }
                    }
                }
                else if (favoritesType.equals(FAVORITES_TYPE_BUS_STOP))
                {
                    while (!favoritesBackwards.isEmpty())
                    {
                        String favorite = favoritesBackwards.pop();

                        if (favorite.substring(0, 3).equals("^%s"))
                        {
                            favoritesForwards.add(favorite.substring(0, favorite.indexOf("^%sd") + 4));
                        }
                    }
                }
                else
                {
                    while (!favoritesBackwards.isEmpty())
                    {
                        favoritesForwards.add(favoritesBackwards.pop());
                    }
                }

                if (favoritesForwards.size() > 0)
                {
                    FavoritesListCustomAdapter adapter = new FavoritesListCustomAdapter(this, this,
                            favoritesForwards, false);
                    favoritesListView.setAdapter(adapter);
                    favoritesListView.setVisibility(View.VISIBLE);
                }
                else
                {
                    favoritesListView.setVisibility(View.GONE);
                }

                fileInputStream.close();
                inputStreamReader.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFavoriteClicked(String favorite)
    {
        Intent resultIntent = new Intent();

        String favoriteBusStopName = favorite.substring(favorite
                .indexOf("^%sn") + 4, favorite.indexOf("^%sd"));

        resultIntent.putExtra(BUS_STOP_NAME, favoriteBusStopName);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onFavoriteDeleted(String favorite)
    {
        try
        {
            FileInputStream fileInputStream = openFileInput(Constants.FAVORITES_FILE_NAME);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream,
                    "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            ArrayList<String> favorites = new ArrayList<>();
            String line;

            while ((line = bufferedReader.readLine()) != null)
            {
                favorites.add(line);
            }

            favorites.remove(favorite);

            favorites.trimToSize();
            fileInputStream.close();
            inputStreamReader.close();

            FileOutputStream fileOutputStream = openFileOutput(Constants.FAVORITES_FILE_NAME,
                    MODE_PRIVATE);
            for (String aFavorite : favorites)
            {
                fileOutputStream.write((aFavorite + "\n").getBytes());
            }
            fileOutputStream.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(this, "Unknown error occurred! Couldn't un-favourite this bus...", Toast
                    .LENGTH_SHORT).show();
        }

        initialiseFavorites();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                break;
            default:
                return false;
        }
        return true;
    }
}