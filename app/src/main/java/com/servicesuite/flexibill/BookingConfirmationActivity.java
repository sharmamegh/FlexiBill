package com.servicesuite.flexibill;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BookingConfirmationActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private Button sharePdfButton;
    private Button closeButton;
    private static final String TAG = "BookingConfirmationActivity";
    private ImageView pdfImageView;
    private Uri PDF_URL;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);

        pdfImageView = findViewById(R.id.pdf_image_view);
        sharePdfButton = findViewById(R.id.share_pdf_button);
        closeButton = findViewById(R.id.close_button);

        // Check permissions and proceed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
            } else {
                proceedWithBooking();
            }
        } else {
            proceedWithBooking();
        }

        sharePdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PDF_URL != null) {
                    sharePdf(PDF_URL);
                } else {
                    Toast.makeText(BookingConfirmationActivity.this, "PDF not found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up close button listener
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAllActivities();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                proceedWithBooking();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sharePdf(Uri pdfUri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
        startActivity(Intent.createChooser(shareIntent, "Share PDF"));
    }

    private void proceedWithBooking() {
        // Show booking confirmed toast
        Toast.makeText(this, "Booking Confirmed", Toast.LENGTH_LONG).show();

        // Generate PDF and display it
        String pdfUri = generateBookingPdf();
        displayPdf(pdfUri);
    }

    private String generateBookingPdf() {
        Document document = new Document();
        String pdfUri = null;

        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "booking_confirmation.pdf");
            values.put(MediaStore.Images.Media.MIME_TYPE, "application/pdf");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);

            Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
            if (uri != null) {
                OutputStream outputStream = getContentResolver().openOutputStream(uri);
                PdfWriter.getInstance(document, outputStream);
                document.open();

                // Add content to PDF
                document.add(new Paragraph("Booking Confirmation"));
                document.add(new Paragraph("Hall Name: Example Hall"));
                document.add(new Paragraph("Package Selected: Example Package"));
                document.add(new Paragraph("Welcome Drink: 2"));
                document.add(new Paragraph("Starter/Snack: 3"));
                document.add(new Paragraph("Soup: 2"));
                // Add more booking details here

                document.close();
                outputStream.close();
                PDF_URL = uri;
                pdfUri = uri.toString();
            }
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error generating PDF: " + e.getMessage());
        }

        return pdfUri;
    }

    private void finishAllActivities() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void displayPdf(String pdfUri) {
        if (pdfUri != null) {
            try {
                Uri uri = Uri.parse(pdfUri);
                ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
                PdfRenderer pdfRenderer = new PdfRenderer(parcelFileDescriptor);
                PdfRenderer.Page page = pdfRenderer.openPage(0);

                Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                pdfImageView.setImageBitmap(bitmap);

                page.close();
                pdfRenderer.close();
                parcelFileDescriptor.close();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error displaying PDF", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "PDF not found", Toast.LENGTH_SHORT).show();
        }
    }
}
