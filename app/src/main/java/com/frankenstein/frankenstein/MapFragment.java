package com.frankenstein.frankenstein;

import android.Manifest;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragment extends android.app.Fragment implements OnMapReadyCallback, ServiceConnection {
    private GoogleMap mMap;
    private MapView mMapView;
    private Messenger mapFragmentMessenger;
    private Messenger trackingServiceMessenger;
    private Application mApplicationContext;
    private Marker mCurrentMarker = null;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mApplicationContext = (Application)getActivity().getApplicationContext();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = view.findViewById(R.id.map_view);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e){
            Log.d("debug", "map initialize error");
        }
        mMapView.getMapAsync(this);
        Log.d("debug", "Getting the map fragment");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        if (TrackingService.isRunning()) mApplicationContext.unbindService(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("debug", "Map is ready");
        checkPermissions();
        Intent trackIntent = new Intent(getActivity(), TrackingService.class);
        mApplicationContext.startService(trackIntent);
        mApplicationContext.bindService(trackIntent, this, Context.BIND_AUTO_CREATE);
    }

    // For version above 23, check permission before initializing location services.
    private void checkPermissions() {
        if(Build.VERSION.SDK_INT < 23)
            return;
        if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        else {
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            String provider = locationManager.getBestProvider(criteria, true);
            try {       // Display the last known location using marker
                Location lastLoc = locationManager.getLastKnownLocation(provider);
                if (lastLoc != null) {
                    Log.d("debug", "here");
                    LatLng lLoc = new LatLng(lastLoc.getLatitude(), lastLoc.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(lLoc));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lLoc, 15));
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    // Callback method from checkPermission.
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            String provider = locationManager.getBestProvider(criteria, true);
            try {       // Display the last known location using marker
                Location lastLoc = locationManager.getLastKnownLocation(provider);
                if (lastLoc != null) {
                    Log.d("debug", "here");
                    LatLng lLoc = new LatLng(lastLoc.getLatitude(), lastLoc.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(lLoc));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lLoc, 15));
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION))
                    requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                else {} // Enter this chunk if permission is asked before
            }
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service){
        mapFragmentMessenger = new Messenger(new MapFragmentMessageHandler());
        trackingServiceMessenger = new Messenger(service);
        try {
            Message msg = Message.obtain(null, TrackingService.ESTABLISH_PORT);
            msg.replyTo = mapFragmentMessenger;
            trackingServiceMessenger.send(msg);
        } catch (RemoteException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name){
        trackingServiceMessenger = null;
    }

    class MapFragmentMessageHandler extends Handler{
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case TrackingService.UPDATE_LOCATION:
                    Bundle bundle = msg.getData();
                    String[] locInfo = bundle.getString(TrackingService.LOCATION_KEY).split(" ");
                    LatLng currLoc = new LatLng(Double.parseDouble(locInfo[0]),
                            Double.parseDouble(locInfo[1]));
                    if (mCurrentMarker != null) {
                        mCurrentMarker.remove();
                    }
                    else mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currLoc, 17));
                    mCurrentMarker = mMap.addMarker(new MarkerOptions()
                                        .position(currLoc)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            }
        }
    }

}