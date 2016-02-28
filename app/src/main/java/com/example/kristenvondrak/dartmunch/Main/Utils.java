package com.example.kristenvondrak.dartmunch.Main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.kristenvondrak.dartmunch.Parse.Recipe;
import com.example.kristenvondrak.dartmunch.Parse.UserMeal;
import com.example.kristenvondrak.dartmunch.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by kristenvondrak on 2/10/16.
 */
public class Utils {

    public static AlertDialog createAlertDialog(Activity activity, String title, String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.BasicAlertDialogStyle);

        LayoutInflater inflater = activity.getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_basic, null);

        TextView titleView = (TextView) v.findViewById(R.id.title);
        titleView.setText(title);

        TextView messageView = (TextView) v.findViewById(R.id.message);
        messageView.setText(message);

        builder.setView(v)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // close
                    }
                });
        return builder.create();
    }


    public static boolean isValidEmail(String email) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(Constants.Validation.EmailRegex);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    public static boolean isValidPassword(String password) {
        return (password.length() >= Constants.Validation.MinimumPasswordLength &&
                password.length() <= Constants.Validation.MaximumPasswordLength);
    }

    protected boolean isValidConfirmation(String first, String second) {
        return first.equals(second);
    }

    public static int getServingsFracIndex(float value) {
        for (int i = 0; i < Constants.ServingsFracFloats.size(); i++) {
            if (value == Constants.ServingsFracFloats.get(i))
                return i;
            else if (value < Constants.ServingsFracFloats.get(i)) {
                float d1 = Constants.ServingsFracFloats.get(i) - value;
                float d2 = value - Constants.ServingsFracFloats.get(i - 1);

                return d1 < d2 ? i : i - 1;
            }
        }
        return 0;
    }

    public static Date getDateBefore(Calendar calendar) {
        Calendar c = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        c.set(year, month, day, 23, 59, 59);
        c.add(Calendar.DAY_OF_MONTH, -1);
        return c.getTime();
    }

    public static Date getDateAfter(Calendar calendar) {
        Calendar c = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        c.set(year, month, day, 0, 0, 0);
        c.add(Calendar.DAY_OF_MONTH, 1);
        return c.getTime();
    }

    public static String getDisplayStringFromCal(Calendar cal) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.US);
        return sdf.format(cal.getTime());
    }


    public static String getStringExtraFromCal(Calendar cal) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_EXTRA, Locale.US);
        return sdf.format(cal.getTime());
    }

    public static List<Recipe> copyRecipeList(List<Recipe> list) {
        List<Recipe> copy = new ArrayList<>();
        for (Recipe r : list) {
            copy.add(r);
        }
        return copy;
    }

    public static List<UserMeal> copyMealsList(List<UserMeal> list) {
        List<UserMeal> copy = new ArrayList<>();
        for (UserMeal r : list) {
            copy.add(r);
        }
        return copy;
    }

    public static void showProgressSpinner(ProgressBar view) {

        Drawable d = view.getIndeterminateDrawable();
        if (d != null)
            d.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);

        view.setVisibility(View.VISIBLE);
        view.bringToFront();

    }

    public static void hideProgressSpinner(ProgressBar view) {
        view.setVisibility(View.GONE);
        view.bringToFront();
    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm =
                    (InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static String toSentence(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1) + ".";
    }


    public static int mealTimeToUserMealIndex(Constants.MealTime mealTime) {
        Constants.UserMeals userMeal = Constants.userMealForMealTime.get(mealTime);
        Constants.UserMeals[] array = Constants.UserMeals.values();
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(userMeal))
                return i;
        }
        return 0;
    }
}
