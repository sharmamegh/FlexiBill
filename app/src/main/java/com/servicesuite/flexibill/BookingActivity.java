package com.servicesuite.flexibill;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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
import java.util.Map;

public class BookingActivity extends AppCompatActivity implements BanquetHallAdapter.OnHallClickListener {

    private EditText etDate, etTime, etPeople;
    private TextView loadingText;

    private FirebaseFirestore db;
    private List<Map<String, Object>> hallList = new ArrayList<>();
    private BanquetHallAdapter hallAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        etDate = findViewById(R.id.et_date);
        etTime = findViewById(R.id.et_time);
        etPeople = findViewById(R.id.et_people);
        Button btnCheckAvailability = findViewById(R.id.btn_check_availability);
        RecyclerView rvHalls = findViewById(R.id.rv_halls);
        loadingText = findViewById(R.id.loading_text);

        db = FirebaseFirestore.getInstance();

        rvHalls.setLayoutManager(new LinearLayoutManager(this));
        hallAdapter = new BanquetHallAdapter(hallList, this);
        rvHalls.setAdapter(hallAdapter);

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
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                        etDate.setText(selectedDate);
                    }
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
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String selectedTime = hourOfDay + ":" + (minute < 10 ? "0" + minute : minute);
                        etTime.setText(selectedTime);
                    }
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

        // Fetch available halls from Firestore
        db.collection("BanquetHalls")
                .whereGreaterThanOrEqualTo("capacity", people)
                .get()
                .addOnCompleteListener(task -> {
                    loadingText.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        hallList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            hallList.add(document.getData());
                        }
                        hallAdapter.notifyDataSetChanged();
                    } else {
                        Log.w("BookingActivity", "Error getting documents.", task.getException());
                        Toast.makeText(BookingActivity.this, "Error getting data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onHallClick(Map<String, Object> hall) {
        Intent intent = new Intent(BookingActivity.this, PackageSelectionActivity.class);
        intent.putExtra("hallId", (String) hall.get("id"));
        intent.putExtra("hallName", (String) hall.get("name"));
        intent.putExtra("date", etDate.getText().toString());
        intent.putExtra("time", etTime.getText().toString());
        intent.putExtra("people", Integer.parseInt(etPeople.getText().toString()));
        startActivity(intent);
    }
}
