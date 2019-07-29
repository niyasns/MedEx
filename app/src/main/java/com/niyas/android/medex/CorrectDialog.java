package com.niyas.android.medex;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

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
        mActivity.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(R.layout.correct_dialog);

        Typeface raleway_bold = Typeface.createFromAsset(mActivity.getAssets(),"fonts/Raleway-Bold.ttf" );
        Typeface raleway_regular = Typeface.createFromAsset(mActivity.getAssets(),"fonts/Raleway-Regular.ttf" );

        TextView dialogheading, dialogSubheading;
        dialogheading = (TextView) findViewById(R.id.dialog_head_success);
        dialogSubheading = (TextView) findViewById(R.id.dialog_sub_success);

        dialogheading.setTypeface(raleway_bold);
        dialogSubheading.setTypeface(raleway_regular);
    }
}
