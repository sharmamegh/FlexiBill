package com.servicesuite.flexibill;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class BusinessSetupActivity extends AppCompatActivity {
    private EditText userNameInput, businessNameInput;
    private ImageView logoImageView, signatureImageView;
    private Uri logoUri, signatureUri;
    private ProgressDialog progressDialog;
    private FirebaseUser user;

    private static final int PICK_LOGO_REQUEST = 1;
    private static final int PICK_SIGNATURE_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_business_setup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processing...");
        progressDialog.setCancelable(false);

        userNameInput = findViewById(R.id.user_name_input);
        businessNameInput = findViewById(R.id.business_name_input);
        logoImageView = findViewById(R.id.logo_image_view);
        signatureImageView = findViewById(R.id.signature_image_view);

        user = FirebaseAuth.getInstance().getCurrentUser();

        findViewById(R.id.select_logo_button).setOnClickListener(v -> pickImage(PICK_LOGO_REQUEST));
        findViewById(R.id.select_signature_button).setOnClickListener(v -> pickImage(PICK_SIGNATURE_REQUEST));

        findViewById(R.id.save_button).setOnClickListener(v -> {
            showProgressDialog();
            String userName = userNameInput.getText().toString().trim();
            String businessName = businessNameInput.getText().toString().trim();

            if (userName.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            } else if (businessName.isEmpty()) {
                Toast.makeText(this, "Please enter your business name", Toast.LENGTH_SHORT).show();
            } else if (logoUri == null) {
                Toast.makeText(this, "Please upload a logo", Toast.LENGTH_SHORT).show();
            } else if (signatureUri == null) {
                Toast.makeText(this, "Please upload a signature", Toast.LENGTH_SHORT).show();
            } else {
                uploadImagesAndSaveDetails(userName, businessName);
            }
        });

    }

    private void pickImage(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            if (requestCode == PICK_LOGO_REQUEST) {
                logoUri = selectedImageUri;
                logoImageView.setImageURI(logoUri);  // Display selected logo
            } else if (requestCode == PICK_SIGNATURE_REQUEST) {
                signatureUri = selectedImageUri;
                signatureImageView.setImageURI(signatureUri);  // Display selected signature
            }
        }
    }

    private void uploadImagesAndSaveDetails(String userName, String businessName) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Upload logo
        StorageReference logoRef = storageRef.child("logos/" + user.getUid() + "_logo.png");
        logoRef.putFile(logoUri)
                .addOnSuccessListener(taskSnapshot -> logoRef.getDownloadUrl()
                        .addOnSuccessListener(logoDownloadUrl -> {
                            // Upload signature after logo is uploaded
                            StorageReference signatureRef = storageRef.child("signatures/" + user.getUid() + "_signature.png");
                            signatureRef.putFile(signatureUri)
                                    .addOnSuccessListener(signatureTaskSnapshot -> signatureRef.getDownloadUrl()
                                            .addOnSuccessListener(signatureDownloadUrl -> {
                                                // Now save business details to Firestore
                                                saveBusinessDetails(userName, businessName, logoDownloadUrl.toString(), signatureDownloadUrl.toString());
                                            }))
                                    .addOnFailureListener(e -> {
                                        hideProgressDialog();
                                        Log.w("BusinessSetup", "Error uploading signature", e);
                                        Toast.makeText(this, "Signature upload failed. Please try again.", Toast.LENGTH_LONG).show();
                                    });
                        }))
                .addOnFailureListener(e -> {
                    hideProgressDialog();
                    Log.w("BusinessSetup", "Error uploading logo", e);
                    Toast.makeText(this, "Logo upload failed. Please try again.", Toast.LENGTH_LONG).show();
                });
    }



    private void saveBusinessDetails(String userName, String businessName, String logoUrl, String signatureUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Prepare the business details map
        Map<String, Object> businessMap = new HashMap<>();
        businessMap.put("userName", userName);
        businessMap.put("businessName", businessName);
        businessMap.put("logoUrl", logoUrl);
        businessMap.put("signatureUrl", signatureUrl);

        // Update Firestore with business details
        db.collection("users").document(user.getUid())
                .update(businessMap)
                .addOnSuccessListener(aVoid -> db.collection("users").document(user.getUid())
                        .update("businessDetailsComplete", true)  // Mark setup as complete
                        .addOnSuccessListener(aVoid2 -> {
                            Log.d("BusinessSetup", "Business details successfully saved.");
                            startActivity(new Intent(BusinessSetupActivity.this, HomeActivity.class));
                            finish();
                        }))
                .addOnFailureListener(e -> {
                    hideProgressDialog();
                    Log.w("BusinessSetup", "Error saving business details", e);
                    Toast.makeText(this, "Error saving business details. Please try again.", Toast.LENGTH_LONG).show();
                });
    }

    private void showProgressDialog() {
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}