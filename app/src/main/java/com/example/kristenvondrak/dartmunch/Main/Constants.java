package com.example.kristenvondrak.dartmunch.Main;


import com.example.kristenvondrak.dartmunch.R;
import com.parse.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kristenvondrak on 1/9/16.
 */
public class Constants {

    public static final int REQUEST_ADD_FROM_MENU = 0;
    public static final int REQUEST_ADD_FROM_DIARY = 1;
    public static final int REQUEST_EDIT_FOOD = 2;


    public static class Database {

        public static final String ApiKey = "yh3tgkw5HHJfqDNJkWhiKUdKsbeNRSf3wjc8VY6w";
        public static final String SearchBaseUrl = "http://api.nal.usda.gov/ndb/search";
        public static final String ReportsBaseUrl = "http://api.nal.usda.gov/ndb/reports";

        public static final class ParameterKeys {
            public static String ApiKey = "api_key";
            public static String SearchText = "q";
            public static String SortType = "sort";
            public static String MaxResultCount = "max";
            public static String ResultOffset = "offset";
            public static String ResponseFormat = "format";
            public static String NDBNumber = "ndbno";
            public static String ReportType = "type";
        }

        public static class ParameterValues {
            public static String Relevance = "r";
            public static String Name = "n";
            public static String JSON = "JSON";
            public static String Max = "50";
            public static String Offset = "0";
            public static String XML = "XML";
            public static String ReportBasic = "b";
            public static String ReportFull = "f";
            public static String ReportStats = "s";
        }

        public static final class ReportNutrients {
            public static final String Calories = "Energy";
            public static final String TotalFat = "Total lipid (fat)";
            public static final String SaturatedFat = "Fatty acids, total saturated";
            public static final String Cholesterol = "Cholesterol";
            public static final String Sodium = "Sodium, Na";
            public static final String TotalCarbs = "Carbohydrate, by difference";
            public static final String Fiber = "Fiber, total dietary";
            public static final String Sugar = "Sugars, total";
            public static final String Protein = "Protein";
        }
    }


    public static final String DATE_FORMAT_DISPLAY = "EEE, LLL d";
    public static final String DATE_FORMAT_STATS = "LLL d";

    public enum Venue {Foco, Hop, Novack, Collis};

    public enum MealTime {Breakfast, Lunch, Dinner, LateNight, AllDay};

    public enum Menu {AllItems, Specials, EverydayItems, Beverage, Cereal, Condiments, GlutenFree,
                        Deli, Grill, GrabGo, Snacks};


    public enum UserMeals {Breakfast, Lunch, Dinner, Snacks};

    public static final int[] MealTimeIcons = {
            R.drawable.sunrise_filled,
            R.drawable.sun_filled,
            R.drawable.sunset_filled,
            R.drawable.clock
    };

    public static final Map<Venue, MealTime[]> mealTimesForVenue = Collections.unmodifiableMap(
            new HashMap<Venue, MealTime[]>() {{
                put(Venue.Foco, new MealTime[]{
                        MealTime.Breakfast,
                        MealTime.Lunch,
                        MealTime.Dinner});


                put(Venue.Hop, new MealTime[]{
                        MealTime.Lunch,
                        MealTime.Dinner,
                        MealTime.LateNight});

                put(Venue.Collis, new MealTime[]{
                        MealTime.Breakfast,
                        MealTime.Lunch,
                        MealTime.Dinner});

                put(Venue.Novack, new MealTime[]{MealTime.AllDay});

            }});



    public static final Map<Venue, Menu[]> menusForVenue = Collections.unmodifiableMap(
            new HashMap<Venue, Menu[]>() {{
                put(Venue.Foco, new Menu[]{Menu.AllItems,
                        Menu.Specials,
                        Menu.EverydayItems,
                        Menu.GlutenFree,
                        Menu.Beverage,
                        Menu.Condiments});


                put(Venue.Hop, new Menu[]{Menu.AllItems,
                        Menu.Specials,
                        Menu.EverydayItems,
                        Menu.Deli,
                        Menu.Grill,
                        Menu.GrabGo,
                        Menu.Snacks,
                        Menu.Beverage,
                        Menu.Condiments});

                put(Venue.Collis, new Menu[]{Menu.AllItems,
                        Menu.EverydayItems,});


                put(Venue.Novack, new Menu[]{Menu.AllItems,
                        Menu.Specials,
                        Menu.EverydayItems,});
            }});

    public static final Map<MealTime, UserMeals> userMealForMealTime = Collections.unmodifiableMap(
            new HashMap<MealTime, UserMeals>() {{
                put(MealTime.Breakfast, UserMeals.Breakfast);
                put(MealTime.Lunch, UserMeals.Lunch);
                put(MealTime.Dinner, UserMeals.Dinner);
                put(MealTime.LateNight, UserMeals.Snacks);
                put(MealTime.AllDay, UserMeals.Snacks);
            }});

    public static final Map<Venue, String> venueDisplayStrings = Collections.unmodifiableMap(
            new HashMap<Venue, String>() {{
                put(Venue.Foco, "Foco");
                put(Venue.Hop, "Hop");
                put(Venue.Collis, "Collis");
                put(Venue.Novack, "Novack");
            }});

    public static final Map<Venue, String> venueParseStrings = Collections.unmodifiableMap(
            new HashMap<Venue, String>() {{
                put(Venue.Foco, "DDS");
                put(Venue.Hop, "CYC");
                put(Venue.Collis, "COLLIS");
                put(Venue.Novack, "NOVACK");
            }});

    public static final Map<MealTime, String> mealTimeDisplayStrings = Collections.unmodifiableMap(
            new HashMap<MealTime, String>() {{
                put(MealTime.Breakfast, "Breakfast");
                put(MealTime.Lunch, "Lunch");
                put(MealTime.Dinner, "Dinner");
                put(MealTime.LateNight, "Late Night");
                put(MealTime.AllDay, "All Day");
            }});

    public static final Map<MealTime, String[]> mealTimeParseStrings = Collections.unmodifiableMap(
            new HashMap<MealTime, String[]>() {{
                put(MealTime.Breakfast, new String[]{"Breakfast"});
                put(MealTime.Lunch, new String[]{"Lunch"});
                put(MealTime.Dinner, new String[]{"Dinner"});
                put(MealTime.LateNight, new String[]{"Late Night"});
                put(MealTime.AllDay, new String[]{"Every Day", "Everyday"});
            }});

    public static final Map<Menu, String> menuDisplayStrings = Collections.unmodifiableMap(
            new HashMap<Menu, String>() {{
                put(Menu.AllItems, "All Items");
                put(Menu.Specials, "Today's Specials");
                put(Menu.EverydayItems, "Everyday Items");
                put(Menu.Beverage, "Beverage");
                put(Menu.Cereal, "Cereal");
                put(Menu.Condiments, "Condiments");
                put(Menu.GlutenFree, "Gluten Free");
                put(Menu.EverydayItems, "Everyday Items");
                put(Menu.Deli, "Deli");
                put(Menu.Grill, "Grill");
                put(Menu.GrabGo, "Grab & Go");
                put(Menu.Snacks, "Snacks");
            }});

    public static final Map<Menu, String> menuParseStrings = Collections.unmodifiableMap(
            new HashMap<Menu, String>() {{
                put(Menu.AllItems, null);
                put(Menu.Specials, "Today's Specials");
                put(Menu.EverydayItems, "Everyday Items");
                put(Menu.Beverage, "Beverage");
                put(Menu.Cereal, "Cereal");
                put(Menu.Condiments, "Condiments");
                put(Menu.GlutenFree, "Additional Gluten Free");
                put(Menu.EverydayItems, "Everyday Items");
                put(Menu.Deli, "Courtyard Deli");
                put(Menu.Grill, "Courtyard Grill");
                put(Menu.GrabGo, "Grab & Go");
                put(Menu.Snacks, "Courtyard Snacks");
            }});


    public static class Validation {
        static int MinimumPasswordLength = 6;
        static int MaximumPasswordLength = 25;
        static String EmailRegex = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";

        //static String EmailRegex = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";

        static String InvalidEmailTitle = "Invalid Email";
        static String InvalidEmailMessage = "Please sign up with a valid email.";
        static String InvalidPasswordTitle = "Invalid Password";
        static String InvalidPasswordMessage = "Please enter a password between " +
                Integer.toString(6) +  " and " + Integer.toString(MaximumPasswordLength) +
                " characters.";
        static String NoMatchPasswordsTitle = "Passwords Don't Match";
        static String NoMatchPasswordsMessage = "Please correctly confirm your password.";
        static String SignupErrorTitle = "Error Signing Up";
        static String SignupErrorDefaultMessage = "Unknown error signing up.";
        static String SigninErrorTitle = "Error Logging In";
        static String SigninErrorDefaultMessage = "Unknown error signing in.";
        static String OkActionTitle = "OK";
    }


    public static final String[] ServingsFracDisplay = {
                "-",
                Character.toString('\u215B'),  // 1/8
                Character.toString('\u00BC'),  // 1/4
                Character.toString('\u2153'),  // 1/3
                Character.toString('\u00BD'),  // 1/2
                Character.toString('\u2154'),  // 2/3
                Character.toString('\u00BE')}; // 3/4


    public static final float[] ServingsFracFloats = {
                (float) 0,
                (float) 1.0 / 8,
                (float) 1.0 / 4,
                (float) 1.0 / 3,
                (float) 1.0 / 2,
                (float) 2.0 / 3,
                (float) 3.0 / 4};

    }
