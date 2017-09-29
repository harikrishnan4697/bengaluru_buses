package com.bangalorebuses.trips;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bangalorebuses.R;
import com.bangalorebuses.core.BusRoute;
import com.bangalorebuses.utils.ErrorImageResIds;

import org.w3c.dom.Text;

import java.util.ArrayList;

import static com.bangalorebuses.utils.Constants.DESTINATION_BUS_STOP_NAME;
import static com.bangalorebuses.utils.Constants.ORIGIN_BUS_STOP_NAME;
import static com.bangalorebuses.utils.Constants.TRANSIT_POINT_BUS_STOP_NAME;

public class IndirectTripDetailsActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener, IndirectTripHelper
{
    // Bus stop names
    private String originBusStopName;
    private String transitPointBusStopName;
    private String destinationBusStopName;

    // Tasks
    private BusRoutesToAndFromTransitPointDbTask
            busRoutesToAndFromTransitPointDbTask;

    // Variable for displaying errors
    private LinearLayout errorLinearLayout;
    private ImageView errorImageView;
    private TextView errorTextView;
    private TextView errorResolutionTextView;

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indirect_trip_details);

        // Initialise the action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialise the back button
        ImageView backButtonImageView = (ImageView) findViewById(R.id.back_button_image_view);
        backButtonImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        errorLinearLayout = (LinearLayout) findViewById(R.id.errorLinearLayout);
        errorImageView = (ImageView) findViewById(R.id.errorImageView);
        errorTextView = (TextView) findViewById(R.id.errorTextView);
        errorResolutionTextView = (TextView) findViewById(R.id.errorResolutionTextView);

        errorResolutionTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                findTrips();
            }
        });

        // Initialise the trip details variables
        originBusStopName = getIntent().getStringExtra(ORIGIN_BUS_STOP_NAME);
        transitPointBusStopName = getIntent().getStringExtra(TRANSIT_POINT_BUS_STOP_NAME);
        destinationBusStopName = getIntent().getStringExtra(DESTINATION_BUS_STOP_NAME);

        findTrips();
    }

    private void findTrips()
    {
        swipeRefreshLayout.setRefreshing(true);

        if (originBusStopName != null && transitPointBusStopName != null
                && destinationBusStopName != null)
        {
            TransitPoint transitPoint = new TransitPoint();
            transitPoint.setTransitPointName(transitPointBusStopName);

            // Execute the task that will query the db and return a list of bus routes
            // to the transit point and from the transit point.
            busRoutesToAndFromTransitPointDbTask = new BusRoutesToAndFromTransitPointDbTask(this,
                    originBusStopName, transitPoint, destinationBusStopName);
            busRoutesToAndFromTransitPointDbTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else
        {
            setErrorLayoutContent(ErrorImageResIds.ERROR_IMAGE_SOMETHING_WENT_WRONG,
                    "Sorry! Something went wrong. Please try again...", "Retry");
            errorLinearLayout.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onTransitPointsAndRouteCountOriginToTPFound(ArrayList<TransitPoint> transitPoints)
    {

    }

    @Override
    public void onTransitPointsAndRouteCountTPToDestFound(ArrayList<TransitPoint> transitPoints)
    {

    }

    @Override
    public void onBusRoutesToAndFromTransitPointFound(TransitPoint transitPoint)
    {

    }

    @Override
    public void onBusETAsOnLeg1BusRouteFound(String errorMessage, BusRoute busRoute, TransitPoint transitPoint)
    {

    }

    private void cancelAllTasks()
    {
        busRoutesToAndFromTransitPointDbTask.cancel(true);
    }

    @Override
    public void onRefresh()
    {
        findTrips();
    }

    private void setErrorLayoutContent(int drawableResId, String errorMessage,
                                       String resolutionButtonText)
    {
        errorImageView.setImageResource(drawableResId);
        errorTextView.setText(errorMessage);
        errorResolutionTextView.setText(resolutionButtonText);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        cancelAllTasks();
    }
}