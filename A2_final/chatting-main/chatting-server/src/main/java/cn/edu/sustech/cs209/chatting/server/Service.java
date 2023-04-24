package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Service implements Runnable {
    private Socket socket;
    private Socket socketForClient;
    private Socket socketForMessage;

    private List<Client> clientList;
    private List<Client> onlineClientList;
    private List<Room> allOnlineRoom;

    private InputStream inputStream;
    private OutputStream outputStream;
    private OutputStream outputStreamForOnlineClient;
    private OutputStream outputStreamForMessage;

    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private ObjectOutputStream objectOutputStreamForOnlineClient;
    private ObjectOutputStream objectOutputStreamForMessage;

    private Client userClient;

    private List<Service> allService;

    public Service(Socket socket, Socket socketForClient, Socket socketForMessage,
                   List<Client> clientList, List<Client> onlineClientList,
                   List<Service> allService, List<Room> allOnlineRoom) throws IOException {
        this.socket = socket;
        this.socketForClient = socketForClient;
        this.socketForMessage = socketForMessage;

        this.clientList = clientList;
        this.onlineClientList = onlineClientList;
        this.allOnlineRoom = allOnlineRoom;

        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();

        this.objectInputStream = new ObjectInputStream(inputStream);
        this.objectOutputStream = new ObjectOutputStream(outputStream);

        this.outputStreamForOnlineClient = socketForClient.getOutputStream();
        this.objectOutputStreamForOnlineClient =
                new ObjectOutputStream(outputStreamForOnlineClient);

        this.outputStreamForMessage = socketForMessage.getOutputStream();
        this.objectOutputStreamForMessage = new ObjectOutputStream(outputStreamForMessage);

        this.allService = allService;
    }

    @Override
    public void run() {
        try {
            o:
            while (true) {
                String command;
                if ((command = (String) objectInputStream.readObject()) != null) {
                    switch (command) {
                        case "login": {
                            checkLogin();
                            break;
                        }
                        case "exit": {
                            String userName = (String) objectInputStream.readObject();
                            logOut(userName);
                            System.out.println(userClient.getUserName() + " has disConnected");
                            break o;
                        }
                        case "sendMessage": {
                            sendMessage();
                            break;
                        }
                        case "sendGroupMessage": {
                            sendGroupMessage();
                            break;
                        }
                        case "quitGroup": {
                            sendGroupQuitRequest();
                            break;
                        }
                        default:
                            System.out.println("Else case:" + command);
                            break;
                    }
                }
            }

        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public void sendGroupQuitRequest() throws IOException, ClassNotFoundException {
        String roomCode = (String) objectInputStream.readObject();
        if (roomCode != null) {
            List<String> newMemberList = (List<String>) objectInputStream.readObject();
            System.out.println("new Member");
            newMemberList.forEach(o -> System.out.print("{" + o + "}"));
            System.out.println();
            for (Service s : allService) {
                if (newMemberList.contains(s.getUserClient().getUserName())) {
                    s.sendGroupQuitRequestToClient(roomCode, userClient.getUserName());
                }
            }
        }
    }

    public void sendGroupQuitRequestToClient(
            String roomCode, String quitMemberName) throws IOException {
        objectOutputStreamForOnlineClient.writeObject("quitMember");
        objectOutputStreamForOnlineClient.writeObject(roomCode);
        objectOutputStreamForOnlineClient.writeObject(quitMemberName);
    }

    public void checkLogin() throws IOException, ClassNotFoundException, InterruptedException {
        String userName = (String) objectInputStream.readObject();
        Client[] user = new Client[1];
        String info = checkUser(userName, user);
        if (info.equals("yes")) {
            userClient = user[0];
            objectOutputStream.writeObject("YES");
            objectOutputStream.writeObject(user[0].getUserName());

            onlineClientList.add(user[0]);

            for (Service s : allService) {
                s.sendAllOnlineClient();
            }
            System.out.println("User Connect:" + user[0].getUserName());
        } else {
            if (info.equals("er0")) {
                objectOutputStream.writeObject("NO0");  // note user doesn't exist
            } else if (info.equals("er1")) {
                objectOutputStream.writeObject("NO1");   // note the user has online
            }

        }
    }

    private void logOut(String userName) throws IOException {

        objectOutputStreamForOnlineClient.writeObject("exit");
        objectOutputStreamForMessage.writeObject("exit");

        if (onlineClientList.size() > 0) {
            for (int i = 0; i < onlineClientList.size(); i++) {
                if (onlineClientList.get(i).getUserName().equals(userName)) {
                    onlineClientList.remove(i);
                    break;
                }
            }
        }
        if (allService.size() > 0) {
            for (int i = 0; i < allService.size(); i++) {
                if (allService.get(i).getUserClient().getUserName().equals(userName)) {
                    allService.remove(i);
                    break;
                }
            }
        }
        if (allService.size() > 0) {
            for (Service s : allService) {
                s.infoSomeOneLogOut(userName);
                s.sendAllOnlineClient();
            }
        }
        socketForClient.close();
        socketForMessage.close();
        socket.close();
    }

    private String checkUser(String username, Client[] user) {
        for (int i = 0; i < onlineClientList.size(); i++) {
            if (username.equals(onlineClientList.get(i).getUserName())) {
                user[0] = null;
                return "er1";
            }
        }
        for (int i = 0; i < clientList.size(); i++) {
            if (username.equals(clientList.get(i).getUserName())) {
                user[0] = clientList.get(i);
                return "yes";
            }
        }
        user[0] = null;
        return "er0";
    }

    public void infoSomeOneLogOut(String userName) throws IOException {
        System.out.println(userClient.getUserName() + " : " + userName + "试图断开链接");
        objectOutputStreamForMessage.writeObject("infoLogOut");
        objectOutputStreamForMessage.writeObject(userName);
    }

    public Client getUserClient() {
        return userClient;
    }

    private void sendMessage() throws IOException, ClassNotFoundException {
        Message m = (Message) objectInputStream.readObject();
        if (m != null) {
            String sendToName = m.getSendTo();
            for (Service s : allService) {
                if (s.getUserClient().getUserName().equals(sendToName)) {
                    s.sendMessageToClient(m);
                    System.out.println("服务器成功接受并转送消息");
                    break;
                }
            }
        }
    }

    private void sendGroupMessage() throws IOException, ClassNotFoundException {
        Message m = (Message) objectInputStream.readObject();
        if (m != null) {
            List<String> sendToName = m.getGroupSendTo();
            for (Service s : allService) {
                if (sendToName.contains(s.getUserClient().getUserName())
                        && !s.getUserClient().getUserName().equals(m.getSentBy())) {
                    s.sendMessageToGroup(m);
                    System.out.println(s.getUserClient().getUserName() + "服务器成功接受并转送消息");
                }
            }
        }
    }

    public void sendMessageToGroup(Message m) throws IOException {
        objectOutputStreamForMessage.writeObject("message");
        objectOutputStreamForMessage.writeObject(m);

        System.out.println("服务器成功发送消息");
    }

    public void sendMessageToClient(Message m) throws IOException {
        objectOutputStreamForMessage.writeObject("message");
        objectOutputStreamForMessage.writeObject(m);

        System.out.println("服务器成功发送消息");
    }

    public void sendAllOnlineClient() throws IOException {
        objectOutputStreamForOnlineClient.writeObject("onlineList");

        List<String> copy = new ArrayList<>();
        onlineClientList.forEach(client -> copy.add(client.getUserName()));
        objectOutputStreamForOnlineClient.writeObject(copy);
    }
}
