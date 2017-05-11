package com.umbc.android.pictag;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseUser user;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        user = FirebaseAuth.getInstance().getCurrentUser();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    finish();
                    Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(myIntent);
                } else{
                    finish();
                    Intent myIntent = new Intent(MainActivity.this, HomeActivity.class);
                    String email = user.getEmail();
                    myIntent.putExtra("email", email);
                    startActivity(myIntent);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (user == null) {
            finish();
            Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(myIntent);
        } else{
            finish();
            Intent myIntent = new Intent(MainActivity.this, HomeActivity.class);
            String email = user.getEmail();
            myIntent.putExtra("email", email);
            startActivity(myIntent);
        }
    }
}
