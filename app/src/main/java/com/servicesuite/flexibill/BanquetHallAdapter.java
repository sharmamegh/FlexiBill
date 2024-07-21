package com.servicesuite.flexibill;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class BanquetHallAdapter extends RecyclerView.Adapter<BanquetHallAdapter.ViewHolder> {

    private final List<Map<String, Object>> banquetHalls;
    private final OnHallClickListener onHallClickListener;

    public BanquetHallAdapter(List<Map<String, Object>> banquetHalls, OnHallClickListener onHallClickListener) {
        this.banquetHalls = banquetHalls;
        this.onHallClickListener = onHallClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banquet_hall, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> hall = banquetHalls.get(position);
        holder.hallName.setText((String) hall.get("name"));
        holder.hallCapacity.setText("Capacity: " + hall.get("capacity").toString());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onHallClickListener.onHallClick(hall);
            }
        });
    }

    @Override
    public int getItemCount() {
        return banquetHalls.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView hallName;
        TextView hallCapacity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            hallName = itemView.findViewById(R.id.hall_name);
            hallCapacity = itemView.findViewById(R.id.hall_capacity);
        }
    }

    public interface OnHallClickListener {
        void onHallClick(Map<String, Object> hall);
    }
}
