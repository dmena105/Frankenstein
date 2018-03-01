package com.frankenstein.frankenstein;

import android.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.soundcloud.android.crop.Crop;

import java.io.File;

public class SignUpActivity extends AppCompatActivity {
    private EditText mUsernameText;
    private EditText mPasswordText;
    private Button mSignupButton;
    private TextView mLogInLink;
    private EditText mNicknameText;
    private ImageButton mProfilePicture;
    private FirebaseAuth mFirebaseAuth;
    private Uri uriPic;
    private Uri uriGallery;
    private Uri mImageSource = null;
    private final String IMAGE_SRC = "profile_picture.jpg";
    private final String IMAGE_SRC2 = "profile_picture_from_gallery.jpg";
    private final int CAPTURE_IMAGE = 0;
    private final int SELECT_IMAGE = 1;
    private final int CROP_IMAGE = 2;

    private final Context mContext = this;
    private final Activity mActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        FrankensteinPermission.checkPermission(mActivity);
        File file = new File(Environment.getExternalStorageDirectory(), IMAGE_SRC);
        uriPic = FileProvider.getUriForFile(mContext, "com.frankenstein.frankenstein.fileprovider", file);
        File file2 = new File(Environment.getExternalStorageDirectory(), IMAGE_SRC2);
        uriGallery = FileProvider.getUriForFile(mContext, "com.frankenstein.frankenstein.fileprovider", file2);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mUsernameText = findViewById(R.id.editText_signup_username);
        mPasswordText = findViewById(R.id.editText_signup_password);
        mSignupButton = findViewById(R.id.button_signup);
        mLogInLink = findViewById(R.id.textView_toLogIn);
        mProfilePicture = findViewById(R.id.imageButton_profilePicture);
        mNicknameText = findViewById(R.id.editText_signup_nickName);

        mProfilePicture.setOnClickListener(new View.OnClickListener() {
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
        mSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mUsernameText.getText().toString().trim();
                String password = mPasswordText.getText().toString().trim();
                final String nickname = mNicknameText.getText().toString().trim();
                if (email.isEmpty() || password.isEmpty() || nickname.isEmpty()){ // Show a dialog if an error occurs
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Sign up Error")
                            .setMessage("Please double check your username and password")
                            .setPositiveButton("OK", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else {
                    mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {  // After a successful signup, direct back to login activity
                                        Intent intent = new Intent(mContext, MainActivity.class);
                                        intent.putExtra("mode", 0);
                                        intent.putExtra("nickname", nickname);
                                        if (mImageSource != null) intent.putExtra("profile", mImageSource.toString());
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        Toast.makeText(mContext, "Success", Toast.LENGTH_SHORT).show();
                                    }
                                    else {      // Show a dialog when an error occurs
                                        AlertDialog.Builder builder= new AlertDialog.Builder(mContext);
                                        builder.setTitle("Error!")
                                                .setMessage(task.getException().getMessage())
                                                .setPositiveButton("OK", null);
                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    }
                                }
                            });

                }
            }
        });
        mLogInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, LogInActivity.class));
            }
        });
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
                    mProfilePicture.setImageURI(null);
                    mProfilePicture.setImageURI(image_crop);
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
}
