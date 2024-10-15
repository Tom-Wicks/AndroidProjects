package com.example.cw2_geotracker.MetricDB;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {MetricDay.class}, version = 1, exportSchema = false)
public abstract class MetricDayDatabase extends RoomDatabase {
    public abstract MetricDayDao metricDayDao();
    private static volatile MetricDayDatabase instance;
    private static final int threadCount = 4;
    public static final ExecutorService databaseExecutor = Executors.newFixedThreadPool(threadCount);

    private static RoomDatabase.Callback createCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            Log.d("comp3018", "onCreate");
            databaseExecutor.execute(() -> {
                MetricDayDao metricDayDao = instance.metricDayDao();
                metricDayDao.deleteAll();
            });
        }
    };

    public static MetricDayDatabase getDatabase(final Context context) {
        if (instance == null) {
            synchronized (MetricDayDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    MetricDayDatabase.class, "metrics")
                            .fallbackToDestructiveMigration()
                            .addCallback(createCallback)
                            .build();
                }
            }
        }
        return instance;
    }
}
