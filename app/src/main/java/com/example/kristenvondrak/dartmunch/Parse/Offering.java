package com.example.kristenvondrak.dartmunch.Parse;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseRelation;

/**
 * Created by kristenvondrak on 1/10/16.
 */

@ParseClassName("Offering")
public class Offering extends ParseObject {


    // @TODO: Recipes relation

    public ParseRelation getRecipes() {
        return getRelation("recipes");
    }

    public String getUUID() {
        return getString("uuid");
    }

    public int getMonth() {
        return getInt("month");
    }

    public int getDay() {
        return getInt("day");
    }

    public int getYear() {
        return getInt("year");
    }

    public String getVenueKey() {
        return getString("venueKey");
    }

    public String getMealName() {
        return getString("mealName");
    }

    public String getMenuName() {
        return getString("menuName");
    }


    public void setDay(String value) {
        put("day", value);
    }

    public void setMonth(String value) {
        put("month", value);
    }

    public void setYear(String value) {
        put("year", value);
    }

    public void setMealName(String value) {
        put("mealName", value);
    }

    public void setVenueKey(String value) {
        put("venueKey", value);
    }

    public void setMenuName(String value) {
        put("menuName", value);
    }

    public void setRecipes(ParseRelation value) {
        put("recipes", value);
    }

}
