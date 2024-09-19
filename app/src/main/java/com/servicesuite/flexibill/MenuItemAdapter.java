package com.servicesuite.flexibill;

import android.provider.ContactsContract;
import android.view.LayoutInflater;

import com.google.firebase.firestore.FirebaseFirestore;
import com.servicesuite.flexibill.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

// MenuItemAdapter.java
// MenuItemAdapter.java
public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.ViewHolder> {

    private List<MenuItem> menuItems;
    private FirebaseFirestore db;

    public MenuItemAdapter(List<MenuItem> menuItems, FirebaseFirestore db) {
        this.menuItems = menuItems;
        this.db = db;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MenuItem menuItem = menuItems.get(position);
        holder.itemNameTextView.setText(menuItem.getName());

        // Handle delete button click
        holder.deleteButton.setOnClickListener(v -> deleteMenuItem(menuItem));
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    private void deleteMenuItem(MenuItem menuItem) {
        db.collection("menuItems").whereEqualTo("name", menuItem.getName())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String itemId = task.getResult().getDocuments().get(0).getId();
                        db.collection("menuItems").document(itemId).delete()
                                .addOnSuccessListener(aVoid -> {
                                    menuItems.remove(menuItem);
                                    notifyDataSetChanged();
                                });
                    }
                });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTextView;
        ImageView deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
