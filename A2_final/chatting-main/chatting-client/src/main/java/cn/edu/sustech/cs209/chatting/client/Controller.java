package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;



public class Controller implements Initializable {
    @FXML
    ListView<Message> chatContentList;
    @FXML
    ListView<String> chatList;
    @FXML
    Label currentUsername;
    @FXML
    Label chatClientName;
    @FXML
    TextArea inputArea;
    @FXML
    Label currentOnlineCnt;


    //后端
    User user;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            user = Main.getUser();
            if (user.login()) {
                Thread.sleep(100);
                ObservableList<Message> chatContent = FXCollections
                        .observableArrayList(user.getChatInfo().get(user.getChatClientName()));
                chatContentList.setCellFactory(new MessageCellFactory());
                chatContentList.setItems(chatContent);
                //左下角用户名称
                currentUsername.setText("Current User: " + user.getClientName());
                //左侧用户名单
                ObservableList<String> chatClientNameList = FXCollections
                        .observableArrayList(user.getChosenChatClientList());
                chatList.setItems(chatClientNameList);
                //在线人数
                currentOnlineCnt.setText(String.valueOf(user.getAllOnlineClient().size()));
                //下方聊天对象名字
                chatClientName.setText(user.getChatClientName());
            }
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //更新左侧聊天用户
    public void updateChatClientList() {
        user.chosenChatClientList.sort((o1, o2) -> {
            if (user.chatInfo.get(o1).size() > 0
                    && user.chatInfo.get(o2).size() > 0) {
                Date t1 = new Date(user.chatInfo.get(o1)
                        .get(user.chatInfo.get(o1).size() - 1).getTimestamp());
                Date t2 = new Date(user.chatInfo.get(o2)
                        .get(user.chatInfo.get(o2).size() - 1).getTimestamp());
                if (t1.after(t2)) {
                    return -1;
                }
                if (t1.equals(t2)) {
                    return 0;
                }
                return 1;
            } else if (user.chatInfo.get(o1).size() == 0) {
                return 1;
            } else {
                return -1;
            }
        });
        List<String> pre = new ArrayList<>(user.getChosenChatClientList());
        user.getGroupChatCodeList().sort(String::compareTo);
        pre.addAll(user.getGroupChatCodeList());
        ObservableList<String> chatClientNameList =
                FXCollections.observableArrayList(pre);
        chatList.setItems(chatClientNameList);
    }

    //更新聊天内容
    public void updateChatContent() {
        ObservableList<Message> chatContent =
                FXCollections.observableArrayList(user.getChatInfo().get(user.getChatClientName()));
        chatContentList.setItems(chatContent);
    }

    //更新在线聊天人数
    public void updateCurrentOnlineCnt() {
        currentOnlineCnt.setText(String.valueOf(user.getAllOnlineClient().size()));
    }

    //更新下方聊天标签
    public void updateChatClientNameLabel() {
        if (!user.getChatClientName().startsWith("room")) {
            chatClientName.setText(user.getChatClientName());
        } else {
            StringBuilder chatMembers = new StringBuilder();
            user.getRoomMember().get(user.getChatClientName()).forEach(o -> {
                chatMembers.append("[").append(o).append("] ");
            });
            chatClientName.setText(chatMembers.toString());
        }
    }

    @FXML
    public void changeChatScene() {
        if (chatList.getSelectionModel().getSelectedItem() != null) {
            user.chatClientName = chatList.getSelectionModel().getSelectedItem();
            //右侧聊天框更新
            //chatContent.clear();
            updateChatContent();
            //下方聊天用户更新
            updateChatClientNameLabel();
        }
    }

    @FXML
    public void quitChatScene() throws IOException {
        String selectedItem = chatList.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("WARNING");
            alert.setHeaderText(null);
            alert.setContentText("Do you want to Exit?");
            Optional<ButtonType> exitResult = alert.showAndWait();
            if (exitResult.get() == ButtonType.OK) {
                if (!selectedItem.startsWith("room")) {
                    user.getChosenChatClientList().remove(selectedItem);
                    if (selectedItem.equals(user.getChatClientName())) {
                        user.chatClientName = "Unselected";
                    }
                    updateChatClientNameLabel();
                    updateChatContent();
                    updateChatClientList();
                } else {
                    if (selectedItem.equals(user.getChatClientName())) {
                        user.chatClientName = "Unselected";
                    }
                    user.sendQuitGroupRequest(selectedItem);


                    updateChatClientNameLabel();
                    updateChatContent();
                    updateChatClientList();
                }
            }

        }
    }


    @FXML
    public void createPrivateChat() {
        List<String> currentClientList = user.getAllOnlineClient();

        AtomicReference<String> chooseUserName = new AtomicReference<>();

        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();

        for (String client : currentClientList) {
            if (!client.equals(user.getClientName())) {
                userSel.getItems().add(client);
            }
        }

        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            chooseUserName.set(userSel.getSelectionModel().getSelectedItem());
            stage.close();
        });


        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(userSel, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();

        if (chooseUserName.get() != null) {
            user.setChatClientName(chooseUserName.get());
            if (!user.getChosenChatClientList().contains(chooseUserName.get())) {
                user.getChosenChatClientList().add(chooseUserName.get());
            } else {
                Alert tips = new Alert(Alert.AlertType.INFORMATION);
                tips.setTitle("Info");
                tips.setHeaderText(null);
                tips.setContentText("You have this Chat Client");
                tips.showAndWait();

                user.chatClientName = chooseUserName.get();
                //右侧聊天栏更新
                updateChatContent();
                //左侧聊天栏更新
                updateChatClientList();
                //下方标签显示聊天对象
                updateChatClientNameLabel();
                return;
            }
        } else {
            return;
        }

        //右边聊天框更更新
        List<Message> chat = new ArrayList<>();
        user.getChatInfo().put(user.chatClientName, chat);
        //chatScene.put(user.getChatClientName(),chat);
        updateChatContent();
        //左侧聊天栏更新
        updateChatClientList();
        //下方标签显示聊天对象
        updateChatClientNameLabel();
    }


    /**
     * A new dialog should contain a multi-select list, showing all user's name.
     * You can select several users that will be joined in the group chat, including yourself.
     * The naming rule for group chats is similar to WeChat:
     * If there are > 3 users: display the first three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for example:
     * UserA, UserB, UserC... (10)
     * If there are <= 3 users: do not display the ellipsis, for example:
     * UserA, UserB (2)
     */
    @FXML
    public void createGroupChat() {
        List<String> currentClientList = user.getAllOnlineClient();

        List<String> chooseUserNameList = new ArrayList<>();

        Stage stage = new Stage();

        VBox father = new VBox(10);

        VBox son = new VBox(10);

        for (String client : currentClientList) {
            if (!client.equals(user.getClientName())) {
                CheckBox tempBox = new CheckBox(client);
                son.getChildren().add(tempBox);
            }
        }

        Button okBtn = new Button("OK");

        okBtn.setOnAction(e -> {
            List<CheckBox> list = (List<CheckBox>) (Object) son.getChildren();
            list.forEach(item -> {
                if (item.isSelected()) {
                    chooseUserNameList.add(item.getText());
                }
            });

            Dialog<String> dialog = new TextInputDialog();
            dialog.setTitle("Group Chat");
            dialog.setHeaderText(null);
            dialog.setContentText("Input the RoomCode\nPlease began with \"room\"");
            Optional<String> input = dialog.showAndWait();
            if (input.isPresent() && !input.get().isEmpty() && chooseUserNameList.size() > 0) {
                String getRoomCode = input.get();
                if (getRoomCode.startsWith("room")
                        && !user.getGroupChatCodeList().contains(getRoomCode)) {
                    //todo 确认群聊对象之后
                    user.setChatClientName(getRoomCode);
                    chooseUserNameList.add(user.getClientName());
                    user.getRoomMember().put(getRoomCode, chooseUserNameList);
                    user.getGroupChatCodeList().add(getRoomCode);


                    //左侧聊天栏更新
                    updateChatClientList();
                    //下方标签显示聊天对象
                    updateChatClientNameLabel();
                    //右边聊天框更更新
                    List<Message> chat = new ArrayList<>();
                    user.getChatInfo().put(user.getChatClientName(), chat);
                    //chatScene.put(user.getChatClientName(),chat);
                    updateChatContent();

                    stage.close();
                    return;
                }
            }
            Alert groupCreateError = new Alert(Alert.AlertType.ERROR);
            groupCreateError.setTitle("Error");
            groupCreateError.setHeaderText(null);
            groupCreateError.setContentText("Create fail");
            groupCreateError.showAndWait();
            stage.close();
        });

        father.getChildren().addAll(son, okBtn);
        father.setAlignment(Pos.CENTER);
        father.setPadding(new Insets(20, 20, 20, 20));
        stage.setScene(new Scene(father));
        stage.showAndWait();

    }

    /**
     * Sends the message to the <b>currently selected</b> chat.
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */
    @FXML
    public void doSendMessage() throws IOException {
        if (!user.getChatClientName().equals("Unselected")) {
            Long sendTime = System.currentTimeMillis();
            String sendByName = user.getClientName();
            String content = inputArea.getText();
            if (content != null && !content.equals("")) {
                Message sendMessage;
                if (!user.getChatClientName().startsWith("room")) {
                    sendMessage = new Message(sendTime, sendByName, false);
                    sendMessage.setSendTo(user.getChatClientName());
                    sendMessage.setData(content);
                } else {
                    sendMessage = new Message(sendTime, sendByName, true);
                    ArrayList tempSendTo = new ArrayList(user
                            .getRoomMember().get(user.getChatClientName()));
                    sendMessage.setGroupSendTo(tempSendTo);
                    sendMessage.setData(content);
                    sendMessage.setGroupChatRoomCode(user.getChatClientName());
                }
                user.sendMessage(sendMessage);
                inputArea.setText("");
                //更新聊天界面
                updateChatContent();
                updateChatClientList();
            } else {
                Alert tips = new Alert(Alert.AlertType.INFORMATION);
                tips.setTitle("Info");
                tips.setHeaderText(null);
                tips.setContentText("Don't Input blank Info");
                tips.showAndWait();
            }
        } else {
            Alert tips = new Alert(Alert.AlertType.ERROR);
            tips.setTitle("ERROR");
            tips.setHeaderText(null);
            tips.setContentText("You haven't choose Chat object");
            tips.showAndWait();
            inputArea.setText("");
        }

    }

    /**
     * You may change the cell factory if you changed the design of {@code Message} model.
     * Hint: you may also define a cell factory for the chats displayed in the left panel, or simply override the toString method.
     */

    private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
        @Override
        public ListCell<Message> call(ListView<Message> param) {
            return new ListCell<Message>() {
                @Override
                public void updateItem(Message msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
                        setContentDisplay(ContentDisplay.TEXT_ONLY);
                        setText("");
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label(msg.getSentBy());
                    Label msgLabel = new Label(msg.getData());

                    nameLabel.setPrefSize(50, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: #000000; -fx-border-width: 1px;");

                    if (user.getClientName().equals(msg.getSentBy())) {
                        wrapper.setAlignment(Pos.TOP_RIGHT);
                        wrapper.getChildren().addAll(msgLabel, nameLabel);
                        msgLabel.setPadding(new Insets(0, 20, 0, 0));
                    } else {
                        wrapper.setAlignment(Pos.TOP_LEFT);
                        wrapper.getChildren().addAll(nameLabel, msgLabel);
                        msgLabel.setPadding(new Insets(0, 0, 0, 20));
                    }

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }

}
