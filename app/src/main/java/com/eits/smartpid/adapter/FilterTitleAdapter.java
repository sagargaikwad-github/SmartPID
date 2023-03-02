package com.eits.smartpid.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eits.smartpid.Interface.FilterList_Interface;
import com.eits.smartpid.R;
import com.eits.smartpid.model.SQLiteModel;

import java.util.ArrayList;

public class FilterTitleAdapter extends RecyclerView.Adapter<FilterTitleAdapter.holder> {
    ArrayList<String> arrayList;
    Context context;
    RecyclerView recyclerView;
    SQLiteModel sqLiteModel;
    FilterDataAdapter filterDataAdapter;
    ArrayList<String> getList;
    FilterList_Interface filterList_interface;
    ArrayList<Integer> ListFromDashboard;
    int indexposition = 0;


    public FilterTitleAdapter(ArrayList<String> arrayList, Context context, RecyclerView recyclerView, FilterList_Interface filterList_interface, ArrayList<Integer> apply_filter_list) {
        this.arrayList = arrayList;
        this.context = context;
        this.recyclerView = recyclerView;
        this.filterList_interface = filterList_interface;
        this.ListFromDashboard = apply_filter_list;
    }

    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.bottomsheet_filter_title_item, parent, false);
        return new FilterTitleAdapter.holder(view);


    }

    @Override
    public void onBindViewHolder(@NonNull holder holder, @SuppressLint("RecyclerView") int position) {
        holder.textView.setText(arrayList.get(position));

        sqLiteModel = new SQLiteModel(context);
        String title = arrayList.get(position);

        getList = new ArrayList<>();
        getList.clear();

        getList = sqLiteModel.getComponentNames();

        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        filterDataAdapter = new FilterDataAdapter(getList, context, "Component", filterList_interface, ListFromDashboard);
        recyclerView.setAdapter(filterDataAdapter);



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getList.clear();
                indexposition = position;
                notifyDataSetChanged();

                if (title.equals("Component")) {
                    getList = sqLiteModel.getComponentNames();
                    holder.itemView.setBackgroundColor(Color.WHITE);
                    recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
                    filterDataAdapter = new FilterDataAdapter(getList, context, title, filterList_interface, ListFromDashboard);
                    recyclerView.setAdapter(filterDataAdapter);


                }

                if (title.equals("Facility")) {
                    holder.itemView.setBackgroundColor(Color.WHITE);
                    getList = sqLiteModel.getFacilityNames();
                    recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
                    filterDataAdapter = new FilterDataAdapter(getList, context, title, filterList_interface, ListFromDashboard);
                    recyclerView.setAdapter(filterDataAdapter);


                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }


    class holder extends RecyclerView.ViewHolder {
        TextView textView;

        public holder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.filterTitle_from_item);
        }
    }
}
