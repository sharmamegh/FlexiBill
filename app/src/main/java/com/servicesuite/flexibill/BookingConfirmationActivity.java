package com.servicesuite.flexibill;

import android.content.ActivityNotFoundException;
import android.view.View;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BookingConfirmationActivity extends AppCompatActivity {

    private TextView tvPackageDetails;
    private Button btnCreateBill;
    private ImageView logoImageView;  // ImageView to show the logo
    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 100;
    private Map<String, Object> bookingDetails;  // Contains all booking details
    private String logoUrl;  // Ensure you set this to the user's logo URL
      // Share button
      private boolean isPDFOpened = false;
    private String pdfPath;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private ListenerRegistration logoListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);

        tvPackageDetails = findViewById(R.id.tvPackageDetails);
        btnCreateBill = findViewById(R.id.btnCreateBill);
        logoImageView = findViewById(R.id.logoImageView); // Reference to ImageView for logo

        // Retrieve booking details passed via Intent
        bookingDetails = (Map<String, Object>) getIntent().getSerializableExtra("bookingDetails");
        fetchLogoUrlAndDisplay();
        // Display the details in the text view
        displayBookingDetails();

        btnCreateBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createBillPDF(); // Your method to create the PDF
            }
        });
    }

    private void fetchLogoUrlAndDisplay() {
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        logoUrl = documentSnapshot.getString("logoUrl");  // Assuming logoUrl field is in the user's document
                        if (logoUrl != null) {
                            // Use Glide to load the circular image into ImageView
                            Glide.with(this)
                                    .load(logoUrl)
                                    .circleCrop() // This makes the image circular
                                    .into((ImageView) findViewById(R.id.logoImageView));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch logo", Toast.LENGTH_SHORT).show();
                });
    }


    private void displayBookingDetails() {
        // Extract information from bookingDetails
        String packageName = (String) bookingDetails.get("packageName");
        int packagePrice = (int) bookingDetails.get("packagePrice");
        String date = (String) bookingDetails.get("date");
        String time = (String) bookingDetails.get("time");
        String location = (String) bookingDetails.get("location");
        int numberOfPeople = (int) bookingDetails.get("numberOfPeople");

        // Handle selected items as a Map
        Map<String, Object> selectedItems = (Map<String, Object>) bookingDetails.get("selectedItems");
        StringBuilder selectedItemsString = new StringBuilder("Selected Items:\n");

        for (Map.Entry<String, Object> entry : selectedItems.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().toString().isEmpty())
                selectedItemsString.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        String details = "Package: " + packageName + "\n"
                + "Price: " + packagePrice + " INR\n"
                + "Date: " + date + "\n"
                + "Time: " + time + "\n"
                + "Location: " + location + "\n"
                + "Number of People: " + numberOfPeople + "\n"
                + selectedItemsString;

        tvPackageDetails.setText(details);
    }

    private void createBillPDF() {

        // Save booking details to Firestore
        saveBookingToFirestore(() -> {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference logoRef = storage.getReferenceFromUrl(logoUrl);  // Ensure logoUrl is initialized properly

            logoRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
                try {

                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

                    // File location for the PDF (using getExternalFilesDir for scoped storage)
                    File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),  "BookingBill_" + timeStamp + ".pdf");
                    pdfPath = file.getAbsolutePath();
                    FileOutputStream fileOutputStream = new FileOutputStream(file);

                    // Create the PDF document
                    Document document = new Document();
                    PdfWriter.getInstance(document, fileOutputStream);
                    document.open();

                    // Convert bytes to Bitmap and then to iText Image
                    Bitmap logoBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    logoBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    Image img = Image.getInstance(stream.toByteArray());

                    // Add the logo to the PDF
                    document.add(img);

                    // Add booking details and package information
                    document.add(new Paragraph("Booking Confirmation\n\n"));
                    document.add(new Paragraph("Package Name: " + bookingDetails.get("packageName")));
                    document.add(new Paragraph("Price: " + bookingDetails.get("packagePrice") + " INR"));
                    document.add(new Paragraph("Date: " + bookingDetails.get("date")));
                    document.add(new Paragraph("Time: " + bookingDetails.get("time")));
                    document.add(new Paragraph("Location: " + bookingDetails.get("location")));
                    document.add(new Paragraph("Number of People: " + bookingDetails.get("numberOfPeople")));

                    // Handle selected items as a Map
                    Map<String, Object> selectedItems = (Map<String, Object>) bookingDetails.get("selectedItems");
                    document.add(new Paragraph("Selected Items:"));
                    for (Map.Entry<String, Object> entry : selectedItems.entrySet()) {
                        document.add(new Paragraph(entry.getKey() + ": " + entry.getValue()));
                    }

                    document.add(new Paragraph("\nThank you for choosing our services!"));

                    document.close();
                    fileOutputStream.close();

                    Toast.makeText(this, "Bill PDF saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

                    openPDF(file);
                    isPDFOpened = true;

                } catch (Exception e) {
                    Log.e("PDF Creation", "Error creating PDF", e);
                    Toast.makeText(this, "Error creating PDF", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to download logo", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void openPDF(File file) {
        Uri pdfUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY); // Optional: Prevents the activity from showing in recent apps

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No app found to view PDF", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onBackPressed() {
        if (isPDFOpened) {
            // Navigate back to HomeActivity when the PDF was opened
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Finish CreateBillActivity
        } else {
            super.onBackPressed(); // Default back behavior if PDF was not opened
        }
    }

    // Save the booking to Firestore
    private void saveBookingToFirestore(Runnable onSuccess) {
        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("packageId", bookingDetails.get("packageId"));
        bookingData.put("packageName", bookingDetails.get("packageName"));
        bookingData.put("packagePrice", bookingDetails.get("packagePrice"));
        bookingData.put("location", bookingDetails.get("location"));
        bookingData.put("date", bookingDetails.get("date"));
        bookingData.put("time", bookingDetails.get("time"));
        bookingData.put("numberOfPeople", bookingDetails.get("numberOfPeople"));
        bookingData.put("selectedItems", bookingDetails.get("selectedItems"));

        // Assuming 'bookings' is the Firestore collection for storing bookings
        db.collection("bookings").add(bookingData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "Booking added with ID: " + documentReference.getId());
                    onSuccess.run();  // Proceed with PDF creation after saving the booking
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error adding booking", e);
                    Toast.makeText(this, "Error saving booking", Toast.LENGTH_SHORT).show();
                });
    }

    // Handle the result of runtime permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // Ensure this is called

        if (requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, call createBillPDF again
                createBillPDF();
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied to write to external storage.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
