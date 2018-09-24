package com.example.android.medex;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

import java.security.cert.CertificateNotYetValidException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "HomeActivity";
    TextView heading;
    @SuppressLint("StaticFieldLeak")
    static ProgressBar progressBar;
    /* Reside Menu navigation variables */
    @SuppressLint("StaticFieldLeak")
    static ResideMenu resideMenu;
    ResideMenuItem itemHome;
    ResideMenuItem itemQuiz;
    ResideMenuItem itemModule;
    ResideMenuItem itemProfile;
    ResideMenuItem itemLogout;
    ResideMenuItem itemCredits;
    /* Firebase variables */
    FirebaseFirestore db;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth mAuth;
    /* List to load quizes available from the database.*/
    static ArrayList<QuizSet> quizSets;
    /*List to load year details for module fragment */
    static ArrayList<Year> yearList;
    /*List to load year details for module fragment */
    static ArrayList<Year> tempList;
    /* Boolean to identify initial application instance. */
    boolean isInital = true;
    /* Intent for background sound service*/
    Intent svc;
    private float x1, x2;
    static final int MIN_DISTANCE = 200;
    private long startClickTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        initView();
        initQuizList();
        initAdView();
        setupMenu();
        setupFirebase();
        /* Load home fragemnt initially */
        if (savedInstanceState == null) {
            changeFragment(new HomeFragment());
        }
        /* Listener to display reside menu onclick */
        findViewById(R.id.menu_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });
    }
    /* Initialzing view for the activity */
    private void initView() {

        progressBar = findViewById(R.id.progressbarHome);
        Typeface raleway_bold = Typeface.createFromAsset(this.getAssets(),"fonts/Raleway-Bold.ttf" );
        heading = findViewById(R.id.heading);
        heading.setTypeface(raleway_bold);
    }
    /* Initializing new array list for loading quiz details */
    private void initQuizList() {

        quizSets = new ArrayList<>();
        yearList = new ArrayList<>();
        tempList =new ArrayList<>();
        quizSets.clear();
        yearList.clear();
    }
    /* Initializing AdView */
    private void initAdView() {
        MobileAds.initialize(this, "ca-app-pub-5476381757988116~3744426550");
        AdView mAdview = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdview.loadAd(adRequest);
    }

    @Override
    protected void onStart() {
        super.onStart();
        isInital = true;
    }
    /* Traps back button events during quiz time */
    @Override
    public void onBackPressed() {

        Fragment fragment = getFragmentManager().findFragmentById(R.id.frame_window);
        if(fragment instanceof QuizFragment) {
            new AlertDialog.Builder(this)
                    .setTitle("PRATITI")
                    .setMessage("Are you sure to interrupt quiz?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            changeFragment(new HomeFragment());
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        } else if (fragment instanceof HomeFragment){
            new AlertDialog.Builder(this)
                    .setTitle("PRATITI")
                    .setMessage("Are you sure you want to quit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        } else {
            changeFragment(new HomeFragment());
        }
    }
    /* Reside menu right direction disabled */
    static private void setupNavigation() {

        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);
    }
    /* Setting up firebase listeners for quiz set and quiz start events */
    private void setupFirebase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        new QuizSetLoadAsync().execute();
        firebaseQuizSetListener();
        firebaseQuizStartListener();
    }
    /* Firebase listener to trap quiz start events */
    private void firebaseQuizStartListener() {

        /*final DocumentReference documentReference = db.collection("config").document("currentQuiz");
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null) {
                    Crashlytics.log(Log.WARN, TAG + ": Quiz start event", "Listen Failed");
                    return;
                }

                Fragment current = getFragmentManager().findFragmentById(R.id.frame_window);

                if (documentSnapshot != null) {
                    Crashlytics.log(Log.DEBUG, TAG + ": Quiz start event", "Quiz Fragment :" + (current instanceof QuizFragment));
                    Crashlytics.log(Log.DEBUG, TAG + ": Quiz start event", "isInitial:" + isInital);
                    if (!(current instanceof QuizFragment) && !isInital) {
                        Long temp = documentSnapshot.getLong("qNo");
                        if(temp != null){
                            if(temp == 0) {
                                changeFragment(new QuizFragment());
                            }
                        } else {
                            Crashlytics.log(Log.DEBUG, TAG + ": Quiz start event", "Question number is null");
                        }
                    } else {
                        isInital = false;
                    }

                } else {
                    Crashlytics.log(Log.DEBUG, TAG + ": Quiz start event", "Current data : null");
                }
            }
        });*/

        DatabaseReference databaseReference = firebaseDatabase.getReference("qNo");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Fragment current = getFragmentManager().findFragmentById(R.id.frame_window);
                Crashlytics.log(Log.DEBUG, TAG + ": Quiz start event", "Quiz Fragment :" + (current instanceof QuizFragment));
                Crashlytics.log(Log.DEBUG, TAG + ": Quiz start event", "isInitial:" + isInital);
                Long temp = dataSnapshot.getValue(Long.class);
                Log.d(TAG, "Value is: " + temp);

                if (!(current instanceof QuizFragment) && !isInital) {
                    if(temp != null){
                        if(temp == 0) {
                            changeFragment(new QuizFragment());
                        }
                    } else {
                        Crashlytics.log(Log.DEBUG, TAG + ": Quiz start event", "Question number is null");
                    }
                } else {
                    isInital = false;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", databaseError.toException());

            }
        });
    }
    /* Firebase listener for changes in quiz data */
    private void firebaseQuizSetListener() {

        final CollectionReference quizRef = db.collection("quizes");
        quizRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e != null) {
                    Crashlytics.log(Log.WARN, TAG + ": Quiz list change event", "Listen Failed");
                    return;
                }

                if (queryDocumentSnapshots != null) {
                    Crashlytics.log(Log.DEBUG, TAG + ": Quiz list change event", "Quiz List changed");
                    if(!isInital) {
                        new QuizSetLoadAsync().execute();
                    }
                } else {
                    Crashlytics.log(Log.DEBUG, TAG + ": Quiz list change event", "Current data : null");
                }
            }
        });
    }
    /* Method to load quiz details from firebase */
    static private void firebaseLoadQuizSet() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final CollectionReference quizRef = db.collection("quizes");
        quizRef.whereEqualTo("completed",false)
                .orderBy("scheduledTime", Query.Direction.ASCENDING).limit(20)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    quizSets.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        QuizSet quizSet = document.toObject(QuizSet.class);
                        quizSets.add(quizSet);
                        Log.d(TAG, quizSet.getScheduledTime().toDate().toString());
                    }
                } else {
                    Crashlytics.log(Log.DEBUG, TAG + ": Quiz list loading", "Error getting documents : " + task.getException());
                }
            }
        });
    }
    /* Method to load subject details for modules fragment */
    static private void firebaseLoadSubjects() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final CollectionReference yearRef = db.collection("years");
        yearRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "Year details Loading");
                    Integer j = 0;

                    ArrayList<Object> subject;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        HashMap<String, ArrayList<String>> subjectTopicHash = new HashMap<>();
                        HashMap<String, ArrayList<String>> hashMap = new HashMap<>();
                        ArrayList<String> subjectList = new ArrayList<>();
                        subject = (ArrayList<Object>) document.get("subs");
                        for(int i = 0; i < subject.size(); i++) {
                            hashMap = (HashMap<String, ArrayList<String>>) subject.get(i);
                            String title = String.valueOf(hashMap.get("title"));
                            ArrayList<String> topics = hashMap.get("topics");
                            subjectList.add(title);
                            subjectTopicHash.put(title, topics);
                        }
                        Log.d(TAG, "\nTitle: " + subjectList + "\nTopics:" + subjectTopicHash);
                        Year year = new Year(subjectList, subjectTopicHash);
                        yearList.add(year);
                        hashMap.clear();
                        for (j = 0; j < yearList.size(); j++) {
                            Log.d(TAG, yearList.get(j).getTopics().toString());
                        }

                    }
                    Log.d(TAG, "Complete subject list : \n" + yearList.get(0).getTopics().toString());
                } else {
                    Log.d(TAG, "Year details loading unsuccessful");
                }
            }
        });
    }
    /* Setting up reside menu */
    private void setupMenu() {

        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(R.drawable.reside_background);
        resideMenu.attachToActivity(this);

        itemHome = new ResideMenuItem(this, R.drawable.ic_home, "Home");
        itemQuiz = new ResideMenuItem(this, R.drawable.ic_quiz, "Quiz");
        itemModule = new ResideMenuItem(this, R.drawable.ic_module, "Modules");
        itemProfile = new ResideMenuItem(this, R.drawable.ic_profile, "Profile");
        itemCredits = new ResideMenuItem(this, R.drawable.ic_credits, "Credits");
        itemLogout = new ResideMenuItem(this, R.drawable.ic_logout, "Logout");

        itemHome.setOnClickListener(this);
        itemQuiz.setOnClickListener(this);
        itemModule.setOnClickListener(this);
        itemProfile.setOnClickListener(this);
        itemCredits.setOnClickListener(this);
        itemLogout.setOnClickListener(this);

        resideMenu.addMenuItem(itemHome, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemQuiz, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemModule, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemProfile, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemCredits, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemLogout, ResideMenu.DIRECTION_LEFT);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.dispatchTouchEvent(ev);
    }

    @Override
    public void onClick(View v) {

        if (v == itemHome) {
            changeFragment(new HomeFragment());
        } else if(v == itemQuiz) {
            changeFragment(new CountDownFragment());
        } else if (v == itemModule) {
            changeFragment(new ModuleFragment());
        } else if (v == itemProfile) {
            changeFragment(new ProfileFragment());
        } else if (v == itemCredits) {
            changeFragment(new CreditsFragment());
        } else if (v == itemLogout) {
            logOut();
        }

        resideMenu.closeMenu();
    }
    /* Logout method */
    private void logOut() {
        mAuth.signOut();
        finish();
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        finishAffinity();
        startActivity(intent);
    }
    /* Method fot changing fragement according to menu click */
    private void changeFragment(final Fragment targetFragment) {

        resideMenu.clearIgnoredViewList();
        /* Playing background service for quiz fragement */
        if(targetFragment instanceof QuizFragment) {
            svc = new Intent(this, BackgroundSoundService.class);
            svc.putExtra("quiz_name", quizSets.get(0).getTitle());
            svc.putExtra("quiz_timeout",quizSets.get(0).getTimeOut());
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(TAG, "API LEVEL = " + Build.VERSION.SDK_INT);
                startForegroundService(svc);
            } else {
                Log.d(TAG, "ELSE API LEVEL = " + Build.VERSION.SDK_INT);
                startService(svc);
            }
        }
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setTransitionStyle(android.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                fragmentTransaction.replace(R.id.frame_window, targetFragment);
                fragmentTransaction.commitAllowingStateLoss();
            }
        });

    }
    /* Asynctask for loading quizset in background */
    static class QuizSetLoadAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            firebaseLoadQuizSet();
            firebaseLoadSubjects();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setupNavigation();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
    /* For accessing reside menu from fragments */
    public ResideMenu getResideMenu() {
        return resideMenu;
    }
    /* For accessing quiz list from fragments */
    public ArrayList<QuizSet> getQuizList() {
        return quizSets;
    }
    /* For accessing quiz from fragments */
    public static ArrayList<Year> getYearSets() {
        return yearList;
    }
}

