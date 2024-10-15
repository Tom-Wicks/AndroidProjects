package com.example.cw2_geotracker.MetricDB;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cw2_geotracker.ReminderDB.Reminder;

import java.util.List;

@Dao
public interface MetricDayDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(MetricDay day);

    @Query("DELETE FROM metrics")
    void deleteAll();

    @Query("DELETE FROM metrics WHERE id = :metricId")
    void delete(int metricId);

    @Query("SELECT * FROM metrics")
    Cursor getAllDays();

    //Newest records come first
    @Query("SELECT * FROM metrics ORDER BY date DESC")
    List<MetricDay> getDayList();

    @Query("SELECT * FROM metrics WHERE date = :metricDate")
    MetricDay getDayByDate(String metricDate);

    @Query("SELECT * FROM metrics WHERE id = :metricId")
    MetricDay getDayById(int metricId);

    @Update
    void update(MetricDay metricDay);
}
