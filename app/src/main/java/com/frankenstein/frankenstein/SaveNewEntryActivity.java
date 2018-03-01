package com.frankenstein.frankenstein;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomMenuButton;

import java.io.ByteArrayOutputStream;

public class SaveNewEntryActivity extends AppCompatActivity {
    private Bitmap mImage;
    private LatLng mLocation;
    private ImageView mImagePreview;
    private EditText mSummaryText;
    private EditText mPostText;
    private BoomMenuButton mBoomMenu;
    private Button mSaveEntry;
    private FirebaseUser mFirebaseUser;
    private final Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_new_entry);
        mImagePreview = findViewById(R.id.imageView_newEntry);
        mSummaryText = findViewById(R.id.EditText_summary_newEntry);
        mPostText = findViewById(R.id.editText_text_newEntry);
        mImage = EditNewEntryActivity.bitmap;
        mLocation = EditNewEntryActivity.location;
        mBoomMenu = findViewById(R.id.boombutton_saveNewEntry);
        mSaveEntry = findViewById(R.id.button_save_newEntry);
        mImagePreview.setImageBitmap(mImage);
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mSaveEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable saveToDatabase = new Runnable() {
                    @Override
                    public void run() {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        mImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] imageByteArray = baos.toByteArray();
                        String encodedImage = Base64.encodeToString(imageByteArray, Base64.DEFAULT);
                        String summaryText = mSummaryText.getText().toString();
                        if (summaryText.length() > 50){
                            summaryText = summaryText.substring(0, 46) + "...";
                        }
                        else if (summaryText.equals("")){
                            summaryText =(mPostText.getText().toString().substring(0, 46) + "...");
                        }
                        DatabaseReference refUtil = MainActivity.databaseReference.child("users")
                                .child(MainActivity.username).child("items").push();
                        refUtil.child("entryId").getRef().setValue(MainActivity.itemcount);
                        refUtil.child("userEmail").getRef().setValue(mFirebaseUser.getEmail());
                        refUtil.child("postTime").getRef().setValue(System.currentTimeMillis());
                        // TODO: Azimuth
                        refUtil.child("azimuth");
                        refUtil.child("latitude").getRef().setValue(mLocation.latitude);
                        refUtil.child("longitude").getRef().setValue(mLocation.longitude);
                        refUtil.child("postText").getRef().setValue(mPostText.getText().toString());
                        refUtil.child("picture").getRef().setValue(encodedImage);
                        refUtil.child("summary").getRef().setValue(summaryText);
                        Log.d("debug", "data saved to: " + MainActivity.username);
                    }
                };
                Thread saveEntry = new Thread(saveToDatabase);
                saveEntry.run();
                while (saveEntry.isAlive()){
                    mSaveEntry.setText("Saving");
                    mSaveEntry.setClickable(false);
                }
                finish();
                startActivity(new Intent(mContext, MainActivity.class));
            }
        });
        for (int i=0; i<mBoomMenu.getPiecePlaceEnum().pieceNumber(); i++){
            switch(i){
                case 0:
                    HamButton.Builder builder0 = new HamButton.Builder()
                            .shadowEffect(true)
                            .normalImageRes(R.drawable.ic_boom_button_map)
                            .normalText("Option 1")
                            .listener(new OnBMClickListener() {
                                @Override
                                public void onBoomButtonClick(int index) {
                                    Log.d("debug", "Option 1");
                                }
                            });
                    mBoomMenu.addBuilder(builder0);
                    break;
                case 1:
                    HamButton.Builder builder1 = new HamButton.Builder()
                            .shadowEffect(true)
                            .normalImageRes(R.drawable.ic_boom_button_current_location)
                            .normalText("Option 2")
                            .listener(new OnBMClickListener() {
                                @Override
                                public void onBoomButtonClick(int index) {
                                    Log.d("debug", "Option 2");
                                }
                            });
                    mBoomMenu.addBuilder(builder1);
                    break;
                case 2:
                    HamButton.Builder builder2 = new HamButton.Builder()
                            .shadowEffect(true)
                            .normalImageRes(R.drawable.ic_boom_button_current_location)
                            .normalText("Option 3")
                            .listener(new OnBMClickListener() {
                                @Override
                                public void onBoomButtonClick(int index) {
                                    Log.d("debug", "Option 3");
                                }
                            });
                    mBoomMenu.addBuilder(builder2);
                    break;
            }
        }
    }
}