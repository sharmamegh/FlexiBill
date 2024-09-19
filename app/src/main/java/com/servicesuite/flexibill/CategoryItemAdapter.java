package com.servicesuite.flexibill;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryItemAdapter extends RecyclerView.Adapter<CategoryItemAdapter.ViewHolder> {

    private final Context context;
    private final List<String> categoryItems;
    private final int quantity;

    public CategoryItemAdapter(Context context, List<String> categoryItems, int quantity) {
        this.context = context;
        this.categoryItems = categoryItems;
        this.quantity = quantity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.category_item_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Create a dropdown spinner for each required item
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, categoryItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinner.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return quantity; // The number of dropdowns is equal to the quantity specified in the package
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Spinner spinner;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            spinner = itemView.findViewById(R.id.spinnerCategoryItem);
        }
    }
}

