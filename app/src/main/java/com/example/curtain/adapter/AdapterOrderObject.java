package com.example.curtain.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.curtain.R;
import com.example.curtain.activities.OrderDetail;
import com.example.curtain.constants.Constants;
import com.example.curtain.crud.EditOrderObject;
import com.example.curtain.model.ModelOrderObject;
import com.example.curtain.model.ModelProduct;
import com.example.curtain.model.ModelProductObject;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdapterOrderObject extends RecyclerView.Adapter<AdapterOrderObject.HolderOrderObject> {

    public ArrayList<ModelOrderObject> objectArrayList;
    private ArrayList<ModelProduct> productList;
    private ArrayList<ModelProductObject> productObjectArrayList;
    private Context context;
    private AdapterObjectProducts adapterObjectProduct;
    private AdapterProductObject adapterProductObject;
    private ProgressDialog progressDialog;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private SharedPreferences sharedPreferences;

    private BottomSheetDialog bottomSheetDialog, bottomSheetDialog1;

    private String addExtraTxt;
    public AdapterOrderObject(Context context, ArrayList<ModelOrderObject> objectsArrayList, SharedPreferences sharedPreferences) {
        this.context = context;
        this.objectArrayList = objectsArrayList;
        this.sharedPreferences = sharedPreferences;
    }

    public void setOrderObjects(ArrayList<ModelOrderObject> objectArrayList) {
        this.objectArrayList = objectArrayList;
        notifyDataSetChanged(); // UI ni yangilash
    }

    public void deleteOrderObject(int position) {
        if (position >0 && position < objectArrayList.size()){
            objectArrayList.remove(position);
            notifyItemRemoved(position);
        }
    }

    class HolderOrderObject extends RecyclerView.ViewHolder {

        private TextView orderRoomTV, orderObjectTV, orderObjectDescTV;
        public HolderOrderObject(@NonNull View itemView) {
            super(itemView);

            orderRoomTV = itemView.findViewById(R.id.orderRoomTV);
            orderObjectTV = itemView.findViewById(R.id.orderObjectTV);
            orderObjectDescTV = itemView.findViewById(R.id.orderObjectDescTV);
        }
    }

    @NonNull
    @Override
    public AdapterOrderObject.HolderOrderObject onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_object, parent, false);
        return new AdapterOrderObject.HolderOrderObject(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterOrderObject.HolderOrderObject holder, int position) {

        productList = new ArrayList<>();

        progressDialog = new ProgressDialog(holder.itemView.getContext());
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        final ModelOrderObject modelOrderObject = objectArrayList.get(position);

        String orderRoom = modelOrderObject.getOrderRoom();
        String objRoom = modelOrderObject.getObjRoom();
        String objDesc = modelOrderObject.getObjDescET();

        holder.orderRoomTV.setText(orderRoom);
        holder.orderObjectTV.setText(objRoom);
        if (objDesc!=null){
            holder.orderObjectDescTV.setText(objDesc);
        } else {
            holder.orderObjectDescTV.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(view -> orderObjectBottomSheet(modelOrderObject,
                objectArrayList, position, sharedPreferences));
    }

    private void orderObjectBottomSheet(ModelOrderObject modelOrderObject, ArrayList<ModelOrderObject> objectArrayList,
                                        int position, SharedPreferences sharedPreferences){
        bottomSheetDialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.bs_order_object, null);
        // set view to bottomSheet
        bottomSheetDialog.setContentView(view);

        String sharedUserType = sharedPreferences.getString("user_type", "");

        ImageButton backBtn = view.findViewById(R.id.backBtn);
        ImageButton delOrderObjectBtn = view.findViewById(R.id.delOrderObjectBtn);
        ImageButton editOrderObjectBtn = view.findViewById(R.id.editOrderObjectBtn);

        ImageButton editOrderPoshivIB = view.findViewById(R.id.editOrderPoshivIB);
        ImageButton editOrderUstanovkaIB = view.findViewById(R.id.editOrderUstanovkaIB);

        Button addPrToObjectBtn = view.findViewById(R.id.addPrToObjectBtn);
        Button addExtraObjectBtn = view.findViewById(R.id.addExtraObjectBtn);

        RecyclerView productOrderObjectsRV = view.findViewById(R.id.productOrderObjectsRV);

        LinearLayout addExtraPoshivLL = view.findViewById(R.id.addExtraPoshivLL);
        LinearLayout addExtraUstanovkaLL = view.findViewById(R.id.addExtraUstanovkaLL);

        TextView objectSumTV = view.findViewById(R.id.objectSumTV);
        TextView objectCostTV = view.findViewById(R.id.objectCostTV);
        TextView objectProfitTV = view.findViewById(R.id.objectProfitTV);
        TextView nameOrderObjectTV = view.findViewById(R.id.nameOrderObjectTV);
        TextView orderObjectPoshivPriceTV = view.findViewById(R.id.orderObjectPoshivPriceTV);
        TextView orderObjectUstanovkaPriceTV = view.findViewById(R.id.orderObjectUstanovkaPriceTV);
        TextView noPartsTxt = view.findViewById(R.id.NoPartsTxt);
        TextView descOrderObjectTV = view.findViewById(R.id.descOrderObjectTV);

        objectCostTV.setVisibility(View.GONE);
        objectProfitTV.setVisibility(View.GONE);

        String objectSum = modelOrderObject.getObjectSum();
        String objectCost = modelOrderObject.getObjectCost();
        String objectProfit = modelOrderObject.getObjectProfit();
        String orderRoom = modelOrderObject.getOrderRoom();
        String objRoom = modelOrderObject.getObjRoom();
        String orderObjectId = modelOrderObject.getOrderObjectId();
        String orderId = modelOrderObject.getOrderId();
        String objectDesc = modelOrderObject.getObjDescET();
        String objectPoshiv = modelOrderObject.getObjectPoshiv();
        String objectUstanovka = modelOrderObject.getObjectUstanovka();

        if (sharedUserType.equals("sklad") || sharedUserType.equals("bichuvchi")){
            delOrderObjectBtn.setVisibility(View.GONE);
            editOrderObjectBtn.setVisibility(View.GONE);
            addPrToObjectBtn.setVisibility(View.GONE);
            addExtraObjectBtn.setVisibility(View.GONE);
            editOrderPoshivIB.setVisibility(View.GONE);
            editOrderUstanovkaIB.setVisibility(View.GONE);
        }

        if (objectDesc!=null){
            descOrderObjectTV.setText(objectDesc);
        } else {
            descOrderObjectTV.setVisibility(View.GONE);
        }

        if (objectPoshiv!=null){
            orderObjectPoshivPriceTV.setText(objectPoshiv);
        } else {
            addExtraPoshivLL.setVisibility(View.GONE);
        }
        if (objectUstanovka!=null){
            orderObjectUstanovkaPriceTV.setText(objectUstanovka);
        } else {
            addExtraUstanovkaLL.setVisibility(View.GONE);
        }

        if (objectSum!=null){
            objectSumTV.setText(objectSum);
        } else {
            objectSumTV.setVisibility(View.GONE);
        }

        if (sharedUserType.equals("superAdmin")){
            if (objectCost!=null) {
                objectCostTV.setVisibility(View.VISIBLE);
                objectCostTV.setText(objectCost);
            }
            if (objectProfit!=null) {
                objectProfitTV.setVisibility(View.VISIBLE);
                objectProfitTV.setText(objectProfit);
            }
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference partRef = firestore.collection("OrderObjects").document(orderObjectId);

        editOrderPoshivIB.setOnClickListener(view3 -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context.getApplicationContext());
            View dialogView = inflater.inflate(R.layout.dialog_edit_part, null);
            alertDialog.setTitle("O'zgartirish");

            EditText editPartET = dialogView.findViewById(R.id.editPartET);

            alertDialog.setView(dialogView)
                    .setPositiveButton(R.string.save_me, (dialogInterface, i) -> {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("objectPoshiv", editPartET.getText().toString().trim());
                        if (!TextUtils.isEmpty(editPartET.getText().toString().trim())) {
                            partRef.update(hashMap).addOnSuccessListener(unused -> {
                                        modelOrderObject.setObjectPoshiv(editPartET.getText().toString().trim()); // Modelni yangilash
                                        notifyItemChanged(position); // UI ni yangilash
                                        Toast.makeText(context, "Poshiv haqi O'zgardi", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(context, "Xatolik: Qism o'zgarmadi: "
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

        editOrderUstanovkaIB.setOnClickListener(view3 -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context.getApplicationContext());
            View dialogView = inflater.inflate(R.layout.dialog_edit_part, null);
            alertDialog.setTitle("O'zgartirish");

            EditText editPartET = dialogView.findViewById(R.id.editPartET);

            alertDialog.setView(dialogView)
                    .setPositiveButton(R.string.save_me, (dialogInterface, i) -> {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("objectUstanovka", editPartET.getText().toString().trim());
                        if (!TextUtils.isEmpty(editPartET.getText().toString().trim())) {
                            partRef.update(hashMap).addOnSuccessListener(unused -> {
                                        modelOrderObject.setObjectPoshiv(editPartET.getText().toString().trim()); // Modelni yangilash
                                        notifyItemChanged(position); // UI ni yangilash
                                        Toast.makeText(context, "Ustanovka haqi O'zgardi", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(context, "Xatolik: Qism o'zgarmadi: "
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

        nameOrderObjectTV.setText(String.format("%s, %s", orderRoom, objRoom));

        productObjectArrayList = new ArrayList<>();

        loadProductObjects(orderObjectId,productOrderObjectsRV, noPartsTxt);

        bottomSheetDialog.show();

        backBtn.setOnClickListener(view2 -> {
            bottomSheetDialog.dismiss();
        });

        editOrderObjectBtn.setOnClickListener(view1 -> {
            progressDialog.dismiss();
            Intent intent = new Intent(context, EditOrderObject.class);
            intent.putExtra("orderObjectId", orderObjectId);
            context.startActivity(intent);
        });

        delOrderObjectBtn.setOnClickListener(view13 -> {
            progressDialog.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("O'chirish").setMessage("Rostdan o'chirmoqchimisiz?")
                    .setPositiveButton("O'chirish", (dialog, which) -> {
                        // delete
                        partRef.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult().exists()){
                                String deletedProductId;
                                if (task.getResult().contains("orderObjectId")){
                                    deletedProductId = task.getResult().getString("orderObjectId");
                                } else {
                                    deletedProductId = null;
                                }
                                partRef.delete().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()){
                                        Toast.makeText(context, "Obyekt o'chirildi", Toast.LENGTH_SHORT).show();
                                        if (deletedProductId!=null){
                                            deleteProductByObjects(deletedProductId);
                                        }
                                        objectArrayList.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, objectArrayList.size());
                                        bottomSheetDialog.dismiss();
                                    } else {
                                        Toast.makeText(context, "Mahsulot o'chirishda xato", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(context, "Mahsulot topilmadi", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();
        });

        addPrToObjectBtn.setOnClickListener(view12 -> {

            bottomSheetDialog1 = new BottomSheetDialog(context);
            view12 = LayoutInflater.from(context).inflate(R.layout.bs_pr_to_object, null);
            bottomSheetDialog1.setContentView(view12);

            TextInputEditText searchPrObjET = view12.findViewById(R.id.searchPrObjET);
            TextInputEditText prObjLenET = view12.findViewById(R.id.prObjLenET);
            TextView searchPrIdObjET = view12.findViewById(R.id.searchPrIdObjET);
            searchPrIdObjET.setVisibility(View.GONE);

            RecyclerView prObjRV = view12.findViewById(R.id.prObjRV);
            Button savePrObjBtn = view12.findViewById(R.id.savePrObjBtn);

            bottomSheetDialog1.show();

            CollectionReference collectionReference  = firebaseFirestore.collection("Products");
            collectionReference.addSnapshotListener((value, error) -> {
                if (error!=null){
                    Toast.makeText(context, "Mahsulotlar yuklashda xato", Toast.LENGTH_SHORT).show();
                }
                if (value != null && value.isEmpty()) {
                    progressDialog.dismiss();
                    prObjRV.setVisibility(View.GONE);
                }
                productList.clear();
                for (QueryDocumentSnapshot documentSnapshot:value){
                    ModelProduct modelProduct = documentSnapshot.toObject(ModelProduct.class);
                    productList.add(modelProduct);
                }
                adapterObjectProduct = new AdapterObjectProducts(context, productList, searchPrObjET, searchPrIdObjET);
                prObjRV.setLayoutManager(new LinearLayoutManager(context));
                prObjRV.setAdapter(adapterObjectProduct);
            });

            searchPrObjET.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    try {
                        adapterObjectProduct.getFilter().filter(charSequence);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            savePrObjBtn.setOnClickListener(view1 -> {
                String productObject =  searchPrObjET.getText().toString().trim();
                String lenProductOrder = prObjLenET.getText().toString().trim();
                String productId = searchPrIdObjET.getText().toString();

                if (TextUtils.isEmpty(productObject)){
                    Toast.makeText(context, "Pardani tanlang...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(lenProductOrder)){
                    Toast.makeText(context, "Uzunligini kiriting...", Toast.LENGTH_SHORT).show();
                    return;
                }
                String timestamps = "" + System.currentTimeMillis();
                HashMap<String, Object> hashMap = new HashMap<>();

                hashMap.put("titleProductObject", productObject);
                hashMap.put("lenProductObject", lenProductOrder);
                hashMap.put("objectOrderId", orderObjectId);
                hashMap.put("productObjectId", timestamps);
                hashMap.put("orderId", orderId);
                hashMap.put("productId", productId);
                hashMap.put("partStatusProductObject", "holat");
                hashMap.put("created_by", firebaseAuth.getCurrentUser().getDisplayName());

                DocumentReference productRef = firebaseFirestore.collection("Products").document(productId);
                productRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()){
                            String productPrice = documentSnapshot.getString("prPrice");
                            String productCost = documentSnapshot.getString("prCost");
                            String productType = documentSnapshot.getString("prCat");

                            double len = Double.parseDouble(lenProductOrder);
                            double price = productPrice != null ? Double.parseDouble(productPrice): 0.0;
                            double cost = productCost != null ? Double.parseDouble(productCost): 0.0;
                            double objectSumDouble = productPrice != null ? price * len : 0.0;
                            double objectCosDouble = productCost != null ? len * cost : 0.0;

                            DecimalFormat df = new DecimalFormat("#.0");
                            String formattedObjectSum = df.format(objectSumDouble);
                            String formattedObjectCost = df.format(objectCosDouble);

                            if (productPrice != null){
                                hashMap.put("productPriceProductOrder", productPrice);
                                hashMap.put("objectSum", formattedObjectSum);
                            }
                            if (productCost!= null){
                                hashMap.put("productCostProductOrder", productCost);
                                hashMap.put("objectCost", formattedObjectCost);
                            }
                            if (productType != null){
                                hashMap.put("productTypeProductOrder", productType);
                            }

                            firebaseFirestore.collection("ProductObjectOrder").document(timestamps).set(hashMap).
                                    addOnCompleteListener(task1 -> {
                                        progressDialog.dismiss();
                                        if (task1.isSuccessful()){
//                                            double currentObjectSum = modelOrderObject.getObjectSum() != null ?
//                                                    Double.parseDouble(modelOrderObject.getObjectSum()): 0.0;
//                                            double currentObjectCost = modelOrderObject.getObjectCost() != null ?
//                                                    Double.parseDouble(modelOrderObject.getObjectCost()): 0.0;
//                                            if (productPrice != null){
//                                                currentObjectSum += objectSumDouble;
//                                                modelOrderObject.setObjectSum(df.format(currentObjectSum));
//                                            }
//                                            if (productCost!=null) {
//                                                currentObjectCost += objectCosDouble;
////                                                modelOrderObject.setObjectCost(String.valueOf(currentObjectCost));
//                                                modelOrderObject.setObjectCost(df.format(currentObjectCost));
//                                            }

//                                            Map<String, Object> updateFields = new HashMap<>();
//                                            if (productPrice != null) {
//                                                updateFields.put("objectSum", modelOrderObject.getObjectSum());
//                                            }
//                                            if (productCost != null) {
//                                                updateFields.put("objectCost", modelOrderObject.getObjectCost());
//                                            }

//                                            double finalCurrentObjectSum = currentObjectSum;
//                                            double finalCurrentObjectCost = currentObjectCost;
                                            // o'zgartirish kerak shu joyini
//                                            firebaseFirestore.collection("OrderObjects").document(modelOrderObject.getOrderObjectId())
//                                                            .update(updateFields)
//                                                                    .addOnCompleteListener(updateTask->{
//                                                                        if (updateTask.isSuccessful()) {
//                                                                            Toast.makeText(context, "Qo'shildi", Toast.LENGTH_SHORT).show();
//                                                                            bottomSheetDialog1.dismiss();
//                                                                            loadProductObjects(modelOrderObject.getOrderObjectId(), productOrderObjectsRV, noPartsTxt);
//                                                                            TextView objectSumTVNext = view.findViewById(R.id.objectSumTV);
//                                                                            objectSumTVNext.setText(String.valueOf(finalCurrentObjectSum));
//                                                                            TextView objectCostTVNext = view.findViewById(R.id.objectCostTV);
//                                                                            objectCostTVNext.setText(String.valueOf(finalCurrentObjectCost));
//                                                                            adapterProductObject.notifyDataSetChanged();
//                                                                        } else {
//                                                                            Toast.makeText(context, "Object summasini yangilashda xato " +
//                                                                                    updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                                                                        }
//                                                                    });
                                                                                                                        Toast.makeText(context, "Qo'shildi", Toast.LENGTH_SHORT).show();
                                                                                                                        bottomSheetDialog1.dismiss();
                                            loadProductObjects(modelOrderObject.getOrderObjectId(), productOrderObjectsRV, noPartsTxt);
                                                                                                                        adapterProductObject.notifyDataSetChanged();
                                        } else {
                                            Toast.makeText(context, "Qo'shishda muammo " +
                                                    task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(context, "Mahsulot topilmadi", Toast.LENGTH_SHORT).show();
                    }
                });

            });
        });

        addExtraObjectBtn.setOnClickListener(view14 -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context.getApplicationContext());
            View dialogView = inflater.inflate(R.layout.dialog_add_extra_to_order, null);
            alertDialog.setTitle("Poshiv qo'shish");

            Spinner addExtraSpinner = dialogView.findViewById(R.id.addExtraSpinner);
            EditText addExtraPriceET = dialogView.findViewById(R.id.addExtraPriceET);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                    android.R.layout.simple_spinner_item, Constants.extraOrder);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            addExtraSpinner.setAdapter(adapter);

            addExtraSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view12, int i, long l) {
                    if (!addExtraSpinner.getSelectedItem().toString().trim().equalsIgnoreCase("Tanlang:")){
                        addExtraTxt = addExtraSpinner.getSelectedItem().toString().trim();
                    } else {
                        TextView errTxt = (TextView) addExtraSpinner.getSelectedView();
                        errTxt.setError("");
                        errTxt.setTextColor(Color.RED);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    Toast.makeText(context, "Hech narsa tanlanmadi", Toast.LENGTH_SHORT).show();
                }
            });

            alertDialog.setView(dialogView)
                    .setPositiveButton(R.string.save_me, (dialogInterface, i) -> {
                        if (!TextUtils.isEmpty(addExtraPriceET.getText()) &&
                                !addExtraSpinner.getSelectedItem().toString().trim().equalsIgnoreCase("Tanlang:")
                        ) {
                            String addExtraPrice = addExtraPriceET.getText().toString().trim();
                            HashMap<String, Object> hashMap = new HashMap<>();

                            if (addExtraTxt.equals("Poshiv")){
                                hashMap.put("objectPoshiv", "" + addExtraPrice);
                            }
                            if (addExtraTxt.equals("Ustanovka")){
                                hashMap.put("objectUstanovka", "" + addExtraPrice);
                            }
                            firebaseFirestore.collection("OrderObjects").document(orderObjectId).update(hashMap)
                                    .addOnCompleteListener(task -> {
                                        progressDialog.dismiss();
                                        if (task.isSuccessful()){
                                            Toast.makeText(context, "Qo'shildi", Toast.LENGTH_SHORT).show();
                                            if (addExtraTxt.equals("Poshiv")){
                                                addExtraToOrder("orderPoshiv", addExtraPrice, orderId);
                                                orderObjectPoshivPriceTV.setText(addExtraPrice);
                                                addExtraPoshivLL.setVisibility(View.VISIBLE);
                                            }
                                            if (addExtraTxt.equals("Ustanovka")){
                                                addExtraToOrder("orderUstanovka", addExtraPrice, orderId);
                                                orderObjectUstanovkaPriceTV.setText(addExtraPrice);
                                                addExtraPoshivLL.setVisibility(View.VISIBLE);
                                            }

//                                            Intent intent = new Intent(context, OrderDetail.class);
//                                            intent.putExtra("orderId", orderId);
//                                            context.startActivity(intent);

                                            Toast.makeText(context, "Qo'shildi", Toast.LENGTH_SHORT).show();

                                        } else {
                                            Toast.makeText(context, "Qo'shishda muammo " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(context, "Saqlanmadi", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());

            alertDialog.setView(dialogView);
            AlertDialog dialog = alertDialog.create();
            dialog.show();
        });

    }

    public void dismissAllDialogs() {
        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
            bottomSheetDialog.dismiss();
        }
            if (bottomSheetDialog1 != null && bottomSheetDialog1.isShowing()) {
                bottomSheetDialog1.dismiss();
            }
    }

    private void addExtraToOrder(String orderAddExtra, String addExtraPrice, String orderId) {

        DocumentReference orderRef = firebaseFirestore.collection("Orders").document(orderId);
        orderRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    String ordZaklad = doc.getString(orderAddExtra);
                    HashMap<String, Object> hashMap = new HashMap<>();
                    float newZaklad;
                    if (ordZaklad != null) {
                        newZaklad = Float.parseFloat(ordZaklad) + Float.parseFloat(addExtraPrice);
                    } else {
                        newZaklad = Float.parseFloat(addExtraPrice);
                    }
                    hashMap.put(orderAddExtra, "" + newZaklad);

                    orderRef.update(hashMap).addOnSuccessListener(unused -> {
//                        loadOrderDetail(orderId);
                        Toast.makeText(context, "O'zgartirildi", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> Toast.makeText(context,
                            "Not updated: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                } else {
                    Toast.makeText(context, "Smeta topilmadi", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Error fetching Smeta: "
                        + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadProductObjects(String orderObjectId, RecyclerView productOrderObjectsRV, TextView noPartsTxt) {
        CollectionReference reference = firebaseFirestore.collection("ProductObjectOrder");
        reference.whereEqualTo("objectOrderId", orderObjectId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                progressDialog.dismiss();
                productObjectArrayList.clear();
                for (DocumentSnapshot snapshot : task.getResult()) {
                    ModelProductObject modelProductObject = snapshot.toObject(ModelProductObject.class);
                    productObjectArrayList.add(modelProductObject);
                }

                if (productObjectArrayList.isEmpty()){
                    productOrderObjectsRV.setVisibility(View.GONE);
                    noPartsTxt.setVisibility(View.VISIBLE);
                } else {
                    noPartsTxt.setVisibility(View.GONE);
                }
                    adapterProductObject = new AdapterProductObject(context, productObjectArrayList, sharedPreferences);
                    productOrderObjectsRV.setAdapter(adapterProductObject);
                    adapterProductObject.notifyDataSetChanged();
            } else {
                progressDialog.dismiss();
                Toast.makeText(context, "yuklashda xato", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteProductByObjects(String deletedProductId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference partsPref = firestore.collection("ProductObjectOrder");
        partsPref.whereEqualTo("objectOrderId", deletedProductId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                for (DocumentSnapshot document : task.getResult()){
                    document.getReference().delete();
                }
            } else {
                Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return objectArrayList.size();
    }
}
