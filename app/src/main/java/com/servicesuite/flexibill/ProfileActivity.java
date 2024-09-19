package com.servicesuite.flexibill;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileActivity extends AppCompatActivity {
    private EditText userNameInput, businessNameInput;
    private ImageView logoImageView, signatureImageView;
    private String logoUrl, signatureUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userNameInput = findViewById(R.id.user_name_input);
        businessNameInput = findViewById(R.id.business_name_input);
        logoImageView = findViewById(R.id.logo_image_view);
        signatureImageView = findViewById(R.id.signature_image_view);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String userName = documentSnapshot.getString("name");
                    String businessName = documentSnapshot.getString("businessName");
                    logoUrl = documentSnapshot.getString("logoUrl");
                    signatureUrl = documentSnapshot.getString("signatureUrl");

                    userNameInput.setText(userName);
                    businessNameInput.setText(businessName);

                    Glide.with(this)
                            .load(logoUrl)
                            .into(logoImageView);

                    Glide.with(this)
                            .load(signatureUrl)
                            .into(signatureImageView);
                }
            });
        }

        findViewById(R.id.delete_account_button).setOnClickListener(v -> {
            if (user != null) {
                // Delete Firestore data
                db.collection("users").document(user.getUid()).delete().addOnSuccessListener(aVoid -> {
                    // Delete Storage data (logo, signature)
                    if (logoUrl != null && signatureUrl != null) { // Ensure URLs exist before deletion
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference logoRef = storage.getReferenceFromUrl(logoUrl);
                        StorageReference signRef = storage.getReferenceFromUrl(signatureUrl);

                        // Delete the logo
                        logoRef.delete().addOnSuccessListener(aVoid1 -> {
                            // Delete the signature
                            signRef.delete().addOnSuccessListener(aVoid2 -> {
                                // Finally, delete from Firebase Auth
                                user.delete().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();

                                        FirebaseAuth.getInstance().signOut();

                                        // Sign out from GoogleSignInClient
                                        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this,
                                                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                                        .requestIdToken(getString(R.string.default_web_client_id))
                                                        .requestEmail()
                                                        .build());

                                        googleSignInClient.signOut().addOnCompleteListener(logoutTask -> {
                                            // Navigate to LoginActivity after logout
                                            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        });
                                    } else {
                                        Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }).addOnFailureListener(e -> Toast.makeText(this, "Failed to delete signature", Toast.LENGTH_SHORT).show());
                        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to delete logo", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Failed to retrieve logo/signature", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(this, "Failed to delete user profile", Toast.LENGTH_SHORT).show());
            }
        });
    }
}