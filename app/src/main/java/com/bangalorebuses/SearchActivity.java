package com.bangalorebuses;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SearchActivity extends AppCompatActivity
{
    private EditText searchEditText;
    private ListView searchResultsListView;
    private ArrayAdapter<String> listAdapter;
    private BusNumberListCustomAdapter customListAdapter;
    private String searchType;
    private Intent resultIntent = new Intent();

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
        searchEditText = (EditText) findViewById(R.id.search_edit_text);
        searchResultsListView = (ListView) findViewById(R.id.search_results_list_view);
        searchType = getIntent().getStringExtra("Search_Type");
        initialiseSearchResultsList();
    }

    /**
     * This method is used to Handle the route number list and
     * filter bus routes as the user starts typing in the search box.
     */
    private void initialiseSearchResultsList()
    {
        AssetManager assetManager = getAssets();
        InputStream inputStream;
        InputStreamReader inputStreamReader;

        try
        {
            // Reads the asset
            if (searchType.equals(Constants.SEARCH_TYPE_BUS_ROUTE))
            {
                inputStream = assetManager.open("bangalore_city_bus_routes.txt");
            }
            else
            {
                inputStream = assetManager.open("bangalore_city_bus_stops.txt");
            }
            inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null)
            {
                stringBuilder.append(line);
            }

            // Converts the asset to a JSON array
            JSONArray jsonArray = new JSONArray(stringBuilder.toString());
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
                if(searchType.equals(Constants.SEARCH_TYPE_BUS_STOP))
                {
                    resultIntent.putExtra("Selected_Item", parent.getItemAtPosition(position).toString());
                }
                else
                {
                    resultIntent.putExtra("Selected_Item", parent.getItemAtPosition(position).toString());
                }
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