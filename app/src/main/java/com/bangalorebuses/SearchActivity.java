package com.bangalorebuses;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;

import java.util.HashSet;
import java.util.Set;

public class SearchActivity extends AppCompatActivity
{
    private EditText searchEditText;
    private ListView searchResultsListView;
    private ProgressBar progressBar;
    private ArrayAdapter<String> listAdapter;
    private BusNumberListCustomAdapter customListAdapter;
    private String searchType;
    private Intent resultIntent = new Intent();
    private JSONArray jsonArray;
/*    private GetAllStops getAllStops;
    private GetAllRoutes getAllRoutes;*/
    private Set<String> busStopNames = new HashSet<>();

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
        // Show the soft keyboard by default when the activity is started
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // Initialise some variables
        searchEditText = (EditText) findViewById(R.id.search_edit_text);
        searchResultsListView = (ListView) findViewById(R.id.search_results_list_view);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        searchType = getIntent().getStringExtra("SEARCH_TYPE");

        /*if (searchType.equals(SEARCH_TYPE_BUS_STOP))
        {
            getAllStops = new GetAllStops();
            getAllStops.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else if (searchType.equals(SEARCH_TYPE_BUS_ROUTE))
        {
            getAllRoutes = new GetAllRoutes();
            getAllRoutes.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }*/
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
    }

    private class GetAllRoutes extends AsyncTask<Void, Void, ArrayList<BusRoute>>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<BusRoute> doInBackground(Void... params)
        {
            return DbQueries.getAllRoutes(db);
        }

        @Override
        protected void onPostExecute(ArrayList<BusRoute> busRoutes)
        {
            super.onPostExecute(busRoutes);
            for (BusRoute busRoute: busRoutes)
            {

            }
            progressBar.setVisibility(View.GONE);
        }
    }

    private class GetAllStops extends AsyncTask<Void, Void, ArrayList<BusStop>>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<BusStop> doInBackground(Void... params)
        {
            return DbQueries.getAllStops(db);
        }

        @Override
        protected void onPostExecute(ArrayList<BusStop> busStops)
        {
            super.onPostExecute(busStops);
            for (BusStop busStop: busStops)
            {
                busStopNames.add(busStop.getBusStopName());
            }
            progressBar.setVisibility(View.GONE);
        }
    }*/

    /**
     * This method is called by the on screen back button.
     *
     * @param view Not used.
     */
    public void exit(View view)
    {
        finish();
    }
}