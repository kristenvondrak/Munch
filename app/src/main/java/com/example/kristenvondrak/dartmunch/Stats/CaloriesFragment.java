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

import com.example.kristenvondrak.dartmunch.Main.MainTabFragment;
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
import java.util.Date;
import java.util.List;

/**
 * Created by kristenvondrak on 10/21/15.
 */
public class CaloriesFragment extends Fragment implements OnChartValueSelectedListener, MainTabFragment {

    private Activity m_Activity;

    //colors for filling in graphs
    final int light_gray = Color.argb(200, 0, 171, 61);
    final int gray = Color.GRAY;
    final int bright_green = Color.argb(200, 0, 171, 61);
    final int dull_green = Color.argb(130, 0, 171, 61);
    final int dark_red = Color.argb(235, 155, 26, 25);
    final int bright_red = Color.argb(245, 255, 45, 33);
    final int dull_red = Color.argb(175, 255, 45, 33);

    // Date
    // Date
    private Calendar m_Calendar = Calendar.getInstance();   // REPRESENTS FIRST DAY IN WEEK (MONDAY)
    private int m_SelectedDayOffset = 0;                    // 0 - 7

    private ImageView PreviousWeekButton;
    private TextView CurrentWeekTextView;
    private ImageView NextWeekButton;

    //charts
    private BarChart calories_barChart;
    private PieChart pieChart;
//    private DynamicLayout mCenterTextLayout;
//    private TextPaint mCenterTextPaint;
//    private RectF mCenterTextLastBounds = new RectF();

    //calories variables
    float dailyGoal;
    float caloriesConsumed;
    boolean over;

    //daily calories counts
    float[] calorie_counts = new float[7];

    //int currentWeek;
    //int currentDay;

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

        queryGoalCalories();

        // Set the view for the fragment
        PreviousWeekButton = (ImageView) v.findViewById(R.id.prev_week_btn);
        CurrentWeekTextView = (TextView) v.findViewById(R.id.week_text_view);
        NextWeekButton = (ImageView) v.findViewById(R.id.next_week_btn);
        pieChart = (PieChart) v.findViewById(R.id.chart);
        calories_barChart = (BarChart) v.findViewById(R.id.chart1);

        // Bar and pie chart specifications
        calories_barChart.setAutoScaleMinMaxEnabled(false);
        //calories_barChart.setScaleMinima(0, 0);
        calories_barChart.setDoubleTapToZoomEnabled(false);
        calories_barChart.setBottom(0);
        calories_barChart.setDragEnabled(false);
        pieChart.setTouchEnabled(false);

        Legend legend_pie = pieChart.getLegend();
        Legend legend_bar = calories_barChart.getLegend();
        legend_bar.setEnabled(false);
        legend_pie.setEnabled(false);

        pieChart.setHoleRadius(60f);
        pieChart.setTransparentCircleRadius(65f);
        pieChart.setDrawSliceText(false);
        pieChart.setDrawCenterText(true);
        pieChart.setCenterTextRadiusPercent(80f);


        calories_barChart.setDrawGridBackground(false);

        AxisBase left_axis = calories_barChart.getAxisLeft();
        AxisBase right_axis = calories_barChart.getAxisRight();
        left_axis.setDrawAxisLine(false);
        right_axis.setDrawAxisLine(false);
        left_axis.setDrawGridLines(false);
        right_axis.setDrawGridLines(false);
        right_axis.setDrawLabels(false);
        left_axis.setDrawLabels(false);

        XAxis bar_x = calories_barChart.getXAxis();
        bar_x.setPosition(XAxis.XAxisPosition.BOTTOM);
        bar_x.setDrawGridLines(false);
        bar_x.setDrawAxisLine(false);

        //create empty charts
        for (int i=0;i<=6;i++){calorie_counts[i]=0;}
        caloriesConsumed=0;
        setBarValues();
        setPieValues();

        //get current Calendar to initiate the bar chart data
     /*   m_Calendar = Calendar.getInstance();
        currentWeek = m_Calendar.get(Calendar.WEEK_OF_YEAR);
        currentDay = m_Calendar.get(Calendar.DAY_OF_WEEK);
        m_Calendar.set(Calendar.DAY_OF_WEEK, m_Calendar.getFirstDayOfWeek());
        update(); */

        calories_barChart.setOnChartValueSelectedListener(this);

        //previous week button functionality
        PreviousWeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_Calendar.add(Calendar.WEEK_OF_YEAR, -1);
                update();
            }
        });

        //next week button functionality
        NextWeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_Calendar.add(Calendar.WEEK_OF_YEAR, 1);
                update();
            }
        });

        update(m_Calendar);
        return v;
    }

    // @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        m_SelectedDayOffset = dataSetIndex;
        BarEntry entry = (BarEntry) e;
        caloriesConsumed = entry.getVal();
        setPieValues();
    }

    // @Override
    public void onNothingSelected() {
        calories_barChart.highlightValue(0, 0);
        caloriesConsumed = calorie_counts[0];
        setPieValues();
    }

    // @Override
    public void setBarValues() {

        AxisBase leftAxis = calories_barChart.getAxisLeft();

        LimitLine ll = new LimitLine(dailyGoal);
        ll.setLineColor(gray);
        ll.setLineWidth(.5f);
        leftAxis.removeAllLimitLines();
        leftAxis.addLimitLine(ll);
        leftAxis.setDrawLimitLinesBehindData(true);

        //leftAxis.isDrawLimitLinesBehindDataEnabled();

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

        calories_barChart.invalidate();

    }

    private void setPieValues() {

        over = caloriesConsumed > dailyGoal;

        if (over) {
            float caloriesOver = caloriesConsumed - dailyGoal;

            ArrayList<Entry> entries = new ArrayList<>();
            entries.add(new Entry(dailyGoal, 0));
            entries.add(new Entry((caloriesOver), 1));

            PieDataSet dataset = new PieDataSet(entries, "");
            dataset.setValueTextSize(13f);

            ArrayList<String> labels = new ArrayList<String>();
            labels.add("Calories");
            labels.add("Over");
            pie_center = Math.round(caloriesOver) + "\nCALORIES\nOVER BUDGET";

//            int index = pie_center.indexOf("\n");

//            Spannable tempSpannable = new SpannableString(pie_center);
//            tempSpannable.setSpan(new RelativeSizeSpan(2f), 0, index,
//                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            tempSpannable.setSpan(new ForegroundColorSpan(bright_green),
//                    0, index, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//            // If width is 0, it will crash. Always have a minimum of 1
//            mCenterTextLayout = new DynamicLayout(tempSpannable, mCenterTextPaint,
//                    (int)Math.max(Math.ceil(mCenterTextLastBounds.width()), 1.f),
//                    Layout.Alignment.ALIGN_NORMAL, 1.f, 0.f, false);

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
            dataset.setValueTextSize(13f);

            ArrayList<String> labels = new ArrayList<String>();
            labels.add("Calories");
            labels.add("Left");
            pie_center = Math.round(caloriesLeft) + "\nCALORIES\nUNDER BUDGET";

//            int index = pie_center.indexOf("\n");
//
//            Spannable tempSpannable = new SpannableString(pie_center);
//            tempSpannable.setSpan(new RelativeSizeSpan(2f), 0, index,
//                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            tempSpannable.setSpan(new ForegroundColorSpan(bright_green),
//                    0, index, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//            // If width is 0, it will crash. Always have a minimum of 1
//            pieChart.mCenterTextLayout = new DynamicLayout(tempSpannable, mCenterTextPaint,
//                    (int)Math.max(Math.ceil(mCenterTextLastBounds.width()), 1.f),
//                    Layout.Alignment.ALIGN_NORMAL, 1.f, 0.f, false);

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

    public void update(Calendar calendar) {
        m_Calendar.setTimeInMillis(calendar.getTimeInMillis());
        m_SelectedDayOffset = Utils.getDateOffset(m_Calendar.get(Calendar.DAY_OF_WEEK));

        // We want the week to start on Monday (not Sunday)
        if (m_Calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            m_Calendar.add(Calendar.DAY_OF_WEEK, -1);
        }
        m_Calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        update();
    }

    private void update() {
        CurrentWeekTextView.setText(Utils.getWeekDisplayFromCal(m_Calendar));
        queryWeekCalories(m_Calendar);
    }

    private void queryWeekCalories(Calendar cal) {

        Calendar last = (Calendar) cal.clone();
        last.add(Calendar.DAY_OF_YEAR, 6);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserMeal");
        query.whereGreaterThan("date", Utils.getDateBefore(cal));
        query.whereLessThan("date", Utils.getDateAfter(last));
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.include("entries.recipe");
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.findInBackground(new FindCallback<ParseObject>() {

            public void done(List<ParseObject> meals, ParseException e) {
                List<UserMeal> userMealList = new ArrayList<UserMeal>();
                if (e == null) {
                    for (ParseObject object : meals) {
//                        Log.d("Count after calculate", Float.toString(calorie_counts[i]) + "/" + object.getDate());
                        userMealList.add((UserMeal) object);
                    }
                } else {
                    Log.d("CaloriesFragment", "Error getting user meals: " + e.getMessage());
                }
                updateCalorieSummary(userMealList);
            }
        });
    }

    private void updateCalorieSummary(List<UserMeal> list) {

        //clear the calories count list
        for (int i=0; i<=6; i++){
            calorie_counts[i] = 0;
        }

        //get usermeals for the week, adding to total calorie count based on day
        for (UserMeal m : list) {
            Date mealDate = m.getDate();
            Calendar c = Calendar.getInstance();
            c.setTime(mealDate);
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            for (DiaryEntry e : m.getDiaryEntries()) {
                if (dayOfWeek == 1){
                    calorie_counts[6] += e.getTotalCalories();
                }
                else{
                    calorie_counts[dayOfWeek-2] += e.getTotalCalories();
                }

            }
        }

        setBarValues();
        calories_barChart.highlightValue(m_SelectedDayOffset,0);
        caloriesConsumed = calorie_counts[m_SelectedDayOffset];
        setPieValues();
    }

    private void queryGoalCalories() {
        ParseQuery<User> query = ParseQuery.getQuery("User");
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.getFirstInBackground(new GetCallback<User>() {
            public void done(User CurrentUser, ParseException e) {
                float Goal = 0f;
                if (e == null) {
                    if (CurrentUser == null) {
                        Goal = 2000f;
                    } else {
                        int goal = CurrentUser.getGoalCalories();

                        if (goal == 0.0) {
                            Goal = 2000f;
                        } else {
                            Goal = goal;
                        }
                    }
                } else {
                    Goal = 2000f;
                    ;
                    Log.d("CaloriesFragment", "Error getting goal calories: " + e.getMessage());
                }
                setGoalCals(Goal);
            }
        });
    }

    private void setGoalCals(float goal){
        dailyGoal = goal;
    }

}
