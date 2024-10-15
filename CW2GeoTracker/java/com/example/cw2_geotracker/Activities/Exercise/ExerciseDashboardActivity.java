package com.example.cw2_geotracker.Activities.Exercise;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;

import com.example.cw2_geotracker.MyLocation.MyLocationService;
import com.example.cw2_geotracker.R;
import com.example.cw2_geotracker.Utilities.DialogUtility;

public class ExerciseDashboardActivity extends AppCompatActivity {

    private MyLocationService locationService;
    private boolean bound = false;
    private SharedPreferences sharedPreferences;
    private String preFile = "com.example.cw2_geotracker";
    private DialogUtility dialogUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_dashboard);
        dialogUtility = new DialogUtility(this);

        //Set status text
        sharedPreferences = getSharedPreferences(preFile, MODE_PRIVATE);
        setStatusText();

        //Bind to service if enabled
        if (sharedPreferences.getBoolean("serviceOn",true)) {
            Intent intent = new Intent(ExerciseDashboardActivity.this, MyLocationService.class);
            bindService(intent, connection, 0);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setStatusText(); //Reset text when returning to screen
    }

    public void onExerciseStartClick(View v) {
        if (sharedPreferences.getBoolean("serviceOn",true)) {
            if (!sharedPreferences.getBoolean("exerciseOn", false)) {
                //Go to exercise creation if not already exercising (and service is running)
                Intent intent = new Intent(ExerciseDashboardActivity.this, NewExerciseActivity.class);
                startActivity(intent);
            } else {
                //Error message for exercise already in progress
                dialogUtility.showDialog(R.string.exercise_error_already_on);
            }
        } else {
            //Error message for service being off
            dialogUtility.showDialog(R.string.exercise_error_service_off);
        }
    }

    public void onExercisePauseClick(View v) {
        if (sharedPreferences.getBoolean("exerciseOn", false)) {
            //Can only toggle whether exercise is paused or not if exercise is on
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (sharedPreferences.getBoolean("exercisePaused", false)) {
                //Unpause
                editor.putBoolean("exercisePaused", false);
            } else {
                //Pause
                editor.putBoolean("exercisePaused", true);
            }
            editor.apply();
            setStatusText();
        } else {
            //Error message for not already exercising
            dialogUtility.showDialog(R.string.exercise_error_not_on);
        }
    }

    public void onExerciseStopClick(View v) {
        if (sharedPreferences.getBoolean("exerciseOn", false)) {
            //Stop exercise (ensure service is properly bound first)
            if (bound) {
                locationService.finishExercise(); //Tell service to commit exercise to database
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("exerciseOn", false);
                editor.putBoolean("exercisePaused", false);
                editor.apply();
                setStatusText();
            }
        } else {
            //Error message for not already exercising
            dialogUtility.showDialog(R.string.exercise_error_not_on);
        }
    }

    public void onExerciseListClick(View v) {
        //Go to exercise list
        Intent intent = new Intent(ExerciseDashboardActivity.this, ExerciseListActivity.class);
        startActivity(intent);
    }

    //For convenience: set text to indicate exercise status
    private void setStatusText() {
        TextView status = findViewById(R.id.exerciseStatus);
        //Is an exercise in progress?
        if (sharedPreferences.getBoolean("exerciseOn", false)) {
            //Has the exercise been paused?
            if (sharedPreferences.getBoolean("exercisePaused", false)) {
                status.setText(R.string.exercise_status_pause);
            } else {
                status.setText(R.string.exercise_status_on);
            }
        } else {
            status.setText(R.string.exercise_status_off);
        }
    }

    //Simple service binder
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyLocationService.LocationBinder binder = (MyLocationService.LocationBinder) service;
            locationService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };
}