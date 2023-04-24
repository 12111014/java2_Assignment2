package cn.edu.sustech.cs209.chatting.client;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class getAllOnlineClients implements Runnable{
    Boolean ifWork;
    Boolean ifClose;
    User user;
    Socket socketForAllOnlineClient;

    OutputStream outputStream;
    InputStream inputStream;

    BufferedWriter textOut;
    BufferedReader textIn;

    ObjectOutputStream objectOutputStream;
    ObjectInputStream objectInputStream;

    public getAllOnlineClients(User user,Boolean ifWork, Socket socketForAllOnlineClient) throws IOException {
        this.user = user;
        this.ifClose = false;
        this.ifWork = ifWork;
        this.socketForAllOnlineClient = socketForAllOnlineClient;

        //this.outputStream = socketForAllOnlineClient.getOutputStream();
        this.inputStream = socketForAllOnlineClient.getInputStream();

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
                switch (command) {
                    case "exit": {
                        break o;
                    }
                    case "onlineList": {
                        List<String> temp = (List<String>) objectInputStream.readObject();
                        user.setAllOnlineClient(temp);
                        break;
                    }
                    case "quitMember":{
                        String roomCode = (String) objectInputStream.readObject();
                        String quitMember = (String) objectInputStream.readObject();
                        user.getQuitGroupRequest(roomCode,quitMember);
                        break;
                    }
                }
            }
        }catch (IOException | ClassNotFoundException e) {
            user.outPutError();
            //user.serveException();
            //throw new RuntimeException(e);
        }
    }

}