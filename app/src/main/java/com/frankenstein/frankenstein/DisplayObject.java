package com.frankenstein.frankenstein;

import android.graphics.Rect;
import android.util.Log;

import static java.lang.Math.abs;
import static java.lang.Math.log;
import static java.lang.Math.sin;

/**
 * Created by Diyogon on 2/27/18.
 */

public class DisplayObject {
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

    public DisplayObject(Float[] center, int centerX, int centerY, int spanX, int spanY, float lat, float lng, int width, int height){
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

    public Rect getCurrentBound(Float azimuth, Float pitch){
        float scale = 1/this.lat;
        //The object is only visible if it's within 90 degrees of the camera
        float fract = Global.angleDist(azimuth, this.centerAngles[0])/(float)log(1+scale);
        Log.d("gb3", "fract = "+fract+" azimuth = "+azimuth);
        if(abs(fract) > 90){
            return null;
        }
        int centerX = (int)(this.centerX+sin(Math.toRadians(fract))*width);
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
        return ret;
    }
}
