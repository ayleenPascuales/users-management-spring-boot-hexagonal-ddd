package com.jcaa.usersmanagement.infrastructure.adapter.persistence.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.jcaa.usersmanagement.infrastructure.adapter.persistence.config.DatabaseConfig.DatabaseType;
import org.junit.jupiter.api.Test;

class DatabaseConfigTest {

  private static final String HOST = "mysql.example.com";
  private static final int PORT = 15425;
  private static final String DATABASE = "crud_usuarios";
  private static final String USERNAME = "avnadmin";
  private static final String PASSWORD = "secret";
  private static final String SSL_MODE = "REQUIRED";

  @Test
  void shouldBuildJdbcUrlWithConfiguredSslMode() {
    // Arrange
    final DatabaseConfig config =
        new DatabaseConfig(
            HOST, PORT, DATABASE, USERNAME, PASSWORD, SSL_MODE, DatabaseType.MYSQL);

    // Act
    final String jdbcUrl = config.buildJdbcUrl();

    // Assert
    assertThat(jdbcUrl)
        .isEqualTo(
            "jdbc:mysql://mysql.example.com:15425/crud_usuarios"
                + "?sslMode=REQUIRED&serverTimezone=UTC&allowPublicKeyRetrieval=true");
  }

  @Test
  void shouldBuildPostgresJdbcUrl() {
    // Arrange
    final DatabaseConfig config =
        new DatabaseConfig(
            "postgres.example.com", 5432, DATABASE, USERNAME, PASSWORD, "disable", DatabaseType.POSTGRESQL);

    // Act
    final String jdbcUrl = config.buildJdbcUrl();

    // Assert
    assertThat(jdbcUrl)
        .isEqualTo("jdbc:postgresql://postgres.example.com:5432/crud_usuarios?sslmode=disable");
  }

  @Test
  void shouldDefaultToMysqlWhenTypeIsMissing() {
    // Act & Assert
    assertThat(DatabaseConfig.fromString(null)).isEqualTo(DatabaseType.MYSQL);
    assertThat(DatabaseConfig.fromString("")).isEqualTo(DatabaseType.MYSQL);
    assertThat(DatabaseConfig.fromString("postgresql")).isEqualTo(DatabaseType.POSTGRESQL);
    assertThat(DatabaseConfig.fromString("postgres")).isEqualTo(DatabaseType.POSTGRESQL);
    assertThat(DatabaseConfig.fromString("mysql")).isEqualTo(DatabaseType.MYSQL);
  }
}
