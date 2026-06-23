package com.virtualpet.chatbot.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/** Llama a la API REST de Gemini (Google AI Studio) con Java HttpClient nativo. */
@Component
public class GeminiClient {

  private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

  private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final ObjectMapper objectMapper;

  @Value("${app.gemini.api-key}")
  private String apiKey;

  @Value("${app.gemini.model:gemini-2.5-flash}")
  private String model;

  public GeminiClient(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  private static final int MAX_RETRIES = 3;

  public GeminiDTO.ChatResponse chat(GeminiDTO.ChatRequest request)
      throws IOException, InterruptedException {
    String url = BASE_URL + model + ":generateContent?key=" + apiKey;
    String body = objectMapper.writeValueAsString(request);

    var httpRequest =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

    for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
      HttpResponse<String> response =
          httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 200) {
        log.warn("Gemini raw response: {}", response.body().replace("\n", "").replace("  ", " "));
        return objectMapper.readValue(response.body(), GeminiDTO.ChatResponse.class);
      }
      if (response.statusCode() == 503 && attempt < MAX_RETRIES) {
        Thread.sleep(1000L * attempt); // 1s, 2s
        continue;
      }
      throw new RuntimeException(
          "Gemini API error " + response.statusCode() + ": " + response.body());
    }
    throw new IllegalStateException("unreachable");
  }
}
