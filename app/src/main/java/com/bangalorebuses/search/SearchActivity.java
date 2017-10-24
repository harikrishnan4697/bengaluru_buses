package com.bangalorebuses.search;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bangalorebuses.R;
import com.bangalorebuses.favorites.FavoritesHelper;
import com.bangalorebuses.favorites.FavoritesListCustomAdapter;
import com.bangalorebuses.utils.Constants;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Stack;

import static com.bangalorebuses.utils.Constants.BUS_STOP_NAME;
import static com.bangalorebuses.utils.Constants.FAVORITES_REQUEST_CODE;
import static com.bangalorebuses.utils.Constants.FAVORITES_TYPE;
import static com.bangalorebuses.utils.Constants.FAVORITES_TYPE_BUS_ROUTE;
import static com.bangalorebuses.utils.Constants.FAVORITES_TYPE_BUS_STOP;
import static com.bangalorebuses.utils.Constants.FAVORITES_TYPE_NONE;
import static com.bangalorebuses.utils.Constants.SEARCH_TYPE_BUS_STOP;

public class SearchActivity extends AppCompatActivity implements SearchDbQueriesHelper,
        FavoritesHelper
{
    private ProgressBar progressBar;
    private EditText searchEditText;
    private ListView searchResultsListView;
    private Intent resultIntent = new Intent();
    private LinearLayout favoritesLinearLayout;
    private AllBusStopNamesTask allBusStopNamesTask;
    private ListView favoritesListView;
    private String favoritesType;
    private boolean hasFavorites = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.search_activity_default_hint);
        }

        // Hide the soft keyboard by default when the activity is started
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Initialise some variables
        searchEditText = (EditText) findViewById(R.id.bus_stop_search_edit_text);
        searchResultsListView = (ListView) findViewById(R.id.search_results_list_view);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        favoritesLinearLayout = (LinearLayout) findViewById(R.id.favorites_linear_layout);
        favoritesListView = (ListView) findViewById(R.id.favorites_list_view);

        String searchType = getIntent().getStringExtra("SEARCH_TYPE");
        String editTextHint = getIntent().getStringExtra("EDIT_TEXT_HINT");
        favoritesType = getIntent().getStringExtra(FAVORITES_TYPE);

        if (editTextHint != null)
        {
            searchEditText.setHint(editTextHint);
        }

        progressBar.setVisibility(View.VISIBLE);
        searchResultsListView.setVisibility(View.GONE);
        searchEditText.setEnabled(false);
        favoritesLinearLayout.setVisibility(View.GONE);

        if (searchType.equals(SEARCH_TYPE_BUS_STOP))
        {
            allBusStopNamesTask = new AllBusStopNamesTask(this);
            allBusStopNamesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        if (!favoritesType.equals(FAVORITES_TYPE_NONE))
        {
            initialiseFavorites();
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
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onAllBusStopNamesFound(ArrayList<String> busStopNames)
    {
        final ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                android.R.id.text1, busStopNames);

        searchResultsListView.setAdapter(listAdapter);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);

        searchResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                resultIntent.putExtra("BUS_STOP_NAME", (String) parent.getItemAtPosition(position));
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

                if (count == 0)
                {
                    if (hasFavorites)
                    {
                        favoritesLinearLayout.setVisibility(View.VISIBLE);
                    }

                    searchResultsListView.setVisibility(View.VISIBLE);
                }
                else
                {
                    searchResultsListView.setVisibility(View.VISIBLE);
                    favoritesLinearLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });

        progressBar.setVisibility(View.GONE);

        if (!hasFavorites)
        {
            searchResultsListView.setVisibility(View.VISIBLE);
            favoritesLinearLayout.setVisibility(View.GONE);
        }
        else
        {
            searchResultsListView.setVisibility(View.VISIBLE);
            favoritesLinearLayout.setVisibility(View.VISIBLE);
        }

        searchEditText.setEnabled(true);
    }

    private void initialiseFavorites()
    {
        if (favoritesListView != null)
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

                Stack<String> favoritesBackwards = new Stack<>();
                ArrayList<String> favoritesForwards = new ArrayList<>();

                for (String favorite : favorites)
                {
                    favoritesBackwards.push(favorite);
                }

                if (favoritesType.equals(FAVORITES_TYPE_BUS_ROUTE))
                {
                    while (!favoritesBackwards.isEmpty())
                    {
                        String favorite = favoritesBackwards.pop();

                        if (favorite.substring(0, 3).equals("^%b"))
                        {
                            favoritesForwards.add(favorite);
                        }
                    }
                }
                else if (favoritesType.equals(FAVORITES_TYPE_BUS_STOP))
                {
                    while (!favoritesBackwards.isEmpty())
                    {
                        String favorite = favoritesBackwards.pop();

                        if (favorite.substring(0, 3).equals("^%s"))
                        {
                            favoritesForwards.add(favorite.substring(0, favorite.indexOf("^%sd") + 4));
                        }
                    }
                }
                else
                {
                    while (!favoritesBackwards.isEmpty())
                    {
                        favoritesForwards.add(favoritesBackwards.pop());
                    }
                }

                if (favoritesForwards.size() > 0)
                {
                    FavoritesListCustomAdapter adapter = new FavoritesListCustomAdapter(this, this,
                            favoritesForwards, false);
                    favoritesListView.setAdapter(adapter);
                    favoritesLinearLayout.setVisibility(View.VISIBLE);
                    searchResultsListView.setVisibility(View.GONE);
                    hasFavorites = true;
                }
                else
                {
                    favoritesLinearLayout.setVisibility(View.GONE);
                    searchResultsListView.setVisibility(View.VISIBLE);
                    hasFavorites = false;
                }

                fileInputStream.close();
                inputStreamReader.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFavoriteClicked(String favorite)
    {
        String favoriteBusStopName = favorite.substring(favorite
                .indexOf("^%sn") + 4, favorite.indexOf("^%sd"));

        resultIntent.putExtra(BUS_STOP_NAME, favoriteBusStopName);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onFavoriteDeleted(String favorite)
    {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            if (requestCode == FAVORITES_REQUEST_CODE)
            {
                resultIntent.putExtra(BUS_STOP_NAME,
                        data.getStringExtra(BUS_STOP_NAME));
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (allBusStopNamesTask != null)
        {
            allBusStopNamesTask.cancel(true);
        }
    }
}