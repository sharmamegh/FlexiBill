package com.servicesuite.flexibill;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.EventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public class LocationActivity extends AppCompatActivity {

    private Button addLocationButton;
    private RecyclerView recyclerView;
    private LocationAdapter locationAdapter;
    private List<Location> locationList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        addLocationButton = findViewById(R.id.btn_add_location);
        recyclerView = findViewById(R.id.rv_locations);

        // Set up RecyclerView
        locationList = new ArrayList<>();
        locationAdapter = new LocationAdapter(locationList, new LocationAdapter.OnLocationClickListener() {
            @Override
            public void onLocationClick(Location location) {
                // Handle location click for viewing details or booking
            }

            @Override
            public void onEditClick(Location location) {
                showAddLocationDialog(location);
            }

            @Override
            public void onDeleteClick(Location location) {
                deleteLocation(location);
            }
        }, true); // Show edit and delete buttons



        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(locationAdapter);

        // Fetch locations and listen for real-time updates
        fetchLocations();

        // Add Location button click listener
        addLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddLocationDialog(null);
            }
        });
    }
    private void showLocationOptionsDialog(final Location location) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LocationActivity.this);
        builder.setTitle("Location Options")
                .setItems(new CharSequence[]{"Edit", "Delete", "Cancel"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: // Edit
                                showAddLocationDialog(location);
                                break;
                            case 1: // Delete
                                deleteLocation(location);
                                break;
                            case 2: // Cancel
                                dialog.dismiss();
                                break;
                        }
                    }
                })
                .show();
    }


    private void fetchLocations() {
        db.collection("locations").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    // Log the error for debugging
                    Log.e("LocationActivity", "Error loading data", error);
                    return;
                }

                if (value == null || value.isEmpty()) {
                    // No data found, handle appropriately
                    locationList.clear();
                    locationAdapter.notifyDataSetChanged();
                    return;
                }

                locationList.clear();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    try {
                        Location location = doc.toObject(Location.class);
                        if (location != null) {
                            location.setId(doc.getId()); // Set the document ID to the location object
                            locationList.add(location);
                        }
                    } catch (Exception e) {
                        // Handle deserialization issues
                        Log.e("LocationActivity", "Deserialization error", e);
                    }
                }
                locationAdapter.notifyDataSetChanged();
            }
        });
    }



    private void showAddLocationDialog(final Location location) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LocationActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_location, null);

        EditText locationNameInput = dialogView.findViewById(R.id.edit_location_name);
        EditText locationCapacityInput = dialogView.findViewById(R.id.edit_location_capacity);
        EditText locationAddressInput = dialogView.findViewById(R.id.edit_location_address);

        if (location != null) {
            locationNameInput.setText(location.getName());
            locationCapacityInput.setText(String.valueOf(location.getCapacity()));
            locationAddressInput.setText(location.getAddress());
        }

        builder.setView(dialogView)
                .setTitle(location == null ? "Add Location" : "Edit Location")
                .setPositiveButton(location == null ? "Add" : "Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String locationName = locationNameInput.getText().toString();
                        String locationCapacity = locationCapacityInput.getText().toString();
                        String locationAddress = locationAddressInput.getText().toString();

                        if (!locationName.isEmpty() && !locationCapacity.isEmpty() && !locationAddress.isEmpty()) {
                            if (location == null) {
                                addLocationToFirestore(locationName, locationCapacity, locationAddress);
                            } else {
                                updateLocationInFirestore(location.getId(), locationName, locationCapacity, locationAddress);
                            }
                        } else {
                            Toast.makeText(LocationActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addLocationToFirestore(String name, String capacityStr, String address) {
        int capacity;
        try {
            capacity = Integer.parseInt(capacityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(LocationActivity.this, "Invalid capacity value", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> location = new HashMap<>();
        location.put("name", name);
        location.put("capacity", capacity); // Ensure this is an int
        location.put("address", address);

        db.collection("locations")
                .add(location)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(LocationActivity.this, "Location added successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LocationActivity.this, "Failed to add location", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void updateLocationInFirestore(String locationId, String name, String capacityStr, String address) {
        int capacity;
        try {
            capacity = Integer.parseInt(capacityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(LocationActivity.this, "Invalid capacity value", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> location = new HashMap<>();
        location.put("name", name);
        location.put("capacity", capacity); // Use int for capacity
        location.put("address", address);

        db.collection("locations").document(locationId)
                .update(location)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(LocationActivity.this, "Location updated successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LocationActivity.this, "Failed to update location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteLocation(Location location) {
        db.collection("locations").document(location.getId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(LocationActivity.this, "Location deleted successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LocationActivity.this, "Failed to delete location", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
