package cn.edu.sustech.cs209.chatting.common;

import java.util.List;

public class Room {
    String roomCode;
    List<String> roomMember;
    List<GroupMessage> roomChatInfo;

    public Room(String roomCode) {
        this.roomCode = roomCode;
    }
}
