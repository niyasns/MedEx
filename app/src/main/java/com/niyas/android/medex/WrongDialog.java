package com.niyas.android.medex;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;

/* Dialog for wrong answer */
public class WrongDialog extends Dialog implements View.OnClickListener {

    Activity mActivity;
    Button close;

    public WrongDialog(@NonNull Activity activity) {
        super(activity);
        this.mActivity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.failed_dialog);
        close = findViewById(R.id.btn_close);
        close.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        dismiss();
        mActivity.stopService(new Intent(mActivity, BackgroundSoundService.class));
        ((AppCompatActivity)mActivity).getSupportFragmentManager().popBackStackImmediate();
        android.support.v4.app.FragmentManager fragmentManager = ((AppCompatActivity)mActivity).getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction  fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransitionStyle(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
    }
}
