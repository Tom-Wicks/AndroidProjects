package com.example.cw2_geotracker.ExerciseDB;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//Represents a specific exercise session started and ended by the user, with added info
@Entity(tableName = "exercises")
public class Exercise implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id = 0;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "date")
    private String date;

    @ColumnInfo(name = "distance")
    private double distance;

    @ColumnInfo(name = "time")
    private double time;

    @ColumnInfo(name = "speed")
    private double speed;

    //This is converted to string by type converters
    @ColumnInfo(name = "movement")
    private List<GeoPoint> movement;

    @ColumnInfo(name = "type")
    private String type;

    @ColumnInfo(name = "weather")
    private String weather;

    @ColumnInfo(name = "temperature")
    private String temperature;

    @ColumnInfo(name = "notes")
    private String notes;

    //For ease of use, the latter three values are left alone for now, to be modified on the info screen
    public Exercise(String name, String date, String type) {
        this.name = name;
        this.date = date;
        this.distance = 0;
        this.time = 0;
        this.speed = 0;
        this.movement = new ArrayList<>();
        this.type = type;
        this.weather = "clear";
        this.temperature = "neutral";
        this.notes = "";
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDate() {
        return date;
    }
    public double getDistance() {
        return distance;
    }
    public double getTime() {
        return time;
    }
    public double getSpeed() {
        return speed;
    }
    public List<GeoPoint> getMovement() {
        return movement;
    }
    public String getType() {
        return type;
    }
    public String getWeather() {
        return weather;
    }
    public String getTemperature() {
        return temperature;
    }
    public String getNotes() {
        return notes;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public void setDistance(double distance) {
        this.distance = distance;
    }
    public void setTime(double time) {
        this.time = time;
    }
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    public void setMovement(List<GeoPoint> movement) {
        this.movement = movement;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setWeather(String weather) {
        this.weather = weather;
    }
    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void addDistance(double distance) {
        this.distance += distance;
    }
    public void addTime(double time) {
        this.time += time;
    }
    public void addMovement(GeoPoint move) {
        this.movement.add(move);
    }
}
