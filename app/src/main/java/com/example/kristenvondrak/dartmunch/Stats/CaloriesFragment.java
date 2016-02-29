package com.example.kristenvondrak.dartmunch.Stats;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kristenvondrak.dartmunch.Main.Utils;
import com.example.kristenvondrak.dartmunch.Parse.DiaryEntry;
import com.example.kristenvondrak.dartmunch.Parse.UserMeal;
import com.example.kristenvondrak.dartmunch.Parse.User;
import com.example.kristenvondrak.dartmunch.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.lang.*;
import java.util.Calendar;
import java.util.List;

/**
 * Created by kristenvondrak on 10/21/15.
 */
public class CaloriesFragment extends Fragment implements OnChartValueSelectedListener {

    private Activity m_Activity;

    //colors for filling in graphs
    final int light_gray = Color.LTGRAY;
    final int gray = Color.GRAY;
    final int bright_green = Color.argb(200, 0, 171, 61);
    final int dull_green = Color.argb(130, 0, 171, 61);
    final int dark_red = Color.argb(235, 155, 26, 25);
    final int bright_red = Color.argb(245, 255, 45, 33);
    final int dull_red = Color.argb(175, 255, 45, 33);

    // Date
    private Calendar m_Calendar;
    private ImageView PreviousWeekButton;
    private TextView CurrentWeekTextView;
    private ImageView NextWeekButton;

    //charts
    private BarChart calories_barChart;
    private PieChart pieChart;

    //calories variables
    float dailyGoal;
    float caloriesConsumed;
    boolean over;

    //daily calories counts
    float[] calorie_counts = new float[7];
    float current_cal;

    String pie_center;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_Activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_calories, container, false);

        //queryGoalCalories();
        dailyGoal = 2000f;

        PreviousWeekButton = (ImageView) v.findViewById(R.id.prev_week_btn);
        CurrentWeekTextView = (TextView) v.findViewById(R.id.week_text_view);
        NextWeekButton = (ImageView) v.findViewById(R.id.next_week_btn);

        m_Calendar = Calendar.getInstance();

        int day_of_week = m_Calendar.get(Calendar.DAY_OF_WEEK);

        m_Calendar.set(Calendar.DAY_OF_WEEK, m_Calendar.getFirstDayOfWeek());
        update();

        //set default values for pie chart
        if (day_of_week == 1) {
            caloriesConsumed = calorie_counts[6];
        } else {
            caloriesConsumed = calorie_counts[(day_of_week - 2)];
        }

        PreviousWeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_Calendar.add(Calendar.WEEK_OF_YEAR, -1);
                update();
            }
        });

        NextWeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_Calendar.add(Calendar.WEEK_OF_YEAR, 1);
                update();
            }
        });

        pieChart = (PieChart) v.findViewById(R.id.chart);
        calories_barChart = (BarChart) v.findViewById(R.id.chart1);

        calories_barChart.setScaleEnabled(false);
        calories_barChart.setDoubleTapToZoomEnabled(false);
        calories_barChart.setDragEnabled(false);
        pieChart.setTouchEnabled(false);

        Legend legend_pie = pieChart.getLegend();
        Legend legend_bar = calories_barChart.getLegend();
        legend_pie.setEnabled(false);
        legend_bar.setEnabled(false);

        pieChart.setHoleRadius(60f);
        pieChart.setTransparentCircleRadius(65f);
        pieChart.setDrawSliceText(false);
        pieChart.setDrawCenterText(true);
        pieChart.setCenterTextRadiusPercent(60f);

        calories_barChart.setDrawGridBackground(false);

        AxisBase left_axis = calories_barChart.getAxisLeft();
        AxisBase right_axis = calories_barChart.getAxisRight();
        left_axis.setDrawAxisLine(false);
        right_axis.setDrawAxisLine(false);
        left_axis.setDrawGridLines(false);
        right_axis.setDrawGridLines(false);
        right_axis.setDrawLabels(false);
        left_axis.setDrawLabels(false);

        LimitLine ll = new LimitLine(dailyGoal);
        ll.setLineColor(gray);
        ll.setLineWidth(.5f);

        left_axis.addLimitLine(ll);
        left_axis.isDrawLimitLinesBehindDataEnabled();

        XAxis bar_x = calories_barChart.getXAxis();
        bar_x.setPosition(XAxis.XAxisPosition.BOTTOM);
        bar_x.setDrawGridLines(false);
        bar_x.setDrawAxisLine(false);

        ArrayList<BarEntry> entries1 = new ArrayList<>();
        entries1.add(new BarEntry(calorie_counts[0], 0));
        entries1.add(new BarEntry(calorie_counts[1], 1));
        entries1.add(new BarEntry(calorie_counts[2], 2));
        entries1.add(new BarEntry(calorie_counts[3], 3));
        entries1.add(new BarEntry(calorie_counts[4], 4));
        entries1.add(new BarEntry(calorie_counts[5], 5));
        entries1.add(new BarEntry(calorie_counts[6], 6));

        BarDataSet dataset1 = new BarDataSet(entries1, "");

        dataset1.setBarSpacePercent(50f);
        dataset1.setValueTextSize(10f);

        ArrayList<String> labels1 = new ArrayList<String>();
        labels1.add("Mon");
        labels1.add("Tues");
        labels1.add("Wed");
        labels1.add("Thurs");
        labels1.add("Fri");
        labels1.add("Sat");
        labels1.add("Sun");

        BarData data1 = new BarData(labels1, dataset1);
        int[] colorset1 = new int[]{light_gray, light_gray, light_gray, light_gray, light_gray, light_gray, light_gray};
        dataset1.setColors(colorset1);
        calories_barChart.setDescription("");
        calories_barChart.setData(data1);

        calories_barChart.setOnChartValueSelectedListener(this);

        setPieValues();

        return v;
    }

    // @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        BarEntry entry = (BarEntry) e;
        caloriesConsumed = entry.getVal();
        setPieValues();
    }

    // @Override
    public void onNothingSelected() {

    }

    private void setPieValues() {

        over = caloriesConsumed > dailyGoal;

        if (over) {
            float caloriesOver = caloriesConsumed - dailyGoal;

            ArrayList<Entry> entries = new ArrayList<>();
            entries.add(new Entry(dailyGoal, 0));
            entries.add(new Entry((caloriesOver), 1));

            PieDataSet dataset = new PieDataSet(entries, "");
            dataset.setValueTextSize(10f);

            ArrayList<String> labels = new ArrayList<String>();
            labels.add("Calories");
            labels.add("Over");
            pie_center = Math.round(caloriesOver) + "\nCALORIES\nOVER BUDGET";

            PieData data = new PieData(labels, dataset);
            int[] colorset = new int[]{bright_green, bright_red};
            dataset.setColors(colorset);
            dataset.setDrawValues(false);
            pieChart.setDescription(Math.round(caloriesConsumed) + "/" + Math.round(dailyGoal));
            pieChart.setData(data);
            pieChart.setCenterText(pie_center);

            pieChart.invalidate();

        } else {

            float caloriesLeft = dailyGoal - caloriesConsumed;

            ArrayList<Entry> entries = new ArrayList<>();
            entries.add(new Entry(caloriesConsumed, 0));
            entries.add(new Entry((caloriesLeft), 1));

            PieDataSet dataset = new PieDataSet(entries, "");
            dataset.setValueTextSize(17f);

            ArrayList<String> labels = new ArrayList<String>();
            labels.add("Calories");
            labels.add("Left");
            pie_center = Math.round(caloriesLeft) + "\nCALORIES\nUNDER BUDGET";

            PieData data = new PieData(labels, dataset);
            int[] colorset = new int[]{bright_green, light_gray};
            dataset.setColors(colorset);
            dataset.setDrawValues(false);
            pieChart.setDescription(Math.round(caloriesConsumed) + "/" + Math.round(dailyGoal));
            pieChart.setData(data);
            pieChart.setCenterText(pie_center);

            pieChart.invalidate();

        }

    }

    private void update() {

        CurrentWeekTextView.setText(Utils.getWeekDisplayFromCal(m_Calendar));
        calorie_counts[0] = 1500f;
        calorie_counts[1] = 2250f;
        calorie_counts[2] = 1400f;
        calorie_counts[3] = 2000f;
        calorie_counts[4] = 2150f;
        calorie_counts[5] = 1800f;
        calorie_counts[6] = 1700f;
        //queryWeekCalories(m_Calendar, calorie_counts);
        for (int i = 0; i < 7; i++){

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
                        Log.d("ProgressFragment", "Error getting user meals: " + e.getMessage());
                    }
                    current_cal = calculateCalories(userMealList);
                    Log.d("Count after calculate", Float.toString(current_cal) + "/" + Utils.getDisplayStringFromCal(m_Calendar));

                }
            });

//            queryUserMeals(m_Calendar);
            calorie_counts[i] = current_cal;
                    Log.d("Count/Date", Float.toString(calorie_counts[i])+"/"+ Utils.getDisplayStringFromCal(m_Calendar));
            m_Calendar.add(Calendar.DATE, 1);
        }
        //loop calendar back to start of week
        m_Calendar.add(Calendar.DATE, -7);
        caloriesConsumed = calorie_counts[0];
    }

//    private void queryUserMeals(final Calendar cal) {
//
//        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserMeal");
//        query.whereGreaterThan("date", Utils.getDateBefore(cal));
//        query.whereLessThan("date", Utils.getDateAfter(cal));
//        query.whereEqualTo("user", ParseUser.getCurrentUser());
//        query.include("entries.recipe");
//        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
//        query.findInBackground(new FindCallback<ParseObject>() {
//
//            public void done(List<ParseObject> meals, ParseException e) {
//                List<UserMeal> userMealList = new ArrayList<UserMeal>();
//                if (e == null) {
//                    for (ParseObject object : meals) {
//                        userMealList.add((UserMeal) object);
//                    }
//                } else {
//                    Log.d("ProgressFragment", "Error getting user meals: " + e.getMessage());
//                }
//                current_cal = calculateCalories(userMealList);
//                Log.d("Count after calculate", Float.toString(current_cal) + "/" + Utils.getDisplayStringFromCal(cal));
//
//            }
//        });
//    }

    private float calculateCalories(List<UserMeal> list) {
        // Recalculate the total calories consumed
        float total_calories = 0;
        for (UserMeal m : list) {
            for (DiaryEntry e : m.getDiaryEntries()) {
                total_calories += e.getTotalCalories();
            }
        }
        return total_calories;
    }

    private void queryGoalCalories() {

        ParseQuery<User> query = ParseQuery.getQuery("User");
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.getFirstInBackground(new GetCallback<User>() {
            public void done(User CurrentUser, ParseException e) {
                if (e == null) {
                    if (CurrentUser == null) {
                        dailyGoal = 1000f;
                    } else {
                        CurrentUser.setGoalDailyCalories(1500f);
                        dailyGoal = CurrentUser.getGoalDailyCalories();
                        Log.d("GoalCalories", dailyGoal + e.getMessage());

                    }
                } else {
                    dailyGoal = 500f;
                    Log.d("CaloriesFragment", "Error getting goal calories: " + e.getMessage());
                }
            }
        });
    }

}
