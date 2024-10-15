package com.example.cw2_geotracker.ReminderDB;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//Reminder database with singleton access
@Database(entities = {Reminder.class}, version = 1, exportSchema = false)
public abstract class ReminderDatabase extends RoomDatabase {
    public abstract ReminderDao reminderDao();
    private static volatile ReminderDatabase instance;
    private static final int threadCount = 4;
    public static final ExecutorService databaseExecutor = Executors.newFixedThreadPool(threadCount);

    private static RoomDatabase.Callback createCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            //Creation callback - if database is not initialized
            databaseExecutor.execute(() -> {
                ReminderDao reminderDao = instance.reminderDao();
                reminderDao.deleteAll();
            });
        }
    };

    public static ReminderDatabase getDatabase(final Context context) {
        //Create new instance if not already created
        if (instance == null) {
            synchronized (ReminderDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    ReminderDatabase.class, "reminders")
                            .fallbackToDestructiveMigration()
                            .addCallback(createCallback)
                            .build();
                }
            }
        }
        return instance;
    }
}
