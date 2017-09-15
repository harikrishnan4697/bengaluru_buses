package com.bangalorebuses.trips;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.bangalorebuses.R;

public class TripPlannerActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_planner);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().hide();
        }

        TextView titleTextView = (TextView) findViewById(R.id.title_text_view);

        // Change the font of "Buses" to a custom font.
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/Righteous-Regular.ttf");
        titleTextView.setTypeface(typeFace);
    }
}
