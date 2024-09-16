//package com.servicesuite.flexibill;
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import java.util.Map;
//
//public class PackageItemAdapter extends RecyclerView.Adapter<PackageItemAdapter.PackageItemViewHolder> {
//
//    private final Map<String, Integer> items;
//    private final OnItemInteractionListener onItemInteractionListener;
//    private final Context context;
//
//    public PackageItemAdapter(Map<String, Integer> items, OnItemInteractionListener onItemInteractionListener, Context context) {
//        this.items = items;
//        this.onItemInteractionListener = onItemInteractionListener;
//        this.context = context;
//    }
//
//    @NonNull
//    @Override
//    public PackageItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_package_detail, parent, false);
//        return new PackageItemViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull PackageItemViewHolder holder, int position) {
//        String itemName = (String) items.keySet().toArray()[position];
//        int quantity = items.get(itemName);
//
//        holder.tvItemName.setText(itemName);
//        holder.tvQuantity.setText(String.valueOf(quantity));
//
//        holder.icEdit.setOnClickListener(v -> showEditDialog(holder, itemName, quantity));
//        holder.icDelete.setOnClickListener(v -> {
//            items.remove(itemName);
//            notifyDataSetChanged();
//            onItemInteractionListener.onDeleteItem(itemName);
//        });
//    }
//
//    private void showEditDialog(PackageItemViewHolder holder, String itemName, int quantity) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle("Edit Item");
//
//        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_item, null);
//        EditText etItemName = view.findViewById(R.id.etItemName);
//        EditText etQuantity = view.findViewById(R.id.etQuantity);
//
//        etItemName.setText(itemName);
//        etQuantity.setText(String.valueOf(quantity));
//
//        builder.setView(view);
//        builder.setPositiveButton("Save", (dialog, which) -> {
//            String newItemName = etItemName.getText().toString().trim();
//            String quantityStr = etQuantity.getText().toString().trim();
//
//            if (!newItemName.isEmpty() && !quantityStr.isEmpty()) {
//                int newQuantity;
//                try {
//                    newQuantity = Integer.parseInt(quantityStr);
//                } catch (NumberFormatException e) {
//                    newQuantity = 0; // Default to 0 if invalid
//                }
//
//                // Remove the old item and add the updated one
//                items.remove(itemName);
//                items.put(newItemName, newQuantity);
//                notifyDataSetChanged();
//                onItemInteractionListener.onItemUpdated(newItemName, newQuantity);
//            }
//        });
//
//        builder.setNegativeButton("Cancel", null);
//
//        builder.show();
//    }
//
//    @Override
//    public int getItemCount() {
//        return items.size();
//    }
//
//    public static class PackageItemViewHolder extends RecyclerView.ViewHolder {
//        TextView tvItemName, tvQuantity;
//        ImageView icEdit, icDelete;
//
//        public PackageItemViewHolder(@NonNull View itemView) {
//            super(itemView);
//            tvItemName = itemView.findViewById(R.id.tvItemName);
//            tvQuantity = itemView.findViewById(R.id.tvQuantity);
//            icEdit = itemView.findViewById(R.id.icEdit);
//            icDelete = itemView.findViewById(R.id.icDelete);
//        }
//    }
//
//    public interface OnItemInteractionListener {
//        void onDeleteItem(String itemName);
//        void onItemUpdated(String itemName, int quantity);
//    }
//}

package com.servicesuite.flexibill;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Map;

public class PackageItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    private final Map<String, Integer> items;
    private final OnItemInteractionListener onItemInteractionListener;
    private final Context context;

    public PackageItemAdapter(Map<String, Integer> items, OnItemInteractionListener onItemInteractionListener, Context context) {
        this.items = items;
        this.onItemInteractionListener = onItemInteractionListener;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_package_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_package_detail, parent, false);
            return new PackageItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            // Headers are already set in the layout XML
        } else if (holder instanceof PackageItemViewHolder) {
            String itemName = (String) items.keySet().toArray()[position - 1]; // Adjust for header
            int quantity = items.get(itemName);

            PackageItemViewHolder itemHolder = (PackageItemViewHolder) holder;
            itemHolder.tvItemName.setText(itemName);
            itemHolder.tvQuantity.setText(String.valueOf(quantity));

            itemHolder.icEdit.setOnClickListener(v -> showEditDialog(itemName, quantity, itemHolder.getAdapterPosition()));
            itemHolder.icDelete.setOnClickListener(v -> {
                items.remove(itemName);
                notifyDataSetChanged();
                onItemInteractionListener.onDeleteItem(itemName);
            });
        }
    }

    private void showEditDialog(String itemName, int quantity, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Item");

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_item, null);
        EditText etItemName = view.findViewById(R.id.etItemName);
        EditText etQuantity = view.findViewById(R.id.etQuantity);

        etItemName.setText(itemName);
        etQuantity.setText(String.valueOf(quantity));

        builder.setView(view);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newItemName = etItemName.getText().toString().trim();
            String quantityStr = etQuantity.getText().toString().trim();

            if (!newItemName.isEmpty() && !quantityStr.isEmpty()) {
                int newQuantity;
                try {
                    newQuantity = Integer.parseInt(quantityStr);
                } catch (NumberFormatException e) {
                    newQuantity = 0; // Default to 0 if invalid
                }

                items.remove(itemName);
                items.put(newItemName, newQuantity);
                notifyItemChanged(position);
                onItemInteractionListener.onItemUpdated(newItemName, newQuantity);
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    @Override
    public int getItemCount() {
        return items.size() + 1; // +1 for the header
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeaderName, tvHeaderQuantity;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeaderName = itemView.findViewById(R.id.tvHeaderName);
            tvHeaderQuantity = itemView.findViewById(R.id.tvHeaderQuantity);
        }
    }

    public static class PackageItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvQuantity;
        ImageView icEdit, icDelete;

        public PackageItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            icEdit = itemView.findViewById(R.id.icEdit);
            icDelete = itemView.findViewById(R.id.icDelete);
        }
    }

    public interface OnItemInteractionListener {
        void onDeleteItem(String itemName);
        void onItemUpdated(String itemName, int quantity);
    }
}
