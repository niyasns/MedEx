package com.example.android.medex;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.special.ResideMenu.ResideMenu;

public class HomeFragment extends android.app.Fragment {

    private View parentView;
    private ResideMenu resideMenu;

    Button quiz;
    Button module;
    Button Profile;
    Button Logout;

    TextView heading;


    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.fragment_home, container, false);
        setupViews();
        return parentView;
    }

    private void setupViews() {
        Typeface raleway_bold = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Raleway-Bold.ttf" );
        Typeface raleway_regular = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Raleway-Regular.ttf" );
        HomeActivity parentActivity = (HomeActivity) getActivity();
        Button button = parentActivity.findViewById(R.id.menu_button);
        heading = parentActivity.findViewById(R.id.heading);
        quiz = parentView.findViewById(R.id.quiz);
        module = parentView.findViewById(R.id.modules);
        Profile = parentView.findViewById(R.id.profile);
        Logout = parentView.findViewById(R.id.logout);

        heading.setText(R.string.home);
        quiz.setTypeface(raleway_regular);
        module.setTypeface(raleway_regular);
        Profile.setTypeface(raleway_regular);
        Logout.setTypeface(raleway_regular);

        resideMenu = parentActivity.getResideMenu();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });
    }
}
