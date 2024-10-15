package com.example.cw2_geotracker.ExerciseDB;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.cw2_geotracker.Utilities.Converters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Exercise.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class ExerciseDatabase extends RoomDatabase {
    public abstract ExerciseDao exerciseDao();
    private static volatile ExerciseDatabase instance;
    private static final int threadCount = 4;
    public static final ExecutorService databaseExecutor = Executors.newFixedThreadPool(threadCount);

    private static RoomDatabase.Callback createCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            Log.d("comp3018", "onCreate");
            databaseExecutor.execute(() -> {
                ExerciseDao exerciseDao = instance.exerciseDao();
                exerciseDao.deleteAll();
            });
        }
    };

    public static ExerciseDatabase getDatabase(final Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    ExerciseDatabase.class, "exercises")
                    .fallbackToDestructiveMigration()
                    .addCallback(createCallback)
                    .build();
        }
        return instance;
    }
}
