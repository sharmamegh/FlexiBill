package com.servicesuite.flexibill;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.checkerframework.common.value.qual.IntRangeFromGTENegativeOne;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PackageSelectionActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout packageContainer;
    private int people;
    private String location, date, time;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_selection);

        packageContainer = findViewById(R.id.package_container);
        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        people = intent.getIntExtra("people",0);
        location = intent.getStringExtra("locationName");
        date = intent.getStringExtra("date");
        time = intent.getStringExtra("time");
        // Fetch packages from Firestore
        fetchPackages();
    }

    private void fetchPackages() {
        CollectionReference packagesRef = db.collection("packages");
        packagesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String packageName = document.getString("name");
                    String packageId = document.getId();

                    // Dynamically create buttons for each package
                    Button packageButton = new Button(this);
                    packageButton.setText(packageName);
                    packageButton.setOnClickListener(v -> navigateToPackageItemSelection(packageId));

                    // Add the button to the layout
                    packageContainer.addView(packageButton);
                }
            } else {
                Log.w("PackageSelection", "Error getting packages.", task.getException());
            }
        });
    }

    private void navigateToPackageItemSelection(String packageId) {
        // Fetch item quantities for the selected package
        db.collection("packages").document(packageId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = (DocumentSnapshot) task.getResult();

                        if (document != null) {
                            // Retrieve the items map from the document
                            Map<String, Object> itemsMap = (Map<String, Object>) document.get("items");

                            // Prepare the itemQuantities map
                            Map<String, Integer> itemQuantities = new HashMap<>();

                            // Iterate through the items map and populate itemQuantities
                            if (itemsMap != null) {
                                for (Map.Entry<String, Object> entry : itemsMap.entrySet()) {
                                    String category = entry.getKey();
                                    Long quantityLong = (Long) entry.getValue();
                                    int quantity = quantityLong != null ? quantityLong.intValue() : 0;
                                    itemQuantities.put(category, quantity);
                                }
                            }

                            // Pass packageId and itemQuantities to the PackageItemSelectionActivity
                            Intent intent = new Intent(this, PackageItemSelectionActivity.class);
                            intent.putExtra("PACKAGE_ID", packageId);

                            intent.putExtra("location", location);
                            intent.putExtra("date", date);
                            intent.putExtra("time", time);
                            intent.putExtra("people", people);

                            intent.putExtra("quantityMap", (Serializable) itemQuantities);
                            startActivity(intent);
                        } else {
                            Log.w("PackageSelection", "Document does not exist.");
                        }
                    } else {
                        Log.w("PackageSelection", "Error getting item quantities.", task.getException());
                    }
                });
    }


}



//package com.servicesuite.flexibill;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.firestore.CollectionReference;
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.QuerySnapshot;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class PackageSelectionActivity extends AppCompatActivity {
//
//    private RecyclerView recyclerView;
//    private Button proceedButton;
//    private PackageAdapter packageAdapter;
//    private List<Map<String, Object>> packagesList = new ArrayList<>();
//    private FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_package_selection);
//
//        recyclerView = findViewById(R.id.recycler_view);
//        proceedButton = findViewById(R.id.proceed_button);
//
//        packageAdapter = new PackageAdapter(packagesList, this);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(packageAdapter);
//
//        loadPackages();
//
//        proceedButton.setOnClickListener(v -> {
//            int selectedPosition = packageAdapter.getSelectedPosition();
//            if (selectedPosition != -1) {
//                Map<String, Object> selectedPackage = packagesList.get(selectedPosition);
//                Intent intent = new Intent(PackageSelectionActivity.this, EditPackageActivity.class);
//                intent.putExtra("selectedPackage", (HashMap<String, Object>) selectedPackage);
//                startActivity(intent);
//            }
//        });
//    }
//
//    private void loadPackages() {
//        CollectionReference packagesRef = db.collection("Packages");
//        packagesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()) {
//                    for (DocumentSnapshot document : task.getResult()) {
//                        packagesList.add(document.getData());
//                    }
//                    packageAdapter.notifyDataSetChanged();
//                } else {
//                    Log.w("FirestoreError", "Error getting documents.", task.getException());
//                }
//            }
//        });
//    }
//}
