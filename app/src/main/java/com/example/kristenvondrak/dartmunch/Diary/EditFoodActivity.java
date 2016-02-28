package com.example.kristenvondrak.dartmunch.Diary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kristenvondrak.dartmunch.Main.Constants;
import com.example.kristenvondrak.dartmunch.Main.Utils;
import com.example.kristenvondrak.dartmunch.Menu.MenuFragment;
import com.example.kristenvondrak.dartmunch.Menu.Nutrients;
import com.example.kristenvondrak.dartmunch.Menu.NutritionActivity;
import com.example.kristenvondrak.dartmunch.Parse.DiaryEntry;
import com.example.kristenvondrak.dartmunch.Parse.ParseAPI;
import com.example.kristenvondrak.dartmunch.Parse.Recipe;
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

public class EditFoodActivity extends NutritionActivity {

    public static final String EXTRA_TOAST_MESSAGE = "EXTRA_TOAST_MESSAGE";

    private DiaryEntry m_DiaryEntry;
    private UserMeal m_UserMeal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EDIT_MODE = true;

        super.onCreate(savedInstanceState);

        m_Activity = this;
        m_Calendar = Calendar.getInstance();

        // Set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Show back button
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        initViews();
        initListeners();

        Intent intent = getIntent();
        m_Calendar.setTimeInMillis(intent.getLongExtra(DiaryFragment.EXTRA_DATE, m_Calendar.getTimeInMillis()));
        String diaryEntryId = intent.getStringExtra(DiaryFragment.EXTRA_DIARY_ENTRY_ID);
        String userMealId = intent.getStringExtra(DiaryFragment.EXTRA_USER_MEAL_ID);

        // Get the diary entry
        ParseQuery<ParseObject> query = ParseQuery.getQuery("DiaryEntry");
        query.include("recipe");
        query.getInBackground(diaryEntryId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, com.parse.ParseException e) {
                if (e == null) {
                    // Get DiaryEntry and Recipe objects
                    m_DiaryEntry = (DiaryEntry) object;
                    m_Recipe = m_DiaryEntry.getRecipe();
                    m_RecipeName.setText(m_Recipe.getName());

                    // Set servings selectors
                    float servings = m_DiaryEntry.getServingsMultiplier();
                    m_ServingsWhole = Utils.getServingsWholeIndex(servings);
                    m_ServingsFraction = Utils.getServingsFracIndex(servings);
                    resetServingsSelector();
                    updateNutrients();
                } else {
                    Log.d("Error", e.getMessage());
                }
            }
        });

        // Get the user meal -- for deletion purposes
        ParseQuery<ParseObject> userMealQuery = ParseQuery.getQuery("UserMeal");
        userMealQuery.include("entries");
        userMealQuery.getInBackground(userMealId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, com.parse.ParseException e) {
                if (e == null) {
                    m_UserMeal = (UserMeal) object;
                } else {
                    Log.d("Error", e.getMessage());
                }
            }
        });
    }

    @Override
    protected void initViews() {
        super.initViews();

        // Cannot change meal
        m_UserMealSelector.setVisibility(View.GONE);

    }

    @Override
    protected void initListeners() {
        super.initListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_food, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_save) {
            saveDiaryEntry();
            return true;

        } else if (id == R.id.action_delete) {
            showDeleteDialog();
            return true;

        } else if (id == android.R.id.home) {
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /*
     * Save the new servings value in Parse
     */
    public void saveDiaryEntry() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("DiaryEntry");
        query.getInBackground(m_DiaryEntry.getObjectId(), new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, com.parse.ParseException e) {
                if (e == null) {
                    //TODO: for non-DDS items --> update all
                    DiaryEntry entry = (DiaryEntry) object;
                    float servings = m_ServingsWhole + Constants.ServingsFracFloats[m_ServingsFraction];
                    entry.put("servingsMultiplier", servings);
                    entry.saveInBackground();
                }
                Intent intent = new Intent();
                intent.putExtra(EXTRA_TOAST_MESSAGE, getResources().getString(R.string.toast_food_saved));
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }


    public void showDeleteDialog() {

        LayoutInflater inflater = m_Activity.getLayoutInflater();
        AlertDialog.Builder builder =
                new AlertDialog.Builder(new ContextThemeWrapper(m_Activity, R.style.DeleteDialogStyle));
        View v = inflater.inflate(R.layout.dialog_delete_food, null);

        builder .setView(v);
        final AlertDialog dialog = builder.create();

        TextView cancelBtn = (TextView) v.findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        TextView deleteBtn = (TextView) v.findViewById(R.id.delete_btn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                deleteDiaryEntry();
            }
        });

        dialog.show();
    }

    /*
     * Delete the diary entry from Parse
     */
    public void deleteDiaryEntry() {
        // Remove pointer to entry from user meal in Parse
        if (m_UserMeal.getDiaryEntries().size() == 1)
            m_UserMeal.deleteInBackground();
        else {
            m_UserMeal.removeDiaryEntry(m_DiaryEntry);
            m_UserMeal.saveInBackground();
        }

        // Remove diary entry
        m_DiaryEntry.deleteInBackground();

        Intent intent = new Intent();
        intent.putExtra(EXTRA_TOAST_MESSAGE, getResources().getString(R.string.toast_food_deleted));
        setResult(RESULT_OK, intent);
        finish();

    }

}
