package com.servicesuite.flexibill;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.PackageViewHolder> {

    private final List<Package> packageList;
    private final OnPackageClickListener onPackageClickListener;

    public PackageAdapter(List<Package> packageList, OnPackageClickListener onPackageClickListener) {
        this.packageList = packageList;
        this.onPackageClickListener = onPackageClickListener;
    }

    @NonNull
    @Override
    public PackageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_package, parent, false);
        return new PackageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PackageViewHolder holder, int position) {
        Package packageModel = packageList.get(position);
        holder.tvPackageName.setText(packageModel.getName());
        holder.tvPackagePrice.setText(String.format("₹ %.2f", packageModel.getPrice()));

        // Set click listeners for edit and delete actions
        holder.ivEdit.setOnClickListener(v -> onPackageClickListener.onEditClick(packageModel));
        holder.ivDelete.setOnClickListener(v -> onPackageClickListener.onDeleteClick(packageModel));
    }

    @Override
    public int getItemCount() {
        return packageList.size();
    }

    public interface OnPackageClickListener {
        void onEditClick(Package packageModel);
        void onDeleteClick(Package packageModel);
    }

    public static class PackageViewHolder extends RecyclerView.ViewHolder {
        TextView tvPackageName;
        TextView tvPackagePrice;
        ImageView ivEdit;
        ImageView ivDelete;

        public PackageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPackageName = itemView.findViewById(R.id.tvPackageName);
            tvPackagePrice = itemView.findViewById(R.id.tvPackagePrice);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }
}



//
//import android.content.Context;
//import android.content.Intent;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.RadioButton;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import java.util.List;
//import java.util.Map;
//
//public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.ViewHolder> {
//
//    private final List<Map<String, Object>> packages;
//    private int selectedPosition = -1;
//    private Context context;
//
//    public PackageAdapter(List<Map<String, Object>> packages, Context context) {
//        this.packages = packages;
//        this.context = context;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_package, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        Map<String, Object> pkg = packages.get(position);
//        holder.packageName.setText((String) pkg.get("name"));
//        holder.packagePrice.setText("₹ " + pkg.get("price") + " per person");
//
//        holder.radioButton.setChecked(position == selectedPosition);
//        holder.radioButton.setOnClickListener(v -> {
//            selectedPosition = holder.getAdapterPosition();
//            notifyDataSetChanged();
//        });
//
//        holder.itemView.setOnClickListener(v -> {
//            selectedPosition = holder.getAdapterPosition();
//            notifyDataSetChanged();
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return packages.size();
//    }
//
//    public int getSelectedPosition() {
//        return selectedPosition;
//    }
//
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        TextView packageName;
//        TextView packagePrice;
//        RadioButton radioButton;
//
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//            packageName = itemView.findViewById(R.id.package_name);
//            packagePrice = itemView.findViewById(R.id.package_price);
//            radioButton = itemView.findViewById(R.id.radio_button);
//        }
//    }
//}