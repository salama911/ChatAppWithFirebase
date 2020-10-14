package com.example.chatapp.Model;

public class User  {
    private String id;
    private String username;
    private String imageURL;

    public User(String id, String username, String imageurl) {
        this.id = id;
        this.username = username;
        this.imageURL = imageurl;
    }

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageurl() {
        return imageURL;
    }

    public void setImageurl(String imageurl) {
        this.imageURL = imageurl;
    }
}
