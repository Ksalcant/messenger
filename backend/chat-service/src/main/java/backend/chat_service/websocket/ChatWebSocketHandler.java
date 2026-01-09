package backend.chat_service.websocket;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import backend.chat_service.entity.MessageEntity;
import backend.chat_service.repository.MessageRepository;
import backend.chat_service.security.JwtUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * ChatWebSocketHandler
 *
 * - Authenticates WebSocket connections using a JWT at handshake time
 * - Associates the socket session with a userId
 * - Routes messages between connected users
 * - Persists messages to the database (durability)
 *
 * Protocol (JSON):
 * Client sends: { "to": "<receiverUuid>", "content": "hello" }
 */

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler{
    private final JwtUtil jwtUtil;
    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    //userId -> session (Single session for user for MVP)
    private final Map<UUID, WebSocketSession> sessionsByUserId = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(JwtUtil jwtUtil, MessageRepository messageRepository){
        this.jwtUtil = jwtUtil;
        this.messageRepository = messageRepository; 
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{
        String token = extractToken(session);

        if(token == null){
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Missing JWT"));
            return; 
        }

        UUID userId;
        try {
            userId = jwtUtil.extractUserId(token);
        }catch(Exception e){
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid JWT"));
            return;
        }

        session.getAttributes().put("userId", userId);
        sessionsByUserId.put(userId, session);

        System.out.println("WS connected: "+ userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        UUID senderId = (UUID) session.getAttributes().get("userId");
        if(senderId == null){
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Unauthenticated session"));
            return; 
        }

        IncomingChatMessage incoming = objectMapper.readValue(message.getPayload(), IncomingChatMessage.class);
        
        //1) Persiste message 
        MessageEntity saved = new MessageEntity();
        saved.setSenderId(senderId);
        saved.setReceiverId(UUID.fromString(incoming.to()));
        saved.setContent(incoming.content());
        messageRepository.save(saved);

        //2) Route to receiver if online
        WebSocketSession receiverSession = sessionsByUserId.get(saved.getReceiverId());
        if(receiverSession != null && receiverSession.isOpen()){
            receiverSession.sendMessage(
                new TextMessage(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "from", senderId.toString(),
                            "content", saved.getContent(),
                            "createdAt", saved.getCreatedAt().toString()
                        )
                    )
                )
            );
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status){
        UUID userId = (UUID) session.getAttributes().get("userId");
        if(userId != null) {sessionsByUserId.remove(userId);}
        System.out.println("WS disconnected: "+ userId);
    }

    private String extractToken(WebSocketSession session){
        //A) Browser-friendly: ws://host/ws/chat?token=JWT
        URI uri = session.getUri();
        if(uri != null && uri.getQuery() != null){
            for(String pair : uri.getQuery().split("&")){
                String[] parts = pair.split("=");
                if(parts.length == 2 && parts[0].equals("token")){
                    return parts[1]; 
                }
            }
        }
    
        //B) CLI-friendly: Authorization: Bearer JWT
        String authHeader = session.getHandshakeHeaders().getFirst("Authorization");
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            return authHeader.substring(7);
        }
        
        return null; 
    }

    private record IncomingChatMessage(String to, String content) {

    }
}