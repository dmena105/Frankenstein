package com.frankenstein.frankenstein;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.nightonke.boommenu.Animation.BoomEnum;
import com.nightonke.boommenu.BoomButtons.BoomButton;
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.TextOutsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;

import java.util.ArrayList;

/**
 * Created by Timothyqiu on 2018-03-03.
 */

public class BoomButtonDisplayMain {
    private BoomMenuButton mBoomButton;
    private final Context context;
    private final GoogleMap mMap;
    private Marker mCurrentMarker;
    private ArrayList<Marker> mAllMarkers;
    private Marker mCurrentMarkerSelected;
    private Marker mCustomLocationMarker;
    private DisplayObject mDisplayObject;
    private float mAzimuth;

    public void setCurrentMarker(Marker mCurrentMarker) {
        this.mCurrentMarker = mCurrentMarker;
    }

    public void setAllMarkers(ArrayList<Marker> mAllMarkers) {
        this.mAllMarkers = mAllMarkers;
    }

    public void setCurrentMarkerSelected(Marker mCurrentMarkerSelected) {
        this.mCurrentMarkerSelected = mCurrentMarkerSelected;
    }

    public void setCustomLocationMarker(Marker mCustomLocationMarker) {
        this.mCustomLocationMarker = mCustomLocationMarker;
    }

    // Constructor for map fragment
    public BoomButtonDisplayMain(BoomMenuButton boomMenuButton, Context context
            , GoogleMap mMap, Marker mCurrentMarker, ArrayList<Marker> mAllMarkers
            , Marker mCurrentMarkerSelected, Marker mCustomLocationMarker, float mAzimuth){
        mBoomButton = boomMenuButton;
        this.context = context;
        this.mMap = mMap;
        this.mCurrentMarker = mCurrentMarker;
        this.mAllMarkers = mAllMarkers;
        this.mCurrentMarkerSelected = mCurrentMarkerSelected;
        this.mCustomLocationMarker = mCustomLocationMarker;
        this.mAzimuth = mAzimuth;
    }

    // Constructor for AR display
    public BoomButtonDisplayMain(BoomMenuButton boomMenuButton, Context context){
        mBoomButton = boomMenuButton;
        mMap = null;
        this.context = context;
    }

    public void mapFragmentDisplay(){
        if (mBoomButton.getBuilders().size() != 0) mBoomButton.clearBuilders();
        try {
            // boom menu button general setup
            mBoomButton.setInFragment(true);
            mBoomButton.setPiecePlaceEnum(PiecePlaceEnum.DOT_5_3);
            mBoomButton.setButtonPlaceEnum(ButtonPlaceEnum.SC_5_3);
            mBoomButton.setBoomEnum(BoomEnum.RANDOM);
            mBoomButton.setButtonEnum(ButtonEnum.TextOutsideCircle);
            Log.d("debug", ""+ mBoomButton.getButtonEnum().toString());
            // All builders
            for (int i = 0; i < mBoomButton.getPiecePlaceEnum().pieceNumber(); i++) {
                switch (i) {
                    case 0:
                        TextOutsideCircleButton.Builder builder0 = new TextOutsideCircleButton.Builder()
                                .shadowEffect(true)
                                .normalImageRes(R.drawable.ic_boom_button_current_location)
                                .normalText("Me")
                                .listener(new OnBMClickListener() {
                                    @Override
                                    public void onBoomButtonClick(int index) {
                                        if (mCurrentMarker != null) {
                                            mMap.animateCamera(CameraUpdateFactory
                                                    .newLatLngZoom(mCurrentMarker.getPosition(), 16));
                                        } else Toast.makeText(context
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
                                        if (mAllMarkers.size() != 0 && mAllMarkers != null) {
                                            if (mCurrentMarkerSelected != null) {
                                                mCurrentMarkerSelected.hideInfoWindow();
                                            }
                                            int markerIndex = (int) (Math.random()
                                                    * (mAllMarkers.size()));
                                            Marker marker = mAllMarkers.get(markerIndex);
                                            LatLng cameraLocation = marker.getPosition();
                                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cameraLocation, 16));
                                            marker.showInfoWindow();
                                            Log.d("debug", "" + marker.isInfoWindowShown());
                                        } else Toast.makeText(context
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
                                        if (mCurrentMarker == null) Toast.makeText(context
                                                , "Location cannot be determined, Please try again later"
                                                , Toast.LENGTH_SHORT).show();
                                        else {
                                            Intent newEntry = new Intent(context, EditNewEntryActivity.class);
                                            newEntry.putExtra("location", mCurrentMarker.getPosition());
                                            newEntry.putExtra("azimuth", Global.mapFragment.getAzimuth());
                                            newEntry.putExtra("origin", 0);
                                            context.startActivity(newEntry);
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
                                            Toast.makeText(context
                                                    , "Please long click on the map to place a marker first"
                                                    , Toast.LENGTH_SHORT).show();
                                        else {
                                            Intent newEntry = new Intent(context, EditNewEntryActivity.class);
                                            newEntry.putExtra("location", mCustomLocationMarker.getPosition());
                                            newEntry.putExtra("azimuth", Global.mapFragment.getAzimuth());
                                            newEntry.putExtra("origin", 0);
                                            context.startActivity(newEntry);
                                        }
                                    }
                                });
                        mBoomButton.addBuilder(builder3);
                        break;
                    case 4:
                        TextOutsideCircleButton.Builder builder4 = new TextOutsideCircleButton.Builder()
                                .shadowEffect(true)
                                .normalImageRes(R.drawable.ic_zoom_out)
                                .normalText("Zoom Out To All Posts")
                                .listener(new OnBMClickListener() {
                                    @Override
                                    public void onBoomButtonClick(int index) {
                                        if (mAllMarkers.size() == 0){
                                            Toast.makeText(context, "No posts are available yet, Please try again later"
                                                    , Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            try {
                                                Marker marker = mAllMarkers.get(0);
                                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 2));
                                            } catch (Exception e) {
                                                Toast.makeText(context, "Loading your post. Please try again later"
                                                        , Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                        mBoomButton.addBuilder(builder4);
                        break;
                }
            }
        } catch (Exception e){
            Log.d("debug", "boom button error");
        }
    }

    public void arFragmentDisplay(){
        if (mBoomButton.getBuilders().size() != 0) mBoomButton.clearBuilders();
        try {
            // Boom menu button setup
            mBoomButton.setInFragment(true);
            mBoomButton.setButtonEnum(ButtonEnum.Ham);
            mBoomButton.setPiecePlaceEnum(PiecePlaceEnum.HAM_1);
            mBoomButton.setButtonPlaceEnum(ButtonPlaceEnum.HAM_1);
            mBoomButton.setBoomEnum(BoomEnum.RANDOM);
            // All builders
            for (int i = 0; i < mBoomButton.getPiecePlaceEnum().pieceNumber(); i++) {
                switch (i) {
                    case 0:
                        HamButton.Builder builder2 = new HamButton.Builder()
                                .shadowEffect(true)
                                .normalImageRes(R.drawable.ic_boom_button_add)
                                .normalText("New")
                                .listener(new OnBMClickListener() {
                                    @Override
                                    public void onBoomButtonClick(int index) {
                                        // If current location is null, show a toast
                                        if (MapFragment.mCurrentMarker == null) Toast.makeText(context
                                                , "Location cannot be determined, Please try again later"
                                                , Toast.LENGTH_SHORT).show();
                                        else {
                                            // If current location is available, start editnewentryactivity
                                            Intent newEntry = new Intent(context, EditNewEntryActivity.class);
                                            newEntry.putExtra("location", Global.mapFragment.mCurrentMarker.getPosition());
                                            newEntry.putExtra("azimuth", Global.mapFragment.getAzimuth());
                                            newEntry.putExtra("origin", 1);
                                            context.startActivity(newEntry);
                                        }
                                    }
                                });
                        mBoomButton.addBuilder(builder2);
                        break;
                }
            }
        } catch (Exception e){
            Log.d("debug", "Boom button error");
        }
    }

}
