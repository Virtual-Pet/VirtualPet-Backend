package com.virtualpet.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtualpet.TestRedisConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestRedisConfiguration.class)
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void registerLoginMePatchLogoutFlow() throws Exception {
    String email = "alice@example.com";
    String password = "Strong-pass1";

    /* register */
    mockMvc
        .perform(
            post("/api/v1/auth/register/customer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "%s",
                      "password": "%s",
                      "firstName": "Alice",
                      "lastName": "Doe"
                    }
                    """
                        .formatted(email, password)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value(email))
        .andExpect(jsonPath("$.role").value("CUSTOMER"));

    /* duplicate register → 409 */
    mockMvc
        .perform(
            post("/api/v1/auth/register/customer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "%s",
                      "password": "%s",
                      "firstName": "Alice",
                      "lastName": "Doe"
                    }
                    """
                        .formatted(email, password)))
        .andExpect(status().isConflict())
        .andExpect(header().string("Content-Type", "application/problem+json"));

    /* login */
    MvcResult loginResult =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        { "email": "%s", "password": "%s" }
                        """
                            .formatted(email, password)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.user.email").value(email))
            .andExpect(jsonPath("$.user.role").value("CUSTOMER"))
            .andReturn();

    JsonNode tokens = objectMapper.readTree(loginResult.getResponse().getContentAsString());
    String accessToken = tokens.get("accessToken").asText();
    String refreshToken = tokens.get("refreshToken").asText();

    /* /me */
    mockMvc
        .perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("Alice"))
        .andExpect(jsonPath("$.lastName").value("Doe"))
        .andExpect(jsonPath("$.role").value("CUSTOMER"));

    /* PATCH /me */
    mockMvc
        .perform(
            patch("/api/v1/auth/me")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Alicia\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("Alicia"))
        .andExpect(jsonPath("$.lastName").value("Doe"));

    /* refresh */
    mockMvc
        .perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").isNotEmpty())
        .andExpect(jsonPath("$.tokenType").value("Bearer"))
        .andExpect(jsonPath("$.expiresIn").isNumber());

    /* logout */
    mockMvc
        .perform(
            post("/api/v1/auth/logout")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
        .andExpect(status().isNoContent());

    /* refresh after logout → 401 */
    mockMvc
        .perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void loginWithBadCredentialsReturnsProblemDetail401() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ghost@example.com\",\"password\":\"Strong-pass1\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(header().string("Content-Type", "application/problem+json"))
        .andExpect(jsonPath("$.title").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void validationFailureReturns422WithErrorsArray() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/register/customer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "not-an-email",
                      "password": "x",
                      "firstName": "",
                      "lastName": ""
                    }
                    """))
        .andExpect(status().is(HttpStatus.UNPROCESSABLE_CONTENT.value()))
        .andExpect(header().string("Content-Type", "application/problem+json"))
        .andExpect(jsonPath("$.errors").isArray());
  }

  @Test
  void protectedEndpointWithoutTokenReturns401() throws Exception {
    MvcResult result =
        mockMvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized()).andReturn();
    assertThat(result.getResponse().getHeader("Content-Type"))
        .startsWith("application/problem+json");
  }

  @Test
  void registerEmployeeRequiresAdminRole() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/register/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "emp@example.com",
                      "password": "Strong-pass1",
                      "firstName": "Bob",
                      "lastName": "Smith"
                    }
                    """))
        .andExpect(status().isUnauthorized());
  }
}
