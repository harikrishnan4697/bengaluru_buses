package com.bangalorebuses.busstops;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class BusStopsPagerAdapter extends FragmentStatePagerAdapter
{
    int mNumOfTabs;
    private NearbyBusStopsFragment nearbyBusStopsFragment;

    public BusStopsPagerAdapter(FragmentManager fm, int NumOfTabs)
    {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    public NearbyBusStopsFragment getNearbyBusStopsFragment()
    {
        return nearbyBusStopsFragment;
    }

    @Override
    public Fragment getItem(int position)
    {

        switch (position)
        {
            case 0:
                nearbyBusStopsFragment = new NearbyBusStopsFragment();
                return nearbyBusStopsFragment;
            case 1:
                return new AllBusStopsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount()
    {
        return mNumOfTabs;
    }
}