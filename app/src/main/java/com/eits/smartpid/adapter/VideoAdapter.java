package com.eits.smartpid.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.eits.smartpid.Interface.FileDelete;
import com.eits.smartpid.R;
import com.eits.smartpid.VideoPlayActivity;
import com.eits.smartpid.model.FileModel;

import java.io.File;
import java.util.ArrayList;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.holder> {
    Context context;
    ArrayList<FileModel> arrayList;
    FileDelete fileDelete;

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

        String filename = "<b>" + arrayList.get(position).getFileName() + "</b>";
        String datetime = "Date & Time : " + "<b>" + arrayList.get(position).getFileDateTime() + "</b>";
        String component = "Component : " + "<b>" + arrayList.get(position).getCompID() + "</b>";
        String location = "Location : " + "<b>" + arrayList.get(position).getFileSiteLocation() + "</b>";
        String facility = "Facility : " + "<b>" + arrayList.get(position).getFacID() + "</b>";
        String duration = "Duration : " + "<b>" + arrayList.get(position).getFileDuration() + "</b>";
        String notes = "Notes : " + "<br>" + "<b>" + "    " + arrayList.get(position).getFileNote() + "</b>";
        String min = "Min : " + "<b>" + arrayList.get(position).getFileMin() + "</b>";
        String max = "Max : " + "<b>" + arrayList.get(position).getFileMax() + "</b>";
        String average = "Average : " + "<b>" + arrayList.get(position).getFileAverage() + "</b>";

        String videoPath = arrayList.get(position).getFilePath();

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


        //Loading Thumbnail of a Video:
        Glide.with(context).load(videoPath).into(holder.fileImage_iv);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, VideoPlayActivity.class);
                intent.putExtra("VIDEO_LINK", videoPath);
                context.startActivity(intent);
            }
        });

        holder.fileDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertdialog = new AlertDialog.Builder(context);
                alertdialog.setMessage("Do you want to delete " + arrayList.get(position).getFileName() + " ?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                File file = new File(videoPath);
                                file.delete();
                                fileDelete.fileDeleted();
                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
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
