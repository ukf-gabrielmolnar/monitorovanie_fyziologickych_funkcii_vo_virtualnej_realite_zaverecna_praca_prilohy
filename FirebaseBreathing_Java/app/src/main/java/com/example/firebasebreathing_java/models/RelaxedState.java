package com.example.firebasebreathing_java.models;

public class RelaxedState {

    private String uID;
    private int userID, userSessionNr;
    private Float respirationRate_avg, respirationRate_max, respirationRate_max1, respirationRate_min, respirationRate_min1,
            respirationIntensity_avg, respirationIntensity_max, respirationIntensity_max1, respirationIntensity_min, respirationIntensity_min1,
            heartRate_avg, heartRate_max, heartRate_max1, heartRate_min, heartRate_min1,
            gsr_avg, gsr_max, gsr_max1, gsr_min, gsr_min1,
            temperature, humidity;
    private Long timestamp;

    public RelaxedState(String uID, int userID, int userSessionNr,
                        Float respirationRate_avg, Float respirationRate_max, Float respirationRate_max1, Float respirationRate_min, Float respirationRate_min1,
                        Float respirationIntensity_avg, Float respirationIntensity_max, Float respirationIntensity_max1, Float respirationIntensity_min, Float respirationIntensity_min1,
                        Float heartRate_avg, Float heartRate_max, Float heartRate_max1, Float heartRate_min, Float heartRate_min1,
                        Float gsr_avg, Float gsr_max, Float gsr_max1, Float gsr_min, Float gsr_min1, Float temperature, Float humidity,
                        Long timestamp) {
        this.uID = uID;
        this.userID = userID;
        this.userSessionNr = userSessionNr;
        this.temperature = temperature;
        this.humidity = humidity;

        this.respirationRate_avg = respirationRate_avg;
        this.respirationRate_max = respirationRate_max;
        this.respirationRate_max1 = respirationRate_max1;
        this.respirationRate_min = respirationRate_min;
        this.respirationRate_min1 = respirationRate_min1;

        this.respirationIntensity_avg = respirationIntensity_avg;
        this.respirationIntensity_max = respirationIntensity_max;
        this.respirationIntensity_max1 = respirationIntensity_max1;
        this.respirationIntensity_min = respirationIntensity_min;
        this.respirationIntensity_min1 = respirationIntensity_min1;

        this.heartRate_avg = heartRate_avg;
        this.heartRate_max = heartRate_max;
        this.heartRate_max1 = heartRate_max1;
        this.heartRate_min = heartRate_min;
        this.heartRate_min1 = heartRate_min1;

        this.gsr_avg = gsr_avg;
        this.gsr_max = gsr_max;
        this.gsr_max1 = gsr_max1;
        this.gsr_min = gsr_min;
        this.gsr_min1 = gsr_min1;
        this.timestamp = timestamp;
    }

    public RelaxedState() {
    }

    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    public Float getHumidity() {
        return humidity;
    }

    public void setHumidity(Float humidity) {
        this.humidity = humidity;
    }

    public int getUserSessionNr() {
        return userSessionNr;
    }

    public void setUserSessionNr(int userSessionNr) {
        this.userSessionNr = userSessionNr;
    }

    public Float getRespirationIntensity_avg() {
        return respirationIntensity_avg;
    }

    public void setRespirationIntensity_avg(Float respirationIntensity_avg) {
        this.respirationIntensity_avg = respirationIntensity_avg;
    }

    public Float getRespirationIntensity_max() {
        return respirationIntensity_max;
    }

    public void setRespirationIntensity_max(Float respirationIntensity_max) {
        this.respirationIntensity_max = respirationIntensity_max;
    }

    public Float getRespirationIntensity_max1() {
        return respirationIntensity_max1;
    }

    public void setRespirationIntensity_max1(Float respirationIntensity_max1) {
        this.respirationIntensity_max1 = respirationIntensity_max1;
    }

    public Float getRespirationIntensity_min() {
        return respirationIntensity_min;
    }

    public void setRespirationIntensity_min(Float respirationIntensity_min) {
        this.respirationIntensity_min = respirationIntensity_min;
    }

    public Float getRespirationIntensity_min1() {
        return respirationIntensity_min1;
    }

    public void setRespirationIntensity_min1(Float respirationIntensity_min1) {
        this.respirationIntensity_min1 = respirationIntensity_min1;
    }

    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public Float getRespirationRate_avg() {
        return respirationRate_avg;
    }

    public void setRespirationRate_avg(Float respirationRate_avg) {
        this.respirationRate_avg = respirationRate_avg;
    }

    public Float getRespirationRate_max() {
        return respirationRate_max;
    }

    public void setRespirationRate_max(Float respirationRate_max) {
        this.respirationRate_max = respirationRate_max;
    }

    public Float getRespirationRate_max1() {
        return respirationRate_max1;
    }

    public void setRespirationRate_max1(Float respirationRate_max1) {
        this.respirationRate_max1 = respirationRate_max1;
    }

    public Float getRespirationRate_min() {
        return respirationRate_min;
    }

    public void setRespirationRate_min(Float respirationRate_min) {
        this.respirationRate_min = respirationRate_min;
    }

    public Float getRespirationRate_min1() {
        return respirationRate_min1;
    }

    public void setRespirationRate_min1(Float respirationRate_min1) {
        this.respirationRate_min1 = respirationRate_min1;
    }

    public Float getHeartRate_avg() {
        return heartRate_avg;
    }

    public void setHeartRate_avg(Float heartRate_avg) {
        this.heartRate_avg = heartRate_avg;
    }

    public Float getHeartRate_max() {
        return heartRate_max;
    }

    public void setHeartRate_max(Float heartRate_max) {
        this.heartRate_max = heartRate_max;
    }

    public Float getHeartRate_max1() {
        return heartRate_max1;
    }

    public void setHeartRate_max1(Float heartRate_max1) {
        this.heartRate_max1 = heartRate_max1;
    }

    public Float getHeartRate_min() {
        return heartRate_min;
    }

    public void setHeartRate_min(Float heartRate_min) {
        this.heartRate_min = heartRate_min;
    }

    public Float getHeartRate_min1() {
        return heartRate_min1;
    }

    public void setHeartRate_min1(Float heartRate_min1) {
        this.heartRate_min1 = heartRate_min1;
    }

    public Float getGsr_avg() {
        return gsr_avg;
    }

    public void setGsr_avg(Float gsr_avg) {
        this.gsr_avg = gsr_avg;
    }

    public Float getGsr_max() {
        return gsr_max;
    }

    public void setGsr_max(Float gsr_max) {
        this.gsr_max = gsr_max;
    }

    public Float getGsr_max1() {
        return gsr_max1;
    }

    public void setGsr_max1(Float gsr_max1) {
        this.gsr_max1 = gsr_max1;
    }

    public Float getGsr_min() {
        return gsr_min;
    }

    public void setGsr_min(Float gsr_min) {
        this.gsr_min = gsr_min;
    }

    public Float getGsr_min1() {
        return gsr_min1;
    }

    public void setGsr_min1(Float gsr_min1) {
        this.gsr_min1 = gsr_min1;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
