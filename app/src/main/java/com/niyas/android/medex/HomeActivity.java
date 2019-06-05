package com.niyas.android.medex;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.facebook.login.LoginManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.annotation.Nullable;

import io.fabric.sdk.android.Fabric;

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
    ResideMenuItem itemVideos;
    ResideMenuItem itemPrivacyPolicy;
    /* Firebase variables */
    static FirebaseFirestore db;
    static FirebaseDatabase firebaseDatabase;
    static FirebaseAuth mAuth;
    /* List to load quizes available from the database.*/
    static ArrayList<QuizSet> quizSets;
    /*List to load year details for module fragment */
    static ArrayList<Year> yearList;
    /*List to load year details for module fragment */
    static ArrayList<Year> tempList;
    private static Date current;
    private static Date quizDate;
    private static CountDownTimer quizTimer = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.home_activity);
        Fabric.with(this, new Crashlytics());
        initView();
        initQuizList();
        setupMenu();
        //setupHandlers();
        setupFirebase();
        firebaseQuizStartTimer();
        /* Load home fragemnt initially */
        if (savedInstanceState == null) {
            Crashlytics.log(Log.DEBUG, TAG, "Home fragment loaded");
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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

    @Override
    protected void onStart() {
        super.onStart();
        stopService(new Intent(this, BackgroundSoundService.class));
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /* Traps back button events during quiz time */
    @Override
    public void onBackPressed() {

        android.support.v4.app.Fragment  fragment = getSupportFragmentManager().findFragmentById(R.id.frame_window);
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
                            finishAffinity();
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
        //resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_LEFT);
    }
    /* Setting up firebase listeners for quiz set and quiz start events */
    private void setupFirebase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore.setLoggingEnabled(true);
     /*   firebaseLoadQuizSet();
        firebaseLoadSubjects();*/
        firebaseQuizSetListener();
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
                    firebaseLoadQuizSet();
                    firebaseLoadSubjects();
                } else {
                    Crashlytics.log(Log.DEBUG, TAG + ": Quiz list change event", "Current data : null");
                }
            }
        });
    }
    /* Method to load quiz details from firebase */
    static private void firebaseLoadQuizSet() {
        db.enableNetwork();
        quizSets.clear();
        Crashlytics.log(Log.DEBUG, TAG , "Loading quizset");
        final CollectionReference quizRef = db.collection("quizes");
        quizRef.whereEqualTo("started",false)
                .whereEqualTo("completed", false)
                .orderBy("scheduledTime", Query.Direction.ASCENDING).limit(20)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Integer i = 0;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        QuizSet quizSet = document.toObject(QuizSet.class);
                        quizSets.add(quizSet);
                        Crashlytics.log(Log.DEBUG, TAG , "QuizList: " + quizSets.get(i++).getQuizId());
                    }
                    Crashlytics.log(Log.DEBUG, TAG , "Quiz list reloaded");
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    Crashlytics.log(Log.DEBUG, TAG + ": Quiz list loading", "Error getting documents : " + task.getException());
                }
            }
        });
    }

    private void firebaseQuizStartTimer() {

        final CollectionReference quizRef = db.collection("quizes");

        quizRef.orderBy("scheduledTime", Query.Direction.ASCENDING)
                .whereEqualTo("started", false)
                .whereEqualTo("completed", false)
                .limit(1)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@android.support.annotation.Nullable QuerySnapshot value,
                                        @android.support.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        if (value != null) {
                            Log.d(TAG, "Change in scheduled time");
                            for (QueryDocumentSnapshot doc : value) {
                                if (doc.get("scheduledTime") != null) {
                                    Timestamp timestamp = doc.getTimestamp("scheduledTime");
                                    quizDate = timestamp.toDate();
                                    Log.d(TAG, "Start timer: " + quizDate.toString());
                                    firebaseResetTimer();
                                }
                            }
                        } else {
                            Crashlytics.log("No quizes found for start timer");
                        }
                    }
                });
    }

    void firebaseResetTimer() {

        FirebaseFunctions.getInstance().getHttpsCallable("getTime")
                .call().addOnSuccessListener(new OnSuccessListener<HttpsCallableResult>() {
            @Override
            public void onSuccess(HttpsCallableResult httpsCallableResult) {
                long timestamp = (long) httpsCallableResult.getData();
                current = new Date(timestamp);
                try {
                    Log.d(TAG, current.toString());
                } catch (NullPointerException e) {
                    Log.d(TAG, "Current date null");
                }

                long diff = quizDate.getTime() - current.getTime();

                if(quizTimer != null) {
                    Crashlytics.log(Log.DEBUG, TAG + "Firebase Reset Timer", "Cancelling current timer");
                    quizTimer.cancel();
                }

                if(diff > 0){
                    quizTimer = new CountDownTimer(diff, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            Log.d(TAG, "seconds remaining: " + millisUntilFinished / 1000);
                            Long secondRemain = millisUntilFinished / 1000;
                            if(secondRemain == 20) {
                                if(quizSets.isEmpty()) {
                                    Crashlytics.log(Log.DEBUG, TAG + "Firebase Reset Timer", "Quiz set is null");
                                    firebaseLoadQuizSet();
                                    firebaseLoadSubjects();
                                }
                            }
                        }

                        @Override
                        public void onFinish() {
                            android.support.v4.app.Fragment current = getSupportFragmentManager().findFragmentById(R.id.frame_window);
                            Crashlytics.log(Log.DEBUG, TAG + ": Quiz start event", "Quiz Fragment :" + (current instanceof QuizFragment));

                            if (!(current instanceof QuizFragment)) {
                                changeFragment(new QuizFragment());
                            } else {
                                // isInital = false;
                            }
                        }
                    };
                    quizTimer.start();
                }
            }
        });
    }
    /* Method to load subject details for modules fragment */
    static private void firebaseLoadSubjects() {

        progressBar.setVisibility(View.VISIBLE);
        final CollectionReference yearRef = db.collection("years");
        yearRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    progressBar.setVisibility(View.INVISIBLE);
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
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
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
        itemCredits = new ResideMenuItem(this, R.drawable.ic_credits, "Credits");
        itemPrivacyPolicy = new ResideMenuItem(this, R.drawable.ic_privacy, "Privacy");
        itemLogout = new ResideMenuItem(this, R.drawable.ic_logout, "Logout");

        itemHome.setOnClickListener(this);
        itemCredits.setOnClickListener(this);
        itemPrivacyPolicy.setOnClickListener(this);
        itemLogout.setOnClickListener(this);

        resideMenu.addMenuItem(itemHome, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemCredits, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemPrivacyPolicy, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemLogout, ResideMenu.DIRECTION_LEFT);

        setupNavigation();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.dispatchTouchEvent(ev);
    }

    @Override
    public void onClick(View v) {

        if (v == itemHome) {
            changeFragment(new HomeFragment());
        } else if (v == itemCredits) {
            changeFragment(new CreditsFragment());
        } else if (v == itemPrivacyPolicy) {
            changeFragment(new PrivacyFragment());
        } else if (v == itemLogout) {
            logOut();
        }

        resideMenu.closeMenu();
    }
    /* Logout method */
    private void logOut() {
        mAuth.signOut();
        LoginManager.getInstance().logOut();
        finish();
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        finishAffinity();
        startActivity(intent);
    }
    /* Method fot changing fragement according to menu click */
    private void changeFragment(final Fragment targetFragment) {

        resideMenu.clearIgnoredViewList();
        /* Playing background service for quiz fragement */
    /*    if(targetFragment instanceof QuizFragment) {
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
        }*/
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction  fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setTransitionStyle(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
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

