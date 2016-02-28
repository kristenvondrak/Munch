package com.example.kristenvondrak.dartmunch.Parse;

import com.example.kristenvondrak.dartmunch.Main.Utils;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by kristenvondrak on 1/10/16.
 */
public class ParseAPI {

    public static ParseUser getCurrentParseUser() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            // do stuff with the user
        } else {
            // show the signup or login screen
        }
        return currentUser;
    }

    public static void logOutParseUser() {
        ParseUser.logOut();
        ParseUser currentUser = ParseUser.getCurrentUser(); // this will now be null
    }

    public static void addDiaryEntry(final Calendar cal, final ParseUser user, final Recipe recipe,
                                                        float servings, final String userMeal) {

        // Add recipe to users past recipes
        ParseRelation<ParseObject> relation = user.getRelation("pastRecipes");
        relation.add(recipe);
        user.saveInBackground();

        // Create new diary entry and save to parse
        final DiaryEntry diaryEntry = new DiaryEntry();
        diaryEntry.setDate(cal.getTime());
        diaryEntry.setUser(user);
        diaryEntry.setRecipe(recipe);
        diaryEntry.setServingsMultiplier(servings);
        diaryEntry.saveInBackground();

        List<DiaryEntry> entries = new ArrayList<>();
        entries.add(diaryEntry);
        addUserMeal(cal, user, entries, userMeal);
    }

    public static void addDiaryEntries(final Calendar cal, final ParseUser user,
                                       final List<DiaryEntry> entries, final String userMeal) {


        ParseRelation<ParseObject> pastRecipesRelation = user.getRelation("pastRecipes");
        List<DiaryEntry> diaryEntriesList = new ArrayList<>();
        for (DiaryEntry entry : entries) {
            // Add to user's past recipes
            pastRecipesRelation.add(entry.getRecipe());

            final DiaryEntry diaryEntry = new DiaryEntry();
            diaryEntry.setDate(cal.getTime());
            diaryEntry.setUser(user);
            diaryEntry.setRecipe(entry.getRecipe());
            diaryEntry.setServingsMultiplier(entry.getServingsMultiplier());
            diaryEntry.saveInBackground();
            diaryEntriesList.add(diaryEntry);
        }
        user.saveInBackground();
        addUserMeal(cal, user, diaryEntriesList, userMeal);
    }

    public static void addUserMeal(final Calendar cal, final ParseUser user, final List<DiaryEntry> entries,
                                     final String userMeal) {

        // Check if user meal exists
        ParseQuery<ParseObject> query = ParseQuery.getQuery("UserMeal");
        query.whereGreaterThan("date", Utils.getDateBefore(cal));
        query.whereLessThan("date", Utils.getDateAfter(cal));
        query.whereEqualTo("user", user);
        query.whereEqualTo("title", userMeal);

        query.findInBackground(new FindCallback<ParseObject>() {

            public void done(List<ParseObject> meals, ParseException e) {

                if (e == null && meals.size() > 0) {

                    // Add diary entries to already existing UserMeal
                    UserMeal meal = (UserMeal) meals.get(0);
                    for (DiaryEntry entry : entries) {
                        meal.addDiaryEntry(entry);

                    }
                    meal.saveInBackground();

                } else {

                    // Add new UserMeal to parse
                    UserMeal newmeal = new UserMeal();
                    newmeal.put("date", cal.getTime());
                    newmeal.put("user", user);
                    newmeal.put("title", userMeal);
                    newmeal.put("entries", entries);
                    newmeal.saveInBackground();
                }
            }
        });
    }
}
