package com.eits.smartpid;


import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AlertDialog;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.bumptech.glide.load.engine.Resource;
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
import java.lang.reflect.Field;
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

    //nothingToShow :  if our file_list are empty, that time we use this textView
    //filter_clear : primary this textview visibility are gone. when we apply a filter, so this textview are visible and we can clear all filters in one click.
    //filer_Search : used for navigate filter screen via this textview.
    //filter_count : we added this textview on a filter search textview , if our filterlist has count at that time like filter_clear is visible, this textview also visible.
    TextView nothingToShow, filter_clear, filer_Search, filter_count;


    //Adapter for Sorting a Data.
    private SortByAdapter sortByAdapter;

    //Adapter for Add a Data to RecyclerView.
    private VideoAdapter videoAdapter;

    //Sort Component List :
    private ArrayList<sortBy_modelData> sortBy_List = new ArrayList<sortBy_modelData>();

    //File List which contains FileModel data, used for Recyclerview or Main Data Show.
    private ArrayList<FileModel> file_list = new ArrayList<FileModel>();

    //Video_path_list used for getting a All Video Links which are Download/InspRec folder.
    private ArrayList<videoModel> video_path_list = new ArrayList<videoModel>();


    //When we Filter a Data this Two List are Important.
    ArrayList<Integer> compFilterList = new ArrayList<>();
    ArrayList<Integer> facFilterList = new ArrayList<>();

    //Folder Path making Global : Download/InspRec
    File mVideoFolder;

    //When Retrieve Data from a Video Metadata, we get that data in this components.
    String metaDataTitle, metaDataSiteLocation, metaDataNotes, metaDataMin, metaDataMax, metaDataAverage;
    int metaDataComponent, metaDataFacility;


    SQLiteModel sqLiteModel;
    BottomSheetDialog bottomSheetDialog;
    View decorView;


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

    //We need BottomSheet FullScreen so we will Try to get ScreenSizes.
    int BottomSheetWidth, BottomSheetHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        sqLiteModel = new SQLiteModel(this);


        //We need BottomSheet FullScreen so we will Try to get ScreenSizes.
        BottomSheetWidth = getWindowManager().getDefaultDisplay().getWidth();
        BottomSheetHeight = getWindowManager().getDefaultDisplay().getHeight();

        findID();
        configureToolBar();

        //If We need to add More Sorting Components add Below Line.
        sortBy_List.add(new sortBy_modelData("Date", false));


        //Making a Video Folder to Save a Files, Not necessary this Function Because we already make a
        //Folder in a CameraActivity Screen so this is a Optional.
        createVideoFolder();

        //This Function reads All videos from our Download/InspRec Folder and make a ArrayList's of that.
        startExecution();
    }


    @Override
    protected void onResume() {
        super.onResume();



        decorView = getWindow().getDecorView();

        filter_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Making AlertDialog for confirm to clear Filter

                AlertDialog.Builder alertdialogBuilder = new AlertDialog.Builder(DashboardActivity.this);
                // alertdialogBuilder.create().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

                String message = null;
                //Customization of a Message not needed. we directly gives in alertdialog will also ok.
                if (apply_filter_list.size() > 1) {
                    message = "Do you want to Clear all Filters ?";
                } else {
                    message = "Do you want to Clear Filter ?";
                }

                AlertDialog alertDialog = alertdialogBuilder.setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                //Clearing Applied Filters
                                apply_filter_list.clear();


                                //Update Flters values 0 in Sqlite Tables.
                                sqLiteModel.clearFacFilter();
                                sqLiteModel.clearCompFilter();

                                //Visibility:GONE of clear and count Textviews.
                                filter_count.setVisibility(View.GONE);
                                filter_clear.setVisibility(View.GONE);

                                //Making all List From Scratch, because we clear all Filters.
                                startExecution();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).create();
                alertDialog.show();

                //For making FullScreen or hiding Status and Navigation Bar when Alertdialog Appears:
                alertDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

            }
        });


        //This Filter Search Using Sqlite Filter :
        filer_Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //find_filter_list_from_Sqlite if already some filters are added , we can clear here.
                find_filter_list_from_Sqlite.clear();

                //Which Component and Facility have Filter Value 1 that ID's we can access here.
                // Imp : if Filter value are 1 : selected for filter on a BottomSheet ,  and 0 for Not Selected.
                ArrayList<Integer> compList = sqLiteModel.getCompFilterList();
                ArrayList<Integer> facList = sqLiteModel.getFacFilterList();


                //All Filter Id's we add in a One ArrayList.
                find_filter_list_from_Sqlite.addAll(compList);
                find_filter_list_from_Sqlite.addAll(facList);


                //Bottomsheet for all Filter Selection Stuff.
                bottomSheetDialog = new BottomSheetDialog(DashboardActivity.this);
                //bottomSheetDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

                //Making BottomSheet FullScreen
                bottomSheetDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);

                //For Hide Notification and NavigationBar :
                bottomSheetDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                bottomSheetDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

                //BottomSheetBehavior for customizing BottomSheet
                BottomSheetBehavior<View> bottomSheetBehavior;
                View bottomSheetView = LayoutInflater.from(DashboardActivity.this).inflate(R.layout.bottomsheet_filter, null);
                bottomSheetDialog.setContentView(bottomSheetView);

                bottomSheetBehavior = BottomSheetBehavior.from((View) bottomSheetView.getParent());
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                bottomSheetBehavior.setDraggable(false);
                bottomSheetBehavior.setMaxHeight(BottomSheetHeight);
                bottomSheetBehavior.setMaxWidth(BottomSheetWidth);
                bottomSheetDialog.show();

                // decorView = bottomSheetDialog.getWindow().getDecorView();


                //Our BottomSheet Have Two Recyclerview.
                //WorkFlow of This Recyclerviews : when we click any TitleComponentsRV Item , it will update a content of DataComponentsRV.

                RecyclerView TitleComponentsRV = bottomSheetDialog.findViewById(R.id.bottomSheet_filer_rv_MainComponents);
                RecyclerView DataComponentsRV = bottomSheetDialog.findViewById(R.id.bottomSheet_filer_rv_data_Components);

                //For This TitleComponentList we make a List Here.
                ArrayList<String> filterTitleList = new ArrayList<>();
                filterTitleList.clear();
                filterTitleList.add("Component");
                filterTitleList.add("Facility");

                //We assign a First recyclerview List here that means TitleComponentsRV.
                //and for second DataComponentsRV data, we can update that data when TitleComponentsRV click so it will added in FileTitleAdapter.
                TitleComponentsRV.setLayoutManager(new LinearLayoutManager(DashboardActivity.this, LinearLayoutManager.VERTICAL, false));
                FilterTitleAdapter filterTitleAdapter = new FilterTitleAdapter(filterTitleList, DashboardActivity.this, DataComponentsRV, DashboardActivity.this, find_filter_list_from_Sqlite);
                TitleComponentsRV.setAdapter(filterTitleAdapter);


                //BottomSheet Have Apply and Cancel Buttons so if we choose filter apply or cancel as user convenience.
                Button bottomSheetApply = bottomSheetDialog.findViewById(R.id.bottomsheet_filter_apply_btn);
                Button bottomSheetCancel = bottomSheetDialog.findViewById(R.id.bottomsheet_filter_Cancel_btn);

                bottomSheetApply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetDialog.dismiss();

                        //is already filter Ids in this List , clear that's here, so we can assign new to this List.
                        compFilterList.clear();
                        facFilterList.clear();


                        //Making all To Zero because already added we dont need , we will update every Apply Click
                        sqLiteModel.clearCompFilter();
                        sqLiteModel.clearFacFilter();


                        //dashboard_filter_list get a List from a Adapter via Interface, so we get a Updated every time which filter item we selected on a Adapter Page

                        if (!dashboard_filter_list.isEmpty()) {

                            //This dashboard_filter_list non Empty means we selected some filters, so filter_count and filter_clear are vivible
                            filter_count.setVisibility(View.VISIBLE);
                            filter_clear.setVisibility(View.VISIBLE);

                            //currently we set a text but textsize is 1sp so it will displays like dot.
                            filter_count.setText(String.valueOf(dashboard_filter_list.size()));

                            //Used to Remove Same Id's from a List.
                            // for eg if our list are {1,2,3,3,4,5,5} so when this set used we get {1,2,3,4,5}.
                            Set<Integer> s = new LinkedHashSet<Integer>(dashboard_filter_list);
                            dashboard_filter_list = new ArrayList<>(s);


                            //So we removed a Duplicate ids, now we can update in our sqlite database for this-this ids have filter selected so update that id to filter enable
                            //like making that id Filter column are 1 , so we access anywhere before clear that.
                            //And Most Important we compair if >110 update a updateCompFilter, we can't access both table in single query,
                            // so we categorize if i'th value are greater than 110 update in component table or else update in a Facility Table.
                            for (int i = 0; i < dashboard_filter_list.size(); i++) {
                                if (dashboard_filter_list.get(i) > 110) {
                                    //compFilterList.add(apply_filter_list.get(i));
                                    sqLiteModel.updateCompFilter(dashboard_filter_list.get(i));
                                } else {
                                    //facFilterList.add(apply_filter_list.get(i));
                                    sqLiteModel.updateFacFilter(dashboard_filter_list.get(i));
                                }
                            }

                            //Above function makes filter column update now we can access which are marked are filter or filter=1.
                            compFilterList = sqLiteModel.getCompFilterList();
                            facFilterList = sqLiteModel.getFacFilterList();

                            //apply_filter_list used for next Functionalities, so we assign our dashboard_filter_list which are a Interface list here
                            apply_filter_list = dashboard_filter_list;

                            startExecution();
                        } else {
                            //dashboard_filter_list is empty , so we are here,
                            //so we make all list are clear and getting nonFiltered, whole Data Lists/Sets.

                            apply_filter_list.clear();
                            filter_count.setVisibility(View.GONE);
                            filter_clear.setVisibility(View.GONE);
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

        //Clearing List's for Assign new ones.
        file_list.clear();
        video_path_list.clear();


        //Making Video_path_list() here:
        listVideoFiles();


        //Making file_list from here:
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

        if (video_path_list.isEmpty()) {
            nothingToShow.setVisibility(View.VISIBLE);
            dashboard_fileDisplay_RV.setVisibility(View.GONE);
        } else {

            nothingToShow.setVisibility(View.GONE);
            dashboard_fileDisplay_RV.setVisibility(View.VISIBLE);

            //Reading each Video Path is in video_path_list:
            for (int i = 0; i < video_path_list.size(); i++) {

                //Getting that path in a String.
                String videoPath = video_path_list.get(i).getVideoPath();


                //Making File Access using path.
                File file = new File(videoPath);

                //Getting filename from a File Access.
                String FileName = file.getName();

                //Getting date and time from current VideoPath.
                String FiledateTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").
                        format(new Date(new File(videoPath).lastModified()));


                //Making Temporary TextFile for metadata Related Purpose.
                String textfilePath = mVideoFolder + File.separator + "fileName" + i + ".txt";
                //Making that textfile in a File Access.
                File textfilepath = new File(textfilePath);
                //Geeting exact path of that temporary textFile.
                String filepathTXT = textfilepath.getAbsolutePath();

                //This Function Reads data from a Video
                //Assigned some parameters.
                metaDataRead(FileName, FiledateTime, videoPath, filepathTXT);
            }
        }
    }

    private void metaDataRead(String FileName, String FiledateTime, String videoPath, String filepathTXT) {

        Log.e("metadataread : videopath", videoPath);
        Log.e("metadataread : textfilepath", filepathTXT);


        //We put a videopath in query and assign a texfilepath, because ffmpeg gives a back metadata only in a textfile.
        //so that purpose we make a temporary textfile used.
        String a = " -y -i " + videoPath + " -f ffmetadata " + filepathTXT;

        //FFMpeg execution for getting that videoPath metadata in a textfile.
        long executionId = FFmpeg.executeAsync(a, (executionId1, returnCode) -> {
            if (returnCode == Config.RETURN_CODE_SUCCESS) {
                BufferedReader reader;
                try {

                    //Reading our TextFile.
                    reader = new BufferedReader(new FileReader(filepathTXT));

                    // Our TextFile have a all Metadata.
                    // In that file, 8th Number Line are comment metadata so we read pnly Comment line here.
                    String line = reader.readLine();
                    while (line != null) {
                        if (line.startsWith("comment")) {

                            //Reading 8th Line:
                            String val = line.substring(8);

                            //Decoding That Line :
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
                                metaDataComponent = c;
                                metaDataFacility = f;
                                metaDataSiteLocation = s;
                                metaDataNotes = notes;
                                metaDataMax = max;
                                metaDataMin = min;
                                metaDataAverage = avg;


                                //Getting VideoDuration via videoPath.
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
            // Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
            //Arrays.sort(files, Comparator.comparingLong(File));
            for (File file1 : files) {
                //Ends With MP4 we need that files, so other stuff are added ignored.
                if (file1.getName().endsWith(".mp4")) {
                    //Get Only Path of video , in our arrayList
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
        filter_clear = findViewById(R.id.filter_clear);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        decorView = getWindow().getDecorView();

//        if (!hasFocus) {
//            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//        } else {

        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        // }
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
            // startExecution();
            Collections.shuffle(file_list);
            setAdapterData();
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