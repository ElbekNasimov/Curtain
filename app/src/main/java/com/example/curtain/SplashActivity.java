package com.example.curtain;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.curtain.activities.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        auth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_splash);

        // start login activity after 2 second
        new Handler().postDelayed(() -> {
            FirebaseUser firebaseUser = auth.getCurrentUser();
            if (firebaseUser == null){
                // user not logged in start login activity
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            } else {
                // user is logged, check user type
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, 500);
    }
}
