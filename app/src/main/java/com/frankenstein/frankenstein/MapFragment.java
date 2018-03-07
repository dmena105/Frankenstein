package com.frankenstein.frankenstein;

import android.Manifest;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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
import android.support.design.widget.FloatingActionButton;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.internal.zzp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.nightonke.boommenu.Animation.BoomEnum;
import com.nightonke.boommenu.BoomButtons.BoomButtonBuilder;
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
import java.util.Iterator;
import java.util.TreeMap;

public class MapFragment extends android.app.Fragment implements OnMapReadyCallback{
    public static GoogleMap mMap;
    private MapView mMapView;
    public static Marker mCurrentMarker = null;
    public static GalleryEntry mCurrentSelection;
    private float mZoomLevel;
    private LatLng mPreviousLocation;
    private ImageView mImageViewDialog;
    private TextView mTextViewTime;
    private TextView mTextViewSummary;
    private BoomMenuButton mBoomButton;
    private Marker mCurrentMarkerSelected;
    private Marker mCustomLocationMarker;
    public static ClusterManager<ClusteredMarker> mClusterManager;
    public static ArrayList<Marker> mAllMarkers;
    public static BoomButtonDisplayMain boomDisplay;
    public static TreeMap<String, String> mPictureCache;
    public static boolean mapIsReady = false;
    private final Context mContext = getActivity();
    private float mAzimuth;

    public void setmAzimuth(float azimuth){
        mAzimuth = azimuth;
        Log.d("gb", "Map's azimuth = "+azimuth);
    }

    public float getAzimuth(){
        return mAzimuth;
    }

    public double[] getLatLng(){
        double[] ret = new double[2];
        if(mCurrentMarker != null){
            ret[0] = mCurrentMarker.getPosition().latitude;
            ret[1] = mCurrentMarker.getPosition().longitude;
        } else {
            ret = null;
        }
        return ret;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(mAllMarkers == null || mPictureCache == null) {
            mAllMarkers = new ArrayList<>();
            mPictureCache = new TreeMap<>();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mBoomButton = getActivity().findViewById(R.id.boombutton_mainMap);
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
        mClusterManager = null;
        Log.d("debug", "builder cleared Map");
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
        mapIsReady = true;
        mClusterManager = new ClusterManager<>(getActivity(), mMap);
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<ClusteredMarker>() {
            @Override
            public boolean onClusterItemClick(final ClusteredMarker clusteredMarker) {
                mCurrentSelection = clusteredMarker.getGalleryEntry();
                mZoomLevel = mMap.getCameraPosition().zoom;
                mPreviousLocation = mMap.getCameraPosition().target;
                LatLng mLoc = new LatLng(mCurrentSelection.getLatitude(), mCurrentSelection.getLongitude());
                double lat = mLoc.latitude + 0.0065;
                LatLng cameraLocation = new LatLng(lat, mLoc.longitude);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cameraLocation, 15));
                String posKey = clusteredMarker.getPosition().latitude + "_" + clusteredMarker.getPosition().longitude;
                if (!mPictureCache.containsKey(posKey)) {
                    final DatabaseReference refUtil = MainActivity.databaseReference.child("users")
                            .child(MainActivity.username).child("items");
                    refUtil.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChildren()) {
                                for (DataSnapshot dss : dataSnapshot.getChildren()) {
                                    if (clusteredMarker.getPosition().latitude == dss.child("latitude").getValue(Double.class)
                                            && clusteredMarker.getPosition().longitude == dss.child("longitude").getValue(Double.class)) {
                                        String encodedImage = dss.child("picture").getValue(String.class);
                                        mCurrentSelection.setPicture(encodedImage);
                                        String posKey = clusteredMarker.getPosition().latitude + "_" + clusteredMarker.getPosition().longitude;
                                        mPictureCache.put(posKey, encodedImage);
                                        break;
                                    }
                                }
                            }
                            displayMarker(clusteredMarker);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
                else {
                    mCurrentSelection.setPicture(mPictureCache.get(posKey));
                    displayMarker(clusteredMarker);
                }
                return true;
            }
        });

        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<ClusteredMarker>() {
            @Override
            public boolean onClusterClick(Cluster<ClusteredMarker> cluster) {
                if (cluster.getSize() < 5)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cluster.getPosition(), 16));
                else if (cluster.getSize() < 10)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cluster.getPosition(), 14));
                else
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cluster.getPosition(), 13));
                return true;
            }
        });
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnCameraIdleListener(mClusterManager);
        MarkerRenderer renderer = new MarkerRenderer(getActivity(), mMap);
        mClusterManager.setRenderer(renderer);

        mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                if (mCurrentSelection != null){
                    View window = getLayoutInflater().inflate(R.layout.post_snapshot_popup_window, null);
                    mImageViewDialog = window.findViewById(R.id.imageView_dialog);
                    mTextViewTime = window.findViewById(R.id.textView_dialogTime);
                    mTextViewSummary = window.findViewById(R.id.textView_dialogSummary);
                    if (mCurrentSelection.getPicture() != null) {
                        byte[] decodedString = Base64.decode(mCurrentSelection.getPicture(), Base64.DEFAULT);
                        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        mImageViewDialog.setImageBitmap(decodedBitmap);
                    }
                    else {
                        mImageViewDialog.setImageResource(R.drawable.ic_signup_image_placeholder);
                    }
                    DateFormat formatter = SimpleDateFormat.getDateTimeInstance();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(mCurrentSelection.getPostTime());
                    mTextViewTime.setText(formatter.format(calendar.getTime()));
                    mTextViewSummary.setText(mCurrentSelection.getSummary());
                    return window;
                }
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });
        mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());

        mMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
            @Override
            public void onInfoWindowLongClick(Marker marker) {
                try{
                    Intent intent = new Intent(getActivity(), DisplayEntryActivity.class);
                    startActivity(intent);

                } catch (NullPointerException e){
                    Toast.makeText(mContext, "An Error Occured. Please Try Again Later"
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

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (mCustomLocationMarker != null) {
                    mCustomLocationMarker.remove();
                    mCustomLocationMarker = null;
                }
                mCustomLocationMarker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                        .snippet(latLng.toString()));
                if (boomDisplay != null) boomDisplay.setCustomLocationMarker(mCustomLocationMarker);
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mCustomLocationMarker != null) {
                    mCustomLocationMarker.remove();
                    mCustomLocationMarker = null;
                    if (boomDisplay != null) boomDisplay.setCustomLocationMarker(null);
                }
            }
        });
        boomDisplay = new BoomButtonDisplayMain(mBoomButton, getActivity()
                , mMap, mCurrentMarker, mAllMarkers, mCurrentMarkerSelected, mCustomLocationMarker, mAzimuth);
        boomDisplay.mapFragmentDisplay();
        Log.d("debug", "Map is ready");
        LocationManager locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
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
        new LoadFromCloud().start();
    }


    private void displayMarker(ClusteredMarker clusteredMarker){
        for (Marker marker : mClusterManager.getMarkerCollection().getMarkers()) {
            if (marker.getPosition().latitude == clusteredMarker.getPosition().latitude &&
                    marker.getPosition().longitude == clusteredMarker.getPosition().longitude) {
                mCurrentMarkerSelected = marker;
                if (boomDisplay != null)
                    boomDisplay.setCurrentMarkerSelected(mCurrentMarkerSelected);
                marker.showInfoWindow();
                break;
            }
        }
    }

    public class LoadFromCloud extends Thread{
        Handler handler = new Handler();
        Runnable loadFromCloud = new Runnable() {
            @Override
            public void run() {
                DatabaseReference refUtil = MainActivity.databaseReference.child("users")
                        .child(MainActivity.username);
                Log.d("debug", refUtil.toString());
                Query query = refUtil.orderByChild("latitude");
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String imageEncodedString = null;
                        Bitmap iconBitmap = null;
                        if (dataSnapshot.child("profile").hasChildren()) {
                            for (DataSnapshot dssProfile : dataSnapshot.child("profile").getChildren()) {
                                imageEncodedString = dssProfile.child("profilePicture").getValue(String.class);
                                if (imageEncodedString != null) {
                                    try {
                                        // Base64 into byte arrays
                                        byte[] decodedString = Base64.decode(imageEncodedString, Base64.DEFAULT);
                                        // byte array into bitmap
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                        // scale bitmap
                                        iconBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, false);
                                    } catch (Exception e) {
                                        Log.d("debug", "profile picture not found");
                                    }
                                }
                            }
                        }
                        try {
                            if (dataSnapshot.child("items").hasChildren()) {
                                for (DataSnapshot dss : dataSnapshot.child("items").getChildren()) {
                                    double lat = dss.child("latitude").getValue(Double.class);
                                    double lng = dss.child("longitude").getValue(Double.class);
                                    GalleryEntry briefMarkerInfo = new GalleryEntry();
                                    briefMarkerInfo.setEntryId(dss.child("entryId").getValue(Long.class));
                                    briefMarkerInfo.setLatitude(lat);
                                    briefMarkerInfo.setLongitude(lng);
                                    briefMarkerInfo.setPostTime(dss.child("postTime").getValue(Long.class));
                                    briefMarkerInfo.setSummary(dss.child("summary").getValue(String.class));
                                    mClusterManager.addItem(new ClusteredMarker(briefMarkerInfo, iconBitmap));
                                    MarkerOptions markerOptions = new MarkerOptions()
                                            .position(new LatLng(lat, lng))
                                            .visible(false);
                                    Marker marker = mMap.addMarker(markerOptions);
                                    marker.setTag(briefMarkerInfo);
                                    mAllMarkers.add(marker);
                                }
                                if (boomDisplay != null) boomDisplay.setAllMarkers(mAllMarkers);
                                mClusterManager.cluster();
                            }
                        } catch(Exception e){
                            Log.d("debug", "NULL POINTER FOR BOOM MENU");
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }
        };

        @Override
        public void run(){
            handler.post(loadFromCloud);
        }
    }

    class MarkerRenderer extends DefaultClusterRenderer<ClusteredMarker>{
        private final Context mContext;
        public MarkerRenderer(Context context, GoogleMap map) {
            super(context, map, mClusterManager);
            mContext = context;
        }
        @Override
        protected void onBeforeClusterItemRendered(ClusteredMarker item,
                                                   MarkerOptions markerOptions) {
            final BitmapDescriptor markerDescriptor;
            if(item.getProfilePicture() != null){
                markerDescriptor = BitmapDescriptorFactory.fromBitmap(item.getProfilePicture());
            } else {
                markerDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
            }
            markerOptions
                    .icon(markerDescriptor)
                    .alpha((float)0.77);
        }
    }
}
