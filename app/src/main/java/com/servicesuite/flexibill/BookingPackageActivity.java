package com.servicesuite.flexibill;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingPackageActivity extends AppCompatActivity {

    private LinearLayout llCategories;
    private Button btnSaveItems;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Map<String, List<Spinner>> categorySpinners = new HashMap<>();
    private String packageId;
    private Map<String, Integer> itemQuantities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_package);

        llCategories = findViewById(R.id.llCategories);
        btnSaveItems = findViewById(R.id.btnSaveItems);

        packageId = getIntent().getStringExtra("packageId");
        itemQuantities = (Map<String, Integer>) getIntent().getSerializableExtra("quantityMap");

        if (packageId != null && itemQuantities != null) {
            setupCategoryViews();
        }

        btnSaveItems.setOnClickListener(v -> saveItems());
    }

    private void setupCategoryViews() {
        llCategories.removeAllViews();
        categorySpinners.clear();

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

        for (int i = 0; i < quantity; i++) {
            Spinner spinner = new Spinner(this);
            // Configure the spinner with items, e.g., drinks or other options
            // You should fetch the items from the database or a static list
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, new ArrayList<>()); // replace with actual items
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            llSpinners.addView(spinner);
            spinners.add(spinner);
        }

        llCategories.addView(categoryView);
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
        // Example:
        db.collection("packages").document(packageId).update("items", selectedItems)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(BookingPackageActivity.this, "Items saved.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(BookingPackageActivity.this, "Error saving items.", Toast.LENGTH_SHORT).show());
    }
}
