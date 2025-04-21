package com.example.curtain.activities;


import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.curtain.MainActivity;
import com.example.curtain.R;
import com.example.curtain.constants.Constants;
import com.example.curtain.utilities.NetworkChangeListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    // UI views
    private EditText emailET, passwordET;
    private TextView forgotTV, noAccTV;
    private Button loginBtn;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;
    private FirebaseFirestore userData;

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // init UI views
        init();

        forgotTV.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        loginBtn.setOnClickListener(v -> loginUser());

        noAccTV.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    protected void init(){
        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);
        forgotTV = findViewById(R.id.forgotTV);
        noAccTV = findViewById(R.id.noAccTV);
        loginBtn = findViewById(R.id.loginBtn);

        auth = FirebaseAuth.getInstance();
        userData = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @SuppressWarnings("FieldCanBeLocal")
    private String email, password;
    private void loginUser() {
        email = emailET.getText().toString().trim();
        password = passwordET.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Logging In...");
        progressDialog.show();

        userData.collection("Users").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()){
                if (task.getResult().isEmpty()){
                    Toast.makeText(this, "Bunday foydalanuvchi yo'q", Toast.LENGTH_SHORT).show();
                } else {
//                    user found, proceed with login logic
            //  Assuming (taxmin qilish) only one document matches email
                    DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);

            //  Assuming (taxmin qilish) "user_type" field exists
                    String userType = documentSnapshot.getString("user_type");
                    String username = documentSnapshot.getString("username");
                    String userStatus = documentSnapshot.getString("user_status");

                    if (userStatus != null && userStatus.equals(Constants.userStatus[0])){
                        startActivity(new Intent(LoginActivity.this, FourZeroFourActivity.class));
                        finish();
                    } else {
                        SharedPreferences sharedPreferences = getSharedPreferences("USER_TYPE", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("user_type", userType);
                        editor.putString("username", username);
                        editor.putString("user_status", userStatus);
                        editor.apply();
                        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }).addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Parol xato", Toast.LENGTH_SHORT).show());

                    }
                }
            } else {
//                handle errors during data retrieval (olish)
                Toast.makeText(this, "Error at fetching data "
                        + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // for memory leak
    @Override
    protected void onDestroy() {
        // ProgressDialog'ni tozalash
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

    // BroadcastReceiver'ni tozalash
        try {
            unregisterReceiver(networkChangeListener);
        } catch (IllegalArgumentException e) {
            Log.e("LoginActivity", "Receiver not registered: " + e.getMessage());
        }

        super.onDestroy();
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