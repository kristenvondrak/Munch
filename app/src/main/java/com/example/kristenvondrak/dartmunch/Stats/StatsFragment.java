package com.example.kristenvondrak.dartmunch.Stats;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;

import com.example.kristenvondrak.dartmunch.Main.MainTabFragment;
import com.example.kristenvondrak.dartmunch.Main.OnDateChangedListener;
import com.example.kristenvondrak.dartmunch.R;

import java.util.Calendar;

/**
 * Created by kristenvondrak on 2/28/16.
 */
public class StatsFragment extends Fragment implements MainTabFragment {

    public static final String TAG = StatsFragment.class.getName();

    private FragmentTabHost m_TabHost;

    private String[] m_TabTitles = {
            "Calories",
            "Progress"
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_stats, container, false);

        m_TabHost = (FragmentTabHost) root.findViewById(android.R.id.tabhost);

        // Important: Must use child FragmentManager.
        m_TabHost.setup(getActivity(), getChildFragmentManager(), android.R.id.tabcontent);

        m_TabHost.addTab(m_TabHost.newTabSpec(m_TabTitles[0]).setIndicator(m_TabTitles[0]),
                CaloriesFragment.class, null);

        m_TabHost.addTab(m_TabHost.newTabSpec(m_TabTitles[1]).setIndicator(m_TabTitles[1]),
                ProgressFragment.class, null);

        // Highlight the tabs accordingly
        changeTabColor(m_TabHost.getTabWidget().getChildAt(0), R.color.diaryAccent);
        changeTabColor(m_TabHost.getTabWidget().getChildAt(1), R.color.lightGray);
        m_TabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {
                // Unselected Tabs
                for (int i = 0; i < m_TabHost.getTabWidget().getChildCount(); i++) {
                    changeTabColor(m_TabHost.getTabWidget().getChildAt(i), R.color.lightGray);
                }
                // Selected Tab
                changeTabColor(m_TabHost.getCurrentTabView(), R.color.diaryAccent);
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
        Log.d("** update in stats", "*****************");
        for (Fragment fragment: getChildFragmentManager().getFragments()){
            Log.d("--------", "----------------");
            if (fragment instanceof MainTabFragment) {
                ((MainTabFragment) fragment).update(calendar);
                Log.d("** update in stats", "^^ yup");
            } else {
                Log.d("** update in stats", "^^ nope");
            }
        }
    }

}
