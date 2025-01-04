package com.example.curtain.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.widget.Button;

import com.example.curtain.R;
import com.example.curtain.utilities.NetworkChangeListener;

public class FourZeroFourActivity extends AppCompatActivity {

    private Button backLoginBtn;

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_four_zero_four);

        init();

//        404 page

        backLoginBtn.setOnClickListener(view -> {
            startActivity(new Intent(FourZeroFourActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void init(){
        backLoginBtn = findViewById(R.id.backLoginBtn);
    }

    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }
}