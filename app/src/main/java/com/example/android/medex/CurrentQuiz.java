package com.example.android.medex;

import com.google.firebase.Timestamp;

public class CurrentQuiz {

    Timestamp lastUpdated;
    String qId;
    Integer qNo;

    public CurrentQuiz() {

    }

    public CurrentQuiz(Timestamp lastUpdated, String qId, Integer qNo) {
        this.lastUpdated = lastUpdated;
        this.qId = qId;
        this.qNo = qNo;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getqId() {
        return qId;
    }

    public void setqId(String qId) {
        this.qId = qId;
    }

    public Integer getqNo() {
        return qNo;
    }

    public void setqNo(Integer qNo) {
        this.qNo = qNo;
    }
}
