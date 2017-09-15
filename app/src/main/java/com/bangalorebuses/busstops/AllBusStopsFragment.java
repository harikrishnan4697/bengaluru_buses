package com.bangalorebuses.busstops;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.bangalorebuses.R;
import com.bangalorebuses.busarrivals.BusesArrivingAtBusStopActivity;
import com.bangalorebuses.core.BusStop;
import com.bangalorebuses.search.AllBusStopSearchListCustomAdaptor;

import java.util.ArrayList;

public class AllBusStopsFragment extends Fragment implements BusStopsDbQueriesHelper,
        ListView.OnItemClickListener, TextWatcher
{
    private ListView listView;
    private ProgressBar progressBar;
    private EditText editText;
    private AllBusStopsDbTask allBusStopsDbTask;
    private AllBusStopSearchListCustomAdaptor adaptor;

    public AllBusStopsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_all_bus_stops, container, false);

        // Initialise some variables
        listView = (ListView) view.findViewById(R.id.list_view);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        editText = (EditText) view.findViewById(R.id.edit_text);

        // Hide the soft keyboard by default when the activity is started
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        editText.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        allBusStopsDbTask = new AllBusStopsDbTask(this);
        allBusStopsDbTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onAllBusStopsFound(ArrayList<BusStop> busStops)
    {
        adaptor = new AllBusStopSearchListCustomAdaptor(
                getActivity(), busStops);

        listView.setAdapter(adaptor);
        listView.setOnItemClickListener(this);

        progressBar.setVisibility(View.GONE);
        editText.setEnabled(true);

        editText.addTextChangedListener(this);

        // TODO Decide weather the keyboard should auto open
        /* InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);*/
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Intent getBusesArrivingAtBusStopIntent = new Intent(getContext(), BusesArrivingAtBusStopActivity.class);
        getBusesArrivingAtBusStopIntent.putExtra("BUS_STOP_NAME", ((BusStop) parent.getItemAtPosition(position))
                .getBusStopName());
        getBusesArrivingAtBusStopIntent.putExtra("BUS_STOP_DIRECTION_NAME", ((BusStop) parent.getItemAtPosition(position))
                .getBusStopDirectionName());
        getBusesArrivingAtBusStopIntent.putExtra("BUS_STOP_ID", ((BusStop) parent.getItemAtPosition(position))
                .getBusStopId());
        startActivity(getBusesArrivingAtBusStopIntent);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
        adaptor.getFilter().filter(s);
    }

    @Override
    public void afterTextChanged(Editable s)
    {

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (allBusStopsDbTask != null)
        {
            allBusStopsDbTask.cancel(true);
        }
    }
}
