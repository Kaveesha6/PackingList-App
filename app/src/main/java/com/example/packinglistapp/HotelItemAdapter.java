package com.example.packinglistapp;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class HotelItemAdapter extends RecyclerView.Adapter<HotelItemAdapter.ViewHolder> {

    private List<HotelItem> itemList;
    private Context context;

    public HotelItemAdapter(Context context, List<HotelItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checklist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HotelItem item = itemList.get(position);

        holder.checkboxItem.setText(item.getName());
        holder.checkboxItem.setChecked(item.isChecked());
        holder.quantityValue.setText(String.valueOf(item.getQuantity()));

        holder.checkboxItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setChecked(isChecked);
            saveItemListItem(item);
        });

        holder.buttonPlus.setOnClickListener(v -> {
            int quantity = item.getQuantity() + 1;
            item.setQuantity(quantity);
            holder.quantityValue.setText(String.valueOf(quantity));
            saveItemListItem(item);
        });

        holder.buttonMinus.setOnClickListener(v -> {
            int quantity = item.getQuantity();
            if (quantity > 1) {
                quantity--;
                item.setQuantity(quantity);
                holder.quantityValue.setText(String.valueOf(quantity));
            }
            saveItemListItem(item);
        });

        holder.buttonDelete.setOnClickListener(v -> {
            itemList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, itemList.size());
            removeItemListItem(item);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void addItem(HotelItem item) {
        itemList.add(item);
        notifyItemInserted(itemList.size() - 1);
    }

    public List<HotelItem> getItemList() {
        return itemList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkboxItem;
        TextView quantityValue;
        ImageButton buttonPlus, buttonMinus, buttonDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkboxItem = itemView.findViewById(R.id.checkboxItem);
            quantityValue = itemView.findViewById(R.id.quantityValue);
            buttonPlus = itemView.findViewById(R.id.buttonPlus);
            buttonMinus = itemView.findViewById(R.id.buttonMinus);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }

    private void saveItemListItem(HotelItem hotelItem) {
        // Get the current user's ID (you'll need to replace this with actual user ID)
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();  // Replace with actual user ID

        HotelItem newHotelItem = new HotelItem(hotelItem.getName());
        newHotelItem.setChecked(hotelItem.isChecked());
        newHotelItem.setQuantity(hotelItem.getQuantity());

        // Save the hotel item to Firebase
        DatabaseReference hotelRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(((HotelItemsList) context).tripId)
                .child(((HotelItemsList) context).itemListType)
                .child(hotelItem.getHotelItemId());

            hotelRef.setValue(newHotelItem).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Item saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to save item", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void removeItemListItem(HotelItem hotelItem) {
        // Get the current user's ID (you'll need to replace this with actual user ID)
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();  // Replace with actual user ID

        // Save the hotel item to Firebase
        DatabaseReference hotelRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(((HotelItemsList) context).tripId)
                .child(((HotelItemsList) context).itemListType)
                .child(hotelItem.getHotelItemId());

        hotelRef.removeValue().addOnCompleteListener(
                task -> {
                    if (task.isSuccessful()) {
                        updateItemQuantity();
                        Toast.makeText(context, "Item removed successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to remove item", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void updateItemQuantity() {
        // Get the current user's ID (you'll need to replace this with actual user ID)
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();  // Replace with actual user ID
        final int[] quantity = new int[1];

        // Save the hotel item to Firebase
        DatabaseReference hotelRef = FirebaseDatabase.getInstance().getReference("userTrips")
                .child(userId)
                .child(((HotelItemsList) context).tripId);

        hotelRef.child("itemCount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                quantity[0] = snapshot.getValue(Integer.class);
                quantity[0] -= 1;

                hotelRef.child("itemCount").setValue(quantity[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}