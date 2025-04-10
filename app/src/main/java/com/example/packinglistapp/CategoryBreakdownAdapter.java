package com.example.packinglistapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

public class CategoryBreakdownAdapter extends RecyclerView.Adapter<CategoryBreakdownAdapter.CategoryViewHolder> {

    private List<CategoryBreakdown> categories;
    private String currencySymbol;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private final DecimalFormat percentFormat = new DecimalFormat("0.0");

    public CategoryBreakdownAdapter(List<CategoryBreakdown> categories, String currencySymbol) {
        this.categories = categories;
        this.currencySymbol = currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_categorybreakdown, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryBreakdown category = categories.get(position);

        holder.textCategoryName.setText(category.getCategoryName());
        holder.textCategoryTotal.setText(currencySymbol + decimalFormat.format(category.getTotalAmount()));
        holder.textCategoryPercentage.setText(percentFormat.format(category.getPercentage()) + "% of total");
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView textCategoryName, textCategoryTotal, textCategoryPercentage;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textCategoryName = itemView.findViewById(R.id.textCategoryName);
            textCategoryTotal = itemView.findViewById(R.id.textCategoryTotal);
            textCategoryPercentage = itemView.findViewById(R.id.textCategoryPercentage);
        }
    }
}