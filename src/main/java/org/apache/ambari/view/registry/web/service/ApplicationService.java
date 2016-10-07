/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ambari.view.registry.web.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zafarkhaja.semver.Version;
import lombok.extern.slf4j.Slf4j;
import org.apache.ambari.view.registry.internal.exception.ConfigFileParseException;
import org.apache.ambari.view.registry.internal.exception.ConfigSerializationException;
import org.apache.ambari.view.registry.internal.exception.ConfigValidationException;
import org.apache.ambari.view.registry.internal.exception.ConfigVersionMismatchException;
import org.apache.ambari.view.registry.web.config.ApplicationConfig;
import org.apache.ambari.view.registry.web.model.entity.Application;
import org.apache.ambari.view.registry.web.model.entity.ApplicationVersion;
import org.apache.ambari.view.registry.web.model.repository.ApplicationRepository;
import org.apache.ambari.view.registry.web.model.repository.ApplicationVersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service class to work on the application definations
 */
@Service
@Slf4j
public class ApplicationService {

  private final ApplicationRepository applicationRepository;
  private final ApplicationVersionRepository applicationVersionRepository;

  @Autowired
  public ApplicationService(ApplicationRepository applicationRepository, ApplicationVersionRepository applicationVersionRepository) {
    this.applicationRepository = applicationRepository;
    this.applicationVersionRepository = applicationVersionRepository;
  }

  public ApplicationConfig parseApplicationConfig(InputStream stream, String orgFileName) {
    ObjectMapper mapper = new ObjectMapper();
    ApplicationConfig config = null;
    try {
      config = mapper.readValue(stream, ApplicationConfig.class);
    } catch (IOException e) {
      log.error("Failed to parse the stream for application config. {} not a valid json file. Exception: {}", orgFileName, e);
      throw new ConfigFileParseException(orgFileName, e);
    }
    validate(config, orgFileName);
    return config;
  }

  private void validate(ApplicationConfig config, String orgFileName) {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();
    Set<ConstraintViolation<ApplicationConfig>> contraints = validator.validate(config);
    if (contraints.size() != 0) {
      log.info("Constraints: {}", contraints);
      log.error("Failed to validate application config defined in {}. Exceptions: {}",
          orgFileName, contraints);
      throw new ConfigValidationException(orgFileName, contraints);
    }
  }

  @Transactional
  public ApplicationVersion saveApplicationConfig(ApplicationConfig config) {
    Optional<Application> applicationOptional = applicationRepository.findByName(config.getName());

    Application application = applicationOptional.orElseGet(() -> {
      Application app = new Application();
      app = new Application();
      app.setName(config.getName());
      return app;
    });

    application.setDescription(config.getDescription());
    application.setLabel(config.getLabel());
    application.setUpdatedAt(new Date());


    validateVersionIsGreater(config, application);

    ApplicationVersion version = new ApplicationVersion();
    version.setApplication(application);
    try {
      version.setApplicationConfig(getJsonString(config));
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize configuration to JSON string for {}", config);
      throw new ConfigSerializationException(e);
    }

    version.setVersion(config.getVersion());
    version.setCreatedAt(new Date());

    application.getVersions().add(version);
    applicationRepository.save(application);

    return applicationVersionRepository.save(version);
  }

  private void validateVersionIsGreater(ApplicationConfig config, Application application) {
    Optional<ApplicationVersion> maxVersionOptional =
          application.getVersions().stream().max(
              (v1, v2) -> Version.valueOf(v1.getVersion()).compareTo(Version.valueOf(v2.getVersion()))
          );

    if (maxVersionOptional.isPresent()) {
      ApplicationVersion maxVersion = maxVersionOptional.get();
      if (Version.valueOf(config.getVersion()).lessThanOrEqualTo(Version.valueOf(maxVersion.getVersion()))) {
        String errMessage = String.format("'%s' should be greater than exiting max version '%s' for application '%s'",
            config.getVersion(), maxVersion.getVersion(), config.getName());
        log.error(errMessage);
        throw new ConfigVersionMismatchException(errMessage);
      }
    }
  }

  private String getJsonString(ApplicationConfig config) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(config);
  }

  public List<Application> findApplications(Optional<String> appNameLike, Optional<Long> after) {
    List<Application> applications = new ArrayList<>();
    if (appNameLike.isPresent() && after.isPresent()) {
      applications = applicationRepository.findByNameLikeAndUpdatedAtGreaterThan("%" + appNameLike.get() + "%",
          new Date(after.get()));
    } else if(appNameLike.isPresent()) {
        applications = applicationRepository.findByNameLike("%" + appNameLike.get() + "%");
    } else if(after.isPresent()) {
      applications = applicationRepository.findByUpdatedAtGreaterThan(new Date(after.get()));
    }
    return applications;
  }

  public Optional<Application> findByName(String appName) {
    return applicationRepository.findByName(appName);
  }
}
