package org.ape.control;

import org.ape.layout.LogListView;

import java.util.Date;

public class SimpleLogRecord {

    private Date date;
    private String context;
    private String message;

    public SimpleLogRecord(String context, String message) {
        this.date = new Date();
        this.context = context;
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public String getContext() {
        return context;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return LogListView.getFormatter().format(this.getDate()) + " " + this.getContext() + " " + this.getMessage();
    }
}