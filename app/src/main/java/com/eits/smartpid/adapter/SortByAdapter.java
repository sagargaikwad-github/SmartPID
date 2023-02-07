package com.eits.smartpid.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.eits.smartpid.Interface.SortBy_Interface;
import com.eits.smartpid.R;
import com.eits.smartpid.model.sortBy_modelData;

import java.util.ArrayList;

public class SortByAdapter extends RecyclerView.Adapter<SortByAdapter.holder> {
    ArrayList<sortBy_modelData> arrayList;
    holder lastClickHolder;
    SortBy_Interface sortBy_interface;

    public SortByAdapter(ArrayList<sortBy_modelData> arrayList, SortBy_Interface sortBy_interface) {
        this.arrayList = arrayList;
        this.sortBy_interface = sortBy_interface;
    }

    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.sort_by_item, parent, false);
        return new holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull holder holder, @SuppressLint("RecyclerView") int position) {
        holder.sortBy_TV.setText(arrayList.get(position).getSortByName());
        holder.sortBy_TV_cancel.setText(arrayList.get(position).getSortByName());

        String sortName = arrayList.get(position).getSortByName();


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (lastClickHolder == null) {
                    lastClickHolder = holder;
                    openNew(holder);
                    sortBy_interface.sortBy(arrayList.get(position).getSortByName());
                } else {
                    if (lastClickHolder == holder) {
                        closeSame(lastClickHolder);
                        lastClickHolder = null;
                        sortBy_interface.sortBy(null);
                    } else {
                        closePrevious(lastClickHolder);
                        lastClickHolder = holder;
                        openNew(holder);
                        sortBy_interface.sortBy(arrayList.get(position).getSortByName());
                    }
                }

            }
        });
    }


    private void closeSame(holder lastClickHolder) {
        lastClickHolder.sortBy_TV.setVisibility(View.VISIBLE);
        lastClickHolder.sortBy_TV_cancel.setVisibility(View.GONE);
    }

    private void openNew(holder holder) {
        holder.sortBy_TV.setVisibility(View.GONE);
        holder.sortBy_TV_cancel.setVisibility(View.VISIBLE);
    }

    private void closePrevious(holder lastClickHolder) {
        lastClickHolder.sortBy_TV.setVisibility(View.VISIBLE);
        lastClickHolder.sortBy_TV_cancel.setVisibility(View.GONE);
    }


    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class holder extends RecyclerView.ViewHolder {
        TextView sortBy_TV;
        TextView sortBy_TV_cancel;
        LinearLayout sort_item_LL;

        public holder(@NonNull View itemView) {
            super(itemView);
            sort_item_LL = itemView.findViewById(R.id.sort_by_LL);
            sortBy_TV = itemView.findViewById(R.id.sort_by_item_textView);
            sortBy_TV_cancel = itemView.findViewById(R.id.sort_by_item_textView_cancel);
        }
    }
}
