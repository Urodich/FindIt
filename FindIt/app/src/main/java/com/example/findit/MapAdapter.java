package com.example.findit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MapAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private final List<MapFragment> myList;

    public MapAdapter(List<MapFragment> list) {
        myList = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.map_list_element, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        MapFragment currentItem = myList.get(holder.getAdapterPosition());

        holder.name.setText(currentItem.name);
        holder.image.setImageDrawable(currentItem.image);

        holder.itemView.setOnClickListener(view -> {
            if (listener != null) {
                listener.onItemClick(holder.getAdapterPosition());
            }
        });

        holder.itemView.setOnLongClickListener(view -> {
            if (longListener != null) {
                longListener.onItemClick(holder.getAdapterPosition(), view);
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return myList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    public interface OnItemLongClickListener {
        void onItemClick(int position, View v);
    }
    private OnItemClickListener listener;
    private OnItemLongClickListener longListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longListener = listener;
    }
}

