package com.virtualpet.chatbot.tools;

import com.virtualpet.chatbot.client.GeminiDTO;
import com.virtualpet.orders.service.OrderService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Define las tools del agente y ejecuta cada una delegando en OrderService. */
@Component
@RequiredArgsConstructor
public class ChatToolRegistry {

  private final OrderService orderService;

  public List<GeminiDTO.Tool> getToolDefinitions() {
    var getMyOrders =
        new GeminiDTO.FunctionDeclaration(
            "getMyOrders",
            "Lista las órdenes activas (no canceladas) del usuario autenticado. "
                + "Usarla cuando el usuario no sabe su orderId o quiere ver sus pedidos.",
            Map.of("type", "object", "properties", Map.of(), "required", List.of()));

    var requestInvoice =
        new GeminiDTO.FunctionDeclaration(
            "requestInvoice",
            "Registra la solicitud de factura para una orden con el CUIT indicado. "
                + "Solo llamarla cuando el usuario haya confirmado explícitamente la orden y el CUIT.",
            Map.of(
                "type", "object",
                "properties",
                    Map.of(
                        "orderId",
                            Map.of("type", "string", "description", "UUID de la orden a facturar"),
                        "cuit",
                            Map.of(
                                "type",
                                "string",
                                "description",
                                "CUIT del cliente, formato XX-XXXXXXXX-X")),
                "required", List.of("orderId", "cuit")));

    return List.of(new GeminiDTO.Tool(List.of(getMyOrders, requestInvoice)));
  }

  public String execute(String functionName, Map<String, Object> args, UUID userId) {
    return switch (functionName) {
      case "getMyOrders" -> runGetMyOrders(userId);
      case "requestInvoice" -> runRequestInvoice(args, userId);
      default -> "Función desconocida: " + functionName;
    };
  }

  private String runGetMyOrders(UUID userId) {
    try {
      var orders = orderService.getActiveOrdersForChatbot(userId);
      if (orders.isEmpty()) return "El usuario no tiene órdenes activas.";
      var sb = new StringBuilder();
      for (var o : orders) {
        String shortId = o.orderId().toString().substring(0, 8).toUpperCase();
        sb.append("ID completo: ").append(o.orderId())
            .append(" (mostrado al usuario como #").append(shortId).append(")")
            .append(" | Estado: ").append(o.status())
            .append(" | Total: $").append(o.total())
            .append(" | Fecha: ").append(o.createdAt()).append("\n");
      }
      return sb.toString();
    } catch (Exception e) {
      return "Error al obtener las órdenes: " + e.getMessage();
    }
  }

  private String runRequestInvoice(Map<String, Object> args, UUID userId) {
    try {
      String orderId = (String) args.get("orderId");
      String cuit = (String) args.get("cuit");
      orderService.requestInvoice(UUID.fromString(orderId), cuit, userId);
      return "Solicitud de factura registrada para la orden " + orderId + " al CUIT " + cuit + ".";
    } catch (Exception e) {
      return "Error al registrar la solicitud: " + e.getMessage();
    }
  }
}
