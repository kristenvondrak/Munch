package com.example.kristenvondrak.dartmunch.Main;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kristenvondrak.dartmunch.Diary.DiaryFragment;
import com.example.kristenvondrak.dartmunch.Diary.EditFoodActivity;
import com.example.kristenvondrak.dartmunch.Menu.MenuFragment;
import com.example.kristenvondrak.dartmunch.Menu.MenuFragmentHost;
import com.example.kristenvondrak.dartmunch.Preferences.PrefsFragment;
import com.example.kristenvondrak.dartmunch.R;
import com.example.kristenvondrak.dartmunch.Stats.StatsFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements MenuFragmentHost {

    public Activity m_Activity;
    public Calendar m_Calendar;
    private TabLayout m_TabLayout;
    private int m_CurrentTab = 0;

    // Toolbar
    private Toolbar m_Toolbar;
    private View m_DateToolbar;
    //private View m_StatsToolbar;
    private ImageView m_NextDateButton;
    private ImageView m_PreviousDateButton;
    private TextView m_CurrentDateTextView;
    private TextView m_TitleHeader;
    private DatePickerDialog.OnDateSetListener m_DatePickerListener;

    private int[] m_TabIcons = {
            R.drawable.ic_menu,
            R.drawable.ic_diary,
            R.drawable.ic_stats,
            R.drawable.ic_prefs
    };

    private String[] m_TabTitles = {
            "Menu",
            "Diary",
            "Prefs",
            "Stats"
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
        setContentView(R.layout.activity_main);

        m_Activity = this;
        m_Calendar = Calendar.getInstance();


        m_Toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(m_Toolbar);

        final android.support.v7.app.ActionBar ab = getSupportActionBar(); // TODO: dont think we need this
        ab.setDisplayShowHomeEnabled(false); // hide the default home button
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setDisplayShowTitleEnabled(false); // disable the title

        initViews();
        initListeners();


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        m_SectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        m_ViewPager = (ViewPager) findViewById(R.id.container);
        m_ViewPager.setAdapter(m_SectionsPagerAdapter);
        m_ViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                m_CurrentTab = position;

                // TODO: cleaner way
                if (position == 2 || position == 3) {
                    m_DateToolbar.setVisibility(View.INVISIBLE);
                    m_TitleHeader.setVisibility(View.VISIBLE);
                    m_TitleHeader.setText(position == 2
                            ? getResources().getString(R.string.nutrition_tab) : getResources().getString(R.string.prefs_tab));
                } else {
                    m_DateToolbar.setVisibility(View.VISIBLE);
                    m_TitleHeader.setVisibility(View.INVISIBLE);
                }

                // Change tab icon colors
                for (int i = 0; i < m_TabIcons.length; i++) {
                    int tint = i == position ? getResources().getColor(R.color.colorPrimaryDark) : getResources().getColor(R.color.lightGray);
                    m_TabLayout.getTabAt(i).getIcon().setTint(tint);
                }

                // Update curren
                Fragment fragment = getCurrentFragment();
                if (fragment instanceof MainTabFragment) {
                    ((MainTabFragment) fragment).update(m_Calendar);
                }




            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });


        m_TabLayout = (TabLayout) findViewById(R.id.tabs);
        m_TabLayout.setupWithViewPager(m_ViewPager);
        setupTabIcons();
    }

    private void initViews() {
        // Date located in toolbar
        Toolbar toolbar = (Toolbar) m_Activity.findViewById(R.id.toolbar);
        m_NextDateButton = (ImageView) toolbar.findViewById(R.id.next_date_btn);
        m_PreviousDateButton = (ImageView) toolbar.findViewById(R.id.prev_date_btn);
        m_CurrentDateTextView = (TextView) toolbar.findViewById(R.id.date_text_view);
        m_DateToolbar = toolbar.findViewById(R.id.date_selector);
        m_TitleHeader = (TextView)toolbar.findViewById(R.id.title_header);

        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.US);
        m_CurrentDateTextView.setText(sdf.format(m_Calendar.getTime()));
    }

    private void initListeners() {
        m_DatePickerListener = new DatePickerDialog.OnDateSetListener() {

            // when dialog box is closed, below method will be called.
            public void onDateSet(DatePicker view, int selectedYear,
                                  int selectedMonth, int selectedDay) {
                m_Calendar.set(Calendar.YEAR, selectedYear);
                m_Calendar.set(Calendar.MONTH, selectedMonth);
                m_Calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                onTimeChange();
            }
        };

        m_NextDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_Calendar.add(Calendar.DATE, 1);
                onTimeChange();
            }
        });

        m_PreviousDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_Calendar.add(Calendar.DATE, -1);
                onTimeChange();
            }
        });


        m_CurrentDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Calendar picker dialog
                new DatePickerDialog(m_Activity, R.style.BasicAlertDialog,
                        m_DatePickerListener, m_Calendar.get(Calendar.YEAR),
                        m_Calendar.get(Calendar.MONTH), m_Calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void setupTabIcons() {
        for (int i = 0; i < m_TabIcons.length; i++) {
            int color = (i == 0) ? R.color.colorPrimaryDark : R.color.lightGray;
            m_TabLayout.getTabAt(i).setIcon(m_TabIcons[i]);
            m_TabLayout.getTabAt(i).getIcon().setTint(getResources().getColor(color));
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_main, menu);

       /* SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        m_SearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        m_SearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        m_SearchView.setSubmitButtonEnabled(true); */

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @Override
    public int getMode() {
        return MenuFragment.MENU;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter  {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0)
                return new MenuFragment();

            else if (position == 1)
                return new DiaryFragment();

            else if (position == 2)
                return new StatsFragment();

            else if (position == 3)
                return new PrefsFragment();

            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check which request we're responding to
        switch (requestCode) {

            case Constants.REQUEST_ADD_FROM_MENU:
                // The user added a recipe to the diary
                if (resultCode == RESULT_OK) {
                    String message = getResources().getString(R.string.toast_food_added);
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }
                break;

            case Constants.REQUEST_EDIT_FOOD:
                // The user saved or deleted food from diary
                if (resultCode == RESULT_OK) {
                    String message = data.getStringExtra(EditFoodActivity.EXTRA_TOAST_MESSAGE);
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }

    }

    private List<Fragment> getFragments() {
        return getSupportFragmentManager().getFragments();
    }

    private Fragment getCurrentFragment() {
        return getFragments().get(m_CurrentTab);
    }

    private void onTimeChange() {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.US);
        m_CurrentDateTextView.setText(sdf.format(m_Calendar.getTime()));
        Fragment fragment = getCurrentFragment();
        if (fragment instanceof MainTabFragment) {
            ((MainTabFragment) fragment).update(m_Calendar);
        }
    }

}
