package com.niyas.android.medex;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CreditsFragment extends android.support.v4.app.Fragment {

    private View parentView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.fragment_credits, container, false);
        setupViews();
        return parentView;
    }

    private void setupViews() {

        HomeActivity parentActivity = (HomeActivity) getActivity();
        Typeface raleway_bold = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Raleway-Bold.ttf" );
        Typeface raleway_regular = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Raleway-Regular.ttf" );
        TextView heading = parentActivity.findViewById(R.id.heading);
        TextView app_name = parentView.findViewById(R.id.application_name);
        TextView dev_name_head = parentView.findViewById(R.id.dev_name_head);
        TextView dev_name = parentView.findViewById(R.id.dev_name);
        TextView contrib_head = parentView.findViewById(R.id.contrib_1_head);
        TextView contrib_1 = parentView.findViewById(R.id.contrib_1);
        TextView contrib_2 = parentView.findViewById(R.id.contrib_2);
        heading.setText("Credits");
        heading.setTypeface(raleway_bold);
        app_name.setTypeface(raleway_bold);
        dev_name_head.setTypeface(raleway_regular);
        dev_name.setTypeface(raleway_regular);
        contrib_head.setTypeface(raleway_regular);
        contrib_1.setTypeface(raleway_regular);
        contrib_2.setTypeface(raleway_regular);
    }
}
