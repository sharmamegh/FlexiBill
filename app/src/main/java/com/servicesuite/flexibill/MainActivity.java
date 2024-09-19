package com.servicesuite.flexibill;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    ImageView enter;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        enter = findViewById(R.id.enter);
        enter.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseApp.initializeApp(this);
    }

    @Override
    public void onClick(View v) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // User is logged in, navigate to HomeActivity
            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Boolean businessDetailsComplete = documentSnapshot.getBoolean("businessDetailsComplete");
                            Intent intent;
                            if (businessDetailsComplete != null && businessDetailsComplete) {
                                // Business setup is complete, navigate to HomeActivity
                                intent = new Intent(this, HomeActivity.class);
                            } else {
                                // Business setup is not complete, navigate to BusinessSetupActivity
                                intent = new Intent(this, BusinessSetupActivity.class);
                            }
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            // User document doesn't exist, navigate to BusinessSetupActivity
                            Intent intent = new Intent(this, BusinessSetupActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to check setup status", Toast.LENGTH_SHORT).show());
        } else {
            // User is not logged in, navigate to LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    public void deleteAllLocations() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference locationsRef = db.collection("locations");

        // Get all documents in the collection
        locationsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Iterate through each document and delete it
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        document.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Document successfully deleted
                                    System.out.println("Document successfully deleted!");
                                })
                                .addOnFailureListener(e -> {
                                    // Handle the error
                                    System.err.println("Error deleting document: " + e.getMessage());
                                });
                    }
                } else {
                    // Handle the error
                    System.err.println("Error getting documents: " + task.getException().getMessage());
                }
            }
        });
    }

}