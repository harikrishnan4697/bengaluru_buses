package com.bangalorebuses.trips;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bangalorebuses.R;
import com.bangalorebuses.search.SearchActivity;
import com.bangalorebuses.utils.Animations;
import com.bangalorebuses.utils.CommonMethods;
import com.bangalorebuses.utils.Constants;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.bangalorebuses.utils.Constants.FAVORITES_TYPE;
import static com.bangalorebuses.utils.Constants.FAVORITES_TYPE_BUS_STOP;
import static com.bangalorebuses.utils.Constants.NETWORK_QUERY_NO_ERROR;
import static com.bangalorebuses.utils.Constants.NUMBER_OF_ROUTES_TYPE_ORIGIN_TO_TRANSIT_POINT;
import static com.bangalorebuses.utils.Constants.NUMBER_OF_ROUTES_TYPE_TRANSIT_POINT_TO_DESTINATION;
import static com.bangalorebuses.utils.Constants.SEARCH_END_BUS_STOP_REQUEST_CODE;
import static com.bangalorebuses.utils.Constants.SEARCH_START_BUS_STOP_REQUEST_CODE;
import static com.bangalorebuses.utils.Constants.SEARCH_TYPE_BUS_STOP;

public class TripPlannerActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener, DirectTripsHelper, TransitPointsHelper
{
    private FloatingActionButton favoritesFloatingActionButton;
    private boolean isFavorite = false;
    private TextView originSelectionTextView;
    private TextView destinationSelectionTextView;
    private ImageView swapDirectionImageView;
    private String originBusStopName;
    private String destinationBusStopName;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private LinearLayout errorLinearLayout;
    private ImageView errorImageView;
    private TextView errorMessageTextView;
    private TextView errorResolutionTextView;

    // Direct trip related variables
    private DirectTripsRecyclerViewAdapter directTripsAdapter;
    private ArrayList<DirectTrip> directTripsToDisplay = new ArrayList<>();
    private ArrayList<BusETAsOnDirectTripTask> busETAsOnDirectTripTasks = new ArrayList<>();
    private DirectTripsDbTask directTripsDbTask;
    private int numberOfDirectTripQueriesMade = 0;
    private int numberOfDirectTripRouteBusesFound = 0;

    // Indirect trip related variables
    private IndirectTripsRecyclerViewAdapter indirectTripsAdapter;
    private int numberOfMostFrequentBusRouteQueriesMade = 0;
    private int numberOfMostFrequentBusRouteQueriesComplete = 0;
    private ArrayList<IndirectTrip> indirectTripsToDisplay =
            new ArrayList<>();
    private ArrayList<MostFrequentIndirectTripDbTask> mostFrequentIndirectTripDbTasks = new ArrayList<>();
    private ArrayList<TransitPoint> transitPoints = new ArrayList<>();
    private TransitPointsWithNumberOfRoutesDbTask transitPointsWithNumberOfRoutesDbTask1;
    private TransitPointsWithNumberOfRoutesDbTask transitPointsWithNumberOfRoutesDbTask2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_planner);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.trip_planner_title);
        }

        favoritesFloatingActionButton = (FloatingActionButton) findViewById(R.id
                .favorites_floating_action_button);
        favoritesFloatingActionButton.setVisibility(View.GONE);
        favoritesFloatingActionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                favoriteTrip(originBusStopName, destinationBusStopName);
            }
        });

        originSelectionTextView = (TextView) findViewById(R.id.origin_text_view);
        originSelectionTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                selectOriginBusStop();
            }
        });

        destinationSelectionTextView = (TextView) findViewById(R.id.destination_text_view);
        destinationSelectionTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                selectDestinationBusStop();
            }
        });

        swapDirectionImageView = (ImageView) findViewById(R.id.swap_direction_image_view);
        swapDirectionImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                swapDirection();
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorNonACBus,
                R.color.colorACBus, R.color.colorMetroFeederBus);
        swipeRefreshLayout.setOnRefreshListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        errorLinearLayout = (LinearLayout) findViewById(R.id.errorLinearLayout);
        errorImageView = (ImageView) findViewById(R.id.errorImageView);
        errorMessageTextView = (TextView) findViewById(R.id.errorTextView);
        errorResolutionTextView = (TextView) findViewById(R.id.errorResolutionTextView);
        errorResolutionTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (originBusStopName != null && destinationBusStopName != null)
                {
                    findDirectTrips();
                }
                else
                {
                    Toast.makeText(TripPlannerActivity.this,
                            "Please select a starting and ending bus stop!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        errorLinearLayout.setVisibility(View.GONE);

        if (getIntent().getStringExtra(Constants.TRIP_ORIGIN_BUS_STOP_NAME) != null)
        {
            originBusStopName = getIntent().getStringExtra(Constants
                    .TRIP_ORIGIN_BUS_STOP_NAME);
            originSelectionTextView.setText(originBusStopName);
        }

        if (getIntent().getStringExtra(Constants.TRIP_DESTINATION_BUS_STOP_NAME) != null)
        {
            destinationBusStopName = getIntent().getStringExtra(Constants
                    .TRIP_DESTINATION_BUS_STOP_NAME);
            destinationSelectionTextView.setText(destinationBusStopName);
        }

        if (originBusStopName == null)
        {
            selectOriginBusStop();
        }
        else if (destinationBusStopName == null)
        {
            selectDestinationBusStop();
        }

        if (originBusStopName != null && destinationBusStopName != null)
        {
            findDirectTrips();
            checkIfTripIsFavorite(originBusStopName, destinationBusStopName);
            favoritesFloatingActionButton.setVisibility(View.VISIBLE);
        }
        else
        {
            favoritesFloatingActionButton.setVisibility(View.GONE);
        }
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

    private void selectOriginBusStop()
    {
        Intent searchOriginIntent = new Intent(this, SearchActivity.class);
        searchOriginIntent.putExtra("SEARCH_TYPE", SEARCH_TYPE_BUS_STOP);
        searchOriginIntent.putExtra("EDIT_TEXT_HINT", Constants.ORIGIN_BUS_STOP_SEARCH_HINT);
        searchOriginIntent.putExtra(FAVORITES_TYPE, FAVORITES_TYPE_BUS_STOP);
        startActivityForResult(searchOriginIntent, SEARCH_START_BUS_STOP_REQUEST_CODE);
    }

    private void selectDestinationBusStop()
    {
        Intent searchDestinationIntent = new Intent(this, SearchActivity.class);
        searchDestinationIntent.putExtra("SEARCH_TYPE", SEARCH_TYPE_BUS_STOP);
        searchDestinationIntent.putExtra("EDIT_TEXT_HINT", Constants.DESTINATION_BUS_STOP_SEARCH_HINT);
        searchDestinationIntent.putExtra(FAVORITES_TYPE, FAVORITES_TYPE_BUS_STOP);
        startActivityForResult(searchDestinationIntent, SEARCH_END_BUS_STOP_REQUEST_CODE);
    }

    private void swapDirection()
    {
        swapDirectionImageView.startAnimation(Animations
                .rotateForwardOnce(this));

        String tempDestinationBusStopName = destinationBusStopName;

        destinationBusStopName = originBusStopName;
        originBusStopName = tempDestinationBusStopName;

        originSelectionTextView.setText(originBusStopName);
        destinationSelectionTextView.setText(destinationBusStopName);

        if (originBusStopName != null && destinationBusStopName != null)
        {
            findDirectTrips();
            checkIfTripIsFavorite(originBusStopName, destinationBusStopName);
            favoritesFloatingActionButton.setVisibility(View.VISIBLE);
        }
        else
        {
            favoritesFloatingActionButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRefresh()
    {
        if (originBusStopName != null && destinationBusStopName != null)
        {
            findDirectTrips();
        }
        else
        {
            swipeRefreshLayout.setRefreshing(false);
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
                case SEARCH_START_BUS_STOP_REQUEST_CODE:
                    originBusStopName = data.getStringExtra("BUS_STOP_NAME");
                    originSelectionTextView.setText(originBusStopName);

                    if (destinationBusStopName == null)
                    {
                        selectDestinationBusStop();
                    }

                    break;
                case SEARCH_END_BUS_STOP_REQUEST_CODE:
                    destinationBusStopName = data.getStringExtra("BUS_STOP_NAME");
                    destinationSelectionTextView.setText(destinationBusStopName);

                    if (originBusStopName == null)
                    {
                        selectOriginBusStop();
                    }

                    break;
                default:
                    break;
            }

            if (originBusStopName != null && destinationBusStopName != null)
            {
                findDirectTrips();
                checkIfTripIsFavorite(originBusStopName, destinationBusStopName);
                favoritesFloatingActionButton.setVisibility(View.VISIBLE);
            }
            else
            {
                favoritesFloatingActionButton.setVisibility(View.GONE);
            }
        }
    }

    private void cancelAllPreviousTasks()
    {
        swipeRefreshLayout.setRefreshing(false);

        // Iterate over all the previous GetBusesEnDirectTripTasks to cancel each one of them
        for (BusETAsOnDirectTripTask task : busETAsOnDirectTripTasks)
        {
            if (task != null)
            {
                task.cancel(true);
            }
        }

        for (MostFrequentIndirectTripDbTask task : mostFrequentIndirectTripDbTasks)
        {
            if (task != null)
            {
                task.cancel(true);
            }
        }

        if (directTripsDbTask != null)
        {
            directTripsDbTask.cancel(true);
        }

        if (transitPointsWithNumberOfRoutesDbTask1 != null)
        {
            transitPointsWithNumberOfRoutesDbTask1.cancel(true);
        }

        if (transitPointsWithNumberOfRoutesDbTask2 != null)
        {
            transitPointsWithNumberOfRoutesDbTask2.cancel(true);
        }
    }

    private void findDirectTrips()
    {
        cancelAllPreviousTasks();

        errorLinearLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(true);
        recyclerView.setVisibility(View.GONE);
        transitPoints.clear();

        directTripsDbTask = new DirectTripsDbTask(this, originBusStopName,
                destinationBusStopName);
        directTripsDbTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void onDirectTripsFound(ArrayList<DirectTrip> directTrips)
    {
        if (directTrips.size() != 0)
        {
            numberOfDirectTripQueriesMade = 0;
            numberOfDirectTripRouteBusesFound = 0;
            directTripsToDisplay.clear();
            busETAsOnDirectTripTasks.clear();

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(linearLayoutManager);
            directTripsAdapter = new DirectTripsRecyclerViewAdapter(this,
                    directTripsToDisplay);
            recyclerView.setAdapter(directTripsAdapter);

            if (CommonMethods.checkNetworkConnectivity(this))
            {
                for (DirectTrip directTrip : directTrips)
                {
                    BusETAsOnDirectTripTask task = new BusETAsOnDirectTripTask(this, directTrip);
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    busETAsOnDirectTripTasks.add(task);
                    numberOfDirectTripQueriesMade++;
                }
            }
            else
            {
                recyclerView.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                showError(R.drawable.ic_cloud_off_black,
                        R.string.error_message_internet_unavailable, R.string.fix_error_retry);
            }
        }
        else
        {
            findTransitPoints(originBusStopName, destinationBusStopName);
        }
    }

    @Override
    public void onBusETAsOnDirectTripFound(String errorMessage, DirectTrip directTrip)
    {
        numberOfDirectTripRouteBusesFound++;

        if (errorMessage.equals(NETWORK_QUERY_NO_ERROR))
        {
            if (directTrip.getBusRoute().getBusRouteBuses().size() != 0)
            {
                directTrip.setTripDuration(directTrip.getBusRoute()
                        .getBusRouteBuses().get(0).getBusETA() + CommonMethods.calculateTravelTime(directTrip.getBusRoute().getBusRouteId(),
                        directTrip.getBusRoute().getBusRouteNumber(), directTrip.getOriginBusStop().getBusStopRouteOrder(),
                        directTrip.getDestinationBusStop().getBusStopRouteOrder()));

                directTripsToDisplay.add(directTrip);
                Collections.sort(directTripsToDisplay, new Comparator<DirectTrip>()
                {
                    @Override
                    public int compare(DirectTrip o1, DirectTrip o2)
                    {
                        return o1.getTripDuration() - o2.getTripDuration();
                    }
                });

                directTripsAdapter.notifyDataSetChanged();
                recyclerView.setVisibility(View.VISIBLE);
            }
        }

        if (numberOfDirectTripRouteBusesFound == numberOfDirectTripQueriesMade)
        {
            if (directTripsToDisplay.size() == 0)
            {
                findTransitPoints(originBusStopName, destinationBusStopName);
            }
            else
            {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    private void findTransitPoints(String originBusStopName, String destinationBusStopName)
    {
        if (CommonMethods.checkNetworkConnectivity(this))
        {
            transitPointsWithNumberOfRoutesDbTask1 = new TransitPointsWithNumberOfRoutesDbTask(this, originBusStopName,
                    destinationBusStopName, NUMBER_OF_ROUTES_TYPE_ORIGIN_TO_TRANSIT_POINT);
            transitPointsWithNumberOfRoutesDbTask1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            transitPointsWithNumberOfRoutesDbTask2 = new TransitPointsWithNumberOfRoutesDbTask(this, originBusStopName,
                    destinationBusStopName, NUMBER_OF_ROUTES_TYPE_TRANSIT_POINT_TO_DESTINATION);
            transitPointsWithNumberOfRoutesDbTask2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else
        {
            swipeRefreshLayout.setRefreshing(false);
            showError(R.drawable.ic_cloud_off_black,
                    R.string.error_message_internet_unavailable, R.string.fix_error_retry);
            errorLinearLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onTransitPointsAndRouteCountOriginToTPFound(ArrayList<TransitPoint> transitPoints)
    {
        // Make sure that there are transit points.
        if (transitPoints.size() == 0)
        {
            swipeRefreshLayout.setRefreshing(false);
            showError(R.drawable.ic_directions_bus_black_big,
                    R.string.error_message_no_trips, R.string.fix_error_retry);
            return;
        }

        if (this.transitPoints.size() == 0)
        {
            this.transitPoints = transitPoints;
        }
        else
        {
            for (TransitPoint transitPoint : this.transitPoints)
            {
                for (TransitPoint tp : transitPoints)
                {
                    if (transitPoint.getBusStopName().equals(tp.getBusStopName()))
                    {
                        transitPoint.setNumberOfRoutesBetweenOriginAndTransitPoint(
                                tp.getNumberOfRoutesBetweenOriginAndTransitPoint());
                    }
                }
            }
        }

        if (this.transitPoints.get(0).getNumberOfRoutesBetweenOriginAndTransitPoint() != 0 &&
                this.transitPoints.get(0).getNumberOfRoutesBetweenTransitPointAndDestination() != 0)
        {
            onTransitPointsFound();
        }
    }

    @Override
    public void onTransitPointsAndRouteCountTPToDestFound(ArrayList<TransitPoint> transitPoints)
    {
        // Make sure that there are transit points.
        if (transitPoints.size() == 0)
        {
            swipeRefreshLayout.setRefreshing(false);
            showError(R.drawable.ic_directions_bus_black_big,
                    R.string.error_message_no_trips, R.string.fix_error_retry);
            return;
        }

        if (this.transitPoints.size() == 0)
        {
            this.transitPoints = transitPoints;
        }
        else
        {
            for (TransitPoint transitPoint : this.transitPoints)
            {
                for (TransitPoint tp : transitPoints)
                {
                    if (transitPoint.getBusStopName().equals(tp.getBusStopName()))
                    {
                        transitPoint.setNumberOfRoutesBetweenTransitPointAndDestination(
                                tp.getNumberOfRoutesBetweenTransitPointAndDestination());
                    }
                }
            }
        }

        if (this.transitPoints.get(0).getNumberOfRoutesBetweenOriginAndTransitPoint() != 0 &&
                this.transitPoints.get(0).getNumberOfRoutesBetweenTransitPointAndDestination() != 0)
        {
            onTransitPointsFound();
        }
    }

    private void onTransitPointsFound()
    {
        if (transitPoints.size() == 0)
        {
            recyclerView.setVisibility(View.GONE);

            showError(R.drawable.ic_directions_bus_black_big,
                    R.string.error_message_no_trips, R.string.fix_error_retry);
            errorLinearLayout.setVisibility(View.VISIBLE);

            return;
        }

        // For each transit point, set the score to the smaller number of routes
        // of the two legs.
        for (TransitPoint transitPoint : this.transitPoints)
        {
            if (transitPoint.getNumberOfRoutesBetweenOriginAndTransitPoint() <
                    transitPoint.getNumberOfRoutesBetweenTransitPointAndDestination())
            {
                transitPoint.setTransitPointScore(transitPoint.getNumberOfRoutesBetweenOriginAndTransitPoint());
            }
            else
            {
                transitPoint.setTransitPointScore(transitPoint.getNumberOfRoutesBetweenTransitPointAndDestination());
            }
        }

        // Sort the transit points in descending order based on their scores
        Collections.sort(this.transitPoints, new Comparator<TransitPoint>()
        {
            @Override
            public int compare(TransitPoint tp1, TransitPoint tp2)
            {
                return tp2.getTransitPointScore() - tp1.getTransitPointScore();
            }
        });

        // Keep only the top 5 transit points based on their scores
        ArrayList<TransitPoint> tempTransitPoints = new ArrayList<>();

        for (int i = 0; i < 5; i++)
        {
            if (i < this.transitPoints.size())
            {
                tempTransitPoints.add(this.transitPoints.get(i));
            }
        }

        this.transitPoints = tempTransitPoints;

        indirectTripsToDisplay.clear();
        numberOfMostFrequentBusRouteQueriesMade = 0;
        numberOfMostFrequentBusRouteQueriesComplete = 0;
        mostFrequentIndirectTripDbTasks.clear();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        indirectTripsAdapter = new
                IndirectTripsRecyclerViewAdapter(this, originBusStopName,
                destinationBusStopName, indirectTripsToDisplay);
        recyclerView.setAdapter(indirectTripsAdapter);

        for (TransitPoint transitPoint : this.transitPoints)
        {
            MostFrequentIndirectTripDbTask task = new MostFrequentIndirectTripDbTask(this,
                    originBusStopName, transitPoint.getBusStopName(), destinationBusStopName);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            mostFrequentIndirectTripDbTasks.add(task);
            numberOfMostFrequentBusRouteQueriesMade++;
        }
    }

    @Override
    public void onIndirectTripFound(IndirectTrip indirectTrip)
    {
        numberOfMostFrequentBusRouteQueriesComplete++;

        indirectTripsToDisplay.add(indirectTrip);
        indirectTripsToDisplay.trimToSize();

        Collections.sort(indirectTripsToDisplay, new Comparator<IndirectTrip>()
        {
            @Override
            public int compare(IndirectTrip i1, IndirectTrip i2)
            {
                return i1.getTripDuration() - i2.getTripDuration();
            }
        });

        indirectTripsAdapter.notifyDataSetChanged();
        recyclerView.setVisibility(View.VISIBLE);

        if (numberOfMostFrequentBusRouteQueriesComplete ==
                numberOfMostFrequentBusRouteQueriesMade)
        {
            swipeRefreshLayout.setRefreshing(false);

            if (indirectTripsToDisplay.size() == 0)
            {
                recyclerView.setVisibility(View.GONE);

                showError(R.drawable.ic_directions_bus_black_big,
                        R.string.error_message_no_trips, R.string.fix_error_retry);
                errorLinearLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void checkIfTripIsFavorite(String originBusStopName, String destinationBusStopName)
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

            isFavorite = favorites.contains("^%t" + originBusStopName + "^%td" +
                    destinationBusStopName);

            favorites.trimToSize();
            fileInputStream.close();
            inputStreamReader.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (isFavorite)
        {
            favoritesFloatingActionButton.setImageResource(R.drawable.ic_favorite_white);
        }
        else
        {
            favoritesFloatingActionButton.setImageResource(R.drawable.ic_favorite_border_white);
        }
    }

    private void favoriteTrip(String originBusStopName, String destinationBusStopName)
    {
        if (!isFavorite)
        {
            try
            {
                FileOutputStream fileOutputStream = openFileOutput(Constants.FAVORITES_FILE_NAME,
                        MODE_APPEND);

                fileOutputStream.write(("^%t" + originBusStopName + "^%td" + destinationBusStopName +
                        "\n").getBytes());

                Toast.makeText(this, "Added trip to favourites.", Toast.LENGTH_SHORT)
                        .show();

                fileOutputStream.close();
            }
            catch (IOException e)
            {
                Toast.makeText(this, "Unknown error occurred! Couldn't favourite this trip...",
                        Toast.LENGTH_SHORT).show();
            }

            favoritesFloatingActionButton.setImageResource(R.drawable.ic_favorite_white);
            isFavorite = true;
        }
        else
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

                favorites.remove("^%t" + originBusStopName + "^%td" + destinationBusStopName);

                Toast.makeText(this, "Removed trip from favourites.", Toast.LENGTH_SHORT)
                        .show();

                favorites.trimToSize();
                fileInputStream.close();
                inputStreamReader.close();

                FileOutputStream fileOutputStream = openFileOutput(Constants.FAVORITES_FILE_NAME,
                        MODE_PRIVATE);
                for (String favorite : favorites)
                {
                    fileOutputStream.write((favorite + "\n").getBytes());
                }
                fileOutputStream.close();

            }
            catch (Exception e)
            {
                e.printStackTrace();
                Toast.makeText(this, "Unknown error occurred! Couldn't un-favourite this trip...",
                        Toast.LENGTH_SHORT).show();
            }

            favoritesFloatingActionButton.setImageResource(R.drawable.ic_favorite_border_white);
            isFavorite = false;
        }
    }

    private void showError(int drawableResId, int errorMessageStringResId, int resolutionButtonStringResId)
    {
        swipeRefreshLayout.setRefreshing(false);
        errorImageView.setImageResource(drawableResId);
        errorMessageTextView.setText(errorMessageStringResId);
        errorResolutionTextView.setText(resolutionButtonStringResId);
        errorLinearLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        cancelAllPreviousTasks();
    }
}
