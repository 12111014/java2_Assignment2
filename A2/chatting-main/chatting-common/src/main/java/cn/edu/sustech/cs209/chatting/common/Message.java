package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;
import java.util.List;

public class Message implements Serializable {

    private Boolean ifGroup;

    private Long timestamp;

    private String sentBy;

    private String sendTo;

    private String data;

    private List<String> groupSendTo;

    private String groupChatRoomCode;

    public Message(Long timestamp, String sentBy, boolean ifGroup) {
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.ifGroup = ifGroup;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getSentBy() {
        return sentBy;
    }

    public String getSendTo() {
        return sendTo;
    }

    public String getData() {
        return data;
    }

    public Boolean getIfGroup() {
        return ifGroup;
    }

    public void setIfGroup(Boolean ifGroup) {
        this.ifGroup = ifGroup;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    public void setSendTo(String sendTo) {
        this.sendTo = sendTo;
    }

    public void setData(String data) {
        this.data = data;
    }


    public List<String> getGroupSendTo() {
        return groupSendTo;
    }

    public void setGroupSendTo(List<String> groupSendTo) {
        this.groupSendTo = groupSendTo;
    }


    public String getGroupChatRoomCode() {
        return groupChatRoomCode;
    }

    public void setGroupChatRoomCode(String groupChatRoomCode) {
        this.groupChatRoomCode = groupChatRoomCode;
    }

}
