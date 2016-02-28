package com.example.kristenvondrak.dartmunch.Diary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.kristenvondrak.dartmunch.Main.Constants;
import com.example.kristenvondrak.dartmunch.Main.Utils;
import com.example.kristenvondrak.dartmunch.Menu.NutritionActivity;
import com.example.kristenvondrak.dartmunch.Parse.Recipe;
import com.example.kristenvondrak.dartmunch.R;

import java.util.Calendar;
import java.util.List;


public class RecipeFragment extends Fragment implements SearchView.OnQueryTextListener {

    public static final String EXTRA_RECIPE_ID = "EXTRA_RECIPE_ID";
    public static final String EXTRA_USERMEAL_INDEX = "EXTRA_USERMEAL_INDEX";
    public static final String EXTRA_DATE = "EXTRA_DATE";

    protected Activity m_Activity;
    protected RecipeFragment m_Fragment;
    protected Calendar m_Calendar;

    // Views
    protected ListView m_ListView;
    protected ProgressBar m_ProgressSpinner;

    // Data
    protected List<Recipe> m_RecipesList;
    protected RecipeListAdapter m_RecipeListAdapter;
    protected int m_SelectedMealTime = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        m_RecipeListAdapter.getFilter().filter(query);
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
                m_RecipeListAdapter.update(m_RecipesList);
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
        Intent intent = new Intent(m_Activity, NutritionActivity.class);
        intent.putExtra(EXTRA_RECIPE_ID, recipe.getObjectId());
        intent.putExtra(EXTRA_DATE, m_Calendar.getTimeInMillis());
        intent.putExtra(EXTRA_USERMEAL_INDEX, m_SelectedMealTime);

        m_Activity.startActivityForResult(intent, Constants.REQUEST_ADD_FROM_DIARY);
        //m_Activity.overridePendingTransition(R.anim.none, R.anim.slide_in_from_bottom);
    }


}