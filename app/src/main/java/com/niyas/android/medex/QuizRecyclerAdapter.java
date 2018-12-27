package com.niyas.android.medex;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class QuizRecyclerAdapter extends RecyclerView.Adapter<QuizViewHolder> {

    private static final String TAG = "QuizRecyclerAdapter";
    private CountDownFragment countDownFragment;
    private List<QuizSet> quizSets;
    private ProgressBar progressBar;

    public QuizRecyclerAdapter(CountDownFragment countDownFragment, List<QuizSet> quizSets,
                               ProgressBar progressBar) {
        this.countDownFragment = countDownFragment;
        this.quizSets = quizSets;
        this.progressBar = progressBar;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(countDownFragment.getActivity());
        View view = layoutInflater.inflate(R.layout.quiz_item, parent, false);

        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {

        holder.quizTitle.setText(quizSets.get(position).getTitle());
        SimpleDateFormat simpleDateFormat  =new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        holder.quizDate.setText(simpleDateFormat.format(quizSets.get(position).getScheduledTime().toDate()));
        SimpleDateFormat simpleTimeFormat  =new SimpleDateFormat("HH:mm", Locale.US);
        holder.quizTime.setText(simpleTimeFormat.format(quizSets.get(position).getScheduledTime().toDate()));
        String prizeMoney = countDownFragment.getString(R.string.Rs) + " " + quizSets.get(position).getPrizeMoney().toString();
        holder.quizPrize.setText(prizeMoney);

        Typeface raleway_bold = Typeface.createFromAsset(countDownFragment.getActivity().getAssets(),"fonts/Raleway-Bold.ttf" );
        Typeface raleway_regular = Typeface.createFromAsset(countDownFragment.getActivity().getAssets(),"fonts/Raleway-Regular.ttf" );

        holder.quizTitle.setTypeface(raleway_bold);
        holder.quizDate.setTypeface(raleway_regular);
        holder.quizTime.setTypeface(raleway_regular);
        holder.quizPrize.setTypeface(raleway_bold);
    }

    @Override
    public int getItemCount() {
        return quizSets.size();
    }
}
