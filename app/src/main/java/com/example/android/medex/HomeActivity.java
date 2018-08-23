package com.example.android.medex;

import android.app.Fragment;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    ResideMenu resideMenu;

    ResideMenuItem itemHome;
    ResideMenuItem itemQuiz;
    ResideMenuItem itemModule;
    ResideMenuItem itemProfile;
    ResideMenuItem itemCredits;
    ResideMenuItem itemLogout;

    TextView heading;

    ProgressBar progressBar;

    List<QuizSet> quizSets;

    CurrentQuiz currentQuiz;

    FirebaseFirestore db;

    private HomeActivity mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        db = FirebaseFirestore.getInstance();
        mContext = this;
        progressBar = findViewById(R.id.progressbarHome);
        Typeface raleway_bold = Typeface.createFromAsset(this.getAssets(),"fonts/Raleway-Bold.ttf" );
        Typeface raleway_regular = Typeface.createFromAsset(this.getAssets(),"fonts/Raleway-Regular.ttf" );
        heading = findViewById(R.id.heading);
        heading.setTypeface(raleway_bold);
        setupFirebase();
        setupMenu();
        progressBar.setVisibility(View.INVISIBLE);
        if (savedInstanceState == null) {
            changeFragment(new HomeFragment());
        }
    }

    private void setupFirebase() {

        firebaseLoadQuizSet(db);

        final DocumentReference documentReference = db.collection("config").document("currentQuiz");
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null) {
                    Log.w(TAG, "Listen Failed");
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Log.d(TAG, "Current data: " + documentSnapshot.getData());
                    currentQuiz = documentSnapshot.toObject(CurrentQuiz.class);
                } else {
                    Log.d(TAG, "Current data: null");
                }

            }
        });

    }

    private void firebaseLoadQuizSet(FirebaseFirestore db) {

        final CollectionReference quizRef = db.collection("quizes");
        quizSets = new ArrayList<>();
        quizRef.whereEqualTo("completed", false)
                .orderBy("scheduledTime", Query.Direction.ASCENDING).limit(10)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
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
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);

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
            changeFragment(new HomeFragment());
        } else if(v == itemQuiz) {
            changeFragment(new QuizFragment());
        } else if (v == itemModule) {
            changeFragment(new ModuleFragment());
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

    public void checkStatus() {

        if (currentQuiz.qNo == -1) {

            changeFragment(new CountDownFragment());
        } else {
            firebaseLoadQuizSet(db);
            changeFragment(new CountDownFragment());
        }
    }

    public ResideMenu getResideMenu() {
        return resideMenu;
    }

    public List getQuizList() {
        return quizSets;
    }

    public CurrentQuiz getCurrentQuiz() {
        return currentQuiz;
    }
}

