package com.example.cw1_mp3player.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.cw1_mp3player.R;
import com.example.cw1_mp3player.Utilities.Settings;
import com.example.cw1_mp3player.Services.SongService;

public class SettingsActivity extends AppCompatActivity {

    Settings settings = Settings.getInstance();
    private SongService songService;
    //Indicates whether the activity is bound to the Song Service
    private boolean bound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_page);

        //Set current background colour
        final View pageBg = findViewById(R.id.pageBg);
        pageBg.setBackgroundColor(settings.getColour());

        //Background colour list
        //Based on: https://developer.android.com/develop/ui/views/components/spinner
        //Available under Apache 2.0 License
        Spinner bgSpinner = (Spinner) findViewById(R.id.bgSelect);
        //Set list contents
        ArrayAdapter<CharSequence> bgSpinAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.bg_strings,
                android.R.layout.simple_spinner_dropdown_item
        );
        bgSpinner.setAdapter(bgSpinAdapter);
        //Set logic for item selection
        bgSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        String str = (String) parent.getItemAtPosition(pos);
                        int newColour;
                        //Select new colour resource based on current string
                        switch (str) {
                            case "Choose Background Colour":
                                //Default option - to avoid mismatch between spinner text and background colour
                                newColour = settings.getColour();
                                break;
                            case "Tangerine":
                                newColour = getResources().getColor(R.color.tangerine);
                                break;
                            case "Aqua":
                                newColour = getResources().getColor(R.color.aqua);
                                break;
                            case "Lime":
                                newColour = getResources().getColor(R.color.lime);
                                break;
                            default:
                                newColour = getResources().getColor(R.color.error);
                                break;
                        }
                        //Set colour in settings singleton and update background
                        settings.setColour(newColour);
                        final View pageBg = findViewById(R.id.pageBg);
                        pageBg.setBackgroundColor(newColour);
                    }

                    //This shouldn't really occur, hence setting colour to an error value
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        int newColour = getResources().getColor(R.color.error);
                        settings.setColour(newColour);
                        final View pageBg = findViewById(R.id.pageBg);
                        pageBg.setBackgroundColor(newColour);
                    }
                }
        );

        //Speed Setting Bar
        //Based on https://www.geeksforgeeks.org/android-creating-a-seekbar
        //Available under CCBY-SA license
        SeekBar speedBar = (SeekBar) findViewById(R.id.speedBar);
        TextView speedNum = (TextView) findViewById(R.id.speedNum);
        speedBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int spd;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        spd = 50 + (progress*50); //Conversion from progress on bar to speed
                        String str = spd + "%";
                        speedNum.setText(str);
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        //Don't really need anything here.
                    }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        //Update singleton with new speed setting
                        settings.setSpeed(spd);
                        //If a song is currently playing, update its speed too
                        if (bound) {
                            songService.resetSpeed();
                        }
                    }
                }
        );
        //Set progress to current speed value on entry
        int progress = (settings.getSpeed() / 50) - 1; //Conversion from speed to progress on bar
        speedBar.setProgress(progress);

        //Bind to service if available (for updating song speed)
        Intent intent = new Intent(SettingsActivity.this, SongService.class);
        bindService(intent, connection, 0);
    }

    //Binder code based on https://developer.android.com/guide/components/bound-services
    //Available under Apache 2.0 License
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SongService.SongBinder binder = (SongService.SongBinder) service;
            songService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };
}