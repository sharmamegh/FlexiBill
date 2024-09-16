package com.servicesuite.flexibill;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManagePackageActivity extends AppCompatActivity implements PackageAdapter.OnPackageClickListener {

    private static final int EDIT_PACKAGE_REQUEST_CODE = 1;
    private RecyclerView rvPackages;
    private PackageAdapter packageAdapter;
    private List<Package> packageList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_package);

        rvPackages = findViewById(R.id.rvPackages);
        Button btnAddPackage = findViewById(R.id.btnAddPackage);

        rvPackages.setLayoutManager(new LinearLayoutManager(this));
        packageAdapter = new PackageAdapter(packageList, this);
        rvPackages.setAdapter(packageAdapter);

        // Load packages from Firestore
        loadPackages();

        btnAddPackage.setOnClickListener(v -> {
            Intent intent = new Intent(ManagePackageActivity.this, EditPackageActivity.class);
            startActivity(intent);
        });
    }

    private void loadPackages() {
        db.collection("packages")
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(ManagePackageActivity.this, "Error getting packages.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    packageList.clear();
                    if (snapshot != null) {
                        for (DocumentSnapshot document : snapshot.getDocuments()) {
                            // Ensure you're using QueryDocumentSnapshot here
                            if (document instanceof QueryDocumentSnapshot) {
                                Package pkg = document.toObject(Package.class);
                                pkg.setId(document.getId());
                                packageList.add(pkg);
                            }
                        }
                        packageAdapter.notifyDataSetChanged();
                    }
                });
    }



    @Override
    public void onEditClick(Package pkg) {
        Intent intent = new Intent(ManagePackageActivity.this, EditPackageActivity.class);
        intent.putExtra("packageId", pkg.getId());
        startActivityForResult(intent, EDIT_PACKAGE_REQUEST_CODE);
    }

    @Override
    public void onDeleteClick(Package packageModel) {
        db.collection("packages").document(packageModel.getId()) // Assuming getId() returns the document ID
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove package from local list and notify adapter
                    packageList.remove(packageModel);
                    packageAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Package deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting package", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PACKAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            loadPackages(); // Refresh the package list
        }
    }
}
