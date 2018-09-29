package com.niyas.android.medex;

import java.util.ArrayList;
import java.util.HashMap;

public class Year {

    ArrayList<String> subList;
    HashMap<String, ArrayList<String>> topics;

    public ArrayList<String> getSubList() {
        return subList;
    }

    public void setSubList(ArrayList<String> subList) {
        this.subList = subList;
    }

    public HashMap<String, ArrayList<String>> getTopics() {
        return topics;
    }

    public void setSubjects(HashMap<String, ArrayList<String>> topics) {
        this.topics = topics;
    }

    public Year(ArrayList<String> subList, HashMap<String, ArrayList<String>> topics) {

        this.subList = subList;
        this.topics = topics;
    }
}
