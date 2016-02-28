package com.example.kristenvondrak.dartmunch.Parse;

import com.parse.ParseClassName;
import com.parse.ParseRelation;
import com.parse.ParseUser;

/**
 * Created by kristenvondrak on 1/10/16.
 */
@ParseClassName("_User")
public class User extends ParseUser {

    // Nutrients is a dictionary whose key type is a dictionary.
    // Look at Parse data browser to see how the JSON is formatted.
    // For the inner Dictionary, the value is of type NSObject because
    // it can be a string or an int, depending on the key.

    public boolean getEmailVerified() {
        return getBoolean("emailVerified");
    }

    public int getGoalDailyCalories() {
        return getNumber("goalDailyCalories").intValue();
    }

    public ParseRelation getPastRecipes() {
        return (ParseRelation) get("pastRecipes");
    }

    public String getUsername() {
        return getString("username");
    }

    public String getPassword() {
        return getString("password");
    }

    public String getEmail() {
        return getString("email");
    }

    public void setEmailVerified(boolean value) {
        put("emailVerified", value);
    }

    public void setGoalDailyCalories(Number value){
        put("goalDailyCalories", value);
    }

    public void setPastRecipes(ParseRelation value){
        put("pastRecipes", value);
    }

}
