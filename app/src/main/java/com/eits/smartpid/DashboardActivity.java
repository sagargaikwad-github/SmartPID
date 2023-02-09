package com.eits.smartpid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.eits.smartpid.Interface.SortBy_Interface;
import com.eits.smartpid.adapter.SortByAdapter;
import com.eits.smartpid.adapter.VideoAdapter;
import com.eits.smartpid.model.FileModel;
import com.eits.smartpid.model.sortBy_modelData;

import java.util.ArrayList;


public class DashboardActivity extends AppCompatActivity implements SortBy_Interface {
    private RecyclerView dashboard_sortBy_RV,dashboard_fileDisplay_RV;
    private Toolbar dashboard_Toolbar;

    private SortByAdapter sortByAdapter;
    private VideoAdapter videoAdapter;

    private ArrayList<sortBy_modelData>sortBy_List=new ArrayList<sortBy_modelData>();
    private ArrayList<FileModel>file_list=new ArrayList<FileModel>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        findID();
        configureToolBar();

        sortBy_List.add(new sortBy_modelData("Date",false));

        file_list.add(new FileModel("ECG20230237_141239",
                "02/02/2023 11:22:45 AM",1,1,"Mumbai",10,10,10,
                "InternalStorage","5 Min","ABCDEFG"));
        file_list.add(new FileModel("ECG20102237_141239",
                "02/02/2023 03:45:00 PM",1,1,"Pune",10,10,10,
                "InternalStorage","8 Min","XYZW"));

        setAdapterData();

    }

    private void setAdapterData() {
        dashboard_sortBy_RV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        sortByAdapter = new SortByAdapter(sortBy_List, this);
        dashboard_sortBy_RV.setAdapter(sortByAdapter);


        dashboard_fileDisplay_RV.setLayoutManager(new GridLayoutManager(this, 1));
        videoAdapter=new VideoAdapter(this,file_list);
        dashboard_fileDisplay_RV.setAdapter(videoAdapter);


    }

    private void configureToolBar() {
       dashboard_Toolbar.setTitle("All Files");
       dashboard_Toolbar.setTitleTextColor(Color.WHITE);

       dashboard_Toolbar.setNavigationIcon(R.drawable.ic_back);
       dashboard_Toolbar.setNavigationOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               onBackPressed();
           }
       });


    }

    private void findID() {
        dashboard_Toolbar=findViewById(R.id.dashboard_toolbar);
        dashboard_sortBy_RV=findViewById(R.id.dashboard_sortBy_RV);
        dashboard_fileDisplay_RV=findViewById(R.id.dashboard_fileDisplay_RV);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if (hasFocus) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    @Override
    public void sortBy(String s) {

    }
}