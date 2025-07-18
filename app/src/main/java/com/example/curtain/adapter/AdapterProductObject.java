package com.example.curtain.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import com.example.curtain.activities.OrderDetail;
import com.example.curtain.constants.Constants;
import com.example.curtain.model.ModelPart;
import com.example.curtain.model.ModelProductObject;
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

public class AdapterProductObject  extends RecyclerView.Adapter<AdapterProductObject.HolderProductObject>{
    private Context context;
    private ArrayList<ModelProductObject> productObjectArrayList;
    private ArrayList<ModelPart> partsList;
    private SharedPreferences sharedPreferences;
    private String sharedUserType;
    private ProgressDialog progressDialog;
    private FirebaseFirestore firestore;
    private AdapterCutPartPrOrder adapterCutPartPrOrder;

    public AdapterProductObject(Context context, ArrayList<ModelProductObject> productObjectArrayList,
                                SharedPreferences sharedPreferences) {
        this.context = context;
        this.productObjectArrayList = productObjectArrayList;
        this.sharedPreferences = sharedPreferences;
    }
    @NonNull
    @Override
    public AdapterProductObject.HolderProductObject onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_orders_item, parent, false);
        return new AdapterProductObject.HolderProductObject(view);
    }


    @Override
    public void onBindViewHolder(@NonNull AdapterProductObject.HolderProductObject holder, int position){
        progressDialog = new ProgressDialog(holder.itemView.getContext());
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        sharedUserType = sharedPreferences.getString("user_type", "");
        String sharedUserType = sharedPreferences.getString("user_type", "");

        final ModelProductObject modelProductObject = productObjectArrayList.get(position);
        String productTitle = modelProductObject.getTitleProductObject();
        String productLength = modelProductObject.getLenProductObject();
        String productObjectId = modelProductObject.getProductObjectId();
        String productId = modelProductObject.getProductId();
        String orderId = modelProductObject.getOrderId();
        String partStatusProductObject = modelProductObject.getPartStatusProductObject();
        String productPriceProductOrder = modelProductObject.getProductPriceProductOrder();
        String productType = modelProductObject.getProductTypeProductOrder();

        firestore = FirebaseFirestore.getInstance();

        if (partStatusProductObject!=null){
            holder.productOrderStatusTV.setText(partStatusProductObject);
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
//                            holder.editProductOrderBtn.setVisibility(View.GONE);
                        }
                    }
                } else {
                    Toast.makeText(context, "Smeta topilmadimi", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Error fetching Smeta: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        DocumentReference prObjRef = firestore.collection("ProductObjectOrder").document(productObjectId);
        prObjRef.get().addOnCompleteListener(task -> {
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

        // delete items from objects
        holder.delProductOrderBtn.setOnClickListener(view -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Delete").setMessage("O'chirmoqchimisiz?")
                    .setPositiveButton("Delete", (dialog, which) ->
                            prObjRef.delete().addOnCompleteListener(task -> {
                                if (task.isSuccessful()){

                                    Toast.makeText(context, "Kusok o'chirildi", Toast.LENGTH_SHORT).show();
                                    updateOrderObjectsSumAndCost(orderId);
                                    productObjectArrayList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, productObjectArrayList.size());
                                } else {
                                    Toast.makeText(context, "kusok o'chirishda xato "
                                            + task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(e -> Toast.makeText(context, "Error at Deleted Part..."
                                    + e.getMessage(), Toast.LENGTH_SHORT).show())).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();

        });

        // if sharedUser type equals bichuvchi, then hide holder.editProductOrderBtn
        if (sharedUserType.equals("bichuvchi")){
            holder.editProductOrderBtn.setVisibility(View.GONE);
        }

        // update len items from objects
        holder.editProductOrderBtn.setOnClickListener(view -> {

            showEditProductDialog(modelProductObject, position, productObjectId);

        });

        if (sharedUserType.equals("sklad") || sharedUserType.equals("bichuvchi")){
            holder.delProductOrderBtn.setVisibility(View.GONE);
            holder.editProductOrderBtn.setVisibility(View.GONE);
        }

        // check productType with null, if not null, set text
        if (productType != null) {
            if (productType.startsWith("T")){
                holder.titleProductOrderTV.setText(String.format("T:  %s", productTitle));
            } else if (productType.startsWith("P")){
                holder.titleProductOrderTV.setText(String.format("P:  %s", productTitle));
            } else {
                holder.titleProductOrderTV.setText(productTitle);
            }
        } else {
            holder.titleProductOrderTV.setText(productTitle);
        }

        holder.lenProductOrderTV.setText(String.format("%s m", productLength));

        float price = Float.parseFloat(productPriceProductOrder);
        float len = Float.parseFloat(productLength);
        float sum = price * len;
        holder.sumProductOrderTV.setText(String.format("%s $", sum));

        holder.qoldiqKusokTV.setVisibility(View.GONE);
        holder.cutPartsPrOrderTV.setVisibility(View.GONE);

        if (holder.productOrderStatusTV.getText().toString().equals("bichildi")) {
            holder.productOrderStatusTV.setVisibility(View.VISIBLE); // Yashirilmasligini ta'minlash
        }

        if (holder.productOrderStatusTV.getText().toString().equals("kesildi")){
            // change text color to cutColor from colors.xml
            holder.productOrderStatusTV.setTextColor(Color.parseColor("#008000"));
        } else if (holder.productOrderStatusTV.getText().toString().equals("bichildi")){
            // change text color to cuttingColor from colors.xml
            holder.productOrderStatusTV.setTextColor(Color.parseColor("#800000"));
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

        holder.productOrderStatusTV.setOnClickListener(view -> {
            if (sharedUserType.equals(Constants.userTypes[4]) || sharedUserType.equals(Constants.userTypes[0]))
            {
                if (holder.productOrderStatusTV.getText().toString().equals("holat") ||
                        holder.productOrderStatusTV.getText().toString().equals("kesilmoqda")
                ) {
                    cuttingBottomSheetDialog(productTitle, productId, productLength, productObjectId,
                            orderId, holder.qoldiqKusokTV, holder.cutPartsPrOrderTV);
                }
            }
            else if (holder.productOrderStatusTV.getText().toString().equals("kesildi")){
                if (sharedUserType.equals(Constants.userTypes[4]) || sharedUserType.equals(Constants.userTypes[2])){
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle("Info").setMessage("Bichildimi?").setPositiveButton("Ha", (dialogInterface, i) -> {
                        String status = "bichildi";
                        String statusOrder = "bichilmoqda";
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("partStatusProductObject", ""+status);
                        DocumentReference statusRef = firestore.collection("ProductObjectOrder").document(productObjectId);
                        statusRef.update(hashMap).addOnSuccessListener(unused -> {

                            Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
                            productObjectArrayList.get(position).setPartStatusProductObject("bichildi");
                            holder.productOrderStatusTV.setText(status);
                            changeStatusPartPrOrder(orderId, statusOrder);
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

    private void updateOrderObjectsSumAndCost(String orderId) {

    }

    private void showEditProductDialog(ModelProductObject modelProductObject, int position, String productObjectId) {

        DocumentReference prObjRef = firestore.collection("ProductObjectOrder").document(productObjectId);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context.getApplicationContext());
        View dialogView = inflater.inflate(R.layout.dialog_edit_part, null);
        alertDialog.setView(dialogView);
        alertDialog.setTitle("O'zgartirish");

        EditText editPartET = dialogView.findViewById(R.id.editPartET);
        editPartET.setText(modelProductObject.getLenProductObject());

        alertDialog.setView(dialogView)
                .setPositiveButton(R.string.save_me, (dialogInterface, i) -> {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("lenProductObject", ""+editPartET.getText().toString().trim());

                    if (!TextUtils.isEmpty(editPartET.getText())) {
                        prObjRef.update(hashMap).addOnSuccessListener(unused -> {

                            modelProductObject.setLenProductObject(editPartET.getText().toString().trim()); // Modelni yangilash
                            notifyItemChanged(position); // UI ni yangilash
                            Toast.makeText(context, "Kusok uzunligi o'zgardi...", Toast.LENGTH_SHORT).show();

                        })

                                .addOnFailureListener(e -> Toast.makeText(context, "part not updated "
                                        + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(context, "Miqdor kiritilmagan yoki xato ", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    Toast.makeText(context, "cancel", Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                        });
//        alertDialog.setPositiveButton("O'zgartirish", (dialogInterface, i) -> {
//            String newLen = editPartET.getText().toString().trim();
//            if (TextUtils.isEmpty(newLen)){
//                Toast.makeText(context, "Miqdor kiritilmagan yoki xato ", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            Toast.makeText(context, "update", Toast.LENGTH_SHORT).show();
//            updateProductLength(modelProductObject, newLen, position);
//            }).setNegativeButton("Bekor qilish", (dialogInterface, i) -> dialogInterface.dismiss());

        alertDialog.create().show();
    }

    private void updateProductLength(ModelProductObject modelProductObject, String newLen, int position) {
        double oldLen = Double.parseDouble(modelProductObject.getLenProductObject());
        double newLength = Double.parseDouble(newLen);
        double price = modelProductObject.getProductPriceProductOrder() != null ?
                Double.parseDouble(modelProductObject.getProductPriceProductOrder()) : 0;


    }

    private void cuttingBottomSheetDialog(String productTitle, String productId, String productLength,
                                          String productObjectId,
                                          String orderId, TextView kelganQoldiqKusokTV,
                                          TextView cutPartsPrOrderTV) {
        ArrayList<String> kesilganKusoklarList = new ArrayList<>(); // Create an ArrayList object

        String str = cutPartsPrOrderTV.getText().toString().trim();
        if (!str.equals("kesilgan kusoklar:")){
            String numberStr = str.substring(1, str.length()-1);
            kesilganKusoklarList = new ArrayList<>(Arrays.asList(numberStr.split(",")));
        }

        String kelganQoldiqKusok = kelganQoldiqKusokTV.getText().toString().trim();

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
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
                    hashMap.put("productObjectId", ""+productObjectId); // productObjectId
                    hashMap.put("productId", ""+productId);                    // product id
                    hashMap.put("tanlanganKusokUzunligiOrder", ""+productLength);

                    firestore.collection("CutPartProduct").document(timestamps).set(hashMap).addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {

                            changeLenPrOrder(chosenPartIdPrOrder, chosenPartPrOrder, partCutPrObjLen);
                            changeStatusPartPrOrder(orderId, "kesilmoqda");
                            changeStatusProductsObjectOrder(productObjectId, kusokHolati, keyingiQoldiq, finalMyList);
                            Intent intent = new Intent(context, OrderDetail.class);
                            intent.putExtra("orderId", orderId);
                            context.startActivity(intent);
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
                hashMap.put("cutIdPartProductOrder",  ""+timestamps);      // vaqti va kesilgan smeta kusogi id
                hashMap.put("chosenPartIdPrOrder", ""+chosenPartIdPrOrder);   // tanlangan smeta kusok id
                hashMap.put("chosenPartPrOrder", ""+chosenPartPrOrder);       // tanlangan smeta kusok uzunligi
                hashMap.put("partCutPrObjLen", ""+partCutPrObjLen);           // kesilgan smeta kusok uzunligi
                hashMap.put("orderId", ""+orderId);                           // Smeta id
                hashMap.put("productObjectId", ""+productObjectId); // productObjectId
                hashMap.put("productId", ""+productId);                    // product id
                hashMap.put("tanlanganKusokUzunligiOrder", ""+productLength);

                firestore.collection("CutPartProduct").document(timestamps).set(hashMap).addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        changeLenPrOrder(chosenPartIdPrOrder, chosenPartPrOrder, partCutPrObjLen);
                        changeStatusPartPrOrder(orderId, "Kesilmoqda");
                        changeStatusProductsObjectOrder(productObjectId, kusokHolati, keyingiQoldiq, finalMyList);
                        Intent intent = new Intent(context, OrderDetail.class);
                        intent.putExtra("orderId", orderId);
                        context.startActivity(intent);
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
                hashMap.put("productObjectId", ""+productObjectId); // productObjectId
                hashMap.put("productId", ""+productId);                    // product id
                hashMap.put("tanlanganKusokUzunligiOrder", ""+productLength);
                hashMap.put("partStatusProductObject", ""+kusokHolati);                          // holat - status

                firestore.collection("CutPartProduct").document(timestamps).set(hashMap).addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        changeLenPrOrder(chosenPartIdPrOrder, chosenPartPrOrder, partCutPrObjLen);
                        changeStatusPartPrOrder(orderId, "Kesilmoqda");
                        changeStatusProductsObjectOrder(productObjectId, kusokHolati, farq, finalMyList);
                        Intent intent = new Intent(context, OrderDetail.class);
                        intent.putExtra("orderId", orderId);
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "error to kesish"
                                + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });
    }

    // change order status
    private void changeStatusPartPrOrder(String orderId, String changeOrderStatus) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("orderStatus", ""+changeOrderStatus);  // kesish holati

        DocumentReference orderRef = firestore.collection("Orders").document(orderId);
        orderRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()){

                    String orderStatus = doc.getString("orderStatus");
                    if (!orderStatus.equals("bichilmoqda")){
                        orderRef.update(hashMap).addOnSuccessListener(unused ->
                                Log.d("AdapterProductObject", "Bajarildi"));
                    }

                } else {
                    Toast.makeText(context, "Smeta topilmadi", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Error fetching Smeta: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changeStatusProductsObjectOrder(String productObjectId, String kusokHolati,
                                           Float qoldiqKusok, List<String> kesilganKusoklarList){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("partStatusProductObject", ""+kusokHolati);
        if (!kesilganKusoklarList.isEmpty()) {
            hashMap.put("kesilganKusoklarList", ""+kesilganKusoklarList);
        }
        if (qoldiqKusok>0){
            hashMap.put("qoldiqKusok", ""+qoldiqKusok);
        } else if (qoldiqKusok == 0){
            hashMap.put("partStatusProductObject", "kesildi");
            hashMap.put("qoldiqKusok", "");
        }
        DocumentReference statusRef = firestore.collection("ProductObjectOrder").document(productObjectId);
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

    @Override
    public int getItemCount(){
        return productObjectArrayList.size();
    }

    public class HolderProductObject extends RecyclerView.ViewHolder{
        private TextView titleProductOrderTV, lenProductOrderTV, productOrderStatusTV, qoldiqKusokTV, sumProductOrderTV,
                cutPartsPrOrderTV;
        private ImageButton delProductOrderBtn, editProductOrderBtn;

        public HolderProductObject(@NonNull View itemView){
            super(itemView);

            titleProductOrderTV = itemView.findViewById(R.id.titleProductOrderTV);
            lenProductOrderTV = itemView.findViewById(R.id.lenProductOrderTV);
            delProductOrderBtn = itemView.findViewById(R.id.delProductOrderBtn);
            editProductOrderBtn = itemView.findViewById(R.id.editProductOrderBtn);
            productOrderStatusTV = itemView.findViewById(R.id.productOrderStatusTV);
            qoldiqKusokTV = itemView.findViewById(R.id.qoldiqKusokTV);
            sumProductOrderTV = itemView.findViewById(R.id.sumProductOrderTV);
            cutPartsPrOrderTV = itemView.findViewById(R.id.cutPartsPrOrderTV);
        }
    }
}
