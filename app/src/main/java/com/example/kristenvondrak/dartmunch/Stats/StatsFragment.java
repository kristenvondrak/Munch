package com.example.kristenvondrak.dartmunch.Stats;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;

import com.example.kristenvondrak.dartmunch.Main.MainTabFragment;
import com.example.kristenvondrak.dartmunch.R;

import java.util.Calendar;

/**
 * Created by kristenvondrak on 2/28/16.
 */
public class StatsFragment extends Fragment implements MainTabFragment {

    public static final String TAG = StatsFragment.class.getName();

    private FragmentTabHost m_TabHost;
    private AppCompatActivity m_Activity;

    private String[] m_TabTitles = {
            "Calories",
            "Macros"
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_stats, container, false);

        m_Activity = (AppCompatActivity)getActivity();
        setHasOptionsMenu(true);
        m_TabHost = (FragmentTabHost) root.findViewById(android.R.id.tabhost);

        // Important: Must use child FragmentManager.
        m_TabHost.setup(getActivity(), getChildFragmentManager(), android.R.id.tabcontent);

        m_TabHost.addTab(m_TabHost.newTabSpec(m_TabTitles[0]).setIndicator(m_TabTitles[0]),
                CaloriesFragment.class, null);

        m_TabHost.addTab(m_TabHost.newTabSpec(m_TabTitles[1]).setIndicator(m_TabTitles[1]),
                ProgressFragment.class, null);

        // Highlight the tabs accordingly
        changeTabColor(m_TabHost.getTabWidget().getChildAt(0), R.color.black);
        changeTabColor(m_TabHost.getTabWidget().getChildAt(1), R.color.lightGray);
        m_TabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {
                // Unselected Tabs
                for (int i = 0; i < m_TabHost.getTabWidget().getChildCount(); i++) {
                    changeTabColor(m_TabHost.getTabWidget().getChildAt(i), R.color.lightGray);

                }
                // Selected Tab
                changeTabColor(m_TabHost.getCurrentTabView(), R.color.black);
            }
        });

        return root;
    }

    private void changeTabColor(View tab, int colorId) {
        int color = getResources().getColor(colorId);
        TextView tv = (TextView) tab.findViewById(android.R.id.title); //for Selected Tab
        tv.setTextColor(color);

    }

    public void update(Calendar calendar) {
        for (Fragment fragment: getChildFragmentManager().getFragments()){
            if (fragment instanceof MainTabFragment) {
                ((MainTabFragment) fragment).update(calendar);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_tab_stats, menu);

        final android.support.v7.app.ActionBar ab = m_Activity.getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true); // show the default home button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.ic_dummy_menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();


        // Allow user to pick date from date picker dialog
        if (id == R.id.action_calendar) {
         /*   new DatePickerDialog(m_Activity, R.style.BasicAlertDialog,
                    m_DatePickerListener, m_Calendar.get(Calendar.YEAR),
                    m_Calendar.get(Calendar.MONTH), m_Calendar.get(Calendar.DAY_OF_MONTH)).show(); */
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
