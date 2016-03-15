package com.example.kristenvondrak.dartmunch.Preferences;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.example.kristenvondrak.dartmunch.Diary.AddFoodActivity;
import com.example.kristenvondrak.dartmunch.Diary.DiaryListAdapter;
import com.example.kristenvondrak.dartmunch.Main.Constants;
import com.example.kristenvondrak.dartmunch.Main.LoginActivity;
import com.example.kristenvondrak.dartmunch.Main.MainTabFragment;
import com.example.kristenvondrak.dartmunch.Main.Utils;
import com.example.kristenvondrak.dartmunch.Parse.DiaryEntry;
import com.example.kristenvondrak.dartmunch.Parse.ParseAPI;
import com.example.kristenvondrak.dartmunch.Parse.Recipe;
import com.example.kristenvondrak.dartmunch.Parse.User;
import com.example.kristenvondrak.dartmunch.Parse.UserMeal;
import com.example.kristenvondrak.dartmunch.R;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class PrefsFragment extends Fragment implements MainTabFragment {


    public static final String TAG = "PrefsFragment";

    private AppCompatActivity m_Activity;
    private User m_ParseUser;

    // User input
    private View m_GenderRow;
    private View m_HeightRow;
    private View m_AgeRow;
    private View m_WeightRow;
    private View m_ActivityRow;
    private TextView m_GenderText;
    private TextView m_HeightText;
    private TextView m_AgeText;
    private TextView m_WeightText;
    private TextView m_ActivityText;

    // Calculated values
    private TextView m_EstimatedCalsTextView;
    private ViewFlipper m_CalsViewFlipper;
    private EditText m_BurnedCalsEditText;

    // Button
    private TextView m_GoalButton;


    // Summary values
    private TextView m_CalsTotalTextView;
    private TextView m_CalsEqnTextView;
    private TextView m_CalsDiffTextView;
    private TextView m_CalsGoalTextView;


    private int m_GoalCals = 2000;
    private int m_Gender = Constants.MALE;
    private int m_Height = 64;
    private int m_Age = 20;
    private int m_Weight = 135;
    private float m_Goal = 0;
    private int m_ActivityLevelIndex = 0;

    private int gender;
    private int age;
    private int height_feet;
    private int height_inches;
    private float goal;
    private int activityLevelIndex;

    private boolean customGoal = false;

    private String m_Username = "";


    // Main

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_prefs, container, false);

        m_Activity = (AppCompatActivity) getActivity();
        setHasOptionsMenu(true);
        initViews(v);
        initListeners();

        m_ParseUser = (User) ParseUser.getCurrentUser();
        if (m_ParseUser != null) {
            m_Username = m_ParseUser.getUsername();
            m_Gender = m_ParseUser.getGender();
            m_Age = m_ParseUser.getAge();
            m_Height = m_ParseUser.getHeight();
            m_Weight = m_ParseUser.getWeight();
            m_Goal = m_ParseUser.getGoal();
            m_ActivityLevelIndex = m_ParseUser.getActivityLevel();
            m_GoalCals = m_ParseUser.getGoalCalories();
            updateValues();
        } else {
            Log.d(TAG, "parse user null");
        }



        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_prefs, menu);

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
        if (id == R.id.action_logout) {
            ParseAPI.logOutParseUser();
            Intent intent = new Intent(m_Activity, LoginActivity.class);
            m_Activity.startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void initViews(View v) {
        m_GenderRow = v.findViewById(R.id.choose_gender);
        m_AgeRow = v.findViewById(R.id.choose_age);
        m_WeightRow = v.findViewById(R.id.choose_weight);
        m_HeightRow = v.findViewById(R.id.choose_height);
        m_ActivityRow = v.findViewById(R.id.choose_activity_level);

        m_EstimatedCalsTextView = (TextView) v.findViewById(R.id.cals_estimated_text);
        m_CalsViewFlipper = (ViewFlipper) v.findViewById(R.id.cals_view_flipper);
        m_BurnedCalsEditText = (EditText) v.findViewById(R.id.cals_burned_edit_text);

        m_GoalButton = (TextView) v.findViewById(R.id.goal_button);

        m_CalsTotalTextView = (TextView) v.findViewById(R.id.cal_summary_total);
        m_CalsEqnTextView = (TextView) v.findViewById(R.id.cal_summary_eqn);
        m_CalsDiffTextView = (TextView) v.findViewById(R.id.cal_summary_diff);
        m_CalsGoalTextView = (TextView) v.findViewById(R.id.cal_summary_goal);

        m_GenderText = (TextView) v.findViewById(R.id.gender_value);
        m_AgeText = (TextView) v.findViewById(R.id.age_value);
        m_WeightText = (TextView) v.findViewById(R.id.weight_value);
        m_HeightText = (TextView) v.findViewById(R.id.height_value);
        m_ActivityText = (TextView) v.findViewById(R.id.activity_value);
    }


    private void updateValues() {
        if (m_Gender == User.UNKNOWN) {
            m_GenderText.setText("Choose Gender");
        } else {
            m_GenderText.setText(m_Gender == Constants.MALE ? "Male" : "Female");
        }


        m_AgeText.setText(m_Age == User.UNKNOWN ? "Choose Age" : Integer.toString(m_Age));
        m_WeightText.setText(m_Weight == User.UNKNOWN ? "Choose Weight" : Integer.toString(m_Weight));
        m_HeightText.setText(m_Height == User.UNKNOWN ? "Choose Height" :
                Integer.toString(getHeightFt()) + "' " + Integer.toString(getHeightIn()) + "''");

        m_ActivityText.setText(m_ActivityLevelIndex == User.UNKNOWN ? "Choose Activity Level" :
                Constants.ActivityLevels.get(m_ActivityLevelIndex));

        for (int i  = 0; i < Constants.PoundsPerWeek.length; i++) {
            if (Constants.PoundsPerWeek[i] == m_Goal) {
                m_GoalButton.setText(getResources().getStringArray(R.array.goals_array)[i]);
            }
        }

        int estimatedCals = getBMR();
        if (m_GoalCals == User.UNKNOWN || !customGoal)
            m_GoalCals = estimatedCals;

        m_EstimatedCalsTextView.setText(Integer.toString(estimatedCals));
        m_BurnedCalsEditText.setText(Integer.toString(m_GoalCals));

        customGoal = (m_GoalCals != estimatedCals);
        m_CalsViewFlipper.setDisplayedChild(customGoal ? 1 : 0);
        updateCalSummary();
    }

    // Based on Mifflin-St Jeor equation
    private int getBMR() {
        float genderFactor = (gender == Constants.MALE) ? 5.0f : -161.0f;
        float activityFactor = getActivityMultiplier();

        return (int) (activityFactor * (4.536f * ((float)m_Weight) + 15.875f * ((float)m_Height) - 5.0f * ((float)m_Age) + genderFactor));
    }

    private float getActivityMultiplier() {
        switch (m_ActivityLevelIndex){

            case 0:
                return 1.2f;
            case 1:
                return 1.375f;
            case 2:
                return 1.55f;
            case 3:
                return 1.725f;
            case 4:
                return 1.9f;
            default:
                return 1.2f;
        }
    }
    private void initListeners() {

        m_GenderRow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showGenderDialog();
            }
        });

        m_AgeRow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showAgeDialog();
            }
        });

        m_HeightRow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showHeightDialog();
            }
        });

        m_WeightRow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showWeightDialog();
            }
        });

        m_ActivityRow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showActivityLevelDialog();
            }
        });

        m_GoalButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showGoalDialog();
            }
        });

        m_CalsViewFlipper.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customGoal) {
                    customGoal = false;
                    updateValues();
                }
            }
        });


        m_BurnedCalsEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isEmpty(m_BurnedCalsEditText)) {
                    customGoal = true;
                    m_CalsViewFlipper.setDisplayedChild(1);
                    m_GoalCals = Integer.valueOf(m_BurnedCalsEditText.getText().toString());
                    updateCalSummary();
                }
            }
        });

    }


    public void update(Calendar calendar) {

    }

    private void updateCalSummary() {
        int difference = (int) (m_Goal * 500.0);
        m_CalsTotalTextView.setText(Integer.toString(m_GoalCals));
        m_CalsEqnTextView.setText(difference < 0 ? "-" : "+");
        m_CalsDiffTextView.setText(Integer.toString(Math.abs(difference)));
        m_CalsGoalTextView.setText(Integer.toString(m_GoalCals + difference));
    }

    private boolean isEmpty(EditText view) {
        return view.getText() == null || view.getText().toString().trim().length() == 0;
    }
    private void showGenderDialog() {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(new ContextThemeWrapper(m_Activity, R.style.BasicAlertDialog));

        gender = m_Gender;


        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_prefs_gender, null);

        builder.setView(view);

        final AlertDialog dialog = builder.create();

        RadioButton maleButton = (RadioButton) view.findViewById(R.id.male);
        RadioButton femaleButton = (RadioButton) view.findViewById(R.id.female);


        if (gender == Constants.MALE)
            maleButton.setChecked(true);
        else
            femaleButton.setChecked(true);

        maleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    gender = Constants.MALE;
            }
        });

        femaleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    gender = Constants.FEMALE;
            }
        });

        view.findViewById(R.id.set_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                m_Gender = gender;
                m_ParseUser.setGender(m_Gender);
                m_ParseUser.saveInBackground();
                updateValues();
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.cancel_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showHeightDialog() {


        AlertDialog.Builder builder =
                new AlertDialog.Builder(new ContextThemeWrapper(m_Activity, R.style.BasicAlertDialog));

        View view = View.inflate(new ContextThemeWrapper(m_Activity, R.style.BasicAlertDialog), R.layout.dialog_prefs_height, null);

        height_feet = getHeightFt();
        height_inches = getHeightIn();

        NumberPicker feet = (NumberPicker) view.findViewById(R.id.height_picker_ft);
        NumberPicker inches = (NumberPicker) view.findViewById(R.id.height_picker_in);

        feet.setMinValue(3);
        feet.setMaxValue(8);
        feet.setWrapSelectorWheel(false);
        feet.setValue(getHeightFt());
        feet.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                height_feet = newVal;
            }
        });


        inches.setMinValue(0);
        inches.setMaxValue(11);
        inches.setWrapSelectorWheel(false);
        inches.setValue(getHeightIn());
        inches.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                height_inches = newVal;
            }
        });


        builder.setView(view);

        final AlertDialog dialog = builder.create();

        view.findViewById(R.id.set_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                m_Height = getHeight(height_feet, height_inches);
                m_ParseUser.setHeight(m_Height);
                m_ParseUser.saveInBackground();
                updateValues();
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.cancel_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

       dialog.show();
    }

    private void showAgeDialog() {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(new ContextThemeWrapper(m_Activity, R.style.BasicAlertDialog));

        View view = View.inflate(new ContextThemeWrapper(m_Activity, R.style.BasicAlertDialog), R.layout.dialog_prefs_age, null);

        age = m_Age;
        NumberPicker agePicker = (NumberPicker) view.findViewById(R.id.age_picker);

        agePicker.setMinValue(0);
        agePicker.setMaxValue(150);
        agePicker.setWrapSelectorWheel(false);
        agePicker.setValue(m_Age);
        agePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                age = newVal;
            }
        });

        builder.setView(view);

        final AlertDialog dialog = builder.create();

        view.findViewById(R.id.set_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                m_Age = age;
                m_ParseUser.setAge(m_Age);
                m_ParseUser.saveInBackground();
                updateValues();
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.cancel_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showWeightDialog() {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(new ContextThemeWrapper(m_Activity, R.style.BasicAlertDialog));

        View view = View.inflate(new ContextThemeWrapper(m_Activity, R.style.BasicAlertDialog), R.layout.dialog_prefs_weight, null);

        final EditText weightEditText = (EditText) view.findViewById(R.id.weight_edit_text);
        weightEditText.setText(Integer.toString(m_Weight));


        builder.setView(view);

        final AlertDialog dialog = builder.create();

        view.findViewById(R.id.set_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (weightEditText.getText() != null) {
                    m_Weight = Integer.parseInt(weightEditText.getText().toString());
                    m_ParseUser.setWeight(m_Weight);
                    m_ParseUser.saveInBackground();
                    updateValues();
                }
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.cancel_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showActivityLevelDialog() {

            AlertDialog.Builder builder =
                    new AlertDialog.Builder(new ContextThemeWrapper(m_Activity, R.style.BasicAlertDialog));

            View view = View.inflate(new ContextThemeWrapper(m_Activity, R.style.BasicAlertDialog), R.layout.dialog_prefs_activity_level, null);

            activityLevelIndex = m_ActivityLevelIndex;

            NumberPicker goalPicker =  (NumberPicker) view.findViewById(R.id.activity_level_picker);
            goalPicker.setMinValue(0);
            goalPicker.setMaxValue(Constants.ActivityLevels.size() - 1);
            String[] arrayValues = new String[Constants.ActivityLevels.size()];
            for (int i = 0; i < Constants.ActivityLevels.size(); i++) {
                arrayValues[i] = Constants.ActivityLevels.get(i);
            }
            goalPicker.setDisplayedValues(arrayValues);
            goalPicker.setWrapSelectorWheel(false);
            goalPicker.setValue(m_ActivityLevelIndex);
            goalPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    activityLevelIndex = newVal;
                }
            });



            builder.setView(view);

            final AlertDialog dialog = builder.create();

            view.findViewById(R.id.set_btn).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    m_ActivityLevelIndex = activityLevelIndex;
                    m_ParseUser.setActivityLevel(m_ActivityLevelIndex);
                    m_ParseUser.saveInBackground();
                    updateValues();
                    dialog.dismiss();
                }
            });

            view.findViewById(R.id.cancel_btn).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();
    }

    private void showGoalDialog() {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(new ContextThemeWrapper(m_Activity, R.style.BasicAlertDialog));

        View view = View.inflate(new ContextThemeWrapper(m_Activity, R.style.BasicAlertDialog), R.layout.dialog_prefs_goal, null);

        goal = m_Goal;

        NumberPicker goalPicker =  (NumberPicker) view.findViewById(R.id.goal_picker);
        goalPicker.setMinValue(0);
        goalPicker.setMaxValue(Constants.PoundsPerWeek.length - 1);
        goalPicker.setDisplayedValues(getResources().getStringArray(R.array.goals_array));
        goalPicker.setWrapSelectorWheel(false);

        for (int i = 0; i < Constants.PoundsPerWeek.length; i++) {
            if (Constants.PoundsPerWeek[i] == m_Goal)
                goalPicker.setValue(i);
        }

        goalPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                goal = Constants.PoundsPerWeek[newVal];
            }
        });


        builder.setView(view);

        final AlertDialog dialog = builder.create();

        view.findViewById(R.id.set_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                m_Goal = goal;
                m_ParseUser.setGoal(m_Goal);
                m_ParseUser.saveInBackground();
                updateValues();
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.cancel_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    private void showEditGoalCalsDialog() {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(new ContextThemeWrapper(m_Activity, R.style.BasicAlertDialog));

        View view = View.inflate(new ContextThemeWrapper(m_Activity, R.style.BasicAlertDialog), R.layout.dialog_prefs_edit_goal_cals, null);


        final EditText editText = (EditText) view.findViewById(R.id.cals_edit_text);
        editText.setText(Integer.toString(2674));

        builder.setView(view);

        final AlertDialog dialog = builder.create();

        view.findViewById(R.id.set_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText() != null) {
                    m_GoalCals = Integer.parseInt(editText.getText().toString());
                    m_ParseUser.setGoalCalories(m_GoalCals);
                    m_ParseUser.saveInBackground();
                    updateValues();
                }
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.cancel_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    private void showMacrosDialog() {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(new ContextThemeWrapper(m_Activity, R.style.BasicAlertDialog));

        View view = View.inflate(new ContextThemeWrapper(m_Activity, R.style.BasicAlertDialog), R.layout.dialog_prefs_macros, null);


        builder.setView(view);

        final AlertDialog dialog = builder.create();

        view.findViewById(R.id.set_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                m_Goal = goal;
                m_ParseUser.setGoal(m_Goal);
                m_ParseUser.saveInBackground();
                updateValues();
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.cancel_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }



    private int getHeightFt() {
        return (int) Math.floor(((double)m_Height) / 12);
    }

    private int getHeightIn() {
        return m_Height % 12;
    }

    private int getHeight(int ft, int in) {
        return ft * 12 + in;
    }




}

