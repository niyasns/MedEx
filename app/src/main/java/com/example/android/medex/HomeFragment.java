package com.example.android.medex;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.special.ResideMenu.ResideMenu;

public class HomeFragment extends android.app.Fragment implements View.OnClickListener {

    private View parentView;
    private ResideMenu resideMenu;

    HomeActivity parentActivity;

    Button quiz;
    Button module;
    Button Profile;
    Button Logout;

    TextView heading;

    FirebaseAuth mAuth;


    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.fragment_home, container, false);
        setupViews();
        setupFirebase();
        return parentView;
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mAuth.getCurrentUser();

    }

    private void setupViews() {
        Typeface raleway_bold = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Raleway-Bold.ttf" );
        Typeface raleway_regular = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Raleway-Regular.ttf" );
        parentActivity = (HomeActivity) getActivity();
        Button button = parentActivity.findViewById(R.id.menu_button);
        heading = parentActivity.findViewById(R.id.heading);
        quiz = parentView.findViewById(R.id.quiz);
        module = parentView.findViewById(R.id.modules);
        Profile = parentView.findViewById(R.id.profile);
        Logout = parentView.findViewById(R.id.logout);
        parentActivity.stopService(new Intent(parentActivity, BackgroundSoundService.class));

        heading.setText(R.string.home);
        quiz.setTypeface(raleway_regular);
        module.setTypeface(raleway_regular);
        Profile.setTypeface(raleway_regular);
        Logout.setTypeface(raleway_regular);

        quiz.setOnClickListener(this);
        module.setOnClickListener(this);
        Profile.setOnClickListener(this);
        Logout.setOnClickListener(this);

        resideMenu = parentActivity.getResideMenu();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.quiz:
                changeFragment(new CountDownFragment());
                break;
            case R.id.modules:
                changeFragment(new ModuleFragment());
                break;
            case R.id.profile:
                changeFragment(new ProfileFragment());
                break;
            case R.id.logout:
                logOut();
        }
    }

    private void changeFragment(android.app.Fragment targetFragment) {

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransitionStyle(android.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.replace(R.id.frame_window, targetFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void logOut() {
        mAuth.signOut();
        Intent intent = new Intent(parentActivity, MainActivity.class);
        parentActivity.finishAffinity();
        startActivity(intent);
    }
}
