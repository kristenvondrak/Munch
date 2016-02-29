package com.example.kristenvondrak.dartmunch.Menu;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.kristenvondrak.dartmunch.Main.Constants;
import com.example.kristenvondrak.dartmunch.Main.MainTabFragment;
import com.example.kristenvondrak.dartmunch.Main.OnDateChangedListener;
import com.example.kristenvondrak.dartmunch.Main.Utils;
import com.example.kristenvondrak.dartmunch.Parse.Offering;
import com.example.kristenvondrak.dartmunch.Parse.Recipe;
import com.example.kristenvondrak.dartmunch.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


public class MenuFragment extends Fragment implements SearchView.OnQueryTextListener, MainTabFragment {


    public static final String EXTRA_RECIPE_ID = "EXTRA_RECIPE_ID";
    public static final String EXTRA_DATE = "EXTRA_DATE";
    public static final String EXTRA_USERMEAL_INDEX = "EXTRA_USERMEAL_INDEX";

    public static final int MENU = 0;
    public static final int DIARY = 1;

    // Header
    private AppCompatActivity m_Activity;
    private Calendar m_Calendar = Calendar.getInstance();
    private int m_Mode = 0;

    // Main
    private ProgressBar m_ProgressSpinner;

    // Venue Tabs
    private TableRow m_VenueTabs;
    private View m_CurrentVenue;

    // Meal Time Tabs
    private TableRow m_MealTimesTabs;
    private View m_CurrentMealTime;

    // Menu Tabs
    private LinearLayout m_MenuTabsLinearLayout;
    private View m_CurrentMenu;

    // Food Items
    private List<Recipe> m_MenuItemsList;
    private MenuItemListAdapter m_MenuItemListAdapter;
    private ListView m_MenuItemsListView;
    private LinearLayout m_EmptyMenuText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_menu, container, false);

        m_Activity = (AppCompatActivity) getActivity();
        setHasOptionsMenu(true);

        try {
            m_Mode = ((MenuFragmentHost) m_Activity).getMode();
        } catch (ClassCastException e) {
            // activity must implement MenuFragmentHost interface
        }


        initializeViews(v);
        initializeListeners();

        // Create the venue tabs
        for (Constants.Venue venue : Constants.Venue.values()) {
            ViewGroup tab = (ViewGroup)inflater.inflate(R.layout.menu_tab, null);
            ((TextView)tab.findViewById(R.id.menu_tab_text)).setText(Constants.venueDisplayStrings.get(venue));
            tab.setOnClickListener(venueTabOnClickListener());
            setHighlight(tab, false);
            tab.setTag(venue.name());
            m_VenueTabs.addView(tab);
        }
        updateView(m_VenueTabs);
        m_CurrentVenue = m_VenueTabs.getChildAt(0);
        setHighlight(m_CurrentVenue, true);

        // Create list of recipes and set the adapter
        m_MenuItemsList = new ArrayList<>();
        m_MenuItemListAdapter = new MenuItemListAdapter(m_Activity, this);
        m_MenuItemsListView.setAdapter(m_MenuItemListAdapter);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        changeInVenue();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {

        inflater.inflate(R.menu.menu_tab_menu, menu);

        // Enable search
        MenuItem search = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) search.getActionView();
        searchView.setOnQueryTextListener(this);

        // Need to clear search when done
        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.action_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Clear search when search view is closing
                m_MenuItemListAdapter.updateData(m_MenuItemsList);

                return true;
            }
        });

        // Enable the default home button
        final android.support.v7.app.ActionBar ab = m_Activity.getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);


        // Only show if we are in the diary tab
        if (m_Mode == MENU) {
            ab.setHomeAsUpIndicator(R.drawable.ic_dummy_menu);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;

        } else if (id == android.R.id.home) {
            if (m_Mode == DIARY) {
                Intent intent = new Intent();
                m_Activity.setResult(m_Activity.RESULT_CANCELED, intent);
                m_Activity.finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    // ----------------------------------------------------------------------------------- Views

    private void initializeViews(View v) {
        m_ProgressSpinner = (ProgressBar) v.findViewById(R.id.progress_spinner);
        m_VenueTabs = (TableRow) v.findViewById(R.id.venue_tabs_row);
        m_MealTimesTabs = (TableRow) v.findViewById(R.id.mealtime_tabs_row);
        m_MenuTabsLinearLayout = (LinearLayout) v.findViewById(R.id.category_tabs_ll);
        m_MenuItemsListView = (ListView) v.findViewById(R.id.food_items_list_view);
        m_EmptyMenuText = (LinearLayout) v.findViewById(R.id.empty_food_list);

    }



    // --------------------------------------------------------------------------------- Listeners

    private void initializeListeners() {

        // Tabs need to be resized when changed
        for (int i = 0; i < m_MenuTabsLinearLayout.getChildCount(); i++) {
            final int index = i;
            final View tv = m_MenuTabsLinearLayout.getChildAt(i).findViewById(R.id.menu_tab_text);
            tv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    View v = m_MenuTabsLinearLayout.getChildAt(index).findViewById(R.id.menu_tab_highlight);
                    v.setMinimumWidth(tv.getWidth());
                    v.invalidate();
                    v.requestLayout();

                }
            });
        }
    }

    private View.OnClickListener mealTimeTabOnClickListener() {
        return (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setHighlight(m_CurrentMealTime, false);
                m_CurrentMealTime = v;
                setHighlight(v, true);
                update();
            }
        });
    }

    private View.OnClickListener menuTabOnClickListener() {
        return (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setHighlight(m_CurrentMenu, false);
                m_CurrentMenu = v;
                setHighlight(v, true);
                update();
            }
        });
    }


    private View.OnClickListener venueTabOnClickListener() {
        return (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setHighlight(m_CurrentVenue, false);
                m_CurrentVenue = v;
                setHighlight(v, true);
                changeInVenue();
            }
        });
    }


    public void setHighlight(View v, boolean highlight) {
        TextView tv = (TextView)v.findViewById(R.id.menu_tab_text);
        View h = v.findViewById(R.id.menu_tab_highlight);
        if (highlight) {
            h.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setTextColor(getResources().getColor(R.color.colorPrimary));
        } else {
            h.setBackgroundColor(getResources().getColor(R.color.transparent));
            tv.setTypeface(Typeface.DEFAULT);
            tv.setTextColor(getResources().getColor(R.color.gray));
        }
    }

    private void updateView(View v) {
        v.invalidate();
        v.requestLayout();
    }


    // ----------------------------------------------------------------------------------- Main

    public void changeInVenue() {

        // Update meal times
        boolean found = false;
        m_MealTimesTabs.removeAllViews();
        LayoutInflater inflater = m_Activity.getLayoutInflater();

        Constants.Venue newVenue = Constants.Venue.valueOf(m_CurrentVenue.getTag().toString());
        for (Constants.MealTime mealTime : Constants.mealTimesForVenue.get(newVenue)) {
            ViewGroup tab = (ViewGroup)inflater.inflate(R.layout.menu_tab, null);
            ((TextView)tab.findViewById(R.id.menu_tab_text)).setText(Constants.mealTimeDisplayStrings.get(mealTime));
            tab.setOnClickListener(mealTimeTabOnClickListener());

            if (m_CurrentMealTime != null && mealTime.name().equalsIgnoreCase(m_CurrentMealTime.getTag().toString())) {
                found = true;
                m_CurrentMealTime = tab;
                setHighlight(tab, true);
            } else {
                setHighlight(tab, false);
            }
            tab.setTag(mealTime);
            m_MealTimesTabs.addView(tab);
        }

        if (!found) {
            m_CurrentMealTime = m_MealTimesTabs.getChildAt(0);
            setHighlight(m_CurrentMealTime, true);
        }
        updateView(m_MealTimesTabs);

        // Update menus
        found = false;
        m_MenuTabsLinearLayout.removeAllViews();
        for (Constants.Menu menu : Constants.menusForVenue.get(newVenue)) {
            final ViewGroup tab = (ViewGroup)inflater.inflate(R.layout.menu_tab_scrollview, null);
            ((TextView)tab.findViewById(R.id.menu_tab_text)).setText(Constants.menuDisplayStrings.get(menu));
            tab.setOnClickListener(menuTabOnClickListener());

            if (m_CurrentMenu != null && menu.name().equals(m_CurrentMenu.getTag().toString())) {
                m_CurrentMenu = tab;
                found = true;
                setHighlight(tab, true);
            } else {
                setHighlight(tab, false);
            }
            tab.setTag(menu);

            // Need this
            tab.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    View v = tab.findViewById(R.id.menu_tab_highlight);
                    int width = (tab.findViewById(R.id.menu_tab_text)).getWidth();
                    v.setMinimumWidth((int)(width * 1.15));
                    v.invalidate();
                    v.requestLayout();

                }
            });

            m_MenuTabsLinearLayout.addView(tab);
        }

        if (!found) {
            m_CurrentMenu = m_MenuTabsLinearLayout.getChildAt(0);
            setHighlight(m_CurrentMenu, true);
        }
        updateView(m_MenuTabsLinearLayout);

        update();
    }

    public void onMenuItemClick(Recipe recipe) {
        /*
        if (searchView.isShown()) {
            searchMenuItem.collapseActionView();
            searchView.setQuery("", false);
        }
         */

        Intent intent = new Intent(m_Activity, NutritionActivity.class);
        intent.putExtra(EXTRA_RECIPE_ID, recipe.getObjectId());
        intent.putExtra(EXTRA_DATE, m_Calendar.getTimeInMillis());

        // Get user meal (breakfast, lunch, dinner, snacks) from meal time
        Constants.MealTime mealTime = Constants.MealTime.valueOf(m_CurrentMealTime.getTag().toString());
        intent.putExtra(EXTRA_USERMEAL_INDEX, Utils.mealTimeToUserMealIndex(mealTime));
        m_Activity.startActivityForResult(intent, Constants.REQUEST_ADD_FROM_MENU);
        //m_Activity.overridePendingTransition(R.anim.none, R.anim.slide_in_from_bottom);

    }


    private void notifyMenuListAdapter() {
        Utils.hideProgressSpinner(m_ProgressSpinner);
        m_MenuItemListAdapter.updateData(m_MenuItemsList);

        // If no recipes found, show message
        if (m_MenuItemsList.isEmpty()) {
            m_EmptyMenuText.setVisibility(View.VISIBLE);
        } else {
            m_EmptyMenuText.setVisibility(View.GONE);
        }
    }


    // ---------------------------------------------------------------------------------- Parse

    public void update(Calendar calendar) {
        m_Calendar.setTimeInMillis(calendar.getTimeInMillis());
        update();
    }

    private void update() {

        Utils.showProgressSpinner(m_ProgressSpinner);

        String venue = Constants.venueParseStrings
                .get(Constants.Venue.valueOf(m_CurrentVenue.getTag().toString()));

        String[] mealtime = Constants.mealTimeParseStrings
                .get(Constants.MealTime.valueOf(m_CurrentMealTime.getTag().toString()));

        String menu = Constants.menuParseStrings
                .get(Constants.Menu.valueOf(m_CurrentMenu.getTag().toString()));

        int day = m_Calendar.get(Calendar.DAY_OF_MONTH);
        int month = m_Calendar.get(Calendar.MONTH) + 1;
        int year = m_Calendar.get(Calendar.YEAR);

        queryParseRecipes(day, month, year, venue, mealtime, menu);
    }


    private void queryParseRecipes(int day, int month, int year, String venueKey,
                                   String[] mealNames, String menuName) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Offering");
        query.whereEqualTo("month", month);
        query.whereEqualTo("day", day);
        query.whereEqualTo("year", year);


        if (venueKey != null) {
            query.whereEqualTo("venueKey", venueKey);
        }
        if (mealNames != null) {
            query.whereContainedIn("mealName", Arrays.asList(mealNames));
        }
        if (menuName != null) {
            query.whereEqualTo("menuName", menuName);
        }

        // query.orderByAscending
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> offeringsList, ParseException e) {
                if (e == null) {
                    m_MenuItemsList.clear();

                    List<ParseQuery<ParseObject>> queryList = new ArrayList<ParseQuery<ParseObject>>();
                    for (ParseObject object : offeringsList) {
                        Offering offering = (Offering) object;
                        ParseRelation<ParseObject> relation = offering.getRecipes();
                        ParseQuery q = relation.getQuery();
                        queryList.add(q);
                    }

                    if (queryList.isEmpty()) {
                        notifyMenuListAdapter();
                        return;
                    }

                    ParseQuery<ParseObject> recipesQuery = ParseQuery.or(queryList);
                    recipesQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
                    recipesQuery.orderByAscending("name");
                    recipesQuery.setLimit(1000);


                    recipesQuery.findInBackground(new FindCallback<ParseObject>() {
                        public void done(List<ParseObject> list, ParseException e) {

                            // Create the new list of recipes
                            if (e == null) {
                                for (ParseObject object : list)
                                    m_MenuItemsList.add((Recipe) object);
                            }

                            // If the search bar is currently open, filter the results
                           // if (SEARCH_MODE) {
                                // resetSearch();
                                // updateSearch(m_MenuItemsList, getSearchText());
                           //}
                            notifyMenuListAdapter();
                        }
                    });
                } else {
                    // do something with the error
                }
            }
        });
    }



    // ---------------------------------------------------------------------------------- Search

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        m_MenuItemListAdapter.getFilter().filter(query);
        return true;
    }


}