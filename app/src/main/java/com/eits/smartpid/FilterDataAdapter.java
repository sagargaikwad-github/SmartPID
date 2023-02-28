package com.eits.smartpid;

import static com.eits.smartpid.DashboardActivity.apply_filter_list;

import static com.eits.smartpid.DashboardActivity.filteredList_Dashboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eits.smartpid.Interface.FilterList_Interface;
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


    public FilterDataAdapter(ArrayList<String> arrayList, Context context, String titleName, FilterList_Interface filterListInterfaceInterface) {
        this.arrayList = arrayList;
        this.context = context;
        TitleName = titleName;
        this.filterListInterfaceInterface = filterListInterfaceInterface;

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


//        if(TitleName.equals("Component"))
//        {
//            int getFilter = ComponentLIST.get(position + 1).getCompFilter();
//            if (getFilter == 1) {
//                holder.checkBox.setChecked(true);
//            } else {
//
//            }
//        }else
//        {
//            int getFilter = FacilityLIST.get(position + 1).getFacFilter();
//            if (getFilter == 1) {
//                holder.checkBox.setChecked(true);
//            } else {
//
//            }
//        }



        if (TitleName.equals("Component")) {
            int component = position + 1;
            int componentNUM = ComponentLIST.get(component).getCompId();
            if (filteredList_Dashboard.contains(componentNUM)) {
                holder.checkBox.setChecked(true);



//                int num = position + 1;
//                int MyNum = ComponentLIST.get(num).getCompId();
//
//                filteredList_Dashboard.add(MyNum);
            }
        }

        if (TitleName.equals("Facility")) {
            int facility = position + 1;
            int facilityNUM = FacilityLIST.get(facility).getFacID();

            if (filteredList_Dashboard.contains(facilityNUM)) {
                holder.checkBox.setChecked(true);


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
                    int num = position + 1;
                    int MyNum = ComponentLIST.get(num).getCompId();


                    if (!filteredList_Dashboard.contains(MyNum))
                    {
                        //filterList.add(MyNum);
                        //filterListInterfaceInterface.filterList(filterList);
                        filteredList_Dashboard.add(MyNum);
                        holder.checkBox.setChecked(true);
                        //  filterListInterfaceInterface.filterList(filteredList_Dashboard);


                    } else {
                        //filterList.remove(MyNum);
                        // filterListInterfaceInterface.filterList(filterList);

                        for (int i = 0; i < filteredList_Dashboard.size(); i++) {
                            if (MyNum == filteredList_Dashboard.get(i)) {
                                filteredList_Dashboard.remove(i);
                                holder.checkBox.setChecked(false);
                                // filterListInterfaceInterface.filterList(filteredList_Dashboard);
                            }
                        }

                    }
//                    Boolean isAlreadyInFilterList = sqLiteModel.checkFilteredListOrNotComponent(MyNum);
//                    if (isAlreadyInFilterList) {
//                        sqLiteModel.removeInComponentFilter(MyNum);
//                        Toast.makeText(context, "removed Component", Toast.LENGTH_SHORT).show();
//                    } else {
//                        sqLiteModel.addInComponentFilter(MyNum);
//                        Toast.makeText(context, "Added Component", Toast.LENGTH_SHORT).show();
//                    }
                }
                if (TitleName.equals("Facility")) {

                    FacilityLIST = sqLiteModel.getFacilityList();
                    int num = position + 1;
//
                    int MyNum = FacilityLIST.get(num).getFacID();

                    if (!filteredList_Dashboard.contains(MyNum)) {
//                        filterList.add(MyNum);
//                        filterListInterfaceInterface.filterList(filterList);

                        filteredList_Dashboard.add(MyNum);
                        holder.checkBox.setChecked(true);

                    } else {
//                        filterList.remove(MyNum);
//                        filterListInterfaceInterface.filterList(filterList);

                        for (int i = 0; i < filteredList_Dashboard.size(); i++) {
                            if (MyNum == filteredList_Dashboard.get(i)) {
                                filteredList_Dashboard.remove(i);
                                holder.checkBox.setChecked(false);
                            }
                        }

                    }

//                    Boolean isAlreadyInFilterList = sqLiteModel.checkFilteredListOrNotFacility(MyNum);
//                    if (isAlreadyInFilterList) {
//                        sqLiteModel.removeInFacilityFilter(MyNum);
//                        Toast.makeText(context, "removed Facility", Toast.LENGTH_SHORT).show();
//                    } else {
//                        sqLiteModel.addInFacilityFilter(MyNum);
//                        Toast.makeText(context, "Added Facility", Toast.LENGTH_SHORT).show();
//                    }
                }
            }
        });



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TitleName.equals("Component")) {

                    // ComponentLIST = sqLiteModel.getComponentList();
                    int num = position + 1;
                    int MyNum = ComponentLIST.get(num).getCompId();


                    if (!filteredList_Dashboard.contains(MyNum))
                    {
                        //filterList.add(MyNum);
                        //filterListInterfaceInterface.filterList(filterList);
                        filteredList_Dashboard.add(MyNum);
                        holder.checkBox.setChecked(true);
                        //  filterListInterfaceInterface.filterList(filteredList_Dashboard);


                    } else {
                        //filterList.remove(MyNum);
                        // filterListInterfaceInterface.filterList(filterList);

                        for (int i = 0; i < filteredList_Dashboard.size(); i++) {
                            if (MyNum == filteredList_Dashboard.get(i)) {
                                filteredList_Dashboard.remove(i);
                                holder.checkBox.setChecked(false);
                                // filterListInterfaceInterface.filterList(filteredList_Dashboard);
                            }
                        }

                    }
//                    Boolean isAlreadyInFilterList = sqLiteModel.checkFilteredListOrNotComponent(MyNum);
//                    if (isAlreadyInFilterList) {
//                        sqLiteModel.removeInComponentFilter(MyNum);
//                        Toast.makeText(context, "removed Component", Toast.LENGTH_SHORT).show();
//                    } else {
//                        sqLiteModel.addInComponentFilter(MyNum);
//                        Toast.makeText(context, "Added Component", Toast.LENGTH_SHORT).show();
//                    }
                }
                if (TitleName.equals("Facility")) {

                    FacilityLIST = sqLiteModel.getFacilityList();
                    int num = position + 1;
//
                    int MyNum = FacilityLIST.get(num).getFacID();

                    if (!filteredList_Dashboard.contains(MyNum)) {
//                        filterList.add(MyNum);
//                        filterListInterfaceInterface.filterList(filterList);

                        filteredList_Dashboard.add(MyNum);
                        holder.checkBox.setChecked(true);

                    } else {
//                        filterList.remove(MyNum);
//                        filterListInterfaceInterface.filterList(filterList);

                        for (int i = 0; i < filteredList_Dashboard.size(); i++) {
                            if (MyNum == filteredList_Dashboard.get(i)) {
                                filteredList_Dashboard.remove(i);
                                holder.checkBox.setChecked(false);
                            }
                        }

                    }

//                    Boolean isAlreadyInFilterList = sqLiteModel.checkFilteredListOrNotFacility(MyNum);
//                    if (isAlreadyInFilterList) {
//                        sqLiteModel.removeInFacilityFilter(MyNum);
//                        Toast.makeText(context, "removed Facility", Toast.LENGTH_SHORT).show();
//                    } else {
//                        sqLiteModel.addInFacilityFilter(MyNum);
//                        Toast.makeText(context, "Added Facility", Toast.LENGTH_SHORT).show();
//                    }
                }

            }
        });

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
