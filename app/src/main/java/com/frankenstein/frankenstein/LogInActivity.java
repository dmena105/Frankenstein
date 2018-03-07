package com.frankenstein.frankenstein;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LogInActivity extends AppCompatActivity {

    private static final String TAG = "TESTING123";

    private EditText mUsernameText;
    private EditText mPasswordText;
    private Button mLogInButton;
    private TextView mSignUpLink;
    private FirebaseAuth mFirebaseAuth;
    private final Context mContext = this;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        //Log In Items
        mFirebaseAuth = FirebaseAuth.getInstance();
        mUsernameText = findViewById(R.id.editText_signin_username);
        mPasswordText = findViewById(R.id.editText_signin_password);
        mLogInButton = findViewById(R.id.button_signin);
        mSignUpLink = findViewById(R.id.textView_toSignUp);
        mLogInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Getting email and password from text fields
                final String email = mUsernameText.getText().toString().trim();
                String password = mPasswordText.getText().toString().trim();
                if (email.isEmpty() || password.isEmpty()){
                    // dialogs for invalid entries
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                            .setTitle("Invalid entry")
                            .setMessage("Please enter your username or password")
                            .setPositiveButton("OK", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else {
                    mFirebaseAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LogInActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        // Start main Activity
                                        Intent intent = new Intent(mContext, MainActivity.class);
                                        intent.putExtra("mode", 1);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                    else {
                                        // Show error dialog
                                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                        builder.setTitle("Sign in Error")
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
        mSignUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(mContext, SignUpActivity.class);
                signUpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(signUpIntent);
            }
        });
    }
}
