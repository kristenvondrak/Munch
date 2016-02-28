package com.example.kristenvondrak.dartmunch.MyMeals;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.kristenvondrak.dartmunch.Main.Constants;
import com.example.kristenvondrak.dartmunch.Main.Utils;
import com.example.kristenvondrak.dartmunch.Parse.Recipe;
import com.example.kristenvondrak.dartmunch.Parse.UserMeal;
import com.example.kristenvondrak.dartmunch.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MyMealsFragment extends Fragment implements SearchView.OnQueryTextListener {

    // Constants
    public static final String EXTRA_DATE = "EXTRA_DATE";
    public static final String EXTRA_USERMEAL_ID = "EXTRA_USERMEAL_ID";


    // Main
    private Activity m_Activity;
    private MyMealsFragment m_Fragment;
    private Calendar m_Calendar;

    // Views
    private ListView m_MealListView;
    private ProgressBar m_ProgressSpinner;

    // Data
    private List<UserMeal> m_MealsList;
    private MealListAdapter m_MealsListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_mymeals, container, false);

        m_Activity = getActivity();
        m_Fragment = this;
        m_Calendar = Calendar.getInstance();

        setHasOptionsMenu(true); // need for search

        initViews(v);
        initListeners();

        // Get most recent meals from Parse
        m_MealsList = new ArrayList<>();
        m_MealsListAdapter = new MealListAdapter(m_Activity, this);
        m_MealListView.setAdapter(m_MealsListAdapter);


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        queryPastMeals();
    }


    private void initViews(View v) {
        m_ProgressSpinner = (ProgressBar) v.findViewById(R.id.progress_spinner);
        m_MealListView = (ListView) v.findViewById(R.id.mymeals_listview);

    }


    private void initListeners() {

    }


    public void onMealClick(UserMeal meal) {
        // Pass the UserMeal id and date
        Intent intent = new Intent(m_Activity, AddMealActivity.class);
        intent.putExtra(EXTRA_USERMEAL_ID, meal.getObjectId());
        intent.putExtra(EXTRA_DATE, m_Calendar.getTimeInMillis());

        m_Activity.startActivityForResult(intent, Constants.REQUEST_ADD_FROM_DIARY);
        //m_Activity.overridePendingTransition(R.anim.none, R.anim.slide_in_from_bottom);
    }

    private void queryPastMeals() {
        Utils.showProgressSpinner(m_ProgressSpinner);
        m_MealsList.clear();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserMeal");
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.include("entries.recipe");
        query.orderByDescending("date");
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (ParseObject object : objects) {
                        UserMeal meal = (UserMeal) object;
                        m_MealsList.add(meal);
                    }
                    m_MealsListAdapter.updateData(m_MealsList);
                   /* if (m_Activity.inSearchMode()) {
                        resetSearch();
                        updateSearch(m_UserMealsList, m_Activity.getSearchText());
                    }*/

                } else {
                    Log.d("ERROR:", e.getMessage());
                }
                Utils.hideProgressSpinner(m_ProgressSpinner);
            }
        });

    }



    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        m_MealsListAdapter.getFilter().filter(query);
        return true;
    }


    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {

        inflater.inflate(R.menu.menu_recents, menu);

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
                m_MealsListAdapter.updateData(m_MealsList);
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Intent intent = new Intent();
            m_Activity.setResult(m_Activity.RESULT_CANCELED, intent);
            m_Activity.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onItemClick(Recipe recipe) {
        // launch nutrition activity
    }


}