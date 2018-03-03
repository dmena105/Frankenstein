package com.frankenstein.frankenstein;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.MapFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nightonke.boommenu.Animation.BoomEnum;
import com.nightonke.boommenu.BoomButtons.BoomButton;
import com.nightonke.boommenu.BoomButtons.BoomButtonBuilder;
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.TextInsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import static android.content.Context.SENSOR_SERVICE;
import static java.lang.Math.abs;
import static java.lang.Math.toDegrees;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SensorEventListener {

    private String TAG = "TESTING123";
    private FirebaseUser mFirebaseUser;
    public static String username;
    public static DatabaseReference databaseReference;
    public static long itemcount;
    private String nickname;
    private String profileUri;
    private ImageView mImageViewProfilePic;
    private TextView mTextViewNickname;
    private int mode = 0;
    private int switchAngle = 20;
    private BoomMenuButton mMapButton;
    private BoomMenuButton mARButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapButton = findViewById(R.id.boombutton_mainMap);
        mARButton = findViewById(R.id.boombutton_mainAR);
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        username = mFirebaseUser.getUid();
        //Log.d("debug", "username: " + username);
        Global.mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        Global.accelerometer = Global.mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Global.magnetometer = Global.mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        Global.arFragment= new ARFragment();
        // Map Fragment
        Global.mapFragment = new com.frankenstein.frankenstein.MapFragment();
        getFragmentManager().beginTransaction().replace(R.id.main_frame, Global.arFragment).commit();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //Find the NavView, and then set the users Email On the Header
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View v = navigationView.getHeaderView(0);

        TextView navViewEmail = v.findViewById(R.id.emailTextView);
        navViewEmail.setText(mFirebaseUser.getEmail());

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
                startActivity(intent);
            }
        });
        mImageViewProfilePic = v.findViewById(R.id.imageView_mainDrawer);
        mTextViewNickname = v.findViewById(R.id.textView_mainDrawer_nickname);
        // 0 is sign up activity, 1 is sign-in activity
        int mode = getIntent().getIntExtra("mode", 1);
        if (mode == 0){
            nickname = getIntent().getStringExtra("nickname");
            profileUri = getIntent().getStringExtra("profile");
            final DatabaseReference refUtil = databaseReference.child("users")
                    .child(username).child("profile").push();
            if (nickname != null) {
                refUtil.child("username").setValue(nickname);
                mTextViewNickname.setText(nickname);
            }
            if (profileUri != null) {
                Thread saveProfilePic = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Uri to Bitmap
                            InputStream image_stream = getContentResolver().openInputStream(Uri.parse(profileUri));
                            Bitmap bitmap = BitmapFactory.decodeStream(image_stream);
                            // Bitmap to Base64 String
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                            byte[] byteArray = byteArrayOutputStream .toByteArray();
                            String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
                            // Save Base64 to Firebase
                            refUtil.child("profilePicture").setValue(encodedImage);
                            mImageViewProfilePic.setImageURI(Uri.parse(profileUri));
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                saveProfilePic.start();
            }
            else ((ImageView)v.findViewById(R.id.imageView_mainDrawer))
                    .setImageResource(R.drawable.ic_signup_image_placeholder);
        }
        else {
            Thread loadProfilePic = new Thread(new Runnable() {
                @Override
                public void run() {
                    DatabaseReference refUtil = databaseReference.child("users").child(username);
                    refUtil.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child("items").hasChildren())
                                itemcount = dataSnapshot.child("items").getChildrenCount();

                            if (dataSnapshot.child("profile").hasChildren()){
                                for (DataSnapshot dss: dataSnapshot.child("profile").getChildren()){
                                    nickname = dss.child("username").getValue(String.class);
                                    String encodedImage = dss.child("profilePicture").getValue(String.class);
                                    if (encodedImage != null) {
                                        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                        mImageViewProfilePic.setImageBitmap(decodedByte);
                                        // TODO: Put this into a buffer - SQL username->profile image
                                    }
                                    else mImageViewProfilePic.setImageResource(R.drawable.ic_signup_image_placeholder);
                                    if (nickname != null) mTextViewNickname.setText(nickname);
                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
                }
            });
            loadProfilePic.start();
        }

        //Listener that allows for the nav view to update when firebase changes somethings
        //This is mainly useful for when we return from the USERPROFILE ACTIVITY
        String username = mFirebaseUser.getUid();
        DatabaseReference navViewUpdate = databaseReference.child("users").child(username).child("profile");
        navViewUpdate.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {}
            //Load the data into the NAV view
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                for (DataSnapshot dss : dataSnapshot.getChildren()) {
                    nickname = dss.child("username").getValue(String.class);
                    String encodedImage = dss.child("profilePicture").getValue(String.class);
                    if (encodedImage != null) {
                        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        mImageViewProfilePic.setImageBitmap(decodedByte);
                    }
                    else mImageViewProfilePic.setImageResource(R.drawable.ic_signup_image_placeholder);
                    if (nickname != null) mTextViewNickname.setText(nickname);
                }
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return false;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_gallery) {
            startActivity(new Intent(this, GalleryTimeline.class));

        } else if (id == R.id.nav_personal_profile){
            startActivity(new Intent(this, UserProfileActivity.class));
        } else if (id == R.id.nav_setting){
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putLong("itemcount", itemcount);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        itemcount = savedInstanceState.getLong("itemcount");
    }

    @Override
    public void onResume(){
        super.onResume();
        Global.mSensorManager.registerListener(this, Global.accelerometer, SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM);
        Global.mSensorManager.registerListener(this, Global.magnetometer, SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM);
    }

    @Override
    public void onPause(){
        super.onPause();
        Global.mSensorManager.unregisterListener(this);
    }

    float[] mGravity;
    float[] mGeomagnetic;
    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("gb", "Main");
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = Global.arFragment.lowPassFilter(event.values, mGravity);
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = Global.arFragment.lowPassFilter(event.values, mGeomagnetic);
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                Log.d("s1", ""+toDegrees(orientation[1]));
                if(abs(toDegrees(orientation[1])) < switchAngle && mode == 0){
                    Log.d("s1", "Going to map");
                    getFragmentManager().beginTransaction().replace(com.frankenstein.frankenstein.R.id.main_frame, Global.mapFragment).commit();
                    mode = 1;
                    mARButton.setVisibility(View.GONE);
                    mMapButton.setVisibility(View.VISIBLE);
                } else if(abs(toDegrees(orientation[1])) >= switchAngle && mode == 1){
                    Log.d("s1", "Going to ar");
                    getFragmentManager().beginTransaction().replace(com.frankenstein.frankenstein.R.id.main_frame, Global.arFragment).commit();
                    Global.arFragment.onSensorChanged(orientation);
                    mode = 0;
                    mARButton.setVisibility(View.VISIBLE);
                    mMapButton.setVisibility(View.GONE);
                } else if (abs(toDegrees(orientation[1])) >= switchAngle){
                    Log.d("s1", "updating ar");
                    Global.arFragment.onSensorChanged(orientation);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

}
