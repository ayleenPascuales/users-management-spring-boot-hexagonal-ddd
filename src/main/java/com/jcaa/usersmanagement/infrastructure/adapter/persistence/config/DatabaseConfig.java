package com.jcaa.usersmanagement.infrastructure.adapter.persistence.config;

public record DatabaseConfig(
    String host,
    int port,
    String databaseName,
    String username,
    String password,
    String sslMode,
    DatabaseType type) {

  public enum DatabaseType {
    MYSQL,
    POSTGRESQL
  }

  private static final String MYSQL_URL_TEMPLATE =
      "jdbc:mysql://%s:%d/%s?sslMode=%s&serverTimezone=UTC&allowPublicKeyRetrieval=true";

  private static final String POSTGRES_URL_TEMPLATE =
      "jdbc:postgresql://%s:%d/%s?sslmode=%s";

  public String buildJdbcUrl() {
    if (type == DatabaseType.POSTGRESQL) {
      return String.format(POSTGRES_URL_TEMPLATE, host, port, databaseName, sslMode);
    }
    return String.format(MYSQL_URL_TEMPLATE, host, port, databaseName, sslMode);
  }

  public static DatabaseType fromString(final String value) {
    if (value == null || value.isBlank()) {
      return DatabaseType.MYSQL;
    }
    final String normalized = value.trim().toLowerCase();
    if (normalized.equals("postgres") || normalized.equals("postgresql") || normalized.equals("pg")) {
      return DatabaseType.POSTGRESQL;
    }
    return DatabaseType.MYSQL;
  }
}