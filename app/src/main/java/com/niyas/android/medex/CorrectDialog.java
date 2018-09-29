package com.niyas.android.medex;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Window;

public class CorrectDialog extends Dialog {

    /* Dialog for correct answers */
    Activity mActivity;

    public CorrectDialog(@NonNull Activity activity) {
        super(activity);
        this.mActivity = activity;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.correct_dialog);
    }
}
