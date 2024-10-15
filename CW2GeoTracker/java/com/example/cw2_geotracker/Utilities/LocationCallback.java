package com.example.cw2_geotracker.Utilities;

import org.osmdroid.util.GeoPoint;

import java.util.List;

//Interface used so service can send exercise updates to bound activities
//Based on https://stackoverflow.com/questions/23586031/calling-activity-class-method-from-service-class
//Available under CCBY-SA license
public interface LocationCallback {
    void updateOnLocation(double distance, double time, List<GeoPoint> path);
}
