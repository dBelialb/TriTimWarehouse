package com.tritimwarehouse.models;

public class User {
    private String email, names, access, pass;

    public User() {
    }

    public User(String email, String names, String access, String pass) {
        this.email = email;
        this.names = names;
        this.access = access;
        this.pass = pass;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNames() {
        return names;
    }

    public void setNames(String names) {
        this.names = names;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
}
