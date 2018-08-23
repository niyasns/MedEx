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

public class CompleteDialog extends Dialog implements View.OnClickListener {

    Activity mActivity;
    Button close;

    public CompleteDialog(@NonNull Activity activity) {
        super(activity);
        this.mActivity = activity;
    }

    public CompleteDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected CompleteDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
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
        dismiss();
        Intent intent = new Intent(mActivity, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivity.startActivity(intent);
    }

    @Override
    public void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> data, @Nullable Menu menu, int deviceId) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
