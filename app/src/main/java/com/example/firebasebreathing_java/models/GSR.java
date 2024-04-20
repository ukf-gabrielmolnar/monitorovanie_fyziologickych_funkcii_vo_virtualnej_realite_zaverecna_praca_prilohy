package com.example.firebasebreathing_java.models;

public class GSR {

    private Float value;
    private Long timestamp;

    public GSR(Float value, Long timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

    public GSR() {
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
