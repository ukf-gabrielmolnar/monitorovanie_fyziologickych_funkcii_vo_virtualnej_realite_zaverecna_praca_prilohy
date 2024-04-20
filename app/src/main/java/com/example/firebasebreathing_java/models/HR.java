package com.example.firebasebreathing_java.models;

public class HR {

    private Float heartRate;
    private Long timestamp;

    public HR(Float heartRate, Long timestamp) {
        this.heartRate = heartRate;
        this.timestamp = timestamp;
    }

    public HR() {
    }

    public Float getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(Float heartRate) {
        this.heartRate = heartRate;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}

