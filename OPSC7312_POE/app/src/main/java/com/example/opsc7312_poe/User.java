package com.example.opsc7312_poe;

public class User {
    String username;
    String email;
    String password;
    String measurement;

    public User(String username, String email, String password){

        this.username = username;
        this.email = email;
        this.password = password;
    }

    public User(String username, String email, String password, String measurement){

        this.username = username;
        this.email = email;
        this.password = password;
        this.measurement = measurement;
    }
}
