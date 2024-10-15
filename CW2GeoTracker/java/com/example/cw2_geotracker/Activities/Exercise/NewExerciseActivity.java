package com.example.cw2_geotracker.Activities.Exercise;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.cw2_geotracker.MyLocation.MyLocationService;
import com.example.cw2_geotracker.R;

import java.text.SimpleDateFormat;
import java.util.Date;

//Screen for starting a new exercise session
public class NewExerciseActivity extends AppCompatActivity {

    private MyLocationService locationService;
    private boolean bound = false;
    private SharedPreferences sharedPreferences;
    private String preFile = "com.example.cw2_geotracker";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_exercise);

        sharedPreferences = getSharedPreferences(preFile, MODE_PRIVATE);
        if (sharedPreferences.getBoolean("serviceOn",true)) {
            Intent intent = new Intent(NewExerciseActivity.this, MyLocationService.class);
            bindService(intent, connection, 0);
        }
    }

    public void onExerciseCreateConfirmClick(View v) {
        //Get choices from fields
        if (bound) {
            EditText text = findViewById(R.id.exerciseNameEdit);
            Spinner spinner = findViewById(R.id.exerciseTypeSpinner);
            //Create exercise and pass to service
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            Date todayDate = new Date();
            String todayString = dateFormat.format(todayDate);
            locationService.startExercise(
                    text.getText().toString(),
                    todayString,
                    spinner.getSelectedItem().toString()
            );
            //Turn preference on to indicate an exercise is active
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("exerciseOn",true);
            editor.apply();
            //Close this activity
            unbindService(connection);
            finish();
        }
    }

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