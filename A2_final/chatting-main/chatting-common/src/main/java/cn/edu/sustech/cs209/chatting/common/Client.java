package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

public class Client implements Serializable {

    private int id;

    private String userName;

    private String password;


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

}
