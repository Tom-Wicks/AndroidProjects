package com.example.cw1_mp3player.Activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.example.cw1_mp3player.R;
import com.example.cw1_mp3player.Utilities.Settings;
import com.example.cw1_mp3player.Services.SongService;

public class MainActivity extends AppCompatActivity {

    Settings settings = Settings.getInstance();

    ActivityResultLauncher<Intent> reloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Check permissions
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }

        //Get music list
        final ListView listView = findViewById(R.id.musicList);
        //(And set background color, stored in singleton)
        listView.setBackgroundColor(settings.getColour());

        //Result launcher, so that the bg is reloaded when returning from settings
        reloader = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    listView.setBackgroundColor(settings.getColour());
                }
        );

        //Get music from phone storage
        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                MediaStore.Audio.Media.IS_MUSIC + "!=0",
                null,
                null
        );

        //Populate list with cursor results
        listView.setAdapter(new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_1,
                cursor,
                new String[] {MediaStore.Audio.Media.TITLE},
                new int[] {android.R.id.text1})
        );

        //When list item is clicked...
        listView.setOnItemClickListener((parent, view, position, id) -> {
            //Get uri and title of song clicked
            Cursor c = (Cursor) listView.getItemAtPosition(position);
            @SuppressLint("Range") String uri = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA));
            @SuppressLint("Range") String title = c.getString(c.getColumnIndex(MediaStore.Audio.Media.TITLE));

            //Start Song Service with the clicked song (passing uri and title)
            Intent intent1 = new Intent(MainActivity.this, SongService.class);
            intent1.putExtra("uri", uri);
            intent1.putExtra("title", title);
            startService(intent1);
            //And go to Song Activity
            Intent intent2 = new Intent(MainActivity.this, SongActivity.class);
            startActivity(intent2);
        });
    }

    //Reload on permissions granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            recreate();
        }
    }

    //Menu bar code, based on https://developer.android.com/develop/ui/views/components/menus
    //Available under Apache 2.0 License

    //Display menu bar at top
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_topbar, menu);
        return true;
    }

    //Handler for menu bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            //Settings button - go to Settings Activity
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            reloader.launch(i);
            return true;
        } else if (id == R.id.action_song) {
            //Song button - go to Song Activity WITHOUT starting the Song Service
            Intent intent = new Intent(MainActivity.this, SongActivity.class);
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}