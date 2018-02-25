package com.frankenstein.frankenstein;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class UserProfileActivity extends AppCompatActivity {
    private ViewGroup mTransitionGroup;
    private Button mLogOutButton;
    private FirebaseAuth mFirebaseAuth;
    private TextView mConfirmLogout;
    private View mCancelLogout;
    private boolean textVisible;
    private final Context mContext = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        mTransitionGroup = findViewById(R.id.transitionContainer_logout);
        mConfirmLogout = findViewById(R.id.textView_confirm_logout);
        textVisible = false;
        mConfirmLogout.setVisibility(View.GONE);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCancelLogout = findViewById(R.id.profile_activity_clickListenerView);
        mLogOutButton = findViewById(R.id.button_logout);

        mLogOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                if (textVisible){
                    com.transitionseverywhere.TransitionManager.beginDelayedTransition(mTransitionGroup);
                    textVisible = !textVisible;
                    mConfirmLogout.setVisibility(View.GONE);
                }
            }
        });
    }
}