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
