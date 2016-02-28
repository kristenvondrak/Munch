package com.example.kristenvondrak.dartmunch.Menu;

public class Nutrients {

    public static int NONE = -1;

    public static int stripChars(String string) {
        String digits = "";
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (Character.isDigit(c) || c == '.'){
                digits += string.charAt(i);
            }
        }
        if (digits.equals(""))
            return NONE;

        return (int) Double.parseDouble(digits);
    }

    public static String addSpace(String string) {
        for (int i = 0; i < string.length(); i++) {
            if (!Character.isDigit(string.charAt(i)))
                return string.substring(0, i) + " " + string.substring(i, string.length());
        }
        return string;
    }

    public static double convertToDouble(String string) {
        // Check for empty string
        if (string == null) { return NONE; }

        // Extract digits
        String s = "";
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (Character.isDigit(c) || c == '.')
                s += c;
        }


        if (s.equals(""))
            return NONE;

        return Double.parseDouble(s);
    }

    public static String getUnits(String string) {
        int start = string.length();
        for (int i = 0; i < string.length(); i++) {
            if (Character.isDigit(string.charAt(i))) {
                start = i;
                break;
            }
        }

        // Extract units
        for (int j = start; j < string.length(); j++) {
            if (Character.isLetter(string.charAt(j)))
                return (string.substring(j, string.length()));
        }
        return "";
    }
}
