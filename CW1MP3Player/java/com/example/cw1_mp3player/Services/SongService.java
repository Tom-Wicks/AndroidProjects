package com.example.cw1_mp3player.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.example.cw1_mp3player.Utilities.MP3Player;
import com.example.cw1_mp3player.R;
import com.example.cw1_mp3player.Utilities.Settings;

public class SongService extends Service {

    Settings settings = Settings.getInstance();
    MP3Player player;
    //Instance of SongBinder, defined internally below
    private final IBinder binder = new SongBinder();
    //Stores both the file path and readable title of the given song
    private String uri;
    private String title;
    //Notification channel values
    private static final String CHANNEL_ID = "SongChannel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        player = new MP3Player();

        //Create Notification Channel (with low importance, to stop the sound effect from playing
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Song Service",
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    //When started via intent (from MainActivity)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //If playing a song already, stop it
        if(player.getState() != MP3Player.MP3PlayerState.STOPPED) {
            player.stop();
        }
        //Get speed from settings singleton, and convert from percentage to float (for MP3Player)
        float spd = (float) (settings.getSpeed() / 100.0);
        //URI and Title are passed to the service via intent
        uri = intent.getStringExtra("uri");
        title = intent.getStringExtra("title");
        player.load(uri, spd);

        //Create Notification
        String str = "Now Playing: " + title;
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("MP3 Player")
                .setContentText(str)
                .setSmallIcon(R.drawable.baseline_music_note_24)
                .build();
        startForeground(NOTIFICATION_ID, notification);

        return START_STICKY;
    }

    //Stop MP3Player thread when service ends
    @Override
    public void onDestroy() {
        super.onDestroy();
        player.stop();
    }

    //Return SongBinder reference when bound
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    //Binder class, allows for binding with Song Service
    public class SongBinder extends Binder {
        public SongService getService() {
            return SongService.this;
        }
    }

    //These functions will be called by the activity, via binding
    public String getSongName() {
        return title;
    }
    //Length/Progress converted from milliseconds to seconds
    public int getSongLength() {
        return (player.getDuration())/1000;
    }
    public int getSongProgress() {
        return player.getProgress()/1000;
    }

    public int pauseToggle() {
        //Uses return values to indicate current setting/errors
        if (player.getState() == MP3Player.MP3PlayerState.PLAYING) {
            player.pause();
            return 1; //Indicates paused
        } else if (player.getState() == MP3Player.MP3PlayerState.PAUSED) {
            player.play();
            return 0; //Indicates playing
        } else {
            return -1; //Indicates error
        }
    }

    //Stop service along with song
    public void stopSong() {
        stopSelf();
    }
    //Update speed to match current setting
    public void resetSpeed() {
        player.setPlaybackSpeed((float) (settings.getSpeed() / 100.0));
    }
}