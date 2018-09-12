package com.example.android.medex;

import com.google.firebase.Timestamp;

import java.util.List;

/* Quiz set model */
class QuizSet {

    private boolean completed;
    private boolean started;
    private List<Question> questions;
    private String quizId;
    private Timestamp scheduledTime;
    private Integer timeOut;
    String title;

    public QuizSet()
    {

    }

    public QuizSet(boolean completed, boolean started, List<Question> questions, String quizId, Timestamp scheduledTime, Integer timeOut, String title) {
        this.completed = completed;
        this.started = started;
        this.questions = questions;
        this.quizId = quizId;
        this.scheduledTime = scheduledTime;
        this.timeOut = timeOut;
        this.title = title;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public String getQuizId() {
        return quizId;
    }

    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }

    public Timestamp getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(Timestamp scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public Integer getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(Integer timeOut) {
        this.timeOut = timeOut;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
