package com.eits.smartpid.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eits.smartpid.Interface.FilterList_Interface;
import com.eits.smartpid.R;
import com.eits.smartpid.model.ComponentModel;
import com.eits.smartpid.model.FacilityModel;
import com.eits.smartpid.model.SQLiteModel;

import java.util.ArrayList;

public class FilterDataAdapter extends RecyclerView.Adapter<FilterDataAdapter.holder> {
    ArrayList<String> arrayList;
    Context context;
    String TitleName;
    SQLiteModel sqLiteModel;
    ArrayList<ComponentModel> ComponentLIST;
    ArrayList<FacilityModel> FacilityLIST;
    FilterList_Interface filterListInterfaceInterface;
    ArrayList<Integer> filteredList_Dashboard = new ArrayList<>();
    ArrayList<Integer> dashboard_list = new ArrayList<>();

    TextView filerCount;
    int FilterTitlePosition;


    public FilterDataAdapter(ArrayList<String> arrayList, Context context, String titleName, FilterList_Interface filterListInterfaceInterface, ArrayList<Integer> dashBoardList, TextView filterCount, int position) {
        this.arrayList = arrayList;
        this.context = context;
        TitleName = titleName;
        this.filterListInterfaceInterface = filterListInterfaceInterface;
        this.filteredList_Dashboard = dashBoardList;
        this.filerCount = filterCount;
        this.FilterTitlePosition=position;
    }

    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.bottomsheet_filter_data_item, parent, false);
        return new holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull holder holder, @SuppressLint("RecyclerView") int position) {

        holder.data_title.setText(arrayList.get(position));

        sqLiteModel = new SQLiteModel(context);
        ComponentLIST = sqLiteModel.getComponentList();
        FacilityLIST = sqLiteModel.getFacilityList();


        if (TitleName.equals("Component")) {
//            int component = position + 1;
            int componentNUM = ComponentLIST.get(position).getCompId();
            if (filteredList_Dashboard.contains(componentNUM)) {
                holder.checkBox.setChecked(true);

                updateScreeen();
            }
        }

        if (TitleName.equals("Facility")) {
            //int facility = position + 1;
            int facilityNUM = FacilityLIST.get(position).getFacID();
            if (filteredList_Dashboard.contains(facilityNUM)) {
                holder.checkBox.setChecked(true);

                updateScreeen();

//                int num = position + 1;
//                int MyNum = FacilityLIST.get(num).getFacID();
//
//                filteredList_Dashboard.add(MyNum);

            }
        }

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TitleName.equals("Component")) {

                    // ComponentLIST = sqLiteModel.getComponentList();
                    int num = position;
                    int MyNum = ComponentLIST.get(num).getCompId();

                    if (!filteredList_Dashboard.contains(MyNum)) {
                        filteredList_Dashboard.add(MyNum);
                        holder.checkBox.setChecked(true);
                        filterListInterfaceInterface.filterList(filteredList_Dashboard);



                    } else {
                        for (int i = 0; i < filteredList_Dashboard.size(); i++) {
                            if (MyNum == filteredList_Dashboard.get(i)) {
                                filteredList_Dashboard.remove(i);
                                holder.checkBox.setChecked(false);
                                filterListInterfaceInterface.filterList(filteredList_Dashboard);

                            }
                        }
                    }
                    updateScreeen();
                }
                if (TitleName.equals("Facility")) {

                    FacilityLIST = sqLiteModel.getFacilityList();
                    int num = position;
//
                    int MyNum = FacilityLIST.get(num).getFacID();

                    if (!filteredList_Dashboard.contains(MyNum)) {
                        filteredList_Dashboard.add(MyNum);
                        holder.checkBox.setChecked(true);
                        filterListInterfaceInterface.filterList(filteredList_Dashboard);
                    } else {

                        for (int i = 0; i < filteredList_Dashboard.size(); i++) {
                            if (MyNum == filteredList_Dashboard.get(i)) {
                                filteredList_Dashboard.remove(i);
                                holder.checkBox.setChecked(false);
                                filterListInterfaceInterface.filterList(filteredList_Dashboard);
                            }
                        }
                    }
                    updateScreeen();
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TitleName.equals("Component")) {
                    int num = position;
                    int MyNum = ComponentLIST.get(num).getCompId();

                    if (!filteredList_Dashboard.contains(MyNum)) {
                        filteredList_Dashboard.add(MyNum);
                        holder.checkBox.setChecked(true);
                        filterListInterfaceInterface.filterList(filteredList_Dashboard);
                    } else {
                        for (int i = 0; i < filteredList_Dashboard.size(); i++) {
                            if (MyNum == filteredList_Dashboard.get(i)) {
                                filteredList_Dashboard.remove(i);
                                holder.checkBox.setChecked(false);
                                filterListInterfaceInterface.filterList(filteredList_Dashboard);
                            }
                        }
                    }
                    updateScreeen();
                }
                if (TitleName.equals("Facility")) {

                    FacilityLIST = sqLiteModel.getFacilityList();
                    int num = position;
//
                    int MyNum = FacilityLIST.get(num).getFacID();

                    if (!filteredList_Dashboard.contains(MyNum)) {
                        filteredList_Dashboard.add(MyNum);
                        holder.checkBox.setChecked(true);
                        filterListInterfaceInterface.filterList(filteredList_Dashboard);

                    } else {
                        for (int i = 0; i < filteredList_Dashboard.size(); i++) {
                            if (MyNum == filteredList_Dashboard.get(i)) {
                                filteredList_Dashboard.remove(i);
                                holder.checkBox.setChecked(false);
                                filterListInterfaceInterface.filterList(filteredList_Dashboard);
                            }
                        }

                    }
                    updateScreeen();
                }


            }
        });

    }

    private void updateScreeen() {
        ArrayList<Integer>tempComp=new ArrayList<>();
        ArrayList<Integer>tempFac=new ArrayList<>();
        for(int i=0;i<filteredList_Dashboard.size();i++)
        {
            if(filteredList_Dashboard.get(i)>110)
            {
                tempComp.add(filteredList_Dashboard.get(i));
            }else{
                tempFac.add(filteredList_Dashboard.get(i));
            }
        }

        if(FilterTitlePosition==0)
        {
            filerCount.setText(String.valueOf(tempComp.size()));
        }else
        {
            filerCount.setText(String.valueOf(tempFac.size()));
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class holder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView data_title;

        public holder(@NonNull View itemView) {
            super(itemView);

            checkBox = itemView.findViewById(R.id.data_checkbox);
            data_title = itemView.findViewById(R.id.data_title);
        }
    }


}
