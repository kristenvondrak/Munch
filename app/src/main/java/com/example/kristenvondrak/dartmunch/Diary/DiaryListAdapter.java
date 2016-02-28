package com.example.kristenvondrak.dartmunch.Diary;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.kristenvondrak.dartmunch.Main.Constants;
import com.example.kristenvondrak.dartmunch.Main.MainActivity;
import com.example.kristenvondrak.dartmunch.Parse.DiaryEntry;
import com.example.kristenvondrak.dartmunch.Parse.Recipe;
import com.example.kristenvondrak.dartmunch.Parse.UserMeal;
import com.example.kristenvondrak.dartmunch.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;


/**
 * Created by kristenvondrak on 1/25/16.
 */
public class DiaryListAdapter extends BaseAdapter {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;
    private static final int TYPE_DIVIDER = 2;


    private Activity m_Activity;
    private LayoutInflater m_Inflater;
    private Calendar m_Calendar;

    private ArrayList<Object> m_Data = new ArrayList<Object>();
    private TreeSet<Integer> m_SeparatorsSet = new TreeSet<Integer>();
    private TreeSet<Integer> m_DividersSet = new TreeSet<Integer>();
    private HashMap<String, UserMeal> m_MealsMap = new HashMap<>();

    // Need this for deletion of diary entries
    private HashMap<DiaryEntry, String> m_EntryToUserMealIdMap = new HashMap<>();


    public DiaryListAdapter(Activity activity) {
        m_Activity = activity;
        m_Inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    public void updateData(List<UserMeal> list, Calendar cal) {
        m_Calendar = cal;
        m_Data.clear();
        m_SeparatorsSet.clear();
        m_DividersSet.clear();
        m_MealsMap.clear();
        m_EntryToUserMealIdMap.clear();

        // Create map of ALL meals mapping to empty lists
        // Even if a user does not have any entries for a meal, display a header
        HashMap<String, List<DiaryEntry>> mealToEntriesMap = new HashMap<>();
        for (int i = 0; i < Constants.UserMeals.values().length; i++) {
            String title = Constants.UserMeals.values()[i].name();
            List<DiaryEntry> set = new ArrayList<>();
            mealToEntriesMap.put(title, set);
        }

        // Add diary entries to meals
        for (UserMeal meal : list) {
            // Map the meal title to a reference to object
            m_MealsMap.put(meal.getTitle(), meal);
            for (DiaryEntry entry : meal.getDiaryEntries()) {
                mealToEntriesMap.get(meal.getTitle()).add(entry);
                m_EntryToUserMealIdMap.put(entry, meal.getObjectId());
            }
        }

        // Create ordered list of headers (meals) and items (diary entries)
        for (int i = 0; i < Constants.UserMeals.values().length; i++) {
            String name = Constants.UserMeals.values()[i].name();
            addSeparatorItem(name);
            for (DiaryEntry entry: mealToEntriesMap.get(name))
                addItem(entry);

            // Add dividers between user meals
            if (i != Constants.UserMeals.values().length - 1)
                addDividerItem();
        }
        notifyDataSetChanged();
    }

    public void addItem(DiaryEntry item) {
        m_Data.add(item);
    }

    public void addSeparatorItem(final String item) {
        m_Data.add(item);
        // save separator position
        m_SeparatorsSet.add(m_Data.size() - 1);
    }

    public void addDividerItem() {
        m_Data.add("d");
        m_DividersSet.add(m_Data.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        if (m_SeparatorsSet.contains(position))
            return TYPE_SEPARATOR;
        else if (m_DividersSet.contains(position))
            return TYPE_DIVIDER;
        else
            return TYPE_ITEM;
    }


    public int getCount() {
        return m_Data.size();
    }

    public Object getItem(int position) {
        return m_Data.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
    

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = null;
        int type = getItemViewType(position);

        switch (type) {

            case TYPE_ITEM:

                final DiaryEntry entry = (DiaryEntry) m_Data.get(position);

                rowView = m_Inflater.inflate(R.layout.diary_entry, null);
                Recipe recipe = entry.getRecipe();

                TextView name = (TextView) rowView.findViewById(R.id.item_name);
                name.setText(recipe.getName());

                TextView cal = (TextView) rowView.findViewById(R.id.item_cals);
                cal.setText(Integer.toString(entry.getTotalCalories()));

                TextView servings = (TextView) rowView.findViewById(R.id.item_servings);

                // TODO: use display not float
                servings.setText(Float.toString(entry.getServingsMultiplier()) + " Servings");

                rowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Pass the diary entry id, user meal id, and date
                        Intent intent = new Intent(m_Activity, EditFoodActivity.class);
                        intent.putExtra(DiaryFragment.EXTRA_DIARY_ENTRY_ID, entry.getObjectId());
                        intent.putExtra(DiaryFragment.EXTRA_USER_MEAL_ID, m_EntryToUserMealIdMap.get(entry));
                        intent.putExtra(DiaryFragment.EXTRA_DATE, m_Calendar.getTimeInMillis());

                        // Start EditDiaryEntryActivity
                        m_Activity.startActivityForResult(intent, Constants.REQUEST_EDIT_FOOD);
                        //m_Activity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);

                    }
                });

                break;

            case TYPE_SEPARATOR:

                final String title = (String) m_Data.get(position);
                rowView = m_Inflater.inflate(R.layout.diary_usermeal, null);

                // Meal title
                TextView titleView = (TextView) rowView.findViewById(R.id.usermeal_name);
                titleView.setText(title + ":");

                // Total calories for meal
                TextView cals = (TextView) rowView.findViewById(R.id.usermeal_cals);
                final UserMeal userMeal = m_MealsMap.get(title);
                if (userMeal == null) {
                    cals.setText("0");
                } else {
                    int total = 0;
                    for (DiaryEntry e : userMeal.getDiaryEntries()) {
                        total += e.getTotalCalories();
                    }
                    cals.setText(Integer.toString(total));
                }

                // Add to meal button
                //ImageView addBtn = (ImageView) rowView.findViewById(R.id.usermeal_add_btn);
                rowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Pass the meal time and date
                        Intent intent = new Intent(m_Activity, AddFoodActivity.class);
                        intent.putExtra(DiaryFragment.EXTRA_MEALTIME, title);
                        intent.putExtra(DiaryFragment.EXTRA_DATE, m_Calendar.getTimeInMillis());

                        // Start AddUserMealActivity
                        m_Activity.startActivityForResult(intent, Constants.REQUEST_ADD_FROM_DIARY);
                        //m_Activity.overridePendingTransition(R.anim.none, R.anim.slide_in_from_bottom);

                    }
                });
                break;

            case TYPE_DIVIDER:
                rowView = m_Inflater.inflate(R.layout.diary_usermeal_divider, null);
                break;

        }

        return rowView;
    }

}


