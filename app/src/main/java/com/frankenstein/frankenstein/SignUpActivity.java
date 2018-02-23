package com.frankenstein.frankenstein;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {
    private EditText mUsernameText;
    private EditText mPasswordText;
    private Button mSignupButton;
    private TextView mLogInLink;
    private FirebaseAuth mFirebaseAuth;
    private final Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mUsernameText = findViewById(R.id.editText_signup_username);
        mPasswordText = findViewById(R.id.editText_signup_password);
        mSignupButton = findViewById(R.id.button_signup);
        mLogInLink = findViewById(R.id.textView_toLogIn);
        mSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mUsernameText.getText().toString().trim();
                String password = mPasswordText.getText().toString().trim();
                if (email.isEmpty() || password.isEmpty()){ // Show a dialog if an error occurs
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
                                        Intent intent = new Intent(mContext, LogInActivity.class);
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
}
