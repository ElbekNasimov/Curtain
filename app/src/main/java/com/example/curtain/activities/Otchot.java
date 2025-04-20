package com.example.curtain.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.curtain.R;

public class Otchot extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    Button backBtn;
    private String sharedUserType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_otchot);

        init();


        // if sharedUserType equals "superAdmin" then create new document to Firestore with date and time using workManager
        if (sharedUserType.equals("superAdmin")) {
            // create new document to Firestore with date and time using workManager


        }




        backBtn.setOnClickListener(v -> {
            onBackPressed();
        });

    }

    private void init(){

        sharedUserType = getUserType(sharedPreferences);


        backBtn = findViewById(R.id.backBtn);
    }

    private String getUserType(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString("user_type", "");
    }


}