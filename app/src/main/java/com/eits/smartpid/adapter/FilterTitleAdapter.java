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
    int indexposition=0;


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
        holder.FilterTitle.setText(arrayList.get(position));

        ArrayList<Integer>compList=new ArrayList<>();
        ArrayList<Integer>facList=new ArrayList<>();
        if(!ListFromDashboard.isEmpty())
        {
            for(int i=0;i<ListFromDashboard.size();i++)
            {
                if (ListFromDashboard.get(i) > 110) {
                    //compFilterList.add(apply_filter_list.get(i));
                    compList.add(ListFromDashboard.get(i));
                } else {
                    //facFilterList.add(apply_filter_list.get(i));
                    facList.add(ListFromDashboard.get(i));
                }
            }
        }


        if(position==0)
        {
            holder.FilterCount.setText(String.valueOf(compList.size()));
        }
        else
        {
            holder.FilterCount.setText(String.valueOf(facList.size()));
        }


        sqLiteModel = new SQLiteModel(context);
        String title = arrayList.get(position);

        getList = new ArrayList<>();
        getList.clear();

       // getList = sqLiteModel.getComponentNames();
//        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
//        filterDataAdapter = new FilterDataAdapter(getList, context, "Component", filterList_interface, ListFromDashboard);
//        recyclerView.setAdapter(filterDataAdapter);


        if(indexposition==position)
        {
            holder.itemView.setBackgroundColor(Color.parseColor("#C5F1C9"));
            if(indexposition==0)
            {
                getList = sqLiteModel.getComponentNames();
                recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
                filterDataAdapter = new FilterDataAdapter(getList, context, arrayList.get(position), filterList_interface, ListFromDashboard,holder.FilterCount,position);
                recyclerView.setAdapter(filterDataAdapter);
            }else
            {
                getList = sqLiteModel.getFacilityNames();
                recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
                filterDataAdapter = new FilterDataAdapter(getList, context, arrayList.get(position), filterList_interface, ListFromDashboard,holder.FilterCount,position);
                recyclerView.setAdapter(filterDataAdapter);
            }
        }else
        {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getList.clear();

                indexposition=position;
                notifyDataSetChanged();


//                if (title.equals("Component")) {
//                    getList = sqLiteModel.getComponentNames();
//                    recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
//                    filterDataAdapter = new FilterDataAdapter(getList, context, title, filterList_interface, ListFromDashboard);
//                    recyclerView.setAdapter(filterDataAdapter);
//                }
//
//                if (title.equals("Facility")) {
//                    getList = sqLiteModel.getFacilityNames();
//                    recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
//                    filterDataAdapter = new FilterDataAdapter(getList, context, title, filterList_interface, ListFromDashboard);
//                    recyclerView.setAdapter(filterDataAdapter);
//                }


            }
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }


    class holder extends RecyclerView.ViewHolder {
        TextView FilterTitle,FilterCount;

        public holder(@NonNull View itemView) {
            super(itemView);

            FilterTitle = itemView.findViewById(R.id.filterTitle_from_item);
            FilterCount = itemView.findViewById(R.id.filterTitle_filter_count);
        }
    }
}
