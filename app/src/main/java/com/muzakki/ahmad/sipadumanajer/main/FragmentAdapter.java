package com.muzakki.ahmad.sipadumanajer.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by jeki on 6/13/15.
 */
public class FragmentAdapter extends FragmentPagerAdapter {

    ArrayList<WeakReference<Fragment>> fragmentRef = new ArrayList<>();

    public FragmentAdapter(FragmentManager fm){
        super(fm);

    }
    /**
     * Return the Fragment associated with a specified position.
     *
     * @param position
     */
    @Override
    public Fragment getItem(int position) {
        Fragment fr = null;
        if(position==0){
            fr = new Inbox();
        }else if(position==1){
            fr = new Outbox();
        }

        fragmentRef.add(position, new WeakReference<>(fr));
        return fr;
    }

    public Fragment getFragment(int pos){
        return fragmentRef.get(pos).get();
    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return 2;
    }
}
