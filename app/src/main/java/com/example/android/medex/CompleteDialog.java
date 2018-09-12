package com.example.android.medex;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import java.util.List;

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
        mActivity.getFragmentManager().popBackStackImmediate();
        FragmentTransaction fragmentTransaction = mActivity.getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_window, new HomeFragment());
        fragmentTransaction.commit();
    }
}
