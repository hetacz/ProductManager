package com.hetacz.productmanager.websocket;

import org.jetbrains.annotations.Contract;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate template;

    @Contract(pure = true)
    public WebSocketController(SimpMessagingTemplate template) {
        this.template = template;
    }

    @MessageMapping("/product/{id}")
    public void sendProductUpdate(String message, @DestinationVariable String id) {
        template.convertAndSend("/topic/product/" + id, message);
    }

    @MessageMapping("/category/{id}")
    public void sendCategoryUpdate(String message, @DestinationVariable String id) {
        template.convertAndSend("/topic/category/" + id, message);
    }
}
