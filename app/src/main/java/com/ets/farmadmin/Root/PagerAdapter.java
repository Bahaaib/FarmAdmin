package com.ets.farmadmin.Root;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.ets.farmadmin.Fragments.FruitsFragment;
import com.ets.farmadmin.Fragments.OffersFragment;
import com.ets.farmadmin.Fragments.VegeFragment;


public class PagerAdapter extends FragmentStatePagerAdapter {

    //Setting up the View Pager with tabs
    private int mTabsNum;

    public PagerAdapter(FragmentManager fm, int tabsNum) {
        super(fm);
        this.mTabsNum = tabsNum;
    }

    //Here we control the flow of the pager, What Fragment to go on clicking to which Tab..
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new VegeFragment();
            case 1:
                return new FruitsFragment();
            case 2:
                return new OffersFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mTabsNum;
    }
}