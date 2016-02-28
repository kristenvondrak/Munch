package com.example.kristenvondrak.dartmunch.Menu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.kristenvondrak.dartmunch.Parse.Recipe;
import com.example.kristenvondrak.dartmunch.Parse.User;
import com.example.kristenvondrak.dartmunch.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by kristenvondrak on 1/24/16.
 */
public class MenuItemListAdapter extends BaseAdapter implements Filterable{

    // Constants
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;
    private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;

    // Views
    private LayoutInflater m_Inflater;
    private MenuFragment m_Fragment;

    private ArrayList<Object> m_Data = new ArrayList<Object>();
    private TreeSet<Integer> m_SeparatorsSet = new TreeSet<Integer>();

    private ArrayList<Object> m_FilteredData = new ArrayList<>();
    private TreeSet<Integer> m_FilteredSeparatorsSet = new TreeSet<Integer>();

    private MenuFilter m_Filter;

    public MenuItemListAdapter(Activity activity, MenuFragment fragment) {
        m_Fragment = fragment;
        m_Inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    public void updateFilteredData(List<Recipe> list) {

        // Clear the filtered set
        m_FilteredData.clear();
        m_FilteredSeparatorsSet.clear();

        // Map the categories to recipes
        HashMap<String, List<Recipe>> categoryMap = new HashMap<>();
        for (Recipe r : list) {
            if (categoryMap.containsKey(r.getCategory()))
                categoryMap.get(r.getCategory()).add(r);
            else {
                List<Recipe> set = new ArrayList<>();
                set.add(r);
                categoryMap.put(r.getCategory(), set);
            }
        }

        // Create ordered list of headers (categories) and items (recipes)
        for (String category : categoryMap.keySet()) {
            m_FilteredData.add(category);
            m_FilteredSeparatorsSet.add(m_FilteredData.size() - 1);
            //addFilteredSeparatorItem(category);
            for (Recipe recipe : categoryMap.get(category)) {
                m_FilteredData.add(recipe);
                //addFilteredItem(recipe);
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
    public void updateData(List<Recipe> list) {
        // Clear the full data set
        m_Data.clear();
        m_SeparatorsSet.clear();

        // Clear the filtered data set
        m_FilteredData.clear();
        m_FilteredSeparatorsSet.clear();

        // Map the categories to recipes
        HashMap<String, List<Recipe>> categoryMap = new HashMap<>();
        for (Recipe r : list) {
            if (categoryMap.containsKey(r.getCategory()))
                categoryMap.get(r.getCategory()).add(r);
            else {
                List<Recipe> set = new ArrayList<>();
                set.add(r);
                categoryMap.put(r.getCategory(), set);
            }
        }

        // Create ordered list of headers (categories) and items (recipes)
        for (String category : categoryMap.keySet()) {
            m_Data.add(category);
            m_SeparatorsSet.add(m_Data.size() - 1);

            m_FilteredData.add(category);
            m_FilteredSeparatorsSet.add(m_FilteredData.size() - 1);

            //addSeparatorItem(category);
            for (Recipe recipe : categoryMap.get(category)) {
                m_Data.add(recipe);
                m_FilteredData.add(recipe);
                //addItem(recipe);
            }
        }

        // Initialized the filtered list
        //m_FilteredData.addAll(m_Data); ;
        //m_FilteredSeparatorsSet.addAll(m_SeparatorsSet);

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
                final Recipe recipe = (Recipe) m_FilteredData.get(position);

                // Set recipe name
                rowView = m_Inflater.inflate(R.layout.menu_list_item, null);
                TextView recipe_name = (TextView) rowView.findViewById(R.id.item_name_text_view);
                recipe_name.setText(recipe.getName());

                // Set recipe calories
                TextView cals = (TextView) rowView.findViewById(R.id.item_cals_text_view);
                cals.setText(recipe.getCalories() + " cals");

                // Set listener:  on click show nutrients
                rowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        m_Fragment.onMenuItemClick(recipe);
                    }
                });

                break;

            case TYPE_SEPARATOR:
                String category = (String) m_FilteredData.get(position);

                // Set category name
                rowView = m_Inflater.inflate(R.layout.menu_list_category, null);
                TextView category_name = (TextView) rowView.findViewById(R.id.category_name);
                category_name.setText(category);
                break;
        }

        return rowView;
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
                List<Recipe> tempList = new ArrayList<Recipe>();

                // Search content in recipe list
                for (int i = 0; i < m_Data.size(); i++) {
                    if (!m_SeparatorsSet.contains(i)) {
                        Recipe recipe = (Recipe) m_Data.get(i);
                        if (recipe.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            tempList.add(recipe);
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
                updateFilteredData(((List<Recipe>) results.values));
        }
    }



    public void addItem(Recipe item) {
        m_Data.add(item);
        notifyDataSetChanged();
    }

    public void addSeparatorItem(final String item) {
        m_Data.add(item);
        // save separator position
        m_SeparatorsSet.add(m_Data.size() - 1);
        notifyDataSetChanged();
    }

    public void addFilteredItem(Recipe item) {
        m_FilteredData.add(item);
        notifyDataSetChanged();
    }

    public void addFilteredSeparatorItem(final String item) {
        m_FilteredData.add(item);
        // save separator position
        m_FilteredSeparatorsSet.add(m_FilteredData.size() - 1);
        notifyDataSetChanged();
    }

}