package com.eits.smartpid.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.eits.smartpid.R;
import com.eits.smartpid.model.FileModel;

import java.util.ArrayList;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.holder> {
   Context context;
   ArrayList<FileModel> arrayList;


    public VideoAdapter(Context context, ArrayList<FileModel> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
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

        String filename = "File Name : "+"<b>" +arrayList.get(position).getFileName()+ "</b>";
        holder.filename.setText(Html.fromHtml(filename));

        holder.dateTime.setText(arrayList.get(position).getFileDateTime());
        holder.component.setText(String.valueOf(arrayList.get(position).getCompID()));
        holder.location.setText(arrayList.get(position).getFileSiteLocation());
        holder.facility.setText(String.valueOf(arrayList.get(position).getFacID()));
        holder.duration.setText(arrayList.get(position).getFileDuration());
        holder.min.setText(String.valueOf(arrayList.get(position).getFileMin()));
        holder.max.setText(String.valueOf(arrayList.get(position).getFileMax()));
        holder.average.setText(String.valueOf(arrayList.get(position).getFileAverage()));
        holder.notes.setText(arrayList.get(position).getFileNote());



    }

    @Override
    public int getItemCount() {
    return arrayList.size();
    }

    public class holder extends RecyclerView.ViewHolder {
        TextView filename, dateTime, component, location, duration, facility, min, max, average, notes;
        ImageView fileImage_iv;

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
        }
    }

    private boolean videoFileIsCorrupted(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        try {
            retriever.setDataSource(context, Uri.parse(path));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        String hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);
        return "yes".equals(hasVideo);
    }

    private float fileDuration(String absolutePath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, Uri.parse(absolutePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        float d = Float.parseFloat(duration);
        float seconds = (d / 1000);
        retriever.release();
        return seconds;
    }


}
