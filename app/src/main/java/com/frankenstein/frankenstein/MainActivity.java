package com.frankenstein.frankenstein;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nightonke.boommenu.Animation.BoomEnum;
import com.nightonke.boommenu.BoomButtons.BoomButton;
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.TextInsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String TAG = "TESTING123";
    private FirebaseUser mFirebaseUser;
    public static String username;
    public static DatabaseReference databaseReference;
    public static long itemcount;
    private String nickname;
    private String profileUri;
    private ImageView mImageViewProfilePic;
    private TextView mTextViewNickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        username = mFirebaseUser.getUid();
        Log.d("debug", "username: " + username);

        ARFragment arFragment= new ARFragment();
        // Map Fragment
        com.frankenstein.frankenstein.MapFragment mapFragment = new com.frankenstein.frankenstein.MapFragment();
        getFragmentManager().beginTransaction().replace(R.id.main_frame, arFragment).commit();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Example of what do when one of the Fab Buttons is Clicked
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab1);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

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
        // 0 is sign up activity, 1 is signin activity
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
                                        Log.d("debug", "loading");
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

}
