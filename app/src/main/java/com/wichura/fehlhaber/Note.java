package com.wichura.fehlhaber;

import com.google.firebase.Timestamp;

public class Note {

    public String getLast() {
        return last;
    }
    public Timestamp  getDate() {
        return date;
    }

    private String last;
    private Timestamp  date;
    private Boolean bilder;

    public Note() { }

    public Note(String last, Timestamp date, Boolean bilder) {

        this.last = last;
        this.date = date;
        this.bilder = bilder;
    }

    public Boolean getBilder() {
        return bilder;
    }
}
