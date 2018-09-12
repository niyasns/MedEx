package com.example.android.medex;

import android.app.Activity;
import android.app.Dialog;
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
