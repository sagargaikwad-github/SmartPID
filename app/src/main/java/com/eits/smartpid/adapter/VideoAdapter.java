package com.eits.smartpid.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.eits.smartpid.Interface.FileDelete;
import com.eits.smartpid.R;
import com.eits.smartpid.VideoPlayActivity;
import com.eits.smartpid.model.ComponentModel;
import com.eits.smartpid.model.FacilityModel;
import com.eits.smartpid.model.FileModel;
import com.eits.smartpid.model.SQLiteModel;

import java.io.File;
import java.util.ArrayList;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.holder> {
    Context context;
    ArrayList<FileModel> arrayList;
    FileDelete fileDelete;

    String videoPath;
    SQLiteModel sqLiteModel;

    int CompID;
    int FacID;
    int MyReturn;


    public VideoAdapter(Context context, ArrayList<FileModel> arrayList, FileDelete fileDelete) {
        this.context = context;
        this.arrayList = arrayList;
        this.fileDelete = fileDelete;

    }

    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.filedata_item, parent, false);
        return new holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull holder holder, @SuppressLint("RecyclerView") int position) {

        sqLiteModel = new SQLiteModel(context);
        ArrayList<ComponentModel> comp_list = new ArrayList<>();
        comp_list = sqLiteModel.getComponentList();

        ArrayList<FacilityModel> facility_list = new ArrayList<>();
        facility_list = sqLiteModel.getFacilityList();

        CompID = arrayList.get(position).getCompID();
        FacID = arrayList.get(position).getFacID();

        String filename = "<b>" + arrayList.get(position).getFileName() + "</b>";
        String datetime = "Date & Time : " + "<b>" + arrayList.get(position).getFileDateTime() + "</b>";
        String component = "Component : " + "<b>" + sqLiteModel.getComponentName(CompID) + "</b>";
        String location = "Location : " + "<b>" + arrayList.get(position).getFileSiteLocation() + "</b>";
        String facility = "Facility : " + "<b>" + sqLiteModel.getFacilityName(FacID) + "</b>";
        String duration = "Duration : " + "<b>" + arrayList.get(position).getFileDuration() + "</b>";
        String notes = "Notes : " + "<br>" + "<b>" + "    " + arrayList.get(position).getFileNote() + "</b>";
        String min = "Min : " + "<b>" + arrayList.get(position).getFileMin() + "</b>";
        String max = "Max : " + "<b>" + arrayList.get(position).getFileMax() + "</b>";
        String average = "Average : " + "<b>" + arrayList.get(position).getFileAverage() + "</b>";

        videoPath = arrayList.get(position).getFilePath();


        holder.filename.setText(Html.fromHtml(filename));
        holder.dateTime.setText(Html.fromHtml(datetime));
        holder.component.setText(Html.fromHtml(component));
        holder.location.setText(Html.fromHtml(location));
        holder.facility.setText(Html.fromHtml(facility));
        holder.duration.setText(Html.fromHtml(duration));
        holder.notes.setText(Html.fromHtml(notes));
        holder.min.setText(Html.fromHtml(min));
        holder.max.setText(Html.fromHtml(max));
        holder.average.setText(Html.fromHtml(average));

        Glide.with(context).load(videoPath).into(holder.fileImage_iv);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String FilePath = arrayList.get(position).getFilePath();
                Intent intent = new Intent(context, VideoPlayActivity.class);
                intent.putExtra("VIDEO_LINK", FilePath);
                context.startActivity(intent);
            }
        });

        holder.fileDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String FilePath = arrayList.get(position).getFilePath();
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                //alertdialog.create().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

                AlertDialog alertDialog = alertDialogBuilder.setMessage("Do you want to delete " + arrayList.get(position).getFileName() + " ?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                File file = new File(FilePath);
                                file.delete();
                                fileDelete.fileDeleted();
                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).create();
                alertDialog.show();
                alertDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

            }
        });

    }


    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class holder extends RecyclerView.ViewHolder {
        TextView filename, dateTime, component, location, duration, facility, min, max, average, notes;
        ImageView fileImage_iv, fileDelete;

        public holder(@NonNull View itemView) {
            super(itemView);
            fileImage_iv = itemView.findViewById(R.id.fileImage_iv);
            filename = itemView.findViewById(R.id.fileName_TV);
            dateTime = itemView.findViewById(R.id.fileDateTime_TV);
            component = itemView.findViewById(R.id.fileComponent_TV);
            location = itemView.findViewById(R.id.fileSiteLocation_TV);
            facility = itemView.findViewById(R.id.fileFacility_TV);

            duration = itemView.findViewById(R.id.fileDuration_TV);
            min = itemView.findViewById(R.id.fileMIN);
            max = itemView.findViewById(R.id.fileMax);
            average = itemView.findViewById(R.id.fileAverage);
            notes = itemView.findViewById(R.id.fileNotes_TV);
            fileDelete = itemView.findViewById(R.id.fileDelete);
        }

    }


}

