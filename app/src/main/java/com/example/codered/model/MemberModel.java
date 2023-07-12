package com.example.codered.model;

import android.graphics.Bitmap;

import java.io.Serializable;

public class MemberModel implements Serializable {
    private String name;
    private String mobileNumber;
    private String position;
    private Bitmap imageUrl;
    private String team_name;
    private String gender;
    private boolean selected = false;

    public MemberModel(String name, String gender, String mobileNumber, String position, String team_name) {
        this.name = name;
        this.mobileNumber = mobileNumber;
        this.position = position;
        this.team_name = team_name;
        this.gender = gender;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getGender() {
        return gender;
    }

    public Bitmap getImageUrl() {
        return imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getPosition() {
        return position;
    }

    public String getTeam_name() {
        return team_name;
    }
}
