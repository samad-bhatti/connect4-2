package com.connectfour;
//CLIENT SIDE
import java.io.Serializable;

public class Message implements Serializable {
    static final long serialVersionUID = 42L;

    private final String sender; //this is to put the username of the person chatting
    private final String content; //the message itself

    public Message(String sender, String content) {
        this.sender = sender;
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return sender + ": " + content;
    }
}
