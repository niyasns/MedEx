package com.example.android.medex;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class CompleteDialog extends Dialog implements View.OnClickListener {

    /* Dailog for quiz completion */
    private Activity mActivity;
    private Button close;

    public CompleteDialog(@NonNull Activity activity) {
        super(activity);
        this.mActivity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.complete_dialog);
        close = findViewById(R.id.btn_close_oncomplete);
        close.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        /* Dismiss dailog and replace fragment */
        dismiss();
        ((AppCompatActivity)mActivity).getSupportFragmentManager().popBackStackImmediate();
        android.support.v4.app.FragmentManager fragmentManager = ((AppCompatActivity)mActivity).getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction  fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransitionStyle(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
    }
}
