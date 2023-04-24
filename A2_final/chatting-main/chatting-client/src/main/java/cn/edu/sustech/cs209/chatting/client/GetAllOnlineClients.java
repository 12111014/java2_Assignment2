package cn.edu.sustech.cs209.chatting.client;


import java.io.*;
import java.net.Socket;
import java.util.List;

public class GetAllOnlineClients implements Runnable {
    Boolean ifWork;
    Boolean ifClose;
    User user;
    Socket socketForAllOnlineClient;

    InputStream inputStream;
    ObjectInputStream objectInputStream;

    public GetAllOnlineClients(
            User user, Boolean ifWork, Socket socketForAllOnlineClient) throws IOException {
        this.user = user;
        this.ifClose = false;
        this.ifWork = ifWork;
        this.socketForAllOnlineClient = socketForAllOnlineClient;

        this.inputStream = socketForAllOnlineClient.getInputStream();
        this.objectInputStream = new ObjectInputStream(inputStream);

    }

    @Override
    public void run() {
        try {
            String command;
            o:
            while ((command = (String) objectInputStream.readObject()) != null) {
                switch (command) {
                    case "exit": {
                        break o;
                    }
                    case "onlineList": {
                        List<String> temp = (List<String>) objectInputStream.readObject();
                        user.setAllOnlineClient(temp);
                        break;
                    }
                    case "quitMember": {
                        String roomCode = (String) objectInputStream.readObject();
                        String quitMember = (String) objectInputStream.readObject();
                        user.getQuitGroupRequest(roomCode, quitMember);
                        break;
                    }
                    default: {
                        System.out.println("Else Type: " + command);
                        break;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            user.outPutError();
        }
    }

}