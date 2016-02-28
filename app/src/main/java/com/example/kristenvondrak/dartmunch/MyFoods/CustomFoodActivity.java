package com.example.kristenvondrak.dartmunch.MyFoods;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.example.kristenvondrak.dartmunch.Main.Constants;
import com.example.kristenvondrak.dartmunch.Menu.MenuFragment;
import com.example.kristenvondrak.dartmunch.Parse.ParseAPI;
import com.example.kristenvondrak.dartmunch.Parse.Recipe;
import com.example.kristenvondrak.dartmunch.R;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Calendar;
import java.util.HashMap;

public class CustomFoodActivity extends AppCompatActivity {

    // Views
    private EditText m_NameEditText;
    private EditText m_FatCalsEditText;
    private EditText m_CaloriesEditText;
    private EditText m_FatEditText;
    private EditText m_SaturatedFatEditText;
    private EditText m_CholesterolEditText;
    private EditText m_SodiumEditText;
    private EditText m_CarbsEditText;
    private EditText m_FiberEditText;
    private EditText m_SugarsEditText;
    private EditText m_ProteinEditText;

    private int m_SelectedUserMeal;
    private Calendar m_Calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_food);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Show back button
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        // Retrieve data from calling activity
        Intent intent = getIntent();

        // Meal time
        m_SelectedUserMeal = intent.getIntExtra(MyFoodsFragment.EXTRA_USERMEAL_INDEX, 0);

        // Date
        long date = intent.getLongExtra(MyFoodsFragment.EXTRA_DATE, Calendar.getInstance().getTimeInMillis());
        m_Calendar = Calendar.getInstance();
        m_Calendar.setTimeInMillis(date);

        initViews();
    }


    private void initViews() {
        m_NameEditText = (EditText) findViewById(R.id.input_name);
        m_CaloriesEditText = (EditText) findViewById(R.id.input_calories);
        m_FatCalsEditText = (EditText) findViewById(R.id.input_fat_calories);
        m_FatEditText = (EditText) findViewById(R.id.input_fat);
        m_SaturatedFatEditText = (EditText) findViewById(R.id.input_saturated_fat);
        m_CholesterolEditText = (EditText) findViewById(R.id.input_cholesterol);
        m_SodiumEditText = (EditText) findViewById(R.id.input_sodium);
        m_CarbsEditText = (EditText) findViewById(R.id.input_carbs);
        m_FiberEditText = (EditText) findViewById(R.id.input_fiber);
        m_SugarsEditText = (EditText) findViewById(R.id.input_sugars);
        m_ProteinEditText = (EditText) findViewById(R.id.input_protein);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_custom_food, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_save) {
            if (validate())
                saveRecipe();
            return true;

        } else if (id == android.R.id.home) {
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean validate() {
        boolean valid = true;

        String name = m_NameEditText.getText().toString();
        String cals = m_CaloriesEditText.getText().toString();

        // Check that required fields are filled in
        if (name.isEmpty())  {
            m_NameEditText.setError("This field is required");
            valid = false;
        } else {
            m_NameEditText.setError(null);
        }

        if (cals.isEmpty()) {
            m_CaloriesEditText.setError("This field is required");
            valid = false;
        } else {
            m_CaloriesEditText.setError(null);
        }

       return valid;
    }


    private void saveRecipe() {
        // Create custom recipe from filled in fields and save to Parse
        final Recipe recipe = createCustomRecipe();
        recipe.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    String meal = Constants.UserMeals.values()[m_SelectedUserMeal].name();
                    ParseAPI.addDiaryEntry(m_Calendar, ParseUser.getCurrentUser(), recipe, 1, meal);
                } else {
                    Log.d("Error", e.getMessage());
                }

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }


    private Recipe createCustomRecipe() {

        // Nutrients map
        HashMap<String, HashMap<String, Object>> value = new HashMap<>();

        // Get filled in fields
        HashMap<String, Object> nutrients = new HashMap<>();
        nutrients.put("calories", getText(m_CaloriesEditText));

        if (!isEmpty(m_FatCalsEditText)) {
            nutrients.put("calfat", getText(m_FatCalsEditText));
        }

        if (!isEmpty(m_FatEditText)) {
            nutrients.put("fat", getText(m_FatEditText) + "g");
        }

        if (!isEmpty(m_SaturatedFatEditText)) {
            nutrients.put("sfa", getText(m_SaturatedFatEditText) + "g");
        }

        if (!isEmpty(m_CholesterolEditText)) {
            nutrients.put("cholestrol", getText(m_CholesterolEditText) + "mg");
        }

        if (!isEmpty(m_SodiumEditText)) {
            nutrients.put("sodium", getText(m_SodiumEditText) + "mg");
        }

        if (!isEmpty(m_CarbsEditText)) {
            nutrients.put("carbs", getText(m_CarbsEditText) + "g");
        }

        if (!isEmpty(m_FiberEditText)) {
            nutrients.put("fiberdtry", getText(m_FiberEditText) + "g");
        }

        if (!isEmpty(m_SugarsEditText)) {
            nutrients.put("sugars", getText(m_SugarsEditText) + "g");
        }

        if (!isEmpty(m_ProteinEditText)) {
            nutrients.put("protein", getText(m_ProteinEditText) + "g");
        }

        value.put("result", nutrients);

        // Create new recipe
        Recipe recipe = new Recipe();
        recipe.setName(getText(m_NameEditText));
        recipe.setNutrients(value);
        recipe.setCreatedBy(ParseAPI.getCurrentParseUser());
        return recipe;
    }


    private boolean isEmpty(EditText editText) {
        return editText.getText() == null || editText.getText().toString().trim().equals("");
    }

    private String getText(EditText editText) {
        return editText.getText().toString();
    }

}
