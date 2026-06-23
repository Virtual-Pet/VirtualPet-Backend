package com.virtualpet.chatbot.controller;

import com.virtualpet.chatbot.service.ChatService;
import com.virtualpet.common.security.UserPrincipal;
import java.util.UUID;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE endpoint del chatbot. Accesible sin auth (guests) y con JWT (autenticados). La respuesta
 * llega completa en un único evento SSE porque la llamada a Gemini es síncrona; el widget frontend
 * la recibe y muestra sin cambios en el protocolo.
 */
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

  private static final Logger log = LoggerFactory.getLogger(ChatController.class);

  private final ChatService chatService;
  private final Executor chatExecutor;

  @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter stream(
      @RequestParam String sessionId,
      @RequestParam String message,
      @AuthenticationPrincipal UserPrincipal currentUser) {

    UUID userId = currentUser != null ? currentUser.getId() : null;
    SseEmitter emitter = new SseEmitter(60_000L);

    chatExecutor.execute(
        () -> {
          try {
            String response = chatService.chat(sessionId, message, userId);
            emitter.send(SseEmitter.event().data(response));
            emitter.complete();
          } catch (Exception e) {
            log.error("Error en chatbot [sessionId={}]: {}", sessionId, e.getMessage(), e);
            try {
              emitter.send(SseEmitter.event().data("Error: " + e.getMessage()));
              emitter.complete();
            } catch (Exception ignored) {
              emitter.completeWithError(e);
            }
          }
        });

    return emitter;
  }
}
