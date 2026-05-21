package com.virtualpet.backend.auth.service;

import com.virtualpet.backend.shared.config.VirtualPetProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetMailService {

  private final JavaMailSender mailSender;
  private final VirtualPetProperties properties;

  public void sendResetLink(String toEmail, String resetUrl) {
    if (!isMailConfigured()) {
      log.warn(
          "Gmail no configurado (GMAIL_USERNAME / GMAIL_APP_PASSWORD). Link de recuperación para {}: {}",
          toEmail,
          resetUrl);
      return;
    }

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(properties.getMail().getFrom());
    message.setTo(toEmail);
    message.setSubject("Virtual Pet — Restablecer contraseña");
    message.setText(
        """
                Hola,

                Recibimos una solicitud para restablecer tu contraseña en Virtual Pet.

                Abrí este enlace (válido por 1 hora):
                %s

                Si no pediste este cambio, ignorá este correo.

                Virtual Pet — Mar del Plata
                """
            .formatted(resetUrl));

    mailSender.send(message);
  }

  private boolean isMailConfigured() {
    VirtualPetProperties.Mail mail = properties.getMail();
    return StringUtils.hasText(mail.getFrom());
  }
}
