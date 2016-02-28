package com.example.kristenvondrak.dartmunch.Menu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.ContextThemeWrapper;
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
import android.widget.Toast;

import com.example.kristenvondrak.dartmunch.Main.Constants;
import com.example.kristenvondrak.dartmunch.Parse.ParseAPI;
import com.example.kristenvondrak.dartmunch.Parse.Recipe;
import com.example.kristenvondrak.dartmunch.R;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NutritionActivity extends AppCompatActivity {


    // Main
    protected Activity m_Activity;
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
    protected List<String> m_SelectorMealsList;


    protected boolean EDIT_MODE = false, DB_MODE = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition);

        // Editing an existing diary entry
        if (EDIT_MODE || DB_MODE) {return;}


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
        m_SelectedUserMeal = intent.getIntExtra(MenuFragment.EXTRA_MEAL_TIME, 0);
        resetMealSelector();

        // Date
        long date = intent.getLongExtra(MenuFragment.EXTRA_DATE, Calendar.getInstance().getTimeInMillis());
        m_Calendar = Calendar.getInstance();
        m_Calendar.setTimeInMillis(date);

        // Retrieve the recipe object from Parse
        String recipeId = intent.getStringExtra(MenuFragment.EXTRA_RECIPE_ID);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Recipe");
        query.getInBackground(recipeId, new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    m_Recipe = (Recipe) object;
                    m_RecipeName.setText(m_Recipe.getName());
                    updateNutrients();
                } else {
                    // something went wrong
                    m_Activity.finish();
                }
            }
        });
    }

    protected void initViews() {
        findViewById(R.id.dummy_focus).requestFocus();
        m_RecipeNutrientsView = findViewById(R.id.nutrients);
        m_RecipeName = (TextView) findViewById(R.id.name);
        m_NumberPickerWhole = (NumberPicker) findViewById(R.id.servings_picker_number);
        m_NumberPickerFrac = (NumberPicker) findViewById(R.id.servings_picker_fraction);
        m_UserMealSelector = (LinearLayout) findViewById(R.id.usermeal_selector);
    }


    protected void initListeners() {

        // Selector wheel with values -, 1, 2 ... 99
        final String[] numbers = new String[100];
        numbers[0] = "-";
        for (int i = 1; i < numbers.length; i++) {
            numbers[i] = String.valueOf(i);
        }
        m_NumberPickerWhole.setMinValue(0);
        m_NumberPickerWhole.setMaxValue(numbers.length - 1);
        m_NumberPickerWhole.setDisplayedValues(numbers);
        m_NumberPickerWhole.setWrapSelectorWheel(false);
        m_NumberPickerWhole.setValue(m_ServingsWhole);
        m_NumberPickerWhole.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                m_ServingsWhole = newVal;
                updateNutrients();
            }
        });

        // Selector wheel with values -, 1/8, 1/4, 1/3, 1/2, 2/3, 3/4
        m_NumberPickerFrac.setMinValue(0);
        m_NumberPickerFrac.setMaxValue(Constants.ServingsFracDisplay.length - 1);
        m_NumberPickerFrac.setDisplayedValues(Constants.ServingsFracDisplay);
        m_NumberPickerFrac.setWrapSelectorWheel(false);
        m_NumberPickerFrac.setValue(m_ServingsFraction);
        m_NumberPickerFrac.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                m_ServingsFraction = newVal;
                updateNutrients();
            }
        });
        resetServingsSelector();

        // Selector Dialog with values Breakfast, Lunch, Dinner, Snacks
        m_SelectorMealsList = new ArrayList<>();
        for (Constants.UserMeals m : Constants.UserMeals.values()) {
            m_SelectorMealsList.add(m.name());
        }

        m_UserMealSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMealSelectorDialog();
            }
        });

    }

    protected void updateNutrients() {

        double num_servings = Constants.ServingsFracFloats.get(m_ServingsFraction) + m_ServingsWhole;

        setTextViewValue(m_RecipeNutrientsView, R.id.calories, getNewValue(m_Recipe.getCalories(), num_servings));
        setTextViewValue(m_RecipeNutrientsView, R.id.fat_calories, getNewValue(m_Recipe.getFatCalories(), num_servings));
        setTextViewValue(m_RecipeNutrientsView, R.id.total_fat, getNewValue(m_Recipe.getTotalFat(), num_servings));
        setTextViewValue(m_RecipeNutrientsView, R.id.saturated_fat, getNewValue(m_Recipe.getSaturatedFat(), num_servings));
        setTextViewValue(m_RecipeNutrientsView, R.id.cholesterol, getNewValue(m_Recipe.getCholestrol(), num_servings));
        setTextViewValue(m_RecipeNutrientsView, R.id.sodium, getNewValue(m_Recipe.getSodium(), num_servings));
        setTextViewValue(m_RecipeNutrientsView, R.id.carbs, getNewValue(m_Recipe.getTotalCarbs(), num_servings));
        setTextViewValue(m_RecipeNutrientsView, R.id.fiber, getNewValue(m_Recipe.getFiber(), num_servings));
        setTextViewValue(m_RecipeNutrientsView, R.id.sugars, getNewValue(m_Recipe.getSugars(), num_servings));
        setTextViewValue(m_RecipeNutrientsView, R.id.protein, getNewValue(m_Recipe.getProtein(), num_servings));
        setTextViewValue(m_RecipeNutrientsView, R.id.serving_size, m_Recipe.getServingSize());
        setTextViewValue(m_RecipeNutrientsView, R.id.serving_text, m_Recipe.getServingText());
    }




    protected void resetMealSelector() {
        String text = m_SelectorMealsList.get(m_SelectedUserMeal);
        Drawable img = getResources().getDrawable(Constants.MealTimeIcons[m_SelectedUserMeal]);

        ((TextView) m_UserMealSelector.findViewById(R.id.usermeal_selector_text)).setText(text);
        ((ImageView)m_UserMealSelector.findViewById(R.id.usermeal_selector_icon)).setImageDrawable(img);
    }

    private void resetServingsSelector() {
        m_NumberPickerWhole.setValue(m_ServingsWhole);
        m_NumberPickerFrac.setValue(m_ServingsFraction);

    }

    private void showMealSelectorDialog() {

      /*
  CharSequence[] choices = m_SelectorMealsList.toArray(new CharSequence[m_SelectorMealsList.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(m_Activity, R.style.BasicAlertDialogStyle));
        builder.setTitle("Add to Meal")
                .setSingleChoiceItems(choices, m_SelectorMealsList.indexOf(m_SelectedUserMeal), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_SelectedUserMeal = which;
                        resetMealSelector();
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();

        */
        LayoutInflater inflater = m_Activity.getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(m_Activity);

        View v = inflater.inflate(R.layout.meal_selector_dialog, null);
        RadioGroup rg = (RadioGroup) v.findViewById(R.id.radio_group);

        for (int i = 0; i < m_SelectorMealsList.size(); i++) {
            RadioButton button = (RadioButton) inflater.inflate(R.layout.meal_selector_radiobutton, null);
            button.setText(m_SelectorMealsList.get(i));
            button.setId(i);
            rg.addView(button);
            if (m_SelectedUserMeal == i)
                button.setChecked(true);
        }
        builder.setView(v);
        final AlertDialog dialog = builder.create();

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                m_SelectedUserMeal = checkedId;
                resetMealSelector();
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    private void setTextViewValue(View v, int id, String text) {
        if (text == null)
            return;

        ((TextView)v.findViewById(id)).setText(text);
    }

    private String getNewValue(String value, double multiplier) {
        int v = (int)(Nutrients.convertToDouble(value) * multiplier);
        if (v < 0 || value == null)
            return "-";

        String u = Nutrients.getUnits(value);


        return Integer.toString(v) + " " +  u;
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
            // Add recipe to diary and return to previous activity
            float fraction = Constants.ServingsFracFloats.get(m_ServingsFraction);
            ParseAPI.addDiaryEntry(m_Calendar,
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


}
