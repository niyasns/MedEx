package com.example.android.medex;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.special.ResideMenu.ResideMenu;

import java.util.ArrayList;
import java.util.List;

import at.grabner.circleprogress.CircleProgressView;

public class QuizFragment extends android.app.Fragment implements View.OnClickListener {

    private View parentView;
    private ResideMenu resideMenu;

    Button option_1;
    Button option_2;
    Button option_3;

    TextView heading;
    TextView question;

    static Integer counter;
    static Integer progress;
    Runnable r;
    Integer total_questions;
    Boolean isCorrect;
    QuizSet quizSet;
    List<Question> questionList;
    static CountDownTimer countDownTimer;
    static CountDownTimer spinTimer;

    static String pAnswer;
    ArrayList<String> userResponse;


    static CircleProgressView circleProgressView;

    private HomeActivity parentActivity;

    WrongDialog wrongDialog;
    CorrectDialog correctDialog;
    CompleteDialog completeDialog;

    List QuizList;

    public QuizFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.quiz_fragment, container, false);
        setupViews();
        setupQuiz();
        setupFirebaseRealtimeListner();
        return parentView;
    }

    private void setupFirebaseRealtimeListner() {


    }

    private void setupQuiz() {

        counter = 0;
        progress = 0;
        userResponse = new ArrayList<>();
        isCorrect = true;
        QuizList = parentActivity.getQuizList();
        quizSet = (QuizSet) QuizList.get(0);
        questionList = quizSet.getQuestions();
        total_questions = questionList.size();
        circleProgressView.setMaxValue(quizSet.getTimeOut());

        countDownTimer = new CountDownTimer(quizSet.getTimeOut() * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                progress++;
                circleProgressView.setValue(progress);
            }

            @Override
            public void onFinish() {
                Log.d("QuizFragment", "count down finished");
                circleProgressView.setValue(quizSet.getTimeOut());
            }
        };

        changeQuestion();

        circleProgressView.setOnProgressChangedListener(new CircleProgressView.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(float value) {
                if(value == quizSet.getTimeOut()) {
                    Log.d("QuizFragment", "onprogresscahnged entered");
                    if(pAnswer.equals(questionList.get(counter - 1).getAnswer()) && (counter < total_questions)) {
                        correctDialog = new CorrectDialog(parentActivity);
                        correctDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        correctDialog.show();
                    }else if(pAnswer.equals(questionList.get(counter - 1).getAnswer()) && (counter.equals(total_questions))) {
                        counter = 0;
                        completeDialog = new CompleteDialog(parentActivity);
                        completeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        completeDialog.show();
                    } else {
                        wrongDialog = new WrongDialog(parentActivity);
                        wrongDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        wrongDialog.show();
                    }
                }
            }
        });

        option_1.setOnClickListener(this);
        option_2.setOnClickListener(this);
        option_3.setOnClickListener(this);
    }

    public void changeQuestion() {
        Log.d("QuizFragment", "change fragment entered");
        progress = 0;
        if(counter < total_questions) {
            resetButton();
            enableButton();
            question.setText(questionList.get(counter).getQuestion());
            circleProgressView.setValue(0);
            option_1.setText(questionList.get(counter).getOptions().get(0));
            option_2.setText(questionList.get(counter).getOptions().get(1));
            option_3.setText(questionList.get(counter).getOptions().get(2));
            new TimerAsync().execute();
        }

    }

    private void setupViews() {
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
            counter++;
        }
    }
}
