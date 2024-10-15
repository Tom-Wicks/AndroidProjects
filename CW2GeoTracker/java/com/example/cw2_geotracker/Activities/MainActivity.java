package com.example.cw2_geotracker.Activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.cw2_geotracker.Activities.Exercise.ExerciseDashboardActivity;
import com.example.cw2_geotracker.MyLocation.MyLocationService;
import com.example.cw2_geotracker.R;
import com.example.cw2_geotracker.ReminderDB.Reminder;
import com.example.cw2_geotracker.ReminderDB.ReminderDao;
import com.example.cw2_geotracker.ReminderDB.ReminderDatabase;
import com.example.cw2_geotracker.Utilities.LocationCallback;
import com.example.cw2_geotracker.Utilities.TagUtility;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.library.BuildConfig;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.List;

//Main screen, with OSM map and access to all other screens
public class MainActivity extends AppCompatActivity implements LocationCallback {

    private MapView map = null;
    private MyLocationNewOverlay userLocation;

    //For handling returning from reminder screen
    ActivityResultLauncher<Intent> resultLauncher;
    private boolean resultGet;

    TagUtility tagUtility;
    private SharedPreferences sharedPreferences;
    private String preFile = "com.example.cw2_geotracker";

    private MyLocationService locationService;
    private boolean bound = false;

    private Polyline exercisePath;

    //OSMDroid code, informed by usage examples from https://github.com/osmdroid/osmdroid/wiki
    //Available under Apache 2.0 license
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialize osmdroid
        Context context = getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        Configuration.getInstance().setUserAgentValue(BuildConfig.LIBRARY_PACKAGE_NAME);
        //Before inflating view
        setContentView(R.layout.activity_main);
        tagUtility = new TagUtility(this);

        resultGet = false;
        //Result launcher, so reminders can display location
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                        //If coming from Reminder list with result, get position
                        Intent data = result.getData();
                        if (data != null) {
                            resultGet = true;
                            GeoPoint point = new GeoPoint(
                                    data.getDoubleExtra("latitude", 0.0),
                                    data.getDoubleExtra("longitude", 0.0)
                            );
                            //Move map to position
                            map.getController().setCenter(point);
                        } else {
                            resultGet = false;
                        }
                }
        );

        //Check if reminder location service should be used
        sharedPreferences = getSharedPreferences(preFile, MODE_PRIVATE);
        if (sharedPreferences.getBoolean("serviceOn",true)) {
            //Start service if not already running
            Intent intent = new Intent(MainActivity.this, MyLocationService.class);
            startForegroundService(intent);
            bindService(intent, connection, 0); //And bind so exercise values can be received
        }
        //User Location provider (separate, as it doesn't work when app is closed)
        GpsMyLocationProvider gps = new GpsMyLocationProvider(this);
        gps.setLocationUpdateMinDistance(5);
        gps.setLocationUpdateMinTime(1000);

        //Metric text
        setTextVisibility();

        //Set up map
        map = findViewById(R.id.mapView);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMinZoomLevel(10.0);
        map.getController().setZoom(20.0);

        //Overlays
        placeReminders();

        //User Position Overlay
        userLocation = new MyLocationNewOverlay(gps, map);
        userLocation.enableMyLocation();
        userLocation.enableFollowLocation();
        map.getOverlays().add(userLocation);
        //Scale bar
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        ScaleBarOverlay scale = new ScaleBarOverlay(map);
        scale.setCentred(true);
        scale.setScaleBarOffset(dm.widthPixels/2,10);
        map.getOverlays().add(scale);
        //Rotation
        RotationGestureOverlay rot = new RotationGestureOverlay(map);
        rot.setEnabled(true);
        map.setMultiTouchControls(true);
        map.getOverlays().add(rot);

        //Register long presses on map
        MapEventsOverlay mapEvents = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                //OSMdroid has a bug where long presses can still occur when pressing zoom buttons
                //So we check if the map is zooming before displaying dialog
                if (!map.isAnimating()) {
                    //On long press, display dialog leading to reminder creation
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.app_name)
                            .setMessage(R.string.create_message)
                            .setPositiveButton(R.string.delete_yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //Go to new reminder page
                                    Intent intent = new Intent(MainActivity.this, NewReminderActivity.class);
                                    intent.putExtra("lat", p.getLatitude());
                                    intent.putExtra("long", p.getLongitude());
                                    startActivity(intent);
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(R.string.delete_no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
                return true;
            }
        });
        map.getOverlays().add(mapEvents);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyLocationService.LocationBinder binder = (MyLocationService.LocationBinder) service;
            locationService = binder.getService();
            bound = true;
            //Callback, so service can message this activity
            locationService.setCallback(MainActivity.this);
            //If no exercise is active, turn off exercise preferences (in case of crash)
            if (locationService.getExercise() == null) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("exerciseOn", false);
                editor.putBoolean("exercisePaused", false);
                editor.apply();
            }
            setTextVisibility();
            Log.d("comp3018","Main bound!");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    //Handling map lifecycle appropriately
    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
        //If moving to a reminder's position, stop following user
        if (resultGet) {
            userLocation.disableFollowLocation();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //Reload reminders in case of data change
        placeReminders();
        //And rebind to service
        Intent intent = new Intent(MainActivity.this, MyLocationService.class);
        bindService(intent, connection, 0);
        setTextVisibility();
        //Hide exercise mode graphics if exercise was stopped
        if (!sharedPreferences.getBoolean("exerciseOn", false)) {
            resetExerciseDisplay();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Do not keep service bound needlessly (to reduce CPU usage)
        if (bound) {
            locationService.setCallback(null);
            unbindService(connection);
            bound = false;
        }
    }

    public void onCenterClick(View v) {
        userLocation.enableFollowLocation();
    }

    //Buttons to move to other pages
    public void onRemindersClick(View v) {
        Intent intent = new Intent(MainActivity.this, ReminderListActivity.class);
        resultLauncher.launch(intent);
    }

    public void onMetricsClick(View v) {
        Intent intent = new Intent(MainActivity.this, MetricListActivity.class);
        startActivity(intent);
    }

    public void onExerciseClick(View v) {
        Intent intent = new Intent(MainActivity.this, ExerciseDashboardActivity.class);
        startActivity(intent);
    }

    //Handles showing reminder markers on the map
    private void placeReminders() {
        //Remove all markers (not other overlay types!)
        map.getOverlays().removeIf(overlay -> overlay.getClass() == Marker.class);
        ReminderDao dao = ReminderDatabase.getDatabase(getApplicationContext()).reminderDao();
        ReminderDatabase.databaseExecutor.execute(() -> {
            List<Reminder> reminderList = dao.getReminderList();
            //For each reminder, add a marker
            for (Reminder r : reminderList) {
                Marker mark = new Marker(map);
                GeoPoint point = new GeoPoint(r.getLatitude(), r.getLongitude());
                mark.setPosition(point);
                mark.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                mark.setTitle(r.getText());
                //Set icon by layering tag over pin
                Drawable icon = tagUtility.getTagIcon(r.getTag());
                //Pin graphic created by Vũ Sang - Flaticon - https://www.flaticon.com/free-icons/navigation
                Drawable pin = getResources().getDrawable(R.drawable.pin_resized);
                Drawable[] layers = {pin,icon};
                LayerDrawable layerDrawable = new LayerDrawable(layers);
                layerDrawable.setLayerSize(1,80,80);
                layerDrawable.setLayerInset(1,61,43,0,0);
                mark.setIcon(layerDrawable);
                map.getOverlays().add(mark);
            }
        });
    }

    //Callback function for location service to use
    @Override
    public void updateOnLocation(double distance, double time, List<GeoPoint> path) {
        //Update stats
        TextView distanceText = findViewById(R.id.mapDistanceText);
        distanceText.setText(getString(R.string.map_distance, distance/1000));
        TextView timeText = findViewById(R.id.mapTimeText);
        int seconds = (int) time;
        int ss = seconds % 60;
        int mm = (seconds % 3600) / 60;
        int hh = seconds / 3600;
        timeText.setText(getString(R.string.map_time, hh,mm,ss));
        TextView speedText = findViewById(R.id.mapSpeedText);
        speedText.setText(getString(R.string.map_speed, distance/time));
        //Update path if already created
        if (exercisePath != null) {
            exercisePath.addPoint(path.get(path.size()-1));
        } else {
            //If not created, generate new path with all points and add to map
            exercisePath = new Polyline();
            exercisePath.setPoints(path);
            map.getOverlays().add(exercisePath);
        }
    }

    //Display default values for exercise display
    public void resetExerciseDisplay() {
        map.getOverlays().remove(exercisePath);
        TextView distanceText = findViewById(R.id.mapDistanceText);
        distanceText.setText(getString(R.string.map_distance_zero));
        TextView timeText = findViewById(R.id.mapTimeText);
        timeText.setText(getString(R.string.map_time_zero));
        TextView speedText = findViewById(R.id.mapSpeedText);
        speedText.setText(getString(R.string.map_speed_zero));
    }

    //Set visibility of exercise display
    private void setTextVisibility() {
        if (bound
                && sharedPreferences.getBoolean("exerciseOn", false)
                && !sharedPreferences.getBoolean("exercisePaused", false)) {
            //Show text
            findViewById(R.id.mapDistanceText).setVisibility(View.VISIBLE);
            findViewById(R.id.mapTimeText).setVisibility(View.VISIBLE);
            findViewById(R.id.mapSpeedText).setVisibility(View.VISIBLE);
        } else {
            //Hide text if not exercising
            findViewById(R.id.mapDistanceText).setVisibility(View.INVISIBLE);
            findViewById(R.id.mapTimeText).setVisibility(View.INVISIBLE);
            findViewById(R.id.mapSpeedText).setVisibility(View.INVISIBLE);
        }
    }
}