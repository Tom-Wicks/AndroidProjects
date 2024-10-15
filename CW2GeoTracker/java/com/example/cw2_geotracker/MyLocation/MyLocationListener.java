package com.example.cw2_geotracker.MyLocation;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.cw2_geotracker.ExerciseDB.Exercise;
import com.example.cw2_geotracker.MetricDB.MetricDay;
import com.example.cw2_geotracker.MetricDB.MetricDayDao;
import com.example.cw2_geotracker.MetricDB.MetricDayDatabase;
import com.example.cw2_geotracker.R;
import com.example.cw2_geotracker.ReminderDB.Reminder;
import com.example.cw2_geotracker.ReminderDB.ReminderDao;
import com.example.cw2_geotracker.ReminderDB.ReminderDatabase;

import org.osmdroid.util.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

//Location listener, which handles metrics and reminders on each location update
public class MyLocationListener implements LocationListener {
    ReminderDatabase reminderDb;
    ReminderDao reminderDao;
    MetricDayDatabase metricDb;
    MetricDayDao metricDao;
    MetricDay todayRecord;
    MyLocationService locationService;
    //Remember last reminders, to avoid repetitive notifications
    List<Reminder> lastReminders;
    Context context;
    //For calculations
    Location lastPos;
    long lastTime;

    private SharedPreferences sharedPreferences;
    private String preFile = "com.example.cw2_geotracker";

    public MyLocationListener(Context context) {
        //Context should be the location service
        if (context.getClass() != MyLocationService.class) {
            throw new RuntimeException("Error: Location listener has incorrect context");
        }
        locationService = (MyLocationService) context;

        reminderDb = ReminderDatabase.getDatabase(context);
        reminderDao = reminderDb.reminderDao();
        metricDb = MetricDayDatabase.getDatabase(context);
        metricDao = metricDb.metricDayDao();

        lastReminders = new ArrayList<>();
        this.context = context;
        sharedPreferences = context.getSharedPreferences(preFile, MODE_PRIVATE);

        lastPos = null;
        lastTime = -1;

        //Get current date, informed by https://developer.android.com/reference/android/icu/text/SimpleDateFormat
        //Available under Apache 2.0 license
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date todayDate = new Date();
        String todayString = dateFormat.format(todayDate);
        //Search for date in database
        MetricDayDatabase.databaseExecutor.execute(() -> {
            todayRecord = metricDao.getDayByDate(todayString);
            Log.d("comp3018", "Today's record is: " + todayRecord);
            //If current date isn't in database, add it as a new record
            if (todayRecord == null) {
                todayRecord = new MetricDay(todayString, 0,0,0,0);
                metricDao.insert(todayRecord);
            }
        });
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        Log.d("comp3018", "Listener: "+currentLatitude+" - "+currentLongitude);

        //Get movement metrics
        if (todayRecord != null) {
            //Distance
            float dist = 0;
            if (lastPos != null) {
                //Get distance from last point, and add to record
                dist = location.distanceTo(lastPos);
                todayRecord.addDistance(dist);
            }
            //Set last point to current
            lastPos = location;

            //Time
            long now = SystemClock.elapsedRealtime() / 1000; //Overall time given in seconds
            long time = 0;
            if (lastTime >= 0) {
                time = now - lastTime;
                //Do not add time if movement speed is too low (to avoid counting inactive periods)
                if (dist / time > 0.5) {
                    todayRecord.addTime(time);
                }
            }
            lastTime = now;

            //Average Speed
            double speed = todayRecord.getDistance() / todayRecord.getTime();
            if (speed > 0) {
                todayRecord.setSpeed(speed);
            }
            //Update database
            MetricDayDatabase.databaseExecutor.execute(() -> {
                metricDao.update(todayRecord);
            });

            //If in exercise mode, send to service
            if (sharedPreferences.getBoolean("exerciseOn", false)
                    && !sharedPreferences.getBoolean("exercisePaused", false)) {
                Exercise exercise = locationService.getExercise();
                exercise.addDistance(dist);
                exercise.addTime(time);
                exercise.addMovement(new GeoPoint(location));
                //Top speed recording
                if (dist/time > exercise.getSpeed()) {
                    exercise.setSpeed(dist/time);
                }
                locationService.setExercise(exercise);
            }
        }

        //Check if near any reminders
        ReminderDatabase.databaseExecutor.execute(() -> {
            List<Reminder> list = reminderDao.getReminderList();

            //Count how many reminders are in range, clear sent list if zero
            int count = 0;
            Reminder toDisplay = null;
            //Using this type of loop because:
            // - 'list.forEach(reminder -> {...})' doesn't allow access to local variables outside the loop
            // - 'for (Reminder reminder : list)' stops the listener from working in the background (somehow)
            for (int i = 0; i < list.size(); i++) {
                //Get GeoPoints of user and reminder positions
                //(Because Location class has no constructor that uses lat and long)
                Reminder reminder = list.get(i);
                GeoPoint reminderPos = new GeoPoint(reminder.getLatitude(),reminder.getLongitude());
                GeoPoint currentPos = new GeoPoint(currentLatitude,currentLongitude);
                //Send notification if in range, and not already sent
                if (currentPos.distanceToAsDouble(reminderPos) <= 100
                        && !lastReminders.contains(reminder)) {
                    count++;
                    //Set as reminder to display if none is set, or more important
                    if (toDisplay == null || Objects.equals(reminder.getTag(), "important")) {
                        toDisplay = reminder;
                    }
                    //Add to list of recently triggered reminders
                    lastReminders.add(reminder);
                }
            }
            //If no reminders found, clear list
            if (count == 0) {
                lastReminders.clear();
            } else if (count == 1) {
                //If one is found, simply send a notification with it's text/tag
                locationService.reminderNotify(toDisplay.getText(),toDisplay.getTag());
            } else if (count > 1) {
                //Include "and x more" if multiple reminders
                String text = context.getString(R.string.notification_message,toDisplay.getText(),count-1);
                locationService.reminderNotify(text,toDisplay.getTag());
            }
            //Update metrics as well if necessary
            todayRecord.addReminders(count);
            if (count > 0) {
                MetricDayDatabase.databaseExecutor.execute(() -> {
                    metricDao.update(todayRecord);
                });
            }
        });
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("comp3018", "onStatusChanged: " + provider + " " + status);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Log.d("comp3018", "onProviderEnabled: " + provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Log.d("comp3018", "onProviderDisabled: " + provider);
    }
}
