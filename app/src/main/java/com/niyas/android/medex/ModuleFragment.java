package com.niyas.android.medex;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;

public class ModuleFragment extends android.support.v4.app.Fragment {

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
    Button filterButton;

    String year = null;
    String subject = "Subject";
    Integer yearIndex = null;
    Integer yearFirebase = 1;
    String topic = "Topic";

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
        handleFilter();

        return parentView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    private void handleFilter() {
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!subject.equals("Subject") && !topic.equals("Topic")) {

                    final ArrayList<Module> moduleArrayListNew = new ArrayList<>();
                    progressBar.setVisibility(View.VISIBLE);
                    db.collection("files")
                            .whereEqualTo("type",yearFirebase)
                            .whereEqualTo("subject", subject)
                            .whereEqualTo("topic", topic)
                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                Log.d(TAG, queryDocumentSnapshot.toString());
                                Module module = new Module(queryDocumentSnapshot.getString("fileId"),
                                        queryDocumentSnapshot.getString("fileName"),
                                        queryDocumentSnapshot.getString("subject"),
                                        queryDocumentSnapshot.getTimestamp("time"),
                                        queryDocumentSnapshot.getLong("type"),
                                        queryDocumentSnapshot.getString("url"));

                                moduleArrayListNew.add(module);

                            }
                            Log.d(TAG, moduleArrayListNew.toString());
                            progressBar.setVisibility(View.INVISIBLE);

                            if(moduleArrayListNew.isEmpty()) {
                                recyclerView.setVisibility(View.INVISIBLE);
                                frameLayout.removeAllViews();
                                frameLayout.addView(no_data);
                                no_data.setVisibility(View.VISIBLE);
                            } else {
                                Log.d(TAG, "Entered Recycler refreshing");
                                no_data.setVisibility(View.INVISIBLE);
                                frameLayout.removeAllViews();
                                frameLayout.addView(recyclerView);
                                recyclerView.setVisibility(View.VISIBLE);
                                moduleRecyclerAdapter = new ModuleRecyclerAdapter(getActivity(), moduleArrayListNew, storage, progressBar);
                                recyclerView.setAdapter(moduleRecyclerAdapter);
                            }
                        }
                    });
                }
            }
        });
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
                    if(!subjectList.isEmpty()) {
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
        yearFirebase = index + 1;
        if(!subjectList.isEmpty() && index < subjectList.size()) {
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
        } else {
            Toast.makeText(getActivity(), "Invalid subject", Toast.LENGTH_SHORT).show();
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
                            frameLayout.removeAllViews();
                            frameLayout.addView(no_data);
                        } else {
                            moduleRecyclerAdapter = new ModuleRecyclerAdapter(getActivity(), moduleArrayList, storage, progressBar);
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
        filterButton = parentView.findViewById(R.id.filter_button);

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
