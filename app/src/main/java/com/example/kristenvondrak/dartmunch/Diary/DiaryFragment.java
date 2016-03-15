package com.example.kristenvondrak.dartmunch.Diary;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.kristenvondrak.dartmunch.Main.Constants;
import com.example.kristenvondrak.dartmunch.Main.MainTabFragment;
import com.example.kristenvondrak.dartmunch.Main.OnDateChangedListener;
import com.example.kristenvondrak.dartmunch.Main.Utils;
import com.example.kristenvondrak.dartmunch.Parse.DiaryEntry;
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


public class DiaryFragment extends Fragment implements MainTabFragment {

    public static final String TAG = "DiaryFragment";

    public static final String EXTRA_DIARY_ENTRY_ID = "EXTRA_DIARY_ENTRY_ID";
    public static final String EXTRA_USER_MEAL_ID = "EXTRA_USER_MEAL_ID";
    public static final String EXTRA_USERMEAL_INDEX = "EXTRA_USERMEAL_INDEX";
    public static final String EXTRA_DATE = "EXTRA_DATE";

    private AppCompatActivity m_Activity;

    // Main
    private DiaryListAdapter m_DiaryListAdapter;
    private ListView m_DiaryListView;
    private ProgressBar m_ProgressSpinner;
    private FloatingActionButton m_FloatingAddBtn;

    // Date
    private Calendar m_Calendar = Calendar.getInstance();
    private DatePickerDialog.OnDateSetListener m_DatePickerListener;

    // Summary calorie
    private static final int DEFAULT_GOAL_CALS = 2000;
    private TextView m_BudgetTextView;
    private TextView m_FoodTextView;
    private TextView m_RemainingTextView;
    private TextView m_RemainingHeaderTextView;
    private int m_FoodCals;
    private int m_BudgetCals;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_diary, container, false);

        m_Activity = (AppCompatActivity) getActivity();

        setHasOptionsMenu(true);
        initViews(v);
        initListeners();

        // TODO: store in parse
        m_FoodCals = 0;
        m_BudgetCals = DEFAULT_GOAL_CALS;

        // Create list of meals and set the adapter
        m_DiaryListAdapter = new DiaryListAdapter(m_Activity);
        m_DiaryListView.setAdapter(m_DiaryListAdapter);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        queryUserMeals();
    }


    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_tab_diary, menu);

        final android.support.v7.app.ActionBar ab = m_Activity.getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true); // show the default home button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.ic_dummy_menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();


        // Allow user to pick date from date picker dialog
        if (id == R.id.action_calendar) {
          /*  new DatePickerDialog(m_Activity, R.style.BasicAlertDialog,
                    m_DatePickerListener, m_Calendar.get(Calendar.YEAR),
                    m_Calendar.get(Calendar.MONTH), m_Calendar.get(Calendar.DAY_OF_MONTH)).show(); */
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void initViews(View v) {
        m_DiaryListView = (ListView) v.findViewById(R.id.diary_list_view);
        m_RemainingHeaderTextView = (TextView) v.findViewById(R.id.remaining_header);
        m_BudgetTextView = (TextView) v.findViewById(R.id.budget_cals);
        m_FoodTextView = (TextView) v.findViewById(R.id.food_cals);
        m_RemainingTextView = (TextView) v.findViewById(R.id.remaining_cals);
        m_ProgressSpinner = (ProgressBar) v.findViewById(R.id.progress_spinner);
        m_FloatingAddBtn = (FloatingActionButton) v.findViewById(R.id.fab);

    }


    private void initListeners() {

        m_FloatingAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create dialog with meal time choices
                // start AddFoodActivity
                // Pass the meal time and date
               /* Intent intent = new Intent(m_Activity, AddFoodActivity.class);
                intent.putExtra(DiaryFragment.EXTRA_MEALTIME, title);
                intent.putExtra(DiaryFragment.EXTRA_DATE, m_Calendar.getTimeInMillis());

                // Start AddUserMealActivity
                m_Activity.startActivityForResult(intent, Constants.REQUEST_ADD_FROM_DIARY);
                */
            }
        });

        m_DatePickerListener = new DatePickerDialog.OnDateSetListener() {

            // when dialog box is closed, below method will be called.
            public void onDateSet(DatePicker view, int selectedYear,
                                  int selectedMonth, int selectedDay) {
                m_Calendar.set(Calendar.YEAR, selectedYear);
                m_Calendar.set(Calendar.MONTH, selectedMonth);
                m_Calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                queryUserMeals();
            }
        };
    }


    public void update(Calendar calendar) {
        m_Calendar.setTimeInMillis(calendar.getTimeInMillis());
        queryUserMeals();
    }


    private void updateCalorieSummary(List<UserMeal> list) {
        // Recalculate the total calories consumed
        m_FoodCals = 0;
        for (UserMeal m : list) {
            for (DiaryEntry e : m.getDiaryEntries()) {
                m_FoodCals += e.getTotalCalories();
            }
        }

        m_BudgetTextView.setText(Integer.toString(m_BudgetCals));
        m_FoodTextView.setText(Integer.toString(m_FoodCals));

        int total = m_BudgetCals - m_FoodCals;
        int color = total >= 0 ? getResources().getColor(R.color.calsUnder) : getResources().getColor(R.color.calsOver);
        m_RemainingTextView.setText(Integer.toString(total));
        m_RemainingTextView.setTextColor(color);

        String header = total >= 0 ? "Under" : "Over";
        m_RemainingHeaderTextView.setText(header);


    }

    private void queryUserMeals() {
        Utils.showProgressSpinner(m_ProgressSpinner);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserMeal");
        query.whereGreaterThan("date", Utils.getDateBefore(m_Calendar));
        query.whereLessThan("date", Utils.getDateAfter(m_Calendar));
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.include("entries.recipe");
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.findInBackground(new FindCallback<ParseObject>() {

            public void done(List<ParseObject> meals, ParseException e) {
                List<UserMeal> userMealList = new ArrayList<UserMeal>();
                if (e == null) {
                    for (ParseObject object : meals) {
                        userMealList.add((UserMeal) object);
                    }
                } else {
                    Log.d(TAG, "Error getting user meals: " + e.getMessage());
                }
                m_DiaryListAdapter.updateData(userMealList, m_Calendar);
                updateCalorieSummary(userMealList);
                Utils.hideProgressSpinner(m_ProgressSpinner);
            }
        });
    }



    private void showMealSelectorDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(m_Activity, R.style.BasicAlertDialog);

        String[] list = new String[Constants.UserMeals.values().length];
        for (int i = 0; i < list.length; i++) {
            list[i] = Constants.UserMeals.values()[i].name();
        }
        builder .setTitle(R.string.dialog_meal_selector_title)
                .setItems(list, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Pass the meal time and date
                        Intent intent = new Intent(m_Activity, AddFoodActivity.class);
                        intent.putExtra(DiaryFragment.EXTRA_USERMEAL_INDEX, which);
                        intent.putExtra(DiaryFragment.EXTRA_DATE, m_Calendar.getTimeInMillis());
                        m_Activity.startActivityForResult(intent, Constants.REQUEST_ADD_FROM_DIARY);
                    }
                });

        builder.create().show();
    }
}

