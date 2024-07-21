package com.servicesuite.flexibill;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditPackageActivity extends AppCompatActivity {

    private LinearLayout itemsContainer;
    private Button addItemButton;
    private Button bookButton;
    private Map<String, Object> selectedPackage;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_package);

        itemsContainer = findViewById(R.id.items_container);
        addItemButton = findViewById(R.id.add_item_button);
        bookButton = findViewById(R.id.book_button);

        selectedPackage = (Map<String, Object>) getIntent().getSerializableExtra("selectedPackage");

        displayPackageItems();

        addItemButton.setOnClickListener(v -> addItemView());

        bookButton.setOnClickListener(v -> {
            Map<String, Object> editedPackage = new HashMap<>();

            editedPackage.put("name", selectedPackage.get("name"));
            editedPackage.put("price", selectedPackage.get("price"));

            for (int i = 0; i < itemsContainer.getChildCount(); i++) {
                View itemView = itemsContainer.getChildAt(i);
                EditText itemName = itemView.findViewById(R.id.item_name);
                EditText itemQuantity = itemView.findViewById(R.id.item_quantity);

                String name = itemName.getText().toString();
                int quantity = Integer.parseInt(itemQuantity.getText().toString());

                editedPackage.put(name, quantity);
            }

            db.collection("Bookings").add(editedPackage)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(EditPackageActivity.this, BookingConfirmationActivity.class);
                            intent.putExtra("bookingDetails", (HashMap<String, Object>) editedPackage);
                            startActivity(intent);
                        } else {
                            Log.w("FirestoreError", "Error adding booking.", task.getException());
                        }
                    });
        });

    }

    private void displayPackageItems() {
        for (Map.Entry<String, Object> entry : selectedPackage.entrySet()) {
            if (!entry.getKey().equals("name") && !entry.getKey().equals("price")) {
                addItemView(entry.getKey(), entry.getValue().toString());
            }
        }
    }

    private void addItemView() {
        addItemView("", "");
    }

    private void addItemView(String name, String quantity) {
        View itemView = getLayoutInflater().inflate(R.layout.item_edit, itemsContainer, false);

        EditText itemName = itemView.findViewById(R.id.item_name);
        EditText itemQuantity = itemView.findViewById(R.id.item_quantity);
        Button removeButton = itemView.findViewById(R.id.remove_button);

        itemName.setText(name);
        itemQuantity.setText(quantity);

        removeButton.setOnClickListener(v -> itemsContainer.removeView(itemView));

        itemsContainer.addView(itemView);
    }

}
