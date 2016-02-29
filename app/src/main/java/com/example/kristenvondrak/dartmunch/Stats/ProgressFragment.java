package com.example.kristenvondrak.dartmunch.Stats;

import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import com.example.kristenvondrak.dartmunch.Main.MainTabFragment;
import com.example.kristenvondrak.dartmunch.Main.Utils;
import com.example.kristenvondrak.dartmunch.Menu.Nutrients;
import com.example.kristenvondrak.dartmunch.Parse.DiaryEntry;
import com.example.kristenvondrak.dartmunch.Parse.Recipe;
import com.example.kristenvondrak.dartmunch.Parse.UserMeal;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.*;
import com.github.mikephil.charting.listener.*;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.Calendar;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.example.kristenvondrak.dartmunch.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * Created by kristenvondrak on 10/21/15.
 */
public class ProgressFragment extends Fragment implements OnChartValueSelectedListener, MainTabFragment {

    // Constants
    String COLOR_CARBS = "#F44336";
    String COLOR_FAT = "#FFC107";
    String COLOR_PROTEIN = "#8BC34A";
    int PROTEIN = 0;
    int FAT = 1;
    int CARBS = 2;

    private AppCompatActivity m_Activity;

    // Date
    private Calendar m_Calendar = Calendar.getInstance();   // REPRESENTS FIRST DAY IN WEEK (MONDAY)
    private int m_SelectedDayOffset = 0;                    // 0 - 7

    private ImageView m_PreviousWeekButton;
    private TextView m_CurrentWeekTextView;
    private ImageView m_NextWeekButton;


    // Charts
    private BarChart m_MacroBarChart;
    private PieChart m_MacroPieChart;

    private List<BarValue> m_BarValues;

    // Bar Labels
    String[] m_BarLabels = {"Mon", "Tue", "Wed", "Thurs", "Fri", "Sat", "Sun"};
    String[] m_PieLabels = {"Protein", "Fat", "Carb"};
    //int[] days = {Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, CA}



    int[] m_Colors = new int[]{ Color.parseColor(COLOR_PROTEIN),
                                Color.parseColor(COLOR_FAT),
                                Color.parseColor(COLOR_CARBS)};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_Activity = (AppCompatActivity) getActivity();

    }

    @Override
    public void onResume() {
        super.onResume();
        update(m_Calendar);
    }

    public void update(Calendar calendar) {
        Log.d("update", Utils.getDisplayStringFromCal(calendar));
        m_Calendar.setTimeInMillis(calendar.getTimeInMillis());
        m_SelectedDayOffset = Utils.getDateOffset(m_Calendar.get(Calendar.DAY_OF_WEEK));
        Log.d("offset", Integer.toString(m_SelectedDayOffset));

        // We want the week to start on Monday (not Sunday)
        if (m_Calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            m_Calendar.add(Calendar.DAY_OF_WEEK, -1);
        }
        m_Calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Log.d("new cal", Utils.getDisplayStringFromCal(m_Calendar));
        m_CurrentWeekTextView.setText(Utils.getWeekDisplayFromCal(m_Calendar));
        queryWeeklyMacros();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_progress, container, false);

        setHasOptionsMenu(true);
        initViews(v);
        initListeners();

        initBarChart();
        initPieChart();

        return v;
    }

    private void initPieChart() {
        m_MacroPieChart.setTouchEnabled(false);
        m_MacroPieChart.setHoleRadius(50f);
        m_MacroPieChart.setTransparentCircleRadius(55f);
        m_MacroPieChart.setDrawSliceText(true);
        m_MacroPieChart.setDrawCenterText(false);
        m_MacroPieChart.setUsePercentValues(true);
        m_MacroPieChart.getLegend().setEnabled(false);
    }

    private void initBarChart() {

        m_MacroBarChart.setScaleEnabled(true); // What does this do?
        m_MacroBarChart.setDoubleTapToZoomEnabled(false);
        m_MacroBarChart.setDragEnabled(false);
        m_MacroBarChart.setDrawValueAboveBar(false);
        m_MacroBarChart.setDrawGridBackground(false);
       // m_MacroBarChart.getLegend().setEnabled(false);
        m_MacroBarChart.setDescription("");    // Hide the description


        m_MacroBarChart.getLegend().setEnabled(false);
        //m_MacroBarChart.getLegend().setCustom(m_Colors, new String[]{"Proteins", "Fats", "Carbs"});

        // Disable all axes and gridlines on bar chart
        AxisBase leftAxis = m_MacroBarChart.getAxisLeft();
        leftAxis.setDrawLimitLinesBehindData(true);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawLabels(false);
        leftAxis.setDrawGridLines(false);


        AxisBase rightAxis = m_MacroBarChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawLabels(false);
        rightAxis.setDrawAxisLine(false);

        XAxis xAxis = m_MacroBarChart.getXAxis();
        xAxis.setTextSize(10f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

    }

    private void initViews(View v) {
        m_PreviousWeekButton = (ImageView) v.findViewById(R.id.prev_week_btn);
        m_CurrentWeekTextView = (TextView) v.findViewById(R.id.week_text_view);
        m_NextWeekButton = (ImageView) v.findViewById(R.id.next_week_btn);
        m_MacroBarChart = (BarChart) v.findViewById(R.id.chart1);
        m_MacroPieChart = (PieChart) v.findViewById(R.id.chart);
    }

    private void initListeners() {
        m_PreviousWeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_Calendar.add(Calendar.WEEK_OF_YEAR, -1);
                m_CurrentWeekTextView.setText(Utils.getWeekDisplayFromCal(m_Calendar));
                queryWeeklyMacros();
            }
        });

        m_NextWeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_Calendar.add(Calendar.WEEK_OF_YEAR, 1);
                m_CurrentWeekTextView.setText(Utils.getWeekDisplayFromCal(m_Calendar));
                queryWeeklyMacros();
            }
        });
    }


    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        m_SelectedDayOffset = e.getXIndex();
        BarEntry entry = (BarEntry) e;
        setPieValues(entry.getVals());
    }


    public void onNothingSelected() {

    }


    private void setPieValues(float[] values) {

        // Values
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(values[PROTEIN], PROTEIN));
        entries.add(new Entry(values[FAT], FAT));
        entries.add(new Entry(values[CARBS], CARBS));

        PieDataSet dataset = new PieDataSet(entries, "");
        dataset.setValueFormatter(new PercentFormatter(new DecimalFormat("###,###,##0")));
        dataset.setValueTextSize(15f);
        PieData data = new PieData(m_PieLabels, dataset);
        dataset.setColors(m_Colors);

        m_MacroPieChart.setDescription("");
        m_MacroPieChart.setData(data);
        m_MacroPieChart.invalidate();
    }

    private void updateBarValues() {

        // Values
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < m_BarValues.size(); i++) {
            BarEntry day = new BarEntry(m_BarValues.get(i).values, i);
            entries.add(day);
        }

        BarDataSet dataset = new BarDataSet(entries, "");
        dataset.setDrawValues(false);
        dataset.setBarSpacePercent(40f);
        dataset.setValueTextSize(10f);
        BarData data1 = new BarData(m_BarLabels, dataset);
        dataset.setColors(m_Colors);


        m_MacroBarChart.setDescription("");
        m_MacroBarChart.setData(data1);
        m_MacroBarChart.setOnChartValueSelectedListener(this);
        m_MacroBarChart.invalidate();
    }

    private void queryWeeklyMacros() {

        //Utils.showProgressSpinner(m_ProgressSpinner);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserMeal");

        // Before first day of current week
        query.whereGreaterThan("date", Utils.getDateBefore(m_Calendar));
        Log.d("day before", Utils.getDateBefore(m_Calendar).toString());

        // After last day of current week
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(m_Calendar.getTimeInMillis());
        cal.add(Calendar.DAY_OF_MONTH, 7);
        query.whereLessThan("date", Utils.getDateAfter(cal));
        Log.d("week day after", Utils.getDateAfter(cal).toString());


        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.include("entries.recipe");
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        query.findInBackground(new FindCallback<ParseObject>() {

            public void done(List<ParseObject> meals, ParseException e) {
                List<UserMeal> userMealList = new ArrayList<UserMeal>();
                if (e == null) {
                    for (ParseObject object : meals) {
                        userMealList.add((UserMeal) object);
                    }
                    m_BarValues = calculateMacrosForWeek(userMealList);
                    for (BarValue b : m_BarValues) {
                        Log.d("************", "bar value: " + Integer.toString(b.day) + b.values.toString());
                    }
                    updateBarValues();
                    setPieValues(m_BarValues.get(m_SelectedDayOffset).values);

                } else {
                    Log.d("************", "Error getting user meals: " + e.getMessage());
                }
                //update();
                //Utils.hideProgressSpinner(m_ProgressSpinner);
            }
        });

    }

    private List<BarValue> calculateMacrosForWeek(List<UserMeal> list) {

        // Map the day : float[protein, fat, carbs]
        HashMap<Integer, float[]> dayToMacroMap = new HashMap<>();


        for (UserMeal meal : list) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(meal.getDate());
            int day = Utils.getDateOffset(cal.get(Calendar.DAY_OF_WEEK));
            Log.d("user meal for day: " , Integer.toString(day));

            float[] macros = getTotalMacros(meal.getDiaryEntries());

            float[] dayTotal;
            if (dayToMacroMap.containsKey(day)) {
                dayTotal = dayToMacroMap.get(day);
            } else {
                dayTotal = new float[]{0, 0, 0};
            }
            dayTotal[PROTEIN] += macros[PROTEIN];
            dayTotal[CARBS] += macros[CARBS];
            dayTotal[FAT] += macros[FAT];

            dayToMacroMap.put(day, dayTotal);
        }

        List<BarValue> barValues = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(m_Calendar.getTimeInMillis());
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        for (int i = 0; i < 7; i++) {
            if (dayToMacroMap.containsKey(i)) {
                barValues.add(new BarValue(i, dayToMacroMap.get(i)));
            } else {
                barValues.add(new BarValue(i, new float[]{0,0,0}));
            }
        }

        return barValues;
    }


    private float[] getTotalMacros(List<DiaryEntry> entries) {
        int protein = 0;
        int carbs = 0;
        int fat = 0;
        for (DiaryEntry entry : entries) {
            Recipe recipe = entry.getRecipe();
            if (recipe.getProtein() != null) {
                protein += (int) Nutrients.convertToDouble(recipe.getProtein());
            }
            if (recipe.getTotalCarbs() != null) {
                carbs += (int) Nutrients.convertToDouble(recipe.getTotalCarbs());
            }
            if (recipe.getTotalFat() != null) {
                fat += (int) Nutrients.convertToDouble(recipe.getTotalFat());
            }
        }

        float[] values = new float[3];
        values[PROTEIN] = protein;
        values[CARBS] = carbs;
        values[FAT] = fat;
        return values;
    }


    private class BarValue {

        int day;
        float[] values;

        public BarValue(int day, float[] values) {
            this.day = day;
            this.values = values;
        }

        public void add(float[] macros) {
            values[PROTEIN] += macros[PROTEIN];
            values[CARBS] += macros[CARBS];
            values[FAT] += macros[FAT];
        }

    }

}

