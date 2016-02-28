package com.example.kristenvondrak.dartmunch.Recent;

import android.os.Bundle;
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

import com.example.kristenvondrak.dartmunch.Diary.AddFoodActivity;
import com.example.kristenvondrak.dartmunch.Diary.RecipeFragment;
import com.example.kristenvondrak.dartmunch.Diary.RecipeListAdapter;
import com.example.kristenvondrak.dartmunch.Main.Utils;
import com.example.kristenvondrak.dartmunch.Parse.Recipe;
import com.example.kristenvondrak.dartmunch.Parse.User;
import com.example.kristenvondrak.dartmunch.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class RecentsFragment extends RecipeFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recents, container, false);

        setHasOptionsMenu(true);
        m_Activity = getActivity();
        m_Fragment = this;

        boolean addFoodActivity = m_Activity instanceof AddFoodActivity;
        m_Calendar = addFoodActivity ? ((AddFoodActivity) m_Activity).getCalendar() : Calendar.getInstance();
        m_SelectedMealTime = addFoodActivity ? ((AddFoodActivity)m_Activity).getUserMealIndex() : 0;

        initViews(v);
        initListeners();

        // Get most recent recipes from Parse
        m_RecipesList = new ArrayList<>();
        m_RecipeListAdapter = new RecipeListAdapter(m_Activity, this, m_RecipesList);
        m_ListView.setAdapter(m_RecipeListAdapter);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        queryPastRecipes();
    }

    private void initViews(View v) {
        m_ListView = (ListView) v.findViewById(R.id.recents_listview);
        m_ProgressSpinner = (ProgressBar) v.findViewById(R.id.progress_spinner);
    }


    private void initListeners() {

    }


    private void queryPastRecipes() {
        Utils.showProgressSpinner(m_ProgressSpinner);
        m_RecipesList.clear();
        User user = (User) ParseUser.getCurrentUser();
        ParseRelation<ParseObject> pastRecipesRelation = user.getPastRecipes();
        if (pastRecipesRelation == null)
            return;

        ParseQuery query = pastRecipesRelation.getQuery();
        query.orderByDescending("updatedAt");
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (ParseObject object : objects) {
                        Recipe recipe = (Recipe) object;
                        m_RecipesList.add(recipe);
                    }
                    m_RecipeListAdapter.update(m_RecipesList);

                   /* if (m_AddUserMealActivity.inSearchMode()) {
                        resetSearch();
                        updateSearch(m_RecipesList, m_AddUserMealActivity.getSearchText());
                    }*/
                } else {
                    Log.d("ERROR:", e.getMessage());
                }
                Utils.hideProgressSpinner(m_ProgressSpinner);
            }
        });
    }



}