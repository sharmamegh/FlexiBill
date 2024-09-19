package com.servicesuite.flexibill;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BookingActivity extends AppCompatActivity implements LocationAdapter.OnLocationClickListener {

    private EditText etDate, etTime, etPeople;
    private TextView loadingText;

    private FirebaseFirestore db;
    private List<Location> locationList = new ArrayList<>();
    private LocationAdapter locationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        etDate = findViewById(R.id.et_date);
        etTime = findViewById(R.id.et_time);
        etPeople = findViewById(R.id.et_people);
        Button btnCheckAvailability = findViewById(R.id.btn_check_availability);
        RecyclerView rvLocations = findViewById(R.id.rv_locations);
        loadingText = findViewById(R.id.loading_text);

        db = FirebaseFirestore.getInstance();

        rvLocations.setLayoutManager(new LinearLayoutManager(this));
        locationAdapter = new LocationAdapter(locationList, new LocationAdapter.OnLocationClickListener() {
            @Override
            public void onLocationClick(Location location) {
                // Navigate to package selection with location and booking details
                Intent intent = new Intent(BookingActivity.this, PackageSelectionActivity.class);
                intent.putExtra("locationId", location.getId());
                intent.putExtra("locationName", location.getName());
                intent.putExtra("date", etDate.getText().toString());
                intent.putExtra("time", etTime.getText().toString());
                intent.putExtra("people", Integer.parseInt(etPeople.getText().toString()));
                startActivity(intent);
            }

            @Override
            public void onEditClick(Location location) {
                // Optional: Handle edit if needed (for future extensions)
            }

            @Override
            public void onDeleteClick(Location location) {
                // Optional: Handle delete if needed (for future extensions)
            }
        }, false); // Hide edit and delete buttons

        rvLocations.setAdapter(locationAdapter);

        etDate.setOnClickListener(v -> showDatePickerDialog());

        etTime.setOnClickListener(v -> showTimePickerDialog());

        btnCheckAvailability.setOnClickListener(v -> checkAvailability());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                BookingActivity.this,
                (view, year1, month1, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                    etDate.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                BookingActivity.this,
                (view, hourOfDay, minute1) -> {
                    String selectedTime = hourOfDay + ":" + (minute1 < 10 ? "0" + minute1 : minute1);
                    etTime.setText(selectedTime);
                },
                hour, minute, true
        );
        timePickerDialog.show();
    }

    private void checkAvailability() {
        String date = etDate.getText().toString();
        String time = etTime.getText().toString();
        int people = Integer.parseInt(etPeople.getText().toString());

        loadingText.setVisibility(View.VISIBLE);

        // Fetch available locations from Firestore
        db.collection("locations")
                .whereGreaterThanOrEqualTo("capacity", people)
                .get()
                .addOnCompleteListener(task -> {
                    loadingText.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        locationList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Location location = document.toObject(Location.class);
                            location.setId(document.getId());
                            locationList.add(location);
                        }
                        locationAdapter.notifyDataSetChanged();
                    } else {
                        Log.w("BookingActivity", "Error getting documents.", task.getException());
                        Toast.makeText(BookingActivity.this, "Error getting data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onLocationClick(Location location) {
        // Navigate to package selection or other screens with location data
//        Intent intent = new Intent(BookingActivity.this, PackageSelectionActivity.class);
//        intent.putExtra("locationId", location.getId());
//        intent.putExtra("locationName", location.getName());
//        intent.putExtra("date", etDate.getText().toString());
//        intent.putExtra("time", etTime.getText().toString());
//        intent.putExtra("people", Integer.parseInt(etPeople.getText().toString()));
//        startActivity(intent);
        Toast.makeText(this,"Selected a location",Toast.LENGTH_SHORT);
    }
    @Override
    public void onEditClick(Location location) {
        // No action needed for BookingActivity
    }
    @Override
    public void onDeleteClick(Location location) {
        // No action needed for BookingActivity
    }


}
