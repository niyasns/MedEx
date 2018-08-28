package com.example.android.medex;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.alexfu.countdownview.CountDownView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.special.ResideMenu.ResideMenu;
import com.google.firebase.Timestamp;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CountDownFragment extends Fragment {

    private static final String TAG = "CountDown Firebase";
    private View parentView;
    private ResideMenu resideMenu;

    CountDownView countDownView;
    TextView quizCountText;

    Date nextQuiz;
    Date current;

    private HomeActivity parentActivity;
    QuizSet quizSet;
    List<Question> questionList;

    FirebaseFirestore db;

    List QuizList;

    public CountDownFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.countdown_fragment, container, false);
        setupViews();
        setFirebaseListner();
        setQuiz();
        return parentView;
    }

    private void setupViews() {
        parentActivity = (HomeActivity) getActivity();
        Typeface raleway_bold = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Raleway-Bold.ttf" );
        Typeface raleway_regular = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Raleway-Regular.ttf" );
        Button button = parentActivity.findViewById(R.id.menu_button);
        TextView heading = parentActivity.findViewById(R.id.heading);
        heading.setText("Quiz");
        resideMenu = parentActivity.getResideMenu();

        countDownView = parentView.findViewById(R.id.countDownView);
        quizCountText = parentView.findViewById(R.id.quizTitle);

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

    private void setFirebaseListner() {
        db = FirebaseFirestore.getInstance();

        final CollectionReference quizRef = db.collection("quizes");

        quizRef.orderBy("scheduledTime", Query.Direction.ASCENDING).limit(1)
                .whereEqualTo("started", false)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        if (value != null) {
                            for (QueryDocumentSnapshot doc : value) {
                                if (doc.get("scheduledTime") != null) {
                                    Timestamp timestamp = doc.getTimestamp("scheduledTime");
                                    nextQuiz = timestamp.toDate();
                                    Log.d("Quiz time changed", nextQuiz.toString());
                                    getCurrentTime();
                                }
                            }
                        } else {
                            countDownView.setVisibility(View.INVISIBLE);
                            quizCountText.setText("No Quizes Found");
                        }
                    }
                });
    }

    private void setQuiz() {

        QuizList = parentActivity.getQuizList();
        if(QuizList.isEmpty()) {
            countDownView.setVisibility(View.INVISIBLE);
            quizCountText.setText("No Data Found");
            quizCountText.setTextSize(24);
        } else {
            quizSet = (QuizSet) QuizList.get(0);
            getCurrentTime();
        }
    }

    private void getCurrentTime() {

        FirebaseFunctions.getInstance().getHttpsCallable("getTime")
                .call().addOnSuccessListener(new OnSuccessListener<HttpsCallableResult>() {
            @Override
            public void onSuccess(HttpsCallableResult httpsCallableResult) {
                long timestamp = (long) httpsCallableResult.getData();
                current = new Date(timestamp);
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

                if(total < 0) {
                    countDownView.setVisibility(View.INVISIBLE);
                    quizCountText.setText("No Data Found");
                    quizCountText.setTextSize(24);
                } else {
                    countDownView.reset();
                    countDownView.setStartDuration(total);
                    countDownView.start();
                }
            }
        });
    }
}
