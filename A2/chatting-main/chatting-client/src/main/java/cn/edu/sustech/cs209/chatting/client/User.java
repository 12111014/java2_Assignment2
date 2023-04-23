package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class User {
    Socket socket;
    Socket socketForAllOnlineClient;
    Socket socketForMessage;

    Thread tForClient;
    Thread tForMessage;

    String clientName;
    getAllOnlineClients gc;
    getMessage gm;

    List<String> allOnlineClient;
    //私聊人员表
    List<String> chosenChatClientList;
    //房间号
    List<String> groupChatCodeList;
    //消息记录
    Map<String,List<Message>> chatInfo;
    //房间对应的人
    Map<String,List<String>> roomMember;

    String chatClientName ;

    OutputStream outputStream;
    InputStream inputStream;

    //BufferedWriter textOut;
    //BufferedReader textIn;

    ObjectOutputStream objectOutputStream;
    ObjectInputStream objectInputStream;

    Controller mainController;
    public User(Socket socket, Socket socketForAllOnlineClient, Socket socketForMessage)
            throws IOException {
        this.socket = socket;
        this.socketForAllOnlineClient = socketForAllOnlineClient;
        this.socketForMessage = socketForMessage;

        this.allOnlineClient = new ArrayList<>();
        this.chatClientName = "Unselected";
        this.chosenChatClientList = new ArrayList<>();
        this.groupChatCodeList = new ArrayList<>();
        this.roomMember = new HashMap<>();
        List<Message> tempChatInfo = new ArrayList<>();
        this.chatInfo = new HashMap<>();
        chatInfo.put(chatClientName,tempChatInfo);

        this.outputStream = socket.getOutputStream();
        this.inputStream = socket.getInputStream();

        //this.textOut = new BufferedWriter(new OutputStreamWriter(outputStream));
        //this.textIn = new BufferedReader(new InputStreamReader(inputStream));

        this.objectOutputStream = new ObjectOutputStream(outputStream);
        this.objectInputStream = new ObjectInputStream(inputStream);

        this.gc = new getAllOnlineClients(this,false, socketForAllOnlineClient);
        this.gm = new getMessage(this,false, socketForMessage);
    }

    public boolean login() throws IOException, ClassNotFoundException {
        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("Login");
        dialog.setHeaderText(null);
        dialog.setContentText("Username:");

        Optional<String> input = dialog.showAndWait();
        if (!input.isPresent()){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Checking Exit");
            alert.setHeaderText(null);
            alert.setContentText("Do you want to Exit?");
            Optional<ButtonType> exitResult = alert.showAndWait();
            if(exitResult.get() == ButtonType.OK){
                objectOutputStream.writeObject("exit");
                objectOutputStream.writeObject(clientName);
                //textOut.flush();
                socket.close();
                Platform.exit();
                return false;
            }else {
                return login();
            }
        }
        else if (!input.get().isEmpty()){
            String tempName = input.get();
            objectOutputStream.writeObject("login");
            objectOutputStream.writeObject(tempName);
            //textOut.flush();
            String response = (String) objectInputStream.readObject();
            switch (response){
                case "YES":{
                    setClientName((String)objectInputStream.readObject());
                    //get all client
                    tForClient = new Thread(gc);
                    tForClient.start();
                    //get message
                    tForMessage = new Thread(gm);
                    tForMessage.start();
                    return true;
                }
                case "NO0":{
                    loginFailed("User doesn't exist");
                    return login();
                }
                case "NO1":{
                    loginFailed("The user has online");
                    return login();
                }
                default:{
                    System.out.println("Else types");
                    return false;
                }
            }

        }
        else{
            loginFailed("Empty input");
            return login();
        }
    }

    public void logout() throws IOException, InterruptedException {
        //serve
        objectOutputStream.writeObject("exit");
        objectOutputStream.writeObject(clientName);
        //textOut.flush();

        Thread.sleep(100); //保证服务器有时间处理关闭信息
        //client
        socketForMessage.close();
        socketForAllOnlineClient.close();
        socket.close();
    }

    public void infoUserLogOut(String name){
        System.out.println("已接受：" +name +" 断开连接");
        if(chosenChatClientList.contains(name)){
            Platform.runLater(()->{
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Info");
                info.setHeaderText(null);
                info.setContentText("User: " + name + " has disconnected");
                info.show();
            });
        }
        roomMember.forEach((k,v)->{
            v.remove(name);
        });
        Platform.runLater(() ->{
            mainController.updateChatClientList();
            mainController.updateChatClientNameLabel();
        });


    }

    public static void showTimedDialog() {
        Stage popup = new Stage();
        popup.setAlwaysOnTop(true);
        popup.setOpacity(0.7);
        popup.initModality(Modality.APPLICATION_MODAL);
        Label Info = new Label("NEW MESSAGE");
        Info.setFont(new Font("Arial", 30));
        Scene scene = new Scene(Info);
        popup.setResizable(false);
        popup.setX(1200);
        popup.setY(750);
        popup.setScene(scene);
        popup.setTitle("提示信息");
        popup.show();

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(2000);
                if (popup.isShowing()) {
                    Platform.runLater(() -> popup.close());
                }
            } catch (Exception exp) {
                exp.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void getMessage(Message m){
        if(!m.getIfGroup()) {
            //左侧聊天框更新
            if (!chosenChatClientList.contains(m.getSentBy())) {
                chosenChatClientList.add(m.getSentBy());
                Platform.runLater(() -> mainController.updateChatClientList());
            }
            //更新消息记录
            if (chatInfo.get(m.getSentBy()) == null) {
                List<Message> chat = new ArrayList<>();
                chat.add(m);
                chatInfo.put(m.getSentBy(), chat);
                System.out.println("成功接收消息");
            } else {
                chatInfo.get(m.getSentBy()).add(m);
                System.out.println("成功接收消息");
            }
            //判断接受消息的时候目前用户是否是聊天用户
            if (chatClientName.equals(m.getSentBy())) {
                //右侧聊天框更新
                Platform.runLater(() -> mainController.updateChatContent());
            }else {
                Platform.runLater(User::showTimedDialog);
            }
        }else {
            if(!groupChatCodeList.contains(m.getGroupChatRoomCode())){
                groupChatCodeList.add(m.getGroupChatRoomCode());
                //左侧聊天框更新
                Platform.runLater(() -> mainController.updateChatClientList());
            }
            if(!roomMember.containsKey(m.getGroupChatRoomCode())){
                roomMember.put(m.getGroupChatRoomCode(),m.getGroupSendTo());
            }
            if(chatInfo.get(m.getGroupChatRoomCode()) == null){
                List<Message> chat = new ArrayList<>();
                chat.add(m);
                chatInfo.put(m.getGroupChatRoomCode(), chat);
                System.out.println("成功接收群聊消息");
            } else {
                chatInfo.get(m.getGroupChatRoomCode()).add(m);
                System.out.println("成功接收群聊消息");
            }
            if (chatClientName.equals(m.getGroupChatRoomCode())) {
                //右侧聊天框更新
                Platform.runLater(() -> mainController.updateChatContent());
            }
        }
    }

    public void sendMessage(Message m) throws IOException {
        if(!m.getIfGroup()) {
            if (chatInfo.get(m.getSendTo()) == null) {
                List<Message> chat = new ArrayList<>();
                chat.add(m);
                chatInfo.put(m.getSendTo(), chat);
            } else {
                chatInfo.get(m.getSendTo()).add(m);
            }
            objectOutputStream.writeObject("sendMessage");
            //textOut.flush();

            //objectOutputStream.reset();
            objectOutputStream.writeObject(m);
            //objectOutputStream.flush();

            //右侧聊天框更新
            //mainController.updateChatContent();

            System.out.println("成功发送信息");
            chatInfo.get(m.getSendTo()).forEach(o -> {
                System.out.print(o.getData() + " ");
            });
            System.out.println();
        }else {
            if(chatInfo.get(m.getGroupChatRoomCode()) == null){
                List<Message> chat = new ArrayList<>();
                chat.add(m);
                chatInfo.put(m.getGroupChatRoomCode(), chat);
            } else {
                chatInfo.get(m.getGroupChatRoomCode()).add(m);
            }

            objectOutputStream.writeObject("sendGroupMessage");

            objectOutputStream.writeObject(m);

            System.out.println("成功发送群聊信息");
            chatInfo.get(m.getGroupChatRoomCode()).forEach(o -> {
                System.out.print(o.getData() + " ");
            });
            System.out.println();
            System.out.println("发送对象：");
            m.getGroupSendTo().forEach(o->System.out.print("{" + o + "}"));
            System.out.println();
        }
    }

    public void sendQuitGroupRequest(String roomNumber) throws IOException {
        groupChatCodeList.remove(roomNumber);
        roomMember.get(roomNumber).remove(clientName);
        //发送指令
        objectOutputStream.writeObject("quitGroup");
        objectOutputStream.writeObject(roomNumber);
        //传输新的房间用户
        List<String> tempNew = new ArrayList<>(roomMember.get(roomNumber));
        objectOutputStream.writeObject(tempNew);
        System.out.println("成功发送退群要求");
        roomMember.get(roomNumber).forEach(o->System.out.print("[" + o + "] "));
        System.out.println();
    }

    public void getQuitGroupRequest(String roomCode,String quitMember){
        if(roomMember.get(roomCode)!=null){
            roomMember.get(roomCode).remove(quitMember);
            Platform.runLater(() -> mainController.updateChatClientNameLabel());
            System.out.println("成功处理退群请求");
            roomMember.get(roomCode).forEach(o->System.out.print("{" + o + "}"));
        }
    }

    public void serveException(){
        Platform.runLater(()->{
            Alert serveOut = new Alert(Alert.AlertType.ERROR);
            serveOut.setTitle("Error");
            serveOut.setHeaderText(null);
            serveOut.setContentText("The Server has Log out");
            serveOut.showAndWait();
        });

        Platform.exit();
        System.out.println("SERVE ERROR");
    }

    // Getter and Setter

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public ObjectOutputStream getObjectOutputStream() {
        return objectOutputStream;
    }

    public ObjectInputStream getObjectInputStream() {
        return objectInputStream;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public List<String> getAllOnlineClient() {
        return allOnlineClient;
    }

    public void setAllOnlineClient(List<String> allOnlineClient) {
        this.allOnlineClient = allOnlineClient;
        if(mainController!=null) {
            Platform.runLater(() -> mainController.updateCurrentOnlineCnt());
        }
    }

    public String getChatClientName() {
        return chatClientName;
    }

    public void setChatClientName(String chatClientName) {
        this.chatClientName = chatClientName;
    }

    public List<String> getChosenChatClientList() {
        return chosenChatClientList;
    }

    public Map<String, List<Message>> getChatInfo() {
        return chatInfo;
    }


    public void setMainController(Controller mainController) {
        this.mainController = mainController;
    }

    public List<String> getGroupChatCodeList() {
        return groupChatCodeList;
    }

    public Map<String, List<String>> getRoomMember() {
        return roomMember;
    }

    public void setRoomMember(Map<String, List<String>> roomMember) {
        this.roomMember = roomMember;
    }

    // Exception

    public void loginFailed(String invalidInfo){
        Alert WarningForInvalidLogin = new Alert(Alert.AlertType.ERROR);
        WarningForInvalidLogin.setTitle("Login");
        WarningForInvalidLogin.setHeaderText(null);
        WarningForInvalidLogin.setContentText(invalidInfo);
        WarningForInvalidLogin.showAndWait();
        System.out.println("Invalid: " + invalidInfo);
    }

    public void throwingError(String info){
        Alert WarningForInvalidLogin = new Alert(Alert.AlertType.ERROR);
        WarningForInvalidLogin.setTitle("Error");
        WarningForInvalidLogin.setHeaderText(null);
        WarningForInvalidLogin.setContentText(info);
        WarningForInvalidLogin.showAndWait();
        System.out.println("Error" + info);
    }

    public synchronized void outPutError() {
        Platform.runLater(() -> {
            Alert serveOut = new Alert(Alert.AlertType.ERROR);
            serveOut.setTitle("Error");
            serveOut.setHeaderText(null);
            serveOut.setContentText("The Server has Log out");
            serveOut.showAndWait();


            Platform.exit();
            System.exit(0);
        });

    }
}
