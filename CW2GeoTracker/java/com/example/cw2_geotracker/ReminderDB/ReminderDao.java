package com.example.cw2_geotracker.ReminderDB;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

//Database interface for reminders
@Dao
public interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(Reminder reminder);

    @Query("DELETE FROM reminders")
    void deleteAll();

    @Query("DELETE FROM reminders WHERE id = :reminderId")
    void delete(int reminderId);

    @Query("SELECT * FROM reminders")
    Cursor getAllReminders();

    @Query("SELECT * FROM reminders ORDER BY id")
    List<Reminder> getReminderList();
}
