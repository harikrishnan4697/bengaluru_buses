package com.bangalorebuses.tracker;

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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.bangalorebuses.R;
import com.bangalorebuses.utils.CommonMethods;

import java.util.ArrayList;

import static com.bangalorebuses.utils.Constants.db;

public class BusesActivity extends AppCompatActivity implements BusesDbQueriesHelper,
        ListView.OnItemClickListener, TextWatcher
{
    private ListView listView;
    private ProgressBar progressBar;
    private AllBusRoutesDbTask allBusRoutesDbTask;
    private EditText editText;
    private AllBusRoutesSearchListCustomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buses);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.buses_title);
        }

        if (db == null)
        {
            CommonMethods.initialiseDatabase(this);
        }

        // Hide the soft keyboard by default when the activity is started
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Initialise some variables
        listView = (ListView) findViewById(R.id.list_view);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        editText = (EditText) findViewById(R.id.bus_search_edit_text);

        progressBar.setVisibility(View.VISIBLE);
        editText.setEnabled(false);


        allBusRoutesDbTask = new AllBusRoutesDbTask(this);
        allBusRoutesDbTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
    public void onAllBusRoutesFound(ArrayList<String> busRouteNumbers)
    {
        adapter = new AllBusRoutesSearchListCustomAdapter(
                this, busRouteNumbers);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        progressBar.setVisibility(View.GONE);
        editText.setEnabled(true);

        editText.addTextChangedListener(this);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
        adapter.getFilter().filter(s);
    }

    @Override
    public void afterTextChanged(Editable s)
    {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Intent trackBusActivityIntent = new Intent(this, TrackBusActivity.class);

        trackBusActivityIntent.putExtra("ROUTE_NUMBER", (String) parent.getItemAtPosition(
                position));

        startActivity(trackBusActivityIntent);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if (allBusRoutesDbTask != null)
        {
            allBusRoutesDbTask.cancel(true);
        }
    }
}
