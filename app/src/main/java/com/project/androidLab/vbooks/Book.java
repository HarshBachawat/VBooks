package com.project.androidLab.vbooks;


public class Book {

    private String title, imageUrl, username;

    public Book(String title, String desc, String imageUrl, String username) {
        this.title = title;
        this.imageUrl=imageUrl;
        this.username = username;
    }

    public Book() {
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTitle() {
        return title;
    }

}
