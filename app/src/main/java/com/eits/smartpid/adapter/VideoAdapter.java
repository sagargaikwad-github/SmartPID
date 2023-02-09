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


        String TestNote=" a coastal town in the Erongo Region of western Namibia. It is a holiday and retirement settlement, with angling a popular activity, and also serves as a gateway to the seal colony of Cape Cross, which lies 46 kilometres (29 miles) to the north of the town. The town is named after Major Hendrik \"Henty\" Stefanus van der Merwe";
        String filename = "<b>"+arrayList.get(position).getFileName()+"</b>";
        String datetime = "Date & Time : "+"<b>"+arrayList.get(position).getFileDateTime()+"</b>";
        String component = "Component : "+"<b>"+"Flange"+"</b>";
        String location = "Location : "+"<b>"+arrayList.get(position).getFileSiteLocation()+"</b>";
        String facility = "Facility : "+"<b>"+"DNTL"+"</b>";
        String duration = "Duration : "+"<b>"+arrayList.get(position).getFileDuration()+"</b>";
        String notes = "Notes : "+"<br>"+"<b>"+"    "+TestNote+"</b>";
        String min = "Min : "+"<b>"+arrayList.get(position).getFileMin()+"</b>";
        String max = "Max : "+"<b>"+arrayList.get(position).getFileMax()+"</b>";
        String average = "Average : "+"<b>"+arrayList.get(position).getFileAverage()+"</b>";

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
