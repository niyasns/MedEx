package com.example.android.medex;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class VideoRecyclerAdapter extends RecyclerView.Adapter<VideoViewHolder> {

    private static final String TAG = "ModuleRecyclerAdapter";
    private VideosFragment videosFragment;
    private ArrayList<Video> videoArrayList;
    private ProgressBar progressBar;

    private Context context;

    /**
     * ModuleRecyclerAdapter Constructor
     * @param videosFragment fragment instance
     * @param videoArrayList Array list of data
     * @param progressBar progress bar instance
     */
    public VideoRecyclerAdapter(VideosFragment videosFragment, ArrayList<Video> videoArrayList, ProgressBar progressBar) {
        this.videosFragment = videosFragment;
        this.videoArrayList = videoArrayList;
        this.progressBar = progressBar;
        this.context = videosFragment.getActivity();
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(videosFragment.getActivity());
        View view = layoutInflater.inflate(R.layout.video_item, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final VideoViewHolder holder, int position) {

        holder.title.setText(videoArrayList.get(position).getTitle());
        holder.sub_title.setText(videoArrayList.get(position).getSubject());
        holder.type.setText(checkYear(videoArrayList.get(position).getType()));
        SimpleDateFormat simpleDateFormat  =new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);
        holder.time.setText(simpleDateFormat.format(videoArrayList.get(position).getTime().toDate()));

        Typeface raleway_bold = Typeface.createFromAsset(videosFragment.getActivity().getAssets(),"fonts/Raleway-Bold.ttf" );
        Typeface raleway_regular = Typeface.createFromAsset(videosFragment.getActivity().getAssets(),"fonts/Raleway-Regular.ttf" );

        holder.title.setTypeface(raleway_bold);
        holder.sub_title.setTypeface(raleway_regular);
        holder.type.setTypeface(raleway_regular);
        holder.time.setTypeface(raleway_regular);

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openVideo(holder.getAdapterPosition());
            }
        });

    }

    private String checkYear(Long type) {
        if(type == 1) {
            return "Ist MBBS";
        } else if(type == 2) {
            return "IInd MBBS";
        } else if(type == 3) {
            return "IIIrd MBBS Part 1";
        } else if (type == 4) {
            return "IIIrd MBBS Part 2";
        }
        return "No data";
    }

    /**
     * download file
     * @param position position of the file clicked for download.
     */
    private void openVideo(final int position) {

        progressBar.setVisibility(View.VISIBLE);
        Log.i("Opening video", videoArrayList.get(position).getUrl());
        String fileUrl = videoArrayList.get(position).getUrl();
        String videoId = fileUrl.substring(fileUrl.length() - 11);
        Intent videoAppIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
        Intent videoBrowserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl));
        progressBar.setVisibility(View.INVISIBLE);
        try {
            videosFragment.getActivity().startActivity(videoAppIntent);
        } catch (ActivityNotFoundException e) {
            videosFragment.getActivity().startActivity(videoBrowserIntent);
        }

    }

    @Override
    public int getItemCount() {
        return videoArrayList.size();
    }

}
