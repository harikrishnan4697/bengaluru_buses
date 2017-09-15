package com.bangalorebuses.busstops;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bangalorebuses.R;

public class BusStopsActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_stops);

        TextView titleTextView = (TextView) findViewById(R.id.title_text_view);

        // Change the font of "Buses" to a custom font.
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/Righteous-Regular.ttf");
        titleTextView.setTypeface(typeFace);

        // Hide the action bar
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().hide();
        }

        // Create the two tabs
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Nearby Bus Stops"));
        tabLayout.addTab(tabLayout.newTab().setText("All Bus Stops"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Use a ViewPager to allow the user to swipe between the tabs
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final BusStopsPagerAdapter adapter = new BusStopsPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        // Handle tabs getting selected
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {

            }
        });

        // Initialise some variables
        ImageView backButtonImageView = (ImageView) findViewById(R.id.back_button_image_view);
        backButtonImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }
}
