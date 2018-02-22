package com.frankenstein.frankenstein;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class UserProfileActivity extends AppCompatActivity {
    private Button mLogOutButton;
    private FirebaseAuth mFirebaseAuth;
    private final Context mContext = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mLogOutButton = findViewById(R.id.button_logout);

        mLogOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                        .setTitle("You are about to log out")
                        .setMessage("Are you sure you want to continue?")
                        .setNegativeButton("No", null)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mFirebaseAuth.signOut();
                                Intent login_intent = new Intent(mContext, LogInActivity.class);
                                login_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(login_intent);
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }
}
