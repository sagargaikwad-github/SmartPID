package com.eits.smartpid;

import androidx.appcompat.app.AppCompatActivity;
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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.eits.smartpid.Interface.FileDelete;
import com.eits.smartpid.Interface.SortBy_Interface;
import com.eits.smartpid.adapter.SortByAdapter;
import com.eits.smartpid.adapter.VideoAdapter;
import com.eits.smartpid.model.ComponentModel;
import com.eits.smartpid.model.FacilityModel;
import com.eits.smartpid.model.FileModel;
import com.eits.smartpid.model.SQLiteModel;
import com.eits.smartpid.model.sortBy_modelData;
import com.eits.smartpid.model.videoModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;


public class DashboardActivity extends BaseClass implements SortBy_Interface, FileDelete {
    private RecyclerView dashboard_sortBy_RV, dashboard_fileDisplay_RV;
    private Toolbar dashboard_Toolbar;

    private SortByAdapter sortByAdapter;
    private VideoAdapter videoAdapter;

    private ArrayList<sortBy_modelData> sortBy_List = new ArrayList<sortBy_modelData>();
    private ArrayList<FileModel> file_list = new ArrayList<FileModel>();
    private ArrayList<videoModel> video_path_list = new ArrayList<videoModel>();

    File mVideoFolder;
    String metaDataTitle, metaDataComponent, metaDataFacility, metaDataSiteLocation, metaDataNotes, metaDataMin, metaDataMax, metaDataAverage;

    SQLiteModel sqLiteModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


         sqLiteModel=new SQLiteModel(this);

        findID();
        configureToolBar();

        sortBy_List.add(new sortBy_modelData("Date", false));


        createVideoFolder();

    }

    @Override
    protected void onResume() {
        super.onResume();

        startExecution();

//        Thread thread = new Thread(this::makeDataSet);
//        thread.start();
    }

    private void startExecution() {
        file_list.clear();
        video_path_list.clear();

        listVideoFiles();
        makeDataSet();
        setAdapterData();
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

            //    file_list.add(new FileModel(FileName,FiledateTime,null,null,null, null,null,null,null,null,null));

            //  setAdapterData();
            String textfilePath = mVideoFolder + File.separator + "fileName" + i + ".txt";
            File textfilepath = new File(textfilePath);
            String filepathTXT = textfilepath.getAbsolutePath();

            metaDataRead(FileName,FiledateTime,videoPath, filepathTXT);
        }
    }

    private void metaDataRead(String FileName, String FiledateTime, String videoPath, String filepathTXT) {

        String a = " -y -i " + videoPath + " -f ffmetadata " + filepathTXT;

        long executionId = FFmpeg.executeAsync(a, (executionId1, returnCode) -> {
            if (returnCode == Config.RETURN_CODE_SUCCESS) {
                BufferedReader reader;
                try {
                    reader = new BufferedReader(new FileReader(filepathTXT));

                    String line = reader.readLine();
                    while (line != null) {
                        if (line.startsWith("comment")) {
                            String val = line.substring(8);
                            String sDecoded = URLDecoder.decode(val, "UTF-8");

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


                                ArrayList<ComponentModel>comp_list=new ArrayList<>();
                                comp_list=sqLiteModel.getComponentList();

                                ArrayList<FacilityModel>facility_list=new ArrayList<>();
                                facility_list=sqLiteModel.getFacilityList();

                                metaDataTitle = t;
                                metaDataComponent = comp_list.get(c).getCompName();
                                metaDataFacility = facility_list.get(f).getFacName();
                                metaDataSiteLocation = s;
                                metaDataNotes = notes;
                                metaDataMax=max;
                                metaDataMin=min;
                                metaDataAverage=avg;

                                String duration=VideoDuration(videoPath);

                                //   if (FileName.equals(metaDataTitle)) {
                                file_list.add(new FileModel(FileName, FiledateTime, metaDataComponent, metaDataFacility, metaDataSiteLocation, metaDataMin, metaDataMax, metaDataAverage, videoPath, duration, metaDataNotes));
                            //}

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        line = reader.readLine();
                    }
                    reader.close();

                    File file2=new File(filepathTXT);
                    file2.delete();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (returnCode == Config.RETURN_CODE_CANCEL) {
                Log.e("RETURN_CODE_CANCEL",String.valueOf(returnCode));
            } else {
                Log.e("RETURN_CODE_ERROR",String.valueOf(returnCode));
            }
        });
    }

    private String VideoDuration(String videoPath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, Uri.parse(videoPath));
        } catch (Exception e) {
            e.printStackTrace();
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
            Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
            for (File file1 : files) {
                if (file1.getName().endsWith(".mp4")) {
                    video_path_list.add(new videoModel(file1.getPath()));
                }
            }
        }
    }

    private void setAdapterData() {
        dashboard_sortBy_RV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        sortByAdapter = new SortByAdapter(sortBy_List, this);
        dashboard_sortBy_RV.setAdapter(sortByAdapter);


        dashboard_fileDisplay_RV.setLayoutManager(new GridLayoutManager(this, 1));
        videoAdapter = new VideoAdapter(this, file_list,this);
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
        dashboard_Toolbar = findViewById(R.id.dashboard_toolbar);
        dashboard_sortBy_RV = findViewById(R.id.dashboard_sortBy_RV);
        dashboard_fileDisplay_RV = findViewById(R.id.dashboard_fileDisplay_RV);

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

    @Override
    public void fileDeleted() {
        startExecution();
        Toast.makeText(this, "Video Deleted Successfully", Toast.LENGTH_SHORT).show();
    }
}