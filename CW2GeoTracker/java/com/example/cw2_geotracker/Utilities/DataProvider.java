package com.example.cw2_geotracker.Utilities;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cw2_geotracker.ExerciseDB.Exercise;
import com.example.cw2_geotracker.ExerciseDB.ExerciseDatabase;
import com.example.cw2_geotracker.ExerciseDB.ExerciseDao;

//Content provider for exercises, based on https://github.com/android/architecture-components-samples/blob/master/PersistenceContentProviderSample/app/src/main/java/com/example/android/contentprovidersample/provider/SampleContentProvider.java
//Available under Apache 2.0 license
public class DataProvider extends ContentProvider {

    ExerciseDatabase db;
    ExerciseDao dao;
    Context context;

    public static final String authority = "com.example.CW2GeoTracker.Utilities.provider";
    private static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    @Override
    public boolean onCreate() {
        //If context wasn't found, return false to stop access
        context = getContext();
        if (context == null) {
            return false;
        } else {
            db = ExerciseDatabase.getDatabase(context);
            dao = db.exerciseDao();
            //Uri matches: code 1 for all exercises, 2 for specific
            matcher.addURI(authority,"exercises", 1);
            matcher.addURI(authority,"exercises/*", 2);
            return true;
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int code = matcher.match(uri);
        if (code == 1 || code == 2) {
            Cursor cursor;
            //No specific exercise, so get all
            if (code == 1) {
                cursor = dao.getAllExercises();
            } else { //Get id from uri and get from dao
                cursor = dao.getExerciseById(ContentUris.parseId(uri));
            }
            cursor.setNotificationUri(context.getContentResolver(),uri);
            return cursor;
        } else {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int code = matcher.match(uri);
        if (code == 1) { //Return type as directory
            return "vnd.android.cursor.dir/" + authority + ".exercises";
        } else if (code == 2) { //Return type as item
            return "vnd.android.cursor.item/" + authority + ".exercises";
        } else {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int code = matcher.match(uri);
        if (code == 1) {
            //If values passed are missing any required for a new exercise, throw an error
            if (values != null
                    && values.containsKey("name")
                    && values.containsKey("date")
                    && values.containsKey("type")) {
                //Get values to create new exercise
                Exercise ex = new Exercise(
                        values.getAsString("name"),
                        values.getAsString("date"),
                        values.getAsString("type"));
                long id = dao.insert(ex);
                context.getContentResolver().notifyChange(uri,null);
                return ContentUris.withAppendedId(uri,id);
            } else {
                throw new IllegalArgumentException("Invalid URI, missing initial values: " + uri);
            }
        } else {
            throw new IllegalArgumentException("Invalid URI, cannot insert with ID: " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int code = matcher.match(uri);
        if (code == 2) { //Only allow deletion of specific items
            int count = dao.deleteExerciseById(ContentUris.parseId(uri));
            context.getContentResolver().notifyChange(uri,null);
            return count;
        } else {
            throw new IllegalArgumentException("Invalid URI, cannot insert with ID: " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int code = matcher.match(uri);
        if (code == 2) {
            //Update means an id is needed to specify the exercise, but also initial values
            if (values != null
                    && values.containsKey("name")
                    && values.containsKey("date")
                    && values.containsKey("type")
                    && values.containsKey("id")) {
                Exercise ex = new Exercise(
                        values.getAsString("name"),
                        values.getAsString("date"),
                        values.getAsString("type"));
                ex.setId(values.getAsInteger("id"));
                //Optional values are set if present in input
                if (values.containsKey("distance")) {
                    ex.setDistance(values.getAsDouble("distance"));
                }
                if (values.containsKey("time")) {
                    ex.setTime(values.getAsDouble("time"));
                }
                if (values.containsKey("speed")) {
                    ex.setSpeed(values.getAsDouble("speed"));
                }
                int count = dao.update(ex);
                context.getContentResolver().notifyChange(uri,null);
                return count;
            } else {
                throw new IllegalArgumentException("Invalid URI, missing initial values: " + uri);
            }
        } else {
            throw new IllegalArgumentException("Invalid URI, cannot insert with ID: " + uri);
        }
    }
}
