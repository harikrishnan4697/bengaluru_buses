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

public class MainActivity extends AppCompatActivity
{
    private ActionBar actionBar;
    private Fragment selectedFragment = null;
    private CountDownTimer countDownTimer;
    private boolean wasDisplayingSplashScreen = false;
    private boolean activityWasPaused = false;
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

    private void initializeActivity()
    {
        actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setElevation(0);
            actionBar.hide();
        }
        setContentView(R.layout.splash_screen);
        TextView appTitleTextView = (TextView) findViewById(R.id.appTitleTextView);
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
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                // Configure the actionbar
                if (actionBar != null)
                {
                    actionBar.show();
                }
                bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
                BottomNavigationBarHelper.disableShiftMode(bottomNavigationView);

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
                            /*case R.id.navigation_favourites:
                                selectedFragment = new FavouritesFragment();
                                actionBar.setTitle("Favourites");
                                break;*/
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

    private void getDatabase()
    {
        BengaluruBusesDbHelper bengaluruBusesDbHelper = new BengaluruBusesDbHelper(MainActivity.this);
        try
        {
            Constants.db = bengaluruBusesDbHelper.getReadableDatabase();
        }
        catch (SQLiteException e)
        {
            e.printStackTrace();
            Toast.makeText(this, "Something went wrong! Error code: 1", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        selectedFragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (wasDisplayingSplashScreen && activityWasPaused)
        {
            initializeActivity();
        }
        activityWasPaused = false;
        getDatabase();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        countDownTimer.cancel();
        activityWasPaused = true;
        //Constants.db.close();
    }
}