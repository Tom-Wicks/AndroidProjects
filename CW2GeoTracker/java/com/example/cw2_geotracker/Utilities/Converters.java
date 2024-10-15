package com.example.cw2_geotracker.Utilities;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.osmdroid.util.GeoPoint;

import java.util.List;

//Type Converter for arrays of geopoints (for movement lines), to and from Json
//Using gson library, from https://github.com/google/gson
//Available under Apache 2.0 license
public class Converters {
    @TypeConverter
    public static List<GeoPoint> stringToMovement(String value) {
        Gson gson = new Gson();
        //Define token, so that correct type can be passed to the conversion function
        TypeToken<List<GeoPoint>> token = new TypeToken<List<GeoPoint>>() {};
        return gson.fromJson(value, token.getType());
    }

    @TypeConverter
    public static String movementToString(List<GeoPoint> value) {
        Gson gson = new Gson();
        //Convert list to Json and store as string
        return gson.toJson(value);
    }
}
