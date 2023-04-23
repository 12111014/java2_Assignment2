package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;

import java.io.*;
import java.net.Socket;

public class transMessage implements Runnable{
    Socket socket;
    Boolean ifWork;
    Boolean ifClose;

    InputStream inputStream;
    OutputStream outputStream;

    BufferedReader in;
    BufferedWriter out;
    ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;

    Message message;
    public transMessage(Socket socket, Boolean ifWork) throws IOException {
        this.socket = socket;
        this.ifWork = ifWork;
        this.ifClose = false;

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
                try {
                    //objectOutputStream.reset();
                    objectOutputStream.writeObject(message);
                    objectOutputStream.flush();
                    ifWork = false;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void workOnce(boolean ifWork, Message m){
        this.message = m;
        this.ifWork = ifWork;
    }

    public void closeSending() throws IOException {
        ifClose = true;
    }
}