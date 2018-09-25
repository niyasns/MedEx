package com.example.android.medex;

import com.google.firebase.Timestamp;

class Video {

    String subject;
    Timestamp time;
    String title;
    String topic;
    Long type;
    String url;
    String videoId;

    public Video(String subject, Timestamp time, String title, String topic, Long type, String url, String videoId) {
        this.subject = subject;
        this.time = time;
        this.title = title;
        this.topic = topic;
        this.type = type;
        this.url = url;
        this.videoId = videoId;
    }

    public String getSubject() {
        return subject;
    }

    public Timestamp getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    public String getTopic() {
        return topic;
    }

    public Long getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getVideoId() {
        return videoId;
    }
}
