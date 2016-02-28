package com.example.kristenvondrak.dartmunch.Database;

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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.kristenvondrak.dartmunch.Main.Constants;
import com.example.kristenvondrak.dartmunch.Main.Utils;
import com.example.kristenvondrak.dartmunch.MyMeals.AddMealActivity;
import com.example.kristenvondrak.dartmunch.MyMeals.MealListAdapter;
import com.example.kristenvondrak.dartmunch.Parse.Recipe;
import com.example.kristenvondrak.dartmunch.Parse.UserMeal;
import com.example.kristenvondrak.dartmunch.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DatabaseFragment extends Fragment implements SearchView.OnQueryTextListener {

    // Constants
    public static final String EXTRA_NAME = "EXTRA_NAME";
    public static final String EXTRA_GROUP = "EXTRA_GROUP";
    public static final String EXTRA_NBD = "EXTRA_NBD";
    public static final String EXTRA_MEAL_TIME = "EXTRA_MEAL_TIME";
    public static final String EXTRA_DATE = "EXTRA_DATE";

    // Main
    private Activity m_Activity;
    private DatabaseFragment m_Fragment;
    private Calendar m_Calendar;

    // Views
    private ListView m_ListView;
    private ProgressBar m_ProgressSpinner;
    private TextView m_PromptTextView;
    private View m_EmptyMessage;

    // Data
    private List<DatabaseRecipe> m_DbRecipesList;
    private DatabaseListAdapter m_DbRecipesListAdapter;
    private RequestQueue m_RequestQueue;
    private int m_SelectedMealTime = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_database, container, false);

        m_Activity = getActivity();
        m_Fragment = this;
        m_Calendar = Calendar.getInstance();

        setHasOptionsMenu(true); // need for search

        initViews(v);
        initListeners();

        // TODO: get calendar, mealtime from activity

        m_DbRecipesList = new ArrayList<>();
        m_DbRecipesListAdapter = new DatabaseListAdapter(m_Activity, this, m_DbRecipesList);
        m_ListView.setAdapter(m_DbRecipesListAdapter);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private void initViews(View v) {
        m_ListView = (ListView) v.findViewById(R.id.db_listview);
        m_ProgressSpinner = (ProgressBar) v.findViewById(R.id.progress_spinner);
        m_PromptTextView = (TextView) v.findViewById(R.id.db_prompt_textview);
        m_EmptyMessage = v.findViewById(R.id.empty_food_list);

    }


    private void initListeners() {

    }


    public void onItemClick(DatabaseRecipe dbRecipe) {
        // Pass the UserMeal id and date
        Intent intent = new Intent(m_Activity, DbNutritionActivity.class);
        intent.putExtra(EXTRA_MEAL_TIME, m_SelectedMealTime);
        intent.putExtra(EXTRA_DATE, m_Calendar.getTimeInMillis());
        intent.putExtra(EXTRA_GROUP, dbRecipe.getGroup());
        intent.putExtra(EXTRA_NAME, dbRecipe.getName());
        intent.putExtra(EXTRA_NBD, dbRecipe.getNDBNo());

        m_Activity.startActivityForResult(intent, Constants.REQUEST_ADD_FROM_DIARY);
        //m_Activity.overridePendingTransition(R.anim.none, R.anim.slide_in_from_bottom);
    }




    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.d("***", "query submit");
        queryDbRecipes(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        //m_MealsListAdapter.getFilter().filter(query);
        return true;
    }


    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {

        inflater.inflate(R.menu.menu_recents, menu);

        // Enable search
        MenuItem search = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) search.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(true);

        // Start with the search view open
        search.expandActionView();

        // Need to clear search when done
        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.action_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Clear search when search view is closing

                // Clear the list
                //m_MealsListAdapter.updateData(m_MealsList);
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



    public RequestQueue getRequestQueue() {
        if (m_RequestQueue == null) {
            m_RequestQueue = Volley.newRequestQueue(m_Activity.getApplicationContext());
        }
        return m_RequestQueue;
    }

    private void queryDbRecipes(String searchText) {

        m_PromptTextView.setVisibility(View.GONE);
        m_EmptyMessage.setVisibility(View.GONE);
        Utils.showProgressSpinner(m_ProgressSpinner);

        searchText = convertTextToDbQuery(searchText);

        Map<String, String> params = new HashMap<>();
        params.put(Constants.Database.ParameterKeys.ApiKey, Constants.Database.ApiKey);
        params.put(Constants.Database.ParameterKeys.SearchText, searchText);
        params.put(Constants.Database.ParameterKeys.SortType, Constants.Database.ParameterValues.Relevance);
        params.put(Constants.Database.ParameterKeys.MaxResultCount, Constants.Database.ParameterValues.Max);
        params.put(Constants.Database.ParameterKeys.ResultOffset, Constants.Database.ParameterValues.Offset);
        params.put(Constants.Database.ParameterKeys.ResponseFormat, Constants.Database.ParameterValues.JSON);

        // TODO: figure out why the f*** volley doesnt take the params
        String url = Constants.Database.SearchBaseUrl + getQueryParams(searchText);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        // Clear current list
                        m_DbRecipesList.clear();

                        List<JSONObject> dbRecipesList = new ArrayList<>();
                        try {
                            if (response.has("list") && response.getJSONObject("list").has("item")) {

                                // Extract all food items in list
                                JSONArray itemsJSONArray = response.getJSONObject("list").getJSONArray("item");
                                for (int i = 0; i < itemsJSONArray.length(); i++) {
                                    dbRecipesList.add((JSONObject) itemsJSONArray.get(i));
                                }


                                // Create DbRecipe objects and add to list
                                for (JSONObject item : dbRecipesList) {
                                    if (!isValidJSON(item))
                                        continue;

                                    DatabaseRecipe dbRecipe;
                                    dbRecipe = new DatabaseRecipe(item.getString("group"),
                                            item.getString("name"), item.getString("ndbno"));
                                    m_DbRecipesList.add(dbRecipe);

                                }

                            } else {
                                // TODO: no items found
                                queryDbRecipesFail("No items found.");
                            }

                        } catch (JSONException e) {
                            queryDbRecipes("No items found.");
                            e.printStackTrace();

                        }

                        // Update
                        m_DbRecipesListAdapter.notifyDataSetChanged();
                        Utils.hideProgressSpinner(m_ProgressSpinner);

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        queryDbRecipesFail("Unknown error.");
                        Utils.hideProgressSpinner(m_ProgressSpinner);

                    }
                });
        getRequestQueue().add(jsObjRequest);
    }



    private void queryDbRecipesFail(String message) {
        m_EmptyMessage.setVisibility(View.VISIBLE);
        m_EmptyMessage.bringToFront();
    }

    private String convertTextToDbQuery(String text) {
        return  text.replace(" ", "+");
    }


    private String getQueryParams(String searchText) {
        String params = "?";
        params += Constants.Database.ParameterKeys.ResponseFormat + "=" + Constants.Database.ParameterValues.JSON + "&";
        params += Constants.Database.ParameterKeys.SearchText + "=" + searchText + "&";
        params += Constants.Database.ParameterKeys.MaxResultCount + "=" + Constants.Database.ParameterValues.Max +"&";
        params += Constants.Database.ParameterKeys.ResultOffset + "=" + Constants.Database.ParameterValues.Offset + "&";
        params += Constants.Database.ParameterKeys.ApiKey + "=" + Constants.Database.ApiKey;
        return params;
    }


    private boolean isValidJSON(JSONObject object) {
        return object.has("group") && object.has("name") && object.has("ndbno");

    }
}