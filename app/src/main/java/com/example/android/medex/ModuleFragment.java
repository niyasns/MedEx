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
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import java.util.HashMap;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class ModuleFragment extends android.app.Fragment {

    private static final String TAG = "ModuleFragment";
    private View parentView;
    private ResideMenu resideMenu;
    private TextView no_data;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    ProgressBar progressBar;
    RecyclerView recyclerView;
    FrameLayout frameLayout;

    Spinner moduleYear;
    Spinner moduleSubject;
    Spinner moduleTopic;

    String year = null;
    String subject = null;
    Integer yearIndex = null;
    String topic = null;

    ArrayList<Module> moduleArrayList;
    ModuleRecyclerAdapter moduleRecyclerAdapter;
    ArrayList<Year> subjectList;

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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setAdapter();


    }

    private void setAdapter() {

        final Typeface raleway_regular = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Raleway-Regular.ttf" );
        String[] years = {"Year", "Ist MBBS","IInd MBBS", "IIIrd MBBS Part 1", "IIIrd MBBS Part 2" };
        final String[] subjectTemp = {"Subject"};
        final String[] topicTemp = {"Topic"};
        ArrayAdapter<String> subjectTempAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.spinner_item, subjectTemp);
        ArrayAdapter<String> topicTempAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.spinner_item, topicTemp);
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<String>(this.getActivity(), R.layout.spinner_item, years) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v =  super.getView(position, convertView, parent);
                ((TextView) v).setTypeface(raleway_regular);
                return v;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if(position == 0)
                {
                    //Future update
                }
                return view;
            }

            @Override
            public boolean isEnabled(int position) {
                if(position == 0)
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }
        };
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        topicTempAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectTempAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        moduleYear.setAdapter(yearAdapter);
        moduleTopic.setAdapter(topicTempAdapter);
        moduleSubject.setAdapter(subjectTempAdapter);

        moduleYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                year = parent.getItemAtPosition(position).toString().trim();
                switch (year){
                    case "Ist MBBS":
                        handleSubject(0);
                        break;
                    case "IInd MBBS":
                        handleSubject(1);
                        break;
                    case "IIIrd MBBS Part 1":
                        handleSubject(2);
                        break;
                    case "IIIrd MBBS Part 2":
                        handleSubject(3);
                        break;
                    default:
                        Log.d(TAG,"No option selected");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        moduleSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(yearIndex != null) {
                    subject = parent.getItemAtPosition(position).toString().trim();
                    ArrayList<String> topics = subjectList.get(yearIndex).getTopics().get(subject);
                    if(topics.isEmpty()) {
                        ArrayAdapter<String> topicAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, topicTemp);
                        topicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        moduleTopic.setAdapter(topicAdapter);
                    } else {
                        ArrayAdapter<String> topicAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, topics);
                        topicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        moduleTopic.setAdapter(topicAdapter);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        moduleTopic.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                topic = parent.getItemAtPosition(position).toString().trim();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void handleSubject(Integer index) {
        yearIndex = index;
        ArrayList<String> subjects = subjectList.get(index).getSubList();
        if(subjects.isEmpty()) {
            String[] topicTemp = {"Subject"};
            ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, topicTemp);
            subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            moduleSubject.setAdapter(subjectAdapter);
        } else {
            ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, subjects);
            subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            moduleSubject.setAdapter(subjectAdapter);
        }
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

        HomeActivity parentActivity = (HomeActivity) getActivity();
        Button button = parentActivity.findViewById(R.id.menu_button);
        Typeface raleway_bold = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Raleway-Bold.ttf" );
        final Typeface raleway_regular = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Raleway-Regular.ttf" );

        TextView heading = parentActivity.findViewById(R.id.heading);
        heading.setTypeface(raleway_bold);
        heading.setText(R.string.modules);

        progressBar = parentActivity.findViewById(R.id.progressbarHome);
        frameLayout = parentView.findViewById(R.id.recycle_frame);
        moduleYear = parentView.findViewById(R.id.module_year);
        moduleSubject = parentView.findViewById(R.id.module_sub);
        moduleTopic = parentView.findViewById(R.id.module_topic);

        subjectList = HomeActivity.getYearSets();

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
