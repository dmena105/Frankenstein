package com.frankenstein.frankenstein;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String TAG = "TESTING123";
    private FirebaseUser mFirebaseUser;
    public static String username;
    public static DatabaseReference databaseReference;
    private String nickname;
    private String profileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        username = mFirebaseUser.getUid();
        Log.d("debug", "username: " + username);
        int mode = getIntent().getIntExtra("mode", 1);
        if (mode == 0){
            nickname = getIntent().getStringExtra("nickname");
            profileUri = getIntent().getStringExtra("profile");
            if (nickname != null) databaseReference.child("users").child(username)
                    .child("profile").child("username").getRef().setValue(nickname);
            if (profileUri != null) databaseReference.child("users").child(username)
                    .child("profile").child("profilePicture").getRef().setValue(profileUri);
        }
        else {
            DatabaseReference refUtil = databaseReference.child("users").child(username).child("profile");
            refUtil.orderByChild("username");
            refUtil.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()){
                        for (DataSnapshot dss: dataSnapshot.getChildren()){
                            nickname = dss.child("username").getValue(String.class);
                            profileUri = dss.child("profilePicture").getValue(String.class);
                            Log.d("debug", "profile URi: " + profileUri);
                            Log.d("debug", "nickname" + nickname);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }
        // TODO: Use if statement here to start different fragment at different times
        // Map Fragment
        com.frankenstein.frankenstein.MapFragment mapFragment = new com.frankenstein.frankenstein.MapFragment();
        getFragmentManager().beginTransaction().replace(R.id.main_frame, mapFragment).commit();
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
        if (nickname != null)
            ((TextView)v.findViewById(R.id.textView_mainDrawer_nickname)).setText(nickname);
        if (profileUri != null)
            ((ImageView)v.findViewById(R.id.imageView_mainDrawer)).setImageURI(Uri.parse(profileUri));
        else ((ImageView)v.findViewById(R.id.imageView_mainDrawer))
                .setImageResource(R.drawable.ic_signup_image_placeholder);
        TextView navViewEmail = v.findViewById(R.id.emailTextView);
        navViewEmail.setText(mFirebaseUser.getEmail());

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
                startActivity(intent);
            }
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


}
