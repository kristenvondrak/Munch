package com.example.kristenvondrak.dartmunch.Stats;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import com.example.kristenvondrak.dartmunch.Main.MainTabFragment;
import com.example.kristenvondrak.dartmunch.Main.Utils;
import com.example.kristenvondrak.dartmunch.Parse.DiaryEntry;
import com.example.kristenvondrak.dartmunch.Parse.UserMeal;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.*;
import com.github.mikephil.charting.listener.*;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.util.Calendar;
import java.text.DecimalFormat;

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

    private Activity m_Activity;

    // Date
    private Calendar m_Calendar = Calendar.getInstance();   // REPRESENTS FIRST DAY IN WEEK (MONDAY)
    private int m_SelectedDayOffset = 0;                    // 0 - 7


    private ImageView PreviousWeekButton;
    private TextView CurrentWeekTextView;
    private ImageView NextWeekButton;

    //colors for filling in graphs
    String COLOR_CARBS = "#00897B";
    String COLOR_FAT = "#FFA000";
    String COLOR_PROTEIN = "#F44336";
    final int light_gray = Color.LTGRAY;
    final int gray = Color.GRAY;
    final int bright_green = Color.argb(200, 0, 171, 61);
    final int dull_green = Color.argb(130, 0, 171, 61);
    final int dark_red = Color.argb(235, 155, 26, 25);
    final int bright_red = Color.argb(245, 255, 45, 33);
    final int dull_red = Color.argb(175, 255, 45, 33);

    //charts
    private BarChart macro_barChart;
    private PieChart pieChart;

    float proteins;
    float fats;
    float carbs;

    float[] pCounts = new float[7];
    float[] fCounts = new float[7];
    float[] cCounts = new float[7];

    int currentWeek;
    int currentDay;

    int[] colorset = new int[]{ Color.parseColor(COLOR_PROTEIN),
                                Color.parseColor(COLOR_FAT),
                                Color.parseColor(COLOR_CARBS)};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_Activity = getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_progress, container, false);
        m_Calendar = Calendar.getInstance();
//
        //set the view for the fragment
        PreviousWeekButton = (ImageView) v.findViewById(R.id.prev_week_btn);
        CurrentWeekTextView = (TextView) v.findViewById(R.id.week_text_view);
        NextWeekButton = (ImageView) v.findViewById(R.id.next_week_btn);
        pieChart = (PieChart) v.findViewById(R.id.chart);
        macro_barChart = (BarChart) v.findViewById(R.id.chart1);

        //bar and pie chart specifications
        macro_barChart.setScaleEnabled(false);
        macro_barChart.setDoubleTapToZoomEnabled(false);
        macro_barChart.setDragEnabled(false);
        macro_barChart.setDrawValueAboveBar(false);
        pieChart.setTouchEnabled(false);

        Legend legend_pie = pieChart.getLegend();
        legend_pie.setEnabled(true);
        legend_pie.setCustom(colorset, new String[]{"Proteins", "Fats", "Carbs"});


        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.setDrawSliceText(true);
        pieChart.setDrawCenterText(false);
        pieChart.setUsePercentValues(true);
        pieChart.setDescriptionColor(getResources().getColor(R.color.white));
        pieChart.getLegend().setEnabled(false);

        macro_barChart.setDrawGridBackground(false);

        Legend legend_bar = macro_barChart.getLegend();
        legend_bar.setEnabled(false);

        AxisBase left_axis = macro_barChart.getAxisLeft();
        AxisBase right_axis = macro_barChart.getAxisRight();
        left_axis.setDrawAxisLine(false);
        right_axis.setDrawAxisLine(false);
        left_axis.setDrawGridLines(false);
        right_axis.setDrawGridLines(false);
        right_axis.setDrawLabels(false);
        left_axis.setDrawLabels(false);

        XAxis bar_x = macro_barChart.getXAxis();
        bar_x.setPosition(XAxis.XAxisPosition.BOTTOM);
        bar_x.setDrawGridLines(false);
        bar_x.setAxisLineWidth(0);
        bar_x.setDrawAxisLine(false);
        bar_x.setDrawLabels(true);

        //create empty charts
        for (int i=0;i<=6;i++){pCounts[i]=0;fCounts[i]=0;cCounts[i]=0;}
        proteins=0;
        fats=0;
        carbs=0;
        setBarValues();
        getPieValues();

        //get current Calendar to initiate the bar chart data

        macro_barChart.setOnChartValueSelectedListener(this);


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

        update(m_Calendar);
        return v;
    }

    // @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

        macro_barChart.highlightValue(e.getXIndex(), dataSetIndex);
        Log.d("Data set Index", "is" + dataSetIndex);

        m_SelectedDayOffset = dataSetIndex;

        BarEntry entry = (BarEntry) e;
        float[] values = entry.getVals();

        proteins = values[0];
        fats = values[1];
        carbs = values[2];
        getPieValues();
    }

    // @Override
    public void onNothingSelected() {
        macro_barChart.highlightValue(0, 0);
        proteins = pCounts[0];
        fats = fCounts[0];
        carbs = cCounts[0];
        getPieValues();
    }

    private void setBarValues() {
        ArrayList<BarEntry> entries1 = new ArrayList<>();
        entries1.add(new BarEntry(new float[]{pCounts[0], fCounts[0], cCounts[0]}, 0));
        entries1.add(new BarEntry(new float[]{pCounts[1], fCounts[1], cCounts[1]}, 1));
        entries1.add(new BarEntry(new float[]{pCounts[2], fCounts[2], cCounts[2]}, 2));
        entries1.add(new BarEntry(new float[]{pCounts[3], fCounts[3], cCounts[3]}, 3));
        entries1.add(new BarEntry(new float[]{pCounts[4], fCounts[4], cCounts[4]}, 4));
        entries1.add(new BarEntry(new float[]{pCounts[5], fCounts[5], cCounts[5]}, 5));
        entries1.add(new BarEntry(new float[]{pCounts[6], fCounts[6], cCounts[6]}, 6));

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
        dataset1.setDrawValues(false);
        dataset1.setColors(colorset);
        macro_barChart.setDescription("");
        macro_barChart.setData(data1);
        macro_barChart.setDrawValueAboveBar(false);

        macro_barChart.invalidate();

    }

    private void getPieValues() {
        boolean noData = false;
        ArrayList<Entry> entries = new ArrayList<>();
        if (proteins==0 & fats==0 & carbs==0){
            noData = true;
            pieChart.setDrawSliceText(false);
            pieChart.setDrawCenterText(true);
            pieChart.setCenterTextRadiusPercent(40f);
            pieChart.setCenterText("No\nData");
            entries.add(new Entry(100, 0));
            entries.add(new Entry(fats, 1));
            entries.add(new Entry(carbs, 2));
        } else {
            pieChart.setDrawSliceText(true);
            pieChart.setDrawCenterText(false);
            entries.add(new Entry(proteins, 0));
            entries.add(new Entry(fats, 1));
            entries.add(new Entry(carbs, 2));
        }

        PieDataSet dataset = new PieDataSet(entries, "");
        dataset.setValueFormatter(new PercentFormatter(new DecimalFormat("###,###,##0")));
        dataset.setValueTextSize(13f);

        ArrayList<String> labels = new ArrayList<String>();
        labels.add("Proteins");
        labels.add("Fats");
        labels.add("Carbs");
        PieData data = new PieData(labels, dataset);

        if (noData){
            dataset.setDrawValues(false);
            dataset.setColors(new int[]{Color.LTGRAY, Color.LTGRAY, Color.LTGRAY});
        } else {
            dataset.setColors(colorset);
        }

        pieChart.setDescription("");
        pieChart.setData(data);
        pieChart.invalidate();
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
        queryWeekNutrients(m_Calendar);
    }


    private void queryWeekNutrients(Calendar cal) {

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
                        userMealList.add((UserMeal) object);
                    }
                } else {
                    Log.d("ProgressFragment", "Error getting user meals: " + e.getMessage());
                }
                updateNutrientSummary(userMealList);
            }
        });

    }

    private void updateNutrientSummary(List<UserMeal> list) {

        //clear the calories count list
        for (int i=0; i<=6; i++){
            pCounts[i] = 0;
            fCounts[i] = 0;
            cCounts[i] = 0;
        }

        //get usermeals for the week, adding to total calorie count based on day
        for (UserMeal m : list) {
            Date mealDate = m.getDate();
            Calendar c = Calendar.getInstance();
            c.setTime(mealDate);
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            for (DiaryEntry e : m.getDiaryEntries()) {
                if (dayOfWeek == 1){
                    float[] totalNutrients = getTotalNutrients(e);
                    pCounts[6] += totalNutrients[0];
                    fCounts[6] += totalNutrients[1];
                    cCounts[6] += totalNutrients[2];
                }
                else{
                    float[] totalNutrients = getTotalNutrients(e);
                    pCounts[dayOfWeek-2] += totalNutrients[0];
                    fCounts[dayOfWeek-2] += totalNutrients[1];
                    cCounts[dayOfWeek-2] += totalNutrients[2];
                }

            }
        }

        setBarValues();

        macro_barChart.highlightValue(m_SelectedDayOffset,0);
        proteins = pCounts[m_SelectedDayOffset];
        fats = fCounts[m_SelectedDayOffset];
        carbs = cCounts[m_SelectedDayOffset];

    }

    private float[] getTotalNutrients(DiaryEntry e) {

        float[] nutrients = new float[3];

        // get proteins
        String input = (e.getRecipe().getProtein());
        if (input != null) {
            input = input.replaceAll("[^0-9]", "");
            nutrients[0] = (Float.parseFloat(input)) * e.getServingsMultiplier();
        } else {
            nutrients[0] = 0;
        }

        //get fats
        input = (e.getRecipe().getTotalFat());
        if (input != null) {
            input = input.replaceAll("[^0-9]", "");
            nutrients[1] = (Float.parseFloat(input)) * e.getServingsMultiplier();
        } else {
            nutrients[1] = 0;
        }


        //get carbs
        input = (e.getRecipe().getTotalCarbs());
        if (input != null) {
            input = input.replaceAll("[^0-9]", "");
            nutrients[2] = (Float.parseFloat(input)) * e.getServingsMultiplier();
        } else {
            nutrients[2] = 0;
        }
        return nutrients;
    }

}

