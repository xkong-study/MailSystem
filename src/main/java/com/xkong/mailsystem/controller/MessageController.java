package com.xkong.mailsystem.controller;

import com.xkong.mailsystem.service.SessionManager;
import com.xkong.mailsystem.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@RestController
public class MessageController {
    private final SessionManager sessionManager;

    @Autowired
    public MessageController(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @MessageMapping("/sendMessage")
    public void sendMessage(@Payload Message message) throws Exception {
        // 获取发送方用户名
        String from = message.getFrom();

        // 根据发送方用户名获取WebSocket会话
        WebSocketSession senderSession = sessionManager.getSession(from);

        // 如果会话存在，则将消息发送给发送方
        if (senderSession != null && senderSession.isOpen()) {
            TextMessage textMessage = new TextMessage("Message sent successfully!");
            senderSession.sendMessage(textMessage);
        }
    }
}
