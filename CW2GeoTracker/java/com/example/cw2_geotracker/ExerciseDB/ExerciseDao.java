package com.example.cw2_geotracker.ExerciseDB;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(Exercise exercise);

    @Query("DELETE FROM exercises")
    void deleteAll();

    @Query("SELECT * FROM exercises")
    Cursor getAllExercises();

    @Query("SELECT * FROM exercises ORDER BY id DESC")
    List<Exercise> getExerciseList();

    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    Cursor getExerciseById(long exerciseId);

    @Query("SELECT * FROM exercises WHERE type = :exerciseType")
    List<Exercise> getExercisesByType(String exerciseType);

    @Update
    int update(Exercise exercise);

    @Query("DELETE FROM exercises WHERE id = :exerciseId")
    int deleteExerciseById(long exerciseId);
}
