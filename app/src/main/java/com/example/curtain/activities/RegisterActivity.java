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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.curtain.R;
import com.example.curtain.constants.Constants;
import com.example.curtain.utilities.NetworkChangeListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

//    for choosing user type
    private AutoCompleteTextView autoCompleteTextView;
    private ImageButton backBtn;
    private TextInputEditText usernameET, emailET, phoneET, passwordET, confPassET;
    private TextInputLayout labelCP;
    private Button regAdminBtn;
    private ProgressDialog progressDialog;
    private static final String TAG = "RegisterActivity";
    private FirebaseFirestore usersDatabase;
    private FirebaseAuth auth;

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        init();

        backBtn.setOnClickListener(v -> onBackPressed());

        get_user_type_select();

        regAdminBtn.setOnClickListener(v -> input_data());

    }

    private void init(){
        autoCompleteTextView = findViewById(R.id.userTypeACV);
        backBtn = findViewById(R.id.backBtn);
        usernameET = findViewById(R.id.usernameET);
        emailET = findViewById(R.id.emailET);
        phoneET = findViewById(R.id.phoneET);
        passwordET = findViewById(R.id.passwordET);
        confPassET = findViewById(R.id.confPassET);
        regAdminBtn = findViewById(R.id.regAdminBtn);
        labelCP = findViewById(R.id.labelCP);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(true);

        auth = FirebaseAuth.getInstance();
        usersDatabase = FirebaseFirestore.getInstance();
    }

    private String username, email, user_type, phone, password, confPass;
    private void input_data() {

//        Check and Get inputs Begin
        if (usernameET != null && !TextUtils.isEmpty(usernameET.getText())){
            username = usernameET.getText().toString().trim().toLowerCase();
        } else {
            Toast.makeText(this, "Username kiriting... ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (emailET != null && !TextUtils.isEmpty(emailET.getText())){
            if (Patterns.EMAIL_ADDRESS.matcher(emailET.getText()).matches()){
                email = emailET.getText().toString().trim();
            } else {
                Toast.makeText(this, "Xato Email... ", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Toast.makeText(this, "Email kiriting... ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(autoCompleteTextView.getText())){
            Toast.makeText(this, "Tanlang ", Toast.LENGTH_SHORT).show();
            return;
        } else {
            user_type = autoCompleteTextView.getText().toString().trim();
        }

        phone = Objects.requireNonNull(phoneET.getText()).toString().trim();

        if (TextUtils.isEmpty(passwordET.getText())){
            Toast.makeText(this, "Password 6 tadan kam yoki bo'sh...", Toast.LENGTH_SHORT).show();
            return;
        } else {
            password = passwordET.getText().toString().trim();
        }

        if (!password.equals(Objects.requireNonNull(confPassET.getText()).toString())){
            labelCP.setError("Passwordlar mos kelmadi");
            return;
        } else {
            confPass = confPassET.getText().toString().trim();
        }

//        Check and Get inputs End
        createAccount();

    }

    private void createAccount() {
        progressDialog.setMessage("Account yaratilmoqda");
        progressDialog.show();

        usersDatabase.collection("Users").document(username.toLowerCase()).get()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()){
                            Toast.makeText(RegisterActivity.this, "Bunaqa foydalanuvchi bor", Toast.LENGTH_SHORT).show();
                        } else {
                            auth.createUserWithEmailAndPassword(email, password)
                                    .addOnSuccessListener(authResult -> saveFirebaseData())
                                    .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this,
                                            "Account yaratilmadi" + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "err checking username " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveFirebaseData() {

        progressDialog.setMessage("Saqlanmoqda");
        progressDialog.show();

        String timestamp = "" + System.currentTimeMillis();
        Date prDate = new Date(Long.parseLong(timestamp));
        SimpleDateFormat sdfFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String created_at = sdfFormat.format(prDate);

        String uid = UUID.randomUUID().toString();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", ""+uid);
        hashMap.put("username", ""+username);
        hashMap.put("email", ""+email);
        hashMap.put("password", ""+password);
        if (!phone.isEmpty()) {
            hashMap.put("phone", "" + phone);
        }
        hashMap.put("created_at", "" + created_at);
        hashMap.put("user_type", "" + user_type);
        hashMap.put("user_status", "" + Constants.userStatus[0]);

        usersDatabase.collection("Users").document(username).set(hashMap).
                addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()){
                        setUpUser(username);
                        SharedPreferences sharedPreferences = getSharedPreferences("USER_TYPE", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("user_type", user_type);
                        editor.putString("username", username);
                        editor.putString("user_status", "DISABLE");
                        editor.apply();

                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();

                        Toast.makeText(RegisterActivity.this, "added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegisterActivity.this, "error to add user " +
                                task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setUpUser(String username) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest profileChangeRequest = new  UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();
        user.updateProfile(profileChangeRequest).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "user profile updated");
            }
        });
    }

    //    for choosing user type
    private void get_user_type_select() {
        String[] userTypes = {"sklad","dizayner", "bichuvchi","admin","superAdmin"};
        ArrayAdapter<String> arrayAdapter;

        arrayAdapter = new ArrayAdapter<>(this, R.layout.select_item, userTypes);
        autoCompleteTextView.setAdapter(arrayAdapter);
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