package com.hetacz.productmanager.websocket;

import org.jetbrains.annotations.Contract;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private static final String TOPIC = "/topic/";
    private static final String PRODUCT = "product/";
    private static final String CATEGORY = "category/";
    private final SimpMessagingTemplate template;

    @Contract(pure = true)
    public WebSocketController(SimpMessagingTemplate template) {
        this.template = template;
    }

    @MessageMapping("/product/{id}")
    public void sendProductUpdate(@DestinationVariable String id) {
        sendUpdate(TOPIC + PRODUCT + "{id}", id);
    }

    @MessageMapping("/category/{id}")
    public void sendCategoryUpdate(@DestinationVariable String id) {
        sendUpdate(TOPIC + CATEGORY + "{id}", id);
    }

    private void sendUpdate(String message, String id) {
        template.convertAndSend(message, id);
    }
}
