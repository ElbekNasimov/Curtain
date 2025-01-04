package com.example.curtain.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.curtain.R;
import com.example.curtain.adapter.AdapterUser;
import com.example.curtain.model.ModelUser;
import com.example.curtain.utilities.NetworkChangeListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class UsersListActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private ProgressDialog progressDialog;
    private RecyclerView usersListRV;
    private ArrayList<ModelUser> userArrayList;
    private AdapterUser adapterUser;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        init();

        backBtn.setOnClickListener(view -> onBackPressed());

        loadUsers();

    }

    private void init(){

        userArrayList = new ArrayList<>();
        backBtn = findViewById(R.id.backBtn);
        usersListRV = findViewById(R.id.usersListRV);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.wait);
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void loadUsers(){
        progressDialog.show();

        CollectionReference collectionReference  = firebaseFirestore.collection("Users");
        collectionReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                progressDialog.dismiss();
                userArrayList.clear();
                for (DocumentSnapshot snapshot : task.getResult()){
                    ModelUser modelUser = snapshot.toObject(ModelUser.class);
                    userArrayList.add(modelUser);
                }
                if (userArrayList.isEmpty()){
                    usersListRV.setVisibility(View.GONE);
                }
                adapterUser = new AdapterUser(UsersListActivity.this, userArrayList);
                usersListRV.setAdapter(adapterUser);
            } else {
                progressDialog.dismiss();
                Toast.makeText(UsersListActivity.this, "Users yuklashda xato " +
                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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