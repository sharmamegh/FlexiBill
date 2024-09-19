package com.servicesuite.flexibill;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;


public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private List<Location> locationList;
    private OnLocationClickListener onLocationClickListener;
    private boolean showEditDelete; // Flag to control visibility of edit and delete buttons

    public LocationAdapter(List<Location> locationList, OnLocationClickListener listener, boolean showEditDelete) {
        this.locationList = locationList;
        this.onLocationClickListener = listener;
        this.showEditDelete = showEditDelete;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        Location location = locationList.get(position);
        holder.locationName.setText(location.getName());
        holder.locationCapacity.setText(String.valueOf(location.getCapacity()));
        holder.locationAddress.setText(location.getAddress());

        // Show or hide edit/delete buttons based on the flag
        if (showEditDelete) {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.editButton.setOnClickListener(v -> onLocationClickListener.onEditClick(location));
            holder.deleteButton.setOnClickListener(v -> onLocationClickListener.onDeleteClick(location));
        } else {
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(v -> onLocationClickListener.onLocationClick(location));
        }
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView locationName;
        TextView locationCapacity;
        TextView locationAddress;
        ImageView editButton;
        ImageView deleteButton;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            locationName = itemView.findViewById(R.id.text_location_name);
            locationCapacity = itemView.findViewById(R.id.text_location_capacity);
            locationAddress = itemView.findViewById(R.id.text_location_address);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }

    public interface OnLocationClickListener {
        void onLocationClick(Location location);
        void onEditClick(Location location);
        void onDeleteClick(Location location);
    }
}


//public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {
//
//    private List<Location> locationList;
//    private OnLocationClickListener onLocationClickListener;
//
//    public LocationAdapter(List<Location> locationList, OnLocationClickListener listener) {
//        this.locationList = locationList;
//        this.onLocationClickListener = listener;
//    }
//
//    @NonNull
//    @Override
//    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location, parent, false);
//        return new LocationViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
//        Location location = locationList.get(position);
//        holder.locationName.setText(location.getName());
//        holder.locationCapacity.setText(String.valueOf(location.getCapacity()));
//        holder.locationAddress.setText(location.getAddress());
//
//        holder.itemView.setOnClickListener(v -> onLocationClickListener.onLocationClick(location));
//    }
//
//    @Override
//    public int getItemCount() {
//        return locationList.size();
//    }
//
//    public static class LocationViewHolder extends RecyclerView.ViewHolder {
//        TextView locationName;
//        TextView locationCapacity;
//        TextView locationAddress;
//
//        public LocationViewHolder(@NonNull View itemView) {
//            super(itemView);
//            locationName = itemView.findViewById(R.id.text_location_name);
//            locationCapacity = itemView.findViewById(R.id.text_location_capacity);
//            locationAddress = itemView.findViewById(R.id.text_location_address);
//        }
//    }
//
//    // Define the interface with Location object
//    public interface OnLocationClickListener {
//        void onLocationClick(Location location);
//    }
//}
