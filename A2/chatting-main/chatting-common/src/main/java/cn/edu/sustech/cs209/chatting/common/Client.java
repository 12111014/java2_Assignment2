package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Client implements Serializable{

    private int id;

    private String userName;

    private String password;

    //private List<Client> friends;

    public Client(int id, String userName, String password) {
        this.id = id;
        this.userName = userName;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    /*
    public List<Client> getFriends() {
        return friends;
    }

    public void setFriends(List<Client> friends){
        this.friends = friends;
    }

     */
}
