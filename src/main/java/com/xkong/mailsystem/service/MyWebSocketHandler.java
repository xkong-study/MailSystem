package com.xkong.mailsystem.service;

import com.xkong.mailsystem.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class MyWebSocketHandler extends TextWebSocketHandler {
    private final SessionManager sessionManager;

    @Autowired
    public MyWebSocketHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        String username = getUsernameFromQuery(session);
        if(StringUtils.hasText(username)){
            sessionManager.addSession(username, session);
        } else{
            session.close();
        }
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws IOException {
        // parse message
        Message receivedMessage = parseReceivedMessage(message.getPayload());
        // get recipient session
        String to = receivedMessage.getTo();
        String receiveMessagePayload = receivedMessage.getFrom() + ";" + receivedMessage.getContent() + ";" + receivedMessage.getTime();
        if(StringUtils.hasText(to) && to.equals("all")){
            sessionManager.getAllSessions().forEach(session1 -> {
                try{
                    if(session1 != null && session1.isOpen()){
                        session1.sendMessage(new TextMessage(receiveMessagePayload));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            return;
        }
        WebSocketSession recipientSession = sessionManager.getSession(to);
        // send message to recipient
        if (recipientSession != null && recipientSession.isOpen()) {
            recipientSession.sendMessage(new TextMessage(receiveMessagePayload));
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        String username = getUsernameFromQuery(session);
        if(StringUtils.hasText(username)){
            sessionManager.removeSession(username);
        } else{
            session.close();
        }
    }

    private Message parseReceivedMessage(String payload) {
        String[] message = payload.split(";");
        return new Message(message[0], message[1], message[2], LocalDateTime.parse(message[3]));
    }

    private String getUsernameFromQuery(WebSocketSession session) {
        URI uri = session.getUri();
        if(Objects.isNull(uri)){
            return null;
        }
        String query = uri.getQuery();
        if(Objects.isNull(query)){
            return null;
        }
        String[] split = query.split("=");
        if(split.length != 2){
            return null;
        }
        return split[1];
    }
}

