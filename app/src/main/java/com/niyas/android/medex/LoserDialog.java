package com.niyas.android.medex;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/* Dialog for wrong answer */
public class LoserDialog extends Dialog implements View.OnClickListener {

    private Activity mActivity;
    private Button close;
    private String total, correct;
    private TextView correctAnswers, totalQuestions, lossSlash;

    public LoserDialog(@NonNull Activity activity, String total, String correct) {
        super(activity);
        this.mActivity = activity;
        this.total = total;
        this.correct = correct;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mActivity.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(R.layout.loser_dialog);
        Typeface raleway_bold = Typeface.createFromAsset(mActivity.getAssets(),"fonts/Raleway-Bold.ttf" );
        Typeface raleway_regular = Typeface.createFromAsset(mActivity.getAssets(),"fonts/Raleway-Regular.ttf" );
        TextView dialogheading, dialogSubheading;
        dialogheading = (TextView) findViewById(R.id.loser_dailog_heading);
        dialogSubheading = (TextView) findViewById(R.id.loser_dailog_subheading);
        correctAnswers = findViewById(R.id.correct_answers);
        totalQuestions = findViewById(R.id.total_questions);
        lossSlash = findViewById(R.id.loss_slash);

        close = findViewById(R.id.btn_close_oncomplete);

        dialogheading.setTypeface(raleway_bold);
        dialogSubheading.setTypeface(raleway_regular);
        correctAnswers.setTypeface(raleway_bold);
        totalQuestions.setTypeface(raleway_bold);
        lossSlash.setTypeface(raleway_bold);
        correctAnswers.setText(correct);
        totalQuestions.setText(total);
        close.setTypeface(raleway_bold);

        close.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        dismiss();
        //mActivity.stopService(new Intent(mActivity, BackgroundSoundService.class));
        ((AppCompatActivity)mActivity).getSupportFragmentManager().popBackStackImmediate();
        android.support.v4.app.FragmentManager fragmentManager = ((AppCompatActivity)mActivity).getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction  fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_window, new HomeFragment());
        fragmentTransaction.setTransitionStyle(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
    }

}
