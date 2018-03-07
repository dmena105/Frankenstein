package com.frankenstein.frankenstein;

import java.sql.Blob;

/**
 * Created by Timothyqiu on 2018-02-24.
 *
 */

public class GalleryEntry {
    private long entryId;       // ID number
    private String userEmail;   // email address of the user
    private long postTime;      // time the posts are posted
    private float azimuth;      // Azimuth for the AR fragment
    private double latitude;    // latitude of the post
    private double longitude;   // longitude of the post
    private String postText;    // The main text of a post
    private String picture;     // The picture encoded by Base64
    private String summary;     // The summary of a post

    public GalleryEntry(){
        entryId = -1;
        userEmail = "";
        postTime = System.currentTimeMillis();
        azimuth = 0;
        latitude = 0;
        longitude = 0;
        postText = "";
        picture = "";
        summary = "";
    }

    public long getEntryId() {
        return entryId;
    }

    public void setEntryId(long entryId) {
        this.entryId = entryId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public long getPostTime() {
        return postTime;
    }

    public void setPostTime(long postTime) {
        this.postTime = postTime;
    }

    public float getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(float azimuth) {
        this.azimuth = azimuth;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPostText() {
        return postText;
    }

    public void setPostText(String text) {
        this.postText = text;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
