package com.example.curtain.model;

public class ModelUser {
    String created_at, username, email, password, phone, uid, user_status, user_type;

    public ModelUser() {
    }

    public ModelUser(String created_at, String username, String email, String password,
                     String phone, String uid, String user_status, String user_type) {
        this.created_at = created_at;
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.uid = uid;
        this.user_status = user_status;
        this.user_type = user_type;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUser_status() {
        return user_status;
    }

    public void setUser_status(String user_status) {
        this.user_status = user_status;
    }

    public String getUser_type() {
        return user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }
}
