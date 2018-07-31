package com.example.android.medex;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.special.ResideMenu.ResideMenu;

public class CountDownFragment extends Fragment {

    private View parentView;
    private ResideMenu resideMenu;

    public CountDownFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.countdown_fragment, container, false);
        setupViews();
        return parentView;
    }

    private void setupViews() {
        HomeActivity parentActivity = (HomeActivity) getActivity();
        Button button = parentActivity.findViewById(R.id.menu_button);
        TextView heading = parentActivity.findViewById(R.id.heading);
        heading.setText(R.string.home);
        resideMenu = parentActivity.getResideMenu();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });
    }
}
