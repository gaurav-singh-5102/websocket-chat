package com.nagarro.websockets.model;

import java.util.HashMap;
import java.util.List;

public class ConnectMessage {
    private String sender;
    private HashMap<String, String> users;
    private HashMap<String, List<ChatMessage>> history;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public HashMap<String, String> getUsers() {
        return users;
    }

    public void setUsers(HashMap<String, String> users) {
        this.users = users;
    }

    public HashMap<String, List<ChatMessage>> getHistory() {
        return history;
    }

    public void setHistory(HashMap<String, List<ChatMessage>> history) {
        this.history = history;
    }
}
