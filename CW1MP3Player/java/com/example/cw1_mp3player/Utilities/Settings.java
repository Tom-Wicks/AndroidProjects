package com.example.cw1_mp3player.Utilities;

//Settings Singleton
//An instance of this class is created when the app is started (by MainActivity),
//which is then used by the other activities and services to store and retrieve settings.
public class Settings {
    //Reference to the actual instance
    private static Settings instance = null;
    private int colour;
    private int speed;

    //Constructor sets default values, and is hidden from other classes
    protected Settings() {
        colour = 0xFFFFC300;
        speed = 100;
    }

    //Singleton access
    public static Settings getInstance() {
        if(instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    public int getSpeed() {
        return speed;
    }
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getColour() {
        return colour;
    }
    public void setColour(int colour) {
        this.colour = colour;
    }
}

