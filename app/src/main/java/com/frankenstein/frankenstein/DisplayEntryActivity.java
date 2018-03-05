package com.frankenstein.frankenstein;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.nightonke.boommenu.Animation.BoomEnum;
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;
import com.transitionseverywhere.ChangeBounds;
import com.transitionseverywhere.ChangeImageTransform;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DisplayEntryActivity extends AppCompatActivity {
    private ViewGroup mTransitionGroup;
    private TextView mConfirmDelete;
    private boolean textVisible;
    private Button mDeleteButton;
    private View mCancelDelete;
    private LatLng location;
    private BoomMenuButton mBoomButton;
    private GalleryEntry entryToDisplay;
    private ImageView mImageView;
    private TextView mTimePostedText;
    private TextView mUsernameText;
    private TextView mSummaryText;
    private TextView mTextText;
    private ImageView mImageViewProfile;
    private boolean expanded = false;
    private final Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_entry);
        if (MapFragment.mCurrentSelection == null) {
            Toast.makeText(this, "An Error Occurred", Toast.LENGTH_SHORT).show();
            finish();
        }
        else {
            entryToDisplay = MapFragment.mCurrentSelection;
            mTransitionGroup = findViewById(R.id.transitionContainer_delete);
            mConfirmDelete = findViewById(R.id.textView_confirm_delete);
            textVisible = false;
            mConfirmDelete.setVisibility(View.GONE);
            mCancelDelete = findViewById(R.id.profile_activity_clickListenerView);
            mDeleteButton = findViewById(R.id.button_delete_entry);
            mImageView = findViewById(R.id.imageView_displayEntry);
            mTimePostedText = findViewById(R.id.textView_displayEntry_time);
            mUsernameText = findViewById(R.id.textView_displayEntry_user);
            mSummaryText = findViewById(R.id.textView_displayEntry_summary);
            mTextText = findViewById(R.id.textView_displayEntry_Text);
            mImageViewProfile = findViewById(R.id.imageView_displayEntry_profile);
            mBoomButton = findViewById(R.id.boombutton_display_entry);

            mDeleteButton.setVisibility(View.GONE);
            mSummaryText.setText("Loading. Please wait...");
            final DatabaseReference refUtil = MainActivity.databaseReference.child("users").child(MainActivity.username);
            refUtil.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // TODO: Maybe change to Uri
                    if (dataSnapshot.child("profile").hasChildren()){
                        for (DataSnapshot dssProfile: dataSnapshot.child("profile").getChildren()){
                            String encodedImage = dssProfile.child("profilePicture").getValue(String.class);
                            // into byte array
                            byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                            // byte array into bitmap
                            mImageViewProfile.setImageBitmap(BitmapFactory
                                    .decodeByteArray(decodedString, 0, decodedString.length));
                            mUsernameText.setText(dssProfile.child("username").getValue(String.class));
                        }
                    }
                    if (dataSnapshot.child("items").hasChildren()){
                        for (DataSnapshot dss: dataSnapshot.child("items").getChildren()){
                            if (dss.child("latitude").getValue(Double.class) == entryToDisplay.getLatitude()
                                    && dss.child("longitude").getValue(Double.class) == entryToDisplay.getLongitude()){
                                mTextText.setText(dss.child("postText").getValue(String.class));
                            }
                        }
                    }
                    byte[] decodedString = Base64.decode(entryToDisplay.getPicture(), Base64.DEFAULT);
                    // byte array into bitmap
                    mImageView.setImageBitmap(BitmapFactory
                            .decodeByteArray(decodedString, 0, decodedString.length));
                    DateFormat formatter = SimpleDateFormat.getDateTimeInstance();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(entryToDisplay.getPostTime());
                    mTimePostedText.setText(formatter.format(calendar.getTime()));
                    mSummaryText.setText(entryToDisplay.getSummary());
                    mDeleteButton.setVisibility(View.VISIBLE);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
            // Setting up the boom button
            mBoomButton.setButtonEnum(ButtonEnum.Ham);
            mBoomButton.setBoomEnum(BoomEnum.RANDOM);
            mBoomButton.setPiecePlaceEnum(PiecePlaceEnum.HAM_3);
            mBoomButton.setButtonPlaceEnum(ButtonPlaceEnum.HAM_3);
            for (int i=0; i<mBoomButton.getPiecePlaceEnum().pieceNumber(); i++){
                switch (i){
                    case 0:
                        HamButton.Builder builder0 = new HamButton.Builder()
                                .shadowEffect(true)
                                .normalImageRes(R.drawable.ic_boom_button_map)
                                .normalText("Back to Map")
                                .listener(new OnBMClickListener() {
                                    @Override
                                    public void onBoomButtonClick(int index) {
                                        startActivity(new Intent(mContext, MainActivity.class));
                                    }
                                });
                        mBoomButton.addBuilder(builder0);
                        break;
                    case 1:
                        HamButton.Builder builder1 = new HamButton.Builder()
                                .shadowEffect(true)
                                .normalImageRes(R.drawable.ic_boom_button_map)
                                .normalText("Option 2")
                                .listener(new OnBMClickListener() {
                                    @Override
                                    public void onBoomButtonClick(int index) {

                                    }
                                });
                        mBoomButton.addBuilder(builder1);
                        break;
                    case 2:
                        HamButton.Builder builder2 = new HamButton.Builder()
                                .shadowEffect(true)
                                .normalImageRes(R.drawable.ic_boom_button_map)
                                .normalText("Option 3")
                                .listener(new OnBMClickListener() {
                                    @Override
                                    public void onBoomButtonClick(int index) {

                                    }
                                });
                        mBoomButton.addBuilder(builder2);
                        break;
                }
            }
            //Listen for Logout Button Clicked
            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (textVisible){
                        DatabaseReference refUtil = MainActivity.databaseReference.child("users")
                                .child(MainActivity.username).child("items");
                        refUtil.orderByChild("entryId");
                        refUtil.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChildren()){
                                    for (DataSnapshot dss: dataSnapshot.getChildren()){
                                        if (dss.child("latitude").getValue(Double.class)
                                                == entryToDisplay.getLatitude()
                                                && dss.child("longitude").getValue(Double.class)
                                                == entryToDisplay.getLongitude()){
                                            dss.getRef().removeValue();
                                            startActivity(new Intent(mContext, MainActivity.class));
                                        }
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {}
                        });
                    }
                    else {
                        com.transitionseverywhere.TransitionManager.beginDelayedTransition(mTransitionGroup);
                        textVisible = !textVisible;
                        mConfirmDelete.setVisibility(View.VISIBLE);
                    }
                }
            });
            mCancelDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (textVisible){
                        com.transitionseverywhere.TransitionManager.beginDelayedTransition(mTransitionGroup);
                        textVisible = !textVisible;
                        mConfirmDelete.setVisibility(View.GONE);
                    }
                }
            });
        }
    }
}
