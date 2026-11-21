package com.jcaa.usersmanagement.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import com.jcaa.usersmanagement.application.port.out.EmailSenderPort;
import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.enums.UserStatus;
import com.jcaa.usersmanagement.domain.exception.EmailSenderException;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.domain.valueobject.UserName;
import com.jcaa.usersmanagement.domain.valueobject.UserPassword;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("EmailNotificationService")
@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock
    private EmailSenderPort emailSenderPort;

    @Mock
    private EmailSenderPort spyEmailSenderPort;

    private EmailNotificationService service;

    private static final String EMAIL = "john@example.com";
    private static final String NAME = "John Arrieta";
    private static final String TEMPLATE_CONTENT =
            "<html>{{name}} {{email}} {{role}} {{status}}</html>";

    private UserModel user;

    @BeforeEach
    void setUp() {
        service = new EmailNotificationService(emailSenderPort);

        user = new UserModel(
                new UserId("u-001"),
                new UserName(NAME),
                new UserEmail(EMAIL),
                UserPassword.fromPlainText("SecurePass1"),
                UserRole.ADMIN,
                UserStatus.ACTIVE
        );
    }

    @Test
    @DisplayName("notifyUserCreated() envía el correo al destinatario correcto")
    void shouldSendCreatedNotificationToCorrectEmail() {

        service.notifyUserCreated(user);

        verify(emailSenderPort).send(
                argThat(dest ->
                        EMAIL.equals(dest.getDestinationEmail())
                                && dest.getSubject().contains("creada")
                )
        );
    }

    @Test
    @DisplayName("notifyUserUpdated() envía el correo al destinatario correcto")
    void shouldSendUpdatedNotificationToCorrectEmail() {

        service.notifyUserUpdated(user);

        verify(emailSenderPort).send(
                argThat(dest ->
                        EMAIL.equals(dest.getDestinationEmail())
                                && dest.getSubject().contains("actualizada")
                )
        );
    }

    @Test
    @DisplayName("notifyUserDeleted() envía el correo al destinatario correcto")
    void shouldSendDeletedNotificationToCorrectEmail() {

        service.notifyUserDeleted(user);

        verify(emailSenderPort).send(
                argThat(dest ->
                        EMAIL.equals(dest.getDestinationEmail())
                                && dest.getSubject().contains("eliminada")
                )
        );
    }

    @Test
    @DisplayName("notifyUserCreated() re-lanza EmailSenderException cuando el puerto falla")
    void shouldRethrowEmailSenderExceptionOnCreate() {

        final EmailSenderException cause =
                EmailSenderException.becauseApiFailed(EMAIL, "Connection refused");

        doThrow(cause).when(emailSenderPort).send(any());

        assertThrows(
                EmailSenderException.class,
                () -> service.notifyUserCreated(user)
        );
    }

    @Test
    @DisplayName("notifyUserUpdated() re-lanza EmailSenderException cuando el puerto falla")
    void shouldRethrowEmailSenderExceptionOnUpdate() {

        final EmailSenderException cause =
                EmailSenderException.becauseApiFailed(EMAIL, "Connection refused");

        doThrow(cause).when(emailSenderPort).send(any());

        assertThrows(
                EmailSenderException.class,
                () -> service.notifyUserUpdated(user)
        );
    }

    @Test
    @DisplayName("notifyUserDeleted() re-lanza EmailSenderException cuando el puerto falla")
    void shouldRethrowEmailSenderExceptionOnDelete() {

        final EmailSenderException cause =
                EmailSenderException.becauseApiFailed(EMAIL, "Connection refused");

        doThrow(cause).when(emailSenderPort).send(any());

        assertThrows(
                EmailSenderException.class,
                () -> service.notifyUserDeleted(user)
        );
    }

    @Test
    @DisplayName("loadTemplate() lanza EmailSenderException cuando el template no existe")
    void shouldThrowWhenTemplateNotFound() {

        final EmailNotificationService serviceWithoutTemplate =
                serviceWithTemplateStream(null);

        assertThrows(
                EmailSenderException.class,
                () -> serviceWithoutTemplate.notifyUserCreated(user)
        );
    }

    @Test
    @DisplayName("loadTemplate() lanza EmailSenderException cuando ocurre IOException")
    void shouldThrowWhenTemplateThrowsIOException() throws IOException {

        try (InputStream brokenStream = mock(InputStream.class)) {

            doThrow(new IOException("Disk error"))
                    .when(brokenStream).readAllBytes();

            final EmailNotificationService serviceWithBrokenTemplate =
                    serviceWithTemplateStream(brokenStream);

            assertThrows(
                    EmailSenderException.class,
                    () -> serviceWithBrokenTemplate.notifyUserCreated(user)
            );
        }
    }

   
  @Test
  @DisplayName("notifyUserCreated() sustituye los tokens y no incluye la contraseña")
  void shouldRenderCreatedTemplateWithoutPassword() throws IOException {

    try (InputStream templateStream =
                 new ByteArrayInputStream(
                         TEMPLATE_CONTENT.getBytes(StandardCharsets.UTF_8)
                 )) {

        final EmailNotificationService serviceWithTemplate =
                serviceWithTemplateStream(templateStream);

        serviceWithTemplate.notifyUserCreated(user);

        verify(spyEmailSenderPort).send(
                argThat(dest ->
                        dest.getBody().contains(NAME)
                                && dest.getBody().contains(EMAIL)
                                && dest.getBody().contains("ADMIN")
                                && dest.getBody().contains("{{status}}")
                                && !dest.getBody().contains("SecurePass1")
                )
        );
    }
  }


    private EmailNotificationService serviceWithTemplateStream(
            final InputStream templateStream) {

        return new EmailNotificationService(spyEmailSenderPort) {
            @Override
            InputStream openResourceStream(final String path) {
                return templateStream;
            }
        };
    }
}
