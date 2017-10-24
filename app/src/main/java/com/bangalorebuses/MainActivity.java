package com.bangalorebuses;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bangalorebuses.busarrivals.BusesArrivingAtBusStopActivity;
import com.bangalorebuses.busstops.BusStopsActivity;
import com.bangalorebuses.favorites.FavoritesHelper;
import com.bangalorebuses.favorites.FavoritesListCustomAdapter;
import com.bangalorebuses.tracker.BusesActivity;
import com.bangalorebuses.tracker.TrackBusActivity;
import com.bangalorebuses.trips.TripPlannerActivity;
import com.bangalorebuses.utils.BengaluruBusesDbHelper;
import com.bangalorebuses.utils.CommonMethods;
import com.bangalorebuses.utils.Constants;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import static com.bangalorebuses.utils.Constants.db;
import static com.bangalorebuses.utils.Constants.favoritesHashMap;

/**
 * This is the main activity of the app. It displays the
 * Nearby, BusTracker and TripPlanner fragments.
 *
 * @author Nihar Thakkar
 * @version 1.0
 * @since 5-9-2017
 */

public class MainActivity extends AppCompatActivity implements FavoritesHelper
{
    private CountDownTimer countDownTimer;
    private boolean wasDisplayingSplashScreen = false;
    private boolean activityWasPaused = false;

    private ListView favoritesListView;
    private LinearLayout noFavoritesLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Initialise some elements of the splash screen.
        setContentView(R.layout.splash_screen);

        TextView appTitleTextView = (TextView) findViewById(R.id.appTitleTextView);
        appTitleTextView.setText(R.string.app_name);

        // Change the font of "Bengaluru Buses" to a custom font.
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/Righteous-Regular.ttf");
        appTitleTextView.setTypeface(typeFace);

        showSplashScreen();
    }

    private void showSplashScreen()
    {
        wasDisplayingSplashScreen = true;

        countDownTimer = new CountDownTimer(2000, 2000)
        {
            @Override
            public void onTick(long millisUntilFinished)
            {

            }

            @Override
            public void onFinish()
            {
                wasDisplayingSplashScreen = false;

                initializeActivity();
            }
        }.start();
    }

    private void initializeActivity()
    {
        setContentView(R.layout.activity_main);

        if (db == null)
        {
            initialiseDatabase();
        }

        CommonMethods.readFavoritesFileToHashMap(this);

        LinearLayout busesLinearLayout = (LinearLayout) findViewById(R.id.buses_linear_layout);
        busesLinearLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBusesLinearLayoutClicked();
            }
        });

        LinearLayout busStopsLinearLayout = (LinearLayout) findViewById(R.id.bus_stops_linear_layout);
        busStopsLinearLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBusStopsLinearLayoutClicked();
            }
        });

        LinearLayout tripPlannerLinearLayout = (LinearLayout) findViewById(R.id.trip_planner_linear_layout);
        tripPlannerLinearLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onTripPlannerLinearLayoutClicked();
            }
        });

        favoritesListView = (ListView) findViewById(R.id.favourites_list_view);
        noFavoritesLinearLayout = (LinearLayout) findViewById(R.id
                .no_favorites_linear_layout);
        initialiseFavorites();
    }

    private void initialiseDatabase()
    {
        BengaluruBusesDbHelper bengaluruBusesDbHelper = new BengaluruBusesDbHelper(MainActivity.this);

        // Try to initialise the db.
        try
        {
            db = bengaluruBusesDbHelper.getReadableDatabase();
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Unable to load data! Please try again later...", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initialiseFavorites()
    {
        if (favoritesListView != null)
        {
            ArrayList<String> favorites = new ArrayList<>();

            //Set favoriteKeys = favoritesHashMap.keySet();

            Iterator iterator = favoritesHashMap.entrySet().iterator();
            while (iterator.hasNext())
            {
                Map.Entry pair = (Map.Entry) iterator.next();
                String key = (String) pair.getKey();
                String value = (String) pair.getValue();

                if (key.substring(0, 3).equals("^%b"))
                {
                    favorites.add(key + "^%bs" + value);
                }
                else if (key.substring(0, 3).equals("^%s"))
                {
                    favorites.add(key + "^%si" + value);
                }
                else if (key.substring(0, 3).equals("^%t"))
                {
                    favorites.add(key);
                }
            }

            Stack<String> favoritesBackwards = new Stack<>();
            ArrayList<String> favoritesForwards = new ArrayList<>();

            for (String favorite : favorites)
            {
                favoritesBackwards.push(favorite);
            }

            while (!favoritesBackwards.isEmpty())
            {
                favoritesForwards.add(favoritesBackwards.pop());
            }

            if (favoritesForwards.size() > 0)
            {
                FavoritesListCustomAdapter adapter = new FavoritesListCustomAdapter(this, this,
                        favoritesForwards, true);
                favoritesListView.setAdapter(adapter);
                favoritesListView.setVisibility(View.VISIBLE);
                noFavoritesLinearLayout.setVisibility(View.GONE);
            }
            else
            {
                favoritesListView.setVisibility(View.GONE);
                noFavoritesLinearLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onFavoriteClicked(String favorite)
    {
        if (favorite.substring(0, 3).equals("^%b"))
        {
            Intent busTrackerIntent = new Intent(this, TrackBusActivity.class);

            busTrackerIntent.putExtra("ROUTE_NUMBER", favorite.substring(3,
                    favorite.indexOf("^%bd")));

            busTrackerIntent.putExtra("ROUTE_DIRECTION", favorite.substring(
                    favorite.indexOf("^%bd") + 4, favorite.indexOf("^%bs")));

            busTrackerIntent.putExtra("ROUTE_STOP_ID", favorite.substring(
                    favorite.indexOf("^%bs") + 4, favorite.length()));

            startActivity(busTrackerIntent);
        }
        else if (favorite.substring(0, 3).equals("^%s"))
        {
            Intent busesArrivingAtBusStopIntent = new Intent(this, BusesArrivingAtBusStopActivity.class);

            busesArrivingAtBusStopIntent.putExtra("BUS_STOP_ID", Integer.parseInt(favorite
                    .substring(favorite.indexOf("^%si") + 4, favorite.length())));

            busesArrivingAtBusStopIntent.putExtra("BUS_STOP_NAME", favorite
                    .substring(3, favorite.indexOf("^%sd")));

            busesArrivingAtBusStopIntent.putExtra("BUS_STOP_DIRECTION_NAME", favorite
                    .substring(favorite.indexOf("^%sd") + 4, favorite.indexOf("^%si")));

            startActivity(busesArrivingAtBusStopIntent);
        }
        else
        {
            Intent tripPlannerIntent = new Intent(this, TripPlannerActivity.class);

            String originBusStopName = favorite.substring(favorite
                    .indexOf("^%t") + 3, favorite.indexOf("^%td"));
            String destinationBusStopName = favorite.substring(favorite
                    .indexOf("^%td") + 4, favorite.length());

            tripPlannerIntent.putExtra(Constants.TRIP_ORIGIN_BUS_STOP_NAME,
                    originBusStopName);
            tripPlannerIntent.putExtra(Constants.TRIP_DESTINATION_BUS_STOP_NAME,
                    destinationBusStopName);

            startActivity(tripPlannerIntent);
        }
    }

    @Override
    public void onFavoriteDeleted(String favorite)
    {
        if (favorite.substring(0, 3).equals("^%b"))
        {
            favoritesHashMap.remove(favorite.substring(0, favorite
                    .indexOf("^%bs")));
        }
        else if (favorite.substring(0, 3).equals("^%s"))
        {
            favoritesHashMap.remove(favorite.substring(0, favorite
                    .indexOf("^%si")));
        }
        else if (favorite.substring(0, 3).equals("^%t"))
        {
            favoritesHashMap.remove(favorite);
        }
        CommonMethods.writeFavoritesHashMapToFile(this);

        initialiseFavorites();
    }

    private void onBusStopsLinearLayoutClicked()
    {
        Intent busStopsActivityIntent = new Intent(this, BusStopsActivity.class);
        startActivity(busStopsActivityIntent);
    }

    private void onBusesLinearLayoutClicked()
    {
        Intent busesActivityIntent = new Intent(this, BusesActivity.class);
        startActivity(busesActivityIntent);
    }

    private void onTripPlannerLinearLayoutClicked()
    {
        Intent busesActivityIntent = new Intent(this, TripPlannerActivity.class);
        startActivity(busesActivityIntent);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // If the splash screen was being displayed when the app was paused
        // re-initialise the activity.
        if (wasDisplayingSplashScreen && activityWasPaused)
        {
            showSplashScreen();
        }
        activityWasPaused = false;

        initialiseFavorites();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        countDownTimer.cancel();
        activityWasPaused = true;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        // Close the db when this activity is destroyed
        if (db != null)
        {
            db.close();
            db = null;
        }
    }
}