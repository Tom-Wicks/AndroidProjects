package com.example.cw2_geotracker.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.cw2_geotracker.R;
import com.example.cw2_geotracker.ReminderDB.Reminder;
import com.example.cw2_geotracker.ReminderDB.ReminderDao;
import com.example.cw2_geotracker.ReminderDB.ReminderDatabase;

//Screen for creating a new reminder
public class NewReminderActivity extends AppCompatActivity {
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_reminder);

        //Position is passed from the main activity
        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("lat", 0.0);
        longitude = intent.getDoubleExtra("long", 0.0);
    }

    public void onReminderConfirmClick(View v) {
        //Get creation choices
        EditText text = findViewById(R.id.reminderTextEdit);
        Spinner spinner = findViewById(R.id.reminderTagSpinner);
        //Add reminder to list
        ReminderDao dao = ReminderDatabase.getDatabase(getApplicationContext()).reminderDao();
        ReminderDatabase.databaseExecutor.execute(() -> {
            dao.insert(new Reminder(
                    text.getText().toString(),
                    latitude,
                    longitude,
                    spinner.getSelectedItem().toString()
            ));
        });
        finish();
    }
}