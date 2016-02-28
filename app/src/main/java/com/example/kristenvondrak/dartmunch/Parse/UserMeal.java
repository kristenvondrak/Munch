package com.example.kristenvondrak.dartmunch.Parse;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by kristenvondrak on 1/11/16.
 */
@ParseClassName("UserMeal")
public class UserMeal extends ParseObject {

    public String getTitle() {
        return getString("title");
    }

    public Date getDate() {
        return getDate("date");
    }

    public ParseUser getUser() {
        return (ParseUser)get("user");
    }

    public List<DiaryEntry> getDiaryEntries() {
        return (List<DiaryEntry>)get("entries");
    }

    public void getTotalNutrients() {
        // TODO: map
    }


    public void setTitle(String value) {
        put("title", value);
    }

    public void setDate(Date value) {
        put("date", value);
    }

    public void setUser(ParseUser value) {
        put("user", value);
    }

    public void setDiaryEntries(List<DiaryEntry> value) {
        put("entries", value);
    }

    public void addDiaryEntry(DiaryEntry value) {
        List<DiaryEntry> copy = getDiaryEntries();
        copy.add(value);
        setDiaryEntries(copy);
    }

    public void removeDiaryEntry(DiaryEntry value) {
        List<DiaryEntry> newList = new ArrayList<>();
        for (DiaryEntry entry : getDiaryEntries()) {
            if (!entry.getObjectId().equals(value.getObjectId()))
                newList.add(entry);
        }
        setDiaryEntries(newList);
    }

}

