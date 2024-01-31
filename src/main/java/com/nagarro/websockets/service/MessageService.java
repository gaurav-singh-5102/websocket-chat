package com.nagarro.websockets.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nagarro.websockets.exception.ChatMessageValidationException;
import com.nagarro.websockets.model.ChatMessage;

import jakarta.annotation.PreDestroy;

@Service
public class MessageService {

    private HashMap<String, HashMap<String, List<ChatMessage>>> messages = new HashMap<>();

    public void saveMessage(ChatMessage chatMessage) throws ChatMessageValidationException {
        validateChatMessage(chatMessage);
        messages.computeIfAbsent(chatMessage.getSender(), k -> new HashMap<>())
                .computeIfAbsent(chatMessage.getReceiver(), k -> new ArrayList<>())
                .add(chatMessage);
        messages.computeIfAbsent(chatMessage.getReceiver(), k -> new HashMap<>())
                .computeIfAbsent(chatMessage.getSender(), k -> new ArrayList<>())
                .add(chatMessage);
    }

    private void validateChatMessage(ChatMessage chatMessage) throws ChatMessageValidationException {

        if (chatMessage.getContent() == null || chatMessage.getSender() == null || chatMessage.getReceiver() == null
                || chatMessage.getType() == null || chatMessage.getTimestamp() == null) {
            throw new ChatMessageValidationException();
        }

    }

    public HashMap<String, List<ChatMessage>> getMessages(String username) {
        loadMessagesFromFile(username);
        return messages.getOrDefault(username, new HashMap<>());
    }

    // Save messages to a JSON file for a specific user
    public void saveMessagesToFile(String user) {
        try {
            System.out.println("Creating backup for " + user);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            File backupFolder = new File("backups");
            if (!backupFolder.exists()) {
                backupFolder.mkdir();
            }

            File userBackupFile = new File(backupFolder, user + "_backup.json");
            if (!userBackupFile.exists()) {
                userBackupFile.createNewFile();
            }
            objectMapper.writeValue(userBackupFile, getMessages(user));
            messages.remove(user);
        } catch (IOException e) {
            System.err.println("Could not create backup for " + user + "!");
        }
    }

    // Load messages from a JSON file for a specific user
    public void loadMessagesFromFile(String user) {
        try {
            System.out.println("Loading backup for " + user);
            ObjectMapper objectMapper = new ObjectMapper();
            File userBackupFile = new File("backups/" + user + "_backup.json");

            if (userBackupFile.exists()) {
                messages.put(user,
                        objectMapper.readValue(userBackupFile, new TypeReference<HashMap<String, List<ChatMessage>>>() {
                        }));
            }
        } catch (IOException e) {
            System.err.println("Could not load backup for " + user + "!");
        }
    }

    @PreDestroy
    private void saveBackupsOnShutdown() {
        for (String key : messages.keySet()) {
            saveMessagesToFile(key);
        }
    }
}
