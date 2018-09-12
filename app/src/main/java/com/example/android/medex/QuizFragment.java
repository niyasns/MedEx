package com.example.android.medex;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.special.ResideMenu.ResideMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.grabner.circleprogress.CircleProgressView;

public class QuizFragment extends android.app.Fragment implements View.OnClickListener {

    private static final String TAG = "QuizFragmet";
    private View parentView;
    private ResideMenu resideMenu;

    Button option_1;
    Button option_2;
    Button option_3;

    TextView heading;
    TextView question;

    Integer progress;
    Runnable r;
    Integer total_questions;
    Boolean isCorrect;
    QuizSet quizSet;
    List<Question> questionList;
    static CountDownTimer countDownTimer;

    static String pAnswer;
    ArrayList<String> userResponse;


    static CircleProgressView circleProgressView;

    static private HomeActivity parentActivity;

    WrongDialog wrongDialog;
    CorrectDialog correctDialog;
    CompleteDialog completeDialog;

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    Integer currentQue;

    List QuizList;

    ArrayList<String> answers;
    String quiz_id;
    Timestamp created_at;
    String user_id;

    Boolean is_fragment_visible;
    ListenerRegistration listenerRegistration;

    public QuizFragment() {
        // Required empty public constructorprotected
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.quiz_fragment, container, false);
        setupViews();
        mAuth = FirebaseAuth.getInstance();
        setupQuiz();
        setupFirebaseRealtimeListner();
        answers = new ArrayList<>();
        isCorrect = false;
        return parentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart entered");
        is_fragment_visible = true;
        setupResponse();
    }

    private void setupResponse() {

        quiz_id = quizSet.getQuizId();
        user_id = mAuth.getUid();
        created_at = quizSet.getScheduledTime();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        is_fragment_visible = false;
        Log.d("QuizFragement", "Quiz fragement destroyed");
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume Entered");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause Entered");
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        is_fragment_visible = false;
        Log.d("QuizFragement", "Quiz fragement stopped");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "OnDestroyView");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "OnDetach");
        listenerRegistration.remove();
    }

    private void setupFirebaseRealtimeListner() {

        db = FirebaseFirestore.getInstance();
        final DocumentReference documentReference = db.collection("config").document("currentQuiz");
        listenerRegistration = documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot,
                                @javax.annotation.Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Log.d(TAG, "Current data: " + documentSnapshot.getData());
                    if(isCorrect) {
                        Long temp = documentSnapshot.getLong("qNo");
                        if (temp != null) {
                            currentQue = temp.intValue();
                            Log.d(TAG, currentQue + " Current Que");
                        }
                        if(correctDialog.isShowing()) {
                            correctDialog.dismiss();
                        }
                        if(temp != -1 && isCorrect.equals(true)) {
                            changeQuestion(currentQue);
                        }
                    }
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    private void sendResponse(final Boolean isComplete) {

        Map<String, Object> response = new HashMap<>();
        response.put("answers", answers);
        response.put("createdAt", created_at);
        response.put("quizId", quiz_id);
        response.put("userId", user_id);

        db.collection("responses").add(response)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        if(isComplete) {
                            completeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            completeDialog.show();
                        } else {
                            wrongDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            wrongDialog.show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    private void setupQuiz() {

        progress = 0;
        userResponse = new ArrayList<>();
        currentQue = 0;
        pAnswer = "null";
        try{
            QuizList = parentActivity.getQuizList();
        } catch (Exception e) {
            Log.d(TAG, "Quiz List not availble");
            parentActivity.getFragmentManager().popBackStackImmediate();
            FragmentTransaction fragmentTransaction = parentActivity.getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame_window, new HomeFragment());
            fragmentTransaction.commit();
        }

        if(QuizList.isEmpty()){
            Log.d(TAG, "Quiz list empty");
        } else {
            quizSet = (QuizSet) QuizList.get(0);
            questionList = quizSet.getQuestions();
            total_questions = questionList.size();
            circleProgressView.setMaxValue(quizSet.getTimeOut());
            circleProgressView.setValue(0);

            countDownTimer = new CountDownTimer(quizSet.getTimeOut() * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    progress++;
                    circleProgressView.setValue(quizSet.getTimeOut() - (millisUntilFinished / 1000));
                }

                @Override
                public void onFinish() {
                    Log.d("QuizFragment", "count down finished");
                    circleProgressView.setValue(quizSet.getTimeOut());
                    countDownTimer.cancel();
                }
            };

            changeQuestion(0);

            circleProgressView.setOnProgressChangedListener(new CircleProgressView.OnProgressChangedListener() {
                @Override
                public void onProgressChanged(float value) {
                    if(value == quizSet.getTimeOut()) {
                        Log.d("QuizFragment", "onprogresscahnged entered");
                        if (pAnswer.equals("null") || !pAnswer.equals(questionList.get(currentQue).getAnswer())) {
                            isCorrect = false;
                            Log.d("Current value:", (currentQue.toString()));
                            answers.add(pAnswer);
                            sendResponse(false);
                        } else if(pAnswer.equals(questionList.get(currentQue).getAnswer()) && ((currentQue + 1) < total_questions)) {
                            Log.d("Current value:", (currentQue.toString()));
                            answers.add(pAnswer);
                            isCorrect = true;
                            correctDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            correctDialog.show();
                        }else if(pAnswer.equals(questionList.get(currentQue).getAnswer()) && ((currentQue + 1) == (total_questions))) {
                            Log.d("Current value:", (currentQue.toString()));
                            isCorrect = true;
                            answers.add(pAnswer);
                            sendResponse(true);
                        }
                    }
                }
            });

            option_1.setOnClickListener(this);
            option_2.setOnClickListener(this);
            option_3.setOnClickListener(this);
        }
    }

    public void changeQuestion(Integer currentQue) {
        Log.d("QuizFragment", "change question entered");
        progress = 0;
        if(currentQue < total_questions) {
            resetButton();
            enableButton();
            question.setText(questionList.get(currentQue).getQuestion());
            circleProgressView.setValue(0);
            option_1.setText(questionList.get(currentQue).getOptions().get(0));
            option_2.setText(questionList.get(currentQue).getOptions().get(1));
            option_3.setText(questionList.get(currentQue).getOptions().get(2));
            new TimerAsync().execute();
        }

    }

    private void setupViews() {
        is_fragment_visible = true;
        Typeface raleway_bold = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Raleway-Bold.ttf" );
        Typeface raleway_regular = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Raleway-Regular.ttf" );
        parentActivity = (HomeActivity) getActivity();
        Button button = parentActivity.findViewById(R.id.menu_button);
        heading = parentActivity.findViewById(R.id.heading);
        option_1 = parentView.findViewById(R.id.option_1);
        option_2 = parentView.findViewById(R.id.option_2);
        option_3 = parentView.findViewById(R.id.option_3);
        question = parentView.findViewById(R.id.question);
        circleProgressView = parentView.findViewById(R.id.circleProgressView);

        heading.setText(R.string.home);
        option_1.setTypeface(raleway_regular);
        option_2.setTypeface(raleway_regular);
        option_3.setTypeface(raleway_regular);
        question.setTypeface(raleway_regular);

        resideMenu = parentActivity.getResideMenu();
        correctDialog = new CorrectDialog(parentActivity);
        completeDialog = new CompleteDialog(parentActivity);
        wrongDialog = new WrongDialog(parentActivity);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.option_1:
                pAnswer = option_1.getText().toString();
                userResponse.add(pAnswer);
                changeClickedButton(option_1);
                disableButton();
                break;
            case R.id.option_2:
                pAnswer = option_2.getText().toString();
                userResponse.add(pAnswer);
                changeClickedButton(option_2);
                disableButton();
                break;
            case R.id.option_3:
                pAnswer = option_3.getText().toString();
                userResponse.add(pAnswer);
                changeClickedButton(option_3);
                disableButton();
                break;
        }

    }

    public void changeClickedButton(Button button) {
        button.setTextColor(this.getResources().getColor(R.color.colorTransparentWhite));
        button.setBackgroundResource(R.drawable.selected_option);
    }

    public void  resetButton() {
        option_1.setTextColor(this.getResources().getColor(R.color.colorSecondary));
        option_1.setBackgroundResource(R.drawable.rounded_button);

        option_2.setTextColor(this.getResources().getColor(R.color.colorSecondary));
        option_2.setBackgroundResource(R.drawable.rounded_button);

        option_3.setTextColor(this.getResources().getColor(R.color.colorSecondary));
        option_3.setBackgroundResource(R.drawable.rounded_button);
    }

    public void enableButton() {
        option_1.setEnabled(true);
        option_2.setEnabled(true);
        option_3.setEnabled(true);
    }

    public void disableButton() {
        option_1.setEnabled(false);
        option_2.setEnabled(false);
        option_3.setEnabled(false);
    }

    static class TimerAsync extends AsyncTask<Void, Void, Void > {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... voids) {

            countDownTimer.start();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

}


