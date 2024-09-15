package com.servicesuite.flexibill;

import android.content.Context;
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
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onEditClick(Location location);
        void onDeleteClick(Location location);
    }

    public LocationAdapter(List<Location> locationList, OnItemClickListener listener) {
        this.locationList = locationList;
        this.onItemClickListener = listener;
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

        holder.editButton.setOnClickListener(v -> onItemClickListener.onEditClick(location));
        holder.deleteButton.setOnClickListener(v -> onItemClickListener.onDeleteClick(location));
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
}
