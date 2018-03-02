package com.frankenstein.frankenstein;

import android.Manifest;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.nightonke.boommenu.Animation.BoomEnum;
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.TextOutsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MapFragment extends android.app.Fragment implements OnMapReadyCallback,
        ServiceConnection{
    private GoogleMap mMap;
    private MapView mMapView;
    private Messenger mapFragmentMessenger;
    private Messenger trackingServiceMessenger;
    private Application mApplicationContext;
    private Marker mCurrentMarker = null;
    private ArrayList<Marker> mAllGalleryEntries;
    private GalleryEntry mCurrentSelection;
    private float mZoomLevel;
    private LatLng mPreviousLocation;
    private ImageView mImageViewDialog;
    private TextView mTextViewTime;
    private TextView mTextViewSummary;
    private BoomMenuButton mBoomButton;
    private Marker mCurrentMarkerSelected;
    private Marker mCustomLocationMarker;
    /*private long savedMarkerId;
    private boolean savedMarkerIsSelected;
    private boolean savedPreviousLocationExists;
    private CameraUpdate savedCameraPosition;
    private double currLat;*/

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mApplicationContext = (Application)getActivity().getApplicationContext();
        mAllGalleryEntries = new ArrayList<>();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        setRetainInstance(true);
        mMapView = view.findViewById(R.id.map_view);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e){
            Log.d("debug", "map initialize error");
        }
        mMapView.getMapAsync(this);
        return view;
    }

    /*@Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        if (mPreviousLocation != null){
            outState.putBoolean("mPreviousLocationIsNull", false);
            outState.putDouble("prevLat", mPreviousLocation.latitude);
            outState.putDouble("prevLng", mPreviousLocation.longitude);
            outState.putFloat("prevZoom", mZoomLevel);
        }
        else outState.putBoolean("mPreviousLocationIsNull", true);
        outState.putDouble("currLat", mMap.getCameraPosition().target.latitude);
        outState.putDouble("currLng", mMap.getCameraPosition().target.longitude);
        outState.putFloat("currZoom", mMap.getCameraPosition().zoom);
        Log.d("debug", "currLat saved: " + mMap.getCameraPosition().target.latitude);
        if (mCurrentSelection != null){
            outState.putLong("currMarkerId", mCurrentSelection.getEntryId());
            outState.putBoolean("markerIsSelected", true);
        }
        else outState.putBoolean("markerIsSelected", false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        try {
            savedCameraPosition = CameraUpdateFactory.newLatLngZoom
                                    (new LatLng(savedInstanceState.getDouble("currLat")
                                    , savedInstanceState.getDouble("currLng"))
                            , savedInstanceState.getFloat("currZoom"));
            Log.d("debug", "currLat retrieved: " + savedInstanceState.getDouble("currLat"));
            if (!savedInstanceState.getBoolean("mPreviousLocationIsNull")) {
                mPreviousLocation = new LatLng(savedInstanceState.getDouble("prevLat")
                        , savedInstanceState.getDouble("prevLng"));
            }
            if (savedInstanceState.getBoolean("markerIsSelected")) {
                savedMarkerIsSelected = true;
                savedMarkerId = savedInstanceState.getLong("currMarkerId");
            }
            else savedMarkerIsSelected = false;
        } catch (NullPointerException e){}
    }
*/
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
        /*if (savedMarkerIsSelected) {
            for (Marker marker : mAllGalleryEntries) {
                if (((GalleryEntry) marker.getTag()).getEntryId() == savedMarkerId) {
                    marker.showInfoWindow();
                    break;
                }
            }
        }
        Log.d("debug", "currLat used: " + currLat);
        if (savedCameraPosition != null) {
            mMap.animateCamera(savedCameraPosition);
            Log.d("debug", "currLat used: " + savedCameraPosition.toString());
        }*/

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mCustomLocationMarker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                        .snippet(latLng.toString()));
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mCustomLocationMarker != null) {
                    mCustomLocationMarker.remove();
                    mCustomLocationMarker = null;
                }
            }
        });
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                if (mCurrentSelection != null){
                    View window = getLayoutInflater().inflate(R.layout.post_snapshot_popup_window, null);
                    mImageViewDialog = window.findViewById(R.id.imageView_dialog);
                    mTextViewTime = window.findViewById(R.id.textView_dialogTime);
                    mTextViewSummary = window.findViewById(R.id.textView_dialogSummary);
                    GalleryEntry markerInfo = (GalleryEntry) marker.getTag();
                    if (markerInfo.getPicture() != null) {
                        byte[] decodedString = Base64.decode(markerInfo.getPicture(), Base64.DEFAULT);
                        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        mImageViewDialog.setImageBitmap(decodedBitmap);
                    }
                    else {
                        mImageViewDialog.setImageResource(R.drawable.ic_signup_image_placeholder);
                    }
                    DateFormat formatter = SimpleDateFormat.getDateTimeInstance();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(markerInfo.getPostTime());
                    mTextViewTime.setText(formatter.format(calendar.getTime()));
                    mTextViewSummary.setText(markerInfo.getSummary());
                    return window;
                }
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker != null && !marker.equals(mCurrentMarker) && !marker.equals(mCustomLocationMarker)) {
                    mCurrentSelection = new GalleryEntry();
                    final long id = ((GalleryEntry) marker.getTag()).getEntryId();
                    mCurrentSelection.setEntryId(id);
                    Log.d("debug", "Clicked on " + id);
                    DatabaseReference refUtil = MainActivity.databaseReference
                            .child("users").child(MainActivity.username).child("items");
                    refUtil.orderByChild("entryId");
                    refUtil.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChildren()) {
                                for (DataSnapshot dss : dataSnapshot.getChildren()) {
                                    if (dss.child("entryId").getValue(Long.class) == id) {
                                        try {
                                            mCurrentSelection.setLatitude(dss.child("latitude").getValue(Double.class));
                                            mCurrentSelection.setLongitude(dss.child("longitude").getValue(Double.class));
                                            mCurrentSelection.setPostText(dss.child("postText").getValue(String.class));
                                            mCurrentSelection.setPostTime(dss.child("postTime").getValue(Long.class));
                                        } catch (NullPointerException e) {
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
                    if (mCurrentSelection != null) {
                        if (!marker.isInfoWindowShown()) {
                            mZoomLevel = mMap.getCameraPosition().zoom;
                            mPreviousLocation = mMap.getCameraPosition().target;
                            LatLng mLoc = marker.getPosition();
                            double lat = mLoc.latitude + 0.0065;
                            LatLng cameraLocation = new LatLng(lat, mLoc.longitude);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cameraLocation, 15));
                            marker.showInfoWindow();
                            mCurrentMarkerSelected = marker;
                        }
                    }
                }
                return true;
            }

        });
        mMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
            @Override
            public void onInfoWindowLongClick(Marker marker) {
                try{
                    long id = ((GalleryEntry)marker.getTag()).getEntryId();
                    Intent intent = new Intent(getActivity(), DisplayEntryActivity.class);
                    intent.putExtra("ID", id);
                    startActivity(intent);

                } catch (NullPointerException e){
                    Toast.makeText(mApplicationContext, "An Error Occured. Please Try Again Later"
                            , Toast.LENGTH_SHORT).show();
                }

            }
        });
        mMap.setOnInfoWindowCloseListener(new GoogleMap.OnInfoWindowCloseListener() {
            @Override
            public void onInfoWindowClose(Marker marker) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mPreviousLocation, mZoomLevel));
                mCurrentSelection = null;
                mCurrentMarkerSelected = null;
            }
        });
        Log.d("debug", "Map is ready");
        checkPermissions();
        Runnable loadFromCloud = new Runnable() {
            @Override
            public void run() {
                DatabaseReference refUtil = MainActivity.databaseReference.child("users")
                        .child(MainActivity.username);
                Log.d("debug", refUtil.toString());
                refUtil.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String imageEncodedString = null;
                        Bitmap iconBitmap = null;
                        if (dataSnapshot.child("profile").hasChildren()){
                            for (DataSnapshot dssProfile: dataSnapshot.child("profile").getChildren()){
                                imageEncodedString = dssProfile.child("profilePicture").getValue(String.class);
                                if (imageEncodedString != null){
                                    try {
                                        /*Uri image_uri = Uri.parse(imageEncodedString);
                                        InputStream image_stream = getActivity().getContentResolver().openInputStream(image_uri);
                                        Bitmap bitmap= BitmapFactory.decodeStream(image_stream);*/
                                        // Base64 into byte arrays
                                        byte[] decodedString = Base64.decode(imageEncodedString, Base64.DEFAULT);
                                        // byte array into bitmap
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                        // scale bitmap
                                        iconBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, false);
                                    } catch (Exception e){
                                        Log.d("debug", "profile picture not found");
                                    }
                                }
                            }
                        }
                        if (dataSnapshot.child("items").hasChildren()){
                            for (DataSnapshot dss: dataSnapshot.child("items").getChildren()){
                                double lat = dss.child("latitude").getValue(Double.class);
                                double lng = dss.child("longitude").getValue(Double.class);
                                GalleryEntry briefMarkerInfo = new GalleryEntry();
                                briefMarkerInfo.setEntryId(dss.child("entryId").getValue(Long.class));
                                briefMarkerInfo.setLatitude(lat);
                                briefMarkerInfo.setLongitude(lng);
                                briefMarkerInfo.setPicture(dss.child("picture").getValue(String.class));
                                briefMarkerInfo.setSummary(dss.child("summary").getValue(String.class));
                                MarkerOptions markerOptions = new MarkerOptions()
                                        .position(new LatLng(lat, lng))
                                        .alpha((float)0.77);
                                if (iconBitmap != null) {
                                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconBitmap));
                                }
                                else markerOptions.icon(BitmapDescriptorFactory
                                        .defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                Marker marker = mMap.addMarker(markerOptions);
                                marker.setTag(briefMarkerInfo);
                                mAllGalleryEntries.add(marker);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }
        };
        Thread loadGalleryEntryFromCloud = new Thread(loadFromCloud);
        loadGalleryEntryFromCloud.run();

        // BoomButton setup
        mBoomButton = getActivity().findViewById(R.id.boombutton_main);
        mBoomButton.setButtonEnum(ButtonEnum.TextOutsideCircle);
        mBoomButton.setPiecePlaceEnum(PiecePlaceEnum.DOT_5_3);
        mBoomButton.setButtonPlaceEnum(ButtonPlaceEnum.SC_5_3);
        mBoomButton.setBoomEnum(BoomEnum.RANDOM);
        for (int i=0; i<mBoomButton.getPiecePlaceEnum().pieceNumber(); i++){
            switch (i) {
                case 0:
                    TextOutsideCircleButton.Builder builder0 = new TextOutsideCircleButton.Builder()
                            .shadowEffect(true)
                            .normalImageRes(R.drawable.ic_boom_button_current_location)
                            .normalText("Me")
                            .listener(new OnBMClickListener() {
                                @Override
                                public void onBoomButtonClick(int index) {
                                    if (mCurrentMarker != null){
                                        mMap.animateCamera(CameraUpdateFactory
                                                .newLatLngZoom(mCurrentMarker.getPosition(), 16));
                                    }
                                    else Toast.makeText(getActivity()
                                            , "Current Location Not Available", Toast.LENGTH_SHORT).show();
                                }
                            });
                    mBoomButton.addBuilder(builder0);
                    break;
                case 1:
                    TextOutsideCircleButton.Builder builder1 = new TextOutsideCircleButton.Builder()
                            .shadowEffect(true)
                            .normalImageRes(R.drawable.ic_boom_button_random)
                            .normalText("Random")
                            .listener(new OnBMClickListener() {
                                @Override
                                public void onBoomButtonClick(int index) {
                                    if (mAllGalleryEntries != null && mAllGalleryEntries.size() == 0){
                                        if (mCurrentMarkerSelected != null){
                                            mCurrentMarkerSelected.hideInfoWindow();
                                        }
                                        int markerIndex = (int)(Math.random() * (mAllGalleryEntries.size()));
                                        Marker marker = mAllGalleryEntries.get(markerIndex);
                                        LatLng mLoc = marker.getPosition();
                                        double lat = mLoc.latitude + 0.0065;
                                        LatLng cameraLocation = new LatLng(lat, mLoc.longitude);
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cameraLocation, 15));
                                        // TODO: Info Window Does Not Shown Every Time
                                        marker.showInfoWindow();
                                        Log.d("debug", ""+marker.isInfoWindowShown());
                                    }
                                    else Toast.makeText(getActivity()
                                            , "No Posts Are Available", Toast.LENGTH_SHORT).show();
                                }
                            });
                    mBoomButton.addBuilder(builder1);
                    break;
                case 2:
                    TextOutsideCircleButton.Builder builder2 = new TextOutsideCircleButton.Builder()
                            .shadowEffect(true)
                            .normalImageRes(R.drawable.ic_boom_button_add)
                            .normalText("New")
                            .listener(new OnBMClickListener() {
                                @Override
                                public void onBoomButtonClick(int index) {
                                    if (mCurrentMarker == null) Toast.makeText(getActivity()
                                        , "Location cannot be determined, Please try again later"
                                            , Toast.LENGTH_SHORT).show();
                                    else {
                                        Intent newEntry = new Intent(getActivity(), EditNewEntryActivity.class);
                                        newEntry.putExtra("location", mCurrentMarker.getPosition());
                                        startActivity(newEntry);
                                    }
                                }
                            });
                    mBoomButton.addBuilder(builder2);
                    break;
                case 3:
                    TextOutsideCircleButton.Builder builder3 = new TextOutsideCircleButton.Builder()
                            .shadowEffect(true)
                            .normalImageRes(R.drawable.ic_boom_button_add_from_marker)
                            .normalText("New From Marker")
                            .listener(new OnBMClickListener() {
                                @Override
                                public void onBoomButtonClick(int index) {
                                    if (mCustomLocationMarker == null)
                                        Toast.makeText(mApplicationContext
                                                , "Please long click on the map to place a marker first"
                                                , Toast.LENGTH_SHORT).show();
                                    else {
                                        Intent newEntry = new Intent(getActivity(), EditNewEntryActivity.class);
                                        newEntry.putExtra("location", mCustomLocationMarker.getPosition());
                                        startActivity(newEntry);
                                    }
                                }
                            });
                    mBoomButton.addBuilder(builder3);
                    break;
                case 4:
                    TextOutsideCircleButton.Builder builder4 = new TextOutsideCircleButton.Builder()
                            .shadowEffect(true)
                            .normalImageRes(R.drawable.ic_boom_button_add)
                            .normalText("Option 5");
                    mBoomButton.addBuilder(builder4);
                    break;
            }
        }
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
                Intent trackIntent = new Intent(getActivity(), TrackingService.class);
                mApplicationContext.startService(trackIntent);
                mApplicationContext.bindService(trackIntent, this, Context.BIND_AUTO_CREATE);
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
                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(lLoc));
                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lLoc, 15));
                }
                Intent trackIntent = new Intent(getActivity(), TrackingService.class);
                mApplicationContext.startService(trackIntent);
                mApplicationContext.bindService(trackIntent, this, Context.BIND_AUTO_CREATE);
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
                    // else mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currLoc, 17));
                    mCurrentMarker = mMap.addMarker(new MarkerOptions()
                                        .snippet("Current Location")
                                        .position(currLoc)
                                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_current_location)));
            }
        }
    }

}
