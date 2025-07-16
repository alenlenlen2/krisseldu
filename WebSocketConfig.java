package Krisseldu.Krisseldu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * Configura WebSocket con STOMP.
 *  • Endpoint de conexión  :  /ws
 *  • Topic de difusión     :  /topic/*
 *  • Prefijo de app        :  /app  (si luego envías mensajes desde el cliente)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /** Registra el endpoint WebSocket accesible desde el navegador. */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")               // →  ws://HOST/ws
                .setAllowedOriginPatterns("*")    // CORS abierto (ajusta en prod)
                .withSockJS();                    // Fallback SockJS
    }

    /** Configura el broker simple en memoria y el prefijo de aplicación. */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");    // Mensajes salientes → /topic/…
        registry.setApplicationDestinationPrefixes("/app"); // Mensajes entrantes
    }
}
