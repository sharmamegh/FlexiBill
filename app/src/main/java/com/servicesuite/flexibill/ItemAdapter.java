package com.servicesuite.flexibill;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<MenuItem> menuItems;

    public ItemAdapter(List<MenuItem> menuItems) {
        this.menuItems = menuItems;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        MenuItem menuItem = menuItems.get(position);
        holder.itemNameTextView.setText(menuItem.getName());

        // Handle delete button click (optional)
        holder.deleteButton.setOnClickListener(v -> {
            menuItems.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, menuItems.size());
        });
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    // ViewHolder class for items
    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTextView;
        ImageView deleteButton;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
