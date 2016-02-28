package com.example.kristenvondrak.dartmunch.Main;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;

import com.example.kristenvondrak.dartmunch.R;


public class SearchResultsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
        }
    }



    // ------------------------------------------------------------------------------------ Search
/*
    @Override
    public final void onSearchClick() {
        resetSearch();
    }

    @Override
    public final void onCancelSearchClick() {
        clearSearch();
    }

    @Override
    public void onClearSearchClick() {
        clearSearch();
        resetSearch();
    }

    @Override
    public void onEnterClick() {
        // do nothing since we update as user types
    }

    @Override
    public final void onSearchEditTextChanged(String text, int start, int before) {

        // If the part of the input was deleted, search again from original list
        List<Recipe> listToSearch;
        if (start < before) {
            listToSearch = m_RestoredList;
            // Otherwise, search from restricted list
        } else {
            listToSearch = m_MenuItemsList;
        }

        // Substring match
        updateSearch(listToSearch, text);

    }

    private void resetSearch() {
        SEARCH_MODE = true;
        m_RestoredList = Utils.copyRecipeList(m_MenuItemsList);
    }

    private void clearSearch() {
        SEARCH_MODE = false;
        m_RestoredList.clear();

        // Exit search and restore previous items
        update();
    }


    public void updateSearch(List listToSearch, String text) {

        if (text == null)
            return;

        // Fragment initialized with search already open
        if (listToSearch == null) {
            resetSearch();
            listToSearch = m_MenuItemsList;
        }

        // List of results that contain substring
        ArrayList<Recipe> searchResults = new ArrayList<>();
        for (Object item : listToSearch) {
            Recipe r = (Recipe) item;
            String name = r.getName().toLowerCase();
            if (name.contains(text)) {
                searchResults.add(r);
            }
        }

        // Update the list and notify adapter
        m_MenuItemsList = searchResults;
        notifyMenuListAdapter();
    }

    private String getSearchText() {
        String text;
        try {
            text = (m_Mode == MODE.DIARY) ?
                    ((AddUserMealActivity)m_Activity).getSearchText() :
                    m_SearchEditText.getText().toString().toLowerCase().trim();
        } catch (NullPointerException e) {
            text = "";
        }
        return text;
    }*/


}
