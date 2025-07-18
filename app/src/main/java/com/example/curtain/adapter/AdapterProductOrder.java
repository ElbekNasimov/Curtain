package com.example.curtain.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.curtain.R;
import com.example.curtain.constants.Constants;
import com.example.curtain.model.ModelPart;
import com.example.curtain.model.ModelProductOrder;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AdapterProductOrder extends RecyclerView.Adapter<AdapterProductOrder.HolderProductOrder>{
    private FirebaseFirestore firestore;
    private Context context;
    private ArrayList<ModelProductOrder> productOrderArrayList;
    private ArrayList<ModelPart> partsList;
    private SharedPreferences sharedPreferences;
    private String sharedUserType;
    private ProgressDialog progressDialog;
    private AdapterCutPartPrOrder adapterCutPartPrOrder;
    private BottomSheetDialog bottomSheetDialog;
    public AdapterProductOrder(Context context, ArrayList<ModelProductOrder> productOrderArrayList,
                               SharedPreferences sharedPreferences) {
        this.context=context;
        this.productOrderArrayList=productOrderArrayList;
        this.sharedPreferences = sharedPreferences;
    }

    @NonNull
    @Override
    public AdapterProductOrder.HolderProductOrder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_orders_item, parent, false);
        return new AdapterProductOrder.HolderProductOrder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterProductOrder.HolderProductOrder holder, int position) {
        progressDialog = new ProgressDialog(holder.itemView.getContext());
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        sharedUserType = sharedPreferences.getString("user_type", "");

        final ModelProductOrder modelProductOrder = productOrderArrayList.get(position);
        String productTitle = modelProductOrder.getProductObjectOrder();
        String productLength = modelProductOrder.getLenProductObjectOrder();
        String productObjectOrderId = modelProductOrder.getProductObjectOrderId();
        String productId = modelProductOrder.getProductId();
        String orderId = modelProductOrder.getOrderId();
        String partStatusProductOrder = modelProductOrder.getPartStatusProductOrder();
        String productPriceProductOrder = modelProductOrder.getProductPriceProductOrder();

        firestore = FirebaseFirestore.getInstance();
        DocumentReference reference = firestore.collection("ProductsOrder").document(productObjectOrderId);

        if (partStatusProductOrder!=null){
            holder.productOrderStatusTV.setText(partStatusProductOrder);
        }

        DocumentReference orderRef = firestore.collection("Orders").document(orderId);
        orderRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    String orderStatus = doc.getString("orderStatus");
                    if (!orderStatus.equals("Yangi")){
                        if (!sharedUserType.equals(Constants.userTypes[4])) {
                            holder.delProductOrderBtn.setVisibility(View.GONE);
                            holder.editProductOrderBtn.setVisibility(View.GONE);
                        }
                    }
                } else {
                    Toast.makeText(context, "Smeta topilmadimi", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Error fetching Smeta: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        holder.titleProductOrderTV.setText(productTitle);
        holder.lenProductOrderTV.setText(String.format("%s m", productLength));
        float price = Float.parseFloat(productPriceProductOrder);
        float len = Float.parseFloat(productLength);
        float sum = price * len;
        holder.sumProductOrderTV.setText(String.format("%s $", sum));

        if (holder.productOrderStatusTV.getText().toString().equals("kesildi")){
            if (!sharedUserType.equals(Constants.userTypes[4])) {
                holder.delProductOrderBtn.setVisibility(View.GONE);
                holder.editProductOrderBtn.setVisibility(View.GONE);
            }
        }

        if (holder.productOrderStatusTV.getText().toString().equals("holat") ||
        holder.productOrderStatusTV.getText().toString().equals("kesilmoqda")
        ) {
            if (!sharedUserType.equals(Constants.userTypes[0]) && !sharedUserType.equals(Constants.userTypes[4])) {
                holder.productOrderStatusTV.setVisibility(View.GONE);
            }
            if (!sharedUserType.equals(Constants.userTypes[4]) && !sharedUserType.equals(Constants.userTypes[1])) {
                holder.delProductOrderBtn.setVisibility(View.GONE);
                holder.editProductOrderBtn.setVisibility(View.GONE);
            }
        }

        if (holder.productOrderStatusTV.getText().toString().equals("kesildi")){
            // change text color to cutColor from colors.xml
            holder.productOrderStatusTV.setTextColor(Color.parseColor("#008000"));
        } else if (holder.productOrderStatusTV.getText().toString().equals("bichildi")){
            // change text color to cuttingColor from colors.xml
            holder.productOrderStatusTV.setTextColor(Color.parseColor("#800000"));
        }

        holder.qoldiqKusokTV.setVisibility(View.GONE);
        holder.cutPartsPrOrderTV.setVisibility(View.GONE);

        reference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    String qoldiqKusok;
                    if (doc.contains("qoldiqKusok")){
                        qoldiqKusok = doc.getString("qoldiqKusok");
                        if (qoldiqKusok != null && !qoldiqKusok.trim().isEmpty()) {
                            holder.qoldiqKusokTV.setVisibility(View.VISIBLE);
                            holder.qoldiqKusokTV.setText(qoldiqKusok);
                        }
                    }
                    if (doc.contains("kesilganKusoklarList")) {
                        String kesilganlarList = doc.getString("kesilganKusoklarList");
                        holder.cutPartsPrOrderTV.setVisibility(View.VISIBLE);
                        holder.cutPartsPrOrderTV.setText(kesilganlarList);
                    }
                } else {
                    Toast.makeText(context, "bunaqa pr yo'q", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "pr topishda xato " + task.getException(), Toast.LENGTH_SHORT).show();
            }
        });

        holder.delProductOrderBtn.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Delete").setMessage("O'chirmoqchimisiz?")
                    .setPositiveButton("Delete", (dialog, which) ->
                            reference.delete().addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            Toast.makeText(context, "Kusok o'chirildi", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "kusok o'chirishda xato "
                                    + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> Toast.makeText(context, "Error at Deleted Part..."
                            + e.getMessage(), Toast.LENGTH_SHORT).show())).setNegativeButton("No",
                            (dialog, which) -> dialog.dismiss()).show();
        });

        holder.editProductOrderBtn.setOnClickListener(view -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context.getApplicationContext());
            View dialogView = inflater.inflate(R.layout.dialog_edit_part, null);
            alertDialog.setTitle("Change");

            EditText editPartET = dialogView.findViewById(R.id.editPartET);

            alertDialog.setView(dialogView)
                    .setPositiveButton(R.string.save_me, (dialogInterface, i) -> {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("lenProductObjectOrder", ""+editPartET.getText().toString().trim());
                        if (!TextUtils.isEmpty(editPartET.getText())) {
                            reference.update(hashMap).addOnSuccessListener(unused ->
                                            Toast.makeText(context, "Updated...", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(context, "part not updated "
                                            + e.getMessage(), Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(context, "Miqdor kiritilmagan yoki xato ", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());

            alertDialog.setView(dialogView);
            AlertDialog dialog = alertDialog.create();
            dialog.show();
        });

        holder.productOrderStatusTV.setOnClickListener(view ->{
            if (sharedUserType.equals(Constants.userTypes[4]) || sharedUserType.equals(Constants.userTypes[0]))
            {
                if (holder.productOrderStatusTV.getText().toString().equals("holat") ||
                        holder.productOrderStatusTV.getText().toString().equals("kesilmoqda")
                ) {
                    cuttingBottomSheetDialog(productTitle,
                            productId, productLength, productObjectOrderId, orderId, holder.qoldiqKusokTV,
                            holder.cutPartsPrOrderTV, position);
                }
            } else if (holder.productOrderStatusTV.getText().toString().equals("kesildi")){
                if (sharedUserType.equals(Constants.userTypes[4]) || sharedUserType.equals(Constants.userTypes[2])){

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle("Info").setMessage("Bichildimi?").setPositiveButton("Ha", (dialogInterface, i) -> {

                        String status = "bichildi";
                        String statusOrder = "bichilmoqda";
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("partStatusProductOrder", ""+status);

                        DocumentReference statusRef = firestore.collection("ProductsOrder").document(productObjectOrderId);
                        statusRef.update(hashMap).addOnSuccessListener(unused -> {

                            Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
                            productOrderArrayList.get(position).setPartStatusProductOrder(status);
                            changeStatusPartPrOrder(orderId, statusOrder);
                            holder.productOrderStatusTV.setText(status);
                            notifyItemChanged(position);

                        }).addOnFailureListener(e -> Toast.makeText(context, "part not updated "
                                        + e.getMessage(), Toast.LENGTH_SHORT).show());
                            }).setNegativeButton("Yo'q", (dialogInterface, i) -> dialogInterface.dismiss()).show();
                } else {
                    holder.productOrderStatusTV.setClickable(false);
                }
            }
        });
    }
    // shungachasi ko'chirildi AdapterProductObjectga, pasi qoldi
    private void cuttingBottomSheetDialog(String productTitle, String productId, String productLength,
                                          String productObjectOrderId, String orderId, TextView kelganQoldiqKusokTV,
                                          TextView cutPartsPrOrderTV, int position) {

        ArrayList<String> kesilganKusoklarList = new ArrayList<>();

        String str = cutPartsPrOrderTV.getText().toString().trim();
        if (!str.equals("kesilgan kusoklar:")){
            String numberStr = str.substring(1, str.length()-1);
            kesilganKusoklarList = new ArrayList<>(Arrays.asList(numberStr.split(",")));
        }

        String kelganQoldiqKusok = kelganQoldiqKusokTV.getText().toString().trim();

        bottomSheetDialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.bs_parts_pr_to_order, null);
        bottomSheetDialog.setContentView(view);

        progressDialog.show();

        TextView cuttingPrNameTV = view.findViewById(R.id.cuttingPrNameTV);
        TextView chosenPartIdPrOrderTV = view.findViewById(R.id.chosenPartIdPrOrderTV);
        TextInputEditText partCutPrObjLenET = view.findViewById(R.id.partCutPrObjLenET);
        TextInputEditText chosenPartPrOrderET = view.findViewById(R.id.chosenPartPrOrderET);
        RecyclerView partCutPrObjRV = view.findViewById(R.id.partCutPrObjRV);
        Button cutPartsPrObjBtn = view.findViewById(R.id.cutPartsPrObjBtn);

        partsList = new ArrayList<>();

        cuttingPrNameTV.setText(productTitle);

        if (!kelganQoldiqKusok.isEmpty()){
            partCutPrObjLenET.setText(kelganQoldiqKusok);
        } else {
            partCutPrObjLenET.setText(productLength);
        }

        CollectionReference partsRef = firestore.collection("Parts");
        partsRef.whereEqualTo("prId", productId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                progressDialog.dismiss();
                partsList.clear();

                for (DocumentSnapshot snapshot : task.getResult()){
                    ModelPart modelPart = snapshot.toObject(ModelPart.class);
                    partsList.add(modelPart);
                }

                adapterCutPartPrOrder = new AdapterCutPartPrOrder(context, partsList,
                        chosenPartPrOrderET, chosenPartIdPrOrderTV);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                partCutPrObjRV.setLayoutManager(linearLayoutManager);
                partCutPrObjRV.setAdapter(adapterCutPartPrOrder);
            } else {
                progressDialog.dismiss();
                Toast.makeText(context, "qismlar yuklanmadi...", Toast.LENGTH_SHORT).show();
            }
        });

        bottomSheetDialog.show();

        List<String> finalMyList = kesilganKusoklarList;
        cutPartsPrObjBtn.setOnClickListener(view1 -> {
            String partCutPrObjLen, chosenPartPrOrder;

            if (TextUtils.isEmpty(partCutPrObjLenET.getText())){
                Toast.makeText(context, "Uzunligini kiriting...", Toast.LENGTH_SHORT).show();
                return;
            } else {
                partCutPrObjLen = partCutPrObjLenET.getText().toString().trim();
            }

            if (TextUtils.isEmpty(chosenPartPrOrderET.getText())){
                Toast.makeText(context, "Kusokni tanlang...", Toast.LENGTH_SHORT).show();
                return;
            } else {
                chosenPartPrOrder = chosenPartPrOrderET.getText().toString().trim();
            }

            String chosenPartIdPrOrder = chosenPartIdPrOrderTV.getText().toString().trim();
            
            float zakasBerilganKusokUzunligiOrder = Float.parseFloat(productLength);
            float kesiladiganKusokUzunligi = Float.parseFloat(partCutPrObjLen);

            if (zakasBerilganKusokUzunligiOrder < Float.parseFloat(partCutPrObjLen)){
                Toast.makeText(context, "Zakasdan uzun miqdor kiritilgan...", Toast.LENGTH_SHORT).show();
                return;
            }
            if (Float.parseFloat(chosenPartPrOrder) < Float.parseFloat(partCutPrObjLen)) {
                Toast.makeText(context, "Buyurtma kusokdan katta...", Toast.LENGTH_SHORT).show();
                return;
            }

            if ((Float.parseFloat(chosenPartPrOrder) > Float.parseFloat(partCutPrObjLen))){
                if ((Float.parseFloat(partCutPrObjLen) < zakasBerilganKusokUzunligiOrder)) {

                    float keyingiQoldiq;
                    if (!kelganQoldiqKusok.isEmpty()) {
                        keyingiQoldiq = Float.parseFloat(kelganQoldiqKusok) - Float.parseFloat(partCutPrObjLen);
                    } else {
                        keyingiQoldiq = Float.parseFloat(productLength)-Float.parseFloat(partCutPrObjLen);
                    }
                    finalMyList.add(partCutPrObjLen);

                    String kusokHolati = "kesilmoqda";
                    String timestamps = "" + System.currentTimeMillis();
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("cutIdPartProductOrder", ""+timestamps);      // vaqti va kesilgan smeta kusogi id
                    hashMap.put("chosenPartIdPrOrder", ""+chosenPartIdPrOrder);   // tanlangan smeta kusok id
                    hashMap.put("chosenPartPrOrder", ""+chosenPartPrOrder);       // tanlangan smeta kusok uzunligi
                    hashMap.put("partCutPrObjLen", ""+partCutPrObjLen);           // kesilgan smeta kusok uzunligi
                    hashMap.put("orderId", ""+orderId);                           // Smeta id
                    hashMap.put("productObjectOrderId", ""+productObjectOrderId); // productOrderId
                    hashMap.put("productId", ""+productId);                    // product id
                    hashMap.put("tanlanganKusokUzunligiOrder", ""+productLength);

                    firestore.collection("CutPartProduct").document(timestamps).set(hashMap).addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        bottomSheetDialog.dismiss();
                        if (task.isSuccessful()) {

                            changeLenPrOrder(chosenPartIdPrOrder, chosenPartPrOrder, partCutPrObjLen);
                            changeStatusPartPrOrder(orderId, "kesilmoqda");
                            changeStatusProductsOrder(productObjectOrderId, kusokHolati, keyingiQoldiq, finalMyList);

                            productOrderArrayList.get(position).setPartStatusProductOrder("kesildi");
                            notifyItemChanged(position);

                        } else {
                            Toast.makeText(context, "error to kesish"
                                    + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            if ((Float.parseFloat(chosenPartPrOrder) == Float.parseFloat(partCutPrObjLen)) &&
                    (Float.parseFloat(productLength) > Float.parseFloat(partCutPrObjLen))) {
                float keyingiQoldiq;
                if (!kelganQoldiqKusok.isEmpty()) {
                    keyingiQoldiq = Float.parseFloat(kelganQoldiqKusok) - Float.parseFloat(partCutPrObjLen);
                } else {
                     keyingiQoldiq = Float.parseFloat(productLength)-Float.parseFloat(partCutPrObjLen);
                }
                finalMyList.add(partCutPrObjLen);

                String kusokHolati = "kesilmoqda";
                String timestamps = "" + System.currentTimeMillis();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("cutIdPartProductOrder", ""+timestamps);      // vaqti va kesilgan smeta kusogi id
                hashMap.put("chosenPartIdPrOrder", ""+chosenPartIdPrOrder);   // tanlangan smeta kusok id
                hashMap.put("chosenPartPrOrder", ""+chosenPartPrOrder);       // tanlangan smeta kusok uzunligi
                hashMap.put("partCutPrObjLen", ""+partCutPrObjLen);           // kesilgan smeta kusok uzunligi
                hashMap.put("orderId", ""+orderId);                           // Smeta id
                hashMap.put("productObjectOrderId", ""+productObjectOrderId); // productOrderId
                hashMap.put("productId", ""+productId);                    // product id
                hashMap.put("tanlanganKusokUzunligiOrder", ""+productLength);

                firestore.collection("CutPartProduct").document(timestamps).set(hashMap).addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    bottomSheetDialog.dismiss();
                    if (task.isSuccessful()) {

                        changeLenPrOrder(chosenPartIdPrOrder, chosenPartPrOrder, partCutPrObjLen);
                        changeStatusPartPrOrder(orderId, "Kesilmoqda");
                        changeStatusProductsOrder(productObjectOrderId, kusokHolati, keyingiQoldiq, finalMyList);

                        productOrderArrayList.get(position).setPartStatusProductOrder("kesildi");
                        notifyItemChanged(position);

                    } else {
                        Toast.makeText(context, "error to kesish"
                                + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

// Bu tayyor, deylik 12 metr zakas berildi. shundan katta kusokdan 12 metr kesilsa, ishlaydi
            if (zakasBerilganKusokUzunligiOrder == kesiladiganKusokUzunligi) {
                float farq=0;
                String kusokHolati = "kesildi";
                String timestamps = "" + System.currentTimeMillis();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("cutIdPartProductOrder", ""+timestamps);      // vaqti va kesilgan smeta kusogi id
                hashMap.put("chosenPartIdPrOrder", ""+chosenPartIdPrOrder);   // tanlangan smeta kusok id
                hashMap.put("chosenPartPrOrder", ""+chosenPartPrOrder);       // tanlangan smeta kusok uzunligi
                hashMap.put("partCutPrObjLen", ""+partCutPrObjLen);           // kesilgan smeta kusok uzunligi
                hashMap.put("orderId", ""+orderId);                           // Smeta id
                hashMap.put("productObjectOrderId", ""+productObjectOrderId); // productOrderId
                hashMap.put("productId", ""+productId);                    // product id
                hashMap.put("tanlanganKusokUzunligiOrder", ""+productLength);
                hashMap.put("partStatusProductOrder", ""+kusokHolati);                          // holat - status

                firestore.collection("CutPartProduct").document(timestamps).set(hashMap).
                        addOnCompleteListener(task -> {
                            progressDialog.dismiss();
                            bottomSheetDialog.dismiss();
                            if (task.isSuccessful()) {

                                changeLenPrOrder(chosenPartIdPrOrder, chosenPartPrOrder, partCutPrObjLen);
                                changeStatusPartPrOrder(orderId, "Kesilmoqda");
                                changeStatusProductsOrder(productObjectOrderId, kusokHolati, farq, finalMyList);

                                productOrderArrayList.get(position).setPartStatusProductOrder("kesildi");
                                notifyItemChanged(position);

                            } else {
                                Toast.makeText(context, "error to kesish"
                                        + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void changeStatusPartPrOrder(String  orderId, String changeOrderStatus){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("orderStatus", ""+changeOrderStatus);  // kesish holati

        DocumentReference orderRef = firestore.collection("Orders").document(orderId);
        orderRef.update(hashMap).addOnSuccessListener(unused ->{
            Log.d("AdapterProductOrder", "Bajarildi");
        }).addOnFailureListener(e -> Toast.makeText(context, "part not updated "
                        + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void changeStatusProductsOrder(String productObjectOrderId, String kusokHolati,
                                           Float qoldiqKusok, List<String> kesilganKusoklarList){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("partStatusProductOrder", ""+kusokHolati);
        if (!kesilganKusoklarList.isEmpty()) {
            hashMap.put("kesilganKusoklarList", "" + kesilganKusoklarList);
        }
        if (qoldiqKusok>0){
            hashMap.put("qoldiqKusok", "" + qoldiqKusok);
        } else if (qoldiqKusok == 0){
            hashMap.put("partStatusProductOrder", "kesildi");
            hashMap.put("qoldiqKusok", "");
        }

        DocumentReference statusRef = firestore.collection("ProductsOrder").document(productObjectOrderId);
        statusRef.update(hashMap).addOnSuccessListener(unused ->
                Toast.makeText(context, kusokHolati, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "part not updated "
                        + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void changeLenPrOrder(String chosenPartIdPrOrder, String chosenPartPrOrder, String partCutPrObjLen) {

        HashMap<String, Object> hashMap = new HashMap<>();
        float cutPartLen = Float.parseFloat(chosenPartPrOrder) - Float.parseFloat(partCutPrObjLen);
        hashMap.put("partLen", ""+String.format("%.1f",cutPartLen));
        DocumentReference partRef = firestore.collection("Parts").document(chosenPartIdPrOrder);
        partRef.update(hashMap).addOnSuccessListener(unused ->
                        Log.d("AdapterProductOrder", "Bajarildi"))
                .addOnFailureListener(e -> Toast.makeText(context, "part not updated "
                        + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void dismissAllDialogs() {
        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
            bottomSheetDialog.dismiss();
        }
    }

    @Override
    public int getItemCount() {
        return productOrderArrayList.size();
    }

    public class HolderProductOrder extends RecyclerView.ViewHolder {
        private TextView titleProductOrderTV, sumProductOrderTV, lenProductOrderTV, productOrderStatusTV, cutPartsPrOrderTV,
                qoldiqKusokTV;
        private ImageButton delProductOrderBtn, editProductOrderBtn;
        public HolderProductOrder(@NonNull View itemView) {

            super(itemView);

            titleProductOrderTV = itemView.findViewById(R.id.titleProductOrderTV);
            sumProductOrderTV = itemView.findViewById(R.id.sumProductOrderTV);
            lenProductOrderTV = itemView.findViewById(R.id.lenProductOrderTV);
            qoldiqKusokTV = itemView.findViewById(R.id.qoldiqKusokTV);
            productOrderStatusTV = itemView.findViewById(R.id.productOrderStatusTV);
            cutPartsPrOrderTV = itemView.findViewById(R.id.cutPartsPrOrderTV);
            delProductOrderBtn = itemView.findViewById(R.id.delProductOrderBtn);
            editProductOrderBtn = itemView.findViewById(R.id.editProductOrderBtn);
        }
    }
}
