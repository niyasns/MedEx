package com.example.android.medex;

import com.google.firebase.Timestamp;

import java.sql.Time;

/**
 * Class to represent module data.
 */
public class Module {

    String fileId;
    String fileName;
    String subject;
    Timestamp time;
    String type;
    String url;

    public Module() {

    }

    public Module(String fileId, String fileName, String subject, Timestamp time, String type, String url) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.subject = subject;
        this.time = time;
        this.type = type;
        this.url = url;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
