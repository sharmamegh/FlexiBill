package com.servicesuite.flexibill;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.ViewHolder> {

    private final List<Map<String, Object>> packages;
    private int selectedPosition = -1;
    private Context context;

    public PackageAdapter(List<Map<String, Object>> packages, Context context) {
        this.packages = packages;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_package, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> pkg = packages.get(position);
        holder.packageName.setText((String) pkg.get("name"));
        holder.packagePrice.setText("₹ " + pkg.get("price") + " per person");

        holder.radioButton.setChecked(position == selectedPosition);
        holder.radioButton.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition();
            notifyDataSetChanged();
        });

        holder.itemView.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition();
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return packages.size();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView packageName;
        TextView packagePrice;
        RadioButton radioButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            packageName = itemView.findViewById(R.id.package_name);
            packagePrice = itemView.findViewById(R.id.package_price);
            radioButton = itemView.findViewById(R.id.radio_button);
        }
    }
}
