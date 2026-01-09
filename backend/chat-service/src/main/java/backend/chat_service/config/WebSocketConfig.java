
// add package
@Configuration 
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer{
    private final ChatWebSocketHandler chatWebSocketHandler;
    public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler){
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry){
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
        .setAllowedOrigins("*"); // tighten later
    }
}