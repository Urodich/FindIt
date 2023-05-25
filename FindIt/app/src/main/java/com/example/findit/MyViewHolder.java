package com.example.findit;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class MyViewHolder extends RecyclerView.ViewHolder {
    public TextView name;
    public ImageView image;

    public MyViewHolder(View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.mapFragmentName);
        image = itemView.findViewById(R.id.mapFragmentImage);
    }
}
