package com.example.cw1_mp3player.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.cw1_mp3player.R;
import com.example.cw1_mp3player.Utilities.Settings;
import com.example.cw1_mp3player.Services.SongService;

public class SongActivity extends AppCompatActivity {

    Settings settings = Settings.getInstance();
    private SongService songService;
    //Indicates whether the activity is bound to the Song Service
    private boolean bound = false;
    //Progress bar views will be used a lot, so they're not local
    private ProgressBar songBar;
    private TextView progressNum;
    //Handler for progress bar thread
    private Handler handler = new Handler();
    private int songLength = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_page);
        songBar = (ProgressBar) findViewById(R.id.progressBar);
        progressNum = (TextView) findViewById(R.id.progressNum);

        //Background
        final View songBg = findViewById(R.id.songBg);
        songBg.setBackgroundColor(settings.getColour());

        //Set Speed Text
        TextView speed = (TextView) findViewById(R.id.progressSpeed);
        String str = "Speed: " + settings.getSpeed() + "%";
        speed.setText(str);

        //Bind to service if available
        Intent intent = new Intent(SongActivity.this, SongService.class);
        bindService(intent, connection, 0);
    }

    //Unbind on destroy, so progress bar thread stops
    @Override
    protected void onDestroy() {
        super.onDestroy();
        bound = false;
    }

    //Helper function for progress display
    private String secsToMins(int s) {
        return s/60 + ":" + String.format("%02d",s%60);
    }

    //Binder code based on https://developer.android.com/guide/components/bound-services
    //Available under Apache 2.0 License
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //Bind to service
            SongService.SongBinder binder = (SongService.SongBinder) service;
            songService = binder.getService();
            bound = true;

            //Set visual elements on connection
            TextView songName = (TextView) findViewById(R.id.songName);
            songName.setText(songService.getSongName());
            songLength = songService.getSongLength();
            songBar.setMax(songLength);

            //Start Background Thread to update progress bar at regular intervals
            //Based on https://www.digitalocean.com/community/tutorials/android-progressbar-example
            //Available under CC BY-NC-SA 4.0 License
            new Thread(new Runnable() {
                public void run() {
                    while (bound) {
                        handler.post(new Runnable() {
                            public void run() {
                                //Get song progress and update progress bar & text
                                int prog = songService.getSongProgress();
                                songBar.setProgress(prog);
                                String str = secsToMins(prog) + "/" + secsToMins(songLength);
                                progressNum.setText(str);
                            }
                        });
                        try {
                            //Updates every second, relative to song speed
                            Thread.sleep(1000 / ((long) settings.getSpeed()/100));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
            //Reset visuals when Song Service stops
            TextView songName = (TextView) findViewById(R.id.songName);
            songName.setText(R.string.no_song);
            songBar.setProgress(0);
            progressNum.setText(R.string.default_progress);
        }
    };

    public void onPlayButtonClick(View v) {
        //Only responds when bound to a service
        if (bound) {
            int toggled = songService.pauseToggle();
            ImageButton imageButton = (ImageButton) findViewById(R.id.playButton);
            //pauseToggle returns an int to indicate toggle results
            if (toggled == 1) { //Paused the song
                imageButton.setImageResource(R.drawable.baseline_pause_24);
            } else if (toggled == 0) { //Played the song
                imageButton.setImageResource(R.drawable.baseline_play_arrow_24);
            } //(-1 = Unable to toggle song - though the button doesn't need to respond to that)
        }
    }

    public void onStopButtonClick(View v) {
        if (bound) {
            songService.stopSong();
        }
    }
}