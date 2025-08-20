package com.example.curtain.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.curtain.R;
import com.example.curtain.model.ModelReservation;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AdapterReservation extends RecyclerView.Adapter<AdapterReservation.HolderReservation>{
    private Context context;
    private ArrayList<ModelReservation> reservationList;
    private OnReservationChangeListener listener;
    private String partId;

    public interface OnReservationChangeListener {
        void onReservationChanged();
    }

    public AdapterReservation(Context context, ArrayList<ModelReservation> reservationList,
                              String partId, OnReservationChangeListener listener) {
        this.context = context;
        this.reservationList = reservationList;
        this.partId = partId;
        this.listener = listener;
    }



    @Override
    public HolderReservation onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.row_reservation, null);
        return new HolderReservation(view);
    }

    @Override
    public void onBindViewHolder(HolderReservation holder, int position) {
        // Bind data to the ViewHolder
        ModelReservation reservation = reservationList.get(position);

        holder.designerNameTV.setText(reservation.getReservedBy() + " bron qildi");
        holder.reservedLengthTV.setText(reservation.getReservedLength() + " m");
        holder.reservedForTV.setText(reservation.getReservedFor());
        holder.reservedDateTV.setText(reservation.getReservedDate());

        // Permission check
        SharedPreferences prefs = context.getSharedPreferences("USER_TYPE", Context.MODE_PRIVATE);
        String currentUser = prefs.getString("username", "");
        String userType = prefs.getString("userType", "");

        boolean canCancel = currentUser.equals(reservation.getReservedBy()) || "superAdmin".equals(userType);

        if (canCancel && "active".equals(reservation.getStatus())) {
//            holder.cancelReservationBtn.setVisibility(View.VISIBLE);
            holder.cancelReservationBtn.setOnClickListener(v -> {
                cancelReservation(reservation, position);
            });
        } else {
            holder.cancelReservationBtn.setVisibility(View.GONE);
        }
    }

    private void cancelReservation(ModelReservation reservation, int position) {
        Log.d("AdapterReservation", "Canceling reservation for part: " + partId);

        // Logic to cancel the reservation
        new AlertDialog.Builder(context)
                .setTitle("Bron bekor qilish")
                .setMessage("Bronni bekor qilmoqchimisiz?")
                .setPositiveButton("Ha", (dialog, which) -> {
                    // Update status in Firebase first
                    FirebaseFirestore.getInstance()
                            .collection("Parts")
                            .document(partId)
                            .collection("Reservations")
                            .document(reservation.getReservationId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                // Successfully updated the status in Firebase
                                // Now update local data and UI
                                reservationList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, reservationList.size());

                                // Notify parent adapter about the change
                                if (listener != null) {
                                    listener.onReservationChanged();
                                }

                                Toast.makeText(context, "Bron bekor qilindi", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.w("AdapterReservation", "Bron bekor qilishda xatolik", e);
                                Toast.makeText(context, "Bron bekor qilishda xatolik: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Yo'q", null)
                .show();
    }

    @Override
    public int getItemCount() {
        // Return the total number of items
        return reservationList.size(); // Replace with actual implementation
    }

    public static class HolderReservation extends RecyclerView.ViewHolder {
        // Define views in the item layout
        private TextView designerNameTV, reservedLengthTV, reservedForTV, reservedDateTV, cancelReservationBtn;

        public HolderReservation(View itemView) {
            super(itemView);
            // Initialize views
            designerNameTV = itemView.findViewById(R.id.designerNameTV);
            reservedLengthTV = itemView.findViewById(R.id.reservedLengthTV);
            reservedForTV = itemView.findViewById(R.id.reservedForTV);
            reservedDateTV = itemView.findViewById(R.id.reservedDateTV);
            cancelReservationBtn = itemView.findViewById(R.id.cancelReservationBtn);
        }
    }
}
