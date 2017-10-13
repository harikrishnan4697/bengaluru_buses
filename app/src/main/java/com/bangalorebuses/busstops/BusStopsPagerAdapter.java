package com.bangalorebuses.busstops;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class BusStopsPagerAdapter extends FragmentStatePagerAdapter
{
    int mNumOfTabs;
    private NearbyFragment nearbyFragment;

    public BusStopsPagerAdapter(FragmentManager fm, int NumOfTabs)
    {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    public NearbyFragment getNearbyFragment()
    {
        return nearbyFragment;
    }

    @Override
    public Fragment getItem(int position)
    {

        switch (position)
        {
            case 0:
                nearbyFragment = new NearbyFragment();
                return nearbyFragment;
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