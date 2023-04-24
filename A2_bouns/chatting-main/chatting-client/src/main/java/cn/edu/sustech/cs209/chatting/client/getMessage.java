package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.*;
import java.net.Socket;

public class getMessage implements Runnable {
    Boolean ifWork;
    Boolean ifClose;
    User user;
    Socket socketForMessage;

    OutputStream outputStream;
    InputStream inputStream;

    BufferedWriter textOut;
    BufferedReader textIn;

    ObjectOutputStream objectOutputStream;
    ObjectInputStream objectInputStream;

    public getMessage(User user, Boolean ifWork, Socket socketForMessage) throws IOException {
        this.user = user;
        this.ifClose = false;
        this.ifWork = ifWork;
        this.socketForMessage = socketForMessage;

        //this.outputStream = socketForMessage.getOutputStream();
        this.inputStream = socketForMessage.getInputStream();

        //this.textOut = new BufferedWriter(new OutputStreamWriter(outputStream));
        //this.textIn = new BufferedReader(new InputStreamReader(inputStream));

        //this.objectOutputStream = new ObjectOutputStream(outputStream);
        this.objectInputStream = new ObjectInputStream(inputStream);

    }

    @Override
    public void run() {
        try {
            String command;
            o:while ((command = (String) objectInputStream.readObject()) != null) {
                switch (command){
                    case "exit":{
                        break o;
                    }
                    case "message":{
                        Message m = (Message) objectInputStream.readObject();
                        user.getMessage(m);
                        break;
                    }
                    case "infoLogOut":{
                        String logOutUserName = (String) objectInputStream.readObject();
                        user.infoUserLogOut(logOutUserName);
                        break ;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }

            user.outPutError();
            //user.serveException();
            //throw new RuntimeException(e);
        }
    }

}

