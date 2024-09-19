package com.servicesuite.flexibill;

import android.app.AlertDialog;
import android.icu.util.ULocale;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ManageMenuActivity.java
// ManageMenuActivity.java
public class ManageMenuActivity extends AppCompatActivity {

    private Spinner categorySpinner;
    private EditText itemNameEditText;
    private Button addItemButton, addCategoryButton;
    private RecyclerView menuRecyclerView;
    private CustomMenuAdapter menuAdapter;
    private FirebaseFirestore db;
    private List<String> categories = new ArrayList<>();
    private Map<String, List<MenuItem>> categoryItemsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_menu);

        categorySpinner = findViewById(R.id.categorySpinner);
        itemNameEditText = findViewById(R.id.itemNameEditText);
        addItemButton = findViewById(R.id.addItemButton);
        addCategoryButton = findViewById(R.id.addCategoryButton);
        menuRecyclerView = findViewById(R.id.menuRecyclerView);

        db = FirebaseFirestore.getInstance();
        menuAdapter = new CustomMenuAdapter(categoryItemsMap);
        menuRecyclerView.setAdapter(menuAdapter);
        menuRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load categories and items from Firestore
        loadCategoriesAndItems();

        // Add category
        addCategoryButton.setOnClickListener(v -> showAddCategoryDialog());

        // Add item to selected category
        addItemButton.setOnClickListener(v -> addItemToCategory());
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        EditText categoryNameEditText = view.findViewById(R.id.etCategoryName);
        builder.setView(view)
                .setPositiveButton("Add", (dialog, which) -> {
                    String categoryName = categoryNameEditText.getText().toString().trim();
                    if (!categoryName.isEmpty()) {
                        addCategory(categoryName);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addCategory(String categoryName) {
        db.collection("categories").add(new HashMap<String, Object>() {{
            put("name", categoryName);
        }}).addOnSuccessListener(documentReference -> {
            categories.add(categoryName);
            updateCategorySpinner();
        });
    }

    private void addItemToCategory() {
        String selectedCategory = categorySpinner.getSelectedItem().toString();
        String itemName = itemNameEditText.getText().toString().trim();
        if (!itemName.isEmpty()) {
            MenuItem menuItem = new MenuItem(itemName, selectedCategory);
            db.collection("menuItems").add(menuItem).addOnSuccessListener(documentReference -> {
                if (!categoryItemsMap.containsKey(selectedCategory)) {
                    categoryItemsMap.put(selectedCategory, new ArrayList<>());
                }
                categoryItemsMap.get(selectedCategory).add(menuItem);
                menuAdapter.notifyDataSetChanged();
            });
        }
    }

    private void loadCategoriesAndItems() {
        // Load categories from Firestore
        db.collection("categories").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String categoryName = document.getString("name");
                    categories.add(categoryName);
                }
                updateCategorySpinner();
            }
        });

        // Load items for each category
        db.collection("menuItems").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    MenuItem menuItem = document.toObject(MenuItem.class);
                    String category = menuItem.getCategory();
                    if (!categoryItemsMap.containsKey(category)) {
                        categoryItemsMap.put(category, new ArrayList<>());
                    }
                    categoryItemsMap.get(category).add(menuItem);
                }
                menuAdapter.notifyDataSetChanged();
            }
        });
    }

    private void updateCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }
}
