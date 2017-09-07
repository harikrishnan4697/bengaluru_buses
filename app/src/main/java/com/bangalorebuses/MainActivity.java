package com.bangalorebuses;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.bangalorebuses.nearby.NearbyFragment;
import com.bangalorebuses.tracker.BusTrackerFragment;
import com.bangalorebuses.trips.TripPlannerFragment;
import com.bangalorebuses.utils.BengaluruBusesDbHelper;
import com.bangalorebuses.utils.BottomNavigationBarHelper;
import com.bangalorebuses.utils.Constants;

/**
 * This is the main activity of the app. It displays the
 * Nearby, BusTracker and TripPlanner fragments.
 *
 * @author Nihar Thakkar
 * @version 1.0
 * @since 5-9-2017
 */

public class MainActivity extends AppCompatActivity
{
    private ActionBar actionBar;
    private CountDownTimer countDownTimer;
    private boolean wasDisplayingSplashScreen = false;
    private boolean activityWasPaused = false;
    private Fragment selectedFragment = null;
    private NearbyFragment nearbyFragment = new NearbyFragment();
    private BusTrackerFragment busTrackerFragment = new BusTrackerFragment();
    private TripPlannerFragment tripPlannerFragment = new TripPlannerFragment();
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        initializeActivity();
    }

    /**
     * This method shows the app splash screen and then initialises some
     * variables and fragments.
     */
    private void initializeActivity()
    {
        // Hide the action bar for the duration of the splash screen.
        actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setElevation(0);
            actionBar.hide();
        }

        // Initialise some elements of the splash screen.
        setContentView(R.layout.splash_screen);
        TextView appTitleTextView = (TextView) findViewById(R.id.appTitleTextView);

        // Change the font of "Bengaluru Buses" to a custom font.
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/Righteous-Regular.ttf");
        appTitleTextView.setTypeface(typeFace);

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
                setContentView(R.layout.activity_main);

                // Don't let the on-screen keyboard pop up for anything by default.
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                // Configure the actionbar
                if (actionBar != null)
                {
                    actionBar.show();
                }

                bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);

                // Disable the default Android setting that makes icons on the bottom nav bar
                // slide around annoyingly.
                BottomNavigationBarHelper.disableShiftMode(bottomNavigationView);

                initialiseDatabase();

                // Manually displaying the first fragment - one time only
                bottomNavigationView.setSelectedItemId(R.id.navigation_track_bus);
                actionBar.setTitle("Bus tracker");
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_layout, busTrackerFragment);
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                transaction.commitNow();

                wasDisplayingSplashScreen = false;

                bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener()
                {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item)
                    {
                        switch (item.getItemId())
                        {
                            case R.id.navigation_near_me:
                                selectedFragment = nearbyFragment;
                                actionBar.setTitle("Bus stops nearby");
                                break;
                            case R.id.navigation_track_bus:
                                selectedFragment = busTrackerFragment;
                                actionBar.setTitle("Bus tracker");
                                break;
                            case R.id.navigation_trip_planner:
                                selectedFragment = tripPlannerFragment;
                                actionBar.setTitle("Trip planner");
                                break;
                        }

                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.frame_layout, selectedFragment);
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        transaction.commitNow();
                        return true;
                    }
                });

                bottomNavigationView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener()
                {
                    @Override
                    public void onNavigationItemReselected(@NonNull MenuItem item)
                    {

                    }
                });
            }
        }.start();
    }

    /**
     * This method initialises the SQLiteDatabase instance stored in the Constants class.
     */
    private void initialiseDatabase()
    {
        BengaluruBusesDbHelper bengaluruBusesDbHelper = new BengaluruBusesDbHelper(MainActivity.this);

        // Try to initialise the db.
        try
        {
            Constants.db = bengaluruBusesDbHelper.getReadableDatabase();
        }
        catch (SQLiteException e)
        {
            e.printStackTrace();
            Toast.makeText(this, "Something went wrong! Please re-install the app and try again." +
                    " Error code: 1", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // Pass on the result of an activity launched by a fragment back
        // to the fragment.
        if (requestCode == 1 && selectedFragment != null)
        {
            selectedFragment.onActivityResult(requestCode, resultCode, data);
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // If the splash screen was being displayed when the app was paused
        // re-initialise the activity.
        if (wasDisplayingSplashScreen && activityWasPaused)
        {
            initializeActivity();
        }
        activityWasPaused = false;
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

        // Close the db when the app is destroyed
        Constants.db.close();
    }
}