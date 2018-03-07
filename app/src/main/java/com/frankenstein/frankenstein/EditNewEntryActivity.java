package com.frankenstein.frankenstein;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.nightonke.boommenu.Animation.BoomEnum;
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Gesture;
import com.otaliastudios.cameraview.GestureAction;
import com.soundcloud.android.crop.Crop;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class EditNewEntryActivity extends AppCompatActivity{
    private CameraView mCameraView;
    private Button mButtonTakePic;
    private final Context mContext = this;
    private BoomMenuButton mBoomMenu;
    public static Bitmap bitmap;
    public static LatLng location;
    public static Float azimuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_new_entry);
        Log.d("gb", "In editnew");
        location = getIntent().getParcelableExtra("location");
        azimuth = getIntent().getFloatExtra("azimuth", 0f);
        mCameraView = findViewById(R.id.CameraView_newEntry);
        mBoomMenu = findViewById(R.id.boombutton_newEntry);
        mBoomMenu.setBoomEnum(BoomEnum.RANDOM);
        mBoomMenu.setPiecePlaceEnum(PiecePlaceEnum.HAM_3);
        mBoomMenu.setButtonPlaceEnum(ButtonPlaceEnum.HAM_3);
        mBoomMenu.setButtonEnum(ButtonEnum.Ham);
        Log.d("gb", "Edit's azimuth = "+azimuth);
        for (int i=0; i<mBoomMenu.getPiecePlaceEnum().pieceNumber(); i++){
            switch(i){
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
        mButtonTakePic = findViewById(R.id.Button_takePic);
        mButtonTakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("debug", "clicked");
                mCameraView.captureSnapshot();
            }
        });
        mCameraView.mapGesture(Gesture.PINCH, GestureAction.ZOOM);
        mCameraView.mapGesture(Gesture.TAP, GestureAction.FOCUS_WITH_MARKER);
        mCameraView.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(byte[] jpeg) {
                super.onPictureTaken(jpeg);
                bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);

                if (bitmap == null) Toast.makeText(mContext
                        , "An error has occured, please try again later", Toast.LENGTH_SHORT).show();
                else if (location == null) Toast.makeText(mContext
                        , "Unable to determine location, Please try again later", Toast.LENGTH_SHORT).show();
                else startActivity(new Intent(mContext, SaveNewEntryActivity.class));

            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        mCameraView.start();
    }

    @Override
    public void onPause(){
        super.onPause();
        mCameraView.stop();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mCameraView.destroy();
    }
}
