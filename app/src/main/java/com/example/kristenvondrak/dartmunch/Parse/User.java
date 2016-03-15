package com.example.kristenvondrak.dartmunch.Parse;

import com.parse.ParseClassName;
import com.parse.ParseRelation;
import com.parse.ParseUser;

/**
 * Created by kristenvondrak on 1/10/16.
 */
@ParseClassName("_User")
public class User extends ParseUser {

    public static final int UNKNOWN = -1;

    public boolean getEmailVerified() {
        return getBoolean("emailVerified");
    }

    public int getGoalCalories() {
        Number cals = getNumber("goalCalories");
        return  cals != null ? cals.intValue() : UNKNOWN;
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

    public int getAge() {
        Number age = getNumber("age");
        return  age != null ? age.intValue() : UNKNOWN;
    }

    public int getGender() {
        Number gender = getNumber("gender");
        return  gender != null ? gender.intValue() : UNKNOWN;
    }

    public int getHeight() {
        Number height = getNumber("height");
        return height != null ? height.intValue() : UNKNOWN;
    }

    public int getWeight() {
        Number weight = getNumber("weight");
        return weight != null ? weight.intValue() : UNKNOWN;
    }

    public int getActivityLevel() {
        Number act = getNumber("activity");
        return act != null ? act.intValue() : UNKNOWN;
    }

    public float getGoalProtein() {
        Number protein = getNumber("goalProtein");
        return protein != null ? protein.floatValue() : 30;
    }

    public float getGoalCarbs() {
        Number carbs = getNumber("goalCarbs");
        return carbs != null ? carbs.floatValue() : 50;
    }

    public float getGoalFat() {
        Number fat = getNumber("goalFat");
        return fat != null ? fat.floatValue() : 20;
    }

    public float getGoal() {
        Number goal = getNumber("goal");
        return goal != null ? goal.floatValue() : 0;
    }

    public void setGender(Number value){
        put("gender", value);
    }

    public void setAge(Number value){
        put("age", value);
    }

    public void setHeight(Number value){
        put("height", value);
    }

    public void setWeight(Number value){
        put("weight", value);
    }

    public void setActivityLevel(Number value){
        put("activity", value);
    }

    public void setGoalProtein(Float value){
        put("goalProtein", value);
    }

    public void setGoalCarbs(Float value){
        put("goalCarbs", value);
    }

    public void setGoalFat(Number value){
        put("goalFat", value);
    }

    public void setGoal(Float value){
        put("goal", value);
    }

    public void setPastRecipes(ParseRelation value){
        put("pastRecipes", value);
    }

    public void setEmailVerified(boolean value) {
        put("emailVerified", value);
    }

    public void setGoalCalories(Number value){
        put("goalCalories", value);
    }


}
