package com.frankenstein.frankenstein;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by Timothyqiu on 2018-03-02.
 */

public class ClusteredMarker implements ClusterItem {
    private final GalleryEntry galleryEntry;
    private final Bitmap profilePicture;
    private final String title;
    private final String snippet;

    public ClusteredMarker(GalleryEntry galleryEntry, Bitmap profilePicture){
        this.galleryEntry = galleryEntry;
        this.profilePicture = profilePicture;
        title = "";
        snippet = "";
    }

    public ClusteredMarker(GalleryEntry galleryEntry, String title, String snippet, Bitmap profilePicture){
        this.galleryEntry = galleryEntry;
        this.title = title;
        this.snippet = snippet;
        this.profilePicture = profilePicture;
    }

    public GalleryEntry getGalleryEntry(){
        return galleryEntry;
    }
    public Bitmap getProfilePicture(){
        return profilePicture;
    }
    @Override
    public LatLng getPosition() {
        return new LatLng(galleryEntry.getLatitude(), galleryEntry.getLongitude());
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }
}
