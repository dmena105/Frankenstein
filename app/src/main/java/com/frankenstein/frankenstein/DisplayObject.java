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

    //Blank constructor for portability
    public DisplayObject(){
    }

    public DisplayObject(Float[] center, int centerX, int centerY, int spanX, int spanY, int width, int height){
        this.centerAngles = center.clone();
        this.centerX = centerX;
        this.centerY = centerY;
        this.spanX = spanX;
        this.spanY = spanY;
        this.width=width;
        this.height=height;
    }

    public Rect getCurrentBound(Float azimuth, double pitch, double poslat, double poslng, double objlat, double objlng){
        //Get distance in meters
        float[] mdistance = new float[3];
        Location.distanceBetween(poslat, poslng, objlat, objlng, mdistance);
        float distance = abs(mdistance[0]);
        Log.d("gb3", "distance = "+distance);
        //Don't show objects too far away
        if(distance > 100 || distance < 0){
            return null;
        }
        //Scale the object linearly with distance. Will replace with log scale if time
        //(inacurate, but feels better)
        float scale = 1.0f/mdistance[0];
        if(scale > 2)
            scale = 1f;
        //The object is only visible if it's within 90 degrees of the camera. For close objects,
        // use actual azimuth
        float fract;
        if(distance < 5) {
            fract = Global.angleDist(azimuth, this.centerAngles[0]) / (float) log(1 + scale);
        } else {
            //For far ones, use direction to object. Objects should turn to face you in the distance
            Location start = new Location("");
            start.setLatitude(objlat);
            start.setLongitude(objlng);
            Location goal = new Location("");
            goal.setLatitude(poslat);
            goal.setLongitude(poslng);
            fract = Global.angleDist(azimuth, (float)toDegrees(start.bearingTo(goal))%360)/(float)log(1+scale);
        }
        if(abs(fract) > 90){
            return null;
        }
        //Move horizontal center
        int centerX = (int)(this.centerX+sin(Math.toRadians(fract))*width);
        Log.d("gb3", "sin = "+sin(Math.toRadians(fract))+" Angle "+fract);
        float offVertical = Global.angleDist((float)pitch, this.centerAngles[1]);
        int centerY;
        //Check if your within 90 degrees of the vertical
        if(abs(offVertical) < 90){
            offVertical = Math.max(0, offVertical-30);
            centerY = (int)(this.centerY- height*sin(Math.toRadians(offVertical)));
        } else {
            return null;
        }
        //Calculate the bounds
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
}
