package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

public class groupMessage implements Serializable {
    private int roomCode;
    private Long timeStamp;
    private String sendBy;
    private String data;

    public groupMessage(int roomCode, Long timeStamp, String sendBy, String data) {
        this.roomCode = roomCode;
        this.timeStamp = timeStamp;
        this.sendBy = sendBy;
        this.data = data;
    }

    public int getRoomCode() {
        return roomCode;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public String getSendBy() {
        return sendBy;
    }

    public String getData() {
        return data;
    }
}
