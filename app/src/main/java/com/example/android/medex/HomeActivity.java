package com.example.android.medex;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "HomeActivity - Firebase";
    static ResideMenu resideMenu;

    ResideMenuItem itemHome;
    ResideMenuItem itemQuiz;
    ResideMenuItem itemModule;
    ResideMenuItem itemProfile;
    ResideMenuItem itemLogout;

    TextView heading;

    static ProgressBar progressBar;

    static List<QuizSet> quizSets;

    static FirebaseFirestore db;
    FirebaseAuth mAuth;

    static boolean isInital;

    private HomeActivity mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        mContext = this;
        isInital = true;
        progressBar = findViewById(R.id.progressbarHome);
        Typeface raleway_bold = Typeface.createFromAsset(this.getAssets(),"fonts/Raleway-Bold.ttf" );
        Typeface raleway_regular = Typeface.createFromAsset(this.getAssets(),"fonts/Raleway-Regular.ttf" );
        heading = findViewById(R.id.heading);
        heading.setTypeface(raleway_bold);
        quizSets = new ArrayList<>();
        setupMenu();
        setupFirebase();
        if (savedInstanceState == null) {
            changeFragment(new HomeFragment());
        }

        findViewById(R.id.menu_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        isInital = true;
    }

    static private void setupNavigation() {

        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);
    }

    private void setupFirebase() {

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        new QuizSetLoadAsync().execute();
        firebaseQuizSetListener();
        firebaseQuizStartListener();
    }

    private void firebaseQuizStartListener() {

        final DocumentReference documentReference = db.collection("config").document("currentQuiz");

        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null) {
                    Log.w(TAG, "Listen Failed");
                    return;
                }

                Fragment current = getFragmentManager().findFragmentById(R.id.frame_window);

                if (documentSnapshot != null) {
                    Log.d(TAG, "Quiz started" + (current instanceof QuizFragment) + isInital);
                    if (!(current instanceof QuizFragment) && !isInital) {
                        Log.d("!IsInitial","Starting quiz fragment");
                        Long temp = documentSnapshot.getLong("qNo");
                        Log.d("Initial Current QUe: ", temp.toString());
                        if(temp == 0) {
                            changeFragment(new QuizFragment());
                        }
                    } else {
                        isInital = false;
                    }

                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    private void firebaseQuizSetListener() {

        final CollectionReference quizRef = db.collection("quizes");
        quizRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e != null) {
                    Log.w(TAG, "Listen Failed");
                    return;
                }

                if (queryDocumentSnapshots != null) {
                    Log.d(TAG, "Quiz set Changed");
                    if(!isInital) {
                        new QuizSetLoadAsync().execute();
                    }
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    static private void firebaseLoadQuizSet(FirebaseFirestore db) {

        final CollectionReference quizRef = db.collection("quizes");
        quizSets.clear();
        quizRef.whereEqualTo("started", false)
                .orderBy("scheduledTime", Query.Direction.ASCENDING).limit(10)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        QuizSet quizSet = document.toObject(QuizSet.class);
                        quizSets.add(quizSet);
                        Log.d(TAG, quizSet.getScheduledTime().toDate().toString());
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }


    private void setupMenu() {

        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(R.drawable.reside_background);
        resideMenu.attachToActivity(this);

        itemHome = new ResideMenuItem(this, R.drawable.ic_home, "Home");
        itemQuiz = new ResideMenuItem(this, R.drawable.ic_quiz, "Quiz");
        itemModule = new ResideMenuItem(this, R.drawable.ic_module, "Modules");
        itemProfile = new ResideMenuItem(this, R.drawable.ic_profile, "Profile");
        itemLogout = new ResideMenuItem(this, R.drawable.ic_logout, "Logout");

        itemHome.setOnClickListener(this);
        itemQuiz.setOnClickListener(this);
        itemModule.setOnClickListener(this);
        itemProfile.setOnClickListener(this);
        itemLogout.setOnClickListener(this);

        resideMenu.addMenuItem(itemHome, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemQuiz, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemModule, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemProfile, ResideMenu.DIRECTION_LEFT);
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
        } else if (v == itemLogout) {
            logOut();
        }

        resideMenu.closeMenu();
    }

    private void logOut() {
        mAuth.signOut();
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        finishAffinity();
        startActivity(intent);
    }

    private void changeFragment(Fragment targetFragment) {

        resideMenu.clearIgnoredViewList();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransitionStyle(android.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.add(R.id.frame_window, targetFragment);
        fragmentTransaction.commit();
    }

    static class QuizSetLoadAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            firebaseLoadQuizSet(db);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setupNavigation();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public ResideMenu getResideMenu() {
        return resideMenu;
    }

    public List getQuizList() {
        return quizSets;
    }

}

