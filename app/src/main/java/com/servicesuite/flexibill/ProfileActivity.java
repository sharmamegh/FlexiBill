package com.servicesuite.flexibill;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
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

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

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

        if (user != null) {
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String userName = documentSnapshot.getString("userName");
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

        findViewById(R.id.delete_account_button).setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void showDeleteAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Account");
        builder.setMessage("Deleting your account will remove all of your information from our database. This cannot be undone. Please type 'DELETE' to confirm.");

        final View customView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_account, null);
        builder.setView(customView);

        final EditText inputField = customView.findViewById(R.id.confirmation_input);

        builder.setPositiveButton("Delete Account", null); // null for now, we'll handle click manually
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        dialog.show();
        Button deleteButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        deleteButton.setOnClickListener(v -> {
            String inputText = inputField.getText().toString().trim();

            if ("DELETE".equalsIgnoreCase(inputText)) {
                dialog.dismiss();

                showFireAnimationAndDeleteAccount();
            } else {
                Toast.makeText(this, "Please type 'DELETE' to confirm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFireAnimationAndDeleteAccount() {
        LottieAnimationView fireAnimation = findViewById(R.id.fire_animation_view);
        fireAnimation.setVisibility(View.VISIBLE);
        fireAnimation.playAnimation();

        new Handler().postDelayed(this::deleteAccount, 3000); // Delay to let the animation play for a while
    }

    private void deleteAccount() {
        if (user != null) {
            // Delete Firestore data
            db.collection("users").document(user.getUid()).delete().addOnSuccessListener(aVoid -> {
                // Delete Storage data (logo, signature)
                if (logoUrl != null && signatureUrl != null) {
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
    }
}