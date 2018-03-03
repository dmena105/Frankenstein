package com.frankenstein.frankenstein;

import android.hardware.Sensor;
import android.hardware.SensorManager;

import static java.lang.Math.abs;

/**
 * Created by Diyogon on 2/28/18.
 */

public class Global {
    static SensorManager mSensorManager;
    static Sensor accelerometer;
    static Sensor magnetometer;
    static ARFragment arFragment;

    public static float angleDiff(float target, float current, int frames){
        float diff;
        float ret;
        diff = abs(target-current);
        if(diff > 180){
            diff = abs(diff-360);
            diff = -diff/frames;
        } else {
            diff = diff/frames;
        }
        if(current > target){
            ret = current-diff;
        } else {
            ret =  current+diff;
        }
        if(ret < 0){
            ret += 360;
        } else if(ret > 360){
            ret -= 360;
        }
        return ret;
    }

    public static float angleDist(float current, float target){
        float d1 = current-target;
        float d2;
        if(d1 > 0){
            d2 = d1-360;
            if(abs(d2)<d1){
                return d2;
            } else {
                return d1;
            }
        } else {
            d2 = d1+360;
            if(abs(d1)<d2){
                return d1;
            } else {
                return d2;
            }
        }
    }
}
