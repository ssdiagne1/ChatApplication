/*
 * User class
 * Samba Diagne
 */
package edu.uncc.hw08;

import java.io.Serializable;

public class User implements Serializable {

    String name,userID,email;
    boolean status;

    public User() {
    }

    public User(String name, String userID,String email, boolean status) {
        this.name = name;
        this.userID = userID;
        this.status = status;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
