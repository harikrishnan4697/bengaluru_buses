package com.bangalorebuses;

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

public class MainActivity extends AppCompatActivity
{
    private ActionBar actionBar;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener()
    {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {
            Fragment selectedFragment = null;

            switch (item.getItemId())
            {
                /*case R.id.navigation_favourites:
                    selectedFragment = favouritesFragment;
                    actionBar.setTitle("Favourites");
                    break;*/
                case R.id.navigation_near_me:
                    selectedFragment = new NearMeFragment();
                    actionBar.setTitle("Bus stops nearby");
                    break;
                case R.id.navigation_track_bus:
                    selectedFragment = new BusTrackerFragment();
                    actionBar.setTitle("Bus tracker");
                    break;
                case R.id.navigation_trip_planner:
                    selectedFragment = new TripPlannerFragment();
                    actionBar.setTitle("Trip planner");
                    break;
            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout, selectedFragment);
            transaction.commit();
            return true;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setElevation(0);
            actionBar.hide();
        }
        setContentView(R.layout.splash_screen);
        new CountDownTimer(2000, 2000)
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
                BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
                navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
                //Manually displaying the first fragment - one time only
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_layout, new NearMeFragment());
                transaction.commit();

                //Used to select an item programmatically
                navigation.getMenu().getItem(0).setChecked(true);
                actionBar.setTitle("Nearby");
            }
        }.start();
    }
}
