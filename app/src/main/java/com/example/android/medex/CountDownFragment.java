package com.example.android.medex;

import android.app.Fragment;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alexfu.countdownview.CountDownView;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.special.ResideMenu.ResideMenu;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;

public class CountDownFragment extends Fragment {

    private static final String TAG = "CountDown Firebase";
    private View parentView;
    private ResideMenu resideMenu;
    private TextView no_data;
    private HomeActivity parentActivity;

    CountDownView countDownView;
    RecyclerView recyclerView;
    FrameLayout quizListFrame;
    TextView quizCountText;
    Date nextQuiz;
    Date current;

    QuizSet quizSet;
    static ArrayList<QuizSet> QuizList;

    FirebaseFirestore db;
    ProgressBar progressBar;
    ListenerRegistration listenerRegistration;
    ListenerRegistration listenerRegistration_quizSet;

    QuizRecyclerAdapter quizRecyclerAdapter;

    public CountDownFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.countdown_fragment, container, false);
        setupViews();
        setFirebase();
        setFirebaseListener();
        setQuiz();
        setRecyclerView();
        return parentView;
    }

    /* Method to load quiz details from firebase */
    static private void firebaseLoadQuizSet(final ProgressBar progressBar, final TextView no_data,
                                            final RecyclerView recyclerView, final FrameLayout quizListFrame,
                                            final QuizRecyclerAdapter quizRecyclerAdapter) {
        Log.d(TAG, "Firebase lodaing quiz set");
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final CollectionReference quizRef = db.collection("quizes");
        quizRef.whereEqualTo("completed",false)
                .orderBy("scheduledTime", Query.Direction.ASCENDING).limit(20)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuizList.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        QuizSet quizSet = document.toObject(QuizSet.class);
                        QuizList.add(quizSet);
                        Log.d(TAG, quizSet.getScheduledTime().toDate().toString());
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                    if(QuizList.isEmpty()) {
                        recyclerView.setVisibility(View.INVISIBLE);
                        quizListFrame.addView(no_data);
                    } else {
                        quizRecyclerAdapter.notifyDataSetChanged();
                    }

                } else {
                    Crashlytics.log(Log.DEBUG, TAG + ": Quiz list loading", "Error getting documents : " + task.getException());
                }
            }
        });
    }

    private void setRecyclerView() {
        recyclerView = parentView.findViewById(R.id.quiz_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        if(QuizList.isEmpty()) {
            recyclerView.setVisibility(View.INVISIBLE);
            quizListFrame.addView(no_data);
        } else {
            Log.d(TAG, String.valueOf(QuizList.size()));
            quizRecyclerAdapter = new QuizRecyclerAdapter(CountDownFragment.this, QuizList,progressBar);
            recyclerView.setAdapter(quizRecyclerAdapter);
        }
    }

    private void setFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listenerRegistration.remove();
        listenerRegistration_quizSet.remove();
    }

    private void setupViews() {

        parentActivity = (HomeActivity) getActivity();
        Typeface raleway_bold = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Raleway-Bold.ttf" );
        Typeface raleway_regular = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Raleway-Regular.ttf" );

        Button button = parentActivity.findViewById(R.id.menu_button);
        TextView heading = parentActivity.findViewById(R.id.heading);
        progressBar = parentActivity.findViewById(R.id.progressbarHome);
        progressBar.setVisibility(View.VISIBLE);
        heading.setText("Quiz");
        resideMenu = parentActivity.getResideMenu();

        countDownView = parentView.findViewById(R.id.countDownView);
        quizCountText = parentView.findViewById(R.id.quizTitle);
        quizListFrame = parentView.findViewById(R.id.quizListFrame);

        no_data = new TextView(parentActivity);
        no_data.setText("No Data found");
        no_data.setTextColor(parentActivity.getResources().getColor(R.color.colorTransparentWhite));
        no_data.setTypeface(raleway_regular);
        no_data.setTextSize(24);
        no_data.setGravity(Gravity.CENTER);

        heading.setTypeface(raleway_bold);
        quizCountText.setTypeface(raleway_regular);
        quizCountText.setTextSize(30);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });
    }
    /* Firebase listener for listening change in quiz set */
    private void setFirebaseListener() {

        final CollectionReference quizRef = db.collection("quizes");

        listenerRegistration = quizRef.orderBy("scheduledTime", Query.Direction.ASCENDING)
                .whereEqualTo("started", false)
                .limit(1)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        if (value != null) {
                            Log.d(TAG, "Change in scheduled time");
                            for (QueryDocumentSnapshot doc : value) {
                                if (doc.get("scheduledTime") != null) {
                                    Timestamp timestamp = doc.getTimestamp("scheduledTime");
                                    nextQuiz = timestamp.toDate();
                                    Log.d("Quiz time changed", nextQuiz.toString());
                                    Fragment fragment = getFragmentManager().findFragmentById(R.id.frame_window);
                                    if(fragment instanceof CountDownFragment) {
                                        Log.d(TAG,"Count Down Fragment updated");
                                        getCurrentTime();
                                    }
                                }
                            }
                        } else {
                            countDownView.setVisibility(View.INVISIBLE);
                            quizCountText.setText("No Quizes Found");
                        }
                    }
                });

        listenerRegistration_quizSet = quizRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if(e != null) {
                    Crashlytics.log(Log.WARN, TAG + ": Quiz list change event", "Listen Failed");
                    return;
                }

                if (queryDocumentSnapshots != null) {
                    Crashlytics.log(Log.DEBUG, TAG + ": Quiz list change event", "Quiz List changed");
                    Fragment fragment = getFragmentManager().findFragmentById(R.id.frame_window);
                    if(fragment instanceof CountDownFragment) {
                        Log.d(TAG,"Quiz recycler updated");
                        firebaseLoadQuizSet(progressBar, no_data, recyclerView, quizListFrame, quizRecyclerAdapter);
                    }
                } else {
                    Crashlytics.log(Log.DEBUG, TAG + ": Quiz list change event", "Current data : null");
                }
            }
        });
    }
    /* Setting count down timer for next quiz */
    private void setQuiz() {

        QuizList = parentActivity.getQuizList();
        if(QuizList.isEmpty()) {
            progressBar.setVisibility(View.INVISIBLE);
            countDownView.setVisibility(View.INVISIBLE);
            quizCountText.setText("No Data Found");
            quizCountText.setTextSize(24);
        } else {
            quizSet = (QuizSet) QuizList.get(0);
            getCurrentTime();
        }
    }
    /* getting current server time using firebase callable functions */
    private void getCurrentTime() {

        FirebaseFunctions.getInstance().getHttpsCallable("getTime")
                .call().addOnSuccessListener(new OnSuccessListener<HttpsCallableResult>() {
            @Override
            public void onSuccess(HttpsCallableResult httpsCallableResult) {
                long timestamp = (long) httpsCallableResult.getData();
                current = new Date(timestamp);
                try{
                    Log.d(TAG, current.toString());
                } catch (NullPointerException e) {
                    Log.d(TAG, "Current date null");
                }

                if(current != null && nextQuiz != null) {
                    Log.w("Current Time", current.toString());
                    Log.w("Next Quiz Time", nextQuiz.toString());

                    long different = nextQuiz.getTime() - current.getTime();

                    long secondsInMilli = 1000;
                    long minutesInMilli = secondsInMilli * 60;
                    long hoursInMilli = minutesInMilli * 60;
                    long daysInMilli = hoursInMilli * 24;

                    long elapsedDays = different / daysInMilli;
                    different = different % daysInMilli;

                    long elapsedHours = different / hoursInMilli;
                    different = different % hoursInMilli;

                    long elapsedMinutes = different / minutesInMilli;
                    different = different % minutesInMilli;

                    long elapsedSeconds = different / secondsInMilli;

                    elapsedHours = elapsedHours + (elapsedDays * 24);

                    long total = (elapsedHours * 60 * 60000) + (elapsedMinutes * 60000) + ((elapsedSeconds) * 1000);

                    progressBar.setVisibility(View.INVISIBLE);

                    if(total <= 0) {
                        countDownView.setVisibility(View.INVISIBLE);
                        quizCountText.setText("Waiting for quiz initialization");
                        quizCountText.setTextSize(24);
                    } else {
                        countDownView.setVisibility(View.VISIBLE);
                        quizCountText.setText(getString(R.string.next_quiz));
                        quizCountText.setTextSize(30);
                        countDownView.reset();
                        countDownView.setStartDuration(total);
                        countDownView.start();
                    }
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(parentActivity, "Check after some time", Toast.LENGTH_SHORT).show();
                    countDownView.setVisibility(View.INVISIBLE);
                    quizCountText.setText("No data found");
                    quizCountText.setTextSize(24);
                }
            }
        });
    }
}
