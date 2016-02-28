package com.example.kristenvondrak.dartmunch.Parse;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by kristenvondrak on 1/10/16.
 */
public class ParseApplication extends Application {

    public static final String APPLICATION_ID = "BAihtNGpVTx4IJsuuFV5f9LibJGnD1ZBOsnXk9qp";
    public static final String CLIENT_KEY = "TRnSXKYLvWENuPULgil1OtMbTS8BBxfkhV5kcQlz";

    @Override
    public void onCreate() {
        super.onCreate();
        // Add your initialization code here
        ParseObject.registerSubclass(UserMeal.class);
        ParseObject.registerSubclass(DiaryEntry.class);
        ParseObject.registerSubclass(Offering.class);
        ParseObject.registerSubclass(Recipe.class);
        ParseObject.registerSubclass(User.class);
        Parse.initialize(this, APPLICATION_ID, CLIENT_KEY);

    }
}
