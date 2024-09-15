package com.servicesuite.flexibill;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    ImageView enter;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();  // Get FirebaseAuth instance

        enter = findViewById(R.id.enter);
        enter.setOnClickListener(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onClick(View v) {
        // Check if the user is currently logged in
        FirebaseUser currentUser = auth.getCurrentUser();

        Intent intent;
        if (currentUser != null) {
            // User is logged in, navigate to HomeActivity
            intent = new Intent(this, HomeActivity.class);
        } else {
            // User is not logged in, navigate to LoginActivity
            intent = new Intent(this, LoginActivity.class);
        }
        startActivity(intent);
        finish(); // Optional: Close MainActivity after navigating
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