package com.bangalorebuses;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;

import static com.bangalorebuses.Constants.SEARCH_TYPE_BUS_ROUTE;
import static com.bangalorebuses.Constants.SEARCH_TYPE_BUS_STOP;
import static com.bangalorebuses.Constants.SEARCH_TYPE_BUS_STOP_WITH_DIRECTION;
import static com.bangalorebuses.Constants.db;

public class SearchActivity extends AppCompatActivity
{
    ProgressBar progressBar;
    private EditText searchEditText;
    private ListView searchResultsListView;
    private BusNumberListCustomAdapter customListAdapter;
    private String searchType;
    private Intent resultIntent = new Intent();
    private GetAllStops getAllStops;
    private GetAllDistinctRouteNumbers getAllDistinctRouteNumbers;
    private GetAllDistinctStopNames getAllDistinctStopNames;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        // Hides the default actionbar
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().hide();
        }
        // Hide the soft keyboard by default when the activity is started
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Initialise some variables
        searchEditText = (EditText) findViewById(R.id.search_edit_text);
        searchResultsListView = (ListView) findViewById(R.id.search_results_list_view);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        searchType = getIntent().getStringExtra("SEARCH_TYPE");

        progressBar.setVisibility(View.VISIBLE);
        if (searchType.equals(SEARCH_TYPE_BUS_STOP))
        {
            getAllDistinctStopNames = new GetAllDistinctStopNames();
            getAllDistinctStopNames.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else if (searchType.equals(SEARCH_TYPE_BUS_STOP_WITH_DIRECTION))
        {
            getAllStops = new GetAllStops();
            getAllStops.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else if (searchType.equals(SEARCH_TYPE_BUS_ROUTE))
        {
            getAllDistinctRouteNumbers = new GetAllDistinctRouteNumbers();
            getAllDistinctRouteNumbers.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void onAllDistinctStopNamesFound(ArrayList<String> busStopNames)
    {

    }

    private void onAllStopsFound(final ArrayList<BusStop> busStops)
    {
        final AllBusStopSearchListAdaptor listAdapter = new AllBusStopSearchListAdaptor(this, busStops);
        searchResultsListView.setAdapter(listAdapter);
        progressBar.setVisibility(View.GONE);
        searchEditText.setEnabled(true);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
        searchResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                resultIntent.putExtra("BUS_STOP_NAME", ((BusStop) parent.getItemAtPosition(position)).getBusStopName());
                resultIntent.putExtra("BUS_STOP_DIRECTION_NAME", ((BusStop) parent.getItemAtPosition(position)).getBusStopDirectionName());
                resultIntent.putExtra("BUS_STOP_ID", ((BusStop) parent.getItemAtPosition(position)).getBusStopId());
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher()
        {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                listAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });
    }

    private void onAllDistinctRouteNumbersFound(ArrayList<String> routeNumbers)
    {
        final BusNumberListCustomAdapterRouteNumber listAdapter = new BusNumberListCustomAdapterRouteNumber(this, routeNumbers);
        searchResultsListView.setAdapter(listAdapter);
        progressBar.setVisibility(View.GONE);
        searchEditText.setEnabled(true);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
        searchResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                resultIntent.putExtra("ROUTE_NUMBER", ((String) parent.getItemAtPosition(position)));
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher()
        {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                listAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });
    }

    /**
     * This method is called by the on screen back button.
     *
     * @param view Not used.
     */
    public void exit(View view)
    {
        finish();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (getAllDistinctRouteNumbers != null)
        {
            getAllDistinctRouteNumbers.cancel(true);
        }
        if (getAllStops != null)
        {
            getAllStops.cancel(true);
        }
        if (getAllDistinctStopNames != null)
        {
            getAllDistinctStopNames.cancel(true);
        }
    }

    /**
     * This method is used to Handle the route number list and
     * filter bus routes as the user starts typing in the search box.
     */
    /*private void initialiseSearchResultsList()
    {


            // Converts the asset to a JSON array
            jsonArray = new JSONArray(stringBuilder.toString());
            String[] listViewAdapterContent = new String[jsonArray.length()];
            String[] routeTypes = new String[jsonArray.length()];

            // Puts the bus route or stop names into a list
            if (searchType.equals(Constants.SEARCH_TYPE_BUS_ROUTE))
            {
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    listViewAdapterContent[i] = jsonArray.getJSONObject(i).getString("routename");
                    routeTypes[i] = jsonArray.getJSONObject(i).getString("service_type_name");
                }
            }
            else
            {
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    listViewAdapterContent[i] = jsonArray.getJSONObject(i).getString("StopName");
                }
            }

            if (searchType.equals(Constants.SEARCH_TYPE_BUS_ROUTE))
            {
                customListAdapter = new BusNumberListCustomAdapter(this, listViewAdapterContent, routeTypes);
                searchResultsListView.setAdapter(customListAdapter);
            }
            else
            {
                listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, listViewAdapterContent);
                searchResultsListView.setAdapter(listAdapter);
            }
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Couldn't load search results!", Toast.LENGTH_SHORT).show();
            return;
        }

        searchResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (searchType.equals(Constants.SEARCH_TYPE_BUS_STOP))
                {
                    resultIntent.putExtra("BUS_STOP_NAME", parent.getItemAtPosition(position).toString());
                    try
                    {
                        for (int i = 0; i < jsonArray.length(); i++)
                        {
                            if (jsonArray.getJSONObject(i).getString("StopName").equals(parent.getItemAtPosition(position).toString()))
                            {
                                resultIntent.putExtra("BUS_STOP_ID", jsonArray.getJSONObject(i).getInt("StopId"));
                            }
                        }
                    }
                    catch (JSONException e)
                    {
                        //TODO Handle exception
                    }
                }
                else
                {
                    resultIntent.putExtra("Selected_Item", parent.getItemAtPosition(position).toString());
                }
                setResult(RESULT_OK, resultIntent);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                finish();
            }
        });
        searchEditText.addTextChangedListener(new TextWatcher()
        {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (searchType.equals(Constants.SEARCH_TYPE_BUS_ROUTE))
                {
                    customListAdapter.getFilter().filter(s);
                }
                else
                {
                    listAdapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {
            }
        });
    }*/

    private class GetAllDistinctRouteNumbers extends AsyncTask<Void, Void, ArrayList<String>>
    {
        @Override
        protected ArrayList<String> doInBackground(Void... params)
        {
            return DbQueries.getAllDistinctRouteNumbers(db);
        }

        @Override
        protected void onPostExecute(ArrayList<String> routeNumbers)
        {
            super.onPostExecute(routeNumbers);
            onAllDistinctRouteNumbersFound(routeNumbers);
        }
    }

    private class GetAllStops extends AsyncTask<Void, Void, ArrayList<BusStop>>
    {
        @Override
        protected ArrayList<BusStop> doInBackground(Void... params)
        {
            return DbQueries.getAllStops(db);
        }

        @Override
        protected void onPostExecute(ArrayList<BusStop> busStops)
        {
            super.onPostExecute(busStops);
            onAllStopsFound(busStops);
        }
    }

    private class GetAllDistinctStopNames extends AsyncTask<Void, Void, ArrayList<String>>
    {
        @Override
        protected ArrayList<String> doInBackground(Void... params)
        {
            return DbQueries.getAllDistinctStopNames(db);
        }

        @Override
        protected void onPostExecute(ArrayList<String> busStopNames)
        {
            super.onPostExecute(busStopNames);
            onAllDistinctStopNamesFound(busStopNames);
        }

    }
}