package com.example.codered;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;

import com.bumptech.glide.Glide;
import com.example.codered.model.EventModel;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.services.calendar.model.Event;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Store {
    public static int currentIndexBottomNav = 1;
    public static boolean isPermissionGranted = false;
    public static ArrayList<EventModel> events = new ArrayList<>();
    public static Event firstEvent = null;
    public static LocalDateTime firstEventLDT = null;
    public static boolean isPlayingAlert = false;
    public static boolean isAnimating = false;
    private static User user = new User();
    public static boolean inTeamSelectionPage = false;
    public static int[] colors = {R.color.tomato, R.color.sky_blue, R.color.steel_blue, R.color.gold_yellow, R.color.violet};

    //Getters and Setters
    public static User getUser() {
        return user;
    }
    public static void setUser(GoogleSignInAccount userGoogleAccount) {
        Store.user = new User(userGoogleAccount);
    }

    public static Event getFirstEvent() {
        return firstEvent;
    }
    public static void setFirstEvent(Event firstEvent) {
        Store.firstEvent = firstEvent;
    }

    public static LocalDateTime getFirstEventLDT() {
        return firstEventLDT;
    }
    public static void setFirstEventLDT(LocalDateTime firstEventLDT) {
        Store.firstEventLDT = firstEventLDT;
    }

    public static int getCurrentIndexBottomNav() {
        return currentIndexBottomNav;
    }
    public static void setCurrentIndexBottomNav(int currentIndexBottomNav) {
        Store.currentIndexBottomNav = currentIndexBottomNav;
    }

    public static ArrayList<EventModel> getEvents() {
        return events;
    }

    public static void setEvents(ArrayList<EventModel> events) {
        Store.events = events;
    }

    public static boolean isPlayingAlert() {
        return isPlayingAlert;
    }

    public static void setIsPlayingAlert(boolean isPlayingAlert) {
        Store.isPlayingAlert = isPlayingAlert;
    }

    //Handlers
    public static int dpTopx(Context context, int dp){
        return (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.getResources().getDisplayMetrics()
        );
    }
    public static boolean haveAUser(){
        return !user.getUserId().equals("");
    }

    public static class User{
        private String username = "", email = "", userId = "", teamName = "", teamPosition = "", phNo = "", serverAuthCode = "", photoUrl = "";
        private Map<String, String> dbMap = new HashMap<>();
        public User() {}
        public User(GoogleSignInAccount account) {
            this.userId = account.getId();
            this.email = account.getEmail();
            this.photoUrl = Objects.requireNonNull(account.getPhotoUrl()).toString();
            this.username = account.getDisplayName();
            this.serverAuthCode = account.getServerAuthCode();
            dbMap.put("Name", this.username);
            dbMap.put("UserID", this.userId);
            dbMap.put("Email", this.email);
            dbMap.put("PhotoUrl", this.photoUrl);

        }
        public String getUserId() {
            return userId;
        }
        public Uri getPhotoUri() { return Uri.parse(photoUrl); }
        public String getServerAuthCode() {
            return serverAuthCode;
        }
        public String getTeamName() {
            return teamName;
        }
        public String getTeamPosition() {
            return teamPosition;
        }
        public void setPhotoUrl(String photoUrl) {
            this.photoUrl = photoUrl;
            dbMap.put("PhotoUrl", photoUrl);
        }
        public void setTeamName(String teamName) {
            this.teamName = teamName;
            dbMap.put("TeamName", teamName);

        }
        public void setTeamPosition(String teamPosition) {
            this.teamPosition = teamPosition;
            dbMap.put("TeamPosition", teamPosition);
        }
        public void setPhNo(String phNo) {
            this.phNo = phNo;
            dbMap.put("PhoneNumber", phNo);
        }

        public void addMeToFirebase(){
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("users").child(userId).setValue(dbMap);
            mDatabase.child("teams").child(teamName).setValue(userId);
        }

        public void setSnapShotToData(DataSnapshot ds) {
            setTeamPosition(getValueFromSS(ds, "TeamPosition"));
            setTeamName(getValueFromSS(ds, "TeamName"));
            setPhotoUrl(getValueFromSS(ds, "PhotoUrl"));
        }

        private String getValueFromSS(DataSnapshot ds, String key) {
            return Objects.requireNonNull(ds.child(key).getValue()).toString();
        }
    }
}
