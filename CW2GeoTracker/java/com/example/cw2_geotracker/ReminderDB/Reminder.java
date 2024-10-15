package com.example.cw2_geotracker.ReminderDB;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

//Reminder item - Represents a location-based reminder with a specific icon
@Entity(tableName = "reminders")
public class Reminder {
    @PrimaryKey(autoGenerate = true)
    private int id = 0;

    @ColumnInfo(name = "text")
    private String text;

    @ColumnInfo(name = "latitude")
    private double latitude;

    @ColumnInfo(name = "longitude")
    private double longitude;

    //Tag Values: default, important, shop, friend, exercise, attraction
    //Interpreted by the TagUtility
    @ColumnInfo(name = "tag")
    private String tag;

    public Reminder(String text, double latitude, double longitude, String tag) {
        this.text = text;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tag = tag;
    }

    public int getId() {
        return id;
    }
    public String getText() {
        return text;
    }
    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public String getTag() {
        return tag;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setText(String text) {
        this.text = text;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }
}
