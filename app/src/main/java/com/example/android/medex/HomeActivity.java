package com.example.android.medex;

import android.app.Fragment;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    ResideMenu resideMenu;

    ResideMenuItem itemHome;
    ResideMenuItem itemModule;
    ResideMenuItem itemProfile;
    ResideMenuItem itemCredits;
    ResideMenuItem itemLogout;

    TextView heading;

    private HomeActivity mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        mContext = this;
        Typeface raleway_bold = Typeface.createFromAsset(this.getAssets(),"fonts/Raleway-Bold.ttf" );
        Typeface raleway_regular = Typeface.createFromAsset(this.getAssets(),"fonts/Raleway-Regular.ttf" );
        heading = findViewById(R.id.heading);
        heading.setTypeface(raleway_bold);
        setupMenu();
        if (savedInstanceState == null) {
            changeFragment(new QuizFragment());
        }
    }


    private void setupMenu() {

        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(R.drawable.reside_background);
        resideMenu.attachToActivity(this);
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);

        itemHome = new ResideMenuItem(this, R.drawable.ic_home, "Home");
        itemModule = new ResideMenuItem(this, R.drawable.ic_module, "Modules");
        itemProfile = new ResideMenuItem(this, R.drawable.ic_profile, "Profile");
        itemCredits = new ResideMenuItem(this, R.drawable.ic_credits, "Credits");
        itemLogout = new ResideMenuItem(this, R.drawable.ic_logout, "Logout");

        itemHome.setOnClickListener(this);
        itemModule.setOnClickListener(this);
        itemProfile.setOnClickListener(this);
        itemCredits.setOnClickListener(this);
        itemLogout.setOnClickListener(this);

        resideMenu.addMenuItem(itemHome, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemModule, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemProfile, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemCredits, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemLogout, ResideMenu.DIRECTION_LEFT);

        findViewById(R.id.menu_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.dispatchTouchEvent(ev);
    }

    @Override
    public void onClick(View v) {

        if (v == itemHome) {
            changeFragment(new QuizFragment());
        } else if (v == itemModule) {
            changeFragment(new CountDownFragment());
        } else if (v == itemProfile) {

        } else if (v == itemCredits) {

        } else if (v == itemLogout) {

        }

        resideMenu.closeMenu();
    }

    private void changeFragment(Fragment targetFragment) {

        resideMenu.clearIgnoredViewList();

        android.app.FragmentManager fragmentManager = getFragmentManager();
        android.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransitionStyle(android.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.add(R.id.frame_window, targetFragment);
        fragmentTransaction.commit();
    }

    public ResideMenu getResideMenu() {
        return resideMenu;
    }
}

