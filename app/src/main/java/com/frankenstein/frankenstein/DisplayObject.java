package com.frankenstein.frankenstein;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import static java.lang.Math.abs;
import static java.lang.Math.log;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;

/**
 * Created by Diyogon on 2/27/18.
 */

public class DisplayObject {
    Drawable image;
    //The azimuth, pitch, and roll of the object
    Float[] centerAngles;
    //The bounds of the object when the phone is held at that position
    int centerX;
    int centerY;
    int spanX;
    int spanY;
    int width;
    int height;
    //The lat/lng of the object
    Float lat;
    Float lng;

    public DisplayObject(){
    }

    public DisplayObject(Float[] center, int centerX, int centerY, int spanX, int spanY, float lat,
                         float lng, int width, int height){
        this.centerAngles = center.clone();
        this.centerX = centerX;
        this.centerY = centerY;
        this.spanX = spanX;
        this.spanY = spanY;
        this.lat=lat;
        this.lng=lng;
        this.width=width;
        this.height=height;
    }

    public Rect getCurrentBound(Float azimuth, Float pitch, Float lat, Float lng){
        float[] mdistance = new float[3];
        Location.distanceBetween(lat, lng, this.lat, this.lng, mdistance);
        float distance = abs(mdistance[0]);
        Log.d("gb3", "distance = "+distance);
        if(distance > 100 || distance < 0){
            return null;
        }
        float scale = 1.0f/mdistance[0];
        if(scale > 2)
            scale = 1f;
        //The object is only visible if it's within 90 degrees of the camera
        float fract;
        if(distance < 5) {
            fract = Global.angleDist(azimuth, this.centerAngles[0]) / (float) log(1 + scale);
        } else {
            Location start = new Location("");
            start.setLatitude(this.lat);
            start.setLongitude(this.lng);
            Location goal = new Location("");
            goal.setLatitude(lat);
            goal.setLongitude(lng);
            fract = Global.angleDist(azimuth, (float)toDegrees(start.bearingTo(goal))%360)/(float)log(1+scale);
        }
        if(abs(fract) > 90){
            return null;
        }
        int centerX = (int)(this.centerX+sin(Math.toRadians(fract))*width);
        Log.d("gb3", "sin = "+sin(Math.toRadians(fract))+" Angle "+fract);
        float offVertical = Global.angleDist(pitch, this.centerAngles[1]);
        int centerY;
        //Check if your within 90 degrees of the
        if(abs(offVertical) < 90){
            offVertical = Math.max(0, offVertical-30);
            centerY = (int)(this.centerY- height*sin(Math.toRadians(offVertical)));
        } else {
            return null;
        }
        int left = centerX - (int)(scale*spanX/2);
        int right = centerX + (int)(scale*spanX/2);
        int top = centerY - (int)(scale*spanY/2);
        int bottom = centerY + (int)(scale*spanY/2);
        Rect ret = new Rect(left, top, right, bottom);
        Log.d("gb3", "Returning"+left+top+right+bottom);
        return ret;
    }

    public Drawable getImage(){
        Log.d("gb30", "Image returned");
        return this.image;
    }

    public void setImage(Drawable image){
        Log.d("gb30", "Image set!");
        this.image = image;
    }

    private double meterDistanceBetweenPoints(float lat_a, float lng_a, float lat_b, float lng_b) {
        float pk = (float) (180.f/Math.PI);

        float a1 = lat_a / pk;
        float a2 = lng_a / pk;
        float b1 = lat_b / pk;
        float b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000 * tt;
    }
}
