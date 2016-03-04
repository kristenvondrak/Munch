package com.example.kristenvondrak.dartmunch.Diary;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.kristenvondrak.dartmunch.Diary.RecipeFragment;
import com.example.kristenvondrak.dartmunch.Parse.Recipe;
import com.example.kristenvondrak.dartmunch.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kristenvondrak on 1/24/16.
 */
public class RecipeListAdapter extends BaseAdapter implements Filterable {

    // Views
    private LayoutInflater m_Inflater;
    private RecipeFragment m_Fragment;

    private List<Recipe> m_List = new ArrayList<>();
    private List<Recipe> m_FilteredList = new ArrayList<>();
    private RecipeFilter m_Filter;

    public RecipeListAdapter(Activity activity, RecipeFragment fragment, List<Recipe> list) {
        m_Fragment = fragment;
        m_Inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        m_List.addAll(list);
        m_FilteredList.addAll(list);
    }

    public void update(List<Recipe> list) {
        m_List.clear();
        m_FilteredList.clear();
        for (Recipe r : list) {
            m_List.add(r);
            m_FilteredList.add(r);

        }
        notifyDataSetChanged();
    }

    public int getCount() {
        return m_FilteredList.size();
    }

    public Object getItem(int position) {
        return m_FilteredList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Recipe recipe = m_FilteredList.get(position);

        View rowView = m_Inflater.inflate(R.layout.recipe_list_item, null);
        TextView name =(TextView) rowView.findViewById(R.id.item_name_text_view);
        name.setText(recipe.getName());

        TextView cals =(TextView) rowView.findViewById(R.id.item_cals_text_view);
        cals.setText(recipe.getCalories() + " cals");

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_Fragment.onItemClick(recipe);
            }
        });

        return rowView;
    }

    @Override
    public Filter getFilter() {
        if (m_Filter == null) {
            m_Filter = new RecipeFilter();
        }
        return m_Filter;
    }


    /**
     * Custom filter for friend list
     * Filter content in friend list according to the search text
     */
    private class RecipeFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint != null && constraint.length() > 0) {
                List<Recipe> tempList = new ArrayList<Recipe>();

                // Search content in recipe list
                for (Recipe recipe : m_List) {
                    if (recipe.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempList.add(recipe);
                    }
                }
                filterResults.count = tempList.size();
                filterResults.values = tempList;

            } else {
                filterResults.count = m_List.size();
                filterResults.values = m_List;
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
            if (results != null && results.count < m_List.size()) {
                m_FilteredList.clear();
                m_FilteredList.addAll((List<Recipe>) results.values);
                notifyDataSetChanged();
            }

        }
    }
}