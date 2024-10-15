package com.example.cw2_geotracker.Activities.Exercise;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.cw2_geotracker.ExerciseDB.Exercise;
import com.example.cw2_geotracker.ExerciseDB.ExerciseDao;
import com.example.cw2_geotracker.ExerciseDB.ExerciseDatabase;
import com.example.cw2_geotracker.R;
import com.example.cw2_geotracker.Utilities.TagUtility;

import org.osmdroid.config.Configuration;
import org.osmdroid.library.BuildConfig;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.util.List;
import java.util.Objects;

public class ExerciseInfoActivity extends AppCompatActivity {

    ExerciseDatabase db;
    ExerciseDao dao;

    Exercise exercise;
    MapView map;
    TagUtility tagUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialize osmdroid
        Context context = getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        Configuration.getInstance().setUserAgentValue(BuildConfig.LIBRARY_PACKAGE_NAME);
        //Before inflating view
        setContentView(R.layout.activity_exercise_info);
        tagUtility = new TagUtility(this);

        db = ExerciseDatabase.getDatabase(getApplicationContext());
        dao = db.exerciseDao();

        Intent intent = getIntent();
        //Serializable, so that it can be easily passed on here
        exercise = (Exercise) intent.getSerializableExtra("exercise");

        //Setting text
        //Name
        TextView titleText = findViewById(R.id.exerciseInfoNameText);
        titleText.setText(exercise.getName());
        //Date
        TextView dateText = findViewById(R.id.exerciseInfoDateText);
        dateText.setText(exercise.getDate());
        //Distance
        double dist = exercise.getDistance();
        TextView distanceText = findViewById(R.id.exerciseInfoDistanceText);
        distanceText.setText(getString(R.string.metric_distance,dist));
        //Time
        TextView timeText = findViewById(R.id.exerciseInfoTimeText);
        int seconds = (int) exercise.getTime();
        int ss = seconds % 60;
        int mm = (seconds % 3600) / 60;
        int hh = seconds / 3600;
        timeText.setText(getString(R.string.metric_time, hh, mm, ss));
        //Speed
        TextView topSpeedText = findViewById(R.id.exerciseInfoTopSpeedText);
        topSpeedText.setText(getString(R.string.metric_top_speed, exercise.getSpeed()));
        double time = exercise.getTime();
        TextView avgSpeedText = findViewById(R.id.exerciseInfoAvgSpeedText);
        avgSpeedText.setText(getString(R.string.metric_speed, dist/time));

        //Path display, using map
        map = findViewById(R.id.pathMap);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.getController().setZoom(20.0);
        //Add line
        Polyline exercisePath = new Polyline();
        List<GeoPoint> points = exercise.getMovement();
        exercisePath.setPoints(points);
        map.getOverlays().add(exercisePath);
        //Wait until map is ready, as the zoom wont work otherwise
        map.addOnFirstLayoutListener((v, left, top, right, bottom) -> {
            //Center map on line, via bounding box, based on https://stackoverflow.com/questions/37325324/osmdroid-center-to-polyline
            //Available under CCBY-SA license
            double maxLat = -90; double minLat = 90;
            double maxLon = -180; double minLon = 180;
            //Loop through points, if they have the largest/smallest latitude/longitude record it
            for (GeoPoint point : points) {
                double curLat = point.getLatitude();
                double curLon = point.getLongitude();
                if (curLat > maxLat) maxLat = curLat;
                if (curLat < minLat) minLat = curLat;
                if (curLon > maxLon) maxLon = curLon;
                if (curLon < minLon) minLon = curLon;
            }
            //Set bounding box to cover whole line, based on largest/smallest positions
            BoundingBox box = new BoundingBox(maxLat,maxLon,minLat,minLon);
            //Increase by scale, to pad the line slightly
            map.zoomToBoundingBox(box.increaseByScale(1.3f),false);
        });

        //Type icon
        ImageView typeImage = findViewById(R.id.exerciseInfoTypeIcon);
        typeImage.setImageDrawable(tagUtility.getTypeIcon(exercise.getType()));
        //Type text
        ExerciseDatabase.databaseExecutor.execute(() -> {
            //Check all other exercises of the same type
            List<Exercise> list = dao.getExercisesByType(exercise.getType());
            int better = 0;
            //Loop through and compare average speeds, increment counter if it's better than this exercise
            for (Exercise ex : list) {
                double thisSpeed = dist/time;
                double otherSpeed = ex.getDistance()/ex.getTime();
                if (otherSpeed > thisSpeed) {
                    better += 1;
                }
            }
            //Text defaults to "best exercise", if it's not it says how many are better
            if (better > 0) {
                TextView comparisonText = findViewById(R.id.exerciseInfoTypeComparisonText);
                comparisonText.setText(getString(R.string.exercise_type_not_best, better));
            }
        });

        //Weather Selection
        setUpWeatherButton(findViewById(R.id.exerciseInfoWeatherClearButton),"clear");
        setUpWeatherButton(findViewById(R.id.exerciseInfoWeatherWindyButton),"windy");
        setUpWeatherButton(findViewById(R.id.exerciseInfoWeatherRainyButton),"rainy");
        setUpWeatherButton(findViewById(R.id.exerciseInfoWeatherStormyButton),"stormy");
        //Temperature Selection
        setUpTempButton(findViewById(R.id.exerciseInfoTempColdButton),"cold");
        setUpTempButton(findViewById(R.id.exerciseInfoTempNeutralButton),"neutral");
        setUpTempButton(findViewById(R.id.exerciseInfoTempHotButton),"hot");

        //(And finally...) Notes
        EditText notesText = findViewById(R.id.exerciseInfoNotesEdit);
        notesText.setText(exercise.getNotes());
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }

    public void onNotesSaveClick(View v) {
        //Get the current notes and save to database
        EditText notesText = findViewById(R.id.exerciseInfoNotesEdit);
        exercise.setNotes(notesText.getText().toString());
        ExerciseDatabase.databaseExecutor.execute(() -> {
            dao.update(exercise);
        });
    }

    //Convenience functions
    private void setUpWeatherButton(RadioButton button, String weather) {
        //Set radio button as checked if the type matches the exercise record
        if (Objects.equals(exercise.getWeather(), weather)) {
            button.setChecked(true);
        }
        //On click, commit change to database
        button.setOnCheckedChangeListener((buttonView, isChecked) -> {
            exercise.setWeather(weather);
            ExerciseDatabase.databaseExecutor.execute(() -> {
                dao.update(exercise);
            });
        });
    }

    private void setUpTempButton(RadioButton button, String temp) {
        if (Objects.equals(exercise.getTemperature(), temp)) {
            button.setChecked(true);
        }
        button.setOnCheckedChangeListener((buttonView, isChecked) -> {
            exercise.setTemperature(temp);
            ExerciseDatabase.databaseExecutor.execute(() -> {
                dao.update(exercise);
            });
        });
    }
}