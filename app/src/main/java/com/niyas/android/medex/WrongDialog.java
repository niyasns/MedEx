package com.niyas.android.medex;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

/* Dialog for wrong answer */
public class WrongDialog extends Dialog {

    Activity mActivity;

    public WrongDialog(@NonNull Activity activity) {
        super(activity);
        this.mActivity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mActivity.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(R.layout.failed_dialog);
        Typeface raleway_bold = Typeface.createFromAsset(mActivity.getAssets(),"fonts/Raleway-Bold.ttf" );
        Typeface raleway_regular = Typeface.createFromAsset(mActivity.getAssets(),"fonts/Raleway-Regular.ttf" );
        TextView dialogheading, dialogSubheading;
        dialogheading = (TextView) findViewById(R.id.dailog_heading);
        dialogSubheading = (TextView) findViewById(R.id.dailog_subheading);

        dialogheading.setTypeface(raleway_bold);
        dialogSubheading.setTypeface(raleway_regular);
    }

/*    @Override
    public void onClick(View v) {
        dismiss();
        //mActivity.stopService(new Intent(mActivity, BackgroundSoundService.class));
        ((AppCompatActivity)mActivity).getSupportFragmentManager().popBackStackImmediate();
        android.support.v4.app.FragmentManager fragmentManager = ((AppCompatActivity)mActivity).getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction  fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_window, new HomeFragment());
        fragmentTransaction.setTransitionStyle(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
    }*/
}
