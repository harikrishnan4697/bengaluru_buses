package com.bangalorebuses.tracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.bangalorebuses.R;
import com.bangalorebuses.search.SearchActivity;
import com.bangalorebuses.utils.Constants;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Stack;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_APPEND;

public class BusTrackerFragment extends Fragment
{
    private Button busNumberSelectionButton;
    private ListView recentSearchesListView;
    private ArrayList<String> recentSearches = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.bus_tracker_fragment, container, false);
        busNumberSelectionButton = (Button) view.findViewById(R.id.choose_bus_number_button);
        busNumberSelectionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                searchBusNumber();
            }
        });
        ImageView busNumberSelectionImageView = (ImageView) view.findViewById(R.id.busNumberSelectionImageView);
        busNumberSelectionImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                searchBusNumber();
            }
        });
        recentSearchesListView = (ListView) view.findViewById(R.id.recentSearchesListView);
        initialiseRecentSearches();
        return view;
    }

    private void searchBusNumber()
    {
        Intent searchActivityIntent = new Intent(getContext(), SearchActivity.class);
        searchActivityIntent.putExtra("SEARCH_TYPE", Constants.SEARCH_TYPE_BUS_ROUTE);
        startActivityForResult(searchActivityIntent, Constants.SEARCH_REQUEST_CODE);
    }

    private void initialiseRecentSearches()
    {
        try
        {
            recentSearches.clear();
            FileInputStream fileInputStream = getActivity().openFileInput(Constants.ROUTE_SEARCH_HISTORY_FILENAME);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            Stack<String> stack = new Stack<>();
            ArrayList<String> arrayList = new ArrayList<>();
            String line;

            while ((line = bufferedReader.readLine()) != null)
            {
                recentSearches.add(line);
                stack.push(line);
            }

            for (int i = 0; i < 5; i++)
            {
                if (!stack.isEmpty())
                {
                    arrayList.add(stack.pop());
                }
                else
                {
                    break;
                }
            }

            ArrayAdapter<String> listAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, arrayList);
            recentSearchesListView.setAdapter(listAdapter);
            recentSearchesListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    Intent trackBusActivityIntent = new Intent(getContext(), TrackBusActivity.class);
                    trackBusActivityIntent.putExtra("ROUTE_NUMBER", parent.getItemAtPosition(position).toString());
                    startActivity(trackBusActivityIntent);
                }
            });
        }
        catch (IOException e)
        {
            // TODO handle exception
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK && requestCode == Constants.SEARCH_REQUEST_CODE)
        {
            try
            {
                FileOutputStream fileOutputStream = getActivity().openFileOutput(Constants.ROUTE_SEARCH_HISTORY_FILENAME, MODE_APPEND);
                fileOutputStream.write((data.getStringExtra("ROUTE_NUMBER") + "\n").getBytes());
                fileOutputStream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            Intent trackBusActivityIntent = new Intent(getContext(), TrackBusActivity.class);
            trackBusActivityIntent.putExtra("ROUTE_NUMBER", data.getStringExtra("ROUTE_NUMBER"));
            startActivity(trackBusActivityIntent);
        }
    }

    @Override
    public void onResume()
    {
        initialiseRecentSearches();
        super.onResume();
    }
}