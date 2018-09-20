package com.example.android.medex;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.special.ResideMenu.ResideMenu;

import org.w3c.dom.Text;

import java.net.URL;
import java.util.ArrayList;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class ModuleFragment extends android.app.Fragment {

    private View parentView;
    private ResideMenu resideMenu;
    private TextView no_data;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    ProgressBar progressBar;
    RecyclerView recyclerView;
    FrameLayout frameLayout;

    ArrayList<Module> moduleArrayList;
    ModuleRecyclerAdapter moduleRecyclerAdapter;

    public ModuleFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.fragment_module, container, false);
        setupViews();

        moduleArrayList = new ArrayList<>();

        setupRecyclerView();
        setupFirebase();
        loadDataFromFirestore();

        return parentView;
    }
    /* Loading files data from firebase */
    private void loadDataFromFirestore() {

        progressBar.setVisibility(View.VISIBLE);
        db.collection("files")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){

                            Module module = new Module(queryDocumentSnapshot.getString("fileId"),
                                    queryDocumentSnapshot.getString("fileName"),
                                    queryDocumentSnapshot.getString("subject"),
                                    queryDocumentSnapshot.getTimestamp("time"),
                                    queryDocumentSnapshot.getLong("type"),
                                    queryDocumentSnapshot.getString("url"));

                            moduleArrayList.add(module);
                        }

                        progressBar.setVisibility(View.INVISIBLE);

                        if(moduleArrayList.isEmpty()) {
                            recyclerView.setVisibility(View.INVISIBLE);
                            frameLayout.addView(no_data);
                        } else {
                            moduleRecyclerAdapter = new ModuleRecyclerAdapter(ModuleFragment.this, moduleArrayList, storage, progressBar);
                            recyclerView.setAdapter(moduleRecyclerAdapter);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Loading modules failed", Toast.LENGTH_SHORT).show();
                        Log.v("Loading modules failed", e.getMessage());
                    }
                });
    }
    /* Setting up firebase */
    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }
    /* Setting recycler view for modules */
    private void setupRecyclerView() {

        recyclerView = parentView.findViewById(R.id.module_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    private void setupViews() {
        Typeface raleway_bold = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Raleway-Bold.ttf" );
        final Typeface raleway_regular = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Raleway-Regular.ttf" );
        HomeActivity parentActivity = (HomeActivity) getActivity();
        Button button = parentActivity.findViewById(R.id.menu_button);

        TextView heading = parentActivity.findViewById(R.id.heading);
        heading.setTypeface(raleway_bold);
        heading.setText(R.string.modules);

        progressBar = parentActivity.findViewById(R.id.progressbarHome);
        frameLayout = parentView.findViewById(R.id.recycle_frame);

        no_data = new TextView(parentActivity);
        no_data.setText("No files found");
        no_data.setTextColor(parentActivity.getResources().getColor(R.color.colorTransparentWhite));
        no_data.setTypeface(raleway_regular);
        no_data.setTextSize(24);
        no_data.setGravity(Gravity.CENTER);

        resideMenu = parentActivity.getResideMenu();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });
    }
}
