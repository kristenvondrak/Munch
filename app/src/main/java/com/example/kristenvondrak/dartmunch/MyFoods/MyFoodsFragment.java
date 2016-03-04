package com.example.kristenvondrak.dartmunch.MyFoods;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.example.kristenvondrak.dartmunch.Diary.AddFoodActivity;
import com.example.kristenvondrak.dartmunch.Diary.DiaryFragment;
import com.example.kristenvondrak.dartmunch.Main.Constants;
import com.example.kristenvondrak.dartmunch.Main.Utils;
import com.example.kristenvondrak.dartmunch.Menu.NutritionActivity;
import com.example.kristenvondrak.dartmunch.Parse.Recipe;
import com.example.kristenvondrak.dartmunch.Parse.User;
import com.example.kristenvondrak.dartmunch.R;
import com.example.kristenvondrak.dartmunch.Diary.RecipeFragment;
import com.example.kristenvondrak.dartmunch.Diary.RecipeListAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MyFoodsFragment extends RecipeFragment {

    private RelativeLayout m_AddCustomFoodRow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_myfoods, container, false);

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
        queryUserRecipes();
    }

    private void initViews(View v) {
        m_ListView = (ListView) v.findViewById(R.id.myfoods_listview);
        m_ProgressSpinner = (ProgressBar) v.findViewById(R.id.progress_spinner);
        m_AddCustomFoodRow = (RelativeLayout) v.findViewById(R.id.create_new_food_row);
    }


    private void initListeners() {
        m_AddCustomFoodRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(m_Activity, CustomFoodActivity.class);
                intent.putExtra(EXTRA_USERMEAL_INDEX, m_SelectedMealTime);
                intent.putExtra(EXTRA_DATE, m_Calendar.getTimeInMillis());

                // Start AddUserMealActivity
                m_Activity.startActivityForResult(intent, Constants.REQUEST_ADD_FROM_DIARY);
                //m_Activity.overridePendingTransition(R.anim.none, R.anim.slide_in_from_bottom);

            }
        });
    }


    private void queryUserRecipes() {
        Utils.showProgressSpinner(m_ProgressSpinner);
        User user = (User) ParseUser.getCurrentUser();
        ParseRelation<ParseObject> pastRecipesRelation = user.getPastRecipes();
        if (pastRecipesRelation == null)
            return;

        ParseQuery query = pastRecipesRelation.getQuery();
        query.whereEqualTo("createdBy", user);
        query.orderByDescending("updatedAt");
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                m_RecipesList.clear();
                if (e == null) {
                    for (ParseObject object : objects) {
                        Recipe recipe = (Recipe) object;
                        m_RecipesList.add(recipe);
                    }
                    Log.d("****myfoods", Integer.toString(m_RecipesList.size()));
                    m_RecipeListAdapter.update(m_RecipesList);
                 /*   if (m_AddUserMealActivity.inSearchMode()) {
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