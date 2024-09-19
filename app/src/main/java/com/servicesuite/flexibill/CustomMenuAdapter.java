package com.servicesuite.flexibill;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// CustomMenuAdapter.java
public class CustomMenuAdapter extends RecyclerView.Adapter<CustomMenuAdapter.ViewHolder> {

    private Map<String, List<MenuItem>> categoryItemsMap;
    private FirebaseFirestore db;

    public CustomMenuAdapter(Map<String, List<MenuItem>> categoryItemsMap) {
        this.categoryItemsMap = categoryItemsMap;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_with_items, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String category = new ArrayList<>(categoryItemsMap.keySet()).get(position);
        holder.categoryNameTextView.setText(category);

        // Handle delete category button click
        holder.deleteCategoryButton.setOnClickListener(v -> {
            deleteCategory(category);
        });

        // Setup RecyclerView for items in this category
        holder.itemsRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.itemsRecyclerView.setAdapter(new MenuItemAdapter(categoryItemsMap.get(category), db));
    }

    @Override
    public int getItemCount() {
        return categoryItemsMap.size();
    }

    private void deleteCategory(String category) {
        // Remove the category from Firestore
        db.collection("categories")
                .whereEqualTo("name", category)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String categoryId = task.getResult().getDocuments().get(0).getId();
                        db.collection("categories").document(categoryId).delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Remove all items associated with this category
                                    db.collection("menuItems")
                                            .whereEqualTo("category", category)
                                            .get()
                                            .addOnCompleteListener(itemsTask -> {
                                                if (itemsTask.isSuccessful()) {
                                                    for (QueryDocumentSnapshot itemDocument : itemsTask.getResult()) {
                                                        db.collection("menuItems").document(itemDocument.getId()).delete();
                                                    }
                                                }
                                                // Update UI after deletion
                                                categoryItemsMap.remove(category);
                                                notifyDataSetChanged();
                                            });
                                });
                    }
                });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryNameTextView;
        ImageButton deleteCategoryButton;
        RecyclerView itemsRecyclerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryNameTextView = itemView.findViewById(R.id.categoryNameTextView);
            deleteCategoryButton = itemView.findViewById(R.id.deleteCategoryButton);
            itemsRecyclerView = itemView.findViewById(R.id.itemsRecyclerView);
        }
    }
}
