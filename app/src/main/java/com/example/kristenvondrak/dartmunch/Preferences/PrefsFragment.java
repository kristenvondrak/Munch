package com.example.kristenvondrak.dartmunch.Preferences;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.kristenvondrak.dartmunch.Diary.AddFoodActivity;
import com.example.kristenvondrak.dartmunch.Diary.DiaryListAdapter;
import com.example.kristenvondrak.dartmunch.Main.Constants;
import com.example.kristenvondrak.dartmunch.Main.MainTabFragment;
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


public class PrefsFragment extends Fragment implements MainTabFragment {

    public static final String TAG = "PrefsFragment";

    private AppCompatActivity m_Activity;

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
         /*   new DatePickerDialog(m_Activity, R.style.BasicAlertDialog,
                    m_DatePickerListener, m_Calendar.get(Calendar.YEAR),
                    m_Calendar.get(Calendar.MONTH), m_Calendar.get(Calendar.DAY_OF_MONTH)).show(); */
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void initViews(View v) {


    }


    private void initListeners() {
    }


    public void update(Calendar calendar) {

    }



}

