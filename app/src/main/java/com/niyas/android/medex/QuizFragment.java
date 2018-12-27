package com.niyas.android.medex;

import android.support.v4.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.special.ResideMenu.ResideMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.grabner.circleprogress.CircleProgressView;

public class QuizFragment extends android.support.v4.app.Fragment implements View.OnClickListener {

    private static final String TAG = "QuizFragment";
    private View parentView;
    private ResideMenu resideMenu;
    Button option_1;
    Button option_2;
    Button option_3;
    TextView heading;
    TextView question;
    /* Integer to track progress in circular progress view */
    Integer progress;
    /* Integer representing total number of questions in present set */
    Integer total_questions;
    /* Boolean to represent current user selection is correct or not */
    Boolean isCorrect;
    /* QuizSet instance for accessing data */
    QuizSet quizSet;
    /* List to place questions */
    List<Question> questionList;
    /* Countdown timer to run with circular progress view */
    static CountDownTimer countDownTimer;
    /* Answer for present question */
    static String pAnswer;
    /* Array list to store user response */
    ArrayList<String> userResponse;
    /* Cirular progressview instance */
    static CircleProgressView circleProgressView;
    private HomeActivity parentActivity;
    /* Different dailog instances for quiz events */
    WrongDialog wrongDialog;
    CorrectDialog correctDialog;
    CompleteDialog completeDialog;
    /* Firebase instances*/
    FirebaseDatabase firebaseDatabase;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    /* Current question number */
    Integer currentQue;
    /* List of quizes */
    List QuizList;
    /* Answers array list */
    ArrayList<String> answers;
    /* Quiz details for creating response*/
    String quiz_id;
    Timestamp created_at;
    String user_id;
    /* ListenerRegistraion used to stop listener during fragment detach */
    ListenerRegistration listenerRegistration;
    DatabaseReference databaseReference;
    ValueEventListener valueEventListener;

    Button userAnswer;

    public QuizFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.quiz_fragment, container, false);
        setupViews();
        mAuth = FirebaseAuth.getInstance();
        setupQuiz();
        setupFirebaseRealtimeListner();
        setupCircularProgressViewListener();
        answers = new ArrayList<>();
        isCorrect = false;
        userAnswer = null;
        return parentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart entered");
    }

    private void setupResponse() {

        quiz_id = quizSet.getQuizId();
        user_id = mAuth.getUid();
        created_at = quizSet.getScheduledTime();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Quiz fragement destroyed");
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
        Log.d(TAG, "Quiz fragement stopped");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "OnDestroyView");
        databaseReference.removeEventListener(valueEventListener);
        countDownTimer.cancel();
        parentActivity.setIsInitial();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "OnDetach");
        databaseReference.removeEventListener(valueEventListener);
        countDownTimer.cancel();
        parentActivity.setIsInitial();
    }
    /**
     * Firebase Listener for question number events.
     * This Listener listens for the changes in current question number.
     * When an event occurs, it changes the current question.
     */
    private void setupFirebaseRealtimeListner() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("qNo");
        databaseReference.keepSynced(true);
        valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(isCorrect) {
                    Long temp = dataSnapshot.getValue(Long.class);
                    if (temp != null) {
                        currentQue = temp.intValue();
                        Log.d(TAG, currentQue + " Current Que");
                    }
                    if(correctDialog.isShowing()) {
                        correctDialog.dismiss();
                    }
                    if(temp != null) {
                        if(temp != -1 && isCorrect.equals(true)) {
                            changeQuestion(currentQue);
                        }
                    } else {
                        Crashlytics.log(Log.DEBUG, TAG, "Current question value is null");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Sending response after quiz is completed.
     * @param isComplete boolean to check if quiz is complete or not.
     */
    private void sendResponse(final Boolean isComplete) {

        Map<String, Object> response = new HashMap<>();
        response.put("answers", answers);
        response.put("createdAt", created_at);
        response.put("quizId", quiz_id);
        response.put("userId", user_id);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
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
    /* Setting up quiz */
    private void setupQuiz() {
        Log.d(TAG, "Setting Quiz up");
        progress = 0;
        userResponse = new ArrayList<>();
        currentQue = 0;
        pAnswer = "null";
        try{
            /* Getting quiz list by calling parent activity method */
            QuizList = parentActivity.getQuizList();
        } catch (Exception e) {
            /* If quiz list is null, Quiz fragemnt replaces with home fragment */
            Log.d(TAG, "Quiz List not availble");
            parentActivity.getSupportFragmentManager().popBackStackImmediate();
            FragmentTransaction fragmentTransaction = parentActivity.getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame_window, new HomeFragment());
            fragmentTransaction.commitAllowingStateLoss();
        }

        if(QuizList.isEmpty()){
            Log.d(TAG, "Quiz list empty");
        } else {
            quizSet = (QuizSet) QuizList.get(0);
            setupResponse();
            questionList = quizSet.getQuestions();
            total_questions = questionList.size();
            circleProgressView.setMaxValue(quizSet.getTimeOut());
            circleProgressView.setValue(0);

            setupCountDownTimer();
            /* Calling changeQuestion with argument 0 to represent first question */
            changeQuestion(0);
        }
    }
    /* Count down timer initialisation for circular progress view */
    private void setupCountDownTimer() {

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

    }
    /* Event handling for circular progress view */
    private void setupCircularProgressViewListener() {
        android.support.v4.app.Fragment current = parentActivity.getSupportFragmentManager().findFragmentById(R.id.frame_window);
        if(current instanceof QuizFragment) {
            circleProgressView.setOnProgressChangedListener(new CircleProgressView.OnProgressChangedListener() {
                @Override
                public void onProgressChanged(float value) {
                    /* Checking time out reached or not */
                    if(value == quizSet.getTimeOut()) {
                        Log.d("QuizFragment", "onprogresscahnged entered");
                        if (pAnswer.equals("null") || !pAnswer.equals(questionList.get(currentQue).getAnswer())) {
                            /* If no answer is provided or answer is wrong*/
                            isCorrect = false;
                            Log.d("Current value:", (currentQue.toString()));
                            answers.add(pAnswer);
                            showCorrectAnswer(questionList.get(currentQue).getAnswer());
                            showWrongAnswer();
                            sendResponse(false);
                        } else if(pAnswer.equals(questionList.get(currentQue).getAnswer()) && ((currentQue + 1) < total_questions)) {
                            /* If answer is correct and questions remaining */
                            Log.d("Current value:", (currentQue.toString()));
                            answers.add(pAnswer);
                            isCorrect = true;
                            showCorrectAnswer();
                            correctDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            correctDialog.show();
                        }else if(pAnswer.equals(questionList.get(currentQue).getAnswer()) && ((currentQue + 1) == (total_questions))) {
                            /* If answer is correct and no questions remaining */
                            Log.d("Current value:", (currentQue.toString()));
                            isCorrect = true;
                            answers.add(pAnswer);
                            showCorrectAnswer();
                            sendResponse(true);
                        }
                    }
                }
            });
        }
    }

    private void showWrongAnswer() {
        if(userAnswer != null) {
            userAnswer.setTextColor(this.getResources().getColor(R.color.colorRed));
            userAnswer.setBackgroundResource(R.drawable.rounded_button_wrong);
        }
        userAnswer = null;
    }

    private void showCorrectAnswer() {
        userAnswer.setTextColor(this.getResources().getColor(R.color.colorGreen));
        userAnswer.setBackgroundResource(R.drawable.rounded_button_correct);
        userAnswer = null;
    }

    private void showCorrectAnswer(String answer) {
        if(option_1.getText().equals(answer)) {
            option_1.setTextColor(this.getResources().getColor(R.color.colorGreen));
            option_1.setBackgroundResource(R.drawable.rounded_button_correct);
        } else if(option_2.getText().equals(answer)) {
            option_2.setTextColor(this.getResources().getColor(R.color.colorGreen));
            option_2.setBackgroundResource(R.drawable.rounded_button_correct);
        } else if(option_3.getText().equals(answer)) {
            option_3.setTextColor(this.getResources().getColor(R.color.colorGreen));
            option_3.setBackgroundResource(R.drawable.rounded_button_correct);
        }
    }

    /* Method for changing question */
    public void changeQuestion(Integer currentQue) {
        Log.d("QuizFragment", "change question entered");
        progress = 0;
        if(currentQue < total_questions) {
            /* Resetting button for new question */
            resetButton();
            /* Enable button that are disable during previous question */
            enableButton();
            question.setText(questionList.get(currentQue).getQuestion());
            circleProgressView.setValue(0);
            option_1.setText(questionList.get(currentQue).getOptions().get(0));
            option_2.setText(questionList.get(currentQue).getOptions().get(1));
            option_3.setText(questionList.get(currentQue).getOptions().get(2));
            /* Executing async task for countdown timer */
            new TimerAsync().execute();
        }

    }

    private void setupViews() {

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

        option_1.setOnClickListener(this);
        option_2.setOnClickListener(this);
        option_3.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.option_1:
                pAnswer = option_1.getText().toString();
                userResponse.add(pAnswer);
                changeClickedButton(option_1);
                userAnswer = option_1;
                disableButton();
                break;
            case R.id.option_2:
                pAnswer = option_2.getText().toString();
                userResponse.add(pAnswer);
                userAnswer = option_2;
                changeClickedButton(option_2);
                disableButton();
                break;
            case R.id.option_3:
                pAnswer = option_3.getText().toString();
                userResponse.add(pAnswer);
                userAnswer = option_3;
                changeClickedButton(option_3);
                disableButton();
                break;
        }

    }

    /**
     * Change the color of selected option or button
     * @param button selected button
     */
    public void changeClickedButton(Button button) {
        button.setTextColor(this.getResources().getColor(R.color.colorTransparentWhite));
        button.setBackgroundResource(R.drawable.selected_option);
    }
    /* Resetting all button to default state */
    public void  resetButton() {
        option_1.setTextColor(this.getResources().getColor(R.color.colorSecondary));
        option_1.setBackgroundResource(R.drawable.rounded_button);

        option_2.setTextColor(this.getResources().getColor(R.color.colorSecondary));
        option_2.setBackgroundResource(R.drawable.rounded_button);

        option_3.setTextColor(this.getResources().getColor(R.color.colorSecondary));
        option_3.setBackgroundResource(R.drawable.rounded_button);
    }
    /* Enable all buttons for next question */
    public void enableButton() {
        option_1.setEnabled(true);
        option_2.setEnabled(true);
        option_3.setEnabled(true);
    }
    /* Disable all button after selecting one option */
    public void disableButton() {
        option_1.setEnabled(false);
        option_2.setEnabled(false);
        option_3.setEnabled(false);
    }
    /* Async task for count down timer */
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


