package com.example.android.medex;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

class VideoViewHolder extends RecyclerView.ViewHolder{

    public TextView title;
    public TextView sub_title;
    public TextView type;
    public TextView time;
    public ImageButton view;

    public VideoViewHolder(View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.title_item);
        sub_title = itemView.findViewById(R.id.subtitle_item);
        type = itemView.findViewById(R.id.type_item);
        time = itemView.findViewById(R.id.time_item);
        view = itemView.findViewById(R.id.view_item);
    }
}
