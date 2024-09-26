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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
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
    private int people;
    private String location, date, time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_item_selection);

        llCategories = findViewById(R.id.llCategories);
        btnSaveItems = findViewById(R.id.btnSaveItems);

        packageId = getIntent().getStringExtra("PACKAGE_ID");
        itemQuantities = (Map<String, Integer>) getIntent().getSerializableExtra("quantityMap");

        Intent intent = getIntent();
        people = intent.getIntExtra("people",0);
        location = intent.getStringExtra("location");
        date = intent.getStringExtra("date");
        time = intent.getStringExtra("time");

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

                        // If the category has no items, remove the category view and skip adding spinners
                        if (itemNames.isEmpty()) {
                            Log.d(TAG, "No items found for category: " + category);
                            // Cast the ViewParent to View and remove it from the parent layout
                            View categoryView = (View) llSpinners.getParent(); // Get the parent view of llSpinners
                            llCategories.removeView(categoryView); // Remove the whole category view
                            return; // Skip adding this category
                        }

                        // Set up spinners only if there are items
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

        // Loop through the spinners and collect the selected items for each category
        for (Map.Entry<String, List<Spinner>> entry : categorySpinners.entrySet()) {
            String category = entry.getKey();
            List<Spinner> spinners = entry.getValue();
            List<String> items = new ArrayList<>();

            for (Spinner spinner : spinners) {
                String selectedItem = (String) spinner.getSelectedItem();
                if (selectedItem != null && !selectedItem.isEmpty()) {  // Check if a valid item is selected
                    items.add(selectedItem);
                }
            }

            if (!items.isEmpty()) {
                selectedItems.put(category, items);  // Only add if there are selected items
            }
        }

        // Fetch package details from Firestore based on packageId
        db.collection("packages").document(packageId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        String packageName = document.getString("name");
                        Long priceLong = document.getLong("price");

                        if (packageName != null && priceLong != null) { // Ensure name and price exist
                            int packagePrice = priceLong.intValue();

                            // Prepare booking details to pass to BookingConfirmationActivity
                            Map<String, Object> bookingDetails = new HashMap<>();
                            bookingDetails.put("packageId", packageId);
                            bookingDetails.put("selectedItems", selectedItems);
                            bookingDetails.put("location", location);
                            bookingDetails.put("date", date);
                            bookingDetails.put("time", time);
                            bookingDetails.put("numberOfPeople", people);
                            bookingDetails.put("packageName", packageName);
                            bookingDetails.put("packagePrice", packagePrice);

                            // Create Intent for BookingConfirmationActivity
                            Intent intent = new Intent(PackageItemSelectionActivity.this, BookingConfirmationActivity.class);
                            intent.putExtra("bookingDetails", (Serializable) bookingDetails);
                            startActivity(intent);
                        } else {
                            Log.e(TAG, "Package name or price is missing.");
                            Toast.makeText(PackageItemSelectionActivity.this, "Error fetching package details.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Failed to fetch package details", task.getException());
                        Toast.makeText(PackageItemSelectionActivity.this, "Error fetching package details.", Toast.LENGTH_SHORT).show();
                    }
                });
    }



}
