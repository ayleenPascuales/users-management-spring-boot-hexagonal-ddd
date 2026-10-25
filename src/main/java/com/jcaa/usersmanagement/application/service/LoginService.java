package com.jcaa.usersmanagement.application.service;

import com.jcaa.usersmanagement.application.port.in.LoginUseCase;
import com.jcaa.usersmanagement.application.port.out.GetUserByEmailPort;
import com.jcaa.usersmanagement.application.service.dto.command.LoginCommand;
import com.jcaa.usersmanagement.domain.enums.UserStatus;
import com.jcaa.usersmanagement.domain.exception.InvalidCredentialsException;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

  private final GetUserByEmailPort getUserByEmailPort;
  private final Validator validator;

  @Override
  public UserModel execute(final LoginCommand command) {
    validateCommand(command);

    final UserEmail email = new UserEmail(command.email());
    final UserModel user = findUserOrFailWithInvalidCredentials(email);

    verifyPasswordOrFail(command.password(), user);
    ensureUserIsActiveOrFail(user);

    return user;
  }

  private void validateCommand(final LoginCommand command) {
    final Set<ConstraintViolation<LoginCommand>> violations = validator.validate(command);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }

  private UserModel findUserOrFailWithInvalidCredentials(final UserEmail email) {
    return getUserByEmailPort
        .getByEmail(email)
        .orElseThrow(InvalidCredentialsException::becauseCredentialsAreInvalid);
  }

  private static void verifyPasswordOrFail(
        final String plainPassword,
        final UserModel user) {

    log.info("Verificando contraseña del usuario.");

    if (!user.getPassword().verifyPlain(plainPassword)) {
        log.warn("La verificación de contraseña ha fallado.");
        throw InvalidCredentialsException.becauseCredentialsAreInvalid();
    }

    log.info("La contraseña fue verificada correctamente.");
}

  private static void ensureUserIsActiveOrFail(final UserModel user) {
    if (user.getStatus() != UserStatus.ACTIVE) {
      throw InvalidCredentialsException.becauseUserIsNotActive();
    }
  }
}
