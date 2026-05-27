package com.virtualpet.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Translates exceptions into RFC 7807 problem responses (Content-Type: application/problem+json).
 * Adds correlation fields (timestamp, requestId) and field-level errors for validation failures.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String BASE_TYPE = "https://api.virtualpet.com/problems/";

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ProblemDetail> handleApi(ApiException ex, HttpServletRequest req) {
    if (ex.getStatus().is5xxServerError()) {
      log.warn("API exception {}: {}", ex.getStatus().value(), ex.getMessage());
    }
    return respond(problem(ex.getStatus(), ex.getMessage(), req));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleValidation(
      MethodArgumentNotValidException ex, HttpServletRequest req) {
    ProblemDetail body = problem(HttpStatus.UNPROCESSABLE_CONTENT, "Validation failed", req);
    List<FieldErrorEntry> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> new FieldErrorEntry(fe.getField(), defaultMessage(fe)))
            .toList();
    body.setProperty("errors", errors);
    return respond(body);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ProblemDetail> handleUnreadable(
      HttpMessageNotReadableException ex, HttpServletRequest req) {
    return respond(problem(HttpStatus.BAD_REQUEST, "Malformed request body", req));
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<ProblemDetail> handleMissingHeader(
      MissingRequestHeaderException ex, HttpServletRequest req) {
    return respond(
        problem(HttpStatus.BAD_REQUEST, "Missing required header: " + ex.getHeaderName(), req));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ProblemDetail> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
    return respond(
        problem(HttpStatus.BAD_REQUEST, "Invalid value for parameter '" + ex.getName() + "'", req));
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ProblemDetail> handleMethodNotAllowed(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
    return respond(problem(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), req));
  }

  @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
  public ResponseEntity<ProblemDetail> handleAuth(Exception ex, HttpServletRequest req) {
    return respond(problem(HttpStatus.UNAUTHORIZED, "Invalid credentials", req));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ProblemDetail> handleForbidden(
      AccessDeniedException ex, HttpServletRequest req) {
    return respond(problem(HttpStatus.FORBIDDEN, "Access denied", req));
  }

  @ExceptionHandler({NoResourceFoundException.class, NoSuchElementException.class})
  public ResponseEntity<ProblemDetail> handleNotFound(Exception ex, HttpServletRequest req) {
    return respond(problem(HttpStatus.NOT_FOUND, "Resource not found", req));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleGeneric(Exception ex, HttpServletRequest req) {
    log.error("Unhandled exception", ex);
    return respond(
        problem(
            HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error: " + ex.getMessage(), req));
  }

  private ProblemDetail problem(HttpStatus status, String detail, HttpServletRequest req) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
    pd.setTitle(status.getReasonPhrase());
    pd.setType(URI.create(BASE_TYPE + slug(status)));
    pd.setInstance(URI.create(req.getRequestURI()));
    pd.setProperty("timestamp", Instant.now().toString());
    String requestId = MDC.get("requestId");
    if (requestId != null) {
      pd.setProperty("requestId", requestId);
    }
    return pd;
  }

  private ResponseEntity<ProblemDetail> respond(ProblemDetail body) {
    return ResponseEntity.status(body.getStatus())
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(body);
  }

  private static String slug(HttpStatus status) {
    return status.name().toLowerCase().replace('_', '-');
  }

  private static String defaultMessage(FieldError fe) {
    return fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage();
  }

  public record FieldErrorEntry(String field, String issue) {}
}
