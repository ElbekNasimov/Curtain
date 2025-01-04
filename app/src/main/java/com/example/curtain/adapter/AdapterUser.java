package com.example.curtain.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.curtain.R;
import com.example.curtain.activities.FourZeroFourActivity;
import com.example.curtain.model.ModelUser;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.HolderUser> {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private Context context;
    public ArrayList<ModelUser> userArrayList;
    private ProgressDialog progressDialog;
    public AdapterUser(Context context, ArrayList<ModelUser> userArrayList) {
        this.context = context;
        this.userArrayList = userArrayList;
    }

    @NonNull
    @Override
    public AdapterUser.HolderUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_users_item, parent, false);
        return new AdapterUser.HolderUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterUser.HolderUser holder, int position) {
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        final ModelUser modelUser = userArrayList.get(position);

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(context.getResources().getString(R.string.wait));
        progressDialog.setCanceledOnTouchOutside(false);

        String username = modelUser.getUsername();
        String userType = modelUser.getUser_type();
        String user_status = modelUser.getUser_status();
        String userUID = modelUser.getUid();

        holder.userTV.setText(username);
        holder.userTypeTV.setText(userType);
        if (user_status.equals("ENABLE")){
            holder.userStatusChB.setChecked(true);
            holder.userStatusChB.setText("Disable");
        }

        holder.userStatusChB.setOnCheckedChangeListener((compoundButton, b) -> {

            if (user_status.equals("ENABLE")){
                progressDialog.show();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("user_status", "DISABLE");
                DocumentReference docRef = firebaseFirestore.collection("Users").document(username);
                        docRef.update(hashMap).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Updated...", Toast.LENGTH_SHORT).show();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Updated Users False... " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (user_status.equals("DISABLE")){
                progressDialog.show();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("user_status", "ENABLE");
                DocumentReference docRef = firebaseFirestore.collection("Users").document(username);
                docRef.update(hashMap).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Updated...", Toast.LENGTH_SHORT).show();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Updated Users False... " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        holder.delUserBtn.setVisibility(View.GONE);

        holder.delUserBtn.setOnClickListener(view -> {
            DocumentReference reference = firebaseFirestore.collection("Users").document(username);
            reference.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()){
                        String email = doc.getString("email");
                        String password = doc.getString("password");
                        reAuthForDelUser(email, password);
                    } else {
                        Toast.makeText(context, "bunday user topilmadi", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "error at User loading", Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    private void reAuthForDelUser(String email, String password) {

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user!=null) {
            AuthCredential authCredential = EmailAuthProvider.getCredential(email, password);

        } else {
            Toast.makeText(context, "Qaytamiz...", Toast.LENGTH_SHORT).show();
            context.startActivity(new Intent(context, FourZeroFourActivity.class));
        }

    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    public class HolderUser extends RecyclerView.ViewHolder {
        private CheckBox userStatusChB;
        private TextView userTV, userTypeTV;
        private ImageButton delUserBtn;
        public HolderUser(@NonNull View itemView) {
            super(itemView);
            userStatusChB = itemView.findViewById(R.id.userStatusChB);
            userTV = itemView.findViewById(R.id.userTV);
            userTypeTV = itemView.findViewById(R.id.userTypeTV);
            delUserBtn = itemView.findViewById(R.id.delUserBtn);
        }
    }
}
