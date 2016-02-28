package com.example.kristenvondrak.dartmunch.Parse;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.HashMap;

/**
 * Created by kristenvondrak on 1/10/16.
 */
@ParseClassName("Recipe")
public class Recipe extends ParseObject {

    public static class Fields {
        public static String NutrientResults = "result";
        public static String Calories = "calories";
        public static String FatCals = "calfat";
        public static String TotalFat = "fat";
        public static String SaturatedFat = "sfa";
        public static String Cholesterol = "cholestrol";
        public static String Sodium = "sodium";
        public static String TotalCarbs = "carbs";
        public static String Fiber = "fiberdtry";
        public static String Sugars = "sugars";
        public static String Protein = "protein";
        public static String ServingSizeGrams = "serving_size_grams";
        public static String ServingSizeText = "serving_size_text";
    }

    // Nutrients is a dictionary whose key type is a dictionary.
    // Look at Parse data browser to see how the JSON is formatted.
    // For the inner Dictionary, the value is of type NSObject because
    // it can be a string or an int, depending on the key.

    public int getDartmouthId() {
        return getInt("dartmouthId");
    }


    public String getName() {
        return getString("name");
    }

    public String getCreatedBy() {
        return getString("createdBy");
    }

    public int getRank() {
        return getInt("rank");
    }

    public HashMap<String, HashMap<String, Object>> getNutrients() {
        return (HashMap<String, HashMap<String, Object>>) get("nutrients");
    }

    public String getUUID() {
        return getString("uuid");
    }

    public String getCategory() {
        return getString("category");
    }

    public void setDartmouthId(int value) {
        put("dartmouthId", value);
    }

    public void setName(String value){
        put("name", value);
    }

    public void setRank(int value) {
        put("rank", value);
    }

    public void setNutrients(HashMap<String, HashMap<String, Object>> value) {
        put("nutrients", value);
    }

    public void setCreatedBy(ParseUser value) {
        put("createdBy", value);
    }

    public void setCategory(String value) {
        put("category", value);
    }

    public String getCalories() {
        return (String) getNutrients().get(Fields.NutrientResults).get(Fields.Calories);
    }

    public String getSugars() {
        return (String) getNutrients().get(Fields.NutrientResults).get(Fields.Sugars);
    }

    public String getFiber() {
        return (String) getNutrients().get(Fields.NutrientResults).get(Fields.Fiber);
    }

    public String getTotalCarbs() {
        return (String) getNutrients().get(Fields.NutrientResults).get(Fields.TotalCarbs);
    }

    public String getSodium() {
        return (String) getNutrients().get(Fields.NutrientResults).get(Fields.Sodium);
    }

    public String getCholestrol() {
        return (String) getNutrients().get(Fields.NutrientResults).get(Fields.Cholesterol);
    }

    public String getSaturatedFat() {
        return (String) getNutrients().get(Fields.NutrientResults).get(Fields.SaturatedFat);
    }

    public String getTotalFat() {
        return (String) getNutrients().get(Fields.NutrientResults).get(Fields.TotalFat);
    }

    public String getFatCalories() {
        return (String) getNutrients().get(Fields.NutrientResults).get(Fields.FatCals);
    }

    public String getProtein() {
        return (String) getNutrients().get(Fields.NutrientResults).get(Fields.Protein);
    }

    public String getServingSize() {
        Integer value =  (Integer) getNutrients().get(Fields.NutrientResults).get(Fields.ServingSizeGrams);
        if (value == null)
            return "";
        return Integer.toString(value) + " g";
    }

    public String getServingText() {
        return (String) getNutrients().get(Fields.NutrientResults).get(Fields.ServingSizeText);
    }

    public String getSfa() {
        return (String) getNutrients().get(Fields.NutrientResults).get(Fields.SaturatedFat);
    }


    public String getServingsPerContainer() {
        return (String) getNutrients().get(Fields.NutrientResults).get("servings_per_container");
    }


    public String getThiamin() {
        return (String) getNutrients().get(Fields.NutrientResults).get("thiamin");
    }

    public String getZinc() {
        return (String) getNutrients().get(Fields.NutrientResults).get("zinc");
    }

    public String getVitb6() {
        return (String) getNutrients().get(Fields.NutrientResults).get("vitb6");
    }

    public String getVitb12() {
        return (String) getNutrients().get(Fields.NutrientResults).get("vitb12");
    }

    public String getVitc() {
        return (String) getNutrients().get(Fields.NutrientResults).get("vitc");
    }

    public String getVitaiu() {
        return (String) getNutrients().get(Fields.NutrientResults).get("vita_iu");
    }

    public String getCalcium() {
        return (String) getNutrients().get(Fields.NutrientResults).get("calcium");
    }

    public String getTransFat() {
        return (String) getNutrients().get(Fields.NutrientResults).get("fatrans");
    }

    public String getFolacin() {
        return (String) getNutrients().get(Fields.NutrientResults).get("folacin");
    }

    public String getIron() {
        return (String) getNutrients().get(Fields.NutrientResults).get("iron");
    }

    public String getMufa() {
        return (String) getNutrients().get(Fields.NutrientResults).get("mufa");
    }

    public String getNiacin() {
        return (String) getNutrients().get(Fields.NutrientResults).get("niacin");
    }

    public String getPhosphorus() {
        return (String) getNutrients().get(Fields.NutrientResults).get("phosphorus");
    }

    public String getPotassium() {
        return (String) getNutrients().get(Fields.NutrientResults).get("potassium");
    }

    public String getPufa() {
        return (String) getNutrients().get(Fields.NutrientResults).get("pufa");
    }

    public String getRiboflavin() {
        return (String) getNutrients().get(Fields.NutrientResults).get("riboflavin");
    }

}
