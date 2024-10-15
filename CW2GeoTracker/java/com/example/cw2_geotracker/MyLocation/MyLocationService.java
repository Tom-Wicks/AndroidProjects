package com.example.cw2_geotracker.MyLocation;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.cw2_geotracker.Activities.MainActivity;
import com.example.cw2_geotracker.ExerciseDB.Exercise;
import com.example.cw2_geotracker.ExerciseDB.ExerciseDao;
import com.example.cw2_geotracker.ExerciseDB.ExerciseDatabase;
import com.example.cw2_geotracker.R;
import com.example.cw2_geotracker.Utilities.LocationCallback;
import com.example.cw2_geotracker.Utilities.TagUtility;

//Main location service, used to handle both reminder notifications and metric tracking
public class MyLocationService extends Service {

    //Instance of LocationBinder, defined internally below
    private final IBinder binder = new LocationBinder();
    //Notification channel values
    private static final String CHANNEL_ID = "LocationChannel";
    private static final int NOTIFICATION_ID = 2;

    private static final String R_CHANNEL_ID = "ReminderChannel";
    private static final int R_NOTIFICATION_ID = 3;

    private TagUtility tagUtility;

    ExerciseDatabase exerciseDb;
    ExerciseDao exerciseDao;
    Exercise currentExercise;

    private LocationCallback callback;

    @Override
    public void onCreate() {
        super.onCreate();
        tagUtility = new TagUtility(this);
        exerciseDb = ExerciseDatabase.getDatabase(this);
        exerciseDao = exerciseDb.exerciseDao();

        //Create Foreground Channel
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "My Location Service",
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        //Create Reminder Channel
        NotificationChannel channel2 = new NotificationChannel(
                R_CHANNEL_ID,
                "Location Reminder Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager notificationManager2 = getSystemService(NotificationManager.class);
        notificationManager2.createNotificationChannel(channel2);

        //Separate location listener (because the osm tracker doesn't work in the background)
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        MyLocationListener listener = new MyLocationListener(this);

        //Get location updates (lower frequency than the map one, to avoid excess CPU usage
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, listener);
        } catch(SecurityException e) {
            Log.d("comp3018", e.toString());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Start with foreground notification, as it doesn't work outside the app otherwise
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Geo Tracker")
                .setContentText("Now tracking...")
                .setSmallIcon(R.drawable.baseline_location_on_24)
                .setContentIntent(toAppIntent())
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
        startForeground(1,notification);
        return START_NOT_STICKY;
    }

    //Return reference when bound
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    //Binder class, allows for binding with Location Service
    public class LocationBinder extends Binder {
        public MyLocationService getService() {
            return MyLocationService.this;
        }
    }

    //Notification Sender, based on https://developer.android.com/develop/ui/views/notifications/build-notification#java
    //Available under Apache 2.0 license
    public void reminderNotify(String text, String tag) {
        //Create Notification
        Notification notification = new NotificationCompat.Builder(this, R_CHANNEL_ID)
                .setSmallIcon(tagUtility.getTagInt(tag))
                .setContentTitle("Geo Reminder")
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(toAppIntent())
                .setAutoCancel(true)
                .build();
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            manager.notify(NOTIFICATION_ID, notification);
        }
    }

    //Convenience function for generating notification intent
    private PendingIntent toAppIntent() {
        //Open main activity, and clear current activities for safety
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_IMMUTABLE);
    }

    //Exercise handling functions, mostly used by the listener
    public void startExercise(String name, String date, String type) {
        currentExercise = new Exercise(name,date,type);
    }
    public Exercise getExercise() {
        return currentExercise;
    }
    public void setExercise(Exercise exercise) {
        currentExercise = exercise;
        //If callback is set, pass updated data to activity
        if (callback != null) {
            callback.updateOnLocation(
                    currentExercise.getDistance(),
                    currentExercise.getTime(),
                    currentExercise.getMovement()
            );
        }
    }
    public void finishExercise() {
        //Record exercise in database
        ExerciseDatabase.databaseExecutor.execute(() -> {
            exerciseDao.insert(currentExercise);
            currentExercise = null;
        });
    }

    public void setCallback(LocationCallback callback) {
        this.callback = callback;
    }
}