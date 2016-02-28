package com.example.kristenvondrak.dartmunch.Diary;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import com.example.kristenvondrak.dartmunch.Database.DatabaseFragment;
import com.example.kristenvondrak.dartmunch.Main.Constants;
import com.example.kristenvondrak.dartmunch.Menu.MenuFragment;
import com.example.kristenvondrak.dartmunch.Menu.MenuFragmentHost;
import com.example.kristenvondrak.dartmunch.MyFoods.MyFoodsFragment;
import com.example.kristenvondrak.dartmunch.MyMeals.MyMealsFragment;
import com.example.kristenvondrak.dartmunch.R;
import com.example.kristenvondrak.dartmunch.Recent.RecentsFragment;

import java.util.Calendar;

public class AddFoodActivity extends AppCompatActivity implements MenuFragmentHost {

    private Toolbar m_Toolbar;
    private TabLayout m_TabLayout;
    private Calendar m_Calendar;
    private int m_SelectedUserMeal;

    private String[] m_TabTitles = {
            "DDS",
            "Recent",
            "My Meals",
            "My Foods",
            "Database"
    };

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter m_SectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager m_ViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);

        m_Toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(m_Toolbar);


        final android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true);         // show the default home button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(true);        // show title


        // Retrieve data from calling activity
        Intent intent = getIntent();

        // Meal time
        m_SelectedUserMeal = intent.getIntExtra(DiaryFragment.EXTRA_USERMEAL_INDEX, 0);

        // Date
        long date = intent.getLongExtra(DiaryFragment.EXTRA_DATE, Calendar.getInstance().getTimeInMillis());
        m_Calendar = Calendar.getInstance();
        m_Calendar.setTimeInMillis(date);


        // Create the adapter that will return a fragment for each of the five
        // primary sections of the activity.
        m_SectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        m_ViewPager = (ViewPager) findViewById(R.id.container);
        m_ViewPager.setAdapter(m_SectionsPagerAdapter);

        // Add ViewPager to the tab layout
        m_TabLayout = (TabLayout) findViewById(R.id.tabs);
        m_TabLayout.setupWithViewPager(m_ViewPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_food, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public int getMode() {
        return MenuFragment.DIARY;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return new MenuFragment();
                case 1:
                    return new RecentsFragment();
                case 2:
                    return new MyMealsFragment();
                case 3:
                    return new MyFoodsFragment();
                case 4:
                    return new DatabaseFragment();
                default:
                    // should never get here
                    return null;
            }
        }

        @Override
        public int getCount() {
            return m_TabTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return m_TabTitles[position];
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check which request we're responding to
        switch (requestCode) {

            case Constants.REQUEST_ADD_FROM_DIARY:
                // The user added a recipe to the diary
                if (resultCode == RESULT_OK) {
                    String message = getResources().getString(R.string.toast_food_added);
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }

    }

    public int getUserMealIndex() {
        return m_SelectedUserMeal;
    }

    public Calendar getCalendar() {
        return m_Calendar;
    }
}
