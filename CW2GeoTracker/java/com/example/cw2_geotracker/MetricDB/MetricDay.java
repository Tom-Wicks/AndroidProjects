package com.example.cw2_geotracker.MetricDB;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.DateFormat;
import java.util.Date;

//Metric item - Represents a day's worth of user metrics
@Entity(tableName = "metrics")
public class MetricDay {
    @PrimaryKey(autoGenerate = true)
    private int id = 0;

    @ColumnInfo(name = "date")
    private String date;

    //In meters
    @ColumnInfo(name = "distance")
    private double distance;

    @ColumnInfo(name = "reminders")
    private int reminders;

    //In seconds
    @ColumnInfo(name = "time")
    private double time;

    //In m/s
    @ColumnInfo(name = "speed")
    private double speed;

    public MetricDay(String date, double distance, int reminders, double time, double speed) {
        this.date = date;
        this.distance = distance;
        this.reminders = reminders;
        this.time = time;
        this.speed = speed;
    }

    public int getId() {
        return id;
    }
    public String getDate() {
        return date;
    }
    public double getDistance() {
        return distance;
    }
    public int getReminders() {
        return reminders;
    }
    public double getTime() {
        return time;
    }
    public double getSpeed() {
        return speed;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public void setDistance(double distance) {
        this.distance = distance;
    }
    public void setReminders(int reminders) {
        this.reminders = reminders;
    }
    public void setTime(double time) {
        this.time = time;
    }
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void addDistance(double distance) {
        this.distance += distance;
    }
    public void addTime(double time) {
        this.time += time;
    }
    public void addReminders(int reminders) {
        this.reminders += reminders;
    }
}
