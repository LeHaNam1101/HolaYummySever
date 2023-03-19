package com.example.holayummyserver.ViewHolder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.holayummyserver.Common.Common;
import com.example.holayummyserver.Interface.ItemClickListener;
import com.example.holayummyserver.R;

public class FoodViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener,View.OnCreateContextMenuListener
{
    public TextView txtMenuName;
    public android.widget.ImageView ImageView;

    private ItemClickListener itemClickListener;

    public FoodViewHolder(@NonNull View itemView) {
        super(itemView);
        txtMenuName = (TextView) itemView.findViewById(R.id.food_name);
        ImageView = itemView.findViewById(R.id.food_image);
        itemView.setOnCreateContextMenuListener(this);
        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Select the action");
        menu.add(0,0,getAdapterPosition(), Common.UPDATE);
        menu.add(0,1,getAdapterPosition(),Common.DELETE);
    }
}