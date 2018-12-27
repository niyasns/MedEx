package com.niyas.android.medex;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class ModuleViewHolder extends RecyclerView.ViewHolder {

    public TextView title;
    public TextView sub_title;
    public TextView type;
    public ImageButton download;
    public ImageButton delete;


    public ModuleViewHolder(View itemView) {
        super(itemView);

        title = itemView.findViewById(R.id.title_item);
        sub_title = itemView.findViewById(R.id.subtitle_item);
        type = itemView.findViewById(R.id.type_item);
        download = itemView.findViewById(R.id.download_item);
        delete = itemView.findViewById(R.id.delete_item);
    }
}
