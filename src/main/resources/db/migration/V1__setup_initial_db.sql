-- Table creation DDLs
CREATE TABLE applications (
  id                              BIGINT AUTO_INCREMENT PRIMARY KEY,
  name                            VARCHAR(100) NOT NULL,
  label                           VARCHAR(100) NOT NULL,
  description                     VARCHAR(2000),
  updated_at                      DATETIME
);

CREATE TABLE application_versions (
  id                              BIGINT AUTO_INCREMENT PRIMARY KEY,
  version                         VARCHAR(50) NOT NULL,
  published                       BOOLEAN NOT NULL,
  application_config              TEXT,
  application_id                  BIGINT NOT NULL,
  created_at                      TIMESTAMP,
  CONSTRAINT fk_application_id    FOREIGN KEY (application_id) REFERENCES applications(id)
);