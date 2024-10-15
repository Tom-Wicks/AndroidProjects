package com.example.cw2_geotracker.Utilities;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.example.cw2_geotracker.R;

//Utility class, for getting drawables based on tag (since database can't store drawables)
public class TagUtility {
    Context context;
    public TagUtility(Context context) {
        this.context = context;
    }

    //Get drawable for the tag passed
    public Drawable getTagIcon(String tag) {
        return context.getResources().getDrawable(getTagInt(tag));
    }

    //Including int version too, since notifications use them directly
    public int getTagInt(String tag) {
        int icon;
        switch (tag) {
            case("shop"):
                icon = R.drawable.baseline_shopping_cart_24;
                break;
            case("important"):
                icon = R.drawable.baseline_priority_high_24;
                break;
            case("people"):
                icon = R.drawable.baseline_groups_24;
                break;
            case("exercise"):
                icon = R.drawable.baseline_directions_run_24;
                break;
            case("fun"):
                icon = R.drawable.baseline_sentiment_very_satisfied_24;
                break;
            default:
                icon = R.drawable.baseline_brightness_1_24;
                break;
        }
        return icon;
    }

    //Version for exercise type icons
    public Drawable getTypeIcon(String type) {
        Drawable icon;
        switch (type) {
            case("cycle"):
                icon = context.getResources().getDrawable(R.drawable.baseline_directions_bike_24);
                break;
            case("walk"):
                icon = context.getResources().getDrawable(R.drawable.baseline_directions_walk_24);
                break;
            default:
                icon = context.getResources().getDrawable(R.drawable.baseline_directions_run_24);
                break;
        }
        return icon;
    }
}
