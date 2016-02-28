package com.example.kristenvondrak.dartmunch.Parse;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;

/**
 * Created by kristenvondrak on 1/11/16.
 */
@ParseClassName("DiaryEntry")
public class DiaryEntry extends ParseObject {


    public Date getDate() {
        return getDate("date");
    }

    public ParseUser getUser() {
        return (ParseUser)get("user");
    }

    public Recipe getRecipe() {
        return (Recipe)get("recipe");
    }

    public float getServingsMultiplier() {
        return getNumber("servingsMultiplier").floatValue();
    }

    public void getTotalNutrients() {
        // TODO: map-> Dictionary<String, NSObject>
    }

    public int getTotalCalories() {
        return (int) ((Integer.parseInt(getRecipe().getCalories())) * getServingsMultiplier());
    }

    public void setDate(Date value) {
        put("date", value);
    }

    public void setUser(ParseUser value) {
        put("user", value);
    }

    public void setRecipe(Recipe value) {
        put("recipe", value);
    }

    public void setServingsMultiplier(float value) {
        put("servingsMultiplier", value);
    }

}
