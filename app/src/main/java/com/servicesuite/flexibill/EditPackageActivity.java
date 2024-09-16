package com.servicesuite.flexibill;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditPackageActivity extends AppCompatActivity implements PackageItemAdapter.OnItemInteractionListener {

    private EditText etPackageName, etPrice;
    private RecyclerView rvPackageItems;
    private Button btnAddItem, btnSavePackage;
    private PackageItemAdapter packageItemAdapter;
//    private TextView tvNoItems;
    private Map<String, Integer> items = new HashMap<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String packageId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_package);

        etPackageName = findViewById(R.id.etPackageName);
        etPrice = findViewById(R.id.etPrice);
        rvPackageItems = findViewById(R.id.rvPackageItems);
        btnAddItem = findViewById(R.id.btnAddItem);
        btnSavePackage = findViewById(R.id.btnSavePackage);
//        tvNoItems = findViewById(R.id.tvNoItems);

        packageItemAdapter = new PackageItemAdapter(items, this, this);
        rvPackageItems.setLayoutManager(new LinearLayoutManager(this));
        rvPackageItems.setAdapter(packageItemAdapter);

        // Check if we're editing an existing package
        packageId = getIntent().getStringExtra("packageId");
        if (packageId != null) {
            loadPackage(packageId);
        }

        btnAddItem.setOnClickListener(v -> showAddItemDialog());

        btnSavePackage.setOnClickListener(v -> savePackage());
    }

    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Item");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null);
        EditText etItemName = view.findViewById(R.id.etItemName);
        EditText etQuantity = view.findViewById(R.id.etQuantity);

        builder.setView(view);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String itemName = etItemName.getText().toString().trim();
            String quantityStr = etQuantity.getText().toString().trim();

            if (TextUtils.isEmpty(itemName) || TextUtils.isEmpty(quantityStr)) {
                Toast.makeText(EditPackageActivity.this, "Please enter item name and quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(quantityStr);
            } catch (NumberFormatException e) {
                Toast.makeText(EditPackageActivity.this, "Invalid quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            items.put(itemName, quantity);
            packageItemAdapter.notifyDataSetChanged();
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void loadPackage(String id) {
        db.collection("packages").document(id).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Package pkg = task.getResult().toObject(Package.class);
                        if (pkg != null) {
                            etPackageName.setText(pkg.getName());
                            etPrice.setText(String.valueOf(pkg.getPrice()));
                            items.clear();
                            items.putAll(pkg.getItems());
                            packageItemAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(EditPackageActivity.this, "Error loading package.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public void savePackage() {
        String name = etPackageName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr)) {
            Toast.makeText(EditPackageActivity.this, "Please enter package name and price", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(EditPackageActivity.this, "Invalid price", Toast.LENGTH_SHORT).show();
            return;
        }

        Package pkg = new Package();
        pkg.setName(name);
        pkg.setItems(items);
        pkg.setPrice(price);

        if (packageId != null) {
            // Update existing package
            db.collection("packages").document(packageId).set(pkg)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EditPackageActivity.this, "Package updated.", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(EditPackageActivity.this, "Error updating package.", Toast.LENGTH_SHORT).show());
        } else {
            // Create new package
            db.collection("packages").add(pkg)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(EditPackageActivity.this, "Package saved.", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(EditPackageActivity.this, "Error saving package.", Toast.LENGTH_SHORT).show());
        }
    }


    @Override
    public void onItemUpdated(String itemName, int quantity) {
        // Update the item in the map
        items.put(itemName, quantity);
        packageItemAdapter.notifyDataSetChanged();
    }


    @Override
    public void onDeleteItem(String itemName) {
        // Remove the item from the map
        items.remove(itemName);
        packageItemAdapter.notifyDataSetChanged();
    }

}
