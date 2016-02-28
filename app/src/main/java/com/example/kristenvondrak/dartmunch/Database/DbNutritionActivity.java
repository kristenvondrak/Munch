package com.example.kristenvondrak.dartmunch.Database;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.kristenvondrak.dartmunch.Main.Constants;
import com.example.kristenvondrak.dartmunch.Main.Utils;
import com.example.kristenvondrak.dartmunch.Menu.MenuFragment;
import com.example.kristenvondrak.dartmunch.Menu.Nutrients;
import com.example.kristenvondrak.dartmunch.Menu.NutritionActivity;
import com.example.kristenvondrak.dartmunch.Parse.ParseAPI;
import com.example.kristenvondrak.dartmunch.Parse.Recipe;
import com.example.kristenvondrak.dartmunch.R;
import com.parse.GetCallback;
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

public class DbNutritionActivity extends NutritionActivity {


    // Main
/*    protected Activity m_Activity;
    protected Calendar m_Calendar;

    // Views
    protected View m_RecipeNutrientsView;
    protected TextView m_RecipeName;
    protected NumberPicker m_NumberPickerWhole;
    protected NumberPicker m_NumberPickerFrac;
    protected LinearLayout m_UserMealSelector;

    // Nutrition data
    protected Recipe m_Recipe;
    protected int m_ServingsWhole = 1;
    protected int m_ServingsFraction = 0;

    // Meal Selector
    protected int m_SelectedUserMeal = 0;
    protected List<String> m_SelectorMealsList; */

    private RequestQueue m_RequestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DB_MODE = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition);

        m_Activity = this;

        // Set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true);             // show back button
        ab.setDisplayHomeAsUpEnabled(true);

        initViews();
        initListeners();

        // Retrieve data from calling activity
        Intent intent = getIntent();

        // Meal time
        m_SelectedUserMeal = intent.getIntExtra(DatabaseFragment.EXTRA_MEAL_TIME, 0);
        resetMealSelector();

        // Date
        long date = intent.getLongExtra(DatabaseFragment.EXTRA_DATE, Calendar.getInstance().getTimeInMillis());
        m_Calendar = Calendar.getInstance();
        m_Calendar.setTimeInMillis(date);

        // Database recipe
        String name = intent.getStringExtra(DatabaseFragment.EXTRA_NAME);
        String group = intent.getStringExtra(DatabaseFragment.EXTRA_GROUP);
        String ndb = intent.getStringExtra(DatabaseFragment.EXTRA_NBD);
        DatabaseRecipe dbRecipe = new DatabaseRecipe(group, name, ndb);

        // Query for nutrients
        queryDbRecipeNutrients(dbRecipe);

    }







    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nutrition, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_add) {
            float fraction = Constants.ServingsFracFloats[m_ServingsFraction];
            m_Recipe.saveInBackground();
            ParseAPI.addDiaryEntry( m_Calendar,
                                    ParseUser.getCurrentUser(),
                                    m_Recipe,
                                    m_ServingsWhole + fraction,
                                    m_SelectorMealsList.get(m_SelectedUserMeal));

            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
            return true;
        } else if (id == android.R.id.home) {
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void queryDbRecipeNutrients(final DatabaseRecipe dbRecipe) {

        String url = Constants.Database.ReportsBaseUrl + getQueryNutrientsParams(dbRecipe);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {


                        List<JSONObject> dbRecipesList = new ArrayList<>();
                        try {
                            if (response.has("report") && response.getJSONObject("report").has("food")
                                    && response.getJSONObject("report").getJSONObject("food").has("nutrients")){

                                // Create parse recipe
                                JSONArray nutrientsJSON = response.getJSONObject("report")
                                        .getJSONObject("food").getJSONArray("nutrients");

                                m_Recipe = new Recipe();
                                m_Recipe.setName(dbRecipe.getName());
                                m_Recipe.setCategory(dbRecipe.getGroup());
                                m_Recipe.setCreatedBy(ParseUser.getCurrentUser());
                                m_Recipe.setNutrients(extractNutrients(nutrientsJSON));
                                m_RecipeName.setText(m_Recipe.getName());
                                updateNutrients();


                            } else {
                                // Fail
                                m_Activity.finish();
                            }

                        } catch (JSONException e) {
                            // Fail
                            m_Activity.finish();
                        }

                        //Utils.hideProgressSpinner(m_ProgressSpinner);

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("ERROR: ", "failure getting nutrients");
                        //queryDbRecipesFail("Unknown error.");
                        //Utils.hideProgressSpinner(m_ProgressSpinner);

                    }
                });

        getRequestQueue().add(jsObjRequest);

    }

    private HashMap<String, HashMap<String, Object>> extractNutrients(JSONArray nutrientsJSON) {
        HashMap<String, Object> nutrients = new HashMap<>();

        // Add all the nutrient values
        for (int i = 0; i < nutrientsJSON.length(); i ++) {
            String name = null;
            JSONObject item = null;
            try {
                item = nutrientsJSON.getJSONObject(i);
                name = item.getString("name");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (name == null)
                continue;

            String value;
            switch (name) {

                case Constants.Database.ReportNutrients.Calories:
                    value = getNutrientValue(item, name);
                    if (value != null)
                        nutrients.put(Recipe.Fields.Calories, value);
                    break;

                case Constants.Database.ReportNutrients.TotalFat:
                    value = getNutrientValue(item, name);
                    if (value != null)
                        nutrients.put(Recipe.Fields.TotalFat, value);
                    break;

                case Constants.Database.ReportNutrients.SaturatedFat:
                    value = getNutrientValue(item, name);
                    if (value != null)
                        nutrients.put(Recipe.Fields.SaturatedFat, value);
                    break;

                case Constants.Database.ReportNutrients.Cholesterol:
                    value = getNutrientValue(item, name);
                    if (value != null)
                        nutrients.put(Recipe.Fields.Cholesterol, value);
                    break;

                case Constants.Database.ReportNutrients.Sodium:
                    value = getNutrientValue(item, name);
                    if (value != null)
                        nutrients.put(Recipe.Fields.Sodium, value);
                    break;

                case Constants.Database.ReportNutrients.TotalCarbs:
                    value = getNutrientValue(item, name);
                    if (value != null)
                        nutrients.put(Recipe.Fields.TotalCarbs, value);
                    break;

                case Constants.Database.ReportNutrients.Fiber:
                    value = getNutrientValue(item, name);
                    if (value != null)
                        nutrients.put(Recipe.Fields.Fiber, value);
                    break;

                case Constants.Database.ReportNutrients.Sugar:
                    value = getNutrientValue(item, name);
                    if (value != null)
                        nutrients.put(Recipe.Fields.Sugars, value);
                    break;

                case Constants.Database.ReportNutrients.Protein:
                    value = getNutrientValue(item, name);
                    if (value != null)
                        nutrients.put(Recipe.Fields.Protein, value);
                    break;

                default:
                    break;
            }
        }

        // Add the serving size and text
        try {
            JSONObject first = nutrientsJSON.getJSONObject(0);
            JSONArray measures = first.getJSONArray("measures");
            JSONObject item = measures.getJSONObject(0);
            int qty = item.getInt("qty");
            String label = item.getString("label");
            int grams = item.getInt("eqv");

            String servingsText = Integer.toString(qty) + " " + label;
            nutrients.put(Recipe.Fields.ServingSizeText, servingsText);
            nutrients.put(Recipe.Fields.ServingSizeGrams, grams);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        HashMap<String, HashMap<String, Object>> map = new HashMap<>();
        map.put(Recipe.Fields.NutrientResults, nutrients);
        return map;
    }

    private String getQueryNutrientsParams(DatabaseRecipe dbRecipe) {
        String params = "?";
        params += Constants.Database.ParameterKeys.ApiKey + "=" + Constants.Database.ApiKey + "&";
        params += Constants.Database.ParameterKeys.NDBNumber + "=" + dbRecipe.getNDBNo() + "&";
        params += Constants.Database.ParameterKeys.ReportType + "=" + Constants.Database.ParameterValues.ReportBasic + "&";
        params += Constants.Database.ParameterKeys.ResponseFormat + "=" + Constants.Database.ParameterValues.JSON;
        return params;
    }

    private String getNutrientValue(JSONObject item, String name) {
        try {
            return item.getString("value");
        } catch (JSONException e) {
            return null;
        }
    }

    public RequestQueue getRequestQueue() {
        if (m_RequestQueue == null) {
            m_RequestQueue = Volley.newRequestQueue(m_Activity.getApplicationContext());
        }
        return m_RequestQueue;
    }


}
