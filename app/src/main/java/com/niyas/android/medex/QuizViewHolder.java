package com.niyas.android.medex;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class QuizViewHolder extends RecyclerView.ViewHolder {

    public TextView quizTitle;
    public TextView quizDate;
    public TextView quizTime;
    public TextView quizPrize;

    public QuizViewHolder(View itemView) {
        super(itemView);

        quizTitle = itemView.findViewById(R.id.quiz_title);
        quizDate = itemView.findViewById(R.id.quiz_date);
        quizTime = itemView.findViewById(R.id.quiz_time);
        quizPrize = itemView.findViewById(R.id.quiz_prize);
    }
}
