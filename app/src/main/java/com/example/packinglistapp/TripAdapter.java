package com.example.packinglistapp;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {
    private Context context;
    private List<Trip> tripList;
    private OnTripClickListener onTripClickListener;

    public interface OnTripClickListener {
        void onTripClick(Trip trip);
    }

    public TripAdapter(Context context, List<Trip> tripList, OnTripClickListener listener) {
        this.context = context;
        this.tripList = tripList;
        this.onTripClickListener = listener;

    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        Trip trip = tripList.get(position);
        holder.tripNameTextView.setText(trip.getName());
        holder.tripDateTextView.setText(trip.getStartDate() + " - " + trip.getEndDate());
        holder.itemCountTextView.setText(trip.getItemCount() + " items");

        // Load trip image using Glide
        Glide.with(context)
                .load(trip.getImageUrl())
                .placeholder(R.drawable.ic_trip)
                .into(holder.tripIconImageView);

        holder.itemView.setOnClickListener(v -> onTripClickListener.onTripClick(trip));
    }

    @Override
    public int getItemCount() {
        return tripList.size();
    }

    public static class TripViewHolder extends RecyclerView.ViewHolder {
        TextView tripNameTextView, tripDateTextView, itemCountTextView;
        ImageView tripIconImageView;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            tripNameTextView = itemView.findViewById(R.id.tripNameTextView);
            tripDateTextView = itemView.findViewById(R.id.tripDateTextView);
            itemCountTextView = itemView.findViewById(R.id.itemCountTextView);
            tripIconImageView = itemView.findViewById(R.id.tripIconImageView);
        }
    }
}
