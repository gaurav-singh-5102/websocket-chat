package com.nagarro.websockets.controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import com.nagarro.websockets.exception.ChatMessageValidationException;
import com.nagarro.websockets.model.ChatMessage;
import com.nagarro.websockets.model.ConnectMessage;
import com.nagarro.websockets.service.MessageService;

@Controller
public class ChatController {

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;
    @Autowired
    private MessageService messageService;
    private HashMap<String, String> users = new HashMap<>();

    @MessageMapping("/chat.join")
    @SendTo("/topic/join")
    public ConnectMessage join(@Payload ConnectMessage connectMessage, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", connectMessage.getSender());
        users.put(connectMessage.getSender(), "Active");
        connectMessage.setHistory(messageService.getMessages(connectMessage.getSender()));
        connectMessage.setUsers(users);
        return connectMessage;
    }

    @MessageMapping("/chat.register")
    public void register(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // TODO : Change username to user id when integrating with main app.

        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());

        // Send the registration message to the private queue of the receiving user
        messagingTemplate.convertAndSend(getDestination(chatMessage.getReceiver()), chatMessage);

    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage chatMessage)
            throws ChatMessageValidationException {
        // For one-to-one messages, send to the private queue of the receiving user
        messageService.saveMessage(chatMessage);
        messagingTemplate.convertAndSend(getDestination(chatMessage.getReceiver()), chatMessage);
    }

    @MessageMapping("/chat.unregister")
    @SendTo("/topic/join")
    public ConnectMessage leave(@Payload ConnectMessage connectMessage, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().remove("username");
        messageService.saveMessagesToFile(connectMessage.getSender());
        users.put(connectMessage.getSender(), "Inactive");
        connectMessage.setUsers(users);
        return connectMessage;
    }

    private String getDestination(String user) {
        return String.format("/user/%s/private", user);
    }
}
