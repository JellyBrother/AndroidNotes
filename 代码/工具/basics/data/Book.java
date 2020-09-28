package com.jelly.app.main.basics.data;

import androidx.annotation.NonNull;

public class Book {
    private final static String TAG = "BookTag";
    private String name;

    public Book() {
    }

    private Book(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return "Book{name=" + name + "}";
    }

    private String getContent(String text) {
        return "content:" + text;
    }
}
