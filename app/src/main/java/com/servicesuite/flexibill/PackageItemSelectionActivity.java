package com.servicesuite.flexibill;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PackageItemSelectionActivity extends AppCompatActivity {

    private static final String TAG = "PackageItemSelection";

    private LinearLayout llCategories;
    private Button btnSaveItems;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String packageId;
    private Map<String, Integer> itemQuantities;
    private Map<String, List<Spinner>> categorySpinners = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_item_selection);

        llCategories = findViewById(R.id.llCategories);
        btnSaveItems = findViewById(R.id.btnSaveItems);

        packageId = getIntent().getStringExtra("PACKAGE_ID");
        itemQuantities = (Map<String, Integer>) getIntent().getSerializableExtra("quantityMap");

        if (packageId != null && itemQuantities != null) {
            Log.d(TAG, "Received packageId: " + packageId);
            Log.d(TAG, "Received itemQuantities: " + itemQuantities);

            for (Map.Entry<String, Integer> entry : itemQuantities.entrySet()) {
                String category = entry.getKey();
                Integer quantity = entry.getValue();
                Log.d(TAG, "Category: " + category + ", Quantity: " + quantity);
            }

            setupCategoryViews();
        } else {
            Log.e(TAG, "packageId or itemQuantities is null");
        }

        btnSaveItems.setOnClickListener(v -> saveItems());
    }

    private void setupCategoryViews() {
        llCategories.removeAllViews();
        categorySpinners.clear();

        // Loop through the itemQuantities map and create views
        for (Map.Entry<String, Integer> entry : itemQuantities.entrySet()) {
            String category = entry.getKey();
            int quantity = entry.getValue();
            addCategoryView(category, quantity);
        }
    }

    private void addCategoryView(String category, int quantity) {
        View categoryView = LayoutInflater.from(this).inflate(R.layout.category_view, llCategories, false);
        TextView tvCategory = categoryView.findViewById(R.id.tvCategory);
        LinearLayout llSpinners = categoryView.findViewById(R.id.llSpinners);

        tvCategory.setText(category);

        List<Spinner> spinners = new ArrayList<>();
        categorySpinners.put(category, spinners);

        // Fetch items for the category
        fetchItemsForCategory(category, spinners, quantity, llSpinners);

        llCategories.addView(categoryView);
    }

    private void fetchItemsForCategory(String category, List<Spinner> spinners, int quantity, LinearLayout llSpinners) {
        db.collection("menuItems")
                .whereEqualTo("category", category)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> itemNames = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String itemName = document.getString("name");
                            if (itemName != null) {
                                itemNames.add(itemName);
                            }
                        }
                        if (itemNames.isEmpty()) {
                            Log.d(TAG, "No items found for category: " + category);
                        }

                        // Set up spinners
                        for (int i = 0; i < quantity; i++) {
                            Spinner spinner = new Spinner(this);
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                    android.R.layout.simple_spinner_item, itemNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(adapter);
                            llSpinners.addView(spinner);
                            spinners.add(spinner);
                        }
                    } else {
                        Log.w(TAG, "Error getting items for category: " + category, task.getException());
                    }
                });
    }

    private void saveItems() {
        Map<String, List<String>> selectedItems = new HashMap<>();

        for (Map.Entry<String, List<Spinner>> entry : categorySpinners.entrySet()) {
            String category = entry.getKey();
            List<Spinner> spinners = entry.getValue();
            List<String> items = new ArrayList<>();

            for (Spinner spinner : spinners) {
                String selectedItem = (String) spinner.getSelectedItem();
                if (selectedItem != null) {
                    items.add(selectedItem);
                }
            }

            selectedItems.put(category, items);
        }

        // Save selected items to database or handle them as needed
        db.collection("packages").document(packageId).update("selectedItems", selectedItems)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PackageItemSelectionActivity.this, "Items saved.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(PackageItemSelectionActivity.this, "Error saving items.", Toast.LENGTH_SHORT).show());
    }
}
