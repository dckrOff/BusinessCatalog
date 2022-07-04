package com.a1tech.businesscatalog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.MyViewHolder> {

    private final LayoutInflater inflater;
    private ArrayList<Item> itemList;

    private final String pattern = "###,###,###.###";
    private final DecimalFormat decimalFormat = new DecimalFormat(pattern);

    public ItemsAdapter(Context context, ArrayList<Item> itemList) {
        this.itemList = itemList;
        this.inflater = LayoutInflater.from(context);
    }

    // method for filtering our recyclerview items.
    @SuppressLint("NotifyDataSetChanged")
    public void filterList(ArrayList<Item> filterllist) {
        // below line is to add our filtered
        // list in our course array list.
        itemList = filterllist;
        // below line is to notify our adapter
        // as change in recycler view data.
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_category, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        Item item = this.itemList.get(position);

        // formatter of number(1234567890)-- > (1 234 567 890)
        String formatPrice = decimalFormat.format(Double.valueOf(Integer.parseInt(item.getItemPrice())));

        holder.drugName.setText(item.getItemName());
        holder.drugPrice.setText("от " + formatPrice + " сум");
        Glide.with(inflater.getContext()).load(item.getItemImg()).into(holder.drugImage);

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView drugName, drugPrice;
        ImageView drugImage;

        public MyViewHolder(View itemView) {
            super(itemView);

            drugName = itemView.findViewById(R.id.tv_item_name);
            drugPrice = itemView.findViewById(R.id.tv_item_price);
            drugImage = itemView.findViewById(R.id.iv_item_image);
        }
    }
}
