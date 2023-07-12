package com.example.codered.model;

public class EventModel {
    private String eventTime, eventTitle, status;
    private int bg_color;

    public EventModel(String eventTitle, String eventTime, String status, int bg_color) {
        this.eventTime = eventTime;
        this.eventTitle = eventTitle;
        this.status = status;
        this.bg_color = bg_color;
    }

    public String getEventTime() {
        return eventTime;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public String getEventStatus() {
        return status;
    }
}
