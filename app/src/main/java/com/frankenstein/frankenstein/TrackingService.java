package com.frankenstein.frankenstein;

import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class TrackingService extends Service {
    public static final int ESTABLISH_PORT = 0;
    public static final int UPDATE_LOCATION = 1;
    public static final String LOCATION_KEY = "loc_key";
    private Messenger mapFragmentMessenger = null;
    private Messenger trackingServiceMessenger = new Messenger(new TrackingServiceMessageHandler());
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private static boolean isRunning = false;
    public TrackingService() {}

    @Override
    public void onCreate(){
        super.onCreate();
        isRunning = true;
        new LocationTracker().start();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        isRunning = false;
    }

    public int onStartCommand(Intent intent, int flag, int start_id){
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return trackingServiceMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent){
        mapFragmentMessenger = null;
        super.onUnbind(intent);
        return true;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent){
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }

    public static boolean isRunning() {return isRunning;}

    public String formMessage(double lat, double lng){
        return lat + " " + lng;
    }

    public void sendMessage(String info){
        Message msg = Message.obtain(null, UPDATE_LOCATION);
        Bundle bundle = new Bundle();
        bundle.putString(LOCATION_KEY, info);
        msg.setData(bundle);
        try {
            if (mapFragmentMessenger != null) mapFragmentMessenger.send(msg);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    class LocationTracker extends Thread{
        Handler handler = new Handler();
        Runnable tracker = new Runnable() {
            @Override
            public void run() {
                mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                String provider = mLocationManager.getBestProvider(criteria, true);
                try {
                    mLocationListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            if (location != null){
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                sendMessage(formMessage(latitude, longitude));
                            }
                        }
                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {}
                        @Override
                        public void onProviderEnabled(String provider) {}
                        @Override
                        public void onProviderDisabled(String provider) {}
                    };
                    mLocationManager.requestLocationUpdates(provider, 0, 0, mLocationListener);
                } catch (SecurityException e){
                    e.printStackTrace();
                }
            }
        };

        @Override
        public void run(){
            handler.post(tracker);
        }
    }

    class TrackingServiceMessageHandler extends Handler{
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case ESTABLISH_PORT:
                    mapFragmentMessenger = msg.replyTo;
                    break;
            }
        }
    }
}
