package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class sendAllOnlineClient implements Runnable{
    Socket socket;
    Boolean ifWork;
    Boolean ifClose;
    List<Client> allOnlineClient;
    //List<String> tempAllOnlineClient;

    InputStream inputStream;
    OutputStream outputStream;

    BufferedReader in;
    BufferedWriter out;
    ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;
    public sendAllOnlineClient(Socket socket, Boolean ifWork, List<Client> allOnlineClient) throws IOException {
        this.socket = socket;
        this.ifWork = ifWork;
        this.ifClose = false;
        this.allOnlineClient = allOnlineClient;
        //this.tempAllOnlineClient = new ArrayList<>();

        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();

        this.in = new BufferedReader(new InputStreamReader(inputStream));
        this.out = new BufferedWriter(new OutputStreamWriter(outputStream));

        this.objectInputStream = new ObjectInputStream(inputStream);
        this.objectOutputStream = new ObjectOutputStream(outputStream);
    }
    @Override
    public void run() {
        while (!ifClose) {
            if (ifWork) {
                List<String> copy = new ArrayList<>();
                allOnlineClient.forEach(client -> copy.add(client.getUserName()));
                try {
                    //objectOutputStream.reset();
                    objectOutputStream.writeObject(copy);
                    objectOutputStream.flush();
                } catch (IOException e) {
                    //System.out.println("Break");
                    throw new RuntimeException(e);
                }
                ifWork = false;
            }
        }
    }

    public void setIfWork(boolean ifWork){
        this.ifWork = ifWork;
    }

    public void closeSending() throws IOException {
        ifClose = true;
    }
}