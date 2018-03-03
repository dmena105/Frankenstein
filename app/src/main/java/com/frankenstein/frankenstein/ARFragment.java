package com.frankenstein.frankenstein;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Gesture;
import com.otaliastudios.cameraview.GestureAction;

import static android.content.Context.SENSOR_SERVICE;
import static java.lang.Math.abs;
import static java.lang.Math.toDegrees;


public class ARFragment extends android.app.Fragment {
    //Currently displayed angles
    Float cAzimuth = (float)0.0;
    Float cPitch = (float)0.0;
    Float cRoll = (float)0.0;

    //True angles
    Float azimuth = (float)0.0;
    Float pitch = (float)0.0;
    Float roll = (float)0.0;

    //Sample positions
    Float[] centerN = {(float) 180, (float) 80.0, (float) 0.0,};
    Float[] centerE = {(float) 90, (float) 0.0, (float) 0.0,};
    Float[] centerS = {(float) 0, (float) 0.0, (float) 0.0,};
    Float[] centerW = {(float) 270, (float) 0.0, (float) 0.0,};


    public class CustomDrawableView extends View {
        Paint paint = new Paint();
        Boolean rotating = false;
        //Testing..
        DisplayObject North;
        DisplayObject NorthF;

        public CustomDrawableView(Context context) {
            super(context);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;
            this.North = new DisplayObject(centerN, (int)(width/2), (int)(height/2),
                    (int)(width*.9), (int)(height*.9), 0f,0f,width,height);
            this.NorthF = new DisplayObject(centerN, (int)(width/2), (int)(height/2),
                    (int)(width*.9), (int)(height*.9), 0.0005f,0f,width,height);
            paint.setColor(0xff00ff00);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            paint.setTextSize(100);
            paint.setAntiAlias(true);
        };

        protected void onDraw(Canvas canvas) {
            if (azimuth != null) {
                cAzimuth = Global.angleDiff(azimuth,cAzimuth, 60);
                cPitch = Global.angleDiff(pitch,cPitch, 20);
                if(abs(roll-cRoll)>3.5){
                    rotating = true;
                }
                if(rotating){
                    cRoll = Global.angleDiff(roll, cRoll, 5);
                    if(abs(roll-cRoll) < 1)
                        rotating = false;
                }
                //Acting weird. Suspended, will ask about removal/readding
                //canvas.rotate(-(cRoll-90), getWidth()/2, getHeight()/2);
                Rect n = North.getCurrentBound(cAzimuth, cPitch, 0f, 0f);
                if(n!=null)
                    canvas.drawRect(n, paint);
                n = NorthF.getCurrentBound(cAzimuth, cPitch, 0f, 0f);
                if(n!=null)
                    canvas.drawRect(n, paint);
                if(abs(azimuth-cAzimuth)> 2 || abs(pitch-cPitch) > 2){
                    mCustomDrawableView.invalidate();
                }
            }
        }
    }

    CustomDrawableView mCustomDrawableView;
    private SensorManager mSensorManager;
    private CameraView cameraView;
    Sensor accelerometer;
    Sensor magnetometer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.camera_fragment, container, false);
        super.onCreate(savedInstanceState);
        cameraView = view.findViewById(R.id.camera);
        cameraView.mapGesture(Gesture.PINCH, GestureAction.ZOOM);
        cameraView.mapGesture(Gesture.TAP, GestureAction.FOCUS_WITH_MARKER);
        mCustomDrawableView = new CustomDrawableView(getContext());
        FrameLayout frameLayout = view.findViewById(R.id.frame);
        frameLayout.addView(mCustomDrawableView);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraView.destroy();
    }
    public void onSensorChanged(float[] orientation) {
        Log.d("gb", "Arsensor");
        if(abs(azimuth-toDegrees(orientation[0])) > 5 ||
                abs(roll-toDegrees(orientation[2])) > 5){
            azimuth = (float)toDegrees(orientation[0])+180;
            pitch = (float)toDegrees(orientation[1])+180;
            roll = (float)toDegrees(orientation[2])+90;
            Log.d("gb", "azimuth: "+azimuth+" pitch: "+pitch+" roll: "+roll);
            mCustomDrawableView.invalidate();
        }
    }

    public float[] lowPassFilter(float[] in, float[] old){
        float changeRate = (float) .05;
        if(old == null){
            return in;
        }
        for(int i=0; i<old.length; i++){
            old[i] = old[i]+changeRate*(in[i]-old[i]);
        }
        return old;
    }
}
