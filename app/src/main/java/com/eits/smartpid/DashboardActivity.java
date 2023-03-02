package com.eits.smartpid;


import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.eits.smartpid.Interface.FileDelete;
import com.eits.smartpid.Interface.FilterList_Interface;
import com.eits.smartpid.Interface.SortBy_Interface;
import com.eits.smartpid.adapter.FilterTitleAdapter;
import com.eits.smartpid.adapter.SortByAdapter;
import com.eits.smartpid.adapter.VideoAdapter;
import com.eits.smartpid.model.ComponentModel;
import com.eits.smartpid.model.FacilityModel;
import com.eits.smartpid.model.FileModel;
import com.eits.smartpid.model.SQLiteModel;
import com.eits.smartpid.model.sortBy_modelData;
import com.eits.smartpid.model.videoModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;


public class DashboardActivity extends BaseClass implements SortBy_Interface, FileDelete, FilterList_Interface {
    private RecyclerView dashboard_sortBy_RV, dashboard_fileDisplay_RV;
    private Toolbar dashboard_Toolbar;
    TextView nothingToShow;

    TextView filer_Search, filter_count;

    private SortByAdapter sortByAdapter;
    private VideoAdapter videoAdapter;

    private ArrayList<sortBy_modelData> sortBy_List = new ArrayList<sortBy_modelData>();
    private ArrayList<FileModel> file_list = new ArrayList<FileModel>();
    private ArrayList<videoModel> video_path_list = new ArrayList<videoModel>();

    ArrayList<Integer> compFilterList = new ArrayList<>();
    ArrayList<Integer> facFilterList = new ArrayList<>();

    File mVideoFolder;
    String metaDataTitle, metaDataSiteLocation, metaDataNotes, metaDataMin, metaDataMax, metaDataAverage;
    int metaDataComponent, metaDataFacility;

    SQLiteModel sqLiteModel;
    BottomSheetDialog bottomSheetDialog;


    //For Sqlite Filter
    ArrayList<Integer> apply_filter_list = new ArrayList<>();
    ArrayList<Integer> dashboard_filter_list = new ArrayList<>();
    ArrayList<Integer> find_filter_list_from_Sqlite = new ArrayList<>();

    //For Non Sqlite Filter search Comment Upper Three ArrayList and where apply_filter_list add only apply_tempList:
    //Also Comment Filter_search Button on ClickListener and add CommentOut a Non Sqlite OnClickListener.

    //For NonSqlite Filter
    ArrayList<Integer> apply_tempList = new ArrayList<>();
    ArrayList<Integer> dashboard_tempList = new ArrayList<>();
    ArrayList<Integer> getList_temp = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        sqLiteModel = new SQLiteModel(this);

        findID();
        configureToolBar();

        sortBy_List.add(new sortBy_modelData("Date", false));

        createVideoFolder();
        startExecution();
    }


    @Override
    protected void onResume() {
        super.onResume();


        //This Filter Search Using Sqlite Filter :
        filer_Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                find_filter_list_from_Sqlite.clear();
                ArrayList<Integer> compList = sqLiteModel.getCompFilterList();
                ArrayList<Integer> facList = sqLiteModel.getFacFilterList();

                find_filter_list_from_Sqlite.addAll(compList);
                find_filter_list_from_Sqlite.addAll(facList);


                bottomSheetDialog = new BottomSheetDialog(DashboardActivity.this);
                bottomSheetDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

                BottomSheetBehavior<View> bottomSheetBehavior;
                View bottomSheetView = LayoutInflater.from(DashboardActivity.this).inflate(R.layout.bottomsheet_filter, null);
                bottomSheetDialog.setContentView(bottomSheetView);

                bottomSheetBehavior = BottomSheetBehavior.from((View) bottomSheetView.getParent());
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                bottomSheetBehavior.setDraggable(false);
                bottomSheetBehavior.setMaxWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                bottomSheetDialog.show();


                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);

                RecyclerView TitleComponentsRV = bottomSheetDialog.findViewById(R.id.bottomSheet_filer_rv_MainComponents);
                RecyclerView DataComponentsRV = bottomSheetDialog.findViewById(R.id.bottomSheet_filer_rv_data_Components);


                ArrayList<String> filterTitleList = new ArrayList<>();
                filterTitleList.clear();

                filterTitleList.add("Component");
                filterTitleList.add("Facility");

                TitleComponentsRV.setLayoutManager(new LinearLayoutManager(DashboardActivity.this, LinearLayoutManager.VERTICAL, false));
                FilterTitleAdapter filterTitleAdapter = new FilterTitleAdapter(filterTitleList, DashboardActivity.this, DataComponentsRV, DashboardActivity.this, find_filter_list_from_Sqlite);
                TitleComponentsRV.setAdapter(filterTitleAdapter);

                Button bottomSheetApply = bottomSheetDialog.findViewById(R.id.bottomsheet_filter_apply_btn);
                Button bottomSheetCancel = bottomSheetDialog.findViewById(R.id.bottomsheet_filter_Cancel_btn);

                bottomSheetApply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetDialog.dismiss();
                        compFilterList.clear();
                        facFilterList.clear();

                        sqLiteModel.clearCompFilter();
                        sqLiteModel.clearFacFilter();

                        if (!dashboard_filter_list.isEmpty()) {
                            filter_count.setVisibility(View.VISIBLE);
                            filter_count.setText(String.valueOf(dashboard_filter_list.size()));

                            Set<Integer> s = new LinkedHashSet<Integer>(dashboard_filter_list);
                            dashboard_filter_list = new ArrayList<>(s);

                            for (int i = 0; i < dashboard_filter_list.size(); i++) {
                                if (dashboard_filter_list.get(i) > 110) {
                                    //compFilterList.add(apply_filter_list.get(i));
                                    sqLiteModel.updateCompFilter(dashboard_filter_list.get(i));
                                } else {
                                    //facFilterList.add(apply_filter_list.get(i));
                                    sqLiteModel.updateFacFilter(dashboard_filter_list.get(i));
                                }
                            }
                            compFilterList = sqLiteModel.getCompFilterList();
                            facFilterList = sqLiteModel.getFacFilterList();

                            apply_filter_list = dashboard_filter_list;

                            startExecution();
                        } else {
                            apply_filter_list.clear();
                            filter_count.setVisibility(View.GONE);
                            compFilterList.clear();
                            facFilterList.clear();
                            startExecution();
                        }
                    }
                });

                bottomSheetCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetDialog.dismiss();
                    }
                });
            }
        });


        //This Filter Search Using Non Sqlite Filter :


//        filer_Search.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////              if (!apply_tempList.isEmpty()) {
//                    getList_temp = apply_tempList;
////                } else {
////                    getList_temp = dashboard_tempList;
////                }
//
//                bottomSheetDialog = new BottomSheetDialog(DashboardActivity.this);
//                bottomSheetDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
//
//                BottomSheetBehavior<View> bottomSheetBehavior;
//                View bottomSheetView = LayoutInflater.from(DashboardActivity.this).inflate(R.layout.bottomsheet_filter, null);
//                bottomSheetDialog.setContentView(bottomSheetView);
//
//                bottomSheetBehavior = BottomSheetBehavior.from((View) bottomSheetView.getParent());
//                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//                bottomSheetBehavior.setDraggable(false);
//                bottomSheetBehavior.setMaxWidth(ViewGroup.LayoutParams.MATCH_PARENT);
//                bottomSheetDialog.show();
//
//                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
//
//                RecyclerView TitleComponentsRV = bottomSheetDialog.findViewById(R.id.bottomSheet_filer_rv_MainComponents);
//                RecyclerView DataComponentsRV = bottomSheetDialog.findViewById(R.id.bottomSheet_filer_rv_data_Components);
//
//
//                ArrayList<String> filterTitleList = new ArrayList<>();
//                filterTitleList.clear();
//
//                filterTitleList.add("Component");
//                filterTitleList.add("Facility");
//
//                TitleComponentsRV.setLayoutManager(new LinearLayoutManager(DashboardActivity.this, LinearLayoutManager.VERTICAL, false));
//                FilterTitleAdapter filterTitleAdapter = new FilterTitleAdapter(filterTitleList, DashboardActivity.this, DataComponentsRV, DashboardActivity.this, getList_temp);
//                TitleComponentsRV.setAdapter(filterTitleAdapter);
//
//                Button bottomSheetApply = bottomSheetDialog.findViewById(R.id.bottomsheet_filter_apply_btn);
//                Button bottomSheetCancel = bottomSheetDialog.findViewById(R.id.bottomsheet_filter_Cancel_btn);
//
//                bottomSheetApply.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        bottomSheetDialog.dismiss();
//                        compFilterList.clear();
//                        facFilterList.clear();
//
//                        apply_tempList = dashboard_tempList;
//
//                        if (!apply_tempList.isEmpty()) {
//                            filter_count.setVisibility(View.VISIBLE);
//                            filter_count.setText(String.valueOf(dashboard_tempList.size()));
//
//                            Set<Integer> s = new LinkedHashSet<Integer>(apply_tempList);
//                            apply_tempList = new ArrayList<>(s);
//
//                            for (int i = 0; i < apply_tempList.size(); i++) {
//                                if (apply_tempList.get(i) > 110) {
//                                    compFilterList.add(apply_tempList.get(i));
//                                } else {
//                                    facFilterList.add(apply_tempList.get(i));
//                                }
//                            }
//
//                            startExecution();
//                        } else {
//                            filter_count.setVisibility(View.GONE);
//                            compFilterList.clear();
//                            facFilterList.clear();
//                            startExecution();
//                        }
//                    }
//                });
//
//                bottomSheetCancel.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        bottomSheetDialog.dismiss();
//
//                    }
//                });
//            }
//        });
    }

    private void startExecution() {

        file_list.clear();
        video_path_list.clear();

        listVideoFiles();


        //Making Data for Files
        makeDataSet();


    }

    private void createVideoFolder() {
        File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        mVideoFolder = new File(movieFile, "InspRec");
        if (!mVideoFolder.exists()) {
            mVideoFolder.mkdirs();
        }
    }

    private void makeDataSet() {
        for (int i = 0; i < video_path_list.size(); i++) {
            String videoPath = video_path_list.get(i).getVideoPath();

            File file = new File(videoPath);
            String FileName = file.getName();

            String FiledateTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").
                    format(new Date(new File(videoPath).lastModified()));


            String textfilePath = mVideoFolder + File.separator + "fileName" + i + ".txt";
            File textfilepath = new File(textfilePath);
            String filepathTXT = textfilepath.getAbsolutePath();


            //This Function Reads data from a Video
            metaDataRead(FileName, FiledateTime, videoPath, filepathTXT);
        }
    }

    private void metaDataRead(String FileName, String FiledateTime, String videoPath, String filepathTXT) {

        Log.e("metadataread : videopath", videoPath);
        Log.e("metadataread : textfilepath", filepathTXT);


        //We make a TextFile in Download folder via our metadata .. here filePathTXT are textFile
        String a = " -y -i " + videoPath + " -f ffmetadata " + filepathTXT;


        long executionId = FFmpeg.executeAsync(a, (executionId1, returnCode) -> {
            if (returnCode == Config.RETURN_CODE_SUCCESS) {
                BufferedReader reader;
                try {
                    reader = new BufferedReader(new FileReader(filepathTXT));

                    // we read Text file for metadata which are we saving . in that file, 8th Line are comment metadata so we read that line and get 8th Line

                    String line = reader.readLine();
                    while (line != null) {
                        if (line.startsWith("comment")) {

                            //Reading 8th Line:
                            String val = line.substring(8);

                            //Decoding That Data:
                            String sDecoded = URLDecoder.decode(val, "UTF-8");


                            //Our Metadata is in Json so we access from here :
                            try {
                                JSONObject jObj = new JSONObject(sDecoded);
                                String t = jObj.getString("videoTitle");
                                int c = jObj.getInt("component");
                                int f = jObj.getInt("facility");
                                String s = jObj.getString("sitelocation");
                                String notes = jObj.getString("notes");
                                String max = jObj.getString("max");
                                String min = jObj.getString("min");
                                String avg = jObj.getString("average");

                                ArrayList<ComponentModel> comp_list = new ArrayList<>();
                                comp_list = sqLiteModel.getComponentList();

                                ArrayList<FacilityModel> facility_list = new ArrayList<>();
                                facility_list = sqLiteModel.getFacilityList();

                                metaDataTitle = t;
                                metaDataComponent = comp_list.get(c).getCompId();
                                metaDataFacility = facility_list.get(f).getFacID();
                                metaDataSiteLocation = s;
                                metaDataNotes = notes;
                                metaDataMax = max;
                                metaDataMin = min;
                                metaDataAverage = avg;

                                String duration = VideoDuration(videoPath);


                                //apply_filter_list not empty means we have selected some filters.
                                if (!apply_filter_list.isEmpty()) {
                                    if (compFilterList.size() >= facFilterList.size()) {
                                        for (int i = 0; i < compFilterList.size(); i++) {
                                            if (metaDataComponent == compFilterList.get(i)) {
                                                if (!facFilterList.isEmpty()) {
                                                    for (int j = 0; j < facFilterList.size(); j++) {
                                                        if (metaDataFacility == facFilterList.get(j)) {
                                                            file_list.add(new FileModel(FileName, FiledateTime, metaDataComponent, metaDataFacility, metaDataSiteLocation, metaDataMin, metaDataMax, metaDataAverage, videoPath, duration, metaDataNotes));
                                                        }
                                                    }
                                                } else {
                                                    file_list.add(new FileModel(FileName, FiledateTime, metaDataComponent, metaDataFacility, metaDataSiteLocation, metaDataMin, metaDataMax, metaDataAverage, videoPath, duration, metaDataNotes));
                                                }
                                            }
                                        }
                                    } else {
                                        for (int i = 0; i < facFilterList.size(); i++) {
                                            if (metaDataFacility == facFilterList.get(i)) {
                                                if (!compFilterList.isEmpty()) {
                                                    for (int j = 0; j < compFilterList.size(); j++) {
                                                        if (metaDataComponent == compFilterList.get(j)) {
                                                            file_list.add(new FileModel(FileName, FiledateTime, metaDataComponent, metaDataFacility, metaDataSiteLocation, metaDataMin, metaDataMax, metaDataAverage, videoPath, duration, metaDataNotes));
                                                        }
                                                    }
                                                } else {
                                                    file_list.add(new FileModel(FileName, FiledateTime, metaDataComponent, metaDataFacility, metaDataSiteLocation, metaDataMin, metaDataMax, metaDataAverage, videoPath, duration, metaDataNotes));
                                                }
                                            }
                                        }
                                    }

                                }
                                //Else makes whole data arraylist whole data.
                                else {
                                    file_list.add(new FileModel(FileName, FiledateTime, metaDataComponent, metaDataFacility, metaDataSiteLocation, metaDataMin, metaDataMax, metaDataAverage, videoPath, duration, metaDataNotes));
                                }

                            } catch (JSONException e) {
                                Log.e("Error1", e.toString());
                            }
                        }
                        line = reader.readLine();
                    }
                    reader.close();


                    //After each File Read that Text File Will Deleted
                    File file2 = new File(filepathTXT);
                    file2.delete();


                    //Set Adapter Data
                    setAdapterData();

                } catch (IOException e) {
                    Log.e("Error2", e.toString());
                }
            } else if (returnCode == Config.RETURN_CODE_CANCEL) {
                Log.e("RETURN_CODE_CANCEL", String.valueOf(returnCode));
            } else {
                Log.e("RETURN_CODE_ERROR", String.valueOf(returnCode));
            }
        });
    }

    private String VideoDuration(String videoPath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, Uri.parse(videoPath));
        } catch (Exception e) {
            Log.e("Error3", e.toString());

        }
        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int dur = Integer.parseInt(duration);
        int seconds = (dur / 1000);
        retriever.release();

        int second = seconds % 60;
        int minute = seconds / 60 % 60;
        int hour = seconds / (60 * 60) % 24;

        if (hour > 0) {
            duration = hour + " hr " + minute + " min";
        } else if (minute > 0) {
            duration = minute + " min " + second + " sec";
        } else {
            duration = second + " sec";
        }
        return duration;
    }

    private void listVideoFiles() {
        String Path = "/storage/emulated/0/Download/InspRec";
        File file = new File(Path);
        File[] files = file.listFiles();

        if (files != null) {
            //  Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
            //Arrays.sort(files, Comparator.comparingLong(File));
            for (File file1 : files) {
                if (file1.getName().endsWith(".mp4")) {
                    video_path_list.add(new videoModel(file1.getPath()));
                    Log.e("VideoPath : ", file1.getAbsolutePath());
                }
            }
        }
    }

    private void setAdapterData() {
        dashboard_sortBy_RV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        sortByAdapter = new SortByAdapter(sortBy_List, this);
        dashboard_sortBy_RV.setAdapter(sortByAdapter);


        //file_list empty ; mostly use for Applied filter have null Results
        if (file_list.isEmpty()) {
            dashboard_fileDisplay_RV.setVisibility(View.GONE);
            nothingToShow.setVisibility(View.VISIBLE);
        } else {
            nothingToShow.setVisibility(View.GONE);
            dashboard_fileDisplay_RV.setVisibility(View.VISIBLE);
            dashboard_fileDisplay_RV.setLayoutManager(new GridLayoutManager(this, 1));
            videoAdapter = new VideoAdapter(this, file_list, this);
            dashboard_fileDisplay_RV.setAdapter(videoAdapter);
        }

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
        dashboard_Toolbar = findViewById(R.id.dashboard_toolbar);
        dashboard_sortBy_RV = findViewById(R.id.dashboard_sortBy_RV);
        dashboard_fileDisplay_RV = findViewById(R.id.dashboard_fileDisplay_RV);

        filer_Search = findViewById(R.id.filer_Search);
        nothingToShow = findViewById(R.id.nothingToShow);
        filter_count = findViewById(R.id.filter_count);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
//        if (hasFocus) {
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        //  }
    }

    @Override
    public void sortBy(String s) {
        ArrayList<FileModel> dateSortList = new ArrayList<>(file_list);

        if (s == "Date") {
            FindSortList(dateSortList, "Date");
            file_list = dateSortList;

            if (file_list.isEmpty()) {
                dashboard_fileDisplay_RV.setVisibility(View.GONE);
                nothingToShow.setVisibility(View.VISIBLE);
            } else {
                nothingToShow.setVisibility(View.GONE);
                dashboard_fileDisplay_RV.setVisibility(View.VISIBLE);
                dashboard_fileDisplay_RV.setLayoutManager(new GridLayoutManager(this, 1));
                videoAdapter = new VideoAdapter(this, file_list, this);
                dashboard_fileDisplay_RV.setAdapter(videoAdapter);
            }
//           videoAdapter = new VideoAdapter(this, dateSortList,this::fileDeleted);
//           dashboard_fileDisplay_RV.setAdapter(videoAdapter);
        } else {
            startExecution();
        }
    }

    public void FindSortList(ArrayList<FileModel> arrayList, String name) {
        Collections.sort(arrayList, new Comparator<FileModel>() {
            @Override
            public int compare(FileModel fileData, FileModel t1) {
                return t1.getFileDateTime().compareTo(fileData.getFileDateTime());
            }
        });
    }

    @Override
    public void fileDeleted() {
        startExecution();
        Toast.makeText(this, "Video Deleted Successfully", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        sqLiteModel.clearFacFilter();
        sqLiteModel.clearCompFilter();
    }


    //Interface for Getting List from FilterDataAdapter :
    @Override
    public void filterList(ArrayList<Integer> arrayList) {
        dashboard_filter_list = arrayList;

//        Set<Integer> s = new LinkedHashSet<Integer>(arrayList);
//        arrayList = new ArrayList<>(s);
//
//        dashboard_tempList = arrayList;
//        for (int i = 0; i < dashboard_tempList.size(); i++) {
//            Log.e("TAG", dashboard_tempList.get(i).toString());
//        }
    }
}