package com.example.firebasebreathing_java.models;

public class User {

    private String uID;
    private int userID, userSessionNr, levelID, breathingIntensity;
    private Float breathingFrequency, heartRate, gsr;
    private Long timestamp;
    private String levelName;

    public User(String uID, int userID, int levelID, int userSessionNr, int breathingIntensity, Float breathingFrequency, Float heartRate, Float gsr, Long timestamp, String levelName) {
        this.uID = uID;
        this.userID = userID;
        this.levelID = levelID;
        this.userSessionNr = userSessionNr;
        this.breathingIntensity = breathingIntensity;
        this.breathingFrequency = breathingFrequency;
        this.heartRate = heartRate;
        this.gsr = gsr;
        this.timestamp = timestamp;
        this.levelName = levelName;
    }

    public User() {
    }

    public int getLevelID() {
        return levelID;
    }

    public void setLevelID(int levelID) {
        this.levelID = levelID;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public int getUserSessionNr() {
        return userSessionNr;
    }

    public void setUserSessionNr(int userSessionNr) {
        this.userSessionNr = userSessionNr;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getBreathingIntensity() {
        return breathingIntensity;
    }

    public void setBreathingIntensity(int breathingIntensity) {
        this.breathingIntensity = breathingIntensity;
    }

    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
    }

    public Float getBreathingFrequency() {
        return breathingFrequency;
    }

    public void setBreathingFrequency(Float breathingFrequency) {
        this.breathingFrequency = breathingFrequency;
    }

    public Float getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(Float heartRate) {
        this.heartRate = heartRate;
    }

    public Float getGsr() {
        return gsr;
    }

    public void setGsr(Float gsr) {
        this.gsr = gsr;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
