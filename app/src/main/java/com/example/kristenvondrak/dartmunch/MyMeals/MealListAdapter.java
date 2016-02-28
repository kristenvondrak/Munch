package com.example.kristenvondrak.dartmunch.MyMeals;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kristenvondrak.dartmunch.Main.Constants;
import com.example.kristenvondrak.dartmunch.Menu.MenuFragment;
import com.example.kristenvondrak.dartmunch.Parse.DiaryEntry;
import com.example.kristenvondrak.dartmunch.Parse.Recipe;
import com.example.kristenvondrak.dartmunch.Parse.UserMeal;
import com.example.kristenvondrak.dartmunch.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by kristenvondrak on 1/24/16.
 */
public class MealListAdapter extends BaseAdapter implements Filterable{

    // Constants
    public static final String DATE_FORMAT = "EEEE, LLL d";
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;
    private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;

    private LayoutInflater m_Inflater;
    private MyMealsFragment m_Fragment;

    private ArrayList<Object> m_Data = new ArrayList<Object>();
    private TreeSet<Integer> m_SeparatorsSet = new TreeSet<Integer>();

    private ArrayList<Object> m_FilteredData = new ArrayList<>();
    private TreeSet<Integer> m_FilteredSeparatorsSet = new TreeSet<Integer>();

    private MenuFilter m_Filter;

    public MealListAdapter(Activity activity, MyMealsFragment fragment) {
        m_Fragment = fragment;
        m_Inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    public void updateFilteredData(List<UserMeal> list) {

        // Clear the filtered set
        m_FilteredData.clear();
        m_FilteredSeparatorsSet.clear();

        // Map the dates to usermeals
        HashMap<String, HashMap<String, UserMeal>> dateMap = new HashMap<>();
        for (UserMeal meal : list) {
            String date = getDateString(meal.getDate());
            if (dateMap.containsKey(date)) {
                dateMap.get(date).put(meal.getTitle(), meal);
            } else {
                HashMap<String, UserMeal> mealMap = new HashMap<>();
                mealMap.put(meal.getTitle(), meal);
                dateMap.put(date, mealMap);
            }
        }

        // Create ordered list of headers (categories) and items (recipes)
        for (String d : getOrderedDates(dateMap.keySet())) {
            m_FilteredData.add(d);
            m_FilteredSeparatorsSet.add(m_FilteredData.size() - 1);

            for (Constants.UserMeals m : Constants.UserMeals.values()) {
                if (dateMap.get(d).containsKey(m.name())) {
                    m_FilteredData.add(dateMap.get(d).get(m.name()));
                }
            }
        }

        Log.d("^^^^", "m_Data size: " + Integer.toString(m_Data.size()));
        Log.d("^^^^", "m_FilteredData size: " + Integer.toString(m_FilteredData.size()));

        notifyDataSetChanged();
    }

    /*
     * Used by MenuFragment only
     *      Entirely new list of recipes
     */
    public void updateData(List<UserMeal> list) {

        // Clear the full data set
        m_Data.clear();
        m_SeparatorsSet.clear();

        // Clear the filtered data set
        m_FilteredData.clear();
        m_FilteredSeparatorsSet.clear();

        // Map the dates to usermeals
        HashMap<String, HashMap<String, UserMeal>> dateMap = new HashMap<>();
        for (UserMeal meal : list) {
            String date = getDateString(meal.getDate());
            if (dateMap.containsKey(date)) {
                dateMap.get(date).put(meal.getTitle(), meal);
            } else {
                HashMap<String, UserMeal> mealMap = new HashMap<>();
                mealMap.put(meal.getTitle(), meal);
                dateMap.put(date, mealMap);
            }
        }

        // Create ordered list of headers (categories) and items (recipes)
        for (String d : getOrderedDates(dateMap.keySet())) {
            //addSeparatorItem(d);
            m_Data.add(d);
            m_FilteredData.add(d);
            m_SeparatorsSet.add(m_Data.size() - 1);
            m_FilteredSeparatorsSet.add(m_FilteredData.size() - 1);

            for (Constants.UserMeals m : Constants.UserMeals.values()) {
                if (dateMap.get(d).containsKey(m.name())) {
                    //addItem(dateMap.get(d).get(m.name()));

                    m_Data.add(dateMap.get(d).get(m.name()));
                    m_FilteredData.add(dateMap.get(d).get(m.name()));
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        //return m_SeparatorsSet.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
        return m_FilteredSeparatorsSet.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
    }

    public int getCount() {
        return m_FilteredData.size();
    }

    public Object getItem(int position) {
        return m_FilteredData.get(position);
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
                final UserMeal meal = (UserMeal) m_FilteredData.get(position);
                rowView = m_Inflater.inflate(R.layout.mymeals_list_item, null);

                // Meal name: Breakfast, Lunch, Dinner, Snacks
                TextView name =(TextView) rowView.findViewById(R.id.meal_name);
                name.setText(meal.getTitle());

                // Meal icon
                ImageView icon = (ImageView) rowView.findViewById(R.id.meal_icon);
                icon.setImageDrawable(getMealIcon(meal));

                // List of recipes in the meal
                TextView items =(TextView) rowView.findViewById(R.id.meal_items_list);
                String list = "";
                int total = meal.getDiaryEntries().size();
                for (int i = 0; i < total; i++) {
                    DiaryEntry entry = meal.getDiaryEntries().get(i);
                    Recipe r = entry.getRecipe();
                    list += r.getName();
                    if (i != total - 1)
                        list += ", ";
                }
                items.setText(list);


                rowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        m_Fragment.onMealClick(meal);
                    }
                });

                break;

            case TYPE_SEPARATOR:
                String date = (String) m_Data.get(position);
                rowView = m_Inflater.inflate(R.layout.mymeals_date_header, null);
                TextView text = (TextView) rowView.findViewById(R.id.date_text);
                text.setText(date);
                break;
        }

        return rowView;
    }

    private Drawable getMealIcon(UserMeal meal) {
        Activity activity = m_Fragment.getActivity();
        switch (meal.getTitle()) {
            case "Breakfast":
                return activity.getResources().getDrawable(R.drawable.sunrise_filled);
            case "Lunch":
                return activity.getResources().getDrawable(R.drawable.sun_filled);
            case "Dinner":
                return activity.getResources().getDrawable(R.drawable.sunset_filled);
            default:
                return activity.getResources().getDrawable(R.drawable.clock);
        }
    }


    public static String getDateString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(date);
    }

    public List<String> getOrderedDates(Set<String> dates) {
        List<String> list = new ArrayList<>();
        for (String s : dates)
            list.add(s);

        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                try {
                    Date dateA = sdf.parse(a);
                    Date dateB = sdf.parse(b);
                    return dateB.compareTo(dateA);
                } catch (ParseException e) {
                    return 0;
                }
            }
        });

        return list;
    }

    @Override
    public Filter getFilter() {
        if (m_Filter == null) {
            m_Filter = new MenuFilter();
        }
        return m_Filter;
    }


    /**
     * Custom filter for friend list
     * Filter content in friend list according to the search text
     */
    private class MenuFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint != null && constraint.length() > 0) {
                List<UserMeal> tempList = new ArrayList<UserMeal>();

                // Search foods within the meal list
                for (int i = 0; i < m_Data.size(); i++) {
                    if (!m_SeparatorsSet.contains(i)) {
                        UserMeal meal = (UserMeal) m_Data.get(i);
                        for (DiaryEntry entry : meal.getDiaryEntries()) {
                            String name = entry.getRecipe().getName().toLowerCase();
                            if (name.contains(constraint.toString().toLowerCase())) {
                                tempList.add(meal);
                                break;
                            }
                        }
                    }
                }
                filterResults.count = tempList.size();
                filterResults.values = tempList;

            } else {
                filterResults.count = m_Data.size();
                filterResults.values = m_Data;
            }
            return filterResults;
        }

        /**
         * Notify about filtered list to ui
         * @param constraint text
         * @param results filtered result
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results != null && results.count < m_Data.size())
                updateFilteredData(((List<UserMeal>) results.values));
        }
    }

}