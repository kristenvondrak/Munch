package com.example.kristenvondrak.dartmunch.Database;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.kristenvondrak.dartmunch.R;

import java.util.List;

/**
 * Created by kristenvondrak on 2/11/16.
 */
public class DatabaseListAdapter extends BaseAdapter {

    private DatabaseFragment m_Fragment;
    private List<DatabaseRecipe> m_List;
    private LayoutInflater m_Inflater;


    public DatabaseListAdapter(Activity activity, DatabaseFragment fragment, List<DatabaseRecipe> list) {
        m_Fragment = fragment;
        m_List = list;
        m_Inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
      /*  if (getCount() == 0) {
            m_Fragment.resetInitialSearchView(true);
        }*/
    }

    @Override
    public int getCount() {
        return m_List.size();
    }

    @Override
    public Object getItem(int position) {
        return m_List.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final DatabaseRecipe dbRecipe = m_List.get(position);

        View rowView = m_Inflater.inflate(R.layout.dbrecipe_list_item, null);
        TextView name =(TextView) rowView.findViewById(R.id.item_name_text_view);
        name.setText(dbRecipe.getName());


        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               m_Fragment.onItemClick(dbRecipe);
            }
        });

        return rowView;
    }


}