package com.example.kristenvondrak.dartmunch.MyMeals;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.example.kristenvondrak.dartmunch.Main.Constants;
import com.example.kristenvondrak.dartmunch.Main.Utils;
import com.example.kristenvondrak.dartmunch.Menu.MenuFragment;
import com.example.kristenvondrak.dartmunch.Menu.Nutrients;
import com.example.kristenvondrak.dartmunch.Parse.DiaryEntry;
import com.example.kristenvondrak.dartmunch.Parse.ParseAPI;
import com.example.kristenvondrak.dartmunch.Parse.Recipe;
import com.example.kristenvondrak.dartmunch.Parse.User;
import com.example.kristenvondrak.dartmunch.Parse.UserMeal;
import com.example.kristenvondrak.dartmunch.R;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddMealActivity extends AppCompatActivity {

    // Main
    private AddMealActivity m_Activity;
    private Calendar m_Calendar;

    // Views
    private ListView m_MealEntriesListView;
    private TextView m_TotalCals;
    private LinearLayout m_UserMealSelector;
    private ProgressBar m_ProgressSpinner;

    // Data
    private int m_SelectedUserMeal;
    private List<String> m_SelectorMealsList;
    private UserMeal m_UserMeal;
    private MealEntriesListAdapter m_MealEntriesListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meal);

        m_Activity = this;
        m_Calendar = Calendar.getInstance();

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

        // Date
        long date = intent.getLongExtra(MyMealsFragment.EXTRA_DATE, m_Calendar.getTimeInMillis());
        m_Calendar.setTimeInMillis(date);

        // Retrieve the UserMeal object from Parse
        String userMealId = intent.getStringExtra(MyMealsFragment.EXTRA_USERMEAL_ID);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserMeal");
        query.include("entries.recipe");
        Utils.showProgressSpinner(m_ProgressSpinner);
        query.getInBackground(userMealId, new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    m_UserMeal = (UserMeal) object;
                    m_SelectedUserMeal = Utils.getUserMealIndex(m_UserMeal.getTitle());
                    resetMealSelector();
                    m_MealEntriesListAdapter = new MealEntriesListAdapter(m_Activity, m_UserMeal.getDiaryEntries());
                    m_MealEntriesListView.setAdapter(m_MealEntriesListAdapter);
                } else {
                    // something went wrong
                    Log.d("ERROR: ", e.getMessage());
                    m_Activity.finish();
                }
                Utils.hideProgressSpinner(m_ProgressSpinner);
            }
        });
    }


    protected void initViews() {
        m_MealEntriesListView = (ListView) findViewById(R.id.meal_entries_list);
        m_UserMealSelector = (LinearLayout) findViewById(R.id.usermeal_selector);
        m_TotalCals = (TextView) findViewById(R.id.meal_total_cals);
        m_ProgressSpinner = (ProgressBar) findViewById(R.id.progress_spinner);
    }


    protected void initListeners() {
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

    public void resetTotalCals(List<DiaryEntry> list) {
        int totalCals = 0;
        for (DiaryEntry entry :list) {
            totalCals += entry.getTotalCalories();
        }
        m_TotalCals.setText("calories: " + Integer.toString(totalCals));
    }

    private void resetMealSelector() {
        String text = m_SelectorMealsList.get(m_SelectedUserMeal);
        Drawable img = getResources().getDrawable(Constants.MealTimeIcons[m_SelectedUserMeal]);

        ((TextView) m_UserMealSelector.findViewById(R.id.usermeal_selector_text)).setText(text);
        ((ImageView)m_UserMealSelector.findViewById(R.id.usermeal_selector_icon)).setImageDrawable(img);
    }


    private void showMealSelectorDialog() {

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_meal, menu);
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
            List<DiaryEntry> list = m_MealEntriesListAdapter.getSelectedEntries();
            ParseAPI.addDiaryEntries(m_Calendar, ParseUser.getCurrentUser(), list,
                    Constants.UserMeals.values()[m_SelectedUserMeal].name());

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
