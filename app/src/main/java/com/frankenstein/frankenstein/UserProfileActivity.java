package com.frankenstein.frankenstein;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.util.Map;
import java.util.Set;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "USERPROFILE";

    private ViewGroup mTransitionGroup;
    private Button mLogOutButton;
    private TextView mConfirmLogout;
    private View mCancelLogout;
    private boolean textVisible;
    private final Context mContext = this;

    //Varibles for Firebase
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;

    //Toolbar
    android.support.v7.widget.Toolbar toolbar;

    //Profile Edit Variables
    private EditText nickName;
    private EditText email;
    private ImageView imageView;
    private Button changeImage;

    //Change image variables
    private Uri uriPic;
    private Uri uriGallery;
    private final String IMAGE_SRC = "profile_picture.jpg";
    private final String IMAGE_SRC2 = "profile_picture_from_gallery.jpg";
    private final int CAPTURE_IMAGE = 0;
    private final int SELECT_IMAGE = 1;
    private final int CROP_IMAGE = 2;
    private Uri mImageSource = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //Set up picture changing varibles
        File file = new File(Environment.getExternalStorageDirectory(), IMAGE_SRC);
        uriPic = FileProvider.getUriForFile(mContext, "com.frankenstein.frankenstein.fileprovider", file);
        File file2 = new File(Environment.getExternalStorageDirectory(), IMAGE_SRC2);
        uriGallery = FileProvider.getUriForFile(mContext, "com.frankenstein.frankenstein.fileprovider", file2);

        //Get the Firebase Instance
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        String username = mFirebaseUser.getUid();

        //Set up Profile items
        changeImage = findViewById(R.id.btnChangePhoto);
        nickName = (EditText) findViewById(R.id.editNickName);
        email = (EditText) findViewById(R.id.editEmail);
        imageView = (ImageView) findViewById(R.id.profileImage);
        //Do not let the user change their email
        email.setKeyListener(null);

        //Listener for when the user CLicks change image
        changeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder ab = new AlertDialog.Builder(mContext);
                ab.setTitle("Pick Profile Picture");
                ab.setItems(R.array.update_profile_picture, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                if (takePic.resolveActivity(getPackageManager()) != null) {
                                    takePic.putExtra(MediaStore.EXTRA_OUTPUT, uriPic);
                                    startActivityForResult(takePic, CAPTURE_IMAGE);
                                }
                                break;
                            case 1:
                                Intent getPic = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                if (getPic.resolveActivity(getPackageManager()) != null) {
                                    getPic.putExtra(MediaStore.EXTRA_OUTPUT, uriGallery);
                                    startActivityForResult(getPic, SELECT_IMAGE);
                                }
                                break;
                        }
                    }
                });
                ab.show();
            }
        });

        //This is to load the UserName and Image from FireBase
        DatabaseReference nickname = mDatabase.child("users").child(username).child("profile");
        nickname.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot dss : dataSnapshot.getChildren()) {
                        String usernam = (String) dss.child("username").getValue();
                        String profilePicURI = (String) dss.child("profilePicture").getValue();
                        if (profilePicURI != null){
                            Log.d(TAG, "UserProfile: The PicUri from FireBase was null");
                            imageView.setImageURI(Uri.parse(profilePicURI));
                        }
                        else imageView.setImageResource(R.drawable.ic_signup_image_placeholder);
                        nickName.setText(usernam);
                        email.setText(mFirebaseUser.getEmail());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Set up the Toolbar
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbarForUserProfile);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("User Profile");

        //Set up the LogOut Animation
        mTransitionGroup = findViewById(R.id.transitionContainer_logout);
        mConfirmLogout = findViewById(R.id.textView_confirm_logout);
        textVisible = false;
        mConfirmLogout.setVisibility(View.GONE);
        mCancelLogout = findViewById(R.id.profile_activity_clickListenerView);
        mLogOutButton = findViewById(R.id.button_logout);



        //Listen for Logout Button Clicked
        mLogOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "LogOut Clicked");
                if (textVisible){
                    mFirebaseAuth.signOut();
                    Intent login_intent = new Intent(mContext, LogInActivity.class);
                    login_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(login_intent);
                }
                else {
                    com.transitionseverywhere.TransitionManager.beginDelayedTransition(mTransitionGroup);
                    textVisible = !textVisible;
                    mConfirmLogout.setVisibility(View.VISIBLE);
                }
            }
        });


        mCancelLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "OnClicked ScrollView");
                if (textVisible){
                    com.transitionseverywhere.TransitionManager.beginDelayedTransition(mTransitionGroup);
                    textVisible = !textVisible;
                    mConfirmLogout.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (textVisible){
            com.transitionseverywhere.TransitionManager.beginDelayedTransition(mTransitionGroup);
            textVisible = !textVisible;
            mConfirmLogout.setVisibility(View.GONE);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAPTURE_IMAGE:
                    Log.d("debug", "case 1");
                    Crop.of(uriPic, uriPic).asSquare().start(this, CROP_IMAGE);
                    break;
                case CROP_IMAGE:
                    Log.d("debug", "case crop image");
                    Log.d("debug", "case 2");
                    Uri image_crop = Crop.getOutput(data);
                    mImageSource = image_crop;
                    imageView.setImageURI(null);
                    imageView.setImageURI(image_crop);
                    break;
                case SELECT_IMAGE:
                    Log.d("debug", "case Select Image");
                    if (data.getData() != null) {
                        uriGallery = data.getData();
                    }
                    Crop.of(uriGallery, uriPic).asSquare().start(this, CROP_IMAGE);
                    break;
            }
        }
        else {
            Log.d("fail", "result code failed");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Listen to for when the user presses the save Button
        if (item.getItemId() == R.id.action_save) {
            String username = mFirebaseUser.getUid();
            final DatabaseReference profile = mDatabase.child("users").child(username).child("profile");
            profile.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        for (DataSnapshot dss : dataSnapshot.getChildren()) {
                            String key = (String) dss.getKey();
                            if (nickName != null)
                                profile.child(key).child("username").setValue(nickName.getText().toString().trim());
                            if (mImageSource != null)
                                profile.child(key).child("profilePicture").setValue(mImageSource.toString());
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            Toast.makeText(this, "Changes have been saved", Toast.LENGTH_SHORT).show();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}

/*

// Create a storage reference from our app
StorageReference storageRef = storage.getReference();

// Create a reference to "mountains.jpg"
StorageReference mountainsRef = storageRef.child("mountains.jpg");

// Create a reference to 'images/mountains.jpg'
StorageReference mountainImagesRef = storageRef.child("images/mountains.jpg");

// While the file names are the same, the references point to different files
mountainsRef.getName().equals(mountainImagesRef.getName());    // true
mountainsRef.getPath().equals(mountainImagesRef.getPath());    // false

 */
