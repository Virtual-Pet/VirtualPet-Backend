package com.virtualpet.chatbot.context;

import java.util.Optional;
import java.util.UUID;

/**
 * Propaga el userId del usuario autenticado desde el hilo HTTP hacia las tool functions del agente.
 *
 * <p>NOTA: usa ThreadLocal para simplicidad. En código reactivo multi-threaded se debería usar
 * Reactor Context + propagación explícita. Válido aquí porque Spring AI ejecuta las tools de forma
 * sincrónica en el mismo hilo de la petición.
 */
public final class ChatUserContext {

  private static final ThreadLocal<UUID> USER_ID = new ThreadLocal<>();

  private ChatUserContext() {}

  public static void set(UUID userId) {
    USER_ID.set(userId);
  }

  public static Optional<UUID> get() {
    return Optional.ofNullable(USER_ID.get());
  }

  public static UUID requireUserId() {
    UUID id = USER_ID.get();
    if (id == null) {
      throw new IllegalStateException("No hay usuario autenticado en el contexto de chat");
    }
    return id;
  }

  public static void clear() {
    USER_ID.remove();
  }
}
