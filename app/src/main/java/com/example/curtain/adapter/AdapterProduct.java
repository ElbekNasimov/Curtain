package com.example.curtain.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.curtain.R;
import com.example.curtain.activities.CutPartsListActivity;
import com.example.curtain.constants.Constants;
import com.example.curtain.crud.EditProduct;
import com.example.curtain.filter.FilterProduct;
import com.example.curtain.model.ModelPart;
import com.example.curtain.model.ModelProduct;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AdapterProduct extends RecyclerView.Adapter<AdapterProduct.HolderProduct> implements Filterable {

    private Context context;
    public ArrayList<ModelProduct> productList, filterList;
    private ArrayList<ModelPart> partsList;
    private FilterProduct filterProduct;
    private ProgressDialog progressDialog;
    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;
    private AdapterPart adapterPart;
    private String location, measurement;
    private SharedPreferences sharedPreferences;
    boolean click = false;  //       To hide/open product cost status

    public AdapterProduct(Context context, ArrayList<ModelProduct> productList, SharedPreferences sharedPreferences) {
        this.context = context;
        this.productList = productList;
        this.filterList = productList;
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public Filter getFilter() {
        if (filterProduct == null){
            filterProduct = new FilterProduct(this, filterList);
        }
        return filterProduct;
    }

    @NonNull
    @Override
    public AdapterProduct.HolderProduct onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_item, parent, false);
        return new HolderProduct(view);
    }

    class HolderProduct extends RecyclerView.ViewHolder{
        // holds views of recView
        private TextView discNoteTV, titleTV, discPriceTV, priceTV, topishlishiTV, colorTV, isCheckTV;

        public HolderProduct(@NonNull View itemView) {
            super(itemView);

            discNoteTV = itemView.findViewById(R.id.discNoteTV);
            discPriceTV = itemView.findViewById(R.id.discPriceTV);
            priceTV = itemView.findViewById(R.id.priceTV);
            titleTV = itemView.findViewById(R.id.titleTV);
            colorTV = itemView.findViewById(R.id.colorTV);
            topishlishiTV = itemView.findViewById(R.id.topishlishiTV);
            isCheckTV = itemView.findViewById(R.id.isCheckTV);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterProduct.HolderProduct holder, int position) {

        auth = FirebaseAuth.getInstance();

        firebaseFirestore = FirebaseFirestore.getInstance();

        // get data
        progressDialog = new ProgressDialog(holder.itemView.getContext());
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        final ModelProduct modelProduct = productList.get(position);

        String title = modelProduct.getPrTitle();
        String price = modelProduct.getPrPrice();
        String oldPrice = modelProduct.getPrOldPrice();
        String discNote = modelProduct.getPrDiscNote();
        String prCost = modelProduct.getPrCost();
        String isAbbos = modelProduct.getIsAbbos();
        String isPodzakaz = modelProduct.getIsPodzakaz();
        String isCheck = modelProduct.getIsCheck();

        // if psCost equals null, set titleTV to red
        if (prCost!=null){
            if (prCost.length()==0) {
                holder.titleTV.setTextColor(Color.RED);
            } else {
                holder.titleTV.setTextColor(Color.BLACK);
            }
        } else {
            holder.titleTV.setTextColor(Color.RED);
        }

        holder.topishlishiTV.setVisibility(View.GONE);

        if (isAbbos != null){
            if (isAbbos.equals("true")){
                holder.topishlishiTV.setText("A");
                holder.topishlishiTV.setTextColor(Color.RED);
                holder.topishlishiTV.setVisibility(View.VISIBLE);
            }
        }
        if (isPodzakaz != null) {
            if (isPodzakaz.equals("true")) {
                holder.topishlishiTV.setText("Podzakaz");
                Toast.makeText(context, "podzakaz " + isPodzakaz, Toast.LENGTH_SHORT).show();
                holder.topishlishiTV.setTextColor(Color.RED);
                holder.topishlishiTV.setVisibility(View.VISIBLE);
            }
        }
        
        // set Data
        holder.titleTV.setText(title);

        if (oldPrice == null) {
            if (price!=null) {
                holder.priceTV.setText(String.format("$ %s", price));
            } else {
                holder.priceTV.setText(String.format("$ %s", "0"));
            }

            if (holder.priceTV.getText().toString().startsWith(")")){
                holder.priceTV.setTextColor(Color.RED);
            }
            holder.discPriceTV.setVisibility(View.GONE);
            holder.discNoteTV.setVisibility(View.GONE);
        } else {
            holder.priceTV.setText(String.format("$ %s", oldPrice));
            holder.priceTV.setPaintFlags(holder.priceTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); // add strike through
            holder.discPriceTV.setText(String.format("$ %s", price));
            if (discNote!=null) {
                holder.discNoteTV.setText(discNote);
            } else {
                holder.discNoteTV.setText(R.string.sale);
            }
        }

        // check if isCheck is null or empty
        if (isCheck != null && isCheck.equals("true")) {
            holder.isCheckTV.setVisibility(View.VISIBLE);
            holder.isCheckTV.setText("Tekshirildi");
            holder.isCheckTV.setTextColor(Color.parseColor("#43A047"));
        } else {
            holder.isCheckTV.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(view -> {
            detailsBottomSheet(modelProduct, sharedPreferences, position);
        });
    }

    private void detailsBottomSheet(ModelProduct modelProduct, SharedPreferences sharedPreferences, int position) {

        String sharedUserType = sharedPreferences.getString("user_type", "");

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        // inflate view for bottomSheet
        View view = LayoutInflater.from(context).inflate(R.layout.bs_pr_detail, null);
        // set view to bottomSheet
        bottomSheetDialog.setContentView(view);

        ImageButton backBtn = view.findViewById(R.id.backBtn);
        ImageButton delBtn = view.findViewById(R.id.delBtn);
        ImageButton editBtn = view.findViewById(R.id.editBtn);
        ImageButton addLenBtn = view.findViewById(R.id.addLenBtn);
        ImageButton discountBtn = view.findViewById(R.id.discountBtn);
        RecyclerView partRV = view.findViewById(R.id.partRV);
        ImageButton hideCostStatus = view.findViewById(R.id.hideCostStatus);
        ImageButton excelPrintBtn = view.findViewById(R.id.excelPrintBtn);

        TextView nameTV = view.findViewById(R.id.nameTV);
        TextView priceTV = view.findViewById(R.id.priceTV);
        TextView costTV = view.findViewById(R.id.costTV);
        TextView prHeightTV = view.findViewById(R.id.prHeightTV);
        TextView prMassTV = view.findViewById(R.id.prMassTV);
        TextView addTV = view.findViewById(R.id.addTV);
        TextView editTV = view.findViewById(R.id.editTV);
        TextView descTV = view.findViewById(R.id.descTV);
        TextView catTV = view.findViewById(R.id.catTV);
        TextView noPartsTxt = view.findViewById(R.id.NoPartsTxt);
        TextView cutPartsListTV = view.findViewById(R.id.cutPartsListTV);
        TextView colorTV = view.findViewById(R.id.colorTV);
        TextView companyTV = view.findViewById(R.id.companyTV);
        TextView checkTV = view.findViewById(R.id.checkTV);

        String price = modelProduct.getPrPrice();
        String cost = modelProduct.getPrCost();
        String title = modelProduct.getPrTitle();
        String createAt = modelProduct.getCreated_at();
        String createdBy = modelProduct.getCreated_by();
        String editBy = modelProduct.getPrEditBy();
        String editAt = modelProduct.getPrEditAt();
        String cat = modelProduct.getPrCat();
        String prHeight = modelProduct.getPrHeight();
        String prMass = modelProduct.getPrMass();
        String desc = modelProduct.getPrDesc();
        String prId = modelProduct.getPrId();
        String color = modelProduct.getPrColor();
        String company = modelProduct.getPrComp();
        String check = modelProduct.getIsCheck();

        partsList = new ArrayList<>();

        loadParts(prId, partRV, noPartsTxt);

        bottomSheetDialog.show();

        adapterPart = new AdapterPart(context, partsList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        partRV.setLayoutManager(linearLayoutManager);
        partRV.setAdapter(adapterPart);

        nameTV.setText(title);
        if (price!=null){
            if (price.length()==0) {
                priceTV.setVisibility(View.GONE);
            } else {
                priceTV.setText(String.format("%s $",price));
            }
        } else {
            priceTV.setVisibility(View.GONE);
        }

        if (check != null && check.equals("true")) {
            checkTV.setVisibility(View.VISIBLE);
            checkTV.setText("Tekshirildi");
            checkTV.setTextColor(Color.parseColor("#43A047"));
        } else {
            checkTV.setText("Tekshirilmagan");
            checkTV.setVisibility(View.VISIBLE);
        }

        costTV.setVisibility(View.GONE);
        companyTV.setVisibility(View.GONE);
        hideCostStatus.setVisibility(View.GONE);
        if (sharedUserType.equals("superAdmin")) {
            hideCostStatus.setVisibility(View.VISIBLE);
        }

        hideCostStatus.setOnClickListener(view3 -> {
            if (!click){
                costTV.setVisibility(View.VISIBLE);
                companyTV.setVisibility(View.VISIBLE);
                click = true;
            } else {
                costTV.setVisibility(View.GONE);
                companyTV.setVisibility(View.GONE);
                click = false;
            }
        });

        if (color!=null){
            if (color.length()==0) {
                colorTV.setVisibility(View.GONE);
            } else {
                colorTV.setText(String.format("Rang: %s", color));
            }
        } else {
            colorTV.setVisibility(View.GONE);
        }

        if (cost!=null){
            if (cost.length()==0) {
                costTV.setVisibility(View.GONE);
            } else {
                if (sharedUserType.equals("superAdmin")) {
                    costTV.setText(String.format("Tannarxi - %s $", cost));
                } else {
                    costTV.setVisibility(View.GONE);
                }
            }
        } else {
            costTV.setVisibility(View.GONE);
        }

        catTV.setText(cat);

        if (company!=null){
//            if (company.length()==0) {
//                companyTV.setVisibility(View.GONE);
//            } else {
                companyTV.setText(String.format("Firma: %s", company));
//            }
//        } else {
//            companyTV.setVisibility(View.GONE);
        }

        if (prHeight!=null){
            if (prHeight.length()==0) {
                prHeightTV.setVisibility(View.GONE);
            } else {
                prHeightTV.setText(String.format("Eni %s", prHeight));
            }
        } else {
            prHeightTV.setVisibility(View.GONE);
        }
// 1 grga massagi
        if (prMass!=null){
            if (prMass.length()==0) {
                prMassTV.setVisibility(View.GONE);
            } else {
                prMassTV.setText(String.format("%s gr/%s", prMass, Html.fromHtml("m<sup>2</sup")));
            }
        } else {
            prMassTV.setVisibility(View.GONE);
        }

        if (desc!=null){
            if (desc.length()==0) {
                descTV.setVisibility(View.GONE);
            } else {
                descTV.setText(desc);
            }
        } else {
            descTV.setVisibility(View.GONE);
        }

        if (sharedUserType.equals("bichuvchi") || sharedUserType.equals("dizayner") || sharedUserType.equals("viewer")){
            editBtn.setVisibility(View.GONE);
            delBtn.setVisibility(View.GONE);
            addLenBtn.setVisibility(View.GONE);
            discountBtn.setVisibility(View.GONE);

            addTV.setVisibility(View.GONE);
            editTV.setVisibility(View.GONE);
        } else {
            addTV.setText(String.format("%s da %s qo'shdi", createAt, createdBy));
            if (editAt!=null){
                editTV.setText(String.format("%s da %s o'zgartirdi", editAt, editBy));
            } else {
                editTV.setVisibility(View.GONE);
            }
        }

        if (sharedUserType.equals("sklad")){
            discountBtn.setVisibility(View.GONE);
        }

        cutPartsListTV.setOnClickListener(view14 -> {
            Intent intent = new Intent(context, CutPartsListActivity.class);
            intent.putExtra("prId", prId);
            context.startActivity(intent);
        });

        backBtn.setOnClickListener(view2 -> bottomSheetDialog.dismiss());

        if (!sharedUserType.equals("superAdmin")){
            excelPrintBtn.setVisibility(View.GONE);
        }

        excelPrintBtn.setOnClickListener(view4 -> {
            progressDialog.setMessage("Faylga saqlanmoqda");
            progressDialog.show();
            downloadExcel(prId);
        });

        editBtn.setOnClickListener(view1 -> {
            bottomSheetDialog.dismiss();
            // open edit pr activity
            Intent intent = new Intent(context, EditProduct.class);
            intent.putExtra("prId", prId);
            context.startActivity(intent);
        });

        addLenBtn.setOnClickListener(view12 -> {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context.getApplicationContext());
            View dialogView = inflater.inflate(R.layout.dialog_add_len, null);
            alertDialog.setTitle("Spinner");

            Spinner spinner = dialogView.findViewById(R.id.locSpinner);
            Spinner measSpinner = dialogView.findViewById(R.id.measSpinner);
            EditText dialAlenET = dialogView.findViewById(R.id.dialAlenET);
            EditText dialDescET = dialogView.findViewById(R.id.dialDescET);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                    android.R.layout.simple_spinner_item, Constants.measurement);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            measSpinner.setAdapter(adapter);
            measSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view12, int i, long l) {
                        measurement = measSpinner.getSelectedItem().toString().trim();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    Toast.makeText(context, "Hech narsa tanlanmadi", Toast.LENGTH_SHORT).show();
                }
            });

            ArrayAdapter<String> adapter1 = new ArrayAdapter<>(context,
                    android.R.layout.simple_spinner_item, Constants.location1);
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter1);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view12, int i, long l) {
                    if (!spinner.getSelectedItem().toString().trim().equalsIgnoreCase("Tanlang:")){
                        location = spinner.getSelectedItem().toString().trim();
                    } else {
                        TextView errTxt = (TextView) spinner.getSelectedView();
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
                        String timestamps = "" + System.currentTimeMillis();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("partId", timestamps);
                        hashMap.put("prTitle", title);
                        hashMap.put("prId", prId);
                        hashMap.put("partLen", ""+dialAlenET.getText().toString().trim());
                        hashMap.put("descPart", ""+dialDescET.getText().toString().trim());
                        hashMap.put("partMeas", measurement);
                        hashMap.put("partLoc", location);

                        bottomSheetDialog.show();

                        if (!TextUtils.isEmpty(dialAlenET.getText()) &&
                            !spinner.getSelectedItem().toString().trim().equalsIgnoreCase("Tanlang:")) {
                            firebaseFirestore.collection("Parts").document(timestamps).set(hashMap).addOnCompleteListener(task -> {
                                progressDialog.dismiss();
                                if (task.isSuccessful()){
                                    Toast.makeText(context, "Qo'shildi", Toast.LENGTH_SHORT).show();
                                    loadParts(prId, partRV, noPartsTxt);
                                } else {
                                    Toast.makeText(context, "Qo'shishda muammo " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(context, "Saqlanmadi", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());

            alertDialog.setView(dialogView);
            AlertDialog dialog = alertDialog.create();
            dialog.show();
        });

        delBtn.setOnClickListener(v -> {
            bottomSheetDialog.show();

            // show del confirm dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("O'chirish").setMessage("Rostdan ham o'chirmoqchimisiz??")
                    .setPositiveButton("O'chirish", (dialog, which) -> {
                        // delete
                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                        DocumentReference productRef = firestore.collection("Products").document(prId);
                        productRef.get().addOnCompleteListener(task -> {
                            bottomSheetDialog.dismiss();
                            if (task.isSuccessful() && task.getResult().exists()){
                                String deletedProductId;
                                if (task.getResult().contains("prId")){
                                    deletedProductId = task.getResult().getString("prId");
                                } else {
                                    deletedProductId = null;
                                }
                                productRef.delete().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()){
                                        productList.remove(position);
                                        notifyItemRemoved(position); // UI ni yangilash
                                        notifyItemRangeChanged(position, productList.size());

                                        Toast.makeText(context, "Mahsulot o'chirildi", Toast.LENGTH_SHORT).show();
                                        if (deletedProductId!=null){
                                            deleteItemsByTitle(deletedProductId);
                                        }
                                    } else {
                                        Toast.makeText(context, "Mahsulot o'chirishda xato", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(context, "Mahsulot topilmadi", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();
        });

        discountBtn.setOnClickListener(view13 -> {
            bottomSheetDialog.show();

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.add_disc_txt);
            LayoutInflater inflater = LayoutInflater.from(context.getApplicationContext());
            View dialogView = inflater.inflate(R.layout.dialog_add_discount, null);
            builder.setView(dialogView)
                    .setPositiveButton(R.string.save_me, (dialogInterface, i) -> {

                        EditText dialDiscPriceET = dialogView.findViewById(R.id.dialDiscPriceET);
                        EditText dialDiscNoteET = dialogView.findViewById(R.id.dialDiscNoteET);
                        String dialDiscPrice = dialDiscPriceET.getText().toString();
                        String dialDiscNote = dialDiscNoteET.getText().toString();

                        String prOldPrice = modelProduct.getPrPrice();
                        String editAT = "" + System.currentTimeMillis();
                        if (dialDiscPrice.length()>0) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("prPrice", dialDiscPrice);
                            hashMap.put("prOldPrice", prOldPrice);
                            if (!dialDiscNote.isEmpty()) {
                                hashMap.put("prDiscNote", dialDiscNote);
                            }

                            Date prDate = new Date(Long.parseLong(editAT));
                            SimpleDateFormat sdfFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                            String edit_at = sdfFormat.format(prDate);
                            hashMap.put("prEditAt", edit_at);
                            if (Objects.requireNonNull(auth.getCurrentUser()).getDisplayName() != null) {
                                hashMap.put("prEditBy", auth.getCurrentUser().getDisplayName());
                            } else {
                                hashMap.put("prEditBy", auth.getCurrentUser().getDisplayName());
                            }

                            DocumentReference productRef = firebaseFirestore.collection("Products").document(prId);
                            productRef.update(hashMap).addOnSuccessListener(unused -> {
                                progressDialog.dismiss();
                                Toast.makeText(context, R.string.successful, Toast.LENGTH_SHORT).show();
                            }).addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(context, R.string.sm_error + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            Toast.makeText(context, "narxni to'g'ri kiriting", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss()).show();
        });

        if (sharedUserType.equals("superAdmin") || sharedUserType.equals("admin")) {
            checkTV.setOnClickListener(view5 -> {
                progressDialog.show();
                // toggle isCheck status

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Tekshirish").setMessage("Rostdan tekshirildimi??")
                        .setPositiveButton("Tekshirish", (dialog, which) -> {
                            String newIsCheckStatus = modelProduct.getIsCheck() != null && modelProduct.getIsCheck().equals("true") ? "false" : "true";
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("isCheck", newIsCheckStatus);

                            DocumentReference productRef = firebaseFirestore.collection("Products").document(prId);
                            productRef.update(hashMap).addOnSuccessListener(unused -> {
                                progressDialog.dismiss();
                                bottomSheetDialog.dismiss();
                                Toast.makeText(context, "Tekshirildi holati o'zgartirildi", Toast.LENGTH_SHORT).show();
                                modelProduct.setIsCheck(newIsCheckStatus);
                                notifyItemChanged(position); // UI ni yangilash
                            }).addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(context, "Tekshirildi holatini o'zgartirishda xatolik: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();
            });
        }
    }
    /**
     * Download product data as an Excel file.
     * @param prId Product ID to filter the data.
     */
    private void downloadExcel(String prId) {
    progressDialog.show();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    db.collection("Products")
            .whereEqualTo("prId", prId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                if (documents == null || documents.isEmpty()) {
                    showError("Excel saqlashda ma'lumot topilmadi");
                } else {
                    // Fayl nomi uchun prTitle ni birinchi documentdan olamiz
                    String prTitle = getString(documents.get(0), "prTitle");
                    createExcelFile(documents, prTitle);
                }
            })
            .addOnFailureListener(e -> showError("Excel saqlashda xatolik: " + (e != null ? e.getMessage() : "Noma'lum xatolik")));
    }

    private void showError(String message) {
        progressDialog.dismiss();
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    private String getString(DocumentSnapshot doc, String key) {
        Object value = doc.get(key);
        return value != null ? value.toString() : "";
    }

    private void createExcelFile(List<DocumentSnapshot> documents, String prTitle) {
        Workbook workbook = new XSSFWorkbook();
        FileOutputStream fileOut = null;
        try {
            // Sheet va sarlavha
            Sheet sheet = workbook.createSheet("Product");
            String[] headers = {"Nomi", "Barcode", "Narxi", "Eni"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // Ma'lumotlarni yozish
            int rowNum = 1;
            for (DocumentSnapshot doc : documents) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(getString(doc, "prTitle"));
                row.createCell(1).setCellValue(getString(doc, "prBarcode"));
                row.createCell(2).setCellValue("100" + getString(doc, "prPrice"));
                row.createCell(3).setCellValue(getString(doc, "prHeight"));
            }

            // Fayl nomini tozalash (harflar va raqamlar, bo'sh joylarni _ bilan almashtiramiz)
            String safeTitle = (prTitle != null && !prTitle.trim().isEmpty()) ? prTitle.trim().replaceAll("[^a-zA-Z0-9]", "_") : "unknown";
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Smetalar");
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IOException("Fayl papkasini yaratib bo'lmadi: " + dir.getAbsolutePath());
            }
            File file = new File(dir, "product_" + safeTitle + ".xlsx");
            fileOut = new FileOutputStream(file, false); // overwrite
            workbook.write(fileOut);

            String visiblePath;
            if (file.getAbsolutePath().startsWith(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                visiblePath = file.getAbsolutePath().substring(Environment.getExternalStorageDirectory().getAbsolutePath().length());
            } else {
                visiblePath = file.getAbsolutePath();
            }
            Toast.makeText(context, "Excel fayl saqlandi: " + visiblePath, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            showError("Excel faylni saqlashda xatolik: " + (e.getMessage() != null ? e.getMessage() : "Noma'lum xatolik"));
        } finally {
            try {
                if (fileOut != null) fileOut.close();
            } catch (IOException ignored) {}
            try {
                workbook.close();
            } catch (IOException ignored) {}
            progressDialog.dismiss();
        }
    }


    private void loadParts(String prId, RecyclerView partRV, TextView noPartsTxt) {
        progressDialog.setMessage("Yuklanmoqda...");
        progressDialog.show();

        firebaseFirestore.collection("Parts")
                .whereEqualTo("prId", prId)
                .get()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();

                    if (task.isSuccessful()) {
                        ArrayList<ModelPart> partsList = new ArrayList<>();
                        for (DocumentSnapshot snapshot : task.getResult()) {
                            ModelPart modelPart = snapshot.toObject(ModelPart.class);
                            if (modelPart != null) {
                                partsList.add(modelPart);
                            }
                        }

                        if (partsList.isEmpty()) {
                            noPartsTxt.setVisibility(View.VISIBLE);
                            partRV.setVisibility(View.GONE);
                        } else {
                            noPartsTxt.setVisibility(View.GONE);
                            partRV.setVisibility(View.VISIBLE);

                            if (adapterPart == null) {
                                // Adapter yaratilmagan bo'lsa, yangi yaratish
                                adapterPart = new AdapterPart(context, partsList);
                                partRV.setAdapter(adapterPart);
                            } else {
                                // Adapter allaqachon yaratilgan bo'lsa, ma'lumotlarni yangilash
                                adapterPart.setPartsList(partsList);
                                adapterPart.notifyDataSetChanged(); // UI ni yangilash
                            }
                        }
                    } else {
                        Toast.makeText(context, "Xatolik: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteItemsByTitle(String deletedProductId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference partsPref = firestore.collection("Parts");
        partsPref.whereEqualTo("prId", deletedProductId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                progressDialog.dismiss();
                for (DocumentSnapshot document : task.getResult()){
                    document.getReference().delete();
                }
            } else {
                progressDialog.dismiss();
                Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}
