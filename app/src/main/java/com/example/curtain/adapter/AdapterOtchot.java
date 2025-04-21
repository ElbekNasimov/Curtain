package com.example.curtain.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.curtain.R;
import com.example.curtain.activities.OtchotDetailActivity;
import com.example.curtain.model.ModelOtchot;

import java.util.ArrayList;

public class AdapterOtchot extends RecyclerView.Adapter {

    private SharedPreferences sharedPreferences;
    private ArrayList<ModelOtchot> otchotList;
    private String sharedUserType;
    private Context context;
    private ProgressDialog progressDialog;
    public AdapterOtchot(Context context, ArrayList<ModelOtchot> otchotList, SharedPreferences sharedPreferences) {
        this.context = context;
        this.otchotList = otchotList;
        this.sharedPreferences = sharedPreferences;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_otchot_item, parent, false);
        return new AdapterOtchot.HolderOtchot(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        HolderOtchot holderOtchot = (HolderOtchot) holder;
        ModelOtchot modelOtchot = otchotList.get(position);

        holderOtchot.orderTitleTV.setText(modelOtchot.getTitle());

        holderOtchot.itemView.setOnClickListener(view -> {
            String otchotId = modelOtchot.getOtchotId();
            Toast.makeText(context, "otchotId " + otchotId, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, OtchotDetailActivity.class);
            intent.putExtra("otchotId", otchotId);
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return otchotList.size();
    }

    public class HolderOtchot extends RecyclerView.ViewHolder {

        private TextView orderTitleTV;

        public HolderOtchot(@NonNull View itemView) {
            super(itemView);

            orderTitleTV = itemView.findViewById(R.id.orderTitleTV);
        }
    }
}
